package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Category
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSTheme
import io.reactivex.Observable

/**
 * Activity displaying and coordinating user interactions for PagerPreview.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PagerPreview() {
    val mockUser = object : CurrentUserTypeV2() {
        override fun setToken(accessToken: String) {}
        override fun login(newUser: User) {}
        override fun logout() {}
        override val accessToken: String? get() = null
        override fun refresh(freshUser: User) {}
        override fun observable(): Observable<KsOptional<User>> = Observable.just(KsOptional.empty())
        override fun getUser(): User? = null
    }
    val env = Environment.builder()
        .apolloClientV2(MockApolloClientV2())
        .currentUserV2(mockUser)
        .build()

    val fakeViewModel = remember { FilterMenuViewModel(environment = env) }

    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            val testPagerState = rememberPagerState(initialPage = 0, pageCount = { FilterPages.entries.size })
            val testSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )

            val categories = CategoryFactory.rootCategories()
            val selectedStatus = DiscoveryParams.State.LIVE

            val appliedFilters = remember { mutableListOf<Pair<DiscoveryParams.State?, Category?>>() }
            val dismissed = remember { mutableListOf<Boolean>() }
            val selectedCounts = remember { mutableListOf<Pair<Int?, Int?>>() }

            Box(modifier = Modifier.size(400.dp)) {
                FilterPagerSheet(
                    selectedProjectStatus = selectedStatus,
                    currentCategory = categories[0],
                    categories = categories,
                    onDismiss = { dismissed.add(true) },
                    onApply = { state, category, bucket, location, amountBucket, _, _, _, _, goalBucket, openCallTag -> appliedFilters.add(Pair(state, category)) },
                    updateSelectedCounts = { statusCount, categoryCount, raisedBucket, location, amountBucketCount, _, _, _, _, goalBucketCount, openCallsCount ->
                        selectedCounts.add(
                            statusCount to categoryCount
                        )
                    },
                    pagerState = testPagerState,
                    sheetState = testSheetState,
                    shouldShowPhase = true
                )
            }
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenPreviewNonEmpty() {
    val mockUser = object : CurrentUserTypeV2() {
        override fun setToken(accessToken: String) {}
        override fun login(newUser: User) {}
        override fun logout() {}
        override val accessToken: String? get() = null
        override fun refresh(freshUser: User) {}
        override fun observable(): Observable<KsOptional<User>> = Observable.just(KsOptional.empty())
        override fun getUser(): User? = null
    }
    val env = Environment.builder()
        .apolloClientV2(MockApolloClientV2())
        .currentUserV2(mockUser)
        .build()

    val fakeViewModel = remember { FilterMenuViewModel(environment = env) }
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            SearchScreen(
                onBackClicked = { },
                errorSnackBarHostState = SnackbarHostState(),
                isLoading = false,
                isDefaultList = true,
                itemsList = List(100) {
                    Project.builder()
                        .name("This is a test $it")
                        .pledged((it * 2).toDouble())
                        .photo(Photo.builder().altText("").full("").build())
                        .goal(100.0)
                        .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                        .build()
                },
                lazyColumnListState = rememberLazyListState(),
                showEmptyView = false,
                categories = listOf(),
                onSearchTermChanged = {},
                onItemClicked = { project -> }
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenPreviewEmpty() {
    val mockUser = object : CurrentUserTypeV2() {
        override fun setToken(accessToken: String) {}
        override fun login(newUser: User) {}
        override fun logout() {}
        override val accessToken: String? get() = null
        override fun refresh(freshUser: User) {}
        override fun observable(): Observable<KsOptional<User>> = Observable.just(KsOptional.empty())
        override fun getUser(): User? = null
    }
    val env = Environment.builder()
        .apolloClientV2(MockApolloClientV2())
        .currentUserV2(mockUser)
        .build()

    val fakeViewModel = remember { FilterMenuViewModel(environment = env) }
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            SearchScreen(
                onBackClicked = { },
                errorSnackBarHostState = SnackbarHostState(),
                isLoading = true,
                itemsList = listOf(),
                lazyColumnListState = rememberLazyListState(),
                showEmptyView = true,
                categories = listOf(),
                onSearchTermChanged = {},
                onItemClicked = { project -> }
            )
        }
    }
}
