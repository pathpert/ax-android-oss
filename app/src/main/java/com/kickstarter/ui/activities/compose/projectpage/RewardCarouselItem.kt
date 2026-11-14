package com.kickstarter.ui.activities.compose.projectpage

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.isAllowedToPledge
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNullOrZero
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.KSRewardCard
import java.math.RoundingMode

@Composable
fun CarouselRewardItem(
    reward: Reward,
    project: Project,
    backing: Backing?,
    currentShippingRule: ShippingRule,
    environment: Environment,
    onRewardSelected: (reward: Reward) -> Unit
) {
    val context: Context = LocalContext.current
    val selectedLocationId = currentShippingRule.location()?.id()
    val ctaButtonEnabled = RewardViewUtils.isRewardSelectable(reward, project, selectedLocationId, backing)
    val isBacked = backing?.isBacked(reward) ?: false

    val ctaButtonText = when {
        ctaButtonEnabled -> R.string.Select
        !RewardUtils.isShippableToLocation(reward, selectedLocationId) -> R.string.Not_available_in_selected_country
        else -> R.string.No_longer_available
    }

    if (RewardUtils.isNoReward(reward)) {
        KSRewardCard(
            modifier = Modifier.testTag(RewardCarouselTestTag.REWARD_CARD.name + reward.id()),
            isCTAButtonEnabled = ctaButtonEnabled,
            ctaButtonText = stringResource(id = ctaButtonText),
            title = if (isBacked) stringResource(id = R.string.You_pledged_without_a_reward) else stringResource(
                id = R.string.Pledge_without_a_reward
            ),
            description = if (isBacked) stringResource(id = R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality) else stringResource(
                id = R.string.Back_it_because_you_believe_in_it
            ),
            onRewardSelectClicked = { onRewardSelected(reward) },
            isCTAButtonVisible = project.isAllowedToPledge(),
            yourSelectionIsVisible = project.backing()?.isBacked(reward) ?: false,
        )
    } else {
        KSRewardCard(
            modifier = Modifier.testTag(RewardCarouselTestTag.REWARD_CARD.name + reward.id()),
            onRewardSelectClicked = { onRewardSelected(reward) },
            amount = environment.ksCurrency()?.let {
                RewardViewUtils.styleCurrency(
                    reward.minimum(),
                    project,
                    it
                ).toString()
            },
            conversion = if (project.currentCurrency() == project.currency()) "" else {
                val conversionAmount = environment.ksCurrency()?.format(
                    reward.convertedMinimum(),
                    project,
                    true,
                    RoundingMode.HALF_UP,
                    true
                )
                environment.ksString()?.format(
                    stringResource(id = R.string.About_reward_amount),
                    "reward_amount",
                    conversionAmount
                )
            },
            description = reward.description(),
            title = reward.title(),
            backerCountBadgeText = if (reward.backersCount().isNullOrZero())
                ""
            else {
                environment.ksString()?.let {
                    it.format(
                        "rewards_info_backer_count_backers",
                        requireNotNull(reward.backersCount()),
                        "backer_count",
                        NumberUtils.format(requireNotNull(reward.backersCount()))
                    )
                }
            },
            image = reward.image(),
            isCTAButtonEnabled = ctaButtonEnabled,
            includes = if (RewardUtils.isItemized(reward) && !reward.rewardsItems()
                .isNullOrEmpty() && environment.ksString().isNotNull()
            ) {
                reward.rewardsItems()?.map { rewardItems ->
                    environment.ksString()?.format(
                        "rewards_info_item_quantity_title", rewardItems.quantity(),
                        "quantity", rewardItems.quantity().toString(),
                        "title", rewardItems.item().name()
                    ) ?: ""
                } ?: emptyList()
            } else {
                emptyList()
            },
            estimatedDelivery = if (reward.estimatedDeliveryOn().isNotNull()) {
                DateTimeUtils.estimatedDeliveryOn(requireNotNull(reward.estimatedDeliveryOn()))
            } else "",
            yourSelectionIsVisible = project.backing()?.isBacked(reward) ?: false,
            localPickup = if (RewardUtils.isLocalPickup(reward) && !RewardUtils.isShippable(
                    reward
                )
            ) {
                reward.localReceiptLocation()?.displayableName() ?: ""
            } else {
                ""
            },
            ctaButtonText = stringResource(id = ctaButtonText),
            expirationDateText = environment.ksString()?.let {
                if (RewardUtils.deadlineCountdownValue(reward) <= 0)
                    ""
                else
                    "" + RewardUtils.deadlineCountdownValue(reward) + " " +
                        RewardUtils.deadlineCountdownDetail(reward, context, it)
            },
            shippingSummaryText = environment.ksString()?.let { ksString ->
                if (RewardUtils.isShippable(reward)) {
                    RewardUtils.shippingSummary(reward)?.let {
                        RewardViewUtils.shippingSummary(
                            context = context,
                            ksString = ksString,
                            it
                        )
                    }
                } else {
                    ""
                }
            },
            remainingText = environment.ksString()?.let { ksString ->
                RewardViewUtils.getQuantityRemainingString(context, ksString, reward)
            },
            estimatedShippingCost = if (!RewardUtils.isDigital(reward) && RewardUtils.isShippable(reward) && !RewardUtils.isLocalPickup(reward)) {
                environment.ksCurrency()?.let { ksCurrency ->
                    environment.ksString()?.let { ksString ->
                        RewardViewUtils.getEstimatedShippingCost(
                            context = context,
                            ksCurrency = ksCurrency,
                            ksString = ksString,
                            project = project,
                            reward = reward,
                            locationId = currentShippingRule.location()?.id() ?: Location.builder().build().id(),
                        )
                    }
                }
            } else null,
            addonsPillVisible = reward.hasAddons(),
            isCTAButtonVisible = project.isAllowedToPledge(),
            isSecret = reward.isSecretReward() == true,
            isFeatured = reward.isFeatured() == true
        )
    }
}

