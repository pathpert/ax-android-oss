package com.kickstarter.viewmodels

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
