package com.kickstarter.features.pledgedprojectsoverview.data

/**
 * Query parameter container for requesting paginated pledged projects overview data.
 *
 * Supports cursor-based forward/backward pagination and filtering by tier types.
 *
 * ### Negative Constraints:
 * - Do not pass negative values to [first] or [last].
 * - Do not provide both forward ([first]/[after]) and backward ([last]/[before]) pagination parameters simultaneously.
 */
data class PledgedProjectsOverviewQueryData(
    val first: Int? = null,
    val after: String? = null,
    val last: Int? = null,
    val before: String? = null,
    val tierTypes: List<PledgeTierType>? = null
)
