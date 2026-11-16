package com.kickstarter.ax.errors

import androidx.annotation.StringRes
import com.kickstarter.R

/**
 * Presentation-boundary mapper selecting localized user-facing error copy from structured
 * [ProblemDetails], so UI fallback strings are driven by the problem type instead of hardcoded
 * generic text.
 *
 * Selecting copy through this mapper keeps the localized human copy while exposing the
 * machine-readable [ProblemDetails.suggestedAction] alongside it for callers, accessibility
 * services, and automated agents.
 *
 * Negative constraints:
 * - Do not bypass this mapper to render raw `Throwable.message` or hardcoded generic strings when
 *   a [ProblemDetails] is available.
 * - Do not drop [ProblemDetails.suggestedAction] when composing display messages.
 */
object ProblemDetailsCopyMapper {

    /**
     * Localized message resource for the problem category.
     *
     * Network and timeout failures surface a connectivity-specific message; every other category
     * intentionally falls back to the generic localized copy while [displayMessage] appends the
     * problem-specific remediation instruction.
     */
    @StringRes
    fun messageRes(problem: ProblemDetails): Int = when (problem.type) {
        ProblemDetails.TYPE_NETWORK, ProblemDetails.TYPE_TIMEOUT ->
            R.string.social_error_no_internet_connection
        else -> R.string.general_error_something_wrong
    }

    /**
     * Human-readable display message combining the problem detail with its remediation
     * instruction, e.g. `"Unable to reach Kickstarter servers to load comments. Check network
     * connectivity and retry when online."`.
     *
     * Negative constraints:
     * - Do not use [ProblemDetails.detail] alone for UI display; the suggested action is the
     *   actionable half of the message.
     */
    fun displayMessage(problem: ProblemDetails): String {
        val detail = problem.detail.trim().trimEnd('.')
        val suggestedAction = problem.suggestedAction.trim()
        return "$detail. $suggestedAction"
    }

    /**
     * Display message for a raw [Throwable] captured at a UI boundary, mapped through
     * [ProblemDetailsMapper] so legacy callers gain structured remediation copy, or `null` when
     * no throwable is available and the caller must fall back to [messageRes] copy.
     *
     * @param throwable Captured failure, or `null` when only a signal channel fired.
     * @param operation Short verb-noun name of the attempted operation (e.g., `"present payment sheet"`).
     */
    fun displayMessageFor(throwable: Throwable?, operation: String): String? =
        throwable?.let { displayMessage(ProblemDetailsMapper.fromThrowable(it, operation)) }
}
