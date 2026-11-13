package com.kickstarter.features.checkout.data

import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.expandedRewardsAndAddOnsList
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.getBackingData
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.services.mutations.getUpdateBackingData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason

/**
 * Pure builder for [CreateBackingData] and [UpdateBackingData] mutations.
 *
 * Negative Constraints:
 * - Do NOT send location IDs or amount for PLOT payment-only updates.
 */
object CheckoutBackingDataFactory {

    fun buildCreateBackingData(
        project: Project,
        pledgeData: PledgeData?,
        selectedPaymentMethod: StoredCard,
        refTag: RefTag?,
        incrementalPledge: Boolean?
    ): CreateBackingData {
        val shouldNotSendId = pledgeData?.reward()?.let {
            RewardUtils.isDigital(it) || RewardUtils.isNoReward(it) || RewardUtils.isLocalPickup(it)
        } ?: true

        val locationID = pledgeData?.shippingRule()?.location()?.id()?.toString()
        return selectedPaymentMethod.getBackingData(
            proj = project,
            amount = pledgeData?.checkoutTotalAmount().toString(),
            locationId = if (shouldNotSendId) null else locationID,
            rewards = RewardUtils.extendAddOns(pledgeData?.rewardsAndAddOnsList() ?: emptyList<Reward>()),
            cookieRefTag = refTag,
            incremental = incrementalPledge
        )
    }

    fun buildUpdateBackingData(
        project: Project,
        backing: Backing,
        pledgeData: PledgeData?,
        pledgeReason: PledgeReason?,
        selectedPaymentMethod: StoredCard,
        incrementalPledge: Boolean?
    ): UpdateBackingData? {
        return when (pledgeReason) {
            PledgeReason.UPDATE_PAYMENT -> {
                if (project.isPledgeOverTimeAllowed().isTrue()) {
                    getUpdateBackingData(
                        backing,
                        amount = null,
                        locationId = null,
                        rewardsList = null,
                        pMethod = selectedPaymentMethod,
                        incremental = null
                    )
                } else {
                    val locationId = backing.locationId()?.toString()
                    val rwl = mutableListOf<Reward>()
                    backing.reward()?.let { rwl.add(it) }
                    backing.addOns()?.let { rwl.addAll(RewardUtils.extendAddOns(it)) }

                    getUpdateBackingData(
                        backing,
                        amount = null,
                        locationId = locationId,
                        rwl,
                        pMethod = selectedPaymentMethod,
                        incremental = null
                    )
                }
            }
            PledgeReason.UPDATE_REWARD,
            PledgeReason.UPDATE_PLEDGE -> {
                val isShippable = pledgeData?.reward()?.let { RewardUtils.isShippable(it) } ?: false
                val locationIdOrNull = if (isShippable) pledgeData?.shippingRule()?.location()?.id().toString() else null
                val isNoRw = pledgeData?.reward()?.let { RewardUtils.isNoReward(it) } ?: false
                val rwListOrEmpty = if (isNoRw) emptyList<Reward>() else pledgeData?.expandedRewardsAndAddOnsList() ?: emptyList()

                getUpdateBackingData(
                    backing,
                    pledgeData?.checkoutTotalAmount().toString(),
                    locationId = locationIdOrNull,
                    rwListOrEmpty,
                    null,
                    incremental = incrementalPledge
                )
            }
            PledgeReason.FIX_PLEDGE -> {
                getUpdateBackingData(
                    backing = backing,
                    pMethod = selectedPaymentMethod
                )
            }
            PledgeReason.PLEDGE,
            PledgeReason.LATE_PLEDGE,
            null -> null
        }
    }
}
