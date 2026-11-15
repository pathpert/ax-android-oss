package com.kickstarter.viewmodels.projectpage

import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule

/**
 * Pure selection and ordering helpers for [RewardsSelectionViewModel].
 */
internal object RewardsSelectionHelper {

    fun getReward(backingObj: Backing?): Reward? {
        backingObj?.let { backing ->
            return backing.reward()?.let { reward ->
                if (backing.addOns().isNullOrEmpty()) reward
                else reward.toBuilder().hasAddons(true).build()
            } ?: RewardFactory.noReward()
        } ?: return null
    }

    fun indexOfBackedReward(project: Project): Int {
        project.rewards()?.run {
            for ((index, reward) in withIndex()) {
                if (project.backing()?.isBacked(reward) == true) {
                    return index
                }
            }
        }
        return 0
    }

    fun getDefaultLocation(
        config: Config,
        project: Project,
        shippingLocations: List<Location>,
    ): Location {
        val backingLocation =
            if (project.isBacking()) {
                val backingLocationId = project.backing()?.locationId()
                if (backingLocationId != null) {
                    shippingLocations.firstOrNull { it.id() == backingLocationId }
                } else {
                    null
                }
            } else {
                null
            }
        return backingLocation
            ?: shippingLocations.firstOrNull { it.country() == config.countryCode() }
            ?: shippingLocations.firstOrNull()
            ?: Location.builder().build()
    }

    fun toShippingRules(locations: List<Location>): List<ShippingRule> =
        locations.map { location -> ShippingRule.builder().location(location).build() }

    fun indexOfFirstUnselectableReward(
        rewards: List<Reward>,
        project: Project,
        selectedLocationId: Long?,
        backing: Backing?
    ): Int {
        return rewards.indexOfFirst { reward ->
            !RewardViewUtils.isRewardSelectable(reward, project, selectedLocationId, backing)
        }
    }

    fun repositionRewards(
        rewards: List<Reward>,
        project: Project,
        selectedLocationId: Long?,
        backing: Backing?
    ): List<Reward> {
        val noRewardOptionIndex = rewards.indexOfFirst { RewardUtils.isNoReward(it) }
        if (noRewardOptionIndex < 0) return rewards

        val mutableRewards = rewards.toMutableList()
        val noRewardOption = mutableRewards.removeAt(noRewardOptionIndex)
        val firstUnselectableRewardIndex =
            indexOfFirstUnselectableReward(mutableRewards, project, selectedLocationId, backing)
        if (firstUnselectableRewardIndex == -1) {
            mutableRewards.add(noRewardOption)
        } else {
            mutableRewards.add(firstUnselectableRewardIndex, noRewardOption)
        }
        return mutableRewards
    }
}
