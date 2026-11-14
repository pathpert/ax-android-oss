package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.User
import com.kickstarter.ui.compose.designsystem.KSTheme
import io.reactivex.Observable

private fun createMockFilterMenuViewModel(isLoggedIn: Boolean = false): FilterMenuViewModel {
    val mockUser = object : CurrentUserTypeV2() {
        private var user = if (isLoggedIn) UserFactory.user() else null
        override fun setToken(accessToken: String) {}
        override fun login(newUser: User) {}
        override fun logout() {}
        override val accessToken: String? get() = if (isLoggedIn) "Token" else null
        override fun refresh(freshUser: User) { user = freshUser }
        override fun observable(): Observable<KsOptional<User>> =
            if (isLoggedIn && user != null) {
                Observable.just(KsOptional.of(user!!))
            } else {
                Observable.just(KsOptional.empty<User>())
            }
        override fun getUser(): User? = user
    }
    val env = Environment.builder()
        .apolloClientV2(MockApolloClientV2())
        .currentUserV2(mockUser)
        .build()
    return FilterMenuViewModel(environment = env)
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarLocationActiveFilterPreview() {
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides createMockFilterMenuViewModel()) {
            SearchTopBar(
                onBackPressed = {},
                onValueChanged = {},
                selectedFilterCounts = mapOf(
                    FilterRowPillType.SORT.name to 0,
                    FilterRowPillType.CATEGORY.name to 0,
                    FilterRowPillType.PROJECT_STATUS.name to 0,
                    FilterRowPillType.FILTER.name to 1,
                    FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                    FilterRowPillType.LOCATION.name to 1,
                    FilterRowPillType.AMOUNT_RAISED.name to 0,
                    FilterRowPillType.RECOMMENDED.name to 0,
                    FilterRowPillType.PROJECTS_LOVED.name to 0,
                    FilterRowPillType.SAVED.name to 0,
                    FilterRowPillType.FOLLOWING.name to 0,
                    FilterRowPillType.GOAL.name to 0
                ),
                onPillPressedOpensBottomSheet = {},
                shouldShowPhase = true
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarAmountRaisedActiveFilterPreview() {
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides createMockFilterMenuViewModel()) {
            SearchTopBar(
                onBackPressed = {},
                onValueChanged = {},
                selectedFilterCounts = mapOf(
                    FilterRowPillType.SORT.name to 0,
                    FilterRowPillType.CATEGORY.name to 0,
                    FilterRowPillType.PROJECT_STATUS.name to 0,
                    FilterRowPillType.FILTER.name to 1,
                    FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                    FilterRowPillType.AMOUNT_RAISED.name to 1,
                    FilterRowPillType.RECOMMENDED.name to 0,
                    FilterRowPillType.PROJECTS_LOVED.name to 0,
                    FilterRowPillType.SAVED.name to 0,
                    FilterRowPillType.FOLLOWING.name to 0,
                ),
                onPillPressedOpensBottomSheet = {},
                shouldShowPhase = true
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarGoalActiveFilterPreview() {
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides createMockFilterMenuViewModel()) {
            SearchTopBar(
                onBackPressed = {},
                onValueChanged = {},
                selectedFilterCounts = mapOf(
                    FilterRowPillType.SORT.name to 0,
                    FilterRowPillType.CATEGORY.name to 0,
                    FilterRowPillType.PROJECT_STATUS.name to 0,
                    FilterRowPillType.FILTER.name to 1,
                    FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                    FilterRowPillType.GOAL.name to 1,
                    FilterRowPillType.RECOMMENDED.name to 0,
                    FilterRowPillType.PROJECTS_LOVED.name to 0,
                    FilterRowPillType.SAVED.name to 0,
                    FilterRowPillType.FOLLOWING.name to 0,
                ),
                onPillPressedOpensBottomSheet = {},
                shouldShowPhase = true
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarPercentageRaisedActiveFilterPreview() {
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides createMockFilterMenuViewModel()) {
            SearchTopBar(
                onBackPressed = {},
                onValueChanged = {},
                selectedFilterCounts = mapOf(
                    FilterRowPillType.SORT.name to 0,
                    FilterRowPillType.CATEGORY.name to 0,
                    FilterRowPillType.PROJECT_STATUS.name to 0,
                    FilterRowPillType.FILTER.name to 1,
                    FilterRowPillType.PERCENTAGE_RAISED.name to 1,
                    FilterRowPillType.RECOMMENDED.name to 0,
                    FilterRowPillType.PROJECTS_LOVED.name to 0,
                    FilterRowPillType.SAVED.name to 0,
                    FilterRowPillType.FOLLOWING.name to 0,
                ),
                onPillPressedOpensBottomSheet = {},
                shouldShowPhase = true
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarProjectStatusActiveFilterPreview() {
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides createMockFilterMenuViewModel()) {
            SearchTopBar(
                projectStatusText = "Live",
                onBackPressed = {},
                onValueChanged = {},
                selectedFilterCounts = mapOf(
                    FilterRowPillType.SORT.name to 0,
                    FilterRowPillType.CATEGORY.name to 0,
                    FilterRowPillType.PROJECT_STATUS.name to 1,
                    FilterRowPillType.FILTER.name to 1,
                    FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                    FilterRowPillType.RECOMMENDED.name to 0,
                    FilterRowPillType.PROJECTS_LOVED.name to 0,
                    FilterRowPillType.SAVED.name to 0,
                    FilterRowPillType.FOLLOWING.name to 0,
                ),
                onPillPressedOpensBottomSheet = {},
                shouldShowPhase = true
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarCategoryActiveFilterPreview() {
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides createMockFilterMenuViewModel()) {
            SearchTopBar(
                categoryPillText = "Art",
                onBackPressed = {},
                onValueChanged = {},
                selectedFilterCounts = mapOf(
                    FilterRowPillType.SORT.name to 0,
                    FilterRowPillType.CATEGORY.name to 1,
                    FilterRowPillType.PROJECT_STATUS.name to 0,
                    FilterRowPillType.FILTER.name to 1,
                    FilterRowPillType.PERCENTAGE_RAISED.name to 0,
                    FilterRowPillType.RECOMMENDED.name to 0,
                    FilterRowPillType.PROJECTS_LOVED.name to 0,
                    FilterRowPillType.SAVED.name to 0,
                    FilterRowPillType.FOLLOWING.name to 0,
                ),
                onPillPressedOpensBottomSheet = {},
                shouldShowPhase = true
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarAllActiveFiltersPreviewLoggedOutUser() {
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides createMockFilterMenuViewModel(isLoggedIn = false)) {
            SearchTopBar(
                categoryPillText = "Art",
                projectStatusText = "Live",
                onBackPressed = {},
                onValueChanged = {},
                selectedFilterCounts = mapOf(
                    FilterRowPillType.SORT.name to 0,
                    FilterRowPillType.CATEGORY.name to 1,
                    FilterRowPillType.PROJECT_STATUS.name to 1,
                    FilterRowPillType.FILTER.name to 1,
                    FilterRowPillType.PERCENTAGE_RAISED.name to 1,
                    FilterRowPillType.LOCATION.name to 1,
                    FilterRowPillType.AMOUNT_RAISED.name to 1,
                    FilterRowPillType.RECOMMENDED.name to 1,
                    FilterRowPillType.PROJECTS_LOVED.name to 1,
                    FilterRowPillType.SAVED.name to 1,
                    FilterRowPillType.FOLLOWING.name to 1,
                    FilterRowPillType.GOAL.name to 1
                ),
                onPillPressedOpensBottomSheet = {},
                shouldShowPhase = true
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchTopBarAllActiveFiltersPreviewLoggedInUser() {
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides createMockFilterMenuViewModel(isLoggedIn = true)) {
            SearchTopBar(
                categoryPillText = "Art",
                projectStatusText = "Live",
                onBackPressed = {},
                onValueChanged = {},
                selectedFilterCounts = mapOf(
                    FilterRowPillType.SORT.name to 0,
                    FilterRowPillType.CATEGORY.name to 1,
                    FilterRowPillType.PROJECT_STATUS.name to 1,
                    FilterRowPillType.FILTER.name to 1,
                    FilterRowPillType.PERCENTAGE_RAISED.name to 1,
                    FilterRowPillType.LOCATION.name to 1,
                    FilterRowPillType.AMOUNT_RAISED.name to 1,
                    FilterRowPillType.RECOMMENDED.name to 1,
                    FilterRowPillType.PROJECTS_LOVED.name to 1,
                    FilterRowPillType.SAVED.name to 1,
                    FilterRowPillType.FOLLOWING.name to 1,
                    FilterRowPillType.GOAL.name to 1
                ),
                onPillPressedOpensBottomSheet = {},
                shouldShowPhase = true
            )
        }
    }
}
