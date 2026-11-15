package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import io.reactivex.Observable
import org.joda.time.DateTime
import java.math.RoundingMode

/**
 * ViewModel managing state transitions and user intents for RewardViewHolderViewModel.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
internal fun RewardViewHolderViewModel.ViewModel.initPipelines() {
    val reward = this.projectDataAndReward
        .map { it.second }
        .distinctUntilChanged()

    val project = this.projectDataAndReward
        .map { it.first.project() }

    val projectAndReward = project
        .compose<Pair<Project, Reward>>(combineLatestPair(reward))
        .map { Pair(it.first, it.second) }
        .distinctUntilChanged()

    projectAndReward
        .map {
            RewardViewUtils.styleCurrency(
                it.second.minimum(),
                it.first,
                this.ksCurrency
            )
        }
        .subscribe { this.minimumAmountTitle.onNext(it) }
        .addToDisposable(disposables)

    val userCreatedProject = this.currentUser.observable()
        .compose<Pair<KsOptional<User>?, Project>>(combineLatestPair(project))
        .map { it.first?.getValue()?.id() == it.second.creator().id() }

    projectAndReward
        .compose<Pair<Pair<Project, Reward>, Boolean>>(combineLatestPair(userCreatedProject))
        .map { RewardViewHolderViewModelHelper.buttonIsGone(it.first.first, it.first.second, it.second) }
        .distinctUntilChanged()
        .subscribe { this.buttonIsGone.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map { RewardViewUtils.pledgeButtonText(it.first, it.second) }
        .distinctUntilChanged()
        .subscribe { this.buttonCTA.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map { it.first }
        .map { it.currency() == it.currentCurrency() }
        .subscribe { this.conversionIsGone.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map {
            this.ksCurrency.format(
                it.second.convertedMinimum(),
                it.first,
                true,
                RoundingMode.HALF_UP,
                true
            )
        }
        .subscribe { this.conversion.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .filter { RewardUtils.isNoReward(it.second) }
        .map { it.first.backing()?.isBacked(it.second) ?: false }
        .map {
            when {
                it -> R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality
                else -> R.string.Back_it_because_you_believe_in_it
            }
        }
        .subscribe { this.descriptionForNoReward.onNext(it) }
        .addToDisposable(disposables)

    reward
        .filter { RewardUtils.isReward(it) }
        .filter { it.description().isNotNull() }
        .map { requireNotNull(it.description()) }
        .subscribe { this.descriptionForReward.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map { RewardUtils.isReward(it.second) && it.second.description().isNullOrEmpty() }
        .distinctUntilChanged()
        .subscribe { this.descriptionIsGone.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map { RewardViewHolderViewModelHelper.shouldContinueFlow(it.first, it.second) }
        .distinctUntilChanged()
        .subscribe { this.buttonIsEnabled.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map { it.first.isLive && RewardUtils.isLimited(it.second) }
        .map { it.negate() }
        .distinctUntilChanged()
        .subscribe { this.remainingIsGone.onNext(it) }
        .addToDisposable(disposables)

    reward
        .filter { RewardUtils.isLimited(it) }
        .map { it.remaining() ?: -1 }
        .subscribe { this.remaining.onNext(it) }
        .addToDisposable(disposables)

    reward
        .filter { RewardUtils.isItemized(it) }
        .filter { it.rewardsItems().isNotNull() }
        .map { requireNotNull(it.rewardsItems()) }
        .subscribe { this.rewardItems.onNext(it) }
        .addToDisposable(disposables)

    reward
        .map { RewardUtils.isItemized(it) }
        .map { it.negate() }
        .distinctUntilChanged()
        .subscribe { this.rewardItemsAreGone.onNext(it) }
        .addToDisposable(disposables)

    reward
        .subscribe { this.reward.onNext(it) }
        .addToDisposable(disposables)

    reward
        .map { it.hasAddons() }
        .subscribe { this.addOnsAvailable.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map { RewardViewHolderViewModelHelper.expirationDateIsGone(it.first, it.second) }
        .distinctUntilChanged()
        .subscribe { this.endDateSectionIsGone.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .filter { RewardViewHolderViewModelHelper.shouldContinueFlow(it.first, it.second) && it.first.isLive }
        .compose<Pair<Project, Reward>>(takeWhenV2(this.rewardClicked))
        .subscribe { this.showFragment.onNext(it) }
        .addToDisposable(disposables)

    this.projectDataAndReward
        .filter { it.first.project().isLive && !it.first.project().isBacking() }
        .compose(takeWhenV2(this.rewardClicked))
        .filter { PledgeData.with(PledgeFlowContext.NEW_PLEDGE, it.first, it.second).isNotNull() }
        .map { PledgeData.with(PledgeFlowContext.NEW_PLEDGE, it.first, it.second) }
        .subscribe { environment.analytics()?.trackSelectRewardCTA(it) }
        .addToDisposable(disposables)

    projectAndReward
        .filter { RewardUtils.isNoReward(it.second) }
        .map { it.first.backing()?.isBacked(it.second) ?: false }
        .map {
            when {
                it -> R.string.You_pledged_without_a_reward
                else -> R.string.Pledge_without_a_reward
            }
        }
        .subscribe { this.titleForNoReward.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map { it.first.backing()?.isBacked(it.second) ?: false }
        .subscribe { this.selectedRewardTagIsGone.onNext(!it) }
        .addToDisposable(disposables)

    reward
        .filter { RewardUtils.isReward(it) }
        .filter { it.title().isNotNull() }
        .map { requireNotNull(it.title()) }
        .subscribe { this.titleForReward.onNext(it) }
        .addToDisposable(disposables)

    reward
        .map { RewardUtils.isReward(it) && it.title().isNullOrEmpty() }
        .distinctUntilChanged()
        .subscribe { this.titleIsGone.onNext(it) }
        .addToDisposable(disposables)

    reward
        .filter { RewardUtils.isShippable(it) }
        .filter { RewardUtils.shippingSummary(it).isNotNull() }
        .map { requireNotNull(RewardUtils.shippingSummary(it)) }
        .subscribe { this.shippingSummary.onNext(it) }
        .addToDisposable(disposables)

    reward
        .filter { !RewardUtils.isShippable(it) }
        .map { RewardUtils.isLocalPickup(it) }
        .subscribe { this.localPickUpIsGone.onNext(!it) }
        .addToDisposable(disposables)

    reward
        .filter { !RewardUtils.isShippable(it) }
        .filter { RewardUtils.isLocalPickup(it) }
        .filter { it.localReceiptLocation()?.displayableName().isNotNull() }
        .map { requireNotNull(it.localReceiptLocation()?.displayableName()) }
        .subscribe { this.localPickUpName.onNext(it) }
        .addToDisposable(disposables)

    projectAndReward
        .map { it.first.isLive && RewardUtils.isShippable(it.second) }
        .map { it.negate() }
        .distinctUntilChanged()
        .subscribe { this.shippingSummaryIsGone.onNext(it) }
        .addToDisposable(disposables)

    Observable.combineLatest(
        this.endDateSectionIsGone,
        this.remainingIsGone,
        this.shippingSummaryIsGone,
        this.addOnsAvailable
    ) { endDateGone, remainingGone, shippingGone, addOnsAvailable ->
        endDateGone && remainingGone && shippingGone && !addOnsAvailable
    }
        .distinctUntilChanged()
        .subscribe { this.limitContainerIsGone.onNext(it) }
        .addToDisposable(disposables)

    reward
        .map { RewardUtils.isNoReward(it) || !RewardUtils.hasBackers(it) }
        .distinctUntilChanged()
        .subscribe { this.backersCountIsGone.onNext(it) }
        .addToDisposable(disposables)

    reward
        .filter { RewardUtils.isReward(it) && RewardUtils.hasBackers(it) }
        .filter { it.backersCount().isNotNull() }
        .map { requireNotNull(it.backersCount()) }
        .subscribe { this.backersCount.onNext(it) }
        .addToDisposable(disposables)

    reward
        .map { RewardUtils.isNoReward(it) || it.estimatedDeliveryOn().isNull() }
        .distinctUntilChanged()
        .subscribe { this.estimatedDeliveryIsGone.onNext(it) }
        .addToDisposable(disposables)

    reward
        .filter { RewardUtils.isReward(it) && it.estimatedDeliveryOn().isNotNull() }
        .map<DateTime> { it.estimatedDeliveryOn() }
        .map { DateTimeUtils.estimatedDeliveryOn(it) }
        .subscribe { this.estimatedDelivery.onNext(it) }
        .addToDisposable(disposables)

    reward.map { RewardUtils.isNoReward(it) }
        .subscribe { this.isMinimumPledgeAmountGone.onNext(it) }
        .addToDisposable(disposables)
}
