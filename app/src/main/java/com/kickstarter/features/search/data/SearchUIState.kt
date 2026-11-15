package com.kickstarter.features.search.data

import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.models.Project

/**
 * Immutable UI state for search and filter operations.
 *
 * Encapsulates loading flags, popular projects for empty queries, and paginated search results.
 *
 * Negative constraints:
 * - Do not pass mutable lists into [popularProjectsList] or [searchList].
 * - Do not mutate state directly; emit through ViewModel StateFlow reducers.
 *
 * @property isLoading Whether a search query or pagination request is in progress.
 * @property popularProjectsList Curated default projects displayed when query is empty.
 * @property searchList List of projects matching active query and filter criteria.
 * @property hasMore Indicates if additional pages can be fetched.
 * @property problem Structured actionable error details carrying suggestedAction and isRetriable flag, or null if operation succeeded.
 */
data class SearchUIState(
    val isLoading: Boolean = false,
    val popularProjectsList: List<Project> = emptyList(),
    val searchList: List<Project> = emptyList(),
    val hasMore: Boolean = true,
    val problem: ProblemDetails? = null
)
