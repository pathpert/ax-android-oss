package com.kickstarter.ui.activities.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.type.FlaggingKind

/**
 * Activity displaying and coordinating user interactions for rulesMap.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun rulesMap(): Map<Triple<String, String, Boolean>, List<Triple<String, String, String>>> {
    val projectCat = Triple(
        stringResource(id = R.string.This_project_breaks),
        stringResource(id = R.string.Projects_may_not_offer),
        true
    )
    val rulesListProject = listOf(
        Triple(
            stringResource(id = R.string.Prohibited_items),
            stringResource(id = R.string.Projects_may_not_offer),
            FlaggingKind.PROHIBITED_ITEMS.rawValue
        ),
        Triple(
            stringResource(id = R.string.Copying_reselling),
            stringResource(id = R.string.Projects_cannot_plagiarize),
            FlaggingKind.RESALE.rawValue
        ),
        Triple(
            stringResource(id = R.string.Prototype_misrepresentation),
            stringResource(id = R.string.Creators_must_be_transparent),
            FlaggingKind.PROTOTYPE_MISREPRESENTATION.rawValue
        ),
        Triple(
            stringResource(id = R.string.Suspicious_creator_behavior),
            stringResource(id = R.string.Project_creators_and_their),
            FlaggingKind.POST_FUNDING_ISSUES.rawValue
        ),
        Triple(
            stringResource(id = R.string.Not_raising_funds),
            stringResource(id = R.string.Projects_on),
            FlaggingKind.NOT_PROJECT_OTHER.rawValue
        )
    )

    val spamCat = Triple(
        stringResource(id = R.string.Report_spam),
        stringResource(id = R.string.Our),
        true
    )
    val rulesListSpam = listOf(
        Triple(
            stringResource(id = R.string.Spam),
            stringResource(id = R.string.Ex_using),
            FlaggingKind.GUIDELINES_SPAM.rawValue
        ),
        Triple(
            stringResource(id = R.string.Abuse),
            stringResource(id = R.string.Ex_posting),
            FlaggingKind.GUIDELINES_ABUSE.rawValue
        ),
    )

    val intellectualCat = Triple(
        stringResource(id = R.string.Intellectual_property_violation),
        stringResource(id = R.string.A_project_is_infringing),
        false
    )

    val rulesListIntellectual = listOf(
        Triple(
            stringResource(id = R.string.Intellectual_property_violation),
            stringResource(id = R.string.Kickstarter_takes_claims),
            FlaggingKind.NOT_PROJECT.rawValue
        ),
    )

    return mapOf(
        Pair(projectCat, rulesListProject),
        Pair(spamCat, rulesListSpam),
        Pair(intellectualCat, rulesListIntellectual)
    )
}
