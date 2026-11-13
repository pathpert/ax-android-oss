package com.kickstarter.features.search.data

import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.models.Project

/**
 * Immutable UI state for search and filter operations.
 *
 * Encapsulates loading flags, popular projects for empty queries, and paginated search results.
 *
 * ### Negative Constraints:
 * - Do not pass mutable lists into [popularProjectsList] or [searchList].
 * - Do not mutate state directly; emit through ViewModel StateFlow reducers.
 */
data class SearchUIState(
    val isLoading: Boolean = false,
    val popularProjectsList: List<Project> = emptyList(),
    val searchList: List<Project> = emptyList(),
    val hasMore: Boolean = true,
    val problem: ProblemDetails? = null
)
