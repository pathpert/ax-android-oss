package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.features.search.viewmodel.SearchAndFilterViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.isTrimmedEmpty
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHeadsupSnackbar
import com.kickstarter.ui.compose.designsystem.KSProjectCardLarge
import com.kickstarter.ui.compose.designsystem.KSProjectCardSmall
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.KSVideoFeedBanner
import kotlinx.coroutines.launch

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

@Composable
fun SearchAndFilterScreen(
    env: Environment,
    filterMenuVM: FilterMenuViewModel,
    searchViewModel: SearchAndFilterViewModel,
    onBackClicked: () -> Unit = {},
    preLaunchedCallback: (project: Project, reftag: RefTag) -> Unit = { a, b -> },
    projectCallback: (projAndRef: Pair<Project, RefTag>) -> Unit = { a -> },
    onVideoFeedBannerClicked: () -> Unit = {}
) {
    val phaseff = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_SEARCH_FILTER) ?: false
    var currentSearchTerm by rememberSaveable { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val searchUIState by searchViewModel.searchUIState.collectAsStateWithLifecycle()
    val popularProjects = searchUIState.popularProjectsList
    val searchedProjects = searchUIState.searchList
    val isLoading = searchUIState.isLoading
    val hasMorePages = searchUIState.hasMore
    val isVideoFeedBannerVisible by searchViewModel.isVideoFeedBannerVisible.collectAsStateWithLifecycle()
    val isOpenCallsEnabled by searchViewModel.isOpenCallsEnabled.collectAsStateWithLifecycle()

    val categoriesState by filterMenuVM.filterMenuUIState.collectAsStateWithLifecycle()
    val categories = categoriesState.categoriesList
    val tags = categoriesState.tagsList

    // - Fetch the Creative Prompt tags only once the Open Calls gate resolves to enabled.
    LaunchedEffect(isOpenCallsEnabled) {
        if (isOpenCallsEnabled) {
            filterMenuVM.getTags()
        }
    }

    val errorAction = setUpErrorActions(snackbarHostState)

    searchViewModel.provideErrorAction { message ->
        errorAction.invoke(message)
    }

    filterMenuVM.provideErrorAction { message ->
        errorAction.invoke(message)
    }

    CompositionLocalProvider(LocalFilterMenuViewModel provides filterMenuVM) {
        SearchScreen(
            environment = env,
            onBackClicked = {
                onBackClicked()
            },
            errorSnackBarHostState = snackbarHostState,
            isLoading = isLoading,
            isDefaultList = currentSearchTerm.isTrimmedEmpty(),
            itemsList = if (currentSearchTerm.isTrimmedEmpty()) {
                popularProjects
            } else {
                searchedProjects
            },
            lazyColumnListState = lazyListState,
            showEmptyView = !isLoading && (searchedProjects.isEmpty() && popularProjects.isEmpty()),
            categories = categories,
            onSearchTermChanged = { searchTerm ->
                currentSearchTerm = searchTerm
                searchViewModel.updateSearchTerm(searchTerm)
            },
            onItemClicked = { project ->
                val projAndRef = searchViewModel.getProjectAndRefTag(project)
                if (project.displayPrelaunch().isTrue()) {
                    preLaunchedCallback(project, projAndRef.second)
                } else {
                    projectCallback(projAndRef)
                }
            },
            onApplySearchWithParams = { category, sort, projectState, percentageBucket, location, amountRaisedBucket, recomended, projectsLoved, saved, social, goalBucket, openCallTag ->
                searchViewModel.updateParamsToSearchWith(
                    category = category,
                    projectSort = sort
                        ?: DiscoveryParams.Sort.MAGIC, // magic is the default sort
                    projectState = projectState,
                    percentageBucket = percentageBucket,
                    location = location,
                    amountBucket = amountRaisedBucket,
                    goalBucket = goalBucket,
                    recommended = recomended,
                    projectsLoved = projectsLoved,
                    savedProjects = saved,
                    social = social,
                    tagId = openCallTag?.id()?.toInt()
                )
            },
            shouldShowPhase = phaseff,
            isVideoFeedBannerVisible = isVideoFeedBannerVisible,
            onVideoFeedBannerClicked = onVideoFeedBannerClicked,
            tags = tags,
            isOpenCallsEnabled = isOpenCallsEnabled
        )
    }

    // Load more when scroll to the end
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex >= (totalItems - 5) && totalItems > 0
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(shouldLoadMore, lifecycleOwner.lifecycle.currentStateAsState().value, isLoading, hasMorePages) {
        if (shouldLoadMore && lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED && !isLoading && hasMorePages) {
            searchViewModel.loadMore()
        }
    }
}

