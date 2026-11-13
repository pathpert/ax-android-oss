package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.features.discovery.data.DiscoveryFragmentPipeline
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.models.Activity
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.adapters.DiscoveryActivitySampleAdapter
import com.kickstarter.ui.adapters.DiscoveryEditorialAdapter
import com.kickstarter.ui.adapters.DiscoveryOnboardingAdapter
import com.kickstarter.ui.adapters.DiscoveryProjectCardAdapter
import com.kickstarter.ui.adapters.DiscoveryVideoFeedBannerAdapter
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder
import com.kickstarter.ui.viewholders.DiscoveryVideoFeedBannerViewHolder
import io.reactivex.Observable

@Deprecated(
    message = "Legacy monolithic ViewModel replaced by DiscoveryFeatureViewModel",
    replaceWith = ReplaceWith(
        "DiscoveryFeatureViewModel",
        "com.kickstarter.features.discovery.viewmodel.DiscoveryFeatureViewModel"
    )
)
interface DiscoveryFragmentViewModel {
    interface Inputs :
        DiscoveryProjectCardAdapter.Delegate,
        DiscoveryOnboardingAdapter.Delegate,
        DiscoveryEditorialAdapter.Delegate,
        DiscoveryActivitySampleAdapter.Delegate,
        DiscoveryVideoFeedBannerAdapter.Delegate {

        fun fragmentLifeCycle(lifecycleEvent: Lifecycle.State)
        fun clearPage()
        fun heartContainerClicked()
        fun nextPage()
        fun paramsFromActivity(params: DiscoveryParams)
        fun refresh()
        fun rootCategories(rootCategories: List<Category>)
    }

    interface Outputs {
        fun activity(): Observable<Activity>
        fun clearActivities(): Observable<Unit>
        fun isFetchingProjects(): Observable<Boolean>
        fun projectList(): Observable<List<Pair<Project, DiscoveryParams>>>
        fun shouldShowEditorial(): Observable<Editorial>
        fun shouldShowEmptySavedView(): Observable<Boolean>
        fun shouldShowOnboardingView(): Observable<Boolean>
        fun shouldShowVideoFeedBanner(): Observable<Boolean>
        fun startVideoFeedActivity(): Observable<Unit>
        fun showActivityFeed(): Observable<Boolean>
        fun showLoginTout(): Observable<Boolean>
        fun startHeartAnimation(): Observable<Unit>
        fun startEditorialActivity(): Observable<Editorial>
        fun startProjectActivity(): Observable<Pair<Project, RefTag>>
        fun startPreLaunchProjectActivity(): Observable<Pair<Project, RefTag>>
        fun startUpdateActivity(): Observable<Activity>
        fun startLoginToutActivityToSaveProject(): Observable<Project>
        fun scrollToSavedProjectPosition(): Observable<Int>
        fun showSavedPrompt(): Observable<Unit>
        fun startSetPasswordActivity(): Observable<String>
    }

