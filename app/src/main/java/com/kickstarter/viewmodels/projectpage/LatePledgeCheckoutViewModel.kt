package com.kickstarter.viewmodels.projectpage

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.checkout.data.LatePledgePaymentProcessor
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Backing
import com.kickstarter.models.Checkout
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.kickstarter.features.checkout.data.LatePledgeCheckoutUIState as DomainLatePledgeCheckoutUIState

typealias LatePledgeCheckoutUIState = DomainLatePledgeCheckoutUIState

/**
 * ViewModel coordinating late pledge checkout operations.
 *
 * Deconstructed to satisfy AX Rule 1 (Strangler Fig) & Rule 2 (Dev-Primitives < 300 LOC),
 * delegating network and payment mutations to [LatePledgePaymentProcessor].
 */
@Deprecated(
    message = "Legacy monolithic path replaced by LatePledgeCheckoutFeatureViewModel under com.kickstarter.features.checkout.viewmodel.",
    replaceWith = ReplaceWith("LatePledgeCheckoutFeatureViewModel", "com.kickstarter.features.checkout.viewmodel.LatePledgeCheckoutFeatureViewModel")
)
class LatePledgeCheckoutViewModel(val environment: Environment) : ViewModel() {

    private var pledgeData: PledgeData? = null
    private var checkoutData: CheckoutData? = null
    private val analytics = requireNotNull(environment.analytics())
    private val statsigClient = requireNotNull(environment.statsigClient())

    private val paymentProcessor = LatePledgePaymentProcessor(
        requireNotNull(environment.apolloClientV2()),
        requireNotNull(environment.stripe())
    )

    private var storedCards: List<StoredCard> = listOf()
    private var userEmail: String = ""
    private var checkoutId: String? = null
    private var backing: Backing? = null
    private val selectedRewards = mutableListOf<Reward>()
    private var buttonEnabled = true

    private var clientSecretForNewCard: String = ""
    private var newStoredCard: StoredCard? = null
    private var errorAction: (message: String?) -> Unit = {}

    private var clientSecretFor3DSVerification: String = ""
    private var selectedCardFor3DSVerification: StoredCard? = null

    private var selectedReward: Reward? = null
    private var mutableLatePledgeCheckoutUIState = MutableStateFlow(LatePledgeCheckoutUIState())

    private var paymentIntent: String? = null
    private var pledgeButtonClickedJob: Job? = null

    private var scope: CoroutineScope = viewModelScope
    private var dispatcher: CoroutineDispatcher = Dispatchers.IO

