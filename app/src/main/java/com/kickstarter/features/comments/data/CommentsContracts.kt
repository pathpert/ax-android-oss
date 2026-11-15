package com.kickstarter.features.comments.data

import android.util.Pair
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import org.joda.time.DateTime

/**
 * Validated comment body value class enforcing business constraints.
 *
 * Negative Constraints:
 * - Do NOT pass blank or empty strings into [CommentBody].
 * - Do NOT pass strings exceeding 9,000 characters into [CommentBody].
 */
@JvmInline
value class CommentBody(val value: String) {
    init {
        require(value.isNotBlank()) { "Negative Constraint Violated: Comment body must not be blank." }
        require(value.length <= 9000) { "Negative Constraint Violated: Comment body must not exceed 9,000 characters." }
    }
}

/**
 * Sealed UI and loading state representation for the Comments feature.
 *
 * Negative constraints:
 * - Do not expose raw exceptions; map errors to [ProblemDetails].
 */
sealed interface CommentsUIState {
    /**
     * Initial comment list loading state.
     *
     * Negative constraints:
     * - Do not render empty or loaded state views while loading.
     */
    data object Loading : CommentsUIState

    /**
     * Non-empty comment list loaded successfully.
     *
     * Negative constraints:
     * - Do not use for empty comment lists; emit [Empty] instead.
     *
     * @property comments Non-empty list of formatted comment cards.
     */
    data class Loaded(val comments: List<CommentCardData>) : CommentsUIState

    /**
     * Project or update has zero comments.
     *
     * Negative constraints:
     * - Do not display retry or error controls in empty state.
     */
    data object Empty : CommentsUIState

    /**
     * Actionable error encountered while loading or posting comments.
     *
     * Negative constraints:
     * - Do not pass uncontextualized errors without suggested action.
     *
     * @property problem Structured RFC 9457 error details carrying suggestedAction and isRetriable flag.
     * @property throwable Underlying exception cause.
     */
    data class Error(val problem: ProblemDetails, val throwable: Throwable = Exception(problem.detail)) : CommentsUIState
}

/**
 * Modern user action inputs for the Comments feature.
 *
 * Negative Constraints:
 * - Do NOT invoke with unvalidated comment strings.
 */
interface CommentsFeatureInputs {
    fun refresh()
    fun nextPage()
    fun backPressed()
    fun insertNewCommentToList(comment: String, createdAt: DateTime)
    fun onReplyClicked(comment: Comment, openKeyboard: Boolean)
    fun onShowGuideLinesLinkClicked()
    fun checkIfThereAnyPendingComments(isBackAction: Boolean)
    fun refreshComment(comment: Comment, position: Int)
    fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int)
    fun onShowCanceledPledgeComment(comment: Comment)
    fun onResumeActivity()
}

/**
 * Modern reactive outputs emitted by the Comments feature.
 *
 * Negative Constraints:
 * - Do NOT emit raw throwable without problem details fallback.
 */
interface CommentsFeatureOutputs {
    fun closeCommentsPage(): Observable<Unit>
    fun currentUserAvatar(): Observable<String?>
    fun commentComposerStatus(): Observable<CommentComposerStatus>
    fun enableReplyButton(): Observable<Boolean>
    fun showCommentComposer(): Observable<Boolean>
    fun commentsList(): Observable<List<CommentCardData>>
    fun scrollToTop(): Observable<Boolean>
    fun setEmptyState(): Observable<Boolean>
    fun showCommentGuideLinesLink(): Observable<Unit>
    fun initialLoadCommentsError(): Observable<Throwable>
    fun paginateCommentsError(): Observable<Throwable>
    fun pullToRefreshError(): Observable<Throwable>
    fun startThreadActivity(): Observable<Pair<Pair<CommentCardData, Boolean>, String?>>
    fun startThreadActivityFromDeepLink(): Observable<Pair<CommentCardData, String?>>
    fun hasPendingComments(): Observable<Pair<Boolean, Boolean>>
    fun isFetchingComments(): Observable<Boolean>
    fun shouldShowPaginationErrorUI(): Observable<Boolean>
    fun shouldShowInitialLoadErrorUI(): Observable<Boolean>
}
