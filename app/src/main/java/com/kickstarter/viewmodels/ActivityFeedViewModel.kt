package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.ApiPaginatorV2
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
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
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.ActivityEnvelope
import com.kickstarter.ui.adapters.ActivityFeedAdapter
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder
import com.kickstarter.ui.viewholders.FriendBackingViewHolder
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder
import com.kickstarter.ui.viewholders.RewardShippedViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

@Deprecated(
    message = "Legacy monolithic path replaced by HomeScreenViewModel under com.kickstarter.features.home.viewmodel.",
    replaceWith = ReplaceWith("HomeScreenViewModel", "com.kickstarter.features.home.viewmodel.HomeScreenViewModel")
)
interface ActivityFeedViewModel {
    interface Inputs : ActivityFeedAdapter.Delegate {
        /** Invoke when pagination should happen.  */
        fun nextPage()

        /** Invoke when activity's onResume runs  */
        fun resume()

        /** Invoke when the feed should be refreshed.  */
        fun refresh()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        /** Emits a list of activities representing the user's activity feed.  */
        fun activityList(): Observable<List<Activity>>

        /** Emits a list of the user's errored backings.  */
        fun erroredBackings(): Observable<List<ErroredBacking>>

        /** Emits when view should be returned to Discovery projects.  */
        fun goToDiscovery(): Observable<Unit>

        /** Emits when login tout should be shown.  */
        fun goToLogin(): Observable<Unit>

        /** Emits a project when it should be shown.  */
        fun goToProject(): Observable<Project>

        /** Emits a SurveyResponse when it should be shown.  */
        fun goToSurvey(): Observable<SurveyResponse>

        /** Emits a boolean indicating whether activities are being fetched from the API.  */
        fun isFetchingActivities(): Observable<Boolean>

        /** Emits a boolean that determines if a logged-out, empty state should be displayed.  */
        fun loggedOutEmptyStateIsVisible(): Observable<Boolean>

        /** Emits a logged-in user with zero activities in order to display an empty state.  */
        fun loggedInEmptyStateIsVisible(): Observable<Boolean>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectPageActivity].  */
        fun startFixPledge(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.UpdateActivity].  */
        fun startUpdateActivity(): Observable<Activity>

        /** Emits when we should open the browser to view shipment tracking.  */
        fun trackShipmentClicked(): Observable<String>

