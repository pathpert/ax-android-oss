package com.kickstarter.ax.errors

/**
 * Structured, actionable domain or network error modeled on RFC 9457 Problem Details.
 *
 * Designed for both Jetpack Compose UI states and automated callers to determine exact failure
 * semantics, retry safety, and field-level remediation instructions without inspecting raw
 * exceptions or stack traces.
 *
 * Negative constraints:
 * - Do not populate [detail] with raw stack traces or generic messages such as
 *   "Something went wrong".
 * - Do not use `isRetriable = true` for deterministic 4xx validation or authorization errors.
 *
 * @property type Stable URN identifying the problem category (e.g., `"urn:kickstarter:error:validation"`).
 * @property title Short, stable human- and agent-readable summary of the failure class.
 * @property status HTTP status code when the failure originated from an HTTP/GraphQL transport, or `null`.
 * @property detail Specific contextual explanation of what failed for this operation.
 * @property suggestedAction Exact remediation instruction telling the caller or UI how to recover.
 * @property isRetriable Whether retrying the exact same operation is safe and potentially productive.
 * @property retryAfterSeconds Optional server-requested cooldown in whole seconds before retrying.
 * @property invalidParams Field-level validation violations when input constraints failed.
 */
data class ProblemDetails(
    val type: String,
    val title: String,
    val status: Int? = null,
    val detail: String,
    val suggestedAction: String,
    val isRetriable: Boolean,
    val retryAfterSeconds: Long? = null,
    val invalidParams: List<InvalidParam> = emptyList()
) {
    init {
        require(type.isNotBlank()) { "ProblemDetails.type must not be blank." }
        require(title.isNotBlank()) { "ProblemDetails.title must not be blank." }
        require(detail.isNotBlank()) { "ProblemDetails.detail must not be blank." }
        require(suggestedAction.isNotBlank()) { "ProblemDetails.suggestedAction must not be blank." }
        if (retryAfterSeconds != null) {
            require(retryAfterSeconds >= 0L) { "ProblemDetails.retryAfterSeconds must be >= 0." }
        }
    }

    /**
     * Field-level validation error entry.
     *
     * Negative constraints:
     * - Do not omit [reason] or pass raw regex patterns without human-readable explanation.
     *
     * @property name Exact parameter or input field name that violated a constraint.
     * @property reason Specific constraint violated and expected input format.
     */
    data class InvalidParam(
        val name: String,
        val reason: String
    ) {
        init {
            require(name.isNotBlank()) { "InvalidParam.name must not be blank." }
            require(reason.isNotBlank()) { "InvalidParam.reason must not be blank." }
        }
    }

    companion object {
        const val TYPE_VALIDATION = "urn:kickstarter:error:validation"
        const val TYPE_UNAUTHORIZED = "urn:kickstarter:error:unauthorized"
        const val TYPE_FORBIDDEN = "urn:kickstarter:error:forbidden"
        const val TYPE_NOT_FOUND = "urn:kickstarter:error:not-found"
        const val TYPE_CONFLICT = "urn:kickstarter:error:conflict"
        const val TYPE_IN_FLIGHT = "urn:kickstarter:error:mutation-in-flight"
        const val TYPE_RATE_LIMITED = "urn:kickstarter:error:rate-limited"
        const val TYPE_NETWORK = "urn:kickstarter:error:network"
        const val TYPE_TIMEOUT = "urn:kickstarter:error:timeout"
        const val TYPE_SERVER = "urn:kickstarter:error:server"
        const val TYPE_UNEXPECTED = "urn:kickstarter:error:unexpected"
    }
}

/**
 * Typed outcome wrapper replacing nullable returns, `Boolean` flags, or `Observable<Any>`.
 *
 * Negative constraints:
 * - Do not swallow failures by returning `null` or `DomainResult.Success(Unit)` when an error occurred.
 */
sealed interface DomainResult<out T> {
    /**
     * Successful domain operation outcome carrying [value].
     *
     * Negative constraints:
     * - Do not wrap null or unvalidated error models in [Success].
     *
     * @property value Successful result payload.
     */
    data class Success<T>(val value: T) : DomainResult<T>

    /**
     * Failed domain operation outcome carrying structured [problem].
     *
     * Negative constraints:
     * - Do not pass uncontextualized errors without suggested action.
     *
     * @property problem Structured RFC 9457 actionable error details.
     */
    data class Failure(val problem: ProblemDetails) : DomainResult<Nothing>
}

/**
 * Exception carrier used when bridging [ProblemDetails] through legacy RxJava or throwing APIs.
 *
 * Negative constraints:
 * - Do not swallow the underlying problem details or strip remediation instructions.
 *
 * @property problem Structured RFC 9457 error details.
 */
class ProblemException(val problem: ProblemDetails, cause: Throwable? = null) :
    RuntimeException(problem.detail, cause)
