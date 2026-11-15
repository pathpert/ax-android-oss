package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

@Deprecated(
    message = "Legacy monolithic path replaced by DiscoveryFeatureViewModel under com.kickstarter.features.discovery.viewmodel.",
    replaceWith = ReplaceWith("DiscoveryFeatureViewModel", "com.kickstarter.features.discovery.viewmodel.DiscoveryFeatureViewModel")
)
interface ProjectCardHolderViewModel {
    interface Inputs {
        /** Call to configure view model with a project and current discovery params.  */
        fun configureWith(projectAndDiscoveryParams: Pair<Project, DiscoveryParams>)

        /** Call when the project card has been clicked.  */
        fun projectCardClicked()

        /** Call when the heart button is clicked.  */
        fun heartButtonClicked()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        fun backersCountTextViewText(): Observable<String>
        fun backingViewGroupIsGone(): Observable<Boolean>
        fun deadlineCountdownText(): Observable<String>
        fun featuredViewGroupIsGone(): Observable<Boolean>
        fun friendsForNamepile(): Observable<List<User>>
        fun friendAvatar2IsGone(): Observable<Boolean>
        fun friendAvatar3IsGone(): Observable<Boolean>
        fun friendAvatarUrl1(): Observable<String>
        fun friendAvatarUrl2(): Observable<String>
        fun friendAvatarUrl3(): Observable<String>
        fun imageIsInvisible(): Observable<Boolean>
        fun friendBackingViewIsHidden(): Observable<Boolean>
        fun fundingSuccessfulViewGroupIsGone(): Observable<Boolean>
        fun fundingUnsuccessfulViewGroupIsGone(): Observable<Boolean>
        fun locationContainerIsGone(): Observable<Boolean>
        fun locationName(): Observable<String>
        fun metadataViewGroupIsGone(): Observable<Boolean>
        fun metadataViewGroupBackgroundDrawable(): Observable<Int>
        fun projectForDeadlineCountdownDetail(): Observable<Project>
        fun percentageFundedForProgressBar(): Observable<Int>
        fun percentageFundedProgressBarIsGone(): Observable<Boolean>
        fun percentageFundedTextViewText(): Observable<String>
        fun photoUrl(): Observable<String>
        fun nameAndBlurbText(): Observable<Pair<String, String>>
        fun notifyDelegateOfProjectClick(): Observable<Project>
        fun projectCanceledAt(): Observable<DateTime>
        fun projectCardStatsViewGroupIsGone(): Observable<Boolean>
        fun projectFailedAt(): Observable<DateTime>
        fun projectStateViewGroupIsGone(): Observable<Boolean>
        fun projectSubcategoryIsGone(): Observable<Boolean>
        fun projectSubcategoryName(): Observable<String>
        fun projectSuccessfulAt(): Observable<DateTime>
        fun projectSuspendedAt(): Observable<DateTime>
        fun projectTagContainerIsGone(): Observable<Boolean>
        fun projectWeLoveIsGone(): Observable<Boolean>
        fun rootCategoryNameForFeatured(): Observable<String>
        fun savedViewGroupIsGone(): Observable<Boolean>
        fun comingSoonViewGroupIsGone(): Observable<Boolean>
        fun setDefaultTopPadding(): Observable<Boolean>
        fun heartDrawableId(): Observable<Int>
        fun notifyDelegateOfHeartButtonClicked(): Observable<Project>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class ViewModel : Inputs, Outputs {
        internal val heartButtonClicked = PublishSubject.create<Unit>()
        internal val discoveryParams = PublishSubject.create<DiscoveryParams?>()
        internal val project = PublishSubject.create<Project>()
        internal val projectCardClicked = PublishSubject.create<Unit>()
        internal lateinit var backersCountTextViewText: Observable<String>
        internal lateinit var backingViewGroupIsGone: Observable<Boolean>
        internal lateinit var deadlineCountdownText: Observable<String>
        internal lateinit var featuredViewGroupIsGone: Observable<Boolean>
        internal lateinit var friendAvatar2IsGone: Observable<Boolean>
        internal lateinit var friendAvatar3IsGone: Observable<Boolean>
        internal lateinit var friendAvatarUrl1: Observable<String>
        internal lateinit var friendAvatarUrl2: Observable<String>
        internal lateinit var friendAvatarUrl3: Observable<String>
        internal lateinit var friendBackingViewIsHidden: Observable<Boolean>
        internal lateinit var friendsForNamepile: Observable<List<User>>
        internal lateinit var fundingSuccessfulViewGroupIsGone: Observable<Boolean>
        internal lateinit var fundingUnsuccessfulViewGroupIsGone: Observable<Boolean>
        internal lateinit var imageIsInvisible: Observable<Boolean>
        internal val locationName = BehaviorSubject.create<String>()
        internal val locationContainerIsGone = BehaviorSubject.create<Boolean>()
        internal lateinit var metadataViewGroupBackground: Observable<Int>
        internal lateinit var metadataViewGroupIsGone: Observable<Boolean>
        internal lateinit var nameAndBlurbText: Observable<Pair<String, String>>
        internal lateinit var notifyDelegateOfProjectClick: Observable<Project>
        internal lateinit var percentageFundedForProgressBar: Observable<Int>
        internal lateinit var percentageFundedProgressBarIsGone: Observable<Boolean>
        internal lateinit var percentageFundedTextViewText: Observable<String>
        internal lateinit var photoUrl: Observable<String>
        internal lateinit var projectForDeadlineCountdownDetail: Observable<Project>
        internal lateinit var projectCardStatsViewGroupIsGone: Observable<Boolean>
        internal lateinit var projectStateViewGroupIsGone: Observable<Boolean>
        internal lateinit var projectCanceledAt: Observable<DateTime>
        internal lateinit var projectFailedAt: Observable<DateTime>
        internal lateinit var projectSubcategoryName: Observable<String>
        internal lateinit var projectSubcategoryIsGone: Observable<Boolean>
        internal lateinit var projectSuccessfulAt: Observable<DateTime>
        internal lateinit var projectSuspendedAt: Observable<DateTime>
        internal lateinit var projectTagContainerIsGone: Observable<Boolean>
        internal lateinit var projectWeLoveIsGone: Observable<Boolean>
        internal lateinit var rootCategoryNameForFeatured: Observable<String>
        internal lateinit var savedViewGroupIsGone: Observable<Boolean>
        internal lateinit var comingSoonViewGroupIsGone: Observable<Boolean>
        internal lateinit var setDefaultTopPadding: Observable<Boolean>
        internal val heartDrawableId = BehaviorSubject.create<Int>()
        internal val notifyDelegateOfHeartButtonClicked = BehaviorSubject.create<Project>()

        internal val disposables = CompositeDisposable()

        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        override fun configureWith(projectAndDiscoveryParams: Pair<Project, DiscoveryParams>) {
            project.onNext(projectAndDiscoveryParams.first)
            discoveryParams.onNext(projectAndDiscoveryParams.second)
        }

        override fun heartButtonClicked() {
            this.heartButtonClicked.onNext(Unit)
        }

        override fun projectCardClicked() {
            projectCardClicked.onNext(Unit)
        }

        override fun heartDrawableId(): Observable<Int> = this.heartDrawableId
        override fun backersCountTextViewText(): Observable<String> = backersCountTextViewText
        override fun backingViewGroupIsGone(): Observable<Boolean> = backingViewGroupIsGone
        override fun deadlineCountdownText(): Observable<String> = deadlineCountdownText
        override fun featuredViewGroupIsGone(): Observable<Boolean> = featuredViewGroupIsGone
        override fun friendAvatar2IsGone(): Observable<Boolean> = friendAvatar2IsGone
        override fun friendAvatar3IsGone(): Observable<Boolean> = friendAvatar3IsGone
        override fun friendAvatarUrl1(): Observable<String> = friendAvatarUrl1
        override fun friendAvatarUrl2(): Observable<String> = friendAvatarUrl2
        override fun friendAvatarUrl3(): Observable<String> = friendAvatarUrl3
        override fun friendBackingViewIsHidden(): Observable<Boolean> = friendBackingViewIsHidden
        override fun friendsForNamepile(): Observable<List<User>> = friendsForNamepile
        override fun fundingSuccessfulViewGroupIsGone(): Observable<Boolean> = fundingSuccessfulViewGroupIsGone
        override fun fundingUnsuccessfulViewGroupIsGone(): Observable<Boolean> = fundingUnsuccessfulViewGroupIsGone
        override fun imageIsInvisible(): Observable<Boolean> = imageIsInvisible
        override fun locationContainerIsGone(): Observable<Boolean> = locationContainerIsGone
        override fun locationName(): Observable<String> = locationName
        override fun metadataViewGroupBackgroundDrawable(): Observable<Int> = metadataViewGroupBackground
        override fun metadataViewGroupIsGone(): Observable<Boolean> = metadataViewGroupIsGone
        override fun nameAndBlurbText(): Observable<Pair<String, String>> = nameAndBlurbText
        override fun notifyDelegateOfProjectClick(): Observable<Project> = notifyDelegateOfProjectClick
        override fun percentageFundedForProgressBar(): Observable<Int> = percentageFundedForProgressBar
        override fun percentageFundedProgressBarIsGone(): Observable<Boolean> = percentageFundedProgressBarIsGone
        override fun percentageFundedTextViewText(): Observable<String> = percentageFundedTextViewText
        override fun photoUrl(): Observable<String> = photoUrl
        override fun projectCardStatsViewGroupIsGone(): Observable<Boolean> = projectCardStatsViewGroupIsGone
        override fun projectForDeadlineCountdownDetail(): Observable<Project> = projectForDeadlineCountdownDetail
        override fun projectStateViewGroupIsGone(): Observable<Boolean> = projectStateViewGroupIsGone
        override fun projectSubcategoryIsGone(): Observable<Boolean> = projectSubcategoryIsGone
        override fun projectSubcategoryName(): Observable<String> = projectSubcategoryName
        override fun projectCanceledAt(): Observable<DateTime> = projectCanceledAt
        override fun projectFailedAt(): Observable<DateTime> = projectFailedAt
        override fun projectSuccessfulAt(): Observable<DateTime> = projectSuccessfulAt
        override fun projectSuspendedAt(): Observable<DateTime> = projectSuspendedAt
        override fun projectTagContainerIsGone(): Observable<Boolean> = projectTagContainerIsGone
        override fun projectWeLoveIsGone(): Observable<Boolean> = projectWeLoveIsGone
        override fun rootCategoryNameForFeatured(): Observable<String> = rootCategoryNameForFeatured
        override fun setDefaultTopPadding(): Observable<Boolean> = setDefaultTopPadding
        override fun savedViewGroupIsGone(): Observable<Boolean> = savedViewGroupIsGone
        override fun comingSoonViewGroupIsGone(): Observable<Boolean> = comingSoonViewGroupIsGone
        override fun notifyDelegateOfHeartButtonClicked(): Observable<Project> = this.notifyDelegateOfHeartButtonClicked

        init {
            initPipelines()
        }

        fun onCleared() {
            disposables.clear()
        }
    }
}
