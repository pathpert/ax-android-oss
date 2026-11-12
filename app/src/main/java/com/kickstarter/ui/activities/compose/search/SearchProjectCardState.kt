package com.kickstarter.ui.activities.compose.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.libs.utils.extensions.isLatePledgesActive
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Project

/**
 * Visual lifecycle state for project cards displayed in search results.
 */
enum class CardProjectState {
    LIVE,
    LATE_PLEDGES_ACTIVE,
    LAUNCHING_SOON,
    ENDED_SUCCESSFUL,
    ENDED_UNSUCCESSFUL
}

/**
 * Derives the [CardProjectState] from a given [Project] model.
 */
fun getCardProjectState(project: Project): CardProjectState {
    return if (project.isSuccessful && !project.isLatePledgesActive())
        CardProjectState.ENDED_SUCCESSFUL
    else if (project.isFailed)
        CardProjectState.ENDED_UNSUCCESSFUL
    else if (project.isLatePledgesActive())
        CardProjectState.LATE_PLEDGES_ACTIVE
    else if (project.prelaunchActivated().isTrue() && !project.isLive)
        CardProjectState.LAUNCHING_SOON
    else if (project.isLive)
        CardProjectState.LIVE
    else {
        CardProjectState.LIVE
    }
}

/**
 * Formats funding progress information string based on [CardProjectState].
 */
@Composable
fun getFundingInfoString(
    projectCardState: CardProjectState,
    environment: Environment?,
    project: Project
): String {
    return when (projectCardState) {
        CardProjectState.LIVE -> environment?.ksString()?.let {
            NumberUtils.format(
                project.deadlineCountdownValue(),
            ) + " " + project.deadlineCountdownDetail(
                LocalContext.current,
                it
            ) + " • " + project.percentageFunded()
                .toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)
        }.toString()

        CardProjectState.LATE_PLEDGES_ACTIVE -> stringResource(R.string.Late_pledges_active) + " • " + project.percentageFunded()
            .toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)

        CardProjectState.LAUNCHING_SOON -> stringResource(R.string.Launching_soon)
        CardProjectState.ENDED_SUCCESSFUL -> stringResource(R.string.Ended) + " • " + project.percentageFunded()
            .toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)

        CardProjectState.ENDED_UNSUCCESSFUL -> stringResource(R.string.Ended) + " • " + project.percentageFunded()
            .toInt() + "% " + stringResource(id = R.string.discovery_baseball_card_stats_funded)
    }
}

