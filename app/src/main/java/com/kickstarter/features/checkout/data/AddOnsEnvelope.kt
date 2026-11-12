package com.kickstarter.features.checkout.data

import com.kickstarter.models.Reward
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

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
