package com.kickstarter.services.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxFlowable
import com.apollographql.apollo3.rx2.rxSingle
import com.kickstarter.CreateCommentMutation
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.services.interceptors.RateLimitBackoffInterceptor
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.GetCommentQuery
import com.kickstarter.GetProjectCommentsQuery
import com.kickstarter.GetProjectUpdateCommentsQuery
import com.kickstarter.GetRepliesForCommentQuery
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.models.Comment
import com.kickstarter.services.APOLLO_PAGE_SIZE
import com.kickstarter.services.APOLLO_REPLIES_PAGE_SIZE
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.services.transformers.commentTransformer
import com.kickstarter.services.transformers.encodeRelayId
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Apollo GraphQL domain client for comments, update comments, comment replies, and comment creation.
 */
class ApolloCommentClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable,
    private val idempotencyGuard: IdempotencyGuard = IdempotencyGuard(),
    val rateLimitInterceptor: RateLimitBackoffInterceptor = RateLimitBackoffInterceptor()
) {
    fun getComment(commentableId: String): Observable<Comment> {
        return Observable.defer {
            val ps = PublishSubject.create<Comment>()
            val query = GetCommentQuery(commentableId = commentableId)
            service.query(query)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            val comment = mapGetCommentQueryResponseToComment(responseData)
                            ps.onNext(comment)
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun mapGetCommentQueryResponseToComment(responseData: GetCommentQuery.Data): Comment {
        val commentFragment = responseData.commentable?.onComment?.comment
        return commentTransformer(commentFragment)
    }

    fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()
            val query = GetProjectUpdateCommentsQuery(
                cursor = if (cursor.isPresent()) Optional.present(cursor) else Optional.absent(),
                id = updateId,
                limit = limit
            )
            service.query(query)
                .rxFlowable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            data.post?.freeformPost?.comments?.let { graphComments ->
                                val comments = graphComments.edges?.map { edge ->
                                    commentTransformer(edge?.node?.comment)
                                        .toBuilder()
                                        .cursor(edge?.cursor)
                                        .build()
                                }
                                val envelope = CommentEnvelope.builder()
                                    .comments(comments)
                                    .commentableId(data.post?.id ?: "")
                                    .totalCount(data.post?.freeformPost?.comments?.totalCount ?: 0)
                                    .pageInfoEnvelope(
                                        createPageInfoObject(
                                            data.post?.freeformPost?.comments?.pageInfo?.pageInfo
                                        )
                                    )
                                    .build()
                                ps.onNext(envelope)
                            }
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun getProjectComments(
        slug: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()
            val query = GetProjectCommentsQuery(
                cursor = if (cursor.isPresent()) Optional.present(cursor) else Optional.absent(),
                slug = slug,
                limit = limit
            )
            service.query(query)
                .rxFlowable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            data.project?.comments?.let { graphComments ->
                                val comments = graphComments.edges?.map { edge ->
                                    commentTransformer(
                                        edge?.node?.comment
                                    ).toBuilder()
                                        .cursor(edge?.cursor)
                                        .build()
                                }
                                val envelope = CommentEnvelope.builder()
                                    .commentableId(data.project?.id)
                                    .comments(comments)
                                    .totalCount(data.project?.comments?.totalCount ?: 0)
                                    .pageInfoEnvelope(
                                        createPageInfoObject(
                                            data.project?.comments?.pageInfo?.pageInfo
                                        )
                                    )
                                    .build()
                                ps.onNext(envelope)
                            }
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun getRepliesForComment(
        comment: Comment,
        cursor: String? = null,
        pageSize: Int = APOLLO_REPLIES_PAGE_SIZE
    ): Observable<CommentEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<CommentEnvelope>()
            val query = GetRepliesForCommentQuery(
                commentableId = encodeRelayId(comment),
                cursor = if (cursor.isNullOrBlank()) Optional.absent() else Optional.present(cursor),
                pageSize = Optional.present(pageSize)
            )
            service.query(query)
                .rxFlowable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            ps.onNext(createCommentEnvelope(responseData))
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    private fun createCommentEnvelope(responseData: GetRepliesForCommentQuery.Data): CommentEnvelope {
        val replies = responseData.commentable?.onComment?.replies
        val listOfComments = replies?.nodes?.map { commentFragment ->
            commentTransformer(commentFragment?.comment)
        } ?: emptyList()
        val totalCount = replies?.totalCount ?: 0
        val pageInfo = createPageInfoObject(replies?.pageInfo?.pageInfo)

        return CommentEnvelope.builder()
            .comments(listOfComments)
            .pageInfoEnvelope(pageInfo)
            .totalCount(totalCount)
            .build()
    }

    fun createComment(comment: PostCommentData): Observable<Comment> {
        return Observable.defer {
            val ps = PublishSubject.create<Comment>()
            val key = comment.clientMutationId?.let { (IdempotencyKey.create(it) as? DomainResult.Success)?.value } ?: IdempotencyKey.generate()
            val mutation = CreateCommentMutation(
                parentId = if (comment.parent?.id().isNotNull()) Optional.present(comment.parent?.let { encodeRelayId(it) }) else Optional.absent(),
                commentableId = comment.commentableId,
                clientMutationId = Optional.present(key.value),
                body = comment.body
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    } else {
                        ps.onNext(
                            commentTransformer(
                                response.data?.createComment?.comment?.comment
                            )
                        )
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }
}
