package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Location
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LocationSheetPreview() {
    val env = Environment.builder().apolloClientV2(
        object : MockApolloClientV2() {
            override suspend fun getLocations(
                useDefault: Boolean,
                term: String?,
                lat: Float?,
                long: Float?,
                radius: Float?,
                filterByCoordinates: Boolean?
            ): Result<List<Location>> {
                if (useDefault) return Result.success(listOf(LocationFactory.vancouver()))
                val searched = listOf(
                    LocationFactory.sydney(),
                    LocationFactory.mexico(),
                    LocationFactory.canada(),
                    LocationFactory.germany(),
                    LocationFactory.unitedStates(),
                    LocationFactory.nigeria(),
                    LocationFactory.sydney(),
                    LocationFactory.mexico(),
                    LocationFactory.canada(),
                    LocationFactory.germany(),
                    LocationFactory.unitedStates(),
                    LocationFactory.nigeria(),
                )
                if (term.isNotNull()) return Result.success(searched)

                return Result.success(emptyList())
            }
        }
    ).build()
    val fakeViewModel = FilterMenuViewModel(env)

    KSTheme {
        CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
            LocationSheet()
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultLocationComposablePreview() {
    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            DefaultLocationComposable(
                defaultLocations = listOf(LocationFactory.vancouver(), LocationFactory.sydney()),
                currentLocation = remember { mutableStateOf(LocationFactory.sydney()) },
                onclickCallback = { _ -> }
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SuggestedLocationsComposablePreview() {
    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            SuggestedLocationsComposable(
                suggestedLocations = listOf(LocationFactory.mexico(), LocationFactory.unitedStates(), LocationFactory.germany(), LocationFactory.nigeria()),
                currentLocation = remember { mutableStateOf(LocationFactory.sydney()) },
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InputDefaultPreview() {
    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            val isFocused = remember { mutableStateOf(false) }
            val inputValue = remember { mutableStateOf("") }
            InputSearchComposable(isFocused = isFocused, input = inputValue)
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InputSelectedPreview() {
    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            val isFocused = remember { mutableStateOf(true) }
            val inputValue = remember { mutableStateOf(LocationFactory.vancouver().displayableName()) }
            InputSearchComposable(isFocused = isFocused, input = inputValue)
        }
    }
}
