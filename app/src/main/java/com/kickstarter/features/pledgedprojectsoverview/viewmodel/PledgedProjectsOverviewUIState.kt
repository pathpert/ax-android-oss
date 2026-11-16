package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import com.kickstarter.ax.errors.ProblemDetails

/**
 * UI state representation for Pledged Projects Overview screen.
 *
 * Negative constraints:
 * - Do not mutate state directly; emit through ViewModel StateFlow reducers.
 * - Do not pass raw unhandled exceptions; map errors to [ProblemDetails].
 *
 * @property isLoading Whether initial alerts or card data is actively fetching.
 * @property isErrored Flag indicating network or server failure.
 * @property totalAlerts Total count of urgent actionable alerts.
 * @property problem Structured RFC 9457 error details carrying suggestedAction and isRetriable flag.
 */
data class PledgedProjectsOverviewUIState(
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
    val totalAlerts: Int? = null,
    val problem: ProblemDetails? = null
)
