package com.kickstarter.features.backing.viewmodel

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.features.backing.data.BackingFeatureInputs
import com.kickstarter.features.backing.data.BackingFeatureOutputs
import com.kickstarter.features.backing.data.BackingRepository
import com.kickstarter.features.backing.data.BackingRepositoryImpl
import com.kickstarter.features.backing.data.BackingState
import com.kickstarter.features.backing.data.BackingStatusMapper
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProjectViewUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.libs.utils.extensions.isErroredWithPLOT
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Backing
import com.kickstarter.models.PaymentSource
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Modern ViewModel managing backing retrieval, reward state presentation, and status updates.
 *
 * Negative constraints:
 * - Do not expose mutable subjects directly; callers must observe via [BackingFeatureOutputs].
 */
open class BackingFeatureViewModel(
    val environment: Environment,
    customRepository: BackingRepository? = null
) : BackingOutputsHolder(), BackingFeatureInputs {

    private val repository: BackingRepository = customRepository ?: BackingRepositoryImpl(
        requireNotNull(environment.apolloClientV2()),
        requireNotNull(environment.apiClientV2())
    )

    private val ksCurrency = requireNotNull(this.environment.ksCurrency())
    private val analyticEvents = requireNotNull(this.environment.analytics())
    private val currentUser = requireNotNull(this.environment.currentUserV2())
    val ksString = this.environment.ksString()
    private val disposables = CompositeDisposable()

    open val inputs: BackingFeatureInputs = this
    open val outputs: BackingFeatureOutputs = this

    private val _backingStateFlow = MutableStateFlow<BackingState>(BackingState.Loading)
    val backingStateFlow: StateFlow<BackingState> = _backingStateFlow.asStateFlow()

    init {
        this.pledgeSuccessfullyCancelled
            .subscribe { this.showUpdatePledgeSuccess.onNext(it) }
            .addToDisposable(disposables)

        this.projectDataInput
            .distinctUntilChanged()
            .filter { it.project().isBacking() || it.project().userIsCreator(it.user()) }
            .map { projectData -> BackingStatusMapper.joinProjectDataAndReward(projectData) }
            .subscribe { this.projectDataAndReward.onNext(it) }
            .addToDisposable(disposables)

        val backedProject = this.projectDataInput.map { it.project() }

        val backingState = this.projectDataInput
            .switchMap { this.repository.getBackingInfo(it) }
            .share()

        backingState.subscribe {
            this.backingStateSubject.onNext(it)
            _backingStateFlow.value = it
        }.addToDisposable(disposables)

        val backing = backingState
            .ofType(BackingState.Success::class.java)
            .map { it.backing }
            .share()

        val rewardA = backing.filter { it.reward().isNotNull() }.map { requireNotNull(it.reward()) }
        val rewardB = projectDataAndReward.filter { it.second.isNotNull() }.map { requireNotNull(it.second) }
        val reward = Observable.merge(rewardA, rewardB).distinctUntilChanged()

        val isCreator = Observable.combineLatest(
            this.currentUser.observable(),
            backedProject
        ) { user, project -> Pair(user, project) }
            .map { it.second.userIsCreator(it.first.getValue()) }

        backing.map { it.paymentIncrements ?: emptyList() }
            .distinctUntilChanged().subscribe { this.paymentIncrements.onNext(it) }.addToDisposable(disposables)

        backing.map { !it.paymentIncrements.isNullOrEmpty() }
            .distinctUntilChanged().subscribe { this.pledgeIsPlot.onNext(it) }.addToDisposable(disposables)

        backing.filter { it.backerName().isNotNull() }.map { requireNotNull(it.backerName()) }
            .subscribe { this.backerName.onNext(it) }.addToDisposable(disposables)

        backing.filter { it.backerUrl().isNotNull() }.map { requireNotNull(it.backerUrl()) }
            .subscribe { this.backerAvatar.onNext(it) }.addToDisposable(disposables)

        backing.map { NumberUtils.format(it.sequence().toFloat()) }
            .distinctUntilChanged().subscribe { this.backerNumber.onNext(it) }.addToDisposable(disposables)

        backing.filter { it.pledgedAt().isNotNull() }.map { DateTimeUtils.longDate(requireNotNull(it.pledgedAt())) }
            .distinctUntilChanged().subscribe { this.pledgeDate.onNext(it) }.addToDisposable(disposables)

        backing.map { it.amount() - it.shippingAmount() - it.bonusAmount() }
            .filter { it.isNotNull() }
            .compose<Pair<Double, Project>>(combineLatestPair(backedProject))
            .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
            .distinctUntilChanged().subscribe { this.pledgeAmount.onNext(it) }.addToDisposable(disposables)

        backing.map { BackingStatusMapper.shouldHideShipping(it) }
            .distinctUntilChanged().subscribe { this.shippingSummaryIsGone.onNext(it) }.addToDisposable(disposables)

        backing.map { it.reward().isNull() }
            .distinctUntilChanged().subscribe { this.pledgeSummaryIsGone.onNext(it) }.addToDisposable(disposables)

        Observable.combineLatest(
            backedProject,
            backing,
            this.currentUser.loggedInUser()
        ) { p, b, user -> Triple(p, b, user) }
            .map {
                BackingStatusMapper.pledgeStatusData(
                    it.first,
                    it.second,
                    it.third,
                    this.ksCurrency,
                    this.environment.featureFlagClient(),
                    this.environment.webEndpoint()
                )
            }
            .distinctUntilChanged().subscribe { this.pledgeStatusData.onNext(it) }.addToDisposable(disposables)

        backing.filter { it.shippingAmount().isNotNull() }.map { requireNotNull(it.shippingAmount()) }
            .compose<Pair<Float, Project>>(combineLatestPair(backedProject))
            .map { ProjectViewUtils.styleCurrency(it.first.toDouble(), it.second, this.ksCurrency) }
            .distinctUntilChanged().subscribe { this.shippingAmount.onNext(it) }.addToDisposable(disposables)

        backing.filter { it.locationName().isNotNull() }.map { requireNotNull(it.locationName()) }
            .distinctUntilChanged().subscribe { this.shippingLocation.onNext(it) }.addToDisposable(disposables)

        backing.filter { it.amount().isNotNull() }.map { it.amount() }
            .compose<Pair<Double, Project>>(combineLatestPair(backedProject))
            .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
            .distinctUntilChanged().subscribe { this.totalAmount.onNext(it) }.addToDisposable(disposables)

        backing.map { CreditCardPaymentType.safeValueOf(it.paymentSource()?.paymentType() ?: "") }
            .map { it == CreditCardPaymentType.ANDROID_PAY || it == CreditCardPaymentType.APPLE_PAY || it == CreditCardPaymentType.CREDIT_CARD || it == CreditCardPaymentType.BANK_ACCOUNT }
            .map { it.negate() }
            .distinctUntilChanged().subscribe { this.paymentMethodIsGone.onNext(it) }.addToDisposable(disposables)

        backingState.ofType(BackingState.Loading::class.java).map { true }.subscribe(swipeRefresherProgressIsVisible::onNext).addToDisposable(disposables)
        backingState.ofType(BackingState.Error::class.java).map { false }.subscribe(swipeRefresherProgressIsVisible::onNext).addToDisposable(disposables)
        backingState.ofType(BackingState.Success::class.java).map { false }.subscribe(swipeRefresherProgressIsVisible::onNext).addToDisposable(disposables)
        backingState.ofType(BackingState.Error::class.java).map { it.problem }.subscribe(backingError::onNext).addToDisposable(disposables)

        val paymentSource = backing.filter { it.paymentSource().isNotNull() }.map { requireNotNull(it.paymentSource()) }.ofType(PaymentSource::class.java)
        val simpleDateFormat = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

        paymentSource.map { source -> source.expirationDate()?.let { simpleDateFormat.format(it) } ?: "" }
            .distinctUntilChanged().subscribe { this.cardExpiration.onNext(it) }.addToDisposable(disposables)
        paymentSource.map { BackingStatusMapper.cardIssuer(it) }
            .distinctUntilChanged().subscribe { this.cardIssuer.onNext(it) }.addToDisposable(disposables)
        paymentSource.map { it.lastFour() ?: "" }
            .distinctUntilChanged().subscribe { this.cardLastFour.onNext(it) }.addToDisposable(disposables)
        paymentSource.map { BackingStatusMapper.cardLogo(it) }
            .distinctUntilChanged().subscribe { this.cardLogo.onNext(it) }.addToDisposable(disposables)

        val backingIsErroredWithPlot = backing.map { it.isErroredWithPLOT() }.distinctUntilChanged().map { it.negate() }
        backingIsErroredWithPlot.subscribe { this.betaBadgeIsGone.onNext(it) }.addToDisposable(disposables)

        val backingIsNotErrored = backing.map { it.isErrored() && !it.isErroredWithPLOT() }.distinctUntilChanged().map { it.negate() }
        backingIsNotErrored.subscribe { this.fixPaymentMethodButtonIsGone.onNext(it) }.addToDisposable(disposables)
        backingIsNotErrored.subscribe { this.fixPaymentMethodMessageIsGone.onNext(it) }.addToDisposable(disposables)

        this.fixPaymentMethodButtonClicked.subscribe { this.notifyDelegateToShowFixPledge.onNext(Unit) }.addToDisposable(disposables)

        backing.map { it.completedByBacker() }.distinctUntilChanged()
            .subscribe { this.receivedCheckboxChecked.onNext(it) }.addToDisposable(disposables)

        backing.compose<Pair<Backing, Project>>(combineLatestPair(backedProject))
            .compose(takePairWhenV2(this.receivedCheckboxToggled))
            .switchMap { this.repository.postBackingReceived(it.first.second, it.first.first, it.second).compose(neverErrorV2()) }
            .share().subscribe()

        this.isExpanded.filter { it }.compose(combineLatestPair(backing)).map { it.second }
            .compose<Pair<Backing, ProjectData>>(combineLatestPair(projectDataInput))
            .subscribe { this.analyticEvents.trackManagePledgePageViewed(it.first, it.second) }.addToDisposable(disposables)

        val rewardIsReceivable = reward.map { RewardUtils.isReward(it) && it.estimatedDeliveryOn().isNotNull() }
        val backingIsCollected = backing.map { it.status() == Backing.STATUS_COLLECTED }.distinctUntilChanged()
        val sectionShouldBeGone = rewardIsReceivable.compose(combineLatestPair<Boolean, Boolean>(backingIsCollected))
            .map { (it.first && it.second).negate() }.distinctUntilChanged()

        sectionShouldBeGone.compose<Pair<Boolean, Boolean>>(combineLatestPair(isCreator)).subscribe {
            val isUserCreator = it.second
            val shouldBeGone = it.first
            if (isUserCreator) {
                this.receivedSectionIsGone.onNext(true)
                this.receivedSectionCreatorIsGone.onNext(shouldBeGone)
            } else {
                this.receivedSectionIsGone.onNext(shouldBeGone)
                this.receivedSectionCreatorIsGone.onNext(true)
            }
        }.addToDisposable(disposables)

        this.refreshProject.subscribe {
            this.notifyDelegateToRefreshProject.onNext(Unit)
            this.swipeRefresherProgressIsVisible.onNext(true)
        }.addToDisposable(disposables)

        val refreshTimeout = this.notifyDelegateToRefreshProject.delay(10, TimeUnit.SECONDS)
        Observable.merge(refreshTimeout, backedProject.skip(1)).map { false }
            .subscribe { this.swipeRefresherProgressIsVisible.onNext(it) }.addToDisposable(disposables)

        val addOns = backing.map { it.addOns()?.toList() ?: emptyList() }
        projectDataInput.compose<Pair<ProjectData, List<Reward>>>(combineLatestPair(addOns))
            .subscribe { this.addOnsList.onNext(it) }.addToDisposable(disposables)

        backing.filter { it.bonusAmount().isNotNull() }.map { requireNotNull(it.bonusAmount()) }
            .compose<Pair<Double, Project>>(combineLatestPair(backedProject))
            .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
            .distinctUntilChanged().subscribe { this.bonusSupport.onNext(it) }.addToDisposable(disposables)

        reward.filter { RewardUtils.isReward(it) && it.estimatedDeliveryOn().isNotNull() }
            .map<DateTime> { it.estimatedDeliveryOn() }
            .map { DateTimeUtils.estimatedDeliveryOn(it) }
            .subscribe { this.estimatedDelivery.onNext(it) }.addToDisposable(disposables)

        isCreator.subscribe { this.deliveryDisclaimerSectionIsGone.onNext(it) }.addToDisposable(disposables)
    }

    override fun configureWith(projectData: ProjectData) = this.projectDataInput.onNext(projectData)
    override fun fixPaymentMethodButtonClicked() = this.fixPaymentMethodButtonClicked.onNext(Unit)
    override fun pledgeSuccessfullyUpdated() = this.showUpdatePledgeSuccess.onNext(Unit)
    override fun receivedCheckboxToggled(checked: Boolean) = this.receivedCheckboxToggled.onNext(checked)
    override fun refreshProject() = this.refreshProject.onNext(Unit)
    override fun isExpanded(state: Boolean?) { state?.let { this.isExpanded.onNext(it) } }

    override fun onCleared() {
        this.repository.cleanDisposables()
        this.disposables.clear()
        super.onCleared()
    }

    /**
     * Factory producing [BackingFeatureViewModel] instances with injected environment and repository.
     *
     * Negative constraints:
     * - Do not instantiate without a valid [Environment].
     */
    class Factory(
        private val environment: Environment,
        private val repository: BackingRepository? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BackingFeatureViewModel(environment, repository) as T
        }
    }
}
