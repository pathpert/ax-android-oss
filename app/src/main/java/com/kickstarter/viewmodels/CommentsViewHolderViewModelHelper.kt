package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Comment
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isCommentPendingReview
import com.kickstarter.models.extensions.isCurrentUserAuthor
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

internal object CommentsViewHolderViewModelHelper {

    /**
     * In order to decide if a comment needs to be posted we need to check:
     * - The comment author is the current user
     * - the comment id is negative
     * - The state is TRYING_TO_POST or FAILED_TO_SEND_COMMENT
     */
    fun shouldCommentBePosted(dataCommentAndUser: Pair<CommentCardData, User>): Boolean {
        var shouldPost = false
        val currentUser = dataCommentAndUser.second
        val comment = dataCommentAndUser.first?.comment?.let { return@let it }
        val status = dataCommentAndUser.first.commentCardState

        comment?.let {
            shouldPost = it.id() < 0 && it.author() == currentUser
        }

        shouldPost = shouldPost && (
            status == CommentCardStatus.TRYING_TO_POST.commentCardStatus ||
                status == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus
            )

        return shouldPost
    }

    /**
     * Checks if reply button should be visible
     */
    fun shouldReplyButtonBeVisible(
        commentCardData: CommentCardData,
        user: User?
    ): Boolean =
        commentCardData.project?.let {
            (it.isBacking() || it.userIsCreator(user)) &&
                (
                    commentCardData.commentCardState == CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus ||
                        commentCardData.commentCardState == CommentCardStatus.COMMENT_WITH_REPLIES.commentCardStatus
                    ) &&
                (commentCardData.comment?.parentId() ?: -1) < 0
        } ?: false

    /**
     * Updates the status of the current comment card.
     */
    fun cardStatus(
        commentCardData: CommentCardData,
        currentUser: User?,
        isCommentEnableThreads: PublishSubject<Boolean>? = null
    ): CommentCardStatus? = when {
        commentCardData.comment?.isCommentPendingReview() ?: false &&
            commentCardData.comment?.isCurrentUserAuthor(currentUser) == false -> CommentCardStatus.FLAGGED_COMMENT
        commentCardData.comment?.deleted() ?: false -> CommentCardStatus.DELETED_COMMENT
        commentCardData.comment?.authorCanceledPledge() ?: false -> checkCanceledPledgeCommentStatus(commentCardData)
        (commentCardData.comment?.repliesCount() ?: 0 != 0) -> CommentCardStatus.COMMENT_WITH_REPLIES
        else -> CommentCardStatus.values().firstOrNull {
            it.commentCardStatus == commentCardData.commentCardState
        }
    }.also {
        isCommentEnableThreads?.onNext(true)
    }

    private fun checkCanceledPledgeCommentStatus(commentCardData: CommentCardData): CommentCardStatus =
        if (commentCardData.commentCardState != CommentCardStatus.CANCELED_PLEDGE_COMMENT.commentCardStatus)
            CommentCardStatus.CANCELED_PLEDGE_MESSAGE
        else
            CommentCardStatus.CANCELED_PLEDGE_COMMENT

    fun executePostCommentMutation(
        apolloClient: ApolloClientTypeV2,
        postCommentData: PostCommentData,
        errorObservable: BehaviorSubject<Throwable>
    ): Observable<Comment> =
        apolloClient.createComment(postCommentData)
            .doOnError {
                val problem = ProblemDetailsMapper.fromThrowable(it, "post comment")
                errorObservable.onNext(ProblemException(problem, it))
            }
            .onErrorResumeNext(Observable.empty())
}
