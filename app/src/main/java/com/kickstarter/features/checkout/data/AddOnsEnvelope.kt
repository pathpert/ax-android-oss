package com.kickstarter.features.checkout.data

import com.kickstarter.models.Reward
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

/**
 * Legacy envelope holding add-ons list.
 *
 * Negative constraints:
 * - Do not pass into new feature code; use [AddOnsPageContract].
 *
 * @property addOnsList List of rewards acting as add-ons.
 * @property pageInfo Pagination envelope metadata.
 */
/**
 * Legacy envelope wrapper for add-on rewards.
 *
 * Negative constraints:
 * - Do not use in new features; consume [AddOnsPageContract] instead.
 *
 * @property addOnsList Legacy list of reward models.
 * @property pageInfo Pagination envelope for next cursor.
 */
@Deprecated(
    message = "Leaks legacy Reward and PageInfoEnvelope across checkout boundary. " +
        "Use AddOnsPageContract mapped via AddOnBoundaryMapper.fromLegacy().",
    replaceWith = ReplaceWith(
        "AddOnsPageContract",
        "com.kickstarter.features.checkout.data.AddOnsPageContract"
    )
)
data class AddOnsEnvelope(
    val addOnsList: List<Reward> = emptyList(),
    val pageInfo: PageInfoEnvelope? = null
)