    class DiscoveryFragmentViewModel(environment: Environment) :
        ViewModel(),
        Inputs,
        Outputs {
        private val pipeline = DiscoveryFragmentPipeline(environment)

        @JvmField val inputs: Inputs = this
        @JvmField val outputs: Outputs = this

        override fun onCleared() {
            pipeline.clear()
            super.onCleared()
        }

        override fun activitySampleFriendBackingViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendBackingViewHolder?) = pipeline.activityClick.onNext(true)
        override fun activitySampleFriendFollowViewHolderSeeActivityClicked(viewHolder: ActivitySampleFriendFollowViewHolder?) = pipeline.activityClick.onNext(true)
        override fun activitySampleProjectViewHolderSeeActivityClicked(viewHolder: ActivitySampleProjectViewHolder?) = pipeline.activityClick.onNext(true)
        override fun editorialViewHolderClicked(editorial: Editorial) = pipeline.editorialClicked.onNext(editorial)
        override fun refresh() = pipeline.refresh.onNext(Unit)
        override fun rootCategories(rootCategories: List<Category>) = pipeline.rootCategories.onNext(rootCategories)
        override fun clearPage() = pipeline.clearPage.onNext(Unit)
        override fun heartContainerClicked() = pipeline.heartContainerClicked.onNext(Unit)
        override fun activitySampleFriendBackingViewHolderProjectClicked(viewHolder: ActivitySampleFriendBackingViewHolder, project: Project?) { project?.let { pipeline.activitySampleProjectClick.onNext(it) } }
        override fun activitySampleProjectViewHolderProjectClicked(viewHolder: ActivitySampleProjectViewHolder, project: Project?) { project?.let { pipeline.activitySampleProjectClick.onNext(it) } }
        override fun activitySampleProjectViewHolderUpdateClicked(viewHolder: ActivitySampleProjectViewHolder?, activity: Activity) = pipeline.activityUpdateClick.onNext(activity)
        override fun discoveryOnboardingViewHolderLoginToutClick(viewHolder: DiscoveryOnboardingViewHolder?) = pipeline.discoveryOnboardingLoginToutClick.onNext(true)
        override fun discoveryVideoFeedBannerViewHolderClick(viewHolder: DiscoveryVideoFeedBannerViewHolder?) = pipeline.videoFeedBannerClicked.onNext(Unit)
        override fun projectCardViewHolderClicked(project: Project) = pipeline.projectCardClicked.onNext(project)
        override fun nextPage() = pipeline.nextPage.onNext(Unit)
        override fun paramsFromActivity(params: DiscoveryParams) = pipeline.paramsFromActivity.onNext(params)
        override fun fragmentLifeCycle(lifecycleEvent: Lifecycle.State) = pipeline.lifecycleObservable.onNext(lifecycleEvent)

        override fun activity(): Observable<Activity> = pipeline.activity
        override fun clearActivities(): Observable<Unit> = pipeline.clearActivities
        override fun isFetchingProjects(): Observable<Boolean> = pipeline.isFetchingProjects
        override fun projectList(): Observable<List<Pair<Project, DiscoveryParams>>> = pipeline.projectList
        override fun showActivityFeed(): Observable<Boolean> = pipeline.showActivityFeed
        override fun showLoginTout(): Observable<Boolean> = pipeline.showLoginTout
        override fun shouldShowEditorial(): Observable<Editorial> = pipeline.shouldShowEditorial
        override fun shouldShowEmptySavedView(): Observable<Boolean> = pipeline.shouldShowEmptySavedView
        override fun startHeartAnimation(): Observable<Unit> = pipeline.startHeartAnimation
        override fun startEditorialActivity(): Observable<Editorial> = pipeline.startEditorialActivity
        override fun startProjectActivity(): Observable<Pair<Project, RefTag>> = pipeline.startProjectActivity
        override fun startPreLaunchProjectActivity(): Observable<Pair<Project, RefTag>> = pipeline.startPreLaunchProjectActivity
        override fun shouldShowOnboardingView(): Observable<Boolean> = pipeline.shouldShowOnboardingView
        override fun shouldShowVideoFeedBanner(): Observable<Boolean> = pipeline.shouldShowVideoFeedBanner
        override fun startVideoFeedActivity(): Observable<Unit> = pipeline.startVideoFeedActivity
        override fun startUpdateActivity(): Observable<Activity> = pipeline.startUpdateActivity
        override fun onHeartButtonClicked(project: Project) = pipeline.onHeartButtonClicked.onNext(project)
        override fun startLoginToutActivityToSaveProject(): Observable<Project> = pipeline.startLoginToutActivityToSaveProject
        override fun scrollToSavedProjectPosition(): Observable<Int> = pipeline.scrollToSavedProjectPosition
        override fun showSavedPrompt(): Observable<Unit> = pipeline.showSavedPrompt
        override fun startSetPasswordActivity(): Observable<String> = pipeline.startSetPasswordActivity
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiscoveryFragmentViewModel(environment) as T
        }
    }
}
