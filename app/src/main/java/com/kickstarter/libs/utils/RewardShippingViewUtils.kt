package com.kickstarter.libs.utils

import android.content.Context
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import java.math.RoundingMode

private const val EMPTY_STRING = ""

/**
 * Helpers for calculating and formatting reward and add-on shipping estimates.
 */
object RewardShippingViewUtils {

    /**
     * Returns the estimated shipping cost for a single reward at a given location.
     */
    fun getEstimatedShippingCost(
        context: Context,
        ksCurrency: KSCurrency,
        ksString: KSString,
        project: Project,
        reward: Reward,
        locationId: Long,
    ): String? {
        if (RewardUtils.isDigital(reward) || RewardUtils.isLocalPickup(reward) || !RewardUtils.isShippable(reward))
            return null

        val shippingRule = reward.shippingRules()?.firstOrNull { it.location()?.id() == locationId }
            ?: if (RewardUtils.shipsWorldwide(reward)) reward.shippingRules()?.firstOrNull() else null

        if (shippingRule == null)
            return null

        val estimatedMin = shippingRule.estimatedMin()
        val estimatedMax = shippingRule.estimatedMax()
        val cost = shippingRule.cost()

        if (estimatedMin > 0.0 || estimatedMax > 0.0) {
            val estimatedMinFormatted = ksCurrency.format(estimatedMin, project, RoundingMode.HALF_UP)
            val estimatedMaxFormatted = ksCurrency.format(estimatedMax, project, RoundingMode.HALF_UP)
            return if (estimatedMinFormatted.isEmpty() || estimatedMaxFormatted.isEmpty()) {
                EMPTY_STRING
            } else {
                ksString.format(
                    context.getString(R.string.About_reward_amount),
                    "reward_amount",
                    "$estimatedMinFormatted-$estimatedMaxFormatted"
                )
            }
        } else if (cost > 0) {
            val costFormatted = ksCurrency.format(cost, project, RoundingMode.HALF_UP)
            return costFormatted.ifEmpty {
                EMPTY_STRING
            }
        } else {
            return EMPTY_STRING
        }
    }

    /**
     * Return a string for the estimated shipping costs for a collection of
     * Rewards (i.e. a base Reward + Add-Ons) for a given shipping rule.
     */
    fun getEstimatedShippingRange(
        context: Context,
        ksCurrency: KSCurrency,
        ksString: KSString,
        project: Project,
        rewards: List<Reward> = listOf(),
        selectedShippingRule: ShippingRule,
        multipleQuantitiesAllowed: Boolean,
        useUserPreference: Boolean,
        useAbout: Boolean
    ): String {
        var min = ""
        var max = ""
        var minTotal = 0.0
        var maxtotal = 0.0

        rewards.forEach { reward ->
            if (RewardUtils.isDigital(reward) || RewardUtils.isLocalPickup(reward) || !RewardUtils.isShippable(reward))
                return@forEach

            var shippingRule = reward.shippingRules()?.firstOrNull {
                it.location()?.id() == selectedShippingRule.location()?.id()
            }

            if (shippingRule == null && RewardUtils.shipsWorldwide(reward))
                shippingRule = reward.shippingRules()?.firstOrNull()

            shippingRule?.let {
                minTotal += (it.estimatedMin() * (reward.quantity() ?: 1))
                maxtotal += (it.estimatedMax() * (reward.quantity() ?: 1))
            }
        }

        if (minTotal <= 0 && maxtotal <= 0) return ""

        min = if (useUserPreference) {
            ksCurrency.formatWithUserPreference(minTotal, project, RoundingMode.HALF_UP, precision = 2)
        } else {
            ksCurrency.format(minTotal, project, RoundingMode.HALF_UP)
        }
        max = if (useUserPreference) {
            ksCurrency.formatWithUserPreference(maxtotal, project, RoundingMode.HALF_UP, precision = 2)
        } else {
            ksCurrency.format(maxtotal, project, RoundingMode.HALF_UP)
        }

        if (min.isEmpty() || max.isEmpty()) return ""

        val minToMaxString = if (multipleQuantitiesAllowed) "$min-$max each" else "$min-$max"

        return if (useAbout) {
            ksString.format(
                context.getString(R.string.About_reward_amount),
                "reward_amount",
                minToMaxString
            )
        } else {
            minToMaxString
        }
    }

    /**
     * Returns a string for the shipping costs for add-on cards.
     */
    fun getAddOnShippingAmountString(
        context: Context,
        project: Project,
        reward: Reward,
        rewardShippingRules: List<ShippingRule>?,
        ksCurrency: KSCurrency?,
        ksString: KSString?,
        selectedShippingRule: ShippingRule
    ): String {
        if (rewardShippingRules.isNullOrEmpty() || ksCurrency.isNull() || ksString.isNull()) return ""
        val shippingAmount =
            if (!RewardUtils.isDigital(reward) && RewardUtils.isShippable(reward) && !RewardUtils.isLocalPickup(reward)) {
                var cost = 0.0
                rewardShippingRules.filter {
                    it.location()?.id() == selectedShippingRule.location()?.id()
                }.map {
                    cost += it.cost()
                }
                if (cost > 0) ksCurrency?.format(cost, project)
                else ""
            } else {
                ""
            }
        if (shippingAmount.isNullOrEmpty()) return ""
        val rewardAndShippingString =
            context.getString(R.string.reward_amount_plus_shipping_cost_each)
        val stringSections = rewardAndShippingString.split("+")
        val shippingString = " +" + stringSections[1]
        val ammountAndShippingString = ksString?.format(
            shippingString,
            "shipping_cost",
            shippingAmount
        )
        return ammountAndShippingString ?: ""
    }
}
