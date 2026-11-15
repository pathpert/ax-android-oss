package com.kickstarter.viewmodels.projectpage

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.checkout.data.LatePledgePaymentProcessor
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Backing
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
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
 * delegating network and payment mutations to [LatePledgePaymentProcessor] and [LatePledgeCheckoutCoordinator].
 */
@Deprecated(
    message = "Legacy monolithic path replaced by LatePledgeCheckoutFeatureViewModel under com.kickstarter.features.checkout.viewmodel.",
    replaceWith = ReplaceWith("LatePledgeCheckoutFeatureViewModel", "com.kickstarter.features.checkout.viewmodel.LatePledgeCheckoutFeatureViewModel")
)
class LatePledgeCheckoutViewModel(val environment: Environment) : ViewModel() {

    internal var pledgeData: PledgeData? = null
    internal var checkoutData: CheckoutData? = null
    private val analytics = requireNotNull(environment.analytics())
    private val statsigClient = requireNotNull(environment.statsigClient())

    internal val paymentProcessor = LatePledgePaymentProcessor(
        requireNotNull(environment.apolloClientV2()),
        requireNotNull(environment.stripe())
    )

    internal var storedCards: List<StoredCard> = listOf()
    internal var userEmail: String = ""
    internal var checkoutId: String? = null
    internal var backing: Backing? = null
    internal val selectedRewards = mutableListOf<Reward>()
    internal var buttonEnabled = true

    internal var clientSecretForNewCard: String = ""
    internal var newStoredCard: StoredCard? = null
    internal var errorAction: (message: String?) -> Unit = {}

    internal var clientSecretFor3DSVerification: String = ""
    internal var selectedCardFor3DSVerification: StoredCard? = null

    private var selectedReward: Reward? = null
    internal var mutableLatePledgeCheckoutUIState = MutableStateFlow(LatePledgeCheckoutUIState())

    internal var paymentIntent: String? = null
    private var pledgeButtonClickedJob: Job? = null

    internal var scope: CoroutineScope = viewModelScope
    internal var dispatcher: CoroutineDispatcher = Dispatchers.IO

    internal var mutableOnPledgeSuccessAction = MutableSharedFlow<Boolean>()
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

    internal val mutableCheckoutPayment = MutableStateFlow(CheckoutPayment(id = 0L, paymentUrl = null, backing = null))
    val checkoutPayment: StateFlow<CheckoutPayment>
        get() = mutableCheckoutPayment.asStateFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = CheckoutPayment(id = 0L, paymentUrl = null, null)
        )

    internal var mutableClientSecretForNewPaymentMethod = MutableSharedFlow<String>()
    val clientSecretForNewPaymentMethod: SharedFlow<String>
        get() = mutableClientSecretForNewPaymentMethod.asSharedFlow()

    internal var mutablePaymentRequiresAction = MutableSharedFlow<String>()
    val paymentRequiresAction: SharedFlow<String>
        get() = mutablePaymentRequiresAction.asSharedFlow()

    fun getCheckoutData() = checkoutData
    fun getPledgeData() = pledgeData

    fun provideCheckoutIdAndBacking(checkoutId: Long, backing: Backing) {
        this.checkoutId = checkoutId.toString()
        this.backing = backing
    }

    fun onAddNewCardClicked(project: Project) = handleAddNewCard(project)

    fun onNewCardSuccessfullyAdded() = handleNewCardSuccessfullyAdded()

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
                    executeCreatePaymentIntent(selectedCard, project, it.checkoutTotalAmount())
                } else {
                    paymentIntent?.let { pi ->
                        selectedCard?.let { card -> executeValidateCheckout(pi, card) }
                    }
                }
            }
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    fun completeOnSessionCheckoutFor3DS() = handleCompleteOnSessionCheckoutFor3DS()

    fun clear3DSValues() {
        buttonEnabled = true
        clientSecretFor3DSVerification = ""
        selectedCardFor3DSVerification = null
        scope.launch { emitCurrentState() }
    }

    internal suspend fun emitCurrentState(isLoading: Boolean = false) {
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

    fun providePledgeData(pledgeData: PledgeData) = handlePledgeDataProvided(pledgeData)

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
