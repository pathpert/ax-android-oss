package com.kickstarter.viewmodels

import com.kickstarter.libs.Environment
import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardBadge
import com.kickstarter.ui.views.CommentCardStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

@Deprecated(
    message = "Legacy monolithic path replaced by CommentsFeatureViewModel under com.kickstarter.features.comments.viewmodel.",
    replaceWith = ReplaceWith("CommentsFeatureViewModel", "com.kickstarter.features.comments.viewmodel.CommentsFeatureViewModel")
)
interface CommentsViewHolderViewModel {
    interface Inputs {
        /** Call when the user clicks learn more about comment guidelines. */
        fun onCommentGuideLinesClicked()

        /** Call when the user clicks retry view to send message */
        fun onRetryViewClicked()

        /** Call when the user clicks reply to comment */
        fun onReplyButtonClicked()

        /** Call when the user clicks view replies to comment */
        fun onViewRepliesButtonClicked()

        /** Call when the user clicks flag comment */
        fun onFlagButtonClicked()

        /** Configure the view model with the [Comment]. */
        fun configureWith(commentCardData: CommentCardData)

        /** Show cancelled comment pledge */
        fun onShowCommentClicked()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        /** Emits the commentCardStatus */
        fun commentCardStatus(): Observable<CommentCardStatus>

        /** Emits the comment replies count. */
        fun commentRepliesCount(): Observable<Int>

        /** Emits the comment Author Name. */
        fun commentAuthorName(): Observable<String>

        /** Emits the comment Author avatar string url. */
        fun commentAuthorAvatarUrl(): Observable<String>

        /** Emits the comment Author avatar string url. */
        fun commentMessageBody(): Observable<String>

        /** Emits the comment post time */
        fun commentPostTime(): Observable<DateTime>

        /** Emits the visibility of the comment card action group */
        fun isReplyButtonVisible(): Observable<Boolean>

        /** Emits the current [Comment] when Comment GuideLines clicked.. */
        fun openCommentGuideLines(): Observable<Comment>

        /** Emits the current [Comment] when Retry clicked.. */
        fun retrySendComment(): Observable<Comment>

        /** Emits the current [Comment] when Reply clicked.. */
        fun replyToComment(): Observable<Comment>

        /** Emits the current [Comment] when flag clicked.. */
        fun flagComment(): Observable<Comment>

        /** Emits the current [Comment] when view replies clicked.. */
        fun viewCommentReplies(): Observable<Comment>

        /** Emits the current status to the CommentCard UI*/
        fun isCommentEnableThreads(): Observable<Boolean>

        /** Emits if the comment is a reply to root comment */
        fun isCommentReply(): Observable<Unit>

        /** Emits when the execution of the post mutation is successful, it will be used to update the main list state for this comment**/
        fun isSuccessfullyPosted(): Observable<Comment>

        /** Emits when the execution of the post mutation is error, it will be used to update the main list state for this comment**/
        fun isFailedToPost(): Observable<Comment>

        /** Emits the current [Comment] when show comment for canceled pledge. */
        fun showCanceledComment(): Observable<Comment>

        fun authorBadge(): Observable<CommentCardBadge>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class ViewModel(environment: Environment) : Inputs, Outputs {
        internal val commentInput = PublishSubject.create<CommentCardData>()
        internal val onCommentGuideLinesClicked = PublishSubject.create<Unit>()
        internal val onRetryViewClicked = PublishSubject.create<Unit>()
        internal val onReplyButtonClicked = PublishSubject.create<Unit>()
        internal val onFlagButtonClicked = PublishSubject.create<Unit>()
        internal val onViewCommentRepliesButtonClicked = PublishSubject.create<Unit>()
        internal val onShowCommentClicked = PublishSubject.create<Unit>()

        internal val commentCardStatus = BehaviorSubject.create<CommentCardStatus>()
        internal val authorBadge = BehaviorSubject.create<CommentCardBadge>()
        internal val isReplyButtonVisible = BehaviorSubject.create<Boolean>()
        internal val commentAuthorName = BehaviorSubject.create<String>()
        internal val commentAuthorAvatarUrl = BehaviorSubject.create<String>()
        internal val commentMessageBody = BehaviorSubject.create<String>()
        internal val commentRepliesCount = BehaviorSubject.create<Int>()
        internal val commentPostTime = BehaviorSubject.create<DateTime>()
        internal val openCommentGuideLines = PublishSubject.create<Comment>()
        internal val retrySendComment = PublishSubject.create<Comment>()
        internal val replyToComment = PublishSubject.create<Comment>()
        internal val flagComment = PublishSubject.create<Comment>()
        internal val viewCommentReplies = PublishSubject.create<Comment>()
        internal val isCommentEnableThreads = PublishSubject.create<Boolean>()
        internal val internalError = BehaviorSubject.create<Throwable>()
        internal val postedSuccessfully = BehaviorSubject.create<Comment>()
        internal val failedToPosted = BehaviorSubject.create<Comment>()
        internal val showCanceledComment = PublishSubject.create<Comment>()
        internal val isCommentReply = BehaviorSubject.create<Unit>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        internal val apolloClient = requireNotNull(environment.apolloClientV2())
        internal val currentUser = requireNotNull(environment.currentUserV2())

        private val disposables = CompositeDisposable()

        init {
            initPipelines(environment, disposables)
        }

        override fun configureWith(commentCardData: CommentCardData) =
            this.commentInput.onNext(commentCardData)

        override fun onCommentGuideLinesClicked() = this.onCommentGuideLinesClicked.onNext(Unit)

        override fun onRetryViewClicked() = this.onRetryViewClicked.onNext(Unit)

        override fun onReplyButtonClicked() = this.onReplyButtonClicked.onNext(Unit)

        override fun onViewRepliesButtonClicked() =
            this.onViewCommentRepliesButtonClicked.onNext(Unit)

        override fun onFlagButtonClicked() = this.onFlagButtonClicked.onNext(Unit)

        override fun onShowCommentClicked() = this.onShowCommentClicked.onNext(Unit)

        override fun commentCardStatus(): Observable<CommentCardStatus> = this.commentCardStatus

        override fun commentRepliesCount(): Observable<Int> = this.commentRepliesCount

        override fun commentAuthorName(): Observable<String> = this.commentAuthorName

        override fun commentAuthorAvatarUrl(): Observable<String> = this.commentAuthorAvatarUrl

        override fun commentMessageBody(): Observable<String> = this.commentMessageBody

        override fun commentPostTime(): Observable<DateTime> = this.commentPostTime

        override fun isReplyButtonVisible(): Observable<Boolean> = this.isReplyButtonVisible

        override fun openCommentGuideLines(): Observable<Comment> = openCommentGuideLines

        override fun retrySendComment(): Observable<Comment> = retrySendComment

        override fun replyToComment(): Observable<Comment> = replyToComment

        override fun flagComment(): Observable<Comment> = flagComment

        override fun viewCommentReplies(): Observable<Comment> = this.viewCommentReplies

        override fun isCommentEnableThreads(): Observable<Boolean> = this.isCommentEnableThreads

        override fun isCommentReply(): Observable<Unit> = this.isCommentReply

        override fun isSuccessfullyPosted(): Observable<Comment> = this.postedSuccessfully

        override fun isFailedToPost(): Observable<Comment> = this.failedToPosted

        override fun showCanceledComment(): Observable<Comment> = this.showCanceledComment

        override fun authorBadge(): Observable<CommentCardBadge> = this.authorBadge

        fun onCleared() {
            disposables.clear()
        }
    }
}
