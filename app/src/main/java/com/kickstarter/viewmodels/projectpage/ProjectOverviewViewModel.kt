package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

@Deprecated(
    message = "Legacy monolithic path replaced by ProjectFeatureViewModel under com.kickstarter.features.projectpage.viewmodel.",
    replaceWith = ReplaceWith("ProjectFeatureViewModel", "com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel")
)
interface ProjectOverviewViewModel {
    interface Inputs {
        /** Call to configure view holder with ProjectData. */
        fun configureWith(projectData: ProjectData)
        /** Call when the project social view group is clicked. */
        fun projectSocialViewGroupClicked()
        /** Call when the creator button is clicked */
        fun creatorInfoButtonClicked()
        /** Call when the campaign clicked */
        fun campaignButtonClicked()
        /** Call when the comments clicked */
        fun commentsButtonClicked()
        /** Call when the updates clicked */
        fun updatesButtonClicked()
        /** Called when the report project button */
        fun reportProjectButtonClicked()
        /** Called when the report project flow is completed with flaggingKind */
        fun refreshFlaggedState(flaggingKind: String)
        /** Called when the user press some of the links on the flagged project text */
        fun linkClicked(urlTag: String)
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        fun avatarPhotoUrl(): Observable<String>
        fun backersCountTextViewText(): Observable<String>
        fun blurbTextViewText(): Observable<String>
        fun categoryTextViewText(): Observable<String>
        fun commentsCountTextViewText(): Observable<String>
        fun conversionPledgedAndGoalText(): Observable<Pair<String, String>>
        fun conversionTextViewIsGone(): Observable<Boolean>
        fun creatorDetailsLoadingContainerIsVisible(): Observable<Boolean>
        fun creatorDetailsIsGone(): Observable<Boolean>
        fun creatorNameTextViewText(): Observable<String>
        fun deadlineCountdownTextViewText(): Observable<String>
        fun goalStringForTextView(): Observable<String>
        fun locationTextViewText(): Observable<String>
        fun percentageFundedProgress(): Observable<Int>
        fun percentageFundedProgressBarIsGone(): Observable<Boolean>
        fun pledgedTextViewText(): Observable<String>
        fun projectDisclaimerGoalReachedDateTime(): Observable<DateTime>
        fun projectDisclaimerGoalNotReachedString(): Observable<Pair<String, DateTime>>
        fun projectDisclaimerTextViewIsGone(): Observable<Boolean>
        fun projectLaunchDate(): Observable<String>
        fun projectLaunchDateIsGone(): Observable<Boolean>
        fun projectNameTextViewText(): Observable<String>
        fun projectOutput(): Observable<Project>
        fun projectSocialImageViewIsGone(): Observable<Boolean>
        fun projectSocialImageViewUrl(): Observable<String>
        fun projectSocialTextViewFriends(): Observable<List<User>>
        fun projectSocialViewGroupIsGone(): Observable<Boolean>
        fun projectStateViewGroupBackgroundColorInt(): Observable<Int>
        fun projectStateViewGroupIsGone(): Observable<Boolean>
        fun shouldSetDefaultStatsMargins(): Observable<Boolean>
        fun setCanceledProjectStateView(): Observable<Unit>
        fun setProjectSocialClickListener(): Observable<Unit>
        fun setSuccessfulProjectStateView(): Observable<DateTime>
        fun setSuspendedProjectStateView(): Observable<Unit>
        fun setUnsuccessfulProjectStateView(): Observable<DateTime>
        fun startProjectSocialActivity(): Observable<Project>
        fun updatesCountTextViewText(): Observable<String>
        fun startCreatorView(): Observable<ProjectData>
        fun startCommentsView(): Observable<ProjectData>
        fun startUpdatesView(): Observable<ProjectData>
        fun startReportProjectView(): Observable<ProjectData>
        fun startLoginView(): Observable<Unit>
        fun shouldShowReportProject(): Observable<Boolean>
        fun shouldShowProjectFlagged(): Observable<Boolean>
        fun openExternallyWithUrl(): Observable<String>
        fun projectProjectNotice(): Observable<String?>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class ProjectOverviewViewModel(
        internal val environment: Environment
    ) : ViewModel(), Inputs, Outputs {

        internal val apolloClient: ApolloClientTypeV2 = requireNotNull(environment.apolloClientV2())
        internal val currentUser = requireNotNull(environment.currentUserV2())
        internal val ksCurrency: KSCurrency = requireNotNull(environment.ksCurrency())
        internal val analyticEvents = requireNotNull(environment.analytics())
        val ksString = requireNotNull(environment.ksString())

        // Inputs
        internal val projectData = PublishSubject.create<ProjectData>()
        internal val projectSocialViewGroupClicked = PublishSubject.create<Unit>()
        internal val creatorInfoClicked = PublishSubject.create<Unit>()
        internal val campaignClicked = PublishSubject.create<Unit>()
        internal val commentsClicked = PublishSubject.create<Unit>()
        internal val updatesClicked = PublishSubject.create<Unit>()
        internal val reportProjectButtonClicked = PublishSubject.create<Unit>()
        internal val refreshFlagged = PublishSubject.create<String>()
        internal val linkTagClicked = PublishSubject.create<String>()

        // Outputs
        internal var avatarPhotoUrl: Observable<String> = Observable.empty()
        internal var backersCountTextViewText: Observable<String> = Observable.empty()
        internal var blurbTextViewText: Observable<String> = Observable.empty()
        internal var categoryTextViewText: Observable<String> = Observable.empty()
        internal var commentsCountTextViewText: Observable<String> = Observable.empty()
        internal var conversionPledgedAndGoalText: Observable<Pair<String, String>> = Observable.empty()
        internal var conversionTextViewIsGone: Observable<Boolean> = Observable.empty()
        internal val creatorDetailsLoadingContainerIsVisible = BehaviorSubject.create<Boolean>()
        internal val creatorDetailsIsGone = BehaviorSubject.create<Boolean>()
        internal var creatorNameTextViewText: Observable<String> = Observable.empty()
        internal var deadlineCountdownTextViewText: Observable<String> = Observable.empty()
        internal var goalStringForTextView: Observable<String> = Observable.empty()
        internal var locationTextViewText: Observable<String> = Observable.empty()
        internal var percentageFundedProgress: Observable<Int> = Observable.empty()
        internal var percentageFundedProgressBarIsGone: Observable<Boolean> = Observable.empty()
        internal var pledgedTextViewText: Observable<String> = Observable.empty()
        internal var projectDisclaimerGoalReachedDateTime: Observable<DateTime> = Observable.empty()
        internal var projectDisclaimerGoalNotReachedString: Observable<Pair<String, DateTime>> = Observable.empty()
        internal var projectDisclaimerTextViewIsGone: Observable<Boolean> = Observable.empty()
        internal var projectLaunchDate: Observable<String> = Observable.empty()
        internal var projectLaunchDateIsGone: Observable<Boolean> = Observable.empty()
        internal var projectNameTextViewText: Observable<String> = Observable.empty()
        internal var projectOutput: Observable<Project> = Observable.empty()
        internal var projectProjectNotice: Observable<String?> = Observable.empty()
        internal var projectSocialImageViewIsGone: Observable<Boolean> = Observable.empty()
        internal var projectSocialImageViewUrl: Observable<String> = Observable.empty()
        internal var projectSocialTextViewFriends: Observable<List<User>> = Observable.empty()
        internal var projectSocialViewGroupIsGone: Observable<Boolean> = Observable.empty()
        internal var projectStateViewGroupBackgroundColorInt: Observable<Int> = Observable.empty()
        internal var projectStateViewGroupIsGone: Observable<Boolean> = Observable.empty()
        internal var setCanceledProjectStateView: Observable<Unit> = Observable.empty()
        internal var setProjectSocialClickListener: Observable<Unit> = Observable.empty()
        internal var setSuccessfulProjectStateView: Observable<DateTime> = Observable.empty()
        internal var setSuccessfulProjectStillCollectingView: Observable<DateTime> = Observable.empty()
        internal var setSuspendedProjectStateView: Observable<Unit> = Observable.empty()
        internal var setUnsuccessfulProjectStateView: Observable<DateTime> = Observable.empty()
        internal var startProjectSocialActivity: Observable<Project> = Observable.empty()
        internal var shouldSetDefaultStatsMargins: Observable<Boolean> = Observable.empty()
        internal var updatesCountTextViewText: Observable<String> = Observable.empty()
        internal var startCreatorView: Observable<ProjectData> = Observable.empty()
        internal var startCommentsView: Observable<ProjectData> = Observable.empty()
        internal var startUpdatesView: Observable<ProjectData> = Observable.empty()
        internal var startReportProjectView: Observable<ProjectData> = Observable.empty()
        internal val startLogin = PublishSubject.create<Unit>()
        internal var shouldShowReportProject: Observable<Boolean> = Observable.empty()
        internal var shouldShowProjectFlagged: Observable<Boolean> = Observable.empty()
        internal val openExternally = PublishSubject.create<String>()

        internal val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        override fun configureWith(projectData: ProjectData) = this.projectData.onNext(projectData)
        override fun projectSocialViewGroupClicked() = projectSocialViewGroupClicked.onNext(Unit)
        override fun creatorInfoButtonClicked() = this.creatorInfoClicked.onNext(Unit)
        override fun campaignButtonClicked() = this.campaignClicked.onNext(Unit)
        override fun commentsButtonClicked() = this.commentsClicked.onNext(Unit)
        override fun updatesButtonClicked() = this.updatesClicked.onNext(Unit)
        override fun reportProjectButtonClicked() = this.reportProjectButtonClicked.onNext(Unit)
        override fun refreshFlaggedState(flaggingKind: String) = this.refreshFlagged.onNext(flaggingKind)
        override fun linkClicked(urlTag: String) = this.linkTagClicked.onNext(urlTag)

        override fun avatarPhotoUrl(): Observable<String> = avatarPhotoUrl
        override fun backersCountTextViewText(): Observable<String> = backersCountTextViewText
        override fun blurbTextViewText(): Observable<String> = blurbTextViewText
        override fun categoryTextViewText(): Observable<String> = categoryTextViewText
        override fun commentsCountTextViewText(): Observable<String> = commentsCountTextViewText
        override fun conversionTextViewIsGone(): Observable<Boolean> = conversionTextViewIsGone
        override fun conversionPledgedAndGoalText(): Observable<Pair<String, String>> = conversionPledgedAndGoalText
        override fun creatorDetailsLoadingContainerIsVisible(): Observable<Boolean> = creatorDetailsLoadingContainerIsVisible
        override fun creatorDetailsIsGone(): Observable<Boolean> = creatorDetailsIsGone
        override fun creatorNameTextViewText(): Observable<String> = creatorNameTextViewText
        override fun deadlineCountdownTextViewText(): Observable<String> = deadlineCountdownTextViewText
        override fun goalStringForTextView(): Observable<String> = goalStringForTextView
        override fun locationTextViewText(): Observable<String> = locationTextViewText
        override fun percentageFundedProgress(): Observable<Int> = percentageFundedProgress
        override fun percentageFundedProgressBarIsGone(): Observable<Boolean> = percentageFundedProgressBarIsGone
        override fun pledgedTextViewText(): Observable<String> = pledgedTextViewText
        override fun projectDisclaimerGoalReachedDateTime(): Observable<DateTime> = projectDisclaimerGoalReachedDateTime
        override fun projectDisclaimerGoalNotReachedString(): Observable<Pair<String, DateTime>> = projectDisclaimerGoalNotReachedString
        override fun projectDisclaimerTextViewIsGone(): Observable<Boolean> = projectDisclaimerTextViewIsGone
        override fun projectLaunchDate(): Observable<String> = projectLaunchDate
        override fun projectLaunchDateIsGone(): Observable<Boolean> = projectLaunchDateIsGone
        override fun projectNameTextViewText(): Observable<String> = projectNameTextViewText
        override fun projectOutput(): Observable<Project> = projectOutput
        override fun projectProjectNotice(): Observable<String?> = projectProjectNotice
        override fun projectSocialImageViewIsGone(): Observable<Boolean> = projectSocialImageViewIsGone
        override fun projectSocialImageViewUrl(): Observable<String> = projectSocialImageViewUrl
        override fun projectSocialTextViewFriends(): Observable<List<User>> = projectSocialTextViewFriends
        override fun projectSocialViewGroupIsGone(): Observable<Boolean> = projectSocialViewGroupIsGone
        override fun projectStateViewGroupBackgroundColorInt(): Observable<Int> = projectStateViewGroupBackgroundColorInt
        override fun projectStateViewGroupIsGone(): Observable<Boolean> = projectStateViewGroupIsGone
        override fun startProjectSocialActivity(): Observable<Project> = startProjectSocialActivity
        override fun setCanceledProjectStateView(): Observable<Unit> = setCanceledProjectStateView
        override fun setProjectSocialClickListener(): Observable<Unit> = setProjectSocialClickListener
        override fun setSuccessfulProjectStateView(): Observable<DateTime> = setSuccessfulProjectStateView
        fun setSuccessfulProjectStillCollectingView(): Observable<DateTime> = setSuccessfulProjectStillCollectingView
        override fun setSuspendedProjectStateView(): Observable<Unit> = setSuspendedProjectStateView
        override fun setUnsuccessfulProjectStateView(): Observable<DateTime> = setUnsuccessfulProjectStateView
        override fun shouldSetDefaultStatsMargins(): Observable<Boolean> = shouldSetDefaultStatsMargins
        override fun updatesCountTextViewText(): Observable<String> = updatesCountTextViewText
        override fun startCreatorView(): Observable<ProjectData> = this.startCreatorView
        override fun startCommentsView(): Observable<ProjectData> = this.startCommentsView
        override fun startUpdatesView(): Observable<ProjectData> = this.startUpdatesView
        override fun startReportProjectView(): Observable<ProjectData> = this.startReportProjectView
        override fun startLoginView(): Observable<Unit> = this.startLogin
        override fun shouldShowReportProject(): Observable<Boolean> = this.shouldShowReportProject
        override fun shouldShowProjectFlagged(): Observable<Boolean> = this.shouldShowProjectFlagged
        override fun openExternallyWithUrl(): Observable<String> = this.openExternally

        init {
            initPipelines()
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        @Suppress("UNCHECKED_CAST")
        @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
        class Factory(private val environment: Environment) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProjectOverviewViewModel(environment) as T
            }
        }
    }
}
