package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import io.reactivex.Observable

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ProjectStatusRowPreview() {
    KSTheme {
        ProjectStatusRow(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
            text = titleForFilter(FilterType.PROJECT_STATUS)
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun OthersRowPreview() {
    KSTheme {
        OtherFiltersRow(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
            text = titleForFilter(FilterType.OTHERS),
            isUserloggedIn = true
        )
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FilterMenuSheetPreview_UserLoggedOut() {
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
    val fakeViewModel = FilterMenuViewModel(environment = env)
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            FilterMenuSheet(
                selectedProjectStatus = DiscoveryParams.State.LIVE,
                onDismiss = {},
                onApply = { _, _, _, _, _, _ -> },
                selectedLocation = LocationFactory.vancouver(),
                selectedAmount = DiscoveryParams.AmountBuckets.BUCKET_4
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FilterMenuSheetPreview_UserLoggedIn() {
    val mockUser = object : CurrentUserTypeV2() {
        private var user = UserFactory.user()
        override fun setToken(accessToken: String) {}
        override fun login(newUser: User) {}
        override fun logout() {}
        override val accessToken: String? get() = "Token"
        override fun refresh(freshUser: User) { user = freshUser }
        override fun observable(): Observable<KsOptional<User>> = Observable.just(KsOptional.of(user))
        override fun getUser(): User? = null
    }

    val env = Environment.builder()
        .apolloClientV2(MockApolloClientV2())
        .currentUserV2(mockUser)
        .build()

    val fakeViewModel = FilterMenuViewModel(environment = env)
    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            FilterMenuSheet(
                selectedProjectStatus = DiscoveryParams.State.LIVE,
                onDismiss = {},
                onApply = { _, _, _, _, _, _ -> },
                selectedLocation = LocationFactory.vancouver(),
                selectedAmount = DiscoveryParams.AmountBuckets.BUCKET_4
            )
        }
    }
}
