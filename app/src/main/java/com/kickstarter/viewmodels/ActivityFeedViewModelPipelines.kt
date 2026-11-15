package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ApiPaginatorV2
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Activity
import com.kickstarter.models.Activity.Companion.CATEGORY_SHIPPED
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.ActivityEnvelope
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

/**
 * Rx stream wiring and paginator setup for [ActivityFeedViewModel.ActivityFeedViewModel].
 */
internal fun ActivityFeedViewModel.ActivityFeedViewModel.initActivityFeedPipelines(
    disposables: CompositeDisposable
) {
    goToDiscovery = discoverProjectsClick
    goToLogin = loginClick
    goToSurvey = surveyClick

    goToProject = Observable.merge(
        friendBackingClick,
        projectStateChangedClick,
        projectStateChangedPositiveClick,
        projectUpdateProjectClick
    ).map { obj: Activity -> obj.project() }

    goToProject
        .subscribe { p: Project ->
            analyticEvents.trackProjectCardClicked(
                p,
                EventContextValues.ContextPageName.ACTIVITY_FEED.contextName
            )
        }
        .addToDisposable(disposables)

    startUpdateActivity = projectUpdateClick

    val refreshOrResume = Observable.merge(refresh, resume).share()

    val loggedInUser = currentUser.loggedInUser()

    loggedInUser
        .compose(Transformers.takeWhenV2(refreshOrResume))
        .switchMap {
            apiClient.fetchUnansweredSurveys().compose(Transformers.neverErrorV2()).share()
        }
        .subscribe { surveys.onNext(it) }
        .addToDisposable(disposables)

    loggedInUser
        .compose(Transformers.takeWhenV2(refreshOrResume))
        .switchMap {
            apolloClient.erroredBackings().compose(Transformers.neverErrorV2()).share()
        }
        .subscribe { v: List<ErroredBacking> -> erroredBackings.onNext(v) }
        .addToDisposable(disposables)

    loggedInUser
        .compose(Transformers.takeWhenV2(refreshOrResume))
        .map { user: User ->
            user.unseenActivityCount().intValueOrZero() + user.erroredBackingsCount().intValueOrZero()
        }
        .filter { it.isNonZero() }
        .distinctUntilChanged()
        .switchMap {
            apolloClient.clearUnseenActivity().compose(Transformers.neverErrorV2())
        }
        .switchMap {
            apiClient.fetchCurrentUser().compose(Transformers.neverErrorV2())
        }
        .subscribe {
            currentUser.refresh(it)
        }
        .addToDisposable(disposables)

    val paginator = ApiPaginatorV2.builder<Activity, ActivityEnvelope, Unit>()
        .nextPage(nextPage)
        .startOverWith(refresh)
        .envelopeToListOfData { obj: ActivityEnvelope -> obj.activities() }
        .envelopeToMoreUrl { env: ActivityEnvelope -> env.urls().api().moreActivities() }
        .loadWithParams {
            apiClient.fetchActivities().compose(Transformers.neverErrorV2())
        }
        .loadWithPaginationPath { paginationPath: String ->
            apiClient.fetchActivitiesWithPaginationPath(
                paginationPath
            )
        }
        .build()

    paginator.paginatedData()
        .subscribe {
            if (!environment.featureFlagClient()
                ?.getBoolean(FlagKey.ANDROID_REWARD_SHIPMENT_TRACKING).isTrue()
            ) {
                activityList.onNext(it.filter { it.category() != CATEGORY_SHIPPED })
            } else {
                activityList.onNext(it)
            }
        }
        .addToDisposable(disposables)

    paginator.isFetching
        .subscribe { isFetchingActivities.onNext(it) }
        .addToDisposable(disposables)

    currentUser.loggedInUser()
        .take(1)
        .subscribe { refresh() }
        .addToDisposable(disposables)

    currentUser.isLoggedIn
        .map { loggedIn: Boolean -> !loggedIn }
        .subscribe { loggedOutEmptyStateIsVisible.onNext(it) }
        .addToDisposable(disposables)

    managePledgeClicked
        .subscribe { v: String -> startFixPledge.onNext(v) }
        .addToDisposable(disposables)

    currentUser.observable()
        .compose(Transformers.takePairWhenV2(activityList))
        .map { ua: Pair<KsOptional<User>, List<Activity>> -> ua.first != null && ua.second.isEmpty() }
        .subscribe { loggedInEmptyStateIsVisible.onNext(it) }
        .addToDisposable(disposables)

    // Track viewing and paginating activity.
    val feedViewed = nextPage
        .compose(Transformers.incrementalCountV2())
        .startWith(0)

    feedViewed
        .take(1)
        .subscribe { analyticEvents.trackActivityFeedPageViewed() }
        .addToDisposable(disposables)

    discoverProjectsClick
        .subscribe { analyticEvents.trackDiscoverProjectCTAClicked() }
        .addToDisposable(disposables)
}
