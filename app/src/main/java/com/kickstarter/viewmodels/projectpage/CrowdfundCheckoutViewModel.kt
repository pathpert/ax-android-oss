package com.kickstarter.viewmodels.projectpage

import CollectionOptions
import android.os.Bundle
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.checkout.data.CheckoutAnalyticsTracker
import com.kickstarter.features.checkout.data.CheckoutBackingDataFactory
import com.kickstarter.features.checkout.data.ExtractedPledgeInfo
import com.kickstarter.features.checkout.data.PledgeInfoExtractor
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Backing
import com.kickstarter.models.BuildPaymentPlanData
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.models.StripePaymentSheetResult
import com.kickstarter.models.extensions.isFromPaymentSheet
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

@Deprecated(
    message = "Legacy monolithic ViewModel replaced by CrowdfundCheckoutFeatureViewModel",
    replaceWith = ReplaceWith(
        "CrowdfundCheckoutFeatureViewModel",
        "com.kickstarter.features.checkout.viewmodel.CrowdfundCheckoutFeatureViewModel"
    )
)
class CrowdfundCheckoutViewModel(val environment: Environment, bundle: Bundle? = null) : CrowdfundCheckoutStateHolder() {
    val analytics = requireNotNull(environment.analytics())
    val apolloClient = requireNotNull(environment.apolloClientV2())
    val currentUser = requireNotNull(environment.currentUserV2()?.loggedInUser()?.asFlow())
    val cookieManager = requireNotNull(environment.cookieManager())
    val sharedPreferences = requireNotNull(environment.sharedPreferences())
    val ffClient = requireNotNull(environment.featureFlagClient())
    val statsigClient = requireNotNull(environment.statsigClient())

    private var pledgeData: PledgeData? = null
    private var checkoutData: CheckoutData? = null
    private var pledgeReason: PledgeReason? = null
    private var project = Project.builder().build()
    private var backing: Backing? = null
    private var refTag: RefTag? = null
    private var thirdPartyEventSent = Pair(false, "")
    private var errorAction: (message: String?) -> Unit = {}

    private var scope: CoroutineScope = viewModelScope
    private var dispatcher: CoroutineDispatcher = Dispatchers.IO

    fun provideScopeAndDispatcher(scope: CoroutineScope, dispatcher: CoroutineDispatcher) {
        this.scope = scope
        this.dispatcher = dispatcher
    }

    fun getPledgeData() = this.pledgeData
    fun getPledgeReason() = this.pledgeReason

    fun provideBundle(arguments: Bundle?) {
        val pData = arguments?.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as? PledgeData
        pledgeReason = arguments?.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as? PledgeReason

        if (pData != null) {
            pledgeData = pData
            project = pData.projectData().project()
            backing = project.backing()
            refTag = RefTagUtils.storedCookieRefTagForProject(project, cookieManager, sharedPreferences)

            when (pledgeReason) {
                PledgeReason.PLEDGE,
                PledgeReason.UPDATE_REWARD -> applyExtractedInfo(PledgeInfoExtractor.fromPledgeData(pData, refTag))
                PledgeReason.UPDATE_PAYMENT,
                PledgeReason.FIX_PLEDGE -> {
                    backing?.let { applyExtractedInfo(PledgeInfoExtractor.fromBacking(it)) }
                }
                else -> errorAction.invoke(null)
            }

            collectUserInformation()
            CheckoutAnalyticsTracker.sendPageViewedEvent(analytics, checkoutData, pledgeData)
            if (project.isPledgeOverTimeAllowed().isTrue()) {
                buildPaymentPlan()
            }
        }
    }

    private fun applyExtractedInfo(info: ExtractedPledgeInfo) {
        selectedRewards = info.selectedRewards
        shippingRule = info.shippingRule
        shippingAmount = info.shippingAmount
        totalAmount = info.totalAmount
        bonusAmount = info.bonusAmount
        checkoutData = info.checkoutData
    }

