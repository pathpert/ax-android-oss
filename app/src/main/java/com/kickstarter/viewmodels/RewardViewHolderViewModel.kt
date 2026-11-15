package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.VisibleForTesting
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

@Deprecated(
    message = "Legacy monolithic path replaced by RewardTrackingViews under com.kickstarter.features.rewardtracking.",
    replaceWith = ReplaceWith("RewardTrackingViews", "com.kickstarter.features.rewardtracking.RewardTrackingViews")
)
interface RewardViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward]. */
        fun configureWith(projectData: ProjectData, reward: Reward)

        /** Call when the user clicks on a reward. */
        fun rewardClicked(position: Int)
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        fun backersCount(): Observable<Int>
        fun backersCountIsGone(): Observable<Boolean>
        fun buttonCTA(): Observable<Int>
        fun buttonIsEnabled(): Observable<Boolean>
        fun buttonIsGone(): Observable<Boolean>
        fun conversion(): Observable<String>
        fun conversionIsGone(): Observable<Boolean>
        fun descriptionForNoReward(): Observable<Int>
        fun descriptionForReward(): Observable<String>
        fun descriptionIsGone(): Observable<Boolean>
        fun endDateSectionIsGone(): Observable<Boolean>
        fun estimatedDelivery(): Observable<String>
        fun estimatedDeliveryIsGone(): Observable<Boolean>
        fun limitContainerIsGone(): Observable<Boolean>
        fun minimumAmountTitle(): Observable<SpannableString>
        fun remaining(): Observable<Int>
        fun remainingIsGone(): Observable<Boolean>
        fun reward(): Observable<Reward>
        fun rewardItems(): Observable<List<RewardsItem>>
        fun rewardItemsAreGone(): Observable<Boolean>
        fun shippingSummary(): Observable<Pair<Int, String?>>
        fun shippingSummaryIsGone(): Observable<Boolean>
        fun showFragment(): Observable<Pair<Project, Reward>>
        fun titleIsGone(): Observable<Boolean>
        fun titleForNoReward(): Observable<Int>
        fun titleForReward(): Observable<String>
        fun hasAddOnsAvailable(): Observable<Boolean>
        fun isMinimumPledgeAmountGone(): Observable<Boolean>
        fun selectedRewardTagIsGone(): Observable<Boolean>
        fun localPickUpIsGone(): Observable<Boolean>
        fun localPickUpName(): Observable<String>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class ViewModel(val environment: Environment) : Inputs, Outputs {
        internal val currentUser = requireNotNull(environment.currentUserV2())
        internal val ksCurrency = requireNotNull(environment.ksCurrency())

        internal val projectDataAndReward = PublishSubject.create<Pair<ProjectData, Reward>>()
        internal val rewardClicked = PublishSubject.create<Int>()

        internal val backersCount = BehaviorSubject.create<Int>()
        internal val backersCountIsGone = BehaviorSubject.create<Boolean>()
        internal val buttonCTA = BehaviorSubject.create<Int>()
        internal val buttonIsEnabled = BehaviorSubject.create<Boolean>()
        internal val buttonIsGone = BehaviorSubject.create<Boolean>()
        internal val conversion = BehaviorSubject.create<String>()
        internal val conversionIsGone = BehaviorSubject.create<Boolean>()
        internal val descriptionForNoReward = BehaviorSubject.create<Int>()
        internal val descriptionForReward = BehaviorSubject.create<String>()
        internal val descriptionIsGone = BehaviorSubject.create<Boolean>()
        internal val endDateSectionIsGone = BehaviorSubject.create<Boolean>()
        internal val estimatedDelivery = BehaviorSubject.create<String>()
        internal val estimatedDeliveryIsGone = BehaviorSubject.create<Boolean>()
        internal val limitContainerIsGone = BehaviorSubject.create<Boolean>()
        internal val minimumAmountTitle = PublishSubject.create<SpannableString>()
        internal val remaining = BehaviorSubject.create<Int>()
        internal val remainingIsGone = BehaviorSubject.create<Boolean>()
        internal val reward = BehaviorSubject.create<Reward>()
        internal val rewardItems = BehaviorSubject.create<List<RewardsItem>>()
        internal val rewardItemsAreGone = BehaviorSubject.create<Boolean>()
        internal val shippingSummary = BehaviorSubject.create<Pair<Int, String?>>()
        internal val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        internal val showFragment = PublishSubject.create<Pair<Project, Reward>>()
        internal val titleForNoReward = BehaviorSubject.create<Int>()
        internal val titleForReward = BehaviorSubject.create<String>()
        internal val titleIsGone = BehaviorSubject.create<Boolean>()
        internal val addOnsAvailable = BehaviorSubject.create<Boolean>()
        internal val isMinimumPledgeAmountGone = BehaviorSubject.create<Boolean>()
        internal val selectedRewardTagIsGone = PublishSubject.create<Boolean>()
        internal val localPickUpIsGone = BehaviorSubject.create<Boolean>()
        internal val localPickUpName = BehaviorSubject.create<String>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onCAPIEventSent = BehaviorSubject.create<Boolean?>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        val disposables = CompositeDisposable()

        init {
            initPipelines()
        }

        override fun configureWith(projectData: ProjectData, reward: Reward) {
            this.projectDataAndReward.onNext(Pair.create(projectData, reward))
        }

        override fun rewardClicked(position: Int) {
            this.rewardClicked.onNext(position)
        }

        override fun backersCount(): Observable<Int> = this.backersCount
        override fun backersCountIsGone(): Observable<Boolean> = this.backersCountIsGone
        override fun buttonCTA(): Observable<Int> = this.buttonCTA
        override fun buttonIsEnabled(): Observable<Boolean> = this.buttonIsEnabled
        override fun buttonIsGone(): Observable<Boolean> = this.buttonIsGone
        override fun conversion(): Observable<String> = this.conversion
        override fun conversionIsGone(): Observable<Boolean> = this.conversionIsGone
        override fun descriptionForNoReward(): Observable<Int> = this.descriptionForNoReward
        override fun descriptionForReward(): Observable<String> = this.descriptionForReward
        override fun descriptionIsGone(): Observable<Boolean> = this.descriptionIsGone
        override fun endDateSectionIsGone(): Observable<Boolean> = this.endDateSectionIsGone
        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery
        override fun estimatedDeliveryIsGone(): Observable<Boolean> = this.estimatedDeliveryIsGone
        override fun limitContainerIsGone(): Observable<Boolean> = this.limitContainerIsGone
        override fun minimumAmountTitle(): Observable<SpannableString> = this.minimumAmountTitle
        override fun remaining(): Observable<Int> = this.remaining
        override fun remainingIsGone(): Observable<Boolean> = this.remainingIsGone
        override fun reward(): Observable<Reward> = this.reward
        override fun rewardItems(): Observable<List<RewardsItem>> = this.rewardItems
        override fun rewardItemsAreGone(): Observable<Boolean> = this.rewardItemsAreGone
        override fun shippingSummary(): Observable<Pair<Int, String?>> = this.shippingSummary
        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone
        override fun showFragment(): Observable<Pair<Project, Reward>> = this.showFragment
        override fun titleForNoReward(): Observable<Int> = this.titleForNoReward
        override fun titleForReward(): Observable<String> = this.titleForReward
        override fun titleIsGone(): Observable<Boolean> = this.titleIsGone
        override fun hasAddOnsAvailable(): Observable<Boolean> = this.addOnsAvailable
        override fun isMinimumPledgeAmountGone(): Observable<Boolean> = this.isMinimumPledgeAmountGone
        override fun selectedRewardTagIsGone(): Observable<Boolean> = this.selectedRewardTagIsGone
        override fun localPickUpIsGone(): Observable<Boolean> = this.localPickUpIsGone
        override fun localPickUpName(): Observable<String> = this.localPickUpName

        fun onCleared() {
            disposables.clear()
        }
    }
}
