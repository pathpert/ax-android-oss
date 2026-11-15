package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHeadsupSnackbar
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

/**
 * Activity displaying and coordinating user interactions for SearchScreenTestTag.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
enum class SearchScreenTestTag {
    BACK_BUTTON,
    SEARCH_TEXT_INPUT,
    EMPTY_VIEW,
    LOADING_VIEW,
    IN_LIST_LOADING_VIEW,
    LIST_VIEW,
    DISCOVER_PROJECTS_TITLE,
    FEATURED_PROJECT_VIEW,
    NORMAL_PROJECT_VIEW,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    environment: Environment? = null,
    onBackClicked: () -> Unit,
    errorSnackBarHostState: SnackbarHostState = SnackbarHostState(),
    isDefaultList: Boolean = true,
    isLoading: Boolean,
    itemsList: List<Project> = listOf(),
    lazyColumnListState: LazyListState,
    showEmptyView: Boolean,
    categories: List<Category>,
    onSearchTermChanged: (String) -> Unit,
    onItemClicked: (Project) -> Unit,
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
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ ->
    },
    shouldShowPhase: Boolean = true,
    isVideoFeedBannerVisible: Boolean = false,
    onVideoFeedBannerClicked: () -> Unit = {},
    tags: List<Tag> = listOf(),
    isOpenCallsEnabled: Boolean = false,
    isFromDeepLink: Boolean = false
) {
    var currentSearchTerm by rememberSaveable { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val countApiIsReady = false

    val selectedFilterCounts: SnapshotStateMap<String, Int> = rememberSearchFilterCounts()

    val initialCategoryPillText = stringResource(R.string.Category)
    val categoryPillText = remember { mutableStateOf(initialCategoryPillText) }

    val initialProjectStatsPillText = stringResource(R.string.Project_status)
    val projectStatusPill = remember { mutableStateOf(initialProjectStatsPillText) }

    val currentSort = remember { mutableStateOf(DiscoveryParams.Sort.MAGIC) }
    val currentCategory = remember { mutableStateOf<Category?>(null) }
    val currentProjectState = remember { mutableStateOf<DiscoveryParams.State?>(null) }
    val currentPercentage = remember { mutableStateOf<DiscoveryParams.RaisedBuckets?>(null) }
    val currentAmountRaised = remember { mutableStateOf<DiscoveryParams.AmountBuckets?>(null) }
    val currentLocation = remember { mutableStateOf<Location?>(null) }
    val currentStaffPicked = remember { mutableStateOf(false) }
    val currentStarred = remember { mutableStateOf(false) }
    val currentSocial = remember { mutableStateOf(false) }
    val currentRecommended = remember { mutableStateOf(false) }
    val currentGoal = remember { mutableStateOf<DiscoveryParams.GoalBuckets?>(null) }
    val currentOpenCallTag = remember { mutableStateOf<Tag?>(null) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (isOpenCallsEnabled) FilterPages.entries.size else FilterPages.entries.size - 1 }
    )

    val activeBottomSheet = remember {
        mutableStateOf<FilterRowPillType?>(null)
    }

    val sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val isSheetOpen = remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(dimensions.paddingSmall),
                hostState = errorSnackBarHostState,
                snackbar = { data ->
                    if (data.visuals.actionLabel == KSSnackbarTypes.KS_ERROR.name) {
                        KSErrorSnackbar(text = data.visuals.message)
                    } else {
                        KSHeadsupSnackbar(text = data.visuals.message)
                    }
                }
            )
        },
        topBar = {
            Surface(
                shadowElevation = 3.dp,
                tonalElevation = 0.dp
            ) {
                SearchTopBar(
                    countApiIsReady = countApiIsReady,
                    categoryPillText = categoryPillText.value,
                    onBackPressed = onBackClicked,
                    projectStatusText = projectStatusPill.value,
                    percentageRaisedText = currentPercentage.value?.let { textForBucket(it) } ?: stringResource(R.string.Percentage_raised),
                    locationText = currentLocation.value?.displayableName() ?: stringResource(R.string.Location),
                    amountRaisedText = currentAmountRaised.value?.let { textForBucket(it) } ?: stringResource(R.string.Amount_raised),
                    goalText = currentGoal.value?.let { textForGoalPill(it) } ?: stringResource(R.string.Goal),
                    openCallsText = currentOpenCallTag.value?.name() ?: stringResource(R.string.fpo_Open_calls),
                    isOpenCallsEnabled = isOpenCallsEnabled,
                    isFromDeepLink = isFromDeepLink,
                    recommendedStatus = currentRecommended,
                    projectsLovedStatus = currentStaffPicked,
                    savedProjects = currentStarred,
                    following = currentSocial,
                    onValueChanged = {
                        onSearchTermChanged.invoke(it)
                        currentSearchTerm = it
                    },
                    selectedFilterCounts = selectedFilterCounts,
                    onPillPressedOpensBottomSheet = onPillPressedOpensBottomSheet(
                        activeBottomSheet,
                        coroutineScope,
                        sheetState,
                        pagerState,
                        isSheetOpen
                    ),
                    onPillPressedShowOnlyToggles = { rowPillType, value ->
                        selectedFilterCounts[rowPillType.name] = if (value) 1 else 0
                        val total = selectedFilterCounts[FilterRowPillType.FILTER.name] ?: 0
                        selectedFilterCounts[FilterRowPillType.FILTER.name] = if (value) total + 1 else total - 1
                        when (rowPillType) {
                            FilterRowPillType.RECOMMENDED -> currentRecommended.value = value
                            FilterRowPillType.PROJECTS_LOVED -> currentStaffPicked.value = value
                            FilterRowPillType.SAVED -> currentStarred.value = value
                            FilterRowPillType.FOLLOWING -> currentSocial.value = value
                            else -> {}
                        }
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
                    },
                    shouldShowPhase = shouldShowPhase
                )
            }
        },
        containerColor = colors.backgroundSurfacePrimary
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showEmptyView) {
                SearchEmptyStateView(
                    environment = environment,
                    currentSearchTerm = currentSearchTerm,
                    selectedFilterCounts = selectedFilterCounts,
                    currentCategory = currentCategory,
                    currentProjectState = currentProjectState,
                    currentPercentage = currentPercentage,
                    currentLocation = currentLocation,
                    currentAmountRaised = currentAmountRaised,
                    currentRecommended = currentRecommended,
                    currentStaffPicked = currentStaffPicked,
                    currentStarred = currentStarred,
                    currentSocial = currentSocial,
                    currentGoal = currentGoal,
                    currentOpenCallTag = currentOpenCallTag,
                    currentSort = currentSort,
                    onApplySearchWithParams = onApplySearchWithParams
                )
            } else {
                SearchResultsList(
                    itemsList = itemsList,
                    isDefaultList = isDefaultList,
                    isLoading = isLoading,
                    lazyColumnListState = lazyColumnListState,
                    environment = environment,
                    isVideoFeedBannerVisible = isVideoFeedBannerVisible,
                    onVideoFeedBannerClicked = onVideoFeedBannerClicked,
                    onItemClicked = onItemClicked
                )
            }
            if (isLoading && itemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.LOADING_VIEW.name)
                        .fillMaxSize()
                        .background(color = colors.backgroundSurfacePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    KSCircularProgressIndicator()
                }
            }
        }
    }
    SearchFilterModalBottomSheet(
        isSheetOpen = isSheetOpen,
        sheetState = sheetState,
        activeBottomSheet = activeBottomSheet,
        currentCategory = currentCategory,
        onDismissBottomSheet = onApplySearchWithParams,
        currentSort = currentSort,
        currentProjectState = currentProjectState,
        categories = categories,
        categoryPillText = categoryPillText,
        projectStatusPillText = projectStatusPill,
        initialCategoryPillText = initialCategoryPillText,
        selectedFilterCounts = selectedFilterCounts,
        countApiIsReady = countApiIsReady,
        pagerState = pagerState,
        currentPercentage = currentPercentage,
        currentLocation = currentLocation,
        currentAmountRaised = currentAmountRaised,
        currentRecommended = currentRecommended,
        currentProjectsLoved = currentStaffPicked,
        currentSavedProjects = currentStarred,
        currentFollowing = currentSocial,
        currentGoal = currentGoal,
        currentOpenCallTag = currentOpenCallTag,
        tags = tags,
        isOpenCallsEnabled = isOpenCallsEnabled,
        shouldShowPhase = shouldShowPhase
    )
}