    private fun buildPaymentPlan() {
        scope.launch(dispatcher) {
            val slug = pledgeData?.projectData()?.project()?.slug() ?: ""
            val total = pledgeData?.checkoutTotalAmount().toString()
            apolloClient.buildPaymentPlan(BuildPaymentPlanData(slug, total)).asFlow()
                .onStart { emitCurrentState(isLoading = true) }
                .catch {
                    errorAction.invoke(it.message)
                    emitCurrentState(isLoading = false)
                }
                .collectLatest {
                    showPlotWidget = project.isPledgeOverTimeAllowed() == true
                    plotEligible = it.amountIsPledgeOverTimeEligible
                    paymentIncrements = it.paymentIncrements
                    emitCurrentState(isLoading = false)
                }
        }
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    private fun collectUserInformation() {
        scope.launch(dispatcher) {
            emitCurrentState(isLoading = true)
            currentUser.combine(apolloClient.userPrivacy().asFlow()) { cUser, privacy ->
                cUser.toBuilder().email(privacy.email).name(privacy.name).build()
            }.catch {
                errorAction.invoke(it.message)
                emitCurrentState(isLoading = false)
            }.combine(apolloClient.getStoredCards().asFlow()) { updatedUser, cards ->
                user = updatedUser
                storedCards = cards
            }.catch {
                errorAction.invoke(it.message)
                emitCurrentState(isLoading = false)
            }.collectLatest {
                emitCurrentState(isLoading = false)
            }
        }
    }

    fun newlyAddedPaymentMethod(paymentMethodSelected: StoredCard?) {
        paymentMethodSelected?.let {
            selectedPaymentMethod = it
            if (paymentMethodSelected.isFromPaymentSheet()) {
                val updatedCards = mutableListOf(paymentMethodSelected)
                updatedCards.addAll(storedCards)
                storedCards = updatedCards
            }
            scope.launch { emitCurrentState(isLoading = true) }
        }
    }

    fun collectionPlanSelected(collectionOption: CollectionOptions) {
        incrementalPledge = when (collectionOption) {
            CollectionOptions.PLEDGE_IN_FULL -> false
            CollectionOptions.PLEDGE_OVER_TIME -> true
        }
        scope.launch { emitCurrentState(isLoading = false) }
    }

    fun userChangedPaymentMethodSelected(paymentMethodSelected: StoredCard?) {
        paymentMethodSelected?.let { selectedPaymentMethod = it }
        scope.launch(dispatcher) {
            CheckoutAnalyticsTracker.sendThirdPartyAddPaymentInfoEvent(
                sharedPreferences, ffClient, project,
                requireNotNull(environment.currentUserV2()),
                apolloClient, pledgeData, shippingAmount, checkoutData
            ).collect { thirdPartyEventSent = it }
        }
    }

    fun isThirdPartyEventSent(): Pair<Boolean, String> = this.thirdPartyEventSent

    fun pledgeOrUpdatePledge(selectedCard: StoredCard?, isIncremental: Boolean?) {
        selectedCard?.let { selectedPaymentMethod = it }
        incrementalPledge = if (project.isPledgeOverTimeAllowed() == true) isIncremental else null
        scope.launch { emitCurrentState() }

        scope.launch(dispatcher) {
            when (pledgeReason) {
                PledgeReason.PLEDGE -> createBacking()
                PledgeReason.UPDATE_PLEDGE,
                PledgeReason.UPDATE_REWARD,
                PledgeReason.UPDATE_PAYMENT,
                PledgeReason.FIX_PLEDGE -> updateBacking()
                else -> errorAction.invoke(null)
            }
        }
    }

    private suspend fun createBacking() {
        CheckoutAnalyticsTracker.trackPledgeSubmitCTA(analytics, checkoutData, pledgeData)
        val backingData = CheckoutBackingDataFactory.buildCreateBackingData(
            project, pledgeData, selectedPaymentMethod, refTag, incrementalPledge
        )
        this.apolloClient.createBacking(backingData).asFlow()
            .onStart { emitCurrentState(isLoading = true) }
            .catch {
                errorAction.invoke(it.message)
                emitCurrentState(isLoading = false)
            }
            .collectLatest {
                checkoutData = checkoutData?.toBuilder()?.id(it.id())?.build()
                _checkoutResultState.emit(Pair(checkoutData, pledgeData))
                emitCurrentState(isLoading = false)
            }
    }

    private suspend fun updateBacking() {
        project.backing()?.let { b ->
            val backingData = CheckoutBackingDataFactory.buildUpdateBackingData(
                project, b, pledgeData, pledgeReason, selectedPaymentMethod, incrementalPledge
            )
            backingData?.let {
                apolloClient.updateBacking(it).asFlow()
                    .onStart { emitCurrentState(isLoading = true) }
                    .catch {
                        errorAction.invoke(it.message)
                        emitCurrentState(isLoading = false)
                    }
                    .collectLatest {
                        checkoutData = checkoutData?.toBuilder()?.id(it.id())?.build()
                        _checkoutResultState.emit(Pair(checkoutData, pledgeData))
                        emitCurrentState(isLoading = false)
                    }
            }
        }
    }

    fun isPaymentSheetGooglePayEnabled(): Boolean =
        statsigClient.configReady.value && statsigClient.checkGate(StatsigGateKey.ANDROID_PAYMENTSHEET_GOOGLE_PAY.key)

    fun paymentSheetPresented(state: Boolean) {
        scope.launch { emitCurrentState(isLoading = !state) }
    }

    fun getSetupIntent() {
        scope.launch(dispatcher) {
            apolloClient.createSetupIntent(project).asFlow()
                .onStart { emitCurrentState(isLoading = true) }
                .catch {
                    emitCurrentState(isLoading = false)
                    errorAction.invoke(it.message)
                }
                .collectLatest { _presentPaymentSheet.emit(PaymentSheetPresenterState(it)) }
        }
    }

    fun paymentSheetResult(paymentSheetResult: StripePaymentSheetResult) {
        when (paymentSheetResult) {
            is StripePaymentSheetResult.Canceled,
            is StripePaymentSheetResult.Failed -> {
                scope.launch {
                    storedCards = storedCards.filter { !it.isFromPaymentSheet() }
                    emitCurrentState(isLoading = false)
                }
            }
            is StripePaymentSheetResult.Completed -> {
                scope.launch { emitCurrentState(isLoading = false) }
            }
        }
    }

    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CrowdfundCheckoutViewModel(environment, bundle) as T
        }
    }
}
