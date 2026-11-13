package com.kickstarter.features.checkout.data

import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData

/**
 * Immutable extraction result containing normalized rewards, shipping, and amounts.
 *
 * Negative Constraints:
 * - Do NOT instantiate with unnormalized negative amounts.
 */
data class ExtractedPledgeInfo(
    val selectedRewards: List<Reward> = emptyList(),
    val shippingRule: ShippingRule? = null,
    val shippingAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val bonusAmount: Double = 0.0,
    val checkoutData: CheckoutData? = null
)

/**
 * Pure extraction utility for extracting rewards, shipping, and checkout data from [Backing] or [PledgeData].
 *
 * Negative Constraints:
 * - Do NOT perform I/O or network operations in this extractor.
 */
object PledgeInfoExtractor {

    fun fromBacking(backing: Backing): ExtractedPledgeInfo {
        val list = mutableListOf<Reward>()
        backing.reward()?.let { list.add(it) }
        backing.addOns()?.let { list.addAll(it) }

        var shippingRule: ShippingRule? = null
        backing.location()?.let {
            shippingRule = ShippingRule.builder().location(it).build()
        }

        if (backing.location() == null && backing.locationName() != null && backing.locationId() != null) {
            val location = Location.builder()
                .name(backing.locationName())
                .displayableName(backing.locationName())
                .id(backing.locationId())
                .build()
            shippingRule = ShippingRule.builder().location(location).build()
        }

        val shippingAmount = (backing.shippingAmount() ?: 0.0).toDouble()
        var bonusAmount = (backing.bonusAmount() ?: 0.0).toDouble()
        val totalAmount = (backing.amount() ?: 0.0).toDouble()

        if (backing.reward() == null) {
            bonusAmount = 0.0
        }

        val checkoutData = CheckoutData.builder()
            .amount(totalAmount)
            .paymentType(CreditCardPaymentType.CREDIT_CARD)
            .bonusAmount(bonusAmount)
            .shippingAmount(shippingAmount)
            .build()

        return ExtractedPledgeInfo(
            selectedRewards = list.toList(),
            shippingRule = shippingRule,
            shippingAmount = shippingAmount,
            totalAmount = totalAmount,
            bonusAmount = bonusAmount,
            checkoutData = checkoutData
        )
    }

    fun fromPledgeData(pData: PledgeData, storedRefTag: RefTag? = null): ExtractedPledgeInfo {
        val selectedRewards = pData.rewardsAndAddOnsList()
        if (selectedRewards.isEmpty()) {
            return ExtractedPledgeInfo()
        }

        val isNoReward = RewardUtils.isNoReward(selectedRewards.first())
        val shippingRule = pData.shippingRule()
        var shippingAmount = 0.0
        var bonusAmount = 0.0
        var totalAmount = 0.0

        if (!isNoReward) {
            shippingAmount = pData.shippingCostIfShipping()
            bonusAmount = pData.bonusAmount()
            totalAmount = pData.checkoutTotalAmount()
        } else {
            totalAmount = selectedRewards.first().pledgeAmount() + pData.bonusAmount()
            bonusAmount = 0.0
        }

        val checkoutData = CheckoutData.builder()
            .amount(pData.pledgeAmountTotal())
            .paymentType(CreditCardPaymentType.CREDIT_CARD)
            .bonusAmount(bonusAmount)
            .shippingAmount(pData.shippingCostIfShipping())
            .build()

        return ExtractedPledgeInfo(
            selectedRewards = selectedRewards,
            shippingRule = shippingRule,
            shippingAmount = shippingAmount,
            totalAmount = totalAmount,
            bonusAmount = bonusAmount,
            checkoutData = checkoutData
        )
    }
}
