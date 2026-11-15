package com.kickstarter.viewmodels.projectpage

import android.os.Bundle
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData

/**
 * Parsed bundle contents for [AddOnsViewModel].
 */
internal data class AddOnsBundleData(
    val pledgeData: PledgeData?,
    val pledgeReason: PledgeReason,
    val projectData: ProjectData?,
    val project: Project,
    val currentUserReward: Reward,
    val shippingRule: ShippingRule,
    val pledgeFlowContext: PledgeFlowContext,
    val backing: Backing?,
    val backedAddOns: List<Reward>,
    val bonusAmount: Double
)

internal object AddOnsBundleParser {

    fun parse(bundle: Bundle): AddOnsBundleData {
        val pledgeData = bundle.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as? PledgeData
        val pReason = (bundle.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as? PledgeReason)
            ?: PledgeReason.PLEDGE

        val projectData = pledgeData?.projectData()
        var project = projectData?.project() ?: Project.builder().build()

        var currentUserReward = pledgeData?.reward() ?: Reward.builder().build()
        var bonusAmount = if (pledgeData?.reward() != null) {
            RewardUtils.minPledgeAmount(currentUserReward, project)
        } else {
            0.0
        }

        val shippingRule = pledgeData?.shippingRule() ?: ShippingRule.builder().build()
        val flowContext = pledgeData?.pledgeFlowContext() ?: PledgeFlowContext.NEW_PLEDGE

        val backing = pledgeData?.projectData()?.backing() ?: project.backing()
        var backedAddOns = emptyList<Reward>()

        if (pReason == PledgeReason.UPDATE_REWARD && backing?.reward()?.id() != currentUserReward.id()) {
            // Do nothing, selecting a different reward/addOns
        } else {
            backing?.let { b ->
                if (b.reward() == null && b.amount().isNotNull()) {
                    currentUserReward = RewardFactory.noReward().toBuilder().pledgeAmount(b.amount()).build()
                    bonusAmount = b.amount()
                } else {
                    backedAddOns = b.addOns() ?: emptyList()
                    bonusAmount = b.bonusAmount()
                }
            }
        }

        return AddOnsBundleData(
            pledgeData = pledgeData,
            pledgeReason = pReason,
            projectData = projectData,
            project = project,
            currentUserReward = currentUserReward,
            shippingRule = shippingRule,
            pledgeFlowContext = flowContext,
            backing = backing,
            backedAddOns = backedAddOns,
            bonusAmount = bonusAmount
        )
    }
}