        /** Emits a list of unanswered surveys to be shown in the user's activity feed  */
        fun surveys(): Observable<List<SurveyResponse>>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class ActivityFeedViewModel(internal val environment: Environment) : ViewModel(), Inputs, Outputs {

        internal val apiClient: ApiClientTypeV2
        internal val apolloClient: ApolloClientTypeV2
        internal val currentUser: CurrentUserTypeV2
        internal val analyticEvents: AnalyticEvents

        internal val discoverProjectsClick = PublishSubject.create<Unit>()
        internal val friendBackingClick = PublishSubject.create<Activity>()
        internal val loginClick = PublishSubject.create<Unit>()
        internal val managePledgeClicked = PublishSubject.create<String>()
        internal val trackShipmentClicked = PublishSubject.create<String>()
        internal val nextPage = PublishSubject.create<Unit>()
        internal val projectStateChangedClick = PublishSubject.create<Activity>()
        internal val projectStateChangedPositiveClick = PublishSubject.create<Activity>()
        internal val projectUpdateClick = PublishSubject.create<Activity>()
        internal val projectUpdateProjectClick = PublishSubject.create<Activity>()
        internal val refresh = PublishSubject.create<Unit>()
        internal val resume = PublishSubject.create<Unit>()
        internal val surveyClick = PublishSubject.create<SurveyResponse>()
        internal val activityList = BehaviorSubject.create<List<Activity>>()
        internal val erroredBackings = BehaviorSubject.create<List<ErroredBacking>>()
        internal var goToDiscovery: Observable<Unit> = Observable.empty()
        internal var goToLogin: Observable<Unit> = Observable.empty()
        internal var goToProject: Observable<Project> = Observable.empty()
        internal var goToSurvey: Observable<SurveyResponse> = Observable.empty()
        internal val isFetchingActivities = BehaviorSubject.create<Boolean>()
        internal val loggedInEmptyStateIsVisible = BehaviorSubject.create<Boolean>()
        internal val loggedOutEmptyStateIsVisible = BehaviorSubject.create<Boolean>()
        internal val startFixPledge = PublishSubject.create<String>()
        internal var startUpdateActivity: Observable<Activity> = Observable.empty()
        internal val surveys = BehaviorSubject.create<List<SurveyResponse>>()

        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            apiClient = requireNotNull(environment.apiClientV2())
            apolloClient = requireNotNull(environment.apolloClientV2())
            currentUser = requireNotNull(environment.currentUserV2())
            analyticEvents = requireNotNull(environment.analytics())

            initActivityFeedPipelines(disposables)
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        override fun emptyActivityFeedDiscoverProjectsClicked(viewHolder: EmptyActivityFeedViewHolder?) {
            discoverProjectsClick.onNext(Unit)
        }

        override fun emptyActivityFeedLoginClicked(viewHolder: EmptyActivityFeedViewHolder?) {
            loginClick.onNext(Unit)
        }

        override fun projectClicked(viewHolder: RewardShippedViewHolder?, activity: Activity?) {
            if (activity != null) {
                projectUpdateProjectClick.onNext(activity)
            }
        }

        override fun trackingNumberClicked(
            url: String
        ) {
            trackShipmentClicked.onNext(url)
        }

        override fun managePledgeClicked(projectSlug: String) {
            managePledgeClicked.onNext(projectSlug)
        }

        override fun friendBackingClicked(
            viewHolder: FriendBackingViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                friendBackingClick.onNext(activity)
            }
        }

        override fun nextPage() {
            nextPage.onNext(Unit)
        }

        override fun projectStateChangedClicked(
            viewHolder: ProjectStateChangedViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                projectStateChangedClick.onNext(activity)
            }
        }

        override fun projectStateChangedPositiveClicked(
            viewHolder: ProjectStateChangedPositiveViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                projectStateChangedPositiveClick.onNext(activity)
            }
        }

        override fun projectUpdateClicked(
            viewHolder: ProjectUpdateViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                projectUpdateClick.onNext(activity)
            }
        }

        override fun projectUpdateProjectClicked(
            viewHolder: ProjectUpdateViewHolder?,
            activity: Activity?
        ) {
            if (activity != null) {
                projectUpdateProjectClick.onNext(activity)
            }
        }

        override fun refresh() {
            refresh.onNext(Unit)
        }

        override fun resume() {
            resume.onNext(Unit)
        }

        override fun activityList(): Observable<List<Activity>> = activityList
        override fun erroredBackings(): Observable<List<ErroredBacking>> = erroredBackings
        override fun goToDiscovery(): Observable<Unit> = goToDiscovery
        override fun goToLogin(): Observable<Unit> = goToLogin
        override fun goToProject(): Observable<Project> = goToProject
        override fun goToSurvey(): Observable<SurveyResponse> = goToSurvey
        override fun isFetchingActivities(): Observable<Boolean> = isFetchingActivities
        override fun loggedInEmptyStateIsVisible(): Observable<Boolean> = loggedInEmptyStateIsVisible
        override fun loggedOutEmptyStateIsVisible(): Observable<Boolean> = loggedOutEmptyStateIsVisible
        override fun startFixPledge(): Observable<String> = startFixPledge
        override fun startUpdateActivity(): Observable<Activity> = startUpdateActivity
        override fun trackShipmentClicked(): Observable<String> = trackShipmentClicked
        override fun surveys(): Observable<List<SurveyResponse>> = surveys
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityFeedViewModel(environment) as T
        }
    }
}
