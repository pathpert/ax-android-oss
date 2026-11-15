package com.kickstarter.features.projectpage.ui.components

import android.util.Pair
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isGone
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.databinding.ActivityProjectPageBinding
import com.kickstarter.libs.Environment
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.compose.projectpage.ProjectPledgeButtonAndFragmentContainer
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.viewmodels.projectpage.AddOnsViewModel
import com.kickstarter.viewmodels.projectpage.CheckoutFlowViewModel
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import com.kickstarter.viewmodels.projectpage.RewardsSelectionViewModel
import kotlinx.coroutines.launch

/**
 * Encapsulates the Compose host view setup and state observation for the late pledges checkout flow.
 *
 * Negative constraints:
 * - Do not instantiate outside Activity lifecycle scope.
 */
class LatePledgesComposeHost(
    private val composeView: ComposeView,
    private val binding: ActivityProjectPageBinding,
    private val environment: Environment,
    private val checkoutFlowViewModel: CheckoutFlowViewModel,
    private val rewardsSelectionViewModel: RewardsSelectionViewModel,
    private val addOnsViewModel: AddOnsViewModel,
    private val latePledgeCheckoutViewModel: LatePledgeCheckoutViewModel,
    private val onBackPressedDispatcher: OnBackPressedDispatcher,
    private val startLoginToutActivity: () -> Unit,
    private val showDisclaimerScreen: (DisclaimerItems) -> Unit,
    private val showAccountabilityPage: () -> Unit,
    private val showCreatePledgeSuccess: (Pair<CheckoutData, PledgeData>) -> Unit,
    private val showToastError: (String?) -> Unit,
    private val flowControllerPresentPaymentOption: (String, String, String?, String?) -> Unit,
    private val stripeNextAction: (String) -> Unit,
    private val refreshProject: () -> Unit,
    private val finishWithAnimation: () -> Unit
) {
    @OptIn(ExperimentalFoundationApi::class)
    fun setup() {
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KickstarterApp {
                    LatePledgesContent()
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun LatePledgesContent() {
        val flowUIState by checkoutFlowViewModel.flowUIState.collectAsStateWithLifecycle()
        val expanded = flowUIState.expanded
        val currentPage = flowUIState.currentPage

        val rewardSelectionUIState by rewardsSelectionViewModel.rewardSelectionUIState.collectAsStateWithLifecycle()
        val shippingUIState by rewardsSelectionViewModel.shippingUIState.collectAsStateWithLifecycle()

        val projectData = rewardSelectionUIState.project
        val indexOfBackedReward = rewardSelectionUIState.initialRewardIndex
        val rewardsList = shippingUIState.filteredRw
        val rewardLoading = shippingUIState.loading
        val selectedReward = rewardSelectionUIState.selectedReward
        val currentUserShippingRule = shippingUIState.selectedShippingRule
        val shippingRules = shippingUIState.shippingRules
        rewardsSelectionViewModel.sendEvent(expanded, currentPage, projectData)

        LaunchedEffect(Unit) {
            rewardsSelectionViewModel.flowUIRequest.collect {
                checkoutFlowViewModel.changePage(it)
            }
        }

        val addOnsUIState by addOnsViewModel.addOnsUIState.collectAsStateWithLifecycle()
        val addOns = addOnsUIState.addOns
        val addOnsIsLoading = addOnsUIState.isLoading
        val addOnCount = addOnsUIState.totalCount
        val totalPledgeAmount = addOnsUIState.totalPledgeAmount

        addOnsViewModel.provideErrorAction { message ->
            showToastError(message)
        }

        val checkoutPayment by latePledgeCheckoutViewModel.checkoutPayment.collectAsStateWithLifecycle()

        LaunchedEffect(checkoutPayment.id) {
            checkoutPayment.backing?.let {
                latePledgeCheckoutViewModel.provideCheckoutIdAndBacking(
                    checkoutPayment.id,
                    it
                )
            }
        }

        val latePledgeCheckoutUIState by latePledgeCheckoutViewModel.latePledgeCheckoutUIState.collectAsStateWithLifecycle()

        val userStoredCards = latePledgeCheckoutUIState.storeCards
        val userEmail = latePledgeCheckoutUIState.userEmail
        val checkoutLoading = latePledgeCheckoutUIState.isLoading
        val shippingAmount = latePledgeCheckoutUIState.shippingAmount
        val checkoutTotal = latePledgeCheckoutUIState.checkoutTotal
        val isPledgeButtonEnabled = latePledgeCheckoutUIState.isPledgeButtonEnabled

        latePledgeCheckoutViewModel.provideErrorAction { message ->
            showToastError(message)
        }

        LaunchedEffect(Unit) {
            latePledgeCheckoutViewModel.clientSecretForNewPaymentMethod.collect {
                flowControllerPresentPaymentOption(
                    it,
                    latePledgeCheckoutUIState.userEmail,
                    latePledgeCheckoutUIState.projectCountryCode,
                    latePledgeCheckoutUIState.projectCurrency
                )
            }
        }

        LaunchedEffect(Unit) {
            latePledgeCheckoutViewModel.paymentRequiresAction.collect {
                stripeNextAction(it)
            }
        }

        val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })

        onBackPressedDispatcher.addCallback {
            if (expanded) checkoutFlowViewModel.onBackPressed(pagerState.currentPage)
            else finishWithAnimation()
        }

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(currentPage) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(
                    page = currentPage,
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    )
                )

                if (currentPage == 3) {
                    latePledgeCheckoutViewModel.sendPageViewedEvent()
                }

                if (currentPage == 1) {
                    addOnsViewModel.sendEvent()
                }
            }
        }

        ProjectPledgeButtonAndFragmentContainer(
            expanded = expanded,
            onContinueClicked = {
                checkoutFlowViewModel.onContinueClicked(
                    logInCallback = { startLoginToutActivity() },
                    continueCallback = {
                        rewardsSelectionViewModel.provideProjectData(projectData)
                        checkoutFlowViewModel.onBackThisProjectClicked()
                    }
                )
            },
            onBackClicked = {
                checkoutFlowViewModel.onBackPressed(pagerState.currentPage)
            },
            pagerState = pagerState,
            isLoading = addOnsIsLoading || checkoutLoading || rewardLoading,
            onAddOnsContinueClicked = {
                checkoutFlowViewModel.onContinueClicked {
                    val dataAndReason = addOnsViewModel.getPledgeDataAndReason()
                    dataAndReason?.let { pData ->
                        latePledgeCheckoutViewModel.providePledgeData(pData.first)
                    }
                }
            },
            currentShippingRule = currentUserShippingRule,
            shippingRules = shippingRules,
            environment = environment,
            initialRewardCarouselPosition = indexOfBackedReward,
            rewardsList = rewardsList,
            addOns = addOns,
            project = projectData.project(),
            onRewardSelected = { reward: Reward ->
                checkoutFlowViewModel.userRewardSelection(reward)
                addOnsViewModel.userRewardSelection(reward)
                addOnsViewModel.provideSelectedShippingRule(currentUserShippingRule)
                rewardsSelectionViewModel.onUserRewardSelection(reward)
                latePledgeCheckoutViewModel.userRewardSelection(reward)
            },
            onAddOnAddedOrRemoved = { quantityForId: Int, rewardId: Long ->
                addOnsViewModel.updateSelection(rewardId, quantityForId)
            },
            totalSelectedAddOn = addOnCount,
            selectedReward = selectedReward,
            totalPledgeAmount = totalPledgeAmount,
            totalBonusAmount = addOnsUIState.totalBonusAmount,
            bonusAmountChanged = { bonusAmount: Double ->
                addOnsViewModel.bonusAmountUpdated(bonusAmount)
            },
            selectedRewardAndAddOnList = latePledgeCheckoutUIState.selectedRewards,
            onShippingRuleSelected = { shippingRule: ShippingRule ->
                rewardsSelectionViewModel.selectedShippingRule(shippingRule)
            },
            storedCards = userStoredCards,
            userEmail = userEmail,
            shippingAmount = shippingAmount,
            checkoutTotal = checkoutTotal,
            onPledgeCtaClicked = { selectedCard: StoredCard?, isIncremental: Boolean ->
                selectedCard?.apply {
                    latePledgeCheckoutViewModel.sendSubmitCTAEvent()
                    latePledgeCheckoutViewModel.onPledgeButtonClicked(
                        selectedCard = selectedCard,
                    )
                }
            },
            onAddPaymentMethodClicked = {
                latePledgeCheckoutViewModel.onAddNewCardClicked(project = projectData.project())
            },
            onDisclaimerItemClicked = { disclaimerItem: DisclaimerItems ->
                showDisclaimerScreen(disclaimerItem)
            },
            onAccountabilityLinkClicked = {
                showAccountabilityPage()
            },
            isPledgeButtonEnabled = isPledgeButtonEnabled
        )

        val successfulPledge =
            latePledgeCheckoutViewModel.onPledgeSuccess.collectAsStateWithLifecycle(
                initialValue = false
            ).value

        LaunchedEffect(successfulPledge) {
            if (successfulPledge) {
                latePledgeCheckoutViewModel.onPledgeSuccess.collect {
                    if (latePledgeCheckoutViewModel.getCheckoutData() != null && latePledgeCheckoutViewModel.getPledgeData() != null) {
                        showCreatePledgeSuccess(
                            Pair(
                                latePledgeCheckoutViewModel.getCheckoutData(),
                                latePledgeCheckoutViewModel.getPledgeData()
                            )
                        )
                    }
                    checkoutFlowViewModel.onProjectSuccess()
                    refreshProject()
                    binding.pledgeContainerCompose.isGone = true
                    binding.pledgeContainerLayout.pledgeContainerRoot.isGone = false
                }
            }
        }
    }
}
