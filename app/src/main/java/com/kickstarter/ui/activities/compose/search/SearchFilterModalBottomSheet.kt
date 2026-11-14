package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.unit.dp
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterModalBottomSheet(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    activeBottomSheet: MutableState<FilterRowPillType?>,
    currentCategory: MutableState<Category?>,
    onDismissBottomSheet: (
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
    ) -> Unit,
    currentSort: MutableState<DiscoveryParams.Sort>,
    currentProjectState: MutableState<DiscoveryParams.State?>,
    categories: List<Category>,
    categoryPillText: MutableState<String>,
    projectStatusPillText: MutableState<String>,
    initialCategoryPillText: String,
    selectedFilterCounts: SnapshotStateMap<String, Int>,
    countApiIsReady: Boolean,
    pagerState: PagerState,
    currentPercentage: MutableState<DiscoveryParams.RaisedBuckets?>,
    currentLocation: MutableState<Location?>,
    currentAmountRaised: MutableState<DiscoveryParams.AmountBuckets?>,
    currentRecommended: MutableState<Boolean>,
    currentProjectsLoved: MutableState<Boolean>,
    currentSavedProjects: MutableState<Boolean>,
    currentFollowing: MutableState<Boolean>,
    currentGoal: MutableState<DiscoveryParams.GoalBuckets?>,
    currentOpenCallTag: MutableState<Tag?>,
    tags: List<Tag>,
    isOpenCallsEnabled: Boolean,
    shouldShowPhase: Boolean
) {
    if (isSheetOpen.value.isTrue()) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                isSheetOpen.value = false
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = colors.backgroundSurfacePrimary,
            contentWindowInsets = { WindowInsets(0) },
            dragHandle = null,
        ) {
            SheetContent(
                activeBottomSheet = activeBottomSheet,
                currentCategory = currentCategory,
                onDismissBottomSheet = onDismissBottomSheet,
                currentSort = currentSort,
                currentProjectState = currentProjectState,
                categories = categories,
                categoryPillText = categoryPillText,
                projectStatusPillText = projectStatusPillText,
                initialCategoryPillText = initialCategoryPillText,
                selectedFilterCounts = selectedFilterCounts,
                countApiIsReady = countApiIsReady,
                sheetState = sheetState,
                pagerState = pagerState,
                currentPercentage = currentPercentage,
                currentLocation = currentLocation,
                currentAmountRaised = currentAmountRaised,
                currentRecommended = currentRecommended,
                currentProjectsLoved = currentProjectsLoved,
                currentSavedProjects = currentSavedProjects,
                currentFollowing = currentFollowing,
                currentGoal = currentGoal,
                currentOpenCallTag = currentOpenCallTag,
                tags = tags,
                isOpenCallsEnabled = isOpenCallsEnabled,
                shouldShowPhase = shouldShowPhase,
                isSheetOpen = isSheetOpen
            )
        }
    }

    LaunchedEffect(isSheetOpen.value) {
        if (isSheetOpen.value.isTrue()) {
            sheetState.show()
        } else sheetState.hide()
    }
}
