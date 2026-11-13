package com.kickstarter.features.comments.viewmodel

import android.util.Pair
import androidx.lifecycle.ViewModel
import com.kickstarter.features.comments.data.CommentsFeatureInputs
import com.kickstarter.features.comments.data.CommentsFeatureOutputs
import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

/**
 * Holder class providing reactive subject infrastructure and input/output dispatching for the Comments feature.
 */
abstract class CommentsOutputsHolder : ViewModel(), CommentsFeatureInputs, CommentsFeatureOutputs {

    protected val backPressed = PublishSubject.create<Unit>()
    protected val refresh = PublishSubject.create<Unit>()
    protected val nextPage = PublishSubject.create<Unit>()
    protected val onShowGuideLinesLinkClicked = PublishSubject.create<Unit>()
    protected val onReplyClicked = PublishSubject.create<Pair<Comment, Boolean>>()
    protected val checkIfThereAnyPendingComments = PublishSubject.create<Boolean>()
    protected val failedCommentCardToRefresh = PublishSubject.create<Pair<Comment, Int>>()
    protected val showCanceledPledgeComment = PublishSubject.create<Comment>()
    protected val onResumeActivity = PublishSubject.create<Unit>()
    protected val insertNewCommentToList = PublishSubject.create<Pair<String, DateTime>>()
    protected val commentToRefresh = PublishSubject.create<Pair<Comment, Int>>()

    protected val closeCommentsPage = BehaviorSubject.create<Unit>()
    protected val currentUserAvatar = BehaviorSubject.create<String?>()
    protected val commentComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
    protected val showCommentComposer = BehaviorSubject.create<Boolean>()
    protected val commentsList = BehaviorSubject.create<List<CommentCardData>>()
    protected val outputCommentList = BehaviorSubject.create<List<CommentCardData>>()
    protected val showGuideLinesLink = BehaviorSubject.create<Unit>()
    protected val disableReplyButton = BehaviorSubject.create<Boolean>()
    protected val scrollToTop = BehaviorSubject.create<Boolean>()
    protected val isRefreshing = BehaviorSubject.create<Boolean>()
    protected val setEmptyState = BehaviorSubject.create<Boolean>()
    protected val displayInitialError = BehaviorSubject.create<Boolean>()
    protected val displayPaginationError = BehaviorSubject.create<Boolean>()
    protected val startThreadActivity = BehaviorSubject.create<Pair<Pair<CommentCardData, Boolean>, String?>>()
    protected val startThreadActivityFromDeepLink = BehaviorSubject.create<Pair<CommentCardData, String?>>()
    protected val hasPendingComments = BehaviorSubject.create<Pair<Boolean, Boolean>>()
    protected val internalError = BehaviorSubject.create<Throwable>()
    protected val initialError = BehaviorSubject.create<Throwable>()
    protected val paginationError = BehaviorSubject.create<Throwable>()
    protected val pullToRefreshError = BehaviorSubject.create<Throwable>()
    protected val commentableId = BehaviorSubject.create<String>()
    protected val isFetchingComments = BehaviorSubject.create<Boolean>()

    // - Inputs
    override fun backPressed() = backPressed.onNext(Unit)
    override fun refresh() = refresh.onNext(Unit)
    override fun nextPage() = nextPage.onNext(Unit)
    override fun insertNewCommentToList(comment: String, createdAt: DateTime) =
        insertNewCommentToList.onNext(Pair(comment, createdAt))
    override fun onShowGuideLinesLinkClicked() = onShowGuideLinesLinkClicked.onNext(Unit)
    override fun refreshComment(comment: Comment, position: Int) =
        this.commentToRefresh.onNext(Pair(comment, position))
    override fun onReplyClicked(comment: Comment, openKeyboard: Boolean) =
        onReplyClicked.onNext(Pair(comment, openKeyboard))
    override fun checkIfThereAnyPendingComments(isBackAction: Boolean) =
        checkIfThereAnyPendingComments.onNext(isBackAction)
    override fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int) =
        this.failedCommentCardToRefresh.onNext(Pair(comment, position))
    override fun onShowCanceledPledgeComment(comment: Comment) =
        this.showCanceledPledgeComment.onNext(comment)
    override fun onResumeActivity() = this.onResumeActivity.onNext(Unit)

    // - Outputs
    override fun closeCommentsPage(): Observable<Unit> = closeCommentsPage
    override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
    override fun commentComposerStatus(): Observable<CommentComposerStatus> = commentComposerStatus
    override fun showCommentComposer(): Observable<Boolean> = showCommentComposer
    override fun commentsList(): Observable<List<CommentCardData>> = this.outputCommentList
    override fun enableReplyButton(): Observable<Boolean> = disableReplyButton
    override fun showCommentGuideLinesLink(): Observable<Unit> = showGuideLinesLink
    override fun initialLoadCommentsError(): Observable<Throwable> = this.initialError
    override fun paginateCommentsError(): Observable<Throwable> = this.paginationError
    override fun pullToRefreshError(): Observable<Throwable> = this.pullToRefreshError
    override fun scrollToTop(): Observable<Boolean> = this.scrollToTop
    override fun shouldShowInitialLoadErrorUI(): Observable<Boolean> = this.displayInitialError
    override fun shouldShowPaginationErrorUI(): Observable<Boolean> = this.displayPaginationError
    override fun setEmptyState(): Observable<Boolean> = setEmptyState
    override fun startThreadActivity(): Observable<Pair<Pair<CommentCardData, Boolean>, String?>> = this.startThreadActivity
    override fun startThreadActivityFromDeepLink(): Observable<Pair<CommentCardData, String?>> = this.startThreadActivityFromDeepLink
    override fun isFetchingComments(): Observable<Boolean> = this.isFetchingComments
    override fun hasPendingComments(): Observable<Pair<Boolean, Boolean>> = this.hasPendingComments
}
