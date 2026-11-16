package com.kickstarter.ax.errors

import com.apollographql.apollo3.exception.ApolloHttpException
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.kickstarter.ax.types.RateLimitBackoff
import com.kickstarter.services.ApiException
import com.kickstarter.services.KSApolloClientV2.KSApolloClientV2Exception
import com.kickstarter.services.ResponseException
import okhttp3.Headers
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Deterministic boundary mapper converting legacy exceptions, HTTP responses, and GraphQL errors
 * into RFC 9457-aligned [ProblemDetails].
 *
 * Negative constraints:
 * - Do not swallow [Throwable] instances or return null on mapping failure.
 * - Do not expose raw stack traces or generic messages like "Something went wrong".
 */
object ProblemDetailsMapper {

    /**
     * Maps any [Throwable] captured at a repository or ViewModel boundary into [ProblemDetails].
     *
     * @param throwable Captured failure from Apollo, Retrofit, or domain validation.
     * @param operation Short verb-noun name of the attempted operation (e.g., `"cancel pledge"`).
     */
    fun fromThrowable(
        throwable: Throwable,
        operation: String = "complete operation"
    ): ProblemDetails {
        return when (throwable) {
            is ProblemException -> throwable.problem
            is ApiException -> fromApiException(throwable, operation)
            is ResponseException -> fromHttpStatus(
                status = throwable.response().code(),
                rawDetail = throwable.response().message().takeIf { it.isNotBlank() },
                backoff = extractOkHttpBackoff(throwable.response().headers()),
                operation = operation
            )
            is ApolloHttpException -> fromHttpStatus(
                status = throwable.statusCode,
                rawDetail = throwable.message?.takeIf { it.isNotBlank() },
                backoff = RateLimitBackoff.fromHeaders(
                    throwable.headers.map { it.name to it.value }
                ),
                operation = operation
            )
            is KSApolloClientV2Exception.TooManyRequests -> fromHttpStatus(
                status = 429,
                rawDetail = throwable.message,
                backoff = RateLimitBackoff(),
                operation = operation
            )
            is KSApolloClientV2Exception.Timeout,
            is SocketTimeoutException -> ProblemDetails(
                type = ProblemDetails.TYPE_TIMEOUT,
                title = "Request Timed Out",
                status = 504,
                detail = "Timed out while trying to $operation.",
                suggestedAction = "Wait briefly and retry the operation once connectivity stabilizes.",
                isRetriable = true
            )
            is KSApolloClientV2Exception.InvalidProject -> ProblemDetails(
                type = ProblemDetails.TYPE_VALIDATION,
                title = "Invalid Project Identifier",
                status = 400,
                detail = throwable.message?.takeIf { it.isNotBlank() }
                    ?: "Project identifier is missing or invalid while attempting to $operation.",
                suggestedAction = "Reload the project details and pass a valid non-blank project ID.",
                isRetriable = false,
                invalidParams = listOf(
                    ProblemDetails.InvalidParam(
                        name = "projectId",
                        reason = "Must reference a valid existing project."
                    )
                )
            )
            is KSApolloClientV2Exception.ApiError -> fromDomainMessage(
                rawMessage = throwable.message,
                operation = operation
            )
            is ApolloNetworkException,
            is UnknownHostException,
            is IOException -> ProblemDetails(
                type = ProblemDetails.TYPE_NETWORK,
                title = "Network Unavailable",
                status = null,
                detail = "Unable to reach Kickstarter servers to $operation.",
                suggestedAction = "Check network connectivity and retry when online.",
                isRetriable = true
            )
            else -> findProblemInCauseChain(throwable)?.problem
                ?: fromDomainMessage(
                    rawMessage = throwable.message,
                    operation = operation
                )
        }
    }

    /**
     * Walks the cause chain of a wrapped [Throwable] (e.g., an Apollo JSON parsing or RxJava
     * wrapper) to recover a carried [ProblemException] so structured details survive the boundary.
     */
    private fun findProblemInCauseChain(throwable: Throwable): ProblemException? {
        var current: Throwable? = throwable
        var depth = 0
        while (current != null && depth < MAX_CAUSE_DEPTH) {
            if (current is ProblemException) return current
            current = current.cause
            depth++
        }
        return null
    }

    private const val MAX_CAUSE_DEPTH = 16

    /**
     * Maps a domain/GraphQL error message (such as a mutation validation error) into [ProblemDetails].
     */
    fun fromDomainMessage(
        rawMessage: String?,
        operation: String = "complete operation",
        status: Int? = 422
    ): ProblemDetails {
        val cleaned = rawMessage?.trim()?.takeIf { it.isNotEmpty() }
        return if (cleaned != null) {
            ProblemDetails(
                type = ProblemDetails.TYPE_VALIDATION,
                title = "Operation Rejected",
                status = status,
                detail = "Failed to $operation: $cleaned",
                suggestedAction = "Review the rejected parameters or account state and try again if corrected.",
                isRetriable = false
            )
        } else {
            ProblemDetails(
                type = ProblemDetails.TYPE_UNEXPECTED,
                title = "Unexpected Operation Failure",
                status = status,
                detail = "Could not $operation because the server returned an unspecified error.",
                suggestedAction = "Refresh the current screen state before attempting to $operation again.",
                isRetriable = true
            )
        }
    }

