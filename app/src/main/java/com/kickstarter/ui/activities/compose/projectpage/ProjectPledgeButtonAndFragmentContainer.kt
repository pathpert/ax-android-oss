package com.kickstarter.ui.activities.compose.projectpage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.isNullOrZero
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.toolbars.compose.TopToolBar

/**
 * Activity displaying and coordinating user interactions for ProjectPledgeButtonAndFragmentContainer.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectPledgeButtonAndFragmentContainer(
    expanded: Boolean,
    onContinueClicked: () -> Unit,
    onBackClicked: () -> Unit,
    pagerState: PagerState,
    isLoading: Boolean,
    onAddOnsContinueClicked: () -> Unit,
    shippingRules: List<ShippingRule> = listOf(),
    currentShippingRule: ShippingRule,
    environment: Environment?,
    initialRewardCarouselPosition: Int = 0,
    rewardsList: List<Reward>,
    addOns: List<Reward>,
    project: Project,
    onRewardSelected: (reward: Reward) -> Unit,
    onAddOnAddedOrRemoved: (quantityForId: Int, rewardId: Long) -> Unit,
    totalSelectedAddOn: Int,
    totalPledgeAmount: Double,
    totalBonusAmount: Double,
    bonusAmountChanged: (amount: Double) -> Unit,
    selectedReward: Reward? = null,
    onShippingRuleSelected: (ShippingRule) -> Unit,
    selectedRewardAndAddOnList: List<Reward>,
    storedCards: List<StoredCard>,
    userEmail: String,
    shippingAmount: Double,
    checkoutTotal: Double,
    onPledgeCtaClicked: (selectedCard: StoredCard?, isIncrementalPledge: Boolean) -> Unit,
    onAddPaymentMethodClicked: () -> Unit,
    onDisclaimerItemClicked: (disclaimerItem: DisclaimerItems) -> Unit,
    onAccountabilityLinkClicked: () -> Unit,
    isPledgeButtonEnabled: Boolean = false
) {
    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = if (expanded) {
                RectangleShape
            } else {
                RoundedCornerShape(
                    topStart = dimensions.radiusLarge,
                    topEnd = dimensions.radiusLarge
                )
            },
            color = colors.backgroundSurfacePrimary,
            tonalElevation = dimensions.elevationLarge,
        ) {
            AnimatedVisibility(
                visible = !expanded,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.paddingXLarge)
                ) {
                    KSPrimaryGreenButton(
                        onClickAction = onContinueClicked,
                        text = stringResource(id = R.string.Back_this_project),
                        isEnabled = true
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    expandFrom = Alignment.Bottom,
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Bottom,
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Scaffold(
                    topBar = {
                        TopToolBar(
                            title = stringResource(id = R.string.Back_this_project),
                            titleColor = colors.textPrimary,
                            leftOnClickAction = onBackClicked,
                            leftIconColor = colors.textPrimary,
                            backgroundColor = colors.backgroundSurfacePrimary,
                        )
                    },
                    contentWindowInsets = WindowInsets(0)
                ) { padding ->
                    Box(
                        Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        HorizontalPager(
                            userScrollEnabled = false,
                            state = pagerState
                        ) { page ->
                            when (page) {
                                0 -> {
                                    RewardCarouselScreen(
                                        modifier = Modifier,
                                        lazyRowState = rememberLazyListState(
                                            initialFirstVisibleItemIndex = initialRewardCarouselPosition
                                        ),
                                        environment = environment ?: Environment.builder().build(),
                                        rewards = rewardsList,
                                        project = project,
                                        onRewardSelected = onRewardSelected,
                                        isLoading = isLoading,
                                        countryList = shippingRules,
                                        currentShippingRule = currentShippingRule,
                                        onShippingRuleSelected = onShippingRuleSelected,
                                    )
                                }

                                1 -> {
                                    AddOnsScreen(
                                        modifier = Modifier,
                                        environment = environment ?: Environment.builder().build(),
                                        lazyColumnListState = rememberLazyListState(),
                                        selectedReward = selectedReward ?: Reward.builder().build(),
                                        addOns = addOns,
                                        project = project,
                                        onItemAddedOrRemoved = onAddOnAddedOrRemoved,
                                        onContinueClicked = onAddOnsContinueClicked,
                                        isLoading = isLoading,
                                        addOnCount = totalSelectedAddOn,
                                        bonusAmountChanged = bonusAmountChanged,
                                        totalPledgeAmount = totalPledgeAmount,
                                        totalBonusSupport = totalBonusAmount
                                    )
                                }

                                3 -> {
                                    CheckoutScreen(
                                        storedCards = storedCards,
                                        environment = environment ?: Environment.builder().build(),
                                        ksString = environment?.ksString(),
                                        project = project,
                                        email = userEmail,
                                        selectedReward = selectedReward,
                                        selectedRewardsAndAddOns = selectedRewardAndAddOnList,
                                        rewardsList = getRewardListAndPrices(
                                            selectedRewardAndAddOnList,
                                            environment,
                                            project
                                        ),
                                        pledgeReason = PledgeReason.LATE_PLEDGE,
                                        shippingAmount = shippingAmount,
                                        totalAmount = checkoutTotal,
                                        totalBonusSupport = totalBonusAmount,
                                        currentShippingRule = currentShippingRule,
                                        rewardsHaveShippables = selectedRewardAndAddOnList.any {
                                            RewardUtils.isShippable(it)
                                        },
                                        onPledgeCtaClicked = onPledgeCtaClicked,
                                        newPaymentMethodClicked = onAddPaymentMethodClicked,
                                        isLoading = isLoading,
                                        onDisclaimerItemClicked = onDisclaimerItemClicked,
                                        onAccountabilityLinkClicked = onAccountabilityLinkClicked,
                                        isPledgeButtonEnabled = isPledgeButtonEnabled
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getRewardListAndPrices(
    rewardsList: List<Reward>,
    environment: Environment?,
    project: Project
): List<Pair<String, String>> {
    return rewardsList.map { reward ->
        if (!reward.quantity().isNullOrZero()) {
            val title = reward.title() ?: ""
            val quantity = reward.quantity() ?: 1
            Pair(
                "$title X $quantity",
                environment?.ksCurrency()?.let {
                    RewardViewUtils.styleCurrency(
                        reward.minimum() * quantity,
                        project,
                        it
                    ).toString()
                } ?: ""
            )
        } else {
            Pair(
                reward.title() ?: "",
                environment?.ksCurrency()?.let {
                    RewardViewUtils.styleCurrency(
                        reward.minimum(),
                        project,
                        it
                    ).toString()
                } ?: ""
            )
        }
    }
}
