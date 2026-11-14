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
        FilterPagerPageItem(
            page = page,
            coroutineScope = coroutineScope,
            pagerState = pagerState,
            currentLocation = currentLocation,
            currentPercentage = currentPercentage,
            currentAmountRaised = currentAmountRaised,
            currentCategory = currentCategory,
            currentGoal = currentGoal,
            currentRecommended = currentRecommended,
            currentProjectsLoved = currentProjectsLoved,
            currentSavedProjects = currentSavedProjects,
            currentFollowing = currentFollowing,
            category = category,
            projectState = projectState,
            percentageBucket = percentageBucket,
            location = location,
            amountRaised = amountRaised,
            goal = goal,
            openCallTag = openCallTag,
            categories = categories,
            tags = tags,
            isOpenCallsEnabled = isOpenCallsEnabled,
            shouldShowPhase = shouldShowPhase,
            onDismiss = onDismiss,
            onApply = onApply,
            updateSelectedCounts = updateSelectedCounts
        )
    }
}