@Composable
private fun setUpErrorActions(snackbarHostState: SnackbarHostState): (String?) -> Unit {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val defaultErrorMessage = context.getString(R.string.Something_went_wrong_please_try_again)

    // TODO: store SnackbarResult on VM to consult it before showing new one, in case there is multiple enqueue snackbars.
    // TODO:  channels / LaunchedEffect convo might be a good fit
    return { message: String? ->
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message ?: defaultErrorMessage,
                actionLabel = KSSnackbarTypes.KS_ERROR.name,
                duration = SnackbarDuration.Long
            )
        }
    }
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

    val countApiIsReady = false // Hide all result counts until backend API is ready

    val selectedFilterCounts: SnapshotStateMap<String, Int> = remember {
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

    val currentStaffPicked = remember { mutableStateOf<Boolean>(false) }
    val currentStarred = remember { mutableStateOf<Boolean>(false) }
    val currentSocial = remember { mutableStateOf<Boolean>(false) }
    val currentRecommended = remember { mutableStateOf<Boolean>(false) }

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
                            else -> {
                                // Other pills open bottomSheet, handled in onPillPressed
                            }
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
                var numbersActive = 0
                selectedFilterCounts.entries.map { entry ->
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.LIST_VIEW.name)
                        .background(colors.backgroundSurfacePrimary)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = dimensions.paddingMediumLarge,
                        end = dimensions.paddingMediumLarge
                    ),
                    state = lazyColumnListState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isVideoFeedBannerVisible) {
                        item {
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                            KSVideoFeedBanner(onButtonClick = onVideoFeedBannerClicked)
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                        }
                    }

                    itemsIndexed(itemsList) { index, project ->
                        if (index == 0 && isDefaultList) {
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                            Text(
                                modifier = Modifier
                                    .testTag(SearchScreenTestTag.DISCOVER_PROJECTS_TITLE.name)
                                    .fillMaxWidth(),
                                text = stringResource(id = R.string.activity_empty_state_logged_in_button),
                                style = typographyV2.title2,
                                color = colors.kds_support_700,
                                textAlign = TextAlign.Start
                            )
                        }

                        val state = getCardProjectState(project)
                        val fundingInfoString = getFundingInfoString(state, environment, project)

                        if (index == 0) {
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                            KSProjectCardLarge(
                                modifier = Modifier
                                    .testTag(SearchScreenTestTag.FEATURED_PROJECT_VIEW.name),
                                photo = project.photo(),
                                title = project.name(),
                                state = state,
                                fundingInfoString = fundingInfoString,
                                fundedPercentage = project.percentageFunded().toInt(),
                            ) {
                                onItemClicked(project)
                            }

                            if (itemsList.size > 1) {
                                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                            }
                        } else {
                            KSProjectCardSmall(
                                modifier = Modifier
                                    .testTag(SearchScreenTestTag.NORMAL_PROJECT_VIEW.name + index),
                                photo = project.photo(),
                                title = project.name(),
                                state = state,
                                fundingInfoString = fundingInfoString,
                                fundedPercentage = project.percentageFunded().toInt(),
                            ) {
                                onItemClicked(project)
                            }

                            if (index < itemsList.size - 1) {
                                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                            } else {
                                Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                            }
                        }
                    }

                    item(isLoading) {
                        if (isLoading && itemsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                            KSCircularProgressIndicator(
                                modifier = Modifier
                                    .testTag(SearchScreenTestTag.IN_LIST_LOADING_VIEW.name)
                                    .size(size = dimensions.imageSizeLarge)
                            )

                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                        }
                    }
                }
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
                onDismissBottomSheet = onApplySearchWithParams,
                currentSort = currentSort,
                currentProjectState = currentProjectState,
                categories = categories,
                categoryPillText = categoryPillText,
                projectStatusPillText = projectStatusPill,
                initialCategoryPillText = initialCategoryPillText,
                selectedFilterCounts = selectedFilterCounts,
                countApiIsReady = countApiIsReady,
                sheetState = sheetState,
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
                shouldShowPhase = shouldShowPhase,
                isSheetOpen = isSheetOpen
            )
        }
    }

    // - Allow recomposition to happen first to draw the bottomSheet content, later on execute the animation to show/hide for smoother animation
    LaunchedEffect(isSheetOpen.value) {
        if (isSheetOpen.value.isTrue()) {
            sheetState.show()
        } else sheetState.hide()
    }
}
