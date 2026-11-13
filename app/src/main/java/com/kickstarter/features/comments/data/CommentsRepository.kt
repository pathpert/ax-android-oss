package com.kickstarter.features.comments.data

import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import io.reactivex.Observable

/**
 * Domain repository isolating network interactions for project and update comment operations.
 *
 * Negative Constraints:
 * - Do NOT pass raw unvalidated cursors or empty comment IDs.
 */
interface CommentsRepository {
    fun getProjectComments(slug: String, cursor: String): Observable<CommentEnvelope>
    fun getProjectUpdateComments(updateId: String, cursor: String): Observable<CommentEnvelope>
    fun getComment(commentId: String): Observable<Comment>
    fun getProject(projectId: String): Observable<Project?>
    suspend fun getProjectCommentsSuspend(slug: String, cursor: String): Result<CommentEnvelope> = Result.failure(NotImplementedError())
    suspend fun getCommentSuspend(commentId: String): Result<Comment> = Result.failure(NotImplementedError())
    fun cleanDisposables()
}

class CommentsRepositoryImpl(
    private val apolloClient: ApolloClientTypeV2
) : CommentsRepository {

    override fun getProjectComments(slug: String, cursor: String): Observable<CommentEnvelope> {
        return apolloClient.getProjectComments(slug, cursor)
    }

    override fun getProjectUpdateComments(updateId: String, cursor: String): Observable<CommentEnvelope> {
        return apolloClient.getProjectUpdateComments(updateId, cursor)
    }

    override fun getComment(commentId: String): Observable<Comment> {
        return apolloClient.getComment(commentId)
    }

    override fun getProject(projectId: String): Observable<Project?> {
        return apolloClient.getProject(projectId)
            .map<Project?> { it }
            .compose(Transformers.neverErrorV2())
    }

    override suspend fun getProjectCommentsSuspend(slug: String, cursor: String): Result<CommentEnvelope> = runCatching {
        apolloClient.getProjectComments(slug, cursor).blockingFirst()
    }

    override suspend fun getCommentSuspend(commentId: String): Result<Comment> = runCatching {
        apolloClient.getComment(commentId).blockingFirst()
    }

    override fun cleanDisposables() {
        apolloClient.cleanDisposables()
    }
}
