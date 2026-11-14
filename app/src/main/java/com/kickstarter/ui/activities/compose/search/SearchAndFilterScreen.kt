package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import kotlinx.coroutines.launch

@Composable
fun SearchAndFilterScreen(
    env: Environment,
    filterMenuVM: FilterMenuViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = FilterMenuViewModel.Factory(env)),
    searchViewModel: SearchAndFilterViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = SearchAndFilterViewModel.Factory(env)),
    onBackClicked: () -> Unit = {},
    preLaunchedCallback: (project: Project, reftag: RefTag) -> Unit = { _, _ -> },
    projectCallback: (projAndRef: Pair<Project, RefTag>) -> Unit = { _ -> },
    onVideoFeedBannerClicked: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        filterMenuVM.getRootCategories()
    }
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
                    projectSort = sort ?: DiscoveryParams.Sort.MAGIC,
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
