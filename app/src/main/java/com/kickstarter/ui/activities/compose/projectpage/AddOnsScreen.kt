package com.kickstarter.ui.activities.compose.projectpage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.getCurrencySymbols
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.views.compose.checkout.BonusSupportContainer
import java.math.RoundingMode

/**
 * Activity displaying and coordinating user interactions for AddOnsScreen.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun AddOnsScreen(
    modifier: Modifier = Modifier,
    environment: Environment,
    lazyColumnListState: LazyListState,
    selectedReward: Reward,
    addOns: List<Reward>,
    project: Project,
    onItemAddedOrRemoved: (quantityForId: Int, rewardId: Long) -> Unit,
    bonusAmountChanged: (amount: Double) -> Unit,
    isLoading: Boolean = false,
    currentShippingRule: ShippingRule = ShippingRule.builder().build(),
    onContinueClicked: () -> Unit,
    addOnCount: Int = 0,
    totalPledgeAmount: Double,
    totalBonusSupport: Double
) {
    val context = LocalContext.current
    val currencySymbolStartAndEnd = environment.ksCurrency()?.getCurrencySymbols(project)
    val totalAmountString = environment.ksCurrency()?.let {
        RewardViewUtils.styleCurrency(
            value = totalPledgeAmount,
            project = project,
            ksCurrency = it
        ).toString()
    } ?: ""

    preloadImages(context, addOns)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                AddOnsBottomBar(
                    totalAmountString = totalAmountString,
                    addOnCount = addOnCount,
                    environment = environment,
                    onContinueClicked = onContinueClicked
                )
            },
            containerColor = colors.backgroundAccentGraySubtle
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(
                        start = dimensions.paddingMedium,
                        end = dimensions.paddingMedium,
                        top = dimensions.paddingMedium
                    )
                    .padding(paddingValues = padding),
                state = lazyColumnListState
            ) {
                item {
                    if (addOns.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.Customize_your_reward_with_optional_addons),
                            style = typographyV2.headingXL,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    val initAmount = if (project.isBacking())
                        project.backing()?.bonusAmount() ?: 0.0
                    else if (RewardUtils.isNoReward(selectedReward))
                        RewardUtils.minPledgeAmount(selectedReward, project)
                    else 0.0

                    BonusSupportContainer(
                        selectedReward = selectedReward,
                        initialAmount = initAmount,
                        maxAmount = RewardUtils.maxPledgeAmount(selectedReward, project),
                        minPledge = RewardUtils.minPledgeAmount(selectedReward, project),
                        totalAmount = totalPledgeAmount,
                        totalBonusSupport = totalBonusSupport,
                        currencySymbolAtStart = currencySymbolStartAndEnd?.first,
                        currencySymbolAtEnd = currencySymbolStartAndEnd?.second,
                        onBonusSupportPlusClicked = bonusAmountChanged,
                        onBonusSupportMinusClicked = bonusAmountChanged,
                        onBonusSupportInputted = bonusAmountChanged,
                        environment = environment
                    )
                }

                items(
                    items = addOns
                ) { reward ->

                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    AddOnsContainer(
                        rewardId = reward.id(),
                        image = reward.image(),
                        title = reward.title() ?: "",
                        amount = environment.ksCurrency()?.format(
                            reward.minimum(),
                            project,
                            true,
                        ) ?: "",
                        conversionAmount = if (project.currentCurrency() == project.currency()) "" else {
                            environment.ksString()?.format(
                                stringResource(R.string.About_reward_amount),
                                "reward_amount",
                                environment.ksCurrency()?.format(
                                    reward.convertedMinimum(),
                                    project,
                                    true,
                                    RoundingMode.HALF_UP,
                                    true
                                )
                            )
                        },
                        shippingAmount = RewardViewUtils.getAddOnShippingAmountString(
                            context = context,
                            project = project,
                            reward = reward,
                            rewardShippingRules = reward.shippingRules(),
                            ksCurrency = environment.ksCurrency(),
                            ksString = environment.ksString(),
                            selectedShippingRule = currentShippingRule
                        ),
                        description = reward.description() ?: "",
                        includesList = reward.addOnsItems()?.map {
                            environment.ksString()?.format(
                                "rewards_info_item_quantity_title", it.quantity(),
                                "quantity", it.quantity().toString(),
                                "title", it.item().name()
                            ) ?: ""
                        } ?: listOf(),
                        limit = reward.limit() ?: -1,
                        buttonEnabled = reward.isAvailable(),
                        buttonText = stringResource(id = R.string.Add),
                        estimatedShippingCost =
                        if (!RewardUtils.isDigital(reward) && RewardUtils.isShippable(reward) && !RewardUtils.isLocalPickup(reward)) {
                            environment.ksCurrency()?.let { ksCurrency ->
                                environment.ksString()?.let { ksString ->
                                    RewardViewUtils.getEstimatedShippingRange(
                                        context = context,
                                        ksCurrency = ksCurrency,
                                        ksString = ksString,
                                        project = project,
                                        rewards = listOf(reward),
                                        selectedShippingRule = currentShippingRule,
                                        multipleQuantitiesAllowed = (reward.limit() ?: -1) > 1,
                                        useUserPreference = false,
                                        useAbout = true
                                    )
                                }
                            }
                        } else null,
                        onItemAddedOrRemoved = { quantityForId, rwId ->
                            onItemAddedOrRemoved(quantityForId, rwId)
                        },
                        quantity = reward.quantity() ?: 0
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))
                }
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundAccentGraySubtle.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            KSCircularProgressIndicator()
        }
    }
}
