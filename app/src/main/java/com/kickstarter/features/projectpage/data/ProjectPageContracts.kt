package com.kickstarter.features.projectpage.data

import android.content.Intent
import android.util.Pair
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.libs.Either
import com.kickstarter.libs.ProjectPagerTabs
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

/**
 * Configuration descriptor for project pager tabs (e.g. Overview, Campaign, FAQs, AI, Environment).
 *
 * Negative Constraints:
 * - Do not mutate [isActive] directly on an existing instance; create a new immutable copy.
 * - Do not pass invalid or unmapped tab positions.
 *
 * @param tab The target pager tab category.
 * @param isActive Flag indicating if the tab has content and should be displayed.
 */
data class PagerTabConfig(
    val tab: ProjectPagerTabs,
    val isActive: Boolean
)

/**
 * Sealed hierarchy representing explicit UI/domain action states for the project page,
 * ensuring in-band actionable errors conforming to RFC 9457 problem details.
 *
 * Negative constraints:
 * - Do not throw bare exceptions across feature boundaries.
 * - Do not pass untyped generic error messages without actionable remediation advice.
 */
sealed interface ProjectPageActionState {
    /**
     * Idle state before any user action is triggered.
     *
     * Negative constraints:
     * - Do not render error or loading states when idle.
     */
    object Idle : ProjectPageActionState

    /**
     * Action is actively executing.
     *
     * Negative constraints:
     * - Do not allow repeated duplicate action clicks while in loading state.
     */
    object Loading : ProjectPageActionState

    /**
     * Action completed successfully.
     *
     * Negative constraints:
     * - Do not pass unvalidated project models.
     *
     * @property project Updated project entity.
     */
    data class Success(val project: Project) : ProjectPageActionState

    /**
     * Action failed with RFC 9457 problem details.
     *
     * Negative constraints:
     * - Do not pass empty details or blank suggested actions.
     *
     * @property problem Structured actionable error details carrying suggestedAction and isRetriable flag.
     */
    data class Failure(val problem: ProblemDetails) : ProjectPageActionState
}

/**
 * UI and external action inputs contract for the Project Page feature.
 *
 * Negative constraints:
 * - Do not invoke with unvalidated Intent extras.
 */
interface ProjectPageInputs {
    fun configureWith(intent: Intent)
    fun cancelPledgeClicked()
    fun commentsTextViewClicked()
    fun contactCreatorClicked()
    fun fixPaymentMethodButtonClicked()
    fun fragmentStackCount(count: Int)
    fun heartButtonClicked()
    fun nativeProjectActionButtonClicked()
    fun onGlobalLayout()
    fun fullScreenVideoButtonClicked(videoInfo: kotlin.Pair<String, Long>)
    fun pledgePaymentSuccessfullyUpdated()
    fun pledgeSuccessfullyCancelled()
    fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>)
    fun pledgeSuccessfullyUpdated()
    fun pledgeToolbarNavigationClicked()
    fun refreshProject()
    fun reloadProjectContainerClicked()
    fun shareButtonClicked()
    fun updatePaymentClicked()
    fun updatesTextViewClicked()
    fun viewRewardsClicked()
    fun tabSelected(position: Int)
    fun closeFullScreenVideo(seekPosition: Long)
    fun onVideoPlayButtonClicked()
    fun activityResult(result: ActivityResult)
    fun continuePledgeFlow(callback: () -> Unit)
}

/**
 * Reactive state and UI event outputs contract for the Project Page feature.
 *
 * Negative constraints:
 * - Do not emit raw unformatted currency values.
 */
interface ProjectPageOutputs {
    fun backingDetailsIsVisible(): Observable<Boolean>
    fun backingDetailsSubtitle(): Observable<Either<String, Int>?>
    fun backingDetailsTitle(): Observable<Int>
    fun expandPledgeSheet(): Observable<Pair<Boolean, Boolean>>
    fun goBack(): Observable<Unit>
    fun heartDrawableId(): Observable<Int>
    fun pledgeActionButtonColor(): Observable<Int>
    fun pledgeActionButtonContainerIsGone(): Observable<Boolean>
    fun pledgeActionButtonText(): Observable<Int>
    fun pledgeToolbarNavigationIcon(): Observable<Int>
    fun pledgeToolbarTitle(): Observable<Int>
    fun prelaunchUrl(): Observable<String>
    fun projectData(): Observable<ProjectData>
    fun reloadProjectContainerIsGone(): Observable<Boolean>
    fun reloadProgressBarIsGone(): Observable<Boolean>
    fun revealRewardsFragment(): Observable<Unit>
    fun scrimIsVisible(): Observable<Boolean>
    fun setInitialRewardsContainerY(): Observable<Unit>
    fun showCancelPledgeFragment(): Observable<Project>
    fun showCancelPledgeSuccess(): Observable<Unit>
    fun showPledgeNotCancelableDialog(): Observable<Unit>
    fun showSavedPrompt(): Observable<Unit>
    fun showShareSheet(): Observable<Pair<String, String>>
    fun showSocialShareSheet(): Observable<Project>
    fun isNewSocialShareEnabled(): Boolean
    fun showUpdatePledge(): Observable<Pair<PledgeData, PledgeReason>>
    fun showUpdatePledgeSuccess(): Observable<Unit>
    fun startRootCommentsActivity(): Observable<ProjectData>
    fun startRootCommentsForCommentsThreadActivity(): Observable<Pair<String, ProjectData>>
    fun startLoginToutActivity(): Observable<Unit>
    fun startMessagesActivity(): Observable<Project>
    fun startProjectUpdateActivity(): Observable<Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>>
    fun startProjectUpdateToRepliesDeepLinkActivity(): Observable<Pair<Pair<String, String>, Pair<Project, ProjectData>>>
    fun startThanksActivity(): Observable<Pair<CheckoutData, PledgeData>>
    fun openBackingDetailsWebview(): Observable<String>
    fun openPledgeManagerWebview(): Observable<String>
    fun updateFragments(): Observable<ProjectData>
    fun projectMedia(): Observable<MediaElement>
    fun playButtonIsVisible(): Observable<Boolean>
    fun backingViewGroupIsVisible(): Observable<Boolean>
    fun updateTabs(): Observable<List<PagerTabConfig>>
    fun hideVideoPlayer(): Observable<Boolean>
    fun onOpenVideoInFullScreen(): Observable<kotlin.Pair<String, Long>>
    fun updateVideoCloseSeekPosition(): Observable<Long>
    fun showLatePledgeFlow(): Observable<Boolean>
    fun showPledgeRedemptionScreen(): Observable<Pair<Project, User>>
    fun managePledgeMenuOptions(): Observable<ManagePledgeMenuOptions>
}
