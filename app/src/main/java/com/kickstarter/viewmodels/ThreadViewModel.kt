package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

@Deprecated(
    message = "Legacy monolithic path replaced by CommentsFeatureViewModel under com.kickstarter.features.comments.viewmodel.",
    replaceWith = ReplaceWith("CommentsFeatureViewModel", "com.kickstarter.features.comments.viewmodel.CommentsFeatureViewModel")
)
interface ThreadViewModel {

    interface Inputs {
        fun nextPage()
        fun reloadRepliesPage()
        fun insertNewReplyToList(comment: String, createdAt: DateTime)
        fun onShowGuideLinesLinkClicked()
        fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int)
        fun refreshCommentCardInCaseSuccessPosted(comment: Comment, position: Int)
        fun checkIfThereAnyPendingComments()
        fun backPressed()
        fun onShowCanceledPledgeComment(comment: Comment)
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        /** The anchored root comment */
        fun getRootComment(): Observable<CommentCardData>

        /** get comment replies **/
        fun onCommentReplies(): Observable<Pair<List<CommentCardData>, Boolean>>

        /** Will tell to the compose view if should open the keyboard */
        fun shouldFocusOnCompose(): Observable<Boolean>
        fun scrollToBottom(): Observable<Unit>

        fun currentUserAvatar(): Observable<String?>
        fun replyComposerStatus(): Observable<CommentComposerStatus>
        fun showReplyComposer(): Observable<Boolean>

        fun isFetchingReplies(): Observable<Boolean>
        fun loadMoreReplies(): Observable<Unit>

        /** Display the pagination Error Cell **/
        fun shouldShowPaginationErrorUI(): Observable<Boolean>

        /** Display the Initial Error Cell **/
        fun initialLoadCommentsError(): Observable<Boolean>

        fun refresh(): Observable<Unit>

        fun showCommentGuideLinesLink(): Observable<Unit>
        fun hasPendingComments(): Observable<Boolean>
        fun closeThreadActivity(): Observable<Unit>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class ThreadViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {
        internal val apolloClient = requireNotNull(environment.apolloClientV2())
        internal val currentUserStream = requireNotNull(environment.currentUserV2())
        internal val analyticEvents = requireNotNull(environment.analytics())
        internal val nextPage = PublishSubject.create<Unit>()
        internal val onLoadingReplies = PublishSubject.create<Unit>()
        internal val insertNewReplyToList = PublishSubject.create<Pair<String, DateTime>>()
        internal val onShowGuideLinesLinkClicked = PublishSubject.create<Unit>()
        internal val failedCommentCardToRefresh = PublishSubject.create<Pair<Comment, Int>>()
        internal val successfullyPostedCommentCardToRefresh =
            PublishSubject.create<Pair<Comment, Int>>()
        internal val checkIfThereAnyPendingComments = PublishSubject.create<Unit>()
        internal val backPressed = PublishSubject.create<Unit>()
        internal val showCanceledPledgeComment = PublishSubject.create<Comment>()

        internal val rootComment = BehaviorSubject.create<CommentCardData>()
        internal val focusOnCompose = BehaviorSubject.create<Boolean>()
        internal val currentUserAvatar = BehaviorSubject.create<String?>()
        internal val replyComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        internal val showReplyComposer = BehaviorSubject.create<Boolean>()
        internal val scrollToBottom = BehaviorSubject.create<Unit>()
        internal val isFetchingReplies = BehaviorSubject.create<Boolean>()
        internal val hasPreviousElements = BehaviorSubject.create<Boolean>()
        internal val refresh = PublishSubject.create<Unit>()
        internal val loadMoreReplies = PublishSubject.create<Unit>()
        internal val displayPaginationError = BehaviorSubject.create<Boolean>()
        internal val initialLoadCommentsError = BehaviorSubject.create<Boolean>()
        internal val showGuideLinesLink = BehaviorSubject.create<Unit>()
        internal val hasPendingComments = BehaviorSubject.create<Boolean>()
        internal val closeThreadActivity = BehaviorSubject.create<Unit>()

        internal val intent = PublishSubject.create<Intent>()

        internal val disposables = CompositeDisposable()

        internal val onCommentReplies =
            BehaviorSubject.create<Pair<List<CommentCardData>, Boolean>>()
        internal var project: Project? = null
        internal var currentUser: User? = null

        val inputs: Inputs = this
        val outputs: Outputs = this

        internal val internalError = BehaviorSubject.create<Throwable>()
        internal val initialError = BehaviorSubject.create<Throwable>()
        internal val paginationError = BehaviorSubject.create<Throwable>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val newlyPostedRepliesList = mutableListOf<CommentCardData>()

        init {
            initPipelines(disposables)
        }

        override fun nextPage() = nextPage.onNext(Unit)
        override fun reloadRepliesPage() = onLoadingReplies.onNext(Unit)
        override fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int) =
            this.failedCommentCardToRefresh.onNext(Pair(comment, position))

        override fun refreshCommentCardInCaseSuccessPosted(comment: Comment, position: Int) =
            this.successfullyPostedCommentCardToRefresh.onNext(Pair(comment, position))

        override fun getRootComment(): Observable<CommentCardData> = this.rootComment
        override fun onCommentReplies(): Observable<Pair<List<CommentCardData>, Boolean>> =
            this.onCommentReplies

        override fun insertNewReplyToList(comment: String, createdAt: DateTime) =
            this.insertNewReplyToList.onNext(
                Pair(comment, createdAt)
            )

        override fun onShowGuideLinesLinkClicked() = onShowGuideLinesLinkClicked.onNext(Unit)

        override fun shouldFocusOnCompose(): Observable<Boolean> = this.focusOnCompose
        override fun scrollToBottom(): Observable<Unit> = this.scrollToBottom
        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun replyComposerStatus(): Observable<CommentComposerStatus> = replyComposerStatus
        override fun showReplyComposer(): Observable<Boolean> = showReplyComposer
        override fun isFetchingReplies(): Observable<Boolean> = this.isFetchingReplies
        override fun loadMoreReplies(): Observable<Unit> = this.loadMoreReplies
        override fun showCommentGuideLinesLink(): Observable<Unit> = showGuideLinesLink
        override fun checkIfThereAnyPendingComments() = checkIfThereAnyPendingComments.onNext(Unit)
        override fun backPressed() = backPressed.onNext(Unit)
        override fun onShowCanceledPledgeComment(comment: Comment) =
            this.showCanceledPledgeComment.onNext(comment)

        override fun shouldShowPaginationErrorUI(): Observable<Boolean> =
            this.displayPaginationError

        override fun initialLoadCommentsError(): Observable<Boolean> = this.initialLoadCommentsError
        override fun refresh(): Observable<Unit> = this.refresh
        override fun hasPendingComments(): Observable<Boolean> = this.hasPendingComments
        override fun closeThreadActivity(): Observable<Unit> = this.closeThreadActivity

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        fun intent(intent: Intent) {
            this.intent.onNext(intent)
        }
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThreadViewModel(environment) as T
        }
    }
}