    private var mutableOnPledgeSuccessAction = MutableSharedFlow<Boolean>()
    val onPledgeSuccess: SharedFlow<Boolean>
        get() = mutableOnPledgeSuccessAction.asSharedFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    val latePledgeCheckoutUIState: StateFlow<LatePledgeCheckoutUIState>
        get() = mutableLatePledgeCheckoutUIState.asStateFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LatePledgeCheckoutUIState(isLoading = false)
        )

    private val mutableCheckoutPayment = MutableStateFlow(CheckoutPayment(id = 0L, paymentUrl = null, backing = null))
    val checkoutPayment: StateFlow<CheckoutPayment>
        get() = mutableCheckoutPayment.asStateFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = CheckoutPayment(id = 0L, paymentUrl = null, null)
        )

    private var mutableClientSecretForNewPaymentMethod = MutableSharedFlow<String>()
    val clientSecretForNewPaymentMethod: SharedFlow<String>
        get() = mutableClientSecretForNewPaymentMethod.asSharedFlow()

    private var mutablePaymentRequiresAction = MutableSharedFlow<String>()
    val paymentRequiresAction: SharedFlow<String>
        get() = mutablePaymentRequiresAction.asSharedFlow()

    fun getCheckoutData() = checkoutData
    fun getPledgeData() = pledgeData

    fun provideCheckoutIdAndBacking(checkoutId: Long, backing: Backing) {
        this.checkoutId = checkoutId.toString()
        this.backing = backing
    }

    fun onAddNewCardClicked(project: Project) {
        scope.launch(dispatcher) {
            paymentProcessor.createSetupIntent(
                project = project,
                onStart = { emitCurrentState(isLoading = true) },
                onSuccess = { clientSecret ->
                    buttonEnabled = true
                    clientSecretForNewCard = clientSecret
                    mutableClientSecretForNewPaymentMethod.emit(clientSecretForNewCard)
                },
                onFailure = { resetPledgeButtonUIOnFailure() },
                onComplete = { emitCurrentState() }
            )
        }
    }

    fun onNewCardSuccessfullyAdded() {
        scope.launch(dispatcher) {
            paymentProcessor.savePaymentMethod(
                clientSecret = clientSecretForNewCard,
                onStart = { emitCurrentState(isLoading = true) },
                onSuccess = { cards -> storedCards = cards },
                onFailure = { resetPledgeButtonUIOnFailure() },
                onComplete = { emitCurrentState() }
            )
        }
    }

    fun onNewCardFailed() {
        scope.launch {
            emitCurrentState()
            resetPledgeButtonUIOnFailure()
        }
    }

    fun onPledgeButtonClicked(selectedCard: StoredCard?) {
        if (pledgeButtonClickedJob?.isActive.isTrue()) return

        this.pledgeData?.let {
            pledgeButtonClickedJob = scope.launch(dispatcher) {
                val project = it.projectData().project()
                buttonEnabled = false
                emitCurrentState(isLoading = true)

                if (paymentIntent == null) {
                    createPaymentIntentForCheckout(selectedCard, project, it.checkoutTotalAmount())
                } else {
                    paymentIntent?.let { pi ->
                        selectedCard?.let { card -> validateCheckout(pi, card) }
                    }
                }
            }
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    private suspend fun createPaymentIntentForCheckout(selectedCard: StoredCard?, project: Project, totalAmount: Double) {
        if (paymentIntent.isNotNull()) return
        val currentCheckoutId = checkoutId
        val currentBacking = backing
        if (currentCheckoutId != null && currentBacking != null) {
            paymentProcessor.createPaymentIntent(
                project = project,
                amount = totalAmount,
                checkoutId = currentCheckoutId,
                backing = currentBacking,
                onSuccess = { clientSecret ->
                    paymentIntent = clientSecret
                    if (selectedCard != null) {
                        validateCheckout(clientSecret, selectedCard)
                    } else {
                        emitCurrentState()
                        resetPledgeButtonUIOnFailure()
                    }
                },
                onFailure = {
                    paymentIntent = null
                    resetPledgeButtonUIOnFailure()
                }
            )
        } else {
            buttonEnabled = true
            paymentIntent = null
            emitCurrentState()
            errorAction.invoke(null)
        }
    }

    private suspend fun validateCheckout(clientSecret: String, selectedCard: StoredCard) {
        paymentProcessor.validateAndConfirmPayment(
            checkoutId = checkoutId ?: "",
            clientSecret = clientSecret,
            selectedCard = selectedCard,
            onRequiresAction = { secret ->
                clientSecretFor3DSVerification = secret
                selectedCardFor3DSVerification = selectedCard
                mutablePaymentRequiresAction.emit(secret)
                emitCurrentState()
            },
            onPaymentError = { errorMsg ->
                emitCurrentState()
                resetPledgeButtonUIOnFailure(errorMsg)
            },
            onSuccess = {
                completeOnSessionCheckout(clientSecret, selectedCard)
            },
            onValidationFailure = { errorMsg -> resetPledgeButtonUIOnFailure(errorMsg) },
            onComplete = { emitCurrentState() }
        )
    }

    private suspend fun completeOnSessionCheckout(clientSecret: String, selectedCard: StoredCard) {
        paymentProcessor.completeOnSessionCheckout(
            checkoutId = checkoutId ?: "",
            clientSecret = clientSecret,
            selectedCard = selectedCard,
            newStoredCard = newStoredCard,
            onRequiresAction = { secret -> mutablePaymentRequiresAction.emit(secret) },
            onPledgeSuccess = { mutableOnPledgeSuccessAction.emit(true) },
            onFailure = { resetPledgeButtonUIOnFailure() },
            onComplete = { emitCurrentState() }
        )
    }

    fun completeOnSessionCheckoutFor3DS() {
        scope.launch(dispatcher) {
            if (clientSecretFor3DSVerification.isNotEmpty() && selectedCardFor3DSVerification.isNotNull()) {
                paymentProcessor.completeOnSessionCheckout(
                    checkoutId = checkoutId ?: "",
                    clientSecret = clientSecretFor3DSVerification,
                    selectedCard = selectedCardFor3DSVerification,
                    newStoredCard = newStoredCard,
                    onRequiresAction = { secret -> mutablePaymentRequiresAction.emit(secret) },
                    onPledgeSuccess = { mutableOnPledgeSuccessAction.emit(true) },
                    onFailure = {
                        resetPledgeButtonUIOnFailure()
                        clear3DSValues()
                    },
                    onComplete = { emitCurrentState() }
                )
            } else {
                resetPledgeButtonUIOnFailure()
                clear3DSValues()
            }
        }
    }

    fun clear3DSValues() {
        buttonEnabled = true
        clientSecretFor3DSVerification = ""
        selectedCardFor3DSVerification = null
        scope.launch { emitCurrentState() }
    }

    private fun createCheckoutData(shippingAmount: Double, total: Double, bonusAmount: Double?, checkout: Checkout? = null): CheckoutData {
        return CheckoutData.builder()
            .amount(total)
            .id(checkout?.id())
            .paymentType(CreditCardPaymentType.CREDIT_CARD)
            .bonusAmount(bonusAmount)
            .shippingAmount(shippingAmount)
            .build()
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false) {
        mutableLatePledgeCheckoutUIState.emit(
            LatePledgeCheckoutUIState(
                storeCards = storedCards,
                userEmail = userEmail,
                isLoading = isLoading,
                selectedRewards = selectedRewards.toList(),
                shippingAmount = this.pledgeData?.shippingCostIfShipping() ?: 0.0,
                checkoutTotal = this.pledgeData?.checkoutTotalAmount() ?: 0.0,
                isPledgeButtonEnabled = buttonEnabled && !isLoading,
                projectCountryCode = this.pledgeData?.projectData()?.project()?.countryCode(),
                projectCurrency = this.pledgeData?.projectData()?.project()?.currency()
            )
        )
    }

    fun resetPledgeButtonUIOnFailure(errorMessage: String? = null) {
        buttonEnabled = true
        scope.launch { emitCurrentState(isLoading = false) }
        errorAction.invoke(errorMessage)
    }

    fun sendPageViewedEvent() {
        val pData = pledgeData ?: return
        val cData = checkoutData ?: return
        if (selectedReward != null) {
            analytics.trackCheckoutScreenViewed(checkoutData = cData, pledgeData = pData)
        }
    }

    fun sendSubmitCTAEvent() {
        val pData = pledgeData ?: return
        val cData = checkoutData ?: return
        analytics.trackLatePledgeSubmitCTA(checkoutData = cData, pledgeData = pData)
    }

    fun userRewardSelection(reward: Reward) {
        this.selectedReward = reward
    }

    fun loading() {
        scope.launch { emitCurrentState(isLoading = true) }
    }

    @VisibleForTesting
    internal fun provideScopeAndDispatcher(scope: CoroutineScope, dispatcher: CoroutineDispatcher) {
        this.scope = scope
        this.dispatcher = dispatcher
    }

    fun providePledgeData(pledgeData: PledgeData) {
        this.pledgeData = pledgeData
        this.checkoutData = createCheckoutData(
            pledgeData.shippingCostIfShipping(),
            pledgeData.pledgeAmountTotal(),
            pledgeData.bonusAmount()
        )
        selectedRewards.clear()
        pledgeData.addOns()?.let { addOns ->
            selectedRewards.add(pledgeData.reward())
            selectedRewards.addAll(addOns)
        }
        scope.launch(dispatcher) {
            paymentProcessor.loadUserInfo(
                currentUserStream = environment.currentUserV2(),
                onStart = { emitCurrentState(isLoading = true) },
                onSuccess = { email, cards ->
                    userEmail = email
                    storedCards = cards
                    emitCurrentState()
                },
                onError = {
                    errorAction.invoke(null)
                    emitCurrentState()
                }
            )

            scope.launch(dispatcher) {
                paymentProcessor.createCheckout(
                    pledgeData = pledgeData,
                    onStart = { emitCurrentState(isLoading = false) },
                    onSuccess = { checkoutPayment -> mutableCheckoutPayment.emit(checkoutPayment) },
                    onFailure = { msg -> resetPledgeButtonUIOnFailure(msg) },
                    onComplete = { emitCurrentState() },
                    onErrorAction = { errorAction.invoke(null) }
                )
            }
        }
    }

    fun isPaymentSheetGooglePayEnabled(): Boolean =
        statsigClient.configReady.value && statsigClient.checkGate(StatsigGateKey.ANDROID_PAYMENTSHEET_GOOGLE_PAY.key)

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LatePledgeCheckoutViewModel(environment) as T
        }
    }
}
