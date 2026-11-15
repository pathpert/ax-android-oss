package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.loadmore.ApolloPaginateV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Comment
import com.kickstarter.models.extensions.updateCanceledPledgeComment
import com.kickstarter.models.extensions.updateCommentAfterSuccessfulPost
import com.kickstarter.models.extensions.updateCommentFailedToPost
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

/**
 * ViewModel managing state transitions and user intents for ThreadViewModel.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
internal fun ThreadViewModel.ThreadViewModel.initPipelines(disposables: CompositeDisposable) {
    val commentData = ThreadViewModelHelper.getCommentCardDataFromIntent(intent)
        .distinctUntilChanged()
        .filter { it.isNotNull() }
        .map { it }

    intent
        .map { it.getBooleanExtra(IntentKey.REPLY_EXPAND, false) }
        .distinctUntilChanged()
        .subscribe { this.focusOnCompose.onNext(it) }
        .addToDisposable(disposables)

    val comment = ThreadViewModelHelper.getCommentCardDataFromIntent(intent)
        .map { it.comment }
        .map { it }

    val project = commentData
        .filter { it.project.isNotNull() }
        .map { requireNotNull(it.project) }

    project.take(1)
        .subscribe { this.project = it }
        .addToDisposable(disposables)

    currentUserStream
        .observable()
        .take(1)
        .subscribe { this.currentUser = it.getValue() }
        .addToDisposable(disposables)

    loadCommentListFromProjectOrUpdate(comment, disposables)

    this.insertNewReplyToList
        .distinctUntilChanged()
        .withLatestFrom(this.currentUserStream.loggedInUser()) { comment, user -> Pair(comment, user) }
        .withLatestFrom(commentData) { reply, parent -> Pair(reply, parent) }
        .map {
            Pair(
                it.second,
                ThreadViewModelHelper.buildReplyBody(Pair(Pair(it.second, it.first.second), it.first.first))
            )
        }
        .map {
            CommentCardData.builder()
                .comment(it.second)
                .project(it.first.project)
                .commentableId(it.first.commentableId)
                .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
                .build()
        }
        .withLatestFrom(this.onCommentReplies) { reply, pair ->
            Pair(
                pair.first.toMutableList().apply {
                    newlyPostedRepliesList.add(0, reply)
                    add(0, reply)
                }.toList(),
                pair.second
            )
        }
        .subscribe {
            onCommentReplies.onNext(it)
            scrollToBottom.onNext(Unit)
        }
        .addToDisposable(disposables)

    commentData
        .subscribe { this.rootComment.onNext(it) }
        .addToDisposable(disposables)

    val loggedInUser = this.currentUserStream.loggedInUser().map { it }

    loggedInUser
        .subscribe { currentUserAvatar.onNext(it.avatar().small()) }
        .addToDisposable(disposables)

    loggedInUser
        .subscribe { showReplyComposer.onNext(true) }
        .addToDisposable(disposables)

    project
        .compose(Transformers.combineLatestPair(currentUserStream.observable()))
        .subscribe {
            val composerStatus = ThreadViewModelHelper.getCommentComposerStatus(Pair(it.first, it.second.getValue()))
            showReplyComposer.onNext(composerStatus != CommentComposerStatus.GONE)
            replyComposerStatus.onNext(composerStatus)
        }
        .addToDisposable(disposables)

    val projectUpdateId = ThreadViewModelHelper.getProjectUpdateId(intent)

    project
        .compose(Transformers.combineLatestPair(projectUpdateId))
        .compose(Transformers.combineLatestPair(comment))
        .distinctUntilChanged()
        .subscribe {
            analyticEvents.trackThreadCommentPageViewed(
                it.first.first,
                it.second.id().toString(),
                it.first.second
            )
        }
        .addToDisposable(disposables)

    this.onLoadingReplies
        .map { this.onCommentReplies.value ?: Pair(listOf(), false) }
        .subscribe {
            if (it?.first?.isNotEmpty() == true) {
                this.loadMoreReplies.onNext(Unit)
            } else {
                this.refresh.onNext(Unit)
            }
        }
        .addToDisposable(disposables)

    this.initialError
        .subscribe { this.initialLoadCommentsError.onNext(true) }
        .addToDisposable(disposables)

    this.paginationError
        .subscribe { this.displayPaginationError.onNext(true) }
        .addToDisposable(disposables)

    this.onShowGuideLinesLinkClicked
        .subscribe { showGuideLinesLink.onNext(Unit) }
        .addToDisposable(disposables)

    this.onCommentReplies
        .compose(Transformers.combineLatestPair(this.failedCommentCardToRefresh))
        .map {
            val mappedList = it.second.first.updateCommentFailedToPost(it.first.first, it.second.second)
            updateNewlyPostedCommentWithNewStatus(mappedList[it.second.second])
            Pair(mappedList, it.first.second)
        }
        .distinctUntilChanged()
        .subscribe { this.onCommentReplies.onNext(it) }
        .addToDisposable(disposables)

    this.successfullyPostedCommentCardToRefresh
        .map { it.first }
        .compose(Transformers.combineLatestPair(projectUpdateId))
        .compose(Transformers.combineLatestPair(project))
        .compose(Transformers.combineLatestPair(comment))
        .distinctUntilChanged()
        .subscribe {
            analyticEvents.trackRootCommentReplyCTA(
                it.first.second,
                it.first.first.first.id().toString(),
                it.first.first.first.body(),
                it.second.id().toString(),
                it.first.first.second
            )
        }
        .addToDisposable(disposables)

    this.onCommentReplies
        .compose(Transformers.combineLatestPair(this.successfullyPostedCommentCardToRefresh))
        .map {
            val mappedList = it.second.first.updateCommentAfterSuccessfulPost(
                it.first.first,
                it.second.second
            )
            updateNewlyPostedCommentWithNewStatus(mappedList[it.second.second])
            Pair(mappedList, it.first.second)
        }
        .distinctUntilChanged()
        .subscribe { this.onCommentReplies.onNext(it) }
        .addToDisposable(disposables)

    this.onCommentReplies
        .compose(Transformers.takePairWhenV2(this.showCanceledPledgeComment))
        .map { Pair(it.second.updateCanceledPledgeComment(it.first.first), it.first.second) }
        .distinctUntilChanged()
        .subscribe { this.onCommentReplies.onNext(it) }
        .addToDisposable(disposables)

    checkIfThereAnyPendingComments
        .withLatestFrom(this.onCommentReplies) { _, list -> list }
        .subscribe { pair ->
            this.hasPendingComments.onNext(
                pair.first.any {
                    it.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus ||
                        it.commentCardState == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus
                }
            )
        }
        .addToDisposable(disposables)

    this.backPressed
        .subscribe { this.closeThreadActivity.onNext(it) }
        .addToDisposable(disposables)

    intent
        .map { it.getBooleanExtra(IntentKey.REPLY_SCROLL_BOTTOM, false) }
        .filter { it }
        .compose(Transformers.takeWhenV2(this.onCommentReplies))
        .distinctUntilChanged()
        .subscribe { scrollToBottom.onNext(Unit) }
        .addToDisposable(disposables)
}

internal fun ThreadViewModel.ThreadViewModel.updateNewlyPostedCommentWithNewStatus(
    updatedComment: CommentCardData
) {
    this.newlyPostedRepliesList.indexOfFirst { item ->
        item.commentableId == updatedComment.commentableId
    }.also { index ->
        newlyPostedRepliesList[index] = updatedComment
    }
}

internal fun ThreadViewModel.ThreadViewModel.loadCommentListFromProjectOrUpdate(
    comment: Observable<Comment>,
    disposables: CompositeDisposable
) {
    val startOverWith = Observable.merge(
        comment,
        comment.compose(Transformers.takeWhenV2(refresh))
    )

    val apolloPaginate = ApolloPaginateV2.builder<CommentCardData, CommentEnvelope, Comment?>()
        .nextPage(nextPage)
        .distinctUntilChanged(true)
        .startOverWith(startOverWith)
        .envelopeToListOfData {
            hasPreviousElements.onNext(it.pageInfoEnvelope()?.hasPreviousPage ?: false)
            this.project?.let { project -> ThreadViewModelHelper.mapListToData(it, project, currentUser) }
        }
        .loadWithParams {
            ThreadViewModelHelper.loadWithProjectReplies(
                apolloClient,
                Observable.just(it.first),
                it.second,
                internalError
            )
        }
        .isReversed(true)
        .clearWhenStartingOver(true)
        .build()

    apolloPaginate
        .isFetching
        .share()
        .subscribe { this.isFetchingReplies.onNext(it) }
        .addToDisposable(disposables)

    apolloPaginate
        .paginatedData()
        ?.map { it.asReversed() }
        ?.compose(Transformers.combineLatestPair(this.hasPreviousElements))
        ?.distinctUntilChanged()
        ?.share()
        ?.map {
            if (this.newlyPostedRepliesList.isNotEmpty()) {
                Pair(this.newlyPostedRepliesList + it.first, it.second)
            } else {
                it
            }
        }
        ?.subscribe { this.onCommentReplies.onNext(it) }
        ?.addToDisposable(disposables)
    this.internalError
        .subscribe { this.initialError.onNext(it) }
        .addToDisposable(disposables)
    this.onCommentReplies
        .compose(Transformers.takePairWhenV2(internalError))
        .filter { it.first.first.isNotEmpty() }
        .subscribe { this.paginationError.onNext(it.second) }
        .addToDisposable(disposables)
}
