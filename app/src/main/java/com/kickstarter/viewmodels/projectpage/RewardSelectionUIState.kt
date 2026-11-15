package com.kickstarter.viewmodels.projectpage

import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData

/**
 * UI State for [RewardsSelectionViewModel].
 */
data class RewardSelectionUIState(
    val selectedReward: Reward = Reward.builder().build(),
    val initialRewardIndex: Int = 0,
    val project: ProjectData = ProjectData.builder().build()
)
