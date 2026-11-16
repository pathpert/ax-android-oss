package com.kickstarter.features.comments.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
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
    suspend fun getProjectCommentsSuspend(slug: String, cursor: String): DomainResult<CommentEnvelope> =
        DomainResult.Failure(
            ProblemDetails.forMissingConfiguration(
                "CommentsRepository",
                "Not implemented",
                "Implement suspend method in concrete repository."
            )
        )
    suspend fun getCommentSuspend(commentId: String): DomainResult<Comment> =
        DomainResult.Failure(
            ProblemDetails.forMissingConfiguration(
                "CommentsRepository",
                "Not implemented",
                "Implement suspend method in concrete repository."
            )
        )
    fun cleanDisposables()
}

/**
 * Apollo GraphQL implementation of [CommentsRepository].
 *
 * Negative constraints:
 * - Do not instantiate directly in UI classes; inject via ViewModel Factory.
 */
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

    override suspend fun getProjectCommentsSuspend(slug: String, cursor: String): DomainResult<CommentEnvelope> = try {
        DomainResult.Success(apolloClient.getProjectComments(slug, cursor).blockingFirst())
    } catch (e: Exception) {
        val problem = ProblemDetailsMapper.fromThrowable(e, "fetch project comments")
        DomainResult.Failure(problem)
    }

    override suspend fun getCommentSuspend(commentId: String): DomainResult<Comment> = try {
        DomainResult.Success(apolloClient.getComment(commentId).blockingFirst())
    } catch (e: Exception) {
        val problem = ProblemDetailsMapper.fromThrowable(e, "fetch comment")
        DomainResult.Failure(problem)
    }

    override fun cleanDisposables() {
        apolloClient.cleanDisposables()
    }
}
