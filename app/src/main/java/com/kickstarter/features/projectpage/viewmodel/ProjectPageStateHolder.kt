package com.kickstarter.features.projectpage.viewmodel

import android.content.Intent
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.kickstarter.features.projectpage.data.PagerTabConfig
import com.kickstarter.features.projectpage.data.ProjectPageInputs
import com.kickstarter.features.projectpage.data.ProjectPageOutputs
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.ManagePledgeMenuOptions
import com.kickstarter.ui.data.MediaElement
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * State holder managing reactive subjects and dispatching inputs/outputs
 * for the Project Page feature.
 */
abstract class ProjectPageStateHolder(val environment: Environment) :
    ViewModel(),
    ProjectPageInputs,
    ProjectPageOutputs {

    protected val statsigClient = environment.statsigClient()

    // Inputs Subjects
    internal val intent = PublishSubject.create<Intent>()
    internal val activityResult = BehaviorSubject.create<ActivityResult>()
    internal val closeFullScreenVideo = BehaviorSubject.create<Long>()
    internal val cancelPledgeClicked = PublishSubject.create<Unit>()
    internal val commentsTextViewClicked = PublishSubject.create<Unit>()
    internal val contactCreatorClicked = PublishSubject.create<Unit>()
    internal val fixPaymentMethodButtonClicked = PublishSubject.create<Unit>()
    internal val fragmentStackCount = PublishSubject.create<Int>()
    internal val heartButtonClicked = PublishSubject.create<Unit>()
    internal val nativeProjectActionButtonClicked = PublishSubject.create<Unit>()
    internal val onGlobalLayout = PublishSubject.create<Unit>()
    internal val fullScreenVideoButtonClicked = PublishSubject.create<kotlin.Pair<String, Long>>()
    internal val pledgePaymentSuccessfullyUpdated = PublishSubject.create<Unit>()
    internal val pledgeSuccessfullyCancelled = PublishSubject.create<Unit>()
    internal val pledgeSuccessfullyCreated = PublishSubject.create<Pair<CheckoutData, PledgeData>>()
    internal val pledgeSuccessfullyUpdated = PublishSubject.create<Unit>()
    internal val pledgeToolbarNavigationClicked = PublishSubject.create<Unit>()
    internal val refreshProject = PublishSubject.create<Unit>()
    internal val reloadProjectContainerClicked = PublishSubject.create<Unit>()
    internal val shareButtonClicked = PublishSubject.create<Unit>()
    internal val updatePaymentClicked = PublishSubject.create<Unit>()
    internal val updatesTextViewClicked = PublishSubject.create<Unit>()
    internal val viewRewardsClicked = PublishSubject.create<Unit>()
    internal val onVideoPlayButtonClicked = PublishSubject.create<Unit>()
    internal val tabSelected = PublishSubject.create<Int>()
    internal val continuePledgeFlow = PublishSubject.create<() -> Unit>()

    // Outputs Subjects
    internal val backingDetailsIsVisible = BehaviorSubject.create<Boolean>()
    internal val backingDetailsSubtitle = BehaviorSubject.create<Either<String, Int>?>()
    internal val backingDetailsTitle = BehaviorSubject.create<Int>()
    internal val expandPledgeSheet = BehaviorSubject.create<Pair<Boolean, Boolean>>()
    internal val goBack = PublishSubject.create<Unit>()
    internal val heartDrawableId = BehaviorSubject.create<Int>()
    internal val pledgeActionButtonColor = BehaviorSubject.create<Int>()
    internal val pledgeActionButtonContainerIsGone = BehaviorSubject.create<Boolean>()
    internal val pledgeActionButtonText = BehaviorSubject.create<Int>()
    internal val pledgeToolbarNavigationIcon = BehaviorSubject.create<Int>()
    internal val pledgeToolbarTitle = BehaviorSubject.create<Int>()
    internal val prelaunchUrl = BehaviorSubject.create<String>()
    internal val projectData = BehaviorSubject.create<ProjectData>()
    internal val retryProgressBarIsGone = BehaviorSubject.create<Boolean>()
    internal val reloadProjectContainerIsGone = BehaviorSubject.create<Boolean>()
    internal val revealRewardsFragment = PublishSubject.create<Unit>()
    internal val scrimIsVisible = BehaviorSubject.create<Boolean>()
    internal val setInitialRewardPosition = BehaviorSubject.create<Unit>()
    internal val showCancelPledgeFragment = PublishSubject.create<Project>()
    internal val showCancelPledgeSuccess = PublishSubject.create<Unit>()
    internal val showPledgeNotCancelableDialog = PublishSubject.create<Unit>()
    internal val showShareSheet = PublishSubject.create<Pair<String, String>>()
    internal val showSocialShareSheet = PublishSubject.create<Project>()
    internal val showSavedPrompt = PublishSubject.create<Unit>()
    internal val updatePledgeData = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
    internal val showUpdatePledge = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
    internal val showUpdatePledgeSuccess = PublishSubject.create<Unit>()
    internal val startRootCommentsActivity = PublishSubject.create<ProjectData>()
    internal val startRootCommentsForCommentsThreadActivity = PublishSubject.create<Pair<String, ProjectData>>()
    internal val startLoginToutActivity = PublishSubject.create<Unit>()
    internal val startMessagesActivity = PublishSubject.create<Project>()
    internal val startProjectUpdateActivity = PublishSubject.create<Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>>()
    internal val startProjectUpdateToRepliesDeepLinkActivity = PublishSubject.create<Pair<Pair<String, String>, Pair<Project, ProjectData>>>()
    internal val startThanksActivity = PublishSubject.create<Pair<CheckoutData, PledgeData>>()
    internal val openBackingDetailsWebview = PublishSubject.create<String>()
    internal val openPledgeManagerWebview = PublishSubject.create<String>()
    internal val updateFragments = BehaviorSubject.create<ProjectData>()
    internal val hideVideoPlayer = BehaviorSubject.create<Boolean>()
    internal val projectMedia = PublishSubject.create<MediaElement>()
    internal val playButtonIsVisible = PublishSubject.create<Boolean>()
    internal val backingViewGroupIsVisible = PublishSubject.create<Boolean>()
    internal val updateTabs = PublishSubject.create<List<PagerTabConfig>>()
    internal val onOpenVideoInFullScreen = PublishSubject.create<kotlin.Pair<String, Long>>()
    internal val updateVideoCloseSeekPosition = BehaviorSubject.create<Long>()
    internal val showLatePledgeFlow = BehaviorSubject.create<Boolean>()
    internal val showPledgeRedemptionScreen = BehaviorSubject.create<Pair<Project, User>>()
    internal val managePledgeMenuOptions = BehaviorSubject.create<ManagePledgeMenuOptions>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val onThirdPartyEventSent: BehaviorSubject<Boolean?> = BehaviorSubject.create()

    val disposables = CompositeDisposable()

    // Inputs implementation
    override fun configureWith(intent: Intent) = this.intent.onNext(intent)
    override fun cancelPledgeClicked() = this.cancelPledgeClicked.onNext(Unit)
    override fun commentsTextViewClicked() = this.commentsTextViewClicked.onNext(Unit)
    override fun contactCreatorClicked() = this.contactCreatorClicked.onNext(Unit)
    override fun fixPaymentMethodButtonClicked() = this.fixPaymentMethodButtonClicked.onNext(Unit)
    override fun fragmentStackCount(count: Int) = this.fragmentStackCount.onNext(count)
    override fun heartButtonClicked() = this.heartButtonClicked.onNext(Unit)
    override fun nativeProjectActionButtonClicked() = this.nativeProjectActionButtonClicked.onNext(Unit)
    override fun onGlobalLayout() = this.onGlobalLayout.onNext(Unit)
    override fun fullScreenVideoButtonClicked(videoInfo: kotlin.Pair<String, Long>) = fullScreenVideoButtonClicked.onNext(videoInfo)
    override fun pledgePaymentSuccessfullyUpdated() = this.pledgePaymentSuccessfullyUpdated.onNext(Unit)
    override fun pledgeSuccessfullyCancelled() = this.pledgeSuccessfullyCancelled.onNext(Unit)
    override fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>) =
        this.pledgeSuccessfullyCreated.onNext(checkoutDataAndPledgeData)
    override fun pledgeSuccessfullyUpdated() = this.pledgeSuccessfullyUpdated.onNext(Unit)
    override fun pledgeToolbarNavigationClicked() = this.pledgeToolbarNavigationClicked.onNext(Unit)
    override fun refreshProject() = this.refreshProject.onNext(Unit)
    override fun reloadProjectContainerClicked() = this.reloadProjectContainerClicked.onNext(Unit)
    override fun shareButtonClicked() = this.shareButtonClicked.onNext(Unit)
    override fun updatePaymentClicked() = this.updatePaymentClicked.onNext(Unit)
    override fun updatesTextViewClicked() = this.updatesTextViewClicked.onNext(Unit)
    override fun viewRewardsClicked() = this.viewRewardsClicked.onNext(Unit)
    override fun tabSelected(position: Int) = this.tabSelected.onNext(position)
    override fun closeFullScreenVideo(seekPosition: Long) = closeFullScreenVideo.onNext(seekPosition)
    override fun onVideoPlayButtonClicked() = onVideoPlayButtonClicked.onNext(Unit)
    override fun activityResult(result: ActivityResult) = this.activityResult.onNext(result)
    override fun continuePledgeFlow(callback: () -> Unit) = this.continuePledgeFlow.onNext(callback)

    // Outputs implementation
    override fun backingDetailsIsVisible(): Observable<Boolean> = this.backingDetailsIsVisible
    override fun backingDetailsSubtitle(): Observable<Either<String, Int>?> = this.backingDetailsSubtitle
    override fun backingDetailsTitle(): Observable<Int> = this.backingDetailsTitle
    override fun expandPledgeSheet(): Observable<Pair<Boolean, Boolean>> = this.expandPledgeSheet
    override fun goBack(): Observable<Unit> = this.goBack
    override fun heartDrawableId(): Observable<Int> = this.heartDrawableId
    override fun pledgeActionButtonColor(): Observable<Int> = this.pledgeActionButtonColor
    override fun pledgeActionButtonContainerIsGone(): Observable<Boolean> = this.pledgeActionButtonContainerIsGone
    override fun pledgeActionButtonText(): Observable<Int> = this.pledgeActionButtonText
    override fun pledgeToolbarNavigationIcon(): Observable<Int> = this.pledgeToolbarNavigationIcon
    override fun pledgeToolbarTitle(): Observable<Int> = this.pledgeToolbarTitle
    override fun prelaunchUrl(): Observable<String> = this.prelaunchUrl
    override fun projectData(): Observable<ProjectData> = this.projectData
    override fun reloadProjectContainerIsGone(): Observable<Boolean> = this.reloadProjectContainerIsGone
    override fun reloadProgressBarIsGone(): Observable<Boolean> = this.retryProgressBarIsGone
    override fun revealRewardsFragment(): Observable<Unit> = this.revealRewardsFragment
    override fun scrimIsVisible(): Observable<Boolean> = this.scrimIsVisible
    override fun setInitialRewardsContainerY(): Observable<Unit> = this.setInitialRewardPosition
    override fun showCancelPledgeFragment(): Observable<Project> = this.showCancelPledgeFragment
    override fun showCancelPledgeSuccess(): Observable<Unit> = this.showCancelPledgeSuccess
    override fun showPledgeNotCancelableDialog(): Observable<Unit> = this.showPledgeNotCancelableDialog
    override fun showSavedPrompt(): Observable<Unit> = this.showSavedPrompt
    override fun showShareSheet(): Observable<Pair<String, String>> = this.showShareSheet
    override fun showSocialShareSheet(): Observable<Project> = this.showSocialShareSheet
    override fun isNewSocialShareEnabled(): Boolean =
        statsigClient?.isReady?.value == true &&
            statsigClient.checkGate(StatsigGateKey.ANDROID_PRELAUNCH_SOCIAL_SHARE.key)
    override fun showUpdatePledge(): Observable<Pair<PledgeData, PledgeReason>> = this.showUpdatePledge
    override fun showUpdatePledgeSuccess(): Observable<Unit> = this.showUpdatePledgeSuccess
    override fun startRootCommentsActivity(): Observable<ProjectData> = this.startRootCommentsActivity
    override fun startRootCommentsForCommentsThreadActivity(): Observable<Pair<String, ProjectData>> =
        this.startRootCommentsForCommentsThreadActivity
    override fun startLoginToutActivity(): Observable<Unit> = this.startLoginToutActivity
    override fun startMessagesActivity(): Observable<Project> = this.startMessagesActivity
    override fun startProjectUpdateActivity(): Observable<Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>> =
        this.startProjectUpdateActivity
    override fun startProjectUpdateToRepliesDeepLinkActivity(): Observable<Pair<Pair<String, String>, Pair<Project, ProjectData>>> =
        this.startProjectUpdateToRepliesDeepLinkActivity
    override fun startThanksActivity(): Observable<Pair<CheckoutData, PledgeData>> = this.startThanksActivity
    override fun openBackingDetailsWebview(): Observable<String> = this.openBackingDetailsWebview
    override fun openPledgeManagerWebview(): Observable<String> = this.openPledgeManagerWebview
    override fun updateFragments(): Observable<ProjectData> = this.updateFragments
    override fun projectMedia(): Observable<MediaElement> = this.projectMedia
    override fun playButtonIsVisible(): Observable<Boolean> = this.playButtonIsVisible
    override fun backingViewGroupIsVisible(): Observable<Boolean> = this.backingViewGroupIsVisible
    override fun updateTabs(): Observable<List<PagerTabConfig>> = this.updateTabs
    override fun hideVideoPlayer(): Observable<Boolean> = this.hideVideoPlayer
    override fun onOpenVideoInFullScreen(): Observable<kotlin.Pair<String, Long>> = this.onOpenVideoInFullScreen
    override fun updateVideoCloseSeekPosition(): Observable<Long> = this.updateVideoCloseSeekPosition
    override fun showLatePledgeFlow(): Observable<Boolean> = this.showLatePledgeFlow
    override fun showPledgeRedemptionScreen(): Observable<Pair<Project, User>> = this.showPledgeRedemptionScreen
    override fun managePledgeMenuOptions(): Observable<ManagePledgeMenuOptions> = this.managePledgeMenuOptions
}
