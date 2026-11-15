package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.extensions.assignAuthorBadge
import com.kickstarter.models.extensions.isReply
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

internal fun CommentsViewHolderViewModel.ViewModel.initPipelines(
    environment: Environment,
    disposables: CompositeDisposable
) {
    val comment = Observable.merge(this.commentInput.distinctUntilChanged().map { it.comment }, postedSuccessfully)
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }

    configureCommentCardWithComment(comment, disposables)

    val commentCardStatus = this.commentInput
        .compose(combineLatestPair(currentUser.observable()))
        .distinctUntilChanged()
        .filter { it.isNotNull() }
        .map {
            val commentCardState = CommentsViewHolderViewModelHelper.cardStatus(it.first, it.second.getValue(), isCommentEnableThreads)
            it.first.toBuilder().commentCardState(commentCardState?.commentCardStatus ?: 0).build()
        }
    handleStatus(commentCardStatus, disposables)

    val commentData = this.commentInput
        .distinctUntilChanged()
        .withLatestFrom(currentUser.loggedInUser()) { input, user -> Pair(input, user) }
        .filter { CommentsViewHolderViewModelHelper.shouldCommentBePosted(it) }
        .map { Pair(requireNotNull(it.first), requireNotNull(it.first.project)) }

    postComment(commentData, internalError, environment, disposables)

    this.internalError
        .compose(combineLatestPair(commentData))
        .distinctUntilChanged()
        .delay(1, TimeUnit.SECONDS, environment.schedulerV2())
        .subscribe {
            this.commentCardStatus.onNext(CommentCardStatus.FAILED_TO_SEND_COMMENT)
            it.second.first.comment?.let { it1 -> this.failedToPosted.onNext(it1) }
        }
        .addToDisposable(disposables)

    comment
        .withLatestFrom(currentUser.observable()) { c, user -> Pair(c, user) }
        .map { it.first.assignAuthorBadge(it.second.getValue()) }
        .subscribe { this.authorBadge.onNext(it) }
        .addToDisposable(disposables)
}

private fun CommentsViewHolderViewModel.ViewModel.handleStatus(
    commentCardStatus: Observable<CommentCardData>,
    disposables: CompositeDisposable
) {
    commentCardStatus
        .compose(combineLatestPair(currentUser.observable()))
        .subscribe {
            CommentsViewHolderViewModelHelper.cardStatus(it.first, it.second.getValue(), isCommentEnableThreads)?.let { status ->
                this.commentCardStatus.onNext(status)
            }
        }
        .addToDisposable(disposables)

    commentCardStatus
        .compose(combineLatestPair(currentUser.observable()))
        .subscribe {
            this.isReplyButtonVisible.onNext(
                CommentsViewHolderViewModelHelper.shouldReplyButtonBeVisible(
                    it.first,
                    it.second.getValue()
                )
            )
        }
        .addToDisposable(disposables)
}

private fun CommentsViewHolderViewModel.ViewModel.configureCommentCardWithComment(
    comment: Observable<Comment>,
    disposables: CompositeDisposable
) {
    comment
        .filter { it.parentId() > 0 }
        .subscribe { this.isCommentReply.onNext(Unit) }
        .addToDisposable(disposables)

    comment
        .map { it.repliesCount() }
        .compose(combineLatestPair(this.isCommentEnableThreads))
        .subscribe { this.commentRepliesCount.onNext(it.first) }
        .addToDisposable(disposables)

    comment
        .filter { it.author()?.name().isNotNull() }
        .map { it.author()?.name() ?: "" }
        .subscribe { this.commentAuthorName.onNext(it) }
        .addToDisposable(disposables)

    comment
        .filter { it.author().avatar().medium().isNotNull() }
        .map { aComment ->
            aComment.author().avatar().medium().ifBlank {
                if (aComment.author().avatar().small().isNullOrBlank()) ""
                else aComment.author().avatar().small()
            }
        }
        .subscribe { this.commentAuthorAvatarUrl.onNext(it) }
        .addToDisposable(disposables)

    comment
        .map { it.body() }
        .filter { it.isNotNull() }
        .subscribe { this.commentMessageBody.onNext(it) }
        .addToDisposable(disposables)

    comment
        .map { it.createdAt() }
        .filter { it.isNotNull() }
        .subscribe { it?.let { date -> this.commentPostTime.onNext(date) } }
        .addToDisposable(disposables)

    comment
        .compose(takeWhenV2(this.onViewCommentRepliesButtonClicked))
        .subscribe { this.viewCommentReplies.onNext(it) }
        .addToDisposable(disposables)

    comment
        .compose(takeWhenV2(this.onCommentGuideLinesClicked))
        .subscribe { this.openCommentGuideLines.onNext(it) }
        .addToDisposable(disposables)

    comment
        .compose(takeWhenV2(this.onReplyButtonClicked))
        .subscribe { this.replyToComment.onNext(it) }
        .addToDisposable(disposables)

    comment
        .compose(takeWhenV2(this.onRetryViewClicked))
        .doOnNext { this.commentCardStatus.onNext(CommentCardStatus.RE_TRYING_TO_POST) }
        .subscribe { this.retrySendComment.onNext(it) }
        .addToDisposable(disposables)

    comment
        .compose(takeWhenV2(this.onFlagButtonClicked))
        .subscribe { this.flagComment.onNext(it) }
        .addToDisposable(disposables)

    comment
        .compose(takeWhenV2(this.onShowCommentClicked))
        .subscribe { this.showCanceledComment.onNext(it) }
        .addToDisposable(disposables)
}

private fun CommentsViewHolderViewModel.ViewModel.postComment(
    commentData: Observable<Pair<CommentCardData, Project>>,
    errorObservable: BehaviorSubject<Throwable>,
    environment: Environment,
    disposables: CompositeDisposable
) {
    val postCommentData = commentData
        .map {
            Pair(
                requireNotNull(it.first.commentableId),
                requireNotNull(it.first?.comment)
            )
        }
        .map {
            PostCommentData(
                commentableId = it.first,
                body = it.second.body(),
                clientMutationId = null,
                parent = it.second?.parentId()?.let { id -> it.second.toBuilder().id(id).build() }
            )
        }

    postCommentData
        .map { CommentsViewHolderViewModelHelper.executePostCommentMutation(apolloClient, it, errorObservable) }
        .switchMap { it }
        .subscribe {
            this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
            this.postedSuccessfully.onNext(it)
            if (it.isReply()) this.isReplyButtonVisible.onNext(false)
        }
        .addToDisposable(disposables)

    Observable
        .combineLatest(onRetryViewClicked, postCommentData) { _, newData ->
            return@combineLatest CommentsViewHolderViewModelHelper.executePostCommentMutation(apolloClient, newData, errorObservable)
        }
        .switchMap { it }
        .doOnNext {
            this.commentCardStatus.onNext(CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY)
        }
        .delay(3000, TimeUnit.MILLISECONDS, environment.schedulerV2())
        .subscribe {
            this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
            this.postedSuccessfully.onNext(it)
            if (it.isReply()) this.isReplyButtonVisible.onNext(false)
        }
        .addToDisposable(disposables)
}
