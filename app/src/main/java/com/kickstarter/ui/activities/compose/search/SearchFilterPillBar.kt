package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.activities.compose.search.PillBarTestTags.pillTag
import com.kickstarter.ui.compose.designsystem.KSIconPillButton
import com.kickstarter.ui.compose.designsystem.KSPillButton
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

/**
 * Activity displaying and coordinating user interactions for PillBarTestTags.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
object PillBarTestTags {
    fun pillTag(state: FilterRowPillType) = "pill_${state.name}"
}

@Composable
fun PillBar(
    countApiIsReady: Boolean = false,
    categoryPillText: String = stringResource(R.string.Category),
    projectStatusText: String = stringResource(R.string.Project_status),
    percentageRaisedText: String = stringResource(R.string.Percentage_raised),
    locationText: String = stringResource(R.string.Location),
    amountRaisedText: String = stringResource(R.string.Amount_raised),
    recommendedText: String = stringResource(R.string.Recommended),
    projectsLovedText: String = stringResource(R.string.Projects_We_Love),
    savedProjectsText: String = stringResource(R.string.Saved_projects),
    followingText: String = stringResource(R.string.Following),
    goalText: String = stringResource(R.string.Goal),
    openCallsText: String = stringResource(R.string.fpo_Open_calls),
    isOpenCallsEnabled: Boolean = false,
    isFromDeepLink: Boolean = false,
    selectedFilterCounts: Map<String, Int>,
    onPillPressed: (FilterRowPillType) -> Unit,
    shouldShowPhase: Boolean = true,
    onPillPressedShowOnlyToggles: (FilterRowPillType, Boolean) -> Unit = { a, b -> },
    recommendedStatus: MutableState<Boolean> = remember { mutableStateOf(false) },
    projectsLovedStatus: MutableState<Boolean> = remember { mutableStateOf(false) },
    savedProjects: MutableState<Boolean> = remember { mutableStateOf(false) },
    following: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    val viewModel = LocalFilterMenuViewModel.current
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(
                start = dimensions.paddingMediumLarge,
                end = dimensions.paddingMediumLarge,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.listItemSpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KSIconPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.SORT)),
            type = FilterRowPillType.SORT,
            isSelected = selectedFilterCounts.getOrDefault(FilterRowPillType.SORT.name, 0) > 0,
            onClick = { onPillPressed(FilterRowPillType.SORT) }
        )
        val activeFilters: Int =
            selectedFilterCounts.getOrDefault(FilterRowPillType.PROJECT_STATUS.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.CATEGORY.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.PERCENTAGE_RAISED.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.LOCATION.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.AMOUNT_RAISED.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.RECOMMENDED.name, if (recommendedStatus.value.isTrue()) 1 else 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.PROJECTS_LOVED.name, if (projectsLovedStatus.value.isTrue()) 1 else 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.SAVED.name, if (savedProjects.value.isTrue()) 1 else 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.FOLLOWING.name, if (following.value.isTrue()) 1 else 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.GOAL.name, 0) +
                selectedFilterCounts.getOrDefault(FilterRowPillType.OPEN_CALLS.name, 0)

        KSIconPillButton(
            modifier = Modifier.testTag(pillTag(FilterRowPillType.FILTER)),
            type = FilterRowPillType.FILTER,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.FILTER.name,
                0
            ) > 0,
            onClick = { onPillPressed(FilterRowPillType.FILTER) },
            count = activeFilters
        )

        // - Deep-link entry surfaces the Open Calls pill at the front, ahead of Category.
        if (isOpenCallsEnabled && isFromDeepLink) {
            OpenCallsPill(
                text = openCallsText,
                isSelected = selectedFilterCounts.getOrDefault(FilterRowPillType.OPEN_CALLS.name, 0) > 0,
                onClick = { onPillPressed(FilterRowPillType.OPEN_CALLS) }
            )
        }

        KSPillButton(
            shouldShowTrailingIcon = true,
            modifier = Modifier.testTag(pillTag(FilterRowPillType.CATEGORY)),
            countApiIsReady = countApiIsReady,
            text = categoryPillText,
            isSelected = selectedFilterCounts.getOrDefault(FilterRowPillType.CATEGORY.name, 0) > 0,
            count = selectedFilterCounts.getOrDefault(FilterRowPillType.CATEGORY.name, 0),
            onClick = { onPillPressed(FilterRowPillType.CATEGORY) }
        )
        KSPillButton(
            shouldShowTrailingIcon = true,
            modifier = Modifier.testTag(pillTag(FilterRowPillType.PROJECT_STATUS)),
            countApiIsReady = countApiIsReady,
            text = projectStatusText,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.PROJECT_STATUS.name,
                0
            ) > 0,
            count = selectedFilterCounts.getOrDefault(FilterRowPillType.PROJECT_STATUS.name, 0),
            onClick = { onPillPressed(FilterRowPillType.PROJECT_STATUS) }
        )
        KSPillButton(
            shouldShowTrailingIcon = true,
            modifier = Modifier.testTag(pillTag(FilterRowPillType.LOCATION)),
            countApiIsReady = countApiIsReady,
            text = locationText,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.LOCATION.name,
                0
            ) > 0,
            count = selectedFilterCounts.getOrDefault(
                FilterRowPillType.LOCATION.name,
                0
            ),
            onClick = { onPillPressed(FilterRowPillType.LOCATION) }
        )
        if (shouldShowPhase && loggedInUser) {
            KSPillButton(
                modifier = Modifier.testTag(pillTag(FilterRowPillType.RECOMMENDED)),
                text = recommendedText,
                isSelected = recommendedStatus.value,
                count = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.RECOMMENDED.name,
                    0
                ),
                onClick = {
                    recommendedStatus.value = !recommendedStatus.value
                    onPillPressedShowOnlyToggles(
                        FilterRowPillType.RECOMMENDED,
                        recommendedStatus.value
                    )
                }
            )
        }
        KSPillButton(
            shouldShowTrailingIcon = true,
            modifier = Modifier.testTag(pillTag(FilterRowPillType.PERCENTAGE_RAISED)),
            countApiIsReady = countApiIsReady,
            text = percentageRaisedText,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.PERCENTAGE_RAISED.name,
                0
            ) > 0,
            count = selectedFilterCounts.getOrDefault(
                FilterRowPillType.PERCENTAGE_RAISED.name,
                0
            ),
            onClick = { onPillPressed(FilterRowPillType.PERCENTAGE_RAISED) }
        )
        KSPillButton(
            shouldShowTrailingIcon = true,
            modifier = Modifier.testTag(pillTag(FilterRowPillType.AMOUNT_RAISED)),
            countApiIsReady = countApiIsReady,
            text = amountRaisedText,
            isSelected = selectedFilterCounts.getOrDefault(
                FilterRowPillType.AMOUNT_RAISED.name,
                0
            ) > 0,
            count = selectedFilterCounts.getOrDefault(
                FilterRowPillType.AMOUNT_RAISED.name,
                0
            ),
            onClick = { onPillPressed(FilterRowPillType.AMOUNT_RAISED) }
        )
        if (shouldShowPhase) {
            KSPillButton(
                shouldShowLeadingIcon = true,
                modifier = Modifier.testTag(pillTag(FilterRowPillType.PROJECTS_LOVED)),
                text = projectsLovedText,
                isSelected = projectsLovedStatus.value,
                count = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.PROJECTS_LOVED.name,
                    0
                ),
                onClick = {
                    projectsLovedStatus.value = !projectsLovedStatus.value
                    onPillPressedShowOnlyToggles(
                        FilterRowPillType.PROJECTS_LOVED,
                        projectsLovedStatus.value
                    )
                }
            )
            KSPillButton(
                shouldShowTrailingIcon = true,
                modifier = Modifier.testTag(pillTag(FilterRowPillType.GOAL)),
                countApiIsReady = countApiIsReady,
                text = goalText,
                isSelected = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.GOAL.name,
                    0
                ) > 0,
                count = selectedFilterCounts.getOrDefault(
                    FilterRowPillType.GOAL.name,
                    0
                ),
                onClick = { onPillPressed(FilterRowPillType.GOAL) }
            )
            if (loggedInUser) {
                KSPillButton(
                    modifier = Modifier.testTag(pillTag(FilterRowPillType.SAVED)),
                    text = savedProjectsText,
                    isSelected = savedProjects.value,
                    count = selectedFilterCounts.getOrDefault(
                        FilterRowPillType.SAVED.name,
                        0
                    ),
                    onClick = {
                        savedProjects.value = !savedProjects.value
                        onPillPressedShowOnlyToggles(
                            FilterRowPillType.SAVED,
                            savedProjects.value,
                        )
                    }
                )
                KSPillButton(
                    modifier = Modifier.testTag(pillTag(FilterRowPillType.FOLLOWING)),
                    text = followingText,
                    isSelected = following.value,
                    count = selectedFilterCounts.getOrDefault(
                        FilterRowPillType.FOLLOWING.name,
                        0
                    ),
                    onClick = {
                        following.value = !following.value
                        onPillPressedShowOnlyToggles(
                            FilterRowPillType.FOLLOWING,
                            following.value
                        )
                    }
                )
            }
        }
        // - Manual entry surfaces the Open Calls pill last, after Following.
        if (isOpenCallsEnabled && !isFromDeepLink) {
            OpenCallsPill(
                text = openCallsText,
                isSelected = selectedFilterCounts.getOrDefault(FilterRowPillType.OPEN_CALLS.name, 0) > 0,
                onClick = { onPillPressed(FilterRowPillType.OPEN_CALLS) }
            )
        }
    }
}
@Composable
private fun OpenCallsPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    KSPillButton(
        shouldShowTrailingIcon = true,
        modifier = Modifier.testTag(pillTag(FilterRowPillType.OPEN_CALLS)),
        text = text,
        isSelected = isSelected,
        onClick = onClick
    )
}
