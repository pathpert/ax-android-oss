package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Reward
import io.reactivex.disposables.CompositeDisposable
import java.math.RoundingMode

/**
 * Rx stream wiring and pipeline initialization for [BackingAddOnViewHolderViewModel.BackingAddOnViewHolderViewModel].
 */
internal fun BackingAddOnViewHolderViewModel.BackingAddOnViewHolderViewModel.initPipelines(
    disposables: CompositeDisposable
) {
    val addOn = projectDataAndAddOn
        .map { it.second }

    addOn
        .map { it.title() }
        .subscribe(this.title)

    addOn
        .map { it.description() }
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }
        .subscribe { this.description.onNext(it) }
        .addToDisposable(disposables)

    addOn.map { !RewardUtils.isItemized(it) }
        .subscribe { this.rewardItemsAreGone.onNext(it) }
        .addToDisposable(disposables)

    addOn.filter { RewardUtils.isItemized(it) }
        .map { if (it.isAddOn()) it.addOnsItems() else it.rewardsItems() }
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }
        .subscribe { this.rewardItems.onNext(it) }
        .addToDisposable(disposables)

    projectDataAndAddOn.map { this.ksCurrency.format(it.second.minimum(), it.first.project()) }
        .subscribe { this.minimum.onNext(it) }
        .addToDisposable(disposables)

    projectDataAndAddOn.map { this.ksCurrency.format(it.second.convertedMinimum(), it.first.project()) }
        .subscribe { this.convertedMinimum.onNext(it) }
        .addToDisposable(disposables)

    projectDataAndAddOn.map { it.first.project() }
        .map { it.currency() == it.currentCurrency() }
        .subscribe { this.conversionIsGone.onNext(it) }
        .addToDisposable(disposables)

    projectDataAndAddOn
        .map { this.ksCurrency.format(it.second.convertedMinimum(), it.first.project(), true, RoundingMode.HALF_UP, true) }
        .subscribe { this.conversion.onNext(it) }
        .addToDisposable(disposables)

    projectDataAndAddOn
        .map { !it.second.limit().isNotNull() }
        .subscribe { this.backerLimitPillIsGone.onNext(it) }
        .addToDisposable(disposables)

    addOn
        .map { !it.remaining().isNotNull() }
        .subscribe { this.remainingQuantityPillIsGone.onNext(it) }
        .addToDisposable(disposables)

    addOn.map { it.limit().toString() }
        .subscribe { this.backerLimit.onNext(it) }
        .addToDisposable(disposables)

    addOn
        .filter { it.remaining().isNotNull() }
        .map { it.remaining().toString() }
        .subscribe { this.remainingQuantity.onNext(it) }
        .addToDisposable(disposables)

    projectDataAndAddOn.map { it.second.endsAt().isNotNull() }
        .map { !it }
        .subscribe { this.deadlineCountdownIsGone.onNext(it) }
        .addToDisposable(disposables)

    projectDataAndAddOn.map { it.second }
        .subscribe { this.deadlineCountdown.onNext(it) }
        .addToDisposable(disposables)

    addOn.map { it.shippingRules()?.isEmpty() }
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }
        .subscribe { this.shippingAmountIsGone.onNext(it) }
        .addToDisposable(disposables)

    projectDataAndAddOn.map {
        BackingAddOnCalculationHelper.getShippingCost(this.ksCurrency, it.second.shippingRules(), it.first.project(), it.third)
    }
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }
        .subscribe { this.shippingAmount.onNext(it) }
        .addToDisposable(disposables)

    addOn
        .map { it?.quantity() ?: 0 }
        .distinctUntilChanged()
        .subscribe { this.quantity.onNext(it) }
        .addToDisposable(disposables)

    addOn
        .map { BackingAddOnCalculationHelper.maximumLimit(it) }
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }
        .subscribe { this.maxQuantity.onNext(it) }
        .addToDisposable(disposables)

    addOn
        .filter { !RewardUtils.isShippable(it) }
        .map {
            RewardUtils.isLocalPickup(it)
        }
        .subscribe {
            this.localPickUpIsGone.onNext(!it)
        }
        .addToDisposable(disposables)

    addOn
        .filter { !RewardUtils.isShippable(it) }
        .filter { RewardUtils.isLocalPickup(it) }
        .map { it.localReceiptLocation()?.displayableName() }
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }
        .subscribe { this.localPickUpName.onNext(it) }
        .addToDisposable(disposables)

    this.quantity
        .compose<Pair<Int, Reward>>(combineLatestPair(addOn))
        .map { data -> Pair(data.first, data.second.id()) }
        .distinctUntilChanged { quantityPerId1, quantityPerId2 ->
            quantityPerId1.first == quantityPerId2.first && quantityPerId1.second == quantityPerId2.second
        }
        .subscribe { this.quantityPerId.onNext(it) }
        .addToDisposable(disposables)
}
