package com.kickstarter.viewmodels

/**
 * ViewModel managing state transitions and user intents for RewardCardSelectedViewHolderViewModel.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
@Deprecated(
    message = "Legacy monolithic path replaced by RewardTrackingViews under com.kickstarter.features.rewardtracking.",
    replaceWith = ReplaceWith("RewardTrackingViews", "com.kickstarter.features.rewardtracking.RewardTrackingViews")
)
interface RewardCardSelectedViewHolderViewModel : BaseRewardCardViewHolderViewModel {

    interface Inputs : BaseRewardCardViewHolderViewModel.Inputs
    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs : BaseRewardCardViewHolderViewModel.Outputs

    class ViewModel : BaseRewardCardViewHolderViewModel.ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this
    }
}
