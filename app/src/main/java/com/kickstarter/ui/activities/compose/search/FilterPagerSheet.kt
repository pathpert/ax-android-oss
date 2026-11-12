package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import kotlinx.coroutines.launch

/**
 * Filter pages enum identifying tabs within the search filter pager.
 */
enum class FilterPages {
    MAIN_FILTER,
    CATEGORIES,
    PERCENTAGE_RAISED,
    LOCATION,
    AMOUNT_RAISED,
    GOAL,
    OPEN_CALLS
}

/**
 * Main horizontal pager container for category and facet filter sheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPagerSheet(
    selectedProjectStatus: DiscoveryParams.State?,
    currentCategory: Category?,
    categories: List<Category>,
    currentPercentage: DiscoveryParams.RaisedBuckets? = null,
    currentLocation: Location? = null,
    currentAmountRaised: DiscoveryParams.AmountBuckets? = null,
    currentGoal: DiscoveryParams.GoalBuckets? = null,
    currentRecommended: MutableState<Boolean> = remember { mutableStateOf(false) },
    currentProjectsLoved: MutableState<Boolean> = remember { mutableStateOf(false) },
    currentSavedProjects: MutableState<Boolean> = remember { mutableStateOf(false) },
    currentFollowing: MutableState<Boolean> = remember { mutableStateOf(false) },
    currentOpenCallTag: Tag? = null,
    tags: List<Tag> = listOf(),
    isOpenCallsEnabled: Boolean = false,
    onDismiss: () -> Unit,
    onApply: (
        DiscoveryParams.State?,
        Category?,
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
    updateSelectedCounts: (
        projectStatusCount: Int?,
        categoryCount: Int?,
        raisedBucketCount: Int?,
        location: Int?,
        amountBucket: Int?,
        recommended: Int?,
        projectsLoved: Int?,
        savedProjects: Int?,
        social: Int?,
        goalBucket: Int?,
        openCallsCount: Int?
    ) -> Unit,
    pagerState: PagerState,
    sheetState: SheetState,
    shouldShowPhase: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val category = remember { mutableStateOf(currentCategory) }
    val projectState = remember { mutableStateOf(selectedProjectStatus) }
    val percentageBucket = remember { mutableStateOf(currentPercentage) }
    val location = remember { mutableStateOf(currentLocation) }
    val amountRaised = remember { mutableStateOf(currentAmountRaised) }
    val goal = remember { mutableStateOf(currentGoal) }
    val openCallTag = remember { mutableStateOf(currentOpenCallTag) }

    LaunchedEffect(sheetState.currentValue) {
        coroutineScope.launch {
            if (currentCategory != category.value) {
                category.value = currentCategory
            }

            if (selectedProjectStatus != projectState.value) {
                projectState.value = selectedProjectStatus
            }

            if (currentPercentage != percentageBucket.value) {
                percentageBucket.value = currentPercentage
            }

            if (currentLocation != location.value) {
                location.value = currentLocation
            }

            if (currentAmountRaised != amountRaised.value) {
                amountRaised.value = currentAmountRaised
            }

            if (currentGoal != goal.value) {
                goal.value = currentGoal
            }

            if (currentOpenCallTag != openCallTag.value) {
                openCallTag.value = currentOpenCallTag
            }
        }
    }

    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = dimensions.bottomSheetMinHeight,
                max = dimensions.bottomSheetMaxHeight
            ),
        state = pagerState
    ) { page ->
        when (page) {
            FilterPages.MAIN_FILTER.ordinal -> FilterMenuSheet(
                selectedLocation = currentLocation,
                selectedPercentage = currentPercentage,
                selectedProjectStatus = projectState.value,
                selectedAmount = currentAmountRaised,
                selectedCategory = currentCategory,
                selectedRecommended = currentRecommended,
                selectedProjectsLoved = currentProjectsLoved,
                selectedSaved = currentSavedProjects,
                selectedSocial = currentFollowing,
                selectedGoal = currentGoal,
                selectedOpenCallTag = openCallTag.value,
                onDismiss = {
                    onDismiss.invoke()
                },
                onApply = { selectedProjectState, selectedRecommended, selectedProjectsLoved, selectedSaved, selectedSocial, applyAndDismiss ->
                    projectState.value = selectedProjectState
                    currentRecommended.value = selectedRecommended
                    currentProjectsLoved.value = selectedProjectsLoved
                    currentSavedProjects.value = selectedSaved
                    currentFollowing.value = selectedSocial

                    if (applyAndDismiss != null) {
                        if (applyAndDismiss.isFalse()) {
                            category.value = null
                            percentageBucket.value = null
                            location.value = null
                            amountRaised.value = null
                            goal.value = null
                            openCallTag.value = null
                        }
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            goalBucket = goal.value,
                            openCallTag = openCallTag.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss,
                        )
                    }
                },
                onNavigate = { filterType ->
                    if (filterType == FilterType.CATEGORIES) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.CATEGORIES.ordinal)
                        }
                    }

                    if (filterType == FilterType.PERCENTAGE_RAISED) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.PERCENTAGE_RAISED.ordinal)
                        }
                    }

                    if (filterType == FilterType.LOCATION) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.LOCATION.ordinal)
                        }
                    }

                    if (filterType == FilterType.AMOUNT_RAISED) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.AMOUNT_RAISED.ordinal)
                        }
                    }
                    if (filterType == FilterType.GOAL) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.GOAL.ordinal)
                        }
                    }
                    if (filterType == FilterType.OPEN_CALLS) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(FilterPages.OPEN_CALLS.ordinal)
                        }
                    }
                },
                availableFilters = FilterType.values().asList()
                    .filter { shouldShowPhase || it != FilterType.OTHERS }
                    .filter { isOpenCallsEnabled || it != FilterType.OPEN_CALLS }
            )

            FilterPages.CATEGORIES.ordinal -> CategorySelectionSheet(
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                currentCategory = category.value,
                onDismiss = onDismiss,
                categories = categories,
                onApply = { selectedCategory, applyAndDismiss ->
                    category.value = selectedCategory
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            openCallTag = openCallTag.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                },
                isLoading = false
            )

            FilterPages.LOCATION.ordinal -> LocationSheet(
                selectedLocation = currentLocation,
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selLocation, applyAndDismiss ->
                    location.value = selLocation
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            openCallTag = openCallTag.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )

            FilterPages.PERCENTAGE_RAISED.ordinal -> PercentageRaisedSheet(
                currentPercentage = DiscoveryParams.RaisedBuckets.fromString(currentPercentage?.name),
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selectedBucket, applyAndDismiss ->
                    percentageBucket.value = selectedBucket
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            openCallTag = openCallTag.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )

            FilterPages.AMOUNT_RAISED.ordinal -> AmountRaisedSheet(
                currentBucket = DiscoveryParams.AmountBuckets.fromString(currentAmountRaised?.name),
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selectedBucket, applyAndDismiss ->
                    amountRaised.value = selectedBucket
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            openCallTag = openCallTag.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )

            FilterPages.GOAL.ordinal -> GoalSheet(
                currentGoalBucket = DiscoveryParams.GoalBuckets.fromString(currentGoal?.name),
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selectedBucket, applyAndDismiss ->
                    goal.value = selectedBucket
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            openCallTag = openCallTag.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )

            FilterPages.OPEN_CALLS.ordinal -> OpenCallsSheet(
                tags = tags,
                currentTag = openCallTag.value,
                onNavigate = {
                    coroutineScope.launch { pagerState.animateScrollToPage(FilterPages.MAIN_FILTER.ordinal) }
                },
                onDismiss = onDismiss,
                onApply = { selectedTag, applyAndDismiss ->
                    openCallTag.value = selectedTag
                    if (applyAndDismiss != null) {
                        applyUserSelection(
                            onApply = onApply,
                            projectState = projectState.value,
                            category = category.value,
                            percentageRaisedBucket = percentageBucket.value,
                            amountRaisedBucket = amountRaised.value,
                            goalBucket = goal.value,
                            location = location.value,
                            recommended = currentRecommended.value,
                            projectsLoved = currentProjectsLoved.value,
                            savedProjects = currentSavedProjects.value,
                            social = currentFollowing.value,
                            openCallTag = openCallTag.value,
                            updateSelectedCounts = updateSelectedCounts,
                            onDismiss = onDismiss,
                            shouldDismiss = applyAndDismiss
                        )
                    }
                }
            )
        }
    }
}

