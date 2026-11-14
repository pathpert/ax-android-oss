package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.kickstarter.libs.Environment
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

@Composable
fun rememberSearchFilterCounts(): SnapshotStateMap<String, Int> {
    return remember {
        mutableStateMapOf(
            FilterRowPillType.SORT.name to 0,
            FilterRowPillType.CATEGORY.name to 0,
            FilterRowPillType.FILTER.name to 0,
            FilterRowPillType.PROJECT_STATUS.name to 0,
            FilterRowPillType.PERCENTAGE_RAISED.name to 0,
            FilterRowPillType.LOCATION.name to 0,
            FilterRowPillType.AMOUNT_RAISED.name to 0,
            FilterRowPillType.RECOMMENDED.name to 0,
            FilterRowPillType.PROJECTS_LOVED.name to 0,
            FilterRowPillType.SAVED.name to 0,
            FilterRowPillType.FOLLOWING.name to 0,
            FilterRowPillType.GOAL.name to 0,
            FilterRowPillType.OPEN_CALLS.name to 0
        )
    }
}

@Composable
fun SearchEmptyStateView(
    environment: Environment?,
    currentSearchTerm: String,
    selectedFilterCounts: SnapshotStateMap<String, Int>,
    currentCategory: MutableState<Category?>,
    currentProjectState: MutableState<DiscoveryParams.State?>,
    currentPercentage: MutableState<DiscoveryParams.RaisedBuckets?>,
    currentLocation: MutableState<Location?>,
    currentAmountRaised: MutableState<DiscoveryParams.AmountBuckets?>,
    currentRecommended: MutableState<Boolean>,
    currentStaffPicked: MutableState<Boolean>,
    currentStarred: MutableState<Boolean>,
    currentSocial: MutableState<Boolean>,
    currentGoal: MutableState<DiscoveryParams.GoalBuckets?>,
    currentOpenCallTag: MutableState<Tag?>,
    currentSort: MutableState<DiscoveryParams.Sort>,
    onApplySearchWithParams: (
        Category?,
        DiscoveryParams.Sort?,
        DiscoveryParams.State?,
        DiscoveryParams.RaisedBuckets?,
        Location?,
        DiscoveryParams.AmountBuckets?,
        Boolean,
        Boolean,
        Boolean,
        Boolean,
        DiscoveryParams.GoalBuckets?,
        Tag?
    ) -> Unit
) {
    var numbersActive = 0
    selectedFilterCounts.entries.forEach { entry ->
        numbersActive += entry.value
    }

    SearchEmptyView(
        modifier = Modifier
            .testTag(SearchScreenTestTag.EMPTY_VIEW.name)
            .background(colors.backgroundSurfacePrimary),
        environment = environment,
        currentSearchTerm = currentSearchTerm,
        activeFilters = numbersActive > 0,
        onClick = {
            selectedFilterCounts.keys.forEach { key ->
                selectedFilterCounts[key] = 0
            }

            currentCategory.value = null
            currentProjectState.value = null
            currentPercentage.value = null
            currentLocation.value = null
            currentAmountRaised.value = null
            currentRecommended.value = false
            currentStaffPicked.value = false
            currentStarred.value = false
            currentSocial.value = false
            currentGoal.value = null
            currentOpenCallTag.value = null
            onApplySearchWithParams(
                currentCategory.value,
                currentSort.value,
                currentProjectState.value,
                currentPercentage.value,
                currentLocation.value,
                currentAmountRaised.value,
                currentRecommended.value,
                currentStaffPicked.value,
                currentStarred.value,
                currentSocial.value,
                currentGoal.value,
                currentOpenCallTag.value
            )
        }
    )
}
