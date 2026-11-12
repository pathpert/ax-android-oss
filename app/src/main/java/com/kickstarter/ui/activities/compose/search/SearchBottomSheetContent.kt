package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.toDiscoveryParamsList
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.type.ProjectSort
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

/**
 * Top-level Sheet content composable switching between Sort and Filter Pagers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetContent(
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
    sheetState: SheetState,
    pagerState: PagerState,
    currentPercentage: MutableState<DiscoveryParams.RaisedBuckets?>,
    currentAmountRaised: MutableState<DiscoveryParams.AmountBuckets?>,
    currentGoal: MutableState<DiscoveryParams.GoalBuckets?>,
    currentLocation: MutableState<Location?> = mutableStateOf(null),
    currentRecommended: MutableState<Boolean> = mutableStateOf(false),
    currentProjectsLoved: MutableState<Boolean> = mutableStateOf(false),
    currentSavedProjects: MutableState<Boolean> = mutableStateOf(false),
    currentFollowing: MutableState<Boolean> = mutableStateOf(false),
    currentOpenCallTag: MutableState<Tag?> = mutableStateOf(null),
    tags: List<Tag> = listOf(),
    isOpenCallsEnabled: Boolean = false,
    shouldShowPhase: Boolean = true,
    isSheetOpen: MutableState<Boolean>
) {
    val liveString = stringResource(R.string.Project_status_live)
    val successfulString = stringResource(R.string.Project_status_successful)
    val upcomingString = stringResource(R.string.Project_status_upcoming)
    val latePledgeString = stringResource(R.string.Project_status_late_pledge)
    val defaultString = stringResource(R.string.Project_status)

    when (activeBottomSheet.value) {
        FilterRowPillType.CATEGORY,
        FilterRowPillType.PROJECT_STATUS,
        FilterRowPillType.PERCENTAGE_RAISED,
        FilterRowPillType.LOCATION,
        FilterRowPillType.AMOUNT_RAISED,
        FilterRowPillType.RECOMMENDED,
        FilterRowPillType.PROJECTS_LOVED,
        FilterRowPillType.SAVED,
        FilterRowPillType.FOLLOWING,
        FilterRowPillType.GOAL,
        FilterRowPillType.OPEN_CALLS,
        FilterRowPillType.FILTER -> {
            FilterPagerSheet(
                sheetState = sheetState,
                pagerState = pagerState,
                selectedProjectStatus = currentProjectState.value,
                currentCategory = currentCategory.value,
                currentPercentage = currentPercentage.value,
                currentLocation = currentLocation.value,
                currentAmountRaised = currentAmountRaised.value,
                currentRecommended = currentRecommended,
                currentProjectsLoved = currentProjectsLoved,
                currentSavedProjects = currentSavedProjects,
                currentFollowing = currentFollowing,
                currentGoal = currentGoal.value,
                currentOpenCallTag = currentOpenCallTag.value,
                tags = tags,
                isOpenCallsEnabled = isOpenCallsEnabled,
                categories = categories,
                onDismiss = {
                    isSheetOpen.value = false
                },
                onApply = { project, category, percentageBucket, location, amountRaisedBucket, recommended, projectsLoved, savedProjects, following, goalBucket, openCallTag ->
                    currentProjectState.value = project
                    currentCategory.value = category
                    currentPercentage.value = percentageBucket
                    currentLocation.value = location
                    currentAmountRaised.value = amountRaisedBucket
                    currentRecommended.value = recommended
                    currentProjectsLoved.value = projectsLoved
                    currentSavedProjects.value = savedProjects
                    currentFollowing.value = following
                    currentGoal.value = goalBucket
                    currentOpenCallTag.value = openCallTag

                    categoryPillText.value =
                        if (category != null) category.name() else initialCategoryPillText

                    projectStatusPillText.value = when (project) {
                        DiscoveryParams.State.LIVE -> liveString
                        DiscoveryParams.State.SUCCESSFUL -> successfulString
                        DiscoveryParams.State.SUBMITTED -> upcomingString
                        DiscoveryParams.State.LATE_PLEDGES -> latePledgeString
                        else -> defaultString
                    }

                    onDismissBottomSheet(
                        category,
                        currentSort.value,
                        project,
                        percentageBucket,
                        location,
                        amountRaisedBucket,
                        recommended,
                        projectsLoved,
                        savedProjects,
                        following,
                        goalBucket,
                        openCallTag
                    )
                },
                updateSelectedCounts = { projectStatusCount, categoryCount, raisedBucket, location, amountBucketCount, recommended, projectsLoved, savedProjects, social, goalBucketCount, openCallsCount ->
                    if (!countApiIsReady) {
                        selectedFilterCounts[FilterRowPillType.PROJECT_STATUS.name] =
                            projectStatusCount ?: 0
                        selectedFilterCounts[FilterRowPillType.CATEGORY.name] = categoryCount ?: 0
                        selectedFilterCounts[FilterRowPillType.PERCENTAGE_RAISED.name] =
                            raisedBucket ?: 0
                        selectedFilterCounts[FilterRowPillType.LOCATION.name] = location ?: 0
                        selectedFilterCounts[FilterRowPillType.AMOUNT_RAISED.name] =
                            amountBucketCount ?: 0
                        selectedFilterCounts[FilterRowPillType.GOAL.name] = goalBucketCount ?: 0
                        selectedFilterCounts[FilterRowPillType.OPEN_CALLS.name] = openCallsCount ?: 0
                        selectedFilterCounts[FilterRowPillType.FILTER.name] =
                            (projectStatusCount ?: 0) + (categoryCount ?: 0) + (raisedBucket ?: 0) + (location ?: 0) + (amountBucketCount ?: 0) + (recommended ?: 0) + (projectsLoved ?: 0) + (savedProjects ?: 0) + (social ?: 0) + (goalBucketCount ?: 0) + (openCallsCount ?: 0)
                    }
                    openCallsCount?.let {
                        selectedFilterCounts[FilterRowPillType.OPEN_CALLS.name] = it
                    }
                },
                shouldShowPhase = shouldShowPhase
            )
        }

        FilterRowPillType.SORT -> {
            SortSelectionBottomSheet(
                currentSelection = currentSort.value,
                sorts = ProjectSort.knownValues().toDiscoveryParamsList(),
                onDismiss = { sort ->
                    currentSort.value = sort
                    isSheetOpen.value = false
                    onDismissBottomSheet(
                        currentCategory.value,
                        sort,
                        currentProjectState.value,
                        currentPercentage.value,
                        currentLocation.value,
                        currentAmountRaised.value,
                        currentRecommended.value,
                        currentProjectsLoved.value,
                        currentSavedProjects.value,
                        currentFollowing.value,
                        currentGoal.value,
                        currentOpenCallTag.value
                    )
                }
            )
        }

        else -> {}
    }
}
