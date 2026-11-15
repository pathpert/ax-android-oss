package com.kickstarter.features.search.data

import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

/**
 * Envelope holding paginated search results and page metadata.
 *
 * Negative constraints:
 * - Do not mutate [projectList] after creation.
 * - Do not pass negative values for [totalCount].
 *
 * @property projectList List of search result projects for the current query page.
 * @property pageInfo Pagination cursor and next page indicator metadata.
 * @property totalCount Non-negative total number of projects matching the search criteria.
 */
data class SearchEnvelope(
    val projectList: List<Project> = emptyList(),
    val pageInfo: PageInfoEnvelope? = null,
    val totalCount: Int = 0
)
