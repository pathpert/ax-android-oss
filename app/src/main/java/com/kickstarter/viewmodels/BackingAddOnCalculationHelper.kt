package com.kickstarter.viewmodels

import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import kotlin.math.min

/**
 * Calculation helper methods for [BackingAddOnViewHolderViewModel].
 */
internal object BackingAddOnCalculationHelper {

    fun getShippingCost(
        ksCurrency: KSCurrency,
        shippingRules: List<ShippingRule>?,
        project: Project,
        selectedShippingRule: ShippingRule
    ): String? {
        if (shippingRules.isNullOrEmpty()) return ""
        var cost = 0.0
        shippingRules.filter {
            it.location()?.id() == selectedShippingRule.location()?.id()
        }.forEach {
            cost += it.cost()
        }
        return ksCurrency.format(cost, project)
    }

    /**
     * If the addOn is available and within a valid time range for the campaign, the
     * maximumLimit will be the lowest of either the limit or the remaining.
     * If the addOns is not available or the campaign is over, the maxLimit will be
     * the current selected quantity allowing the user to modify the already backed amount
     * just to decrease it.
     */
    fun maximumLimit(addOn: Reward): Int? {
        val limit = addOn.limit()
        val remaining = addOn.remaining()
        return if (addOn.isAvailable() && RewardUtils.isValidTimeRange(addOn)) {
            when {
                remaining != null && limit != null -> min(remaining, limit)
                remaining != null -> remaining
                limit != null -> limit
                else -> null
            }
        } else {
            addOn.quantity()
        }
    }
}
