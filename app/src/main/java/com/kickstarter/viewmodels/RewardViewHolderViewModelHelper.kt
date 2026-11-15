package com.kickstarter.viewmodels

import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.models.Project
import com.kickstarter.models.Reward

internal object RewardViewHolderViewModelHelper {

    fun shouldContinueFlow(project: Project, rw: Reward): Boolean {
        val hasAddOns = rw.hasAddons()
        val backedRwId = project.backing()?.rewardId()
        val selectingOtherRw = backedRwId != rw.id()

        return when {
            !hasAddOns && isSelectable(project, rw) -> true
            selectingOtherRw && RewardUtils.isAvailableForProject(project, rw) -> true
            hasAddOns && !selectingOtherRw && project.isLive -> true
            else -> false
        }
    }

    fun buttonIsGone(
        project: Project,
        reward: Reward,
        userCreatedProject: Boolean
    ): Boolean {
        return when {
            userCreatedProject -> true
            project.backing()?.isBacked(reward) ?: false || project.isLive -> false
            else -> true
        }
    }

    fun expirationDateIsGone(project: Project, reward: Reward): Boolean {
        return when {
            !project.isLive -> true
            RewardUtils.isTimeLimitedEnd(reward) -> RewardUtils.isExpired(reward)
            else -> true
        }
    }

    fun isSelectable(project: Project, reward: Reward): Boolean {
        if (project.backing()?.isBacked(reward) == true) {
            return false
        }
        return RewardUtils.isAvailableForProject(project, reward)
    }
}
