package com.kickstarter.libs.utils

import com.kickstarter.libs.utils.RewardUtils.isItemized
import com.kickstarter.libs.utils.RewardUtils.isShippable
import com.kickstarter.libs.utils.RewardUtils.isTimeLimitedEnd
import com.kickstarter.libs.utils.extensions.addOnsCost
import com.kickstarter.libs.utils.extensions.bonus
import com.kickstarter.libs.utils.extensions.refTag
import com.kickstarter.libs.utils.extensions.rewardCost
import com.kickstarter.libs.utils.extensions.round
import com.kickstarter.libs.utils.extensions.shippingAmount
import com.kickstarter.libs.utils.extensions.totalAmount
import com.kickstarter.libs.utils.extensions.totalCountUnique
import com.kickstarter.libs.utils.extensions.totalQuantity
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import java.util.HashMap
import java.util.Locale

/**
 * Analytics event property builders for checkout and pledge data.
 */
object CheckoutAnalyticsUtils {

    @JvmOverloads
    fun checkoutProperties(checkoutData: CheckoutData, pledgeData: PledgeData, prefix: String = "checkout_"): Map<String, Any> {
        val project = pledgeData.projectData().project()
        val properties = HashMap<String, Any>().apply {
            put("amount", checkoutData.amount().round())
            checkoutData.id()?.let { put("id", it.toString()) }
            put(
                "payment_type",
                checkoutData.paymentType().rawValue.lowercase(Locale.getDefault())
            )
            put("amount_total_usd", checkoutData.totalAmount(project.staticUsdRate()).round())
            put("shipping_amount", checkoutData.shippingAmount())
            put("shipping_amount_usd", checkoutData.shippingAmount(project.staticUsdRate()).round())
            put("bonus_amount", checkoutData.bonus())
            put("bonus_amount_usd", checkoutData.bonus(project.staticUsdRate()).round())
            put("add_ons_count_total", pledgeData.totalQuantity())
            put("add_ons_count_unique", pledgeData.totalCountUnique())
            put("add_ons_minimum_usd", pledgeData.addOnsCost(project.staticUsdRate()).round())
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    @JvmOverloads
    fun checkoutProperties(checkoutData: CheckoutData, project: Project, addOns: List<Reward>?, prefix: String = "checkout_"): Map<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put("amount", checkoutData.amount().round())
            checkoutData.id()?.let { put("id", it.toString()) }
            put(
                "payment_type",
                checkoutData.paymentType().rawValue.lowercase(Locale.getDefault())
            )
            put("amount_total_usd", checkoutData.totalAmount(project.staticUsdRate()).round())
            put("shipping_amount", checkoutData.shippingAmount())
            put("shipping_amount_usd", checkoutData.shippingAmount(project.staticUsdRate()).round())
            put("bonus_amount", checkoutData.bonus())
            put("bonus_amount_usd", checkoutData.bonus(project.staticUsdRate()).round())
            put("add_ons_count_total", totalQuantity(addOns))
            put("add_ons_count_unique", totalCountUnique(addOns))
            put("add_ons_minimum_usd", addOnsCost(project.staticUsdRate(), addOns).round())
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    fun checkoutDataProperties(checkoutData: CheckoutData, pledgeData: PledgeData, loggedInUser: User?): Map<String, Any> {
        val props = pledgeDataProperties(pledgeData, loggedInUser)
        props.putAll(checkoutProperties(checkoutData, pledgeData))
        return props
    }

    fun pledgeDataProperties(pledgeData: PledgeData, loggedInUser: User?): MutableMap<String, Any> {
        val projectData = pledgeData.projectData()
        val props = AnalyticEventsUtils.projectProperties(projectData.project(), loggedInUser)
        props.putAll(pledgeProperties(pledgeData))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props["context_pledge_flow"] = pledgeData.pledgeFlowContext().trackingString
        return props
    }

    @JvmOverloads
    fun pledgeProperties(pledgeData: PledgeData, prefix: String = "checkout_"): Map<String, Any> {
        val reward = pledgeData.reward()
        val project = pledgeData.projectData().project()
        val properties = HashMap<String, Any>().apply {
            reward.estimatedDeliveryOn()?.let { deliveryDate ->
                put("estimated_delivery_on", deliveryDate)
            }
            put("has_items", isItemized(reward))
            put("id", reward.id().toString())
            put("is_limited_time", isTimeLimitedEnd(reward))
            put("is_limited_quantity", reward.limit() != null)
            put("minimum", reward.minimum())
            put("shipping_enabled", isShippable(reward))
            put("minimum_usd", pledgeData.rewardCost(project.staticUsdRate()).round())
            reward.shippingPreference()?.let { put("shipping_preference", it) }
            reward.title()?.let { put("title", it) }
        }

        val props = MapUtils.prefixKeys(properties, "reward_")

        props.apply {
            put("add_ons_count_total", pledgeData.totalQuantity())
            put("add_ons_count_unique", pledgeData.totalCountUnique())
            put("add_ons_minimum_usd", addOnsCost(project.staticUsdRate(), pledgeData.addOns()?.let { it as? List<Reward> } ?: emptyList()).round())
        }

        return MapUtils.prefixKeys(props, prefix)
    }
}
