package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.RoundingMode
import kotlin.math.min

@Deprecated(
    message = "Legacy monolithic path replaced by ProjectFeatureViewModel under com.kickstarter.features.projectpage.viewmodel.",
    replaceWith = ReplaceWith("ProjectFeatureViewModel", "com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel")
)
class BackingAddOnViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward].
         * @param projectData we get the Project for currency
         * @param AddOn  the actual addOn item loading on the ViewHolder
         * @param selectedShippingRule the selected shipping rule
         */
        fun configureWith(projectDataAndAddOn: Triple<ProjectData, Reward, ShippingRule>)

        /** Emits if the increase button has been pressed */
        fun addButtonIsVisible(isVisible: Boolean)

        /** Emits the current quantity displayed on the addons stepper */
        fun currentQuantity(quantity: Int)

        fun clearDisposables()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        /** Emits a pair with the add on title and the quantity in order to build the stylized title  */
        fun titleForAddOn(): Observable<String>

        /** Emits the add on's description.  */
        fun description(): Observable<String>

        /** Emits the add on's minimum amount */
        fun minimum(): Observable<CharSequence>

        /** Emits the add on's converted minimum amount */
        fun convertedMinimum(): Observable<CharSequence>

        /** Emits whether or not the conversion text view is visible */
        fun conversionIsGone(): Observable<Boolean>

        /** Emits the conversion amount */
        fun conversion(): Observable<CharSequence>

        /** Emits the reward items */
        fun rewardItems(): Observable<List<RewardsItem>>

        /** Emits whether or not the remaining quantity pill is visible */
        fun remainingQuantityPillIsGone(): Observable<Boolean>

        /** Emits whether or not the backer limit pill is visible */
        fun backerLimitPillIsGone(): Observable<Boolean>

        /** Emits the backer limit */
        fun backerLimit(): Observable<String>

        /** Emits the remaining quantity*/
        fun remainingQuantity(): Observable<String>

        /** Emits whether or not the shipping amount is visible */
        fun shippingAmountIsGone(): Observable<Boolean>

        /** Emits the shipping amount */
        fun shippingAmount(): Observable<String>

        /** Emits whether or not the countdown pill is gone */
        fun deadlineCountdownIsGone(): Observable<Boolean>

        /** Emits the reward to be used to calculate the deadline countdown */
        fun deadlineCountdown(): Observable<Reward>

        /** Emits whether or not the reward items list is gone */
        fun rewardItemsAreGone(): Observable<Boolean>

        /** Emits quantity selected for which id*/
        fun quantityPerId(): PublishSubject<Pair<Int, Long>>

        /** Emits the maximum quantity for available addon*/
        fun maxQuantity(): Observable<Int>

        /** Emits a boolean that determines if the local PickUp section should be hidden **/
        fun localPickUpIsGone(): Observable<Boolean>

        /** Emits the String with the Local Pickup Displayable name **/
        fun localPickUpName(): Observable<String>
    }

    /**
     *  Logic to handle the UI for backing `Add On` card
     *  Configuring the View for [BackingAddOnViewHolder]
     *  - No interaction with the user just displaying information
     *  - Loading in [BackingAddOnViewHolder] -> [BackingAddOnsAdapter] -> [BackingAddOnsFragment]
     */
    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class BackingAddOnViewHolderViewModel(
        private val environment: Environment,
        private val intent: Intent? = null
    ) : Inputs, Outputs {

        internal val ksCurrency = requireNotNull(environment.ksCurrency())
        internal val projectDataAndAddOn = PublishSubject.create<Triple<ProjectData, Reward, ShippingRule>>()
        internal val title = PublishSubject.create<String>()
        internal val description = PublishSubject.create<String>()
        internal val minimum = PublishSubject.create<CharSequence>()
        internal val convertedMinimum = PublishSubject.create<CharSequence>()
        internal val conversionIsGone = PublishSubject.create<Boolean>()
        internal val conversion = PublishSubject.create<CharSequence>()
        internal val rewardItems = PublishSubject.create<List<RewardsItem>>()
        internal val remainingQuantityPillIsGone = PublishSubject.create<Boolean>()
        internal val backerLimitPillIsGone = PublishSubject.create<Boolean>()
        internal val remainingQuantity = PublishSubject.create<String>()
        internal val backerLimit = PublishSubject.create<String>()
        internal val shippingAmountIsGone = PublishSubject.create<Boolean>()
        internal val shippingAmount = PublishSubject.create<String>()
        internal val deadlineCountdown = PublishSubject.create<Reward>()
        internal val deadlineCountdownIsGone = PublishSubject.create<Boolean>()
        internal val rewardItemsAreGone = PublishSubject.create<Boolean>()
        internal val addButtonIsVisible = PublishSubject.create<Boolean>()
        internal val quantity = PublishSubject.create<Int>()
        internal val quantityPerId = PublishSubject.create<Pair<Int, Long>>()
        internal val maxQuantity = PublishSubject.create<Int>()
        internal val localPickUpIsGone = BehaviorSubject.create<Boolean>()
        internal val localPickUpName = BehaviorSubject.create<String>()
        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            initPipelines(disposables)
        }

        // - Inputs
        override fun configureWith(projectDataAndAddOn: Triple<ProjectData, Reward, ShippingRule>) = this.projectDataAndAddOn.onNext(projectDataAndAddOn)
        override fun addButtonIsVisible(isVisible: Boolean) { this.addButtonIsVisible.onNext(isVisible) }
        override fun currentQuantity(quantity: Int) = this.quantity.onNext(quantity)

        // - Outputs
        override fun titleForAddOn(): PublishSubject<String> = this.title

        override fun description(): PublishSubject<String> = this.description

        override fun minimum(): PublishSubject<CharSequence> = this.minimum

        override fun convertedMinimum(): PublishSubject<CharSequence> = this.convertedMinimum

        override fun conversionIsGone(): PublishSubject<Boolean> = this.conversionIsGone

        override fun conversion(): PublishSubject<CharSequence> = this.conversion

        override fun rewardItems(): PublishSubject<List<RewardsItem>> = this.rewardItems

        override fun remainingQuantityPillIsGone(): PublishSubject<Boolean> = this.remainingQuantityPillIsGone

        override fun backerLimitPillIsGone(): PublishSubject<Boolean> = this.backerLimitPillIsGone

        override fun backerLimit(): PublishSubject<String> = this.backerLimit

        override fun remainingQuantity(): PublishSubject<String> = this.remainingQuantity

        override fun shippingAmountIsGone(): PublishSubject<Boolean> = this.shippingAmountIsGone

        override fun shippingAmount(): PublishSubject<String> = this.shippingAmount

        override fun deadlineCountdown(): PublishSubject<Reward> = this.deadlineCountdown

        override fun deadlineCountdownIsGone(): PublishSubject<Boolean> = this.deadlineCountdownIsGone

        override fun rewardItemsAreGone(): PublishSubject<Boolean> = this.rewardItemsAreGone

        override fun quantityPerId(): PublishSubject<Pair<Int, Long>> = this.quantityPerId

        override fun maxQuantity(): Observable<Int> = this.maxQuantity

        override fun localPickUpIsGone(): Observable<Boolean> = this.localPickUpIsGone

        override fun localPickUpName(): Observable<String> = this.localPickUpName

        override fun clearDisposables() {
            disposables.clear()
        }
    }
}