    private fun fromApiException(exception: ApiException, operation: String): ProblemDetails {
        val envelope = exception.errorEnvelope()
        val status = envelope.httpCode().takeIf { it > 0 } ?: exception.response().code()
        val message = envelope.errorMessage().takeIf { it.isNotBlank() }
        val backoff = extractOkHttpBackoff(exception.response().headers())

        if (envelope.isUnauthorizedError || status == 401) {
            return ProblemDetails(
                type = ProblemDetails.TYPE_UNAUTHORIZED,
                title = "Authentication Required",
                status = 401,
                detail = message ?: "Your session is no longer authorized to $operation.",
                suggestedAction = "Re-authenticate the user session before retrying.",
                isRetriable = false
            )
        }

        return fromHttpStatus(
            status = status,
            rawDetail = message,
            backoff = backoff,
            operation = operation
        )
    }

    /**
     * Maps an HTTP status code and optional [RateLimitBackoff] into [ProblemDetails].
     */
    fun fromHttpStatus(
        status: Int,
        rawDetail: String? = null,
        backoff: RateLimitBackoff = RateLimitBackoff(),
        operation: String = "complete operation"
    ): ProblemDetails {
        val detailSuffix = rawDetail?.trim()?.takeIf { it.isNotEmpty() }?.let { ": $it" } ?: "."
        return when (status) {
            400, 422 -> ProblemDetails(
                type = ProblemDetails.TYPE_VALIDATION,
                title = "Invalid Request Parameters",
                status = status,
                detail = "Request to $operation failed validation$detailSuffix",
                suggestedAction = "Fix the invalid input parameters and submit a new request.",
                isRetriable = false
            )
            401 -> ProblemDetails(
                type = ProblemDetails.TYPE_UNAUTHORIZED,
                title = "Authentication Required",
                status = 401,
                detail = "Authorization is required to $operation$detailSuffix",
                suggestedAction = "Sign in or refresh the access token before retrying.",
                isRetriable = false
            )
            403 -> ProblemDetails(
                type = ProblemDetails.TYPE_FORBIDDEN,
                title = "Operation Forbidden",
                status = 403,
                detail = "Current account is not permitted to $operation$detailSuffix",
                suggestedAction = "Verify account permissions or ownership before retrying.",
                isRetriable = false
            )
            404 -> ProblemDetails(
                type = ProblemDetails.TYPE_NOT_FOUND,
                title = "Resource Not Found",
                status = 404,
                detail = "Target resource required to $operation was not found$detailSuffix",
                suggestedAction = "Refresh the screen to verify the resource still exists.",
                isRetriable = false
            )
            409 -> ProblemDetails(
                type = ProblemDetails.TYPE_CONFLICT,
                title = "State Conflict",
                status = 409,
                detail = "Cannot $operation due to conflicting resource state$detailSuffix",
                suggestedAction = "Refresh the latest state and avoid re-submitting a stale mutation.",
                isRetriable = false
            )
            429 -> {
                val waitHint = backoff.retryAfterSeconds?.let { " after $it seconds" } ?: " after a backoff period"
                ProblemDetails(
                    type = ProblemDetails.TYPE_RATE_LIMITED,
                    title = "Rate Limit Exceeded",
                    status = 429,
                    detail = "Too many requests while attempting to $operation$detailSuffix",
                    suggestedAction = "Respect Retry-After and retry$waitHint.",
                    isRetriable = true,
                    retryAfterSeconds = backoff.retryAfterSeconds
                )
            }
            in 500..599 -> ProblemDetails(
                type = ProblemDetails.TYPE_SERVER,
                title = "Server Error",
                status = status,
                detail = "Upstream server error ($status) while attempting to $operation$detailSuffix",
                suggestedAction = backoff.retryAfterSeconds?.let {
                    "Wait $it seconds per Retry-After header before retrying."
                } ?: "Retry with exponential backoff; do not spin in a tight loop.",
                isRetriable = true,
                retryAfterSeconds = backoff.retryAfterSeconds
            )
            else -> ProblemDetails(
                type = ProblemDetails.TYPE_UNEXPECTED,
                title = "HTTP Error ($status)",
                status = status,
                detail = "Failed to $operation with HTTP status $status$detailSuffix",
                suggestedAction = "Inspect the failure details and refresh state before retrying.",
                isRetriable = false
            )
        }
    }

    private fun extractOkHttpBackoff(headers: Headers): RateLimitBackoff {
        val pairs = headers.names().mapNotNull { name ->
            headers[name]?.let { value -> name to value }
        }
        return RateLimitBackoff.fromHeaders(pairs)
    }
}
