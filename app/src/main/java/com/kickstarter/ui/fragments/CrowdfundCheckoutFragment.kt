package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.databinding.FragmentCrowdfundCheckoutBinding
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getPaymentSheetConfiguration
import com.kickstarter.libs.utils.extensions.isGooglePay
import com.kickstarter.libs.utils.extensions.isLink
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.models.toStripePaymentSheetResult
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.PledgeDelegate
import com.kickstarter.ui.activities.compose.projectpage.CheckoutScreen
import com.kickstarter.ui.activities.compose.projectpage.getRewardListAndPrices
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.extensions.startDisclaimerChromeTab
import com.kickstarter.viewmodels.projectpage.CheckoutUIState
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel.Factory
import com.stripe.android.paymentsheet.PaymentOptionCallback
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.PaymentSheetResultCallback
import timber.log.Timber


/**
 * Fragment presenting UI and collecting view states for CrowdfundCheckoutFragment.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.checkout")
class CrowdfundCheckoutFragment : Fragment() {

    private var binding: FragmentCrowdfundCheckoutBinding? = null

    private lateinit var viewModelFactory: Factory
    private val viewModel: CrowdfundCheckoutViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var paymentSheetHelper: CrowdfundPaymentSheetHelper
    private var currentSetupClientId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentCrowdfundCheckoutBinding.inflate(inflater, container, false)

        paymentSheetHelper = CrowdfundPaymentSheetHelper(
            fragment = this,
            viewModel = viewModel,
            getErrorTargetView = { binding?.composeView }
        ).apply {
            initialize { currentSetupClientId }
        }

        val view = binding?.root
        binding?.composeView?.apply {
            val environment = this.context.getEnvironment()?.let { env ->
                viewModelFactory = Factory(env, bundle = arguments)
                viewModel.provideBundle(arguments)
                env
            }

            viewModel.provideErrorAction { message ->
                activity?.runOnUiThread {
                    showErrorToast(
                        context,
                        this,
                        message ?: getString(R.string.general_error_something_wrong)
                    )
                }
            }

            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Compose world
            setContent {
                KickstarterApp(
                    useDarkTheme = true
                ) {

                    val checkoutStates =
                        viewModel.crowdfundCheckoutUIState.collectAsStateWithLifecycle(
                            initialValue = CheckoutUIState()
                        ).value

                    val rwList = checkoutStates.selectedRewards
                    val email = checkoutStates.userEmail
                    val storedCards = checkoutStates.storeCards
                    val isLoading = checkoutStates.isLoading
                    val shippingAmount = checkoutStates.shippingAmount
                    val totalAmount = checkoutStates.checkoutTotal
                    val shippingRule = checkoutStates.shippingRule
                    val bonus = checkoutStates.bonusAmount
                    val showPlotWidget = checkoutStates.showPlotWidget
                    val plotEligible = checkoutStates.plotEligible
                    val paymentIncrements = checkoutStates.paymentIncrements
                    val isIncremental = checkoutStates.isIncrementalPledge

                    val pledgeData = viewModel.getPledgeData()
                    val pledgeReason = viewModel.getPledgeReason() ?: PledgeReason.PLEDGE
                    val project = pledgeData?.projectData()?.project() ?: Project.builder().build()
                    val selectedRw = pledgeData?.reward() ?: Reward.builder().build()

                    val checkoutSuccess =
                        viewModel.checkoutResultState.collectAsStateWithLifecycle().value
                    val id = checkoutSuccess.first?.id() ?: -1

                    val paymentSheetPresenter =
                        viewModel.presentPaymentSheetStates.collectAsStateWithLifecycle().value
                    val setUpIntent = paymentSheetPresenter.setupClientId

                    val projectCountryCode = pledgeData?.projectData()?.project()?.countryCode()
                    val projectCurrencyCode = pledgeData?.projectData()?.project()?.currency()

                    configurePaymentSheet(paymentSheetPresenter.setupClientId)
                    LaunchedEffect(key1 = setUpIntent) {
                        if (setUpIntent.isNotEmpty() && email.isNotEmpty()) {
                            flowControllerPresentPaymentOption(setUpIntent, email, projectCountryCode, projectCurrencyCode)
                        }
                    }

                    LaunchedEffect(id) {
                        if (id > 0) {
                            if (pledgeReason == PledgeReason.PLEDGE)
                                (activity as PledgeDelegate?)?.pledgeSuccessfullyCreated(
                                    checkoutSuccess
                                )
                            if (pledgeReason == PledgeReason.UPDATE_PAYMENT)
                                (activity as PledgeDelegate?)?.pledgePaymentSuccessfullyUpdated()
                            if (pledgeReason == PledgeReason.UPDATE_REWARD || pledgeReason == PledgeReason.UPDATE_PLEDGE || pledgeReason == PledgeReason.FIX_PLEDGE)
                                (activity as PledgeDelegate?)?.pledgeSuccessfullyUpdated()
                        }
                    }
                    val isEditPledgeFeatureFlagOn = environment?.featureFlagClient()?.getBoolean(FlagKey.ANDROID_PLOT_EDIT_PLEDGE) == true

                    val plotIsVisible = when {
                        pledgeReason == PledgeReason.PLEDGE -> showPlotWidget
                        pledgeReason == PledgeReason.UPDATE_REWARD -> isEditPledgeFeatureFlagOn && showPlotWidget
                        else -> false
                    }

                    val isPostCampaignPhase = project.isInPostCampaignPledgingPhase() == true
                    val emailForCheckout = if (isPostCampaignPhase) email else null

                    val showPaymentMethodSelection = pledgeReason in listOf(
                        PledgeReason.PLEDGE,
                        PledgeReason.LATE_PLEDGE,
                        PledgeReason.UPDATE_PAYMENT,
                        PledgeReason.FIX_PLEDGE
                    )

                    KSTheme {
                        CheckoutScreen(
                            rewardsList = getRewardListAndPrices(rwList, environment, project),
                            selectedRewardsAndAddOns = rwList,
                            environment = requireNotNull(environment),
                            shippingAmount = shippingAmount,
                            selectedReward = selectedRw,
                            currentShippingRule = shippingRule,
                            totalAmount = totalAmount,
                            totalBonusSupport = bonus,
                            storedCards = storedCards,
                            project = project,
                            email = emailForCheckout,
                            pledgeReason = pledgeReason,
                            rewardsHaveShippables = rwList.any {
                                RewardUtils.isShippable(it)
                            },
                            onPledgeCtaClicked = { selectedCard, isIncremental ->
                                viewModel.pledgeOrUpdatePledge(selectedCard, isIncremental)
                            },
                            isLoading = isLoading,
                            newPaymentMethodClicked = {
                                viewModel.getSetupIntent()
                            },
                            onDisclaimerItemClicked = { disclaimerItem ->
                                CrowdfundCheckoutNavigationHelper.openDisclaimerScreen(
                                    activity,
                                    context,
                                    binding?.composeView,
                                    disclaimerItem,
                                    environment
                                )
                            },
                            onAccountabilityLinkClicked = {
                                CrowdfundCheckoutNavigationHelper.showAccountabilityPage(
                                    activity,
                                    context,
                                    binding?.composeView,
                                    environment
                                )
                            },
                            onChangedPaymentMethod = { paymentMethodSelected ->
                                viewModel.userChangedPaymentMethodSelected(paymentMethodSelected)
                            },
                            ksCurrency = environment.ksCurrency(),
                            plotIsVisible = plotIsVisible,
                            isPlotEligible = plotEligible,
                            paymentIncrements = paymentIncrements,
                            isIncrementalPledge = isIncremental == true,
                            onCollectionPlanSelected = {
                                    collectionOptions ->
                                viewModel.collectionPlanSelected(collectionOptions)
                            },
                            showPaymentMethodSelection = showPaymentMethodSelection,
                        )
                    }
                }
            }
        }
        return view
    }

    private fun flowControllerPresentPaymentOption(
        clientSecret: String,
        userEmail: String,
        googlePayCountryCode: String? = null,
        googlePayCurrencyCode: String? = null
    ) {
        context?.let {
            paymentSheetHelper.presentPaymentOption(
                it,
                clientSecret,
                userEmail,
                googlePayCountryCode,
                googlePayCurrencyCode
            )
        }
    }

    private fun configurePaymentSheet(setupClientId: String) {
        currentSetupClientId = setupClientId
    }
}
