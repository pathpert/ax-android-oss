package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Applies user filter selections and updates active facet counts.
 */
fun applyUserSelection(
    onApply: (
        DiscoveryParams.State?,
        Category?,
        DiscoveryParams.RaisedBuckets?,
        Location?,
        DiscoveryParams.AmountBuckets?,
        recommended: Boolean,
        projectsLoved: Boolean,
        savedProjects: Boolean,
        social: Boolean,
        goalBucket: DiscoveryParams.GoalBuckets?,
        openCallTag: Tag?
    ) -> Unit,
    projectState: DiscoveryParams.State?,
    category: Category?,
    location: Location?,
    percentageRaisedBucket: DiscoveryParams.RaisedBuckets?,
    amountRaisedBucket: DiscoveryParams.AmountBuckets?,
    recommended: Boolean,
    projectsLoved: Boolean,
    savedProjects: Boolean,
    social: Boolean,
    goalBucket: DiscoveryParams.GoalBuckets?,
    openCallTag: Tag?,
    updateSelectedCounts: (
        projectStatusCount: Int?,
        categoryCount: Int?,
        raisedBucketCount: Int?,
        location: Int?,
        amountBucketCount: Int?,
        recommended: Int?,
        projectsLoved: Int?,
        savedProjects: Int?,
        social: Int?,
        goalBucketCount: Int?,
        openCallsCount: Int?
    ) -> Unit,
    onDismiss: () -> Unit,
    shouldDismiss: Boolean
) {
    onApply(
        projectState,
        category,
        percentageRaisedBucket,
        location,
        amountRaisedBucket,
        recommended,
        projectsLoved,
        savedProjects,
        social,
        goalBucket,
        openCallTag
    )

    updateSelectedCounts(
        if (projectState != null) 1 else 0,
        if (category != null) 1 else 0,
        if (percentageRaisedBucket != null) 1 else 0,
        if (location != null) 1 else 0,
        if (amountRaisedBucket != null) 1 else 0,
        if (recommended) 1 else 0,
        if (projectsLoved) 1 else 0,
        if (savedProjects) 1 else 0,
        if (social) 1 else 0,
        if (goalBucket != null) 1 else 0,
        if (openCallTag != null) 1 else 0
    )
    if (shouldDismiss) {
        onDismiss.invoke()
    }
}

/**
 * Handles clicks on filter pills to animate the bottom sheet pager to the corresponding tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun onPillPressedOpensBottomSheet(
    activeBottomSheet: MutableState<FilterRowPillType?>,
    coroutineScope: CoroutineScope,
    sheetState: SheetState,
    pagerState: PagerState,
    isSheetOpen: MutableState<Boolean>
): (FilterRowPillType) -> Unit =
    { filterRowPillType ->
        activeBottomSheet.value = filterRowPillType
        when (filterRowPillType) {
            FilterRowPillType.SORT -> isSheetOpen.value = true
            FilterRowPillType.FILTER,
            FilterRowPillType.PROJECT_STATUS -> {
                coroutineScope.launch {
                    isSheetOpen.value = true
                    pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal)
                }
            }

            FilterRowPillType.CATEGORY -> {
                coroutineScope.launch {
                    isSheetOpen.value = true
                    pagerState.animateScrollToPage(FilterPages.CATEGORIES.ordinal)
                }
            }

            FilterRowPillType.LOCATION -> {
                coroutineScope.launch {
                    isSheetOpen.value = true
                    pagerState.animateScrollToPage(FilterPages.LOCATION.ordinal)
                }
            }

            FilterRowPillType.PERCENTAGE_RAISED -> {
                coroutineScope.launch {
                    isSheetOpen.value = true
                    pagerState.animateScrollToPage(FilterPages.PERCENTAGE_RAISED.ordinal)
                }
            }

            FilterRowPillType.AMOUNT_RAISED -> {
                coroutineScope.launch {
                    isSheetOpen.value = true
                    pagerState.animateScrollToPage(FilterPages.AMOUNT_RAISED.ordinal)
                }
            }

            FilterRowPillType.GOAL -> {
                coroutineScope.launch {
                    isSheetOpen.value = true
                    pagerState.animateScrollToPage(FilterPages.GOAL.ordinal)
                }
            }
            FilterRowPillType.OPEN_CALLS -> {
                coroutineScope.launch {
                    isSheetOpen.value = true
                    pagerState.animateScrollToPage(FilterPages.OPEN_CALLS.ordinal)
                }
            }
            FilterRowPillType.SAVED,
            FilterRowPillType.PROJECTS_LOVED,
            FilterRowPillType.FOLLOWING,
            FilterRowPillType.RECOMMENDED -> {
                // Handled directly via toggle callbacks
            }
        }
    }

