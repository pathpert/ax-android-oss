package com.kickstarter.features.comments.viewmodel

import android.content.Intent
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.features.comments.data.CommentsFeatureInputs
import com.kickstarter.features.comments.data.CommentsFeatureOutputs
import com.kickstarter.features.comments.data.CommentsIntentProcessor
import com.kickstarter.features.comments.data.CommentsListMapper
import com.kickstarter.features.comments.data.CommentsPaginationHelper
import com.kickstarter.features.comments.data.CommentsRepository
import com.kickstarter.features.comments.data.CommentsRepositoryImpl
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.models.extensions.cardStatus
import com.kickstarter.models.extensions.updateCanceledPledgeComment
import com.kickstarter.models.extensions.updateCommentAfterSuccessfulPost
import com.kickstarter.models.extensions.updateCommentFailedToPost
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class CommentsFeatureViewModel(
    val environment: Environment,
    private val intent: Intent? = null,
    customRepository: CommentsRepository? = null
) : CommentsOutputsHolder() {

    private val repository: CommentsRepository = customRepository ?: CommentsRepositoryImpl(
        requireNotNull(environment.apolloClientV2())
    )
    private val currentUserStream = requireNotNull(environment.currentUserV2())
    private val analyticEvents = requireNotNull(environment.analytics())

    open val inputs: CommentsFeatureInputs = this
    open val outputs: CommentsFeatureOutputs = this

    private val _commentsStateFlow = MutableStateFlow<List<CommentCardData>>(emptyList())
    val commentsStateFlow: StateFlow<List<CommentCardData>> = _commentsStateFlow.asStateFlow()

    private var project: Project? = null
    private var openedThreadActivityFromDeepLink = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val newlyPostedCommentsList = mutableListOf<CommentCardData>()

    private val disposables = CompositeDisposable()
    private var currentUser: User? = null

    init {
        this.currentUserStream.loggedInUser()
            .map { currentUser = it; it }
            .subscribe { currentUserAvatar.onNext(it.avatar().small()) }
            .addToDisposable(disposables)

        val projectOrUpdate = CommentsIntentProcessor.parseProjectOrUpdate(intent)

        val initialProject = projectOrUpdate
            .flatMap {
                it?.either<Observable<Project?>>(
                    { value: Project? -> Observable.just(value) },
                    { u: Update? -> repository.getProject(u?.projectId().toString()) }
                )
            }
            .map { requireNotNull(it) }
            .doOnNext { this.project = it }
            .share()

        initialProject.compose(combineLatestPair(currentUserStream.observable())).subscribe {
            val composerStatus = CommentsListMapper.getCommentComposerStatus(it.first, it.second)
            showCommentComposer.onNext(composerStatus != CommentComposerStatus.GONE)
            commentComposerStatus.onNext(composerStatus)
        }.addToDisposable(disposables)

        val projectOrUpdateComment = projectOrUpdate.compose(combineLatestPair(initialProject))
            .map { Pair(it.second, it.first?.right()) }

        loadCommentList(projectOrUpdateComment)

        val deepLinkCommentableId = CommentsIntentProcessor.extractDeepLinkId(intent)

        projectOrUpdateComment.distinctUntilChanged()
            .compose(combineLatestPair(CommentsIntentProcessor.hasDeepLinkComment(intent)))
            .filter { !it.second }.map { it.first }
            .subscribe { trackRootCommentPageViewEvent(it) }
            .addToDisposable(disposables)

        projectOrUpdateComment.compose(Transformers.takeWhenV2(onResumeActivity)).subscribe {
            if (openedThreadActivityFromDeepLink) {
                trackRootCommentPageViewEvent(it)
                openedThreadActivityFromDeepLink = false
            }
        }.addToDisposable(disposables)

        deepLinkCommentableId.compose(takePairWhenV2(projectOrUpdateComment))
            .switchMap { repository.getComment(it.first) }
            .compose(Transformers.neverErrorV2())
            .compose(combineLatestPair(deepLinkCommentableId))
            .compose(combineLatestPair(commentableId))
            .compose(combineLatestPair(currentUserStream.observable()))
            .filter { it.first.second.isNotEmpty() && it.second.isPresent() }
            .map {
                CommentCardData.builder()
                    .comment(it.first.first.first)
                    .project(this.project ?: Project.builder().build())
                    .commentCardState(it.first.first.first.cardStatus(it.second.getValue()))
                    .commentableId(it.first.second)
                    .build()
            }
            .withLatestFrom(projectOrUpdateComment) { commentData, projOrUp -> Pair(commentData, projOrUp) }
            .subscribe {
                this.startThreadActivityFromDeepLink.onNext(Pair(it.first, it.second.second?.id()?.toString()))
                openedThreadActivityFromDeepLink = true
            }.addToDisposable(disposables)

        this.insertNewCommentToList
            .withLatestFrom(this.currentUserStream.loggedInUser()) { comment, user -> Pair(comment, user) }
            .map { Pair(it.first, CommentsListMapper.buildComment(it.second, it.first.first, it.first.second)) }
            .withLatestFrom(initialProject) { commentData, proj -> Pair(commentData, proj) }
            .compose(combineLatestPair(commentableId))
            .map {
                Pair(
                    it.first.first,
                    CommentCardData.builder()
                        .comment(it.first.first.second)
                        .project(it.first.second)
                        .commentableId(it.second)
                        .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
                        .build()
                )
            }
            .distinctUntilChanged { prev, curr -> prev.first.first.second == curr.first.first.second }
            .doOnNext { this.scrollToTop.onNext(true) }
            .withLatestFrom(this.commentsList) { it, list ->
                newlyPostedCommentsList.add(0, it.second)
                list.toMutableList().apply { add(0, it.second) }.toList()
            }.subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        this.commentToRefresh.map { it.first }.distinctUntilChanged()
            .withLatestFrom(projectOrUpdateComment) { commentData, proj -> Pair(commentData, proj) }
            .subscribe {
                this.analyticEvents.trackCommentCTA(it.second.first, it.first.id().toString(), it.first.body(), it.second.second?.id()?.toString())
            }.addToDisposable(disposables)

        this.onShowGuideLinesLinkClicked.subscribe { showGuideLinesLink.onNext(Unit) }.addToDisposable(disposables)
        this.commentsList.map { it.size }.distinctUntilChanged().subscribe { this.setEmptyState.onNext(it == 0) }.addToDisposable(disposables)
        this.initialError.subscribe { this.displayInitialError.onNext(true) }.addToDisposable(disposables)
        this.paginationError.subscribe { this.displayPaginationError.onNext(true) }.addToDisposable(disposables)

        this.commentsList.compose(takePairWhenV2(checkIfThereAnyPendingComments)).subscribe { pair ->
            this.hasPendingComments.onNext(
                Pair(
                    pair.first.any {
                        it.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus ||
                            it.commentCardState == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus
                    },
                    pair.second
                )
            )
        }.addToDisposable(disposables)

        this.backPressed.subscribe { this.closeCommentsPage.onNext(it) }.addToDisposable(disposables)

        this.commentsList.withLatestFrom(projectOrUpdateComment) { commentData, projOrUp -> Pair(commentData, projOrUp) }
            .compose(takePairWhenV2(onReplyClicked)).subscribe { pair ->
                val cardData = pair.first.first.first { it.comment?.id() == pair.second.first.id() }
                val threadData = Pair(cardData, pair.second.second)
                this.startThreadActivity.onNext(Pair(threadData, pair.first.second.second?.id()?.toString()))
            }.addToDisposable(disposables)

        this.commentsList.compose(takePairWhenV2(this.commentToRefresh))
            .map {
                val mapped = it.second.first.updateCommentAfterSuccessfulPost(it.first, it.second.second)
                updateNewlyPosted(mapped[it.second.second])
                mapped
            }
            .distinctUntilChanged().doOnNext { this.scrollToTop.onNext(false) }
            .subscribe { this.commentsList.onNext(it) }.addToDisposable(disposables)

        this.commentsList.filter { it.isNotEmpty() }.subscribe {
            this.outputCommentList.onNext(it)
            _commentsStateFlow.value = it
        }.addToDisposable(disposables)

        this.commentsList.compose(takePairWhenV2(this.failedCommentCardToRefresh))
            .map {
                val mapped = it.second.first.updateCommentFailedToPost(it.first, it.second.second)
                updateNewlyPosted(mapped[it.second.second])
                mapped
            }
            .distinctUntilChanged().subscribe { this.commentsList.onNext(it) }.addToDisposable(disposables)

        this.commentsList.compose(takePairWhenV2(this.showCanceledPledgeComment))
            .map { it.second.updateCanceledPledgeComment(it.first) }
            .distinctUntilChanged().subscribe { this.commentsList.onNext(it) }.addToDisposable(disposables)
    }

    private fun updateNewlyPosted(updatedComment: CommentCardData) {
        val index = this.newlyPostedCommentsList.indexOfFirst { it.commentableId == updatedComment.commentableId }
        if (index >= 0) newlyPostedCommentsList[index] = updatedComment
    }

    private fun trackRootCommentPageViewEvent(it: Pair<Project, Update?>) {
        if (it.second?.id() != null) {
            this.analyticEvents.trackRootCommentPageViewed(it.first, it.second?.id()?.toString())
        } else {
            this.analyticEvents.trackRootCommentPageViewed(it.first)
        }
    }

    private fun loadCommentList(projectOrUpdate: Observable<Pair<Project, Update?>>) {
        val startOverWith = Observable.merge(projectOrUpdate, projectOrUpdate.compose(Transformers.takeWhenV2(refresh)))
        val apolloPaginate = CommentsPaginationHelper.buildPaginate(
            nextPage = nextPage,
            startOverWith = startOverWith,
            projectProvider = { this.project ?: Project.builder().build() },
            currentUserProvider = { this.currentUser },
            loadWithComments = { stream, cursor -> loadComments(stream, cursor) }
        )

        apolloPaginate.isFetching.share().subscribe { this.isFetchingComments.onNext(it) }.addToDisposable(disposables)
        apolloPaginate.paginatedData()?.share()
            ?.map { if (this.newlyPostedCommentsList.isNotEmpty()) this.newlyPostedCommentsList + it else it }
            ?.subscribe { this.commentsList.onNext(it) }?.addToDisposable(disposables)

        this.internalError.map { Pair(it, commentsList.value) }
            .filter { it.second.isNullOrEmpty() }.subscribe { this.initialError.onNext(it.first) }.addToDisposable(disposables)
        commentsList.compose(takePairWhenV2(this.internalError))
            .filter { it.first.isNotEmpty() }.subscribe { this.paginationError.onNext(it.second) }.addToDisposable(disposables)

        this.refresh.doOnNext { this.isRefreshing.onNext(true) }
            .subscribe { this.newlyPostedCommentsList.clear() }.addToDisposable(disposables)
        this.internalError.compose(combineLatestPair(isRefreshing)).compose(combineLatestPair(commentsList))
            .filter { it.second.isNullOrEmpty() }.subscribe { this.isRefreshing.onNext(false) }.addToDisposable(disposables)
    }

    private fun loadComments(stream: Observable<Pair<Project, Update?>>, cursor: String): Observable<CommentEnvelope> {
        return stream.switchMap {
            if (it.second?.id() != null) {
                repository.getProjectUpdateComments(it.second?.id().toString(), cursor)
            } else {
                repository.getProjectComments(it.first?.slug() ?: "", cursor)
            }
        }.doOnNext {
            it.commentableId?.let { comId -> commentableId.onNext(comId) } ?: commentableId.onNext("")
            this.displayPaginationError.onNext(false)
            this.displayInitialError.onNext(false)
        }.doOnError {
            val problem = ProblemDetailsMapper.fromThrowable(it, "load comments")
            this.internalError.onNext(problem)
        }.onErrorResumeNext(Observable.empty())
    }

    override fun onCleared() {
        this.repository.cleanDisposables()
        this.disposables.clear()
        super.onCleared()
    }

    /**
     * Factory producing [CommentsFeatureViewModel] instances with injected environment and repository.
     *
     * Negative constraints:
     * - Do not instantiate without a valid [Environment].
     */
    class Factory(
        private val environment: Environment,
        private val intent: Intent? = null,
        private val repository: CommentsRepository? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CommentsFeatureViewModel(environment, intent, repository) as T
        }
    }
}
