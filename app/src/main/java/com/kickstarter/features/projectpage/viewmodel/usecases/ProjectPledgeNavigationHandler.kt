package com.kickstarter.features.projectpage.viewmodel.usecases

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.features.projectpage.viewmodel.ProjectPageStateHolder
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isOrderPresentAndComplete
import com.kickstarter.libs.utils.extensions.pledgeManagementAvailable
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable

/**
 * Handler coordinating project page pledge sheet navigation, back stack
 * transitions, scrim visibility, and pledge flow continuation.
 *
 * Negative constraints:
 * - Do not trigger navigation without an initialized [ProjectPageStateHolder].
 */
class ProjectPledgeNavigationHandler(
    private val environment: Environment,
    private val state: ProjectPageStateHolder
) {
    private val currentUser = requireNotNull(environment.currentUserV2())
    private val featureFlagClient = environment.featureFlagClient()

    fun wire(currentProject: Observable<Project>) {
        val disposables = state.disposables

        state.activityResult
            .filter { it.isOk && it.isRequestCode(ActivityRequestCodes.SHOW_REWARDS) }
            .subscribe { state.expandPledgeSheet.onNext(Pair(true, true)) }
            .addToDisposable(disposables)

        state.intent
            .take(1)
            .filter { it.getBooleanExtra(IntentKey.EXPAND_PLEDGE_SHEET, false) }
            .subscribe { state.expandPledgeSheet.onNext(Pair(true, true)) }
            .addToDisposable(disposables)

        state.nativeProjectActionButtonClicked
            .withLatestFrom(currentProject) { _, project -> project }
            .subscribe { project ->
                val netNewBackFeatureFlag =
                    featureFlagClient?.getBoolean(FlagKey.ANDROID_COMPLETED_PM_CHECKOUT_WEBVIEW) ?: false
                val goToPMWebviewFeatureFlag =
                    featureFlagClient?.getBoolean(FlagKey.ANDROID_NET_NEW_BACKER_GO_TO_PM_WEBVIEW) ?: false

                if (project.backing()?.isOrderPresentAndComplete() == true && netNewBackFeatureFlag) {
                    project.backing()?.backingDetailsPageRoute()?.let {
                        state.openBackingDetailsWebview.onNext(it)
                    }
                } else if (project.pledgeManagementAvailable() && goToPMWebviewFeatureFlag) {
                    project.redemptionPageUrl()?.let { path ->
                        val redemptionPageUrl = UrlUtils.appendPath(environment.webEndpoint(), path)
                        state.openPledgeManagerWebview.onNext(redemptionPageUrl)
                    }
                } else {
                    state.continuePledgeFlow.onNext {
                        state.expandPledgeSheet.onNext(Pair(true, true))
                    }
                }
            }
            .addToDisposable(disposables)

        state.continuePledgeFlow
            .flatMap { callback ->
                currentUser.observable()
                    .take(1)
                    .flatMap { user ->
                        if (user.isPresent()) {
                            Observable.just(callback)
                        } else {
                            state.startLoginToutActivity.onNext(Unit)
                            currentUser.observable()
                                .filter { it.isPresent() }
                                .take(1)
                                .map { callback }
                        }
                    }
            }
            .subscribe { callback -> callback() }
            .addToDisposable(disposables)

        val fragmentStackCount = state.fragmentStackCount.startWith(0)

        fragmentStackCount
            .compose(takeWhenV2(state.pledgeToolbarNavigationClicked))
            .filter { it <= 0 }
            .map { Pair(false, true) }
            .subscribe { state.expandPledgeSheet.onNext(it) }
            .addToDisposable(disposables)

        fragmentStackCount
            .compose(takeWhenV2(state.pledgeToolbarNavigationClicked))
            .filter { it > 0 }
            .compose(ignoreValuesV2())
            .subscribe { state.goBack.onNext(it) }
            .addToDisposable(disposables)

        Observable.merge(state.pledgeSuccessfullyCancelled, state.pledgeSuccessfullyCreated)
            .map { Pair(false, false) }
            .subscribe { state.expandPledgeSheet.onNext(it) }
            .addToDisposable(disposables)

        currentProject
            .compose<Pair<Project, Int>>(combineLatestPair(fragmentStackCount))
            .map { if (it.second <= 0) R.drawable.ic_arrow_down else R.drawable.ic_arrow_back }
            .distinctUntilChanged()
            .subscribe { state.pledgeToolbarNavigationIcon.onNext(it) }
            .addToDisposable(disposables)

        state.fragmentStackCount
            .compose<Pair<Int, Project>>(combineLatestPair(currentProject))
            .map { if (it.second.isBacking()) it.first > 4 else it.first > 3 }
            .distinctUntilChanged()
            .subscribe { state.scrimIsVisible.onNext(it) }
            .addToDisposable(disposables)

        state.onGlobalLayout
            .subscribe { state.setInitialRewardPosition.onNext(it) }
            .addToDisposable(disposables)
    }
}
