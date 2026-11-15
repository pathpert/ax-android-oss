package com.kickstarter.features.projectpage.viewmodel.usecases

import android.util.Pair
import com.kickstarter.features.projectpage.data.ProjectBackingDetailsMapper
import com.kickstarter.features.projectpage.viewmodel.ProjectPageStateHolder
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.ProjectViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.backedReward
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.libs.utils.extensions.showLatePledgeFlow
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.helpers.createManagePledgeMenuOptions
import io.reactivex.Observable

/**
 * Use case coordinating pledge call-to-actions, reward options,
 * backing status details, and payment update/fix interactions.
 *
 * Negative constraints:
 * - Do not invoke [wire] with unbound observables.
 */
class ProjectPledgeCtaUseCase(
    private val environment: Environment,
    private val state: ProjectPageStateHolder
) {
    private val currentUser = requireNotNull(environment.currentUserV2())
    private val ksCurrency = requireNotNull(environment.ksCurrency())
    private val featureFlagClient = requireNotNull(environment.featureFlagClient())
    private val analyticEvents = requireNotNull(environment.analytics())
    private val navigationHandler = ProjectPledgeNavigationHandler(environment, state)

    fun wire(
        currentProject: Observable<Project>,
        currentProjectData: Observable<ProjectData>,
        mappedProjectErrors: Observable<Throwable?>,
        mappedProjectValues: Observable<Project>
    ) {
        val disposables = state.disposables

        navigationHandler.wire(currentProject)

        val pledgeSheetExpanded = state.expandPledgeSheet
            .map { it.first }
            .startWith(false)

        val projectHasRewardsAndSheetCollapsed = currentProject
            .map { it.hasRewards() }
            .distinctUntilChanged()
            .compose<Pair<Boolean, Boolean>>(combineLatestPair(pledgeSheetExpanded))
            .filter { it.second.isFalse() }
            .map { it.first }

        val rewardsLoaded = projectHasRewardsAndSheetCollapsed
            .filter { it.isTrue() }
            .map { true }

        Observable.merge(rewardsLoaded, state.reloadProjectContainerClicked.map { true })
            .subscribe { state.reloadProjectContainerIsGone.onNext(it) }
            .addToDisposable(disposables)

        mappedProjectErrors
            .map { false }
            .subscribe { state.reloadProjectContainerIsGone.onNext(it) }
            .addToDisposable(disposables)

        projectHasRewardsAndSheetCollapsed
            .compose<Pair<Boolean, Boolean>>(combineLatestPair(state.retryProgressBarIsGone))
            .map { (it.first && it.second).negate() }
            .distinctUntilChanged()
            .subscribe { state.pledgeActionButtonContainerIsGone.onNext(it) }
            .addToDisposable(disposables)

        currentProjectData
            .filter { it.project().hasRewards() && !it.project().isBacking() }
            .distinctUntilChanged()
            .subscribe { state.updateFragments.onNext(it) }
            .addToDisposable(disposables)

        currentProject
            .map { createManagePledgeMenuOptions(it, featureFlagClient) }
            .distinctUntilChanged()
            .subscribe { state.managePledgeMenuOptions.onNext(it) }
            .addToDisposable(disposables)

        val backedProject = currentProject.filter { it.isBacking() }
        val projectBacking = backedProject.filter { it.backing().isNotNull() }.map { requireNotNull(it) }
        val backing = projectBacking.map { requireNotNull(it.backing()) }

        val isAdmin = currentUser.observable()
            .filter { it.isPresent() }
            .map { requireNotNull(it.getValue()) }
            .filter { it.isAdmin() && featureFlagClient.getBoolean(FlagKey.ANDROID_PLEDGE_REDEMPTION) }

        Observable.combineLatest(projectBacking, isAdmin) { pBacking, adminUser ->
            Pair(pBacking, adminUser)
        }
            .subscribe {
                environment.firebaseAnalyticsClient()?.sendUserId(it.second)
                state.showPledgeRedemptionScreen.onNext(it)
            }
            .addToDisposable(disposables)

        currentProjectData
            .filter { it.project().hasRewards() }
            .compose<Pair<ProjectData, Backing>>(combineLatestPair(backing))
            .map {
                val updatedProject = if (it.first.project().isBacking()) {
                    it.first.project().toBuilder().backing(it.second).build()
                } else it.first.project()

                it.first.toBuilder().project(updatedProject).build()
            }
            .subscribe { state.updateFragments.onNext(it) }
            .addToDisposable(disposables)

        backedProject
            .compose<Project>(takeWhenV2(state.cancelPledgeClicked))
            .filter { (it.backing()?.cancelable() ?: false).isTrue() }
            .subscribe { state.showCancelPledgeFragment.onNext(it) }
            .addToDisposable(disposables)

        backedProject
            .compose(takeWhenV2(state.cancelPledgeClicked))
            .filter { it.backing()?.cancelable().isFalse() }
            .compose(ignoreValuesV2())
            .subscribe { state.showPledgeNotCancelableDialog.onNext(it) }
            .addToDisposable(disposables)

        val projectDataAndBackedReward = currentProjectData
            .compose<Pair<ProjectData, Backing>>(combineLatestPair(backing))
            .filter { it.first.project().backing().isNotNull() }
            .filter { it.first.project().backing()?.backedReward(it.first.project()).isNotNull() }
            .map { pD ->
                pD.first.project().backing()?.backedReward(pD.first.project())?.let {
                    Pair(pD.first.toBuilder().backing(pD.second).build(), it)
                }
            }

        projectDataAndBackedReward
            .compose(takeWhenV2(state.fixPaymentMethodButtonClicked))
            .map {
                Pair(
                    PledgeData.with(PledgeFlowContext.FIX_ERRORED_PLEDGE, it.first, it.second),
                    PledgeReason.FIX_PLEDGE
                )
            }
            .subscribe { state.updatePledgeData.onNext(it) }
            .addToDisposable(disposables)

        projectDataAndBackedReward
            .compose(takeWhenV2(state.updatePaymentClicked))
            .map {
                Pair(
                    PledgeData.with(PledgeFlowContext.MANAGE_REWARD, it.first, it.second),
                    PledgeReason.UPDATE_PAYMENT
                )
            }
            .subscribe {
                state.updatePledgeData.onNext(it)
                analyticEvents.trackChangePaymentMethod(it.first)
            }
            .addToDisposable(disposables)

        state.updatePledgeData
            .subscribe { state.showUpdatePledge.onNext(it) }
            .addToDisposable(disposables)

        state.viewRewardsClicked
            .subscribe { state.revealRewardsFragment.onNext(it) }
            .addToDisposable(disposables)

        currentProject
            .map { ProjectBackingDetailsMapper.backingDetailsIsVisible(it) }
            .distinctUntilChanged()
            .subscribe { state.backingDetailsIsVisible.onNext(it) }
            .addToDisposable(disposables)

        currentProject
            .filter { it.isBacking() }
            .map { ProjectBackingDetailsMapper.backingDetailsTitle(it) }
            .distinctUntilChanged()
            .subscribe { state.backingDetailsTitle.onNext(it) }
            .addToDisposable(disposables)

        currentProject
            .filter { it.isBacking() }
            .map { ProjectBackingDetailsMapper.backingDetailsSubtitle(it, ksCurrency) }
            .filter { it.isNotNull() }
            .map { requireNotNull(it) }
            .distinctUntilChanged()
            .subscribe { state.backingDetailsSubtitle.onNext(it) }
            .addToDisposable(disposables)

        Observable.combineLatest(currentProjectData, currentUser.observable()) { data, user ->
            ProjectViewUtils.pledgeActionButtonText(data.project(), user.getValue())
        }
            .distinctUntilChanged()
            .subscribe { state.pledgeActionButtonText.onNext(it) }
            .addToDisposable(disposables)

        val currentProjectAndUser = currentProject.compose(combineLatestPair(currentUser.observable()))

        currentProjectAndUser
            .map { ProjectViewUtils.pledgeToolbarTitle(it.first, it.second.getValue()) }
            .distinctUntilChanged()
            .subscribe { state.pledgeToolbarTitle.onNext(it) }
            .addToDisposable(disposables)

        currentProjectAndUser
            .map { ProjectViewUtils.pledgeActionButtonColor(it.first, it.second.getValue()) }
            .distinctUntilChanged()
            .subscribe { state.pledgeActionButtonColor.onNext(it) }
            .addToDisposable(disposables)

        state.pledgePaymentSuccessfullyUpdated
            .subscribe { state.showUpdatePledgeSuccess.onNext(it) }
            .addToDisposable(disposables)

        state.pledgeSuccessfullyCancelled
            .subscribe { state.showCancelPledgeSuccess.onNext(it) }
            .addToDisposable(disposables)

        state.pledgeSuccessfullyCreated
            .subscribe { state.startThanksActivity.onNext(it) }
            .addToDisposable(disposables)

        state.pledgeSuccessfullyUpdated
            .subscribe { state.showUpdatePledgeSuccess.onNext(it) }
            .addToDisposable(disposables)

        mappedProjectValues
            .subscribe {
                if (it.showLatePledgeFlow()) {
                    state.showLatePledgeFlow.onNext(it.showLatePledgeFlow())
                }
                if (it.displayPrelaunch().isTrue()) {
                    state.prelaunchUrl.onNext(it.webProjectUrl())
                }
            }
            .addToDisposable(disposables)
    }
}
