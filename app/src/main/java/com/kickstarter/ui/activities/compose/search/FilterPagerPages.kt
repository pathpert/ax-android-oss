package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Activity displaying and coordinating user interactions for FilterPagerPageItem.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun FilterPagerPageItem(
    page: Int,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    currentLocation: Location?,
    currentPercentage: DiscoveryParams.RaisedBuckets?,
    currentAmountRaised: DiscoveryParams.AmountBuckets?,
    currentCategory: Category?,
    currentGoal: DiscoveryParams.GoalBuckets?,
    currentRecommended: MutableState<Boolean>,
    currentProjectsLoved: MutableState<Boolean>,
    currentSavedProjects: MutableState<Boolean>,
    currentFollowing: MutableState<Boolean>,
    category: MutableState<Category?>,
    projectState: MutableState<DiscoveryParams.State?>,
    percentageBucket: MutableState<DiscoveryParams.RaisedBuckets?>,
    location: MutableState<Location?>,
    amountRaised: MutableState<DiscoveryParams.AmountBuckets?>,
    goal: MutableState<DiscoveryParams.GoalBuckets?>,
    openCallTag: MutableState<Tag?>,
    categories: List<Category>,
    tags: List<Tag>,
    isOpenCallsEnabled: Boolean,
    shouldShowPhase: Boolean,
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
    ) -> Unit
) {
    fun applyCurrent(applyAndDismiss: Boolean) {
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
            onDismiss = onDismiss,
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
                    applyCurrent(applyAndDismiss)
                }
            },
            onNavigate = { filterType ->
                val targetPage = when (filterType) {
                    FilterType.CATEGORIES -> FilterPages.CATEGORIES
                    FilterType.PERCENTAGE_RAISED -> FilterPages.PERCENTAGE_RAISED
                    FilterType.LOCATION -> FilterPages.LOCATION
                    FilterType.AMOUNT_RAISED -> FilterPages.AMOUNT_RAISED
                    FilterType.GOAL -> FilterPages.GOAL
                    FilterType.OPEN_CALLS -> FilterPages.OPEN_CALLS
                    else -> null
                }
                targetPage?.let {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(it.ordinal)
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
                    applyCurrent(applyAndDismiss)
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
                    applyCurrent(applyAndDismiss)
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
                    applyCurrent(applyAndDismiss)
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
                    applyCurrent(applyAndDismiss)
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
                    applyCurrent(applyAndDismiss)
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
                    applyCurrent(applyAndDismiss)
                }
            }
        )
    }
}
