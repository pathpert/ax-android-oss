package com.kickstarter.features.backing.viewmodel

import android.util.Pair
import androidx.lifecycle.ViewModel
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.features.backing.data.BackingFeatureOutputs
import com.kickstarter.features.backing.data.BackingState
import com.kickstarter.libs.Either
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeStatusData
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Holder class providing reactive subject infrastructure and output dispatching for the Backing feature.
 *
 * Negative constraints:
 * - Do not mutate protected subjects directly from external callers; expose read-only Observables via [BackingFeatureOutputs].
 */
abstract class BackingOutputsHolder : ViewModel(), BackingFeatureOutputs {

    protected val fixPaymentMethodButtonClicked = PublishSubject.create<Unit>()
    protected val pledgeSuccessfullyCancelled = PublishSubject.create<Unit>()
    protected val projectDataInput = PublishSubject.create<ProjectData>()
    protected val receivedCheckboxToggled = PublishSubject.create<Boolean>()
    protected val refreshProject = PublishSubject.create<Unit>()
    protected val isExpanded = PublishSubject.create<Boolean>()
    protected val backingError = BehaviorSubject.create<ProblemDetails>()

    protected val backerAvatar = BehaviorSubject.create<String>()
    protected val backerName = BehaviorSubject.create<String>()
    protected val backerNumber = BehaviorSubject.create<String>()
    protected val cardExpiration = BehaviorSubject.create<String>()
    protected val cardIssuer = BehaviorSubject.create<Either<String, Int>>()
    protected val cardLastFour = BehaviorSubject.create<String>()
    protected val cardLogo = BehaviorSubject.create<Int>()
    protected val fixPaymentMethodButtonIsGone = BehaviorSubject.create<Boolean>()
    protected val fixPaymentMethodMessageIsGone = BehaviorSubject.create<Boolean>()
    protected val betaBadgeIsGone = BehaviorSubject.create<Boolean>()
    protected val notifyDelegateToRefreshProject = PublishSubject.create<Unit>()
    protected val notifyDelegateToShowFixPledge = PublishSubject.create<Unit>()
    protected val paymentMethodIsGone = BehaviorSubject.create<Boolean>()
    protected val pledgeAmount = BehaviorSubject.create<CharSequence>()
    protected val pledgeDate = BehaviorSubject.create<String>()
    protected val pledgeStatusData = BehaviorSubject.create<PledgeStatusData>()
    protected val pledgeSummaryIsGone = BehaviorSubject.create<Boolean>()
    protected val projectDataAndReward = BehaviorSubject.create<Pair<ProjectData, Reward>>()
    protected val receivedCheckboxChecked = BehaviorSubject.create<Boolean>()
    protected val receivedSectionIsGone = BehaviorSubject.create<Boolean>()
    protected val receivedSectionCreatorIsGone = BehaviorSubject.create<Boolean>()
    protected val shippingAmount = BehaviorSubject.create<CharSequence>()
    protected val shippingLocation = BehaviorSubject.create<String>()
    protected val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
    protected val showUpdatePledgeSuccess = PublishSubject.create<Unit>()
    protected val swipeRefresherProgressIsVisible = BehaviorSubject.create<Boolean>()
    protected val totalAmount = BehaviorSubject.create<CharSequence>()
    protected val addOnsList = BehaviorSubject.create<Pair<ProjectData, List<Reward>>>()
    protected val bonusSupport = BehaviorSubject.create<CharSequence>()
    protected val estimatedDelivery = BehaviorSubject.create<String>()
    protected val deliveryDisclaimerSectionIsGone = BehaviorSubject.create<Boolean>()
    protected val paymentIncrements = BehaviorSubject.create<List<PaymentIncrement>>()
    protected val pledgeIsPlot = BehaviorSubject.create<Boolean>()
    protected val backingStateSubject = BehaviorSubject.create<BackingState>()

    override fun backerAvatar(): Observable<String> = this.backerAvatar
    override fun backerName(): Observable<String> = this.backerName
    override fun backerNumber(): Observable<String> = this.backerNumber
    override fun cardExpiration(): Observable<String> = this.cardExpiration
    override fun cardIssuer(): Observable<Either<String, Int>> = this.cardIssuer
    override fun cardLastFour(): Observable<String> = this.cardLastFour
    override fun cardLogo(): Observable<Int> = this.cardLogo
    override fun fixPaymentMethodButtonIsGone(): Observable<Boolean> = this.fixPaymentMethodButtonIsGone
    override fun fixPaymentMethodMessageIsGone(): Observable<Boolean> = this.fixPaymentMethodMessageIsGone
    override fun betaBadgeIsGone(): Observable<Boolean> = this.betaBadgeIsGone
    override fun notifyDelegateToRefreshProject(): Observable<Unit> = this.notifyDelegateToRefreshProject
    override fun notifyDelegateToShowFixPledge(): Observable<Unit> = this.notifyDelegateToShowFixPledge
    override fun paymentMethodIsGone(): Observable<Boolean> = this.paymentMethodIsGone
    override fun pledgeAmount(): Observable<CharSequence> = this.pledgeAmount
    override fun pledgeDate(): Observable<String> = this.pledgeDate
    override fun pledgeStatusData(): Observable<PledgeStatusData> = this.pledgeStatusData
    override fun pledgeSummaryIsGone(): Observable<Boolean> = this.pledgeSummaryIsGone
    override fun projectDataAndReward(): Observable<Pair<ProjectData, Reward>> = this.projectDataAndReward
    override fun projectDataAndAddOns(): Observable<Pair<ProjectData, List<Reward>>> = this.addOnsList
    override fun receivedCheckboxChecked(): Observable<Boolean> = this.receivedCheckboxChecked
    override fun receivedSectionIsGone(): Observable<Boolean> = this.receivedSectionIsGone
    override fun receivedSectionCreatorIsGone(): Observable<Boolean> = this.receivedSectionCreatorIsGone
    override fun shippingAmount(): Observable<CharSequence> = this.shippingAmount
    override fun shippingLocation(): Observable<String> = this.shippingLocation
    override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone
    override fun showUpdatePledgeSuccess(): Observable<Unit> = this.showUpdatePledgeSuccess
    override fun swipeRefresherProgressIsVisible(): Observable<Boolean> = this.swipeRefresherProgressIsVisible
    override fun totalAmount(): Observable<CharSequence> = this.totalAmount
    override fun bonusSupport(): Observable<CharSequence> = this.bonusSupport
    override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery
    override fun deliveryDisclaimerSectionIsGone(): Observable<Boolean> = this.deliveryDisclaimerSectionIsGone
    override fun paymentIncrements(): Observable<List<PaymentIncrement>> = this.paymentIncrements
    override fun pledgeIsPlot(): Observable<Boolean> = this.pledgeIsPlot
    override fun backingError(): Observable<ProblemDetails> = this.backingError
    override fun backingState(): Observable<BackingState> = this.backingStateSubject
}
