package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.models.Location
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

object LocationTestTags {
    const val DEFAULT_LOCATION_LIST = "default_location_list"
    const val SUGGESTED_LOCATIONS_LIST = "suggested_location_list"
    const val LOCATION_ANYWHERE = "location_anywhere"
    const val INPUT_SEARCH = "input_search"
    const val INPUT_BUTTON = "input_button"
    fun locationTag(location: Location) = "location_${location.displayableName()}"
}

@Composable
fun LocationSheet(
    selectedLocation: Location? = null,
    onDismiss: () -> Unit = {},
    onApply: (Location?, Boolean?) -> Unit = { _, _ -> },
    onNavigate: () -> Unit = {},
) {
    val viewModel = LocalFilterMenuViewModel.current
    val locationsUIState by viewModel.locationsUIState.collectAsStateWithLifecycle()
    val defaultLocations = if (!LocalInspectionMode.current)
        locationsUIState.nearLocations
    else listOf(LocationFactory.vancouver())

    val searched = if (!LocalInspectionMode.current)
        locationsUIState.searchedLocations
    else listOf(LocationFactory.sydney(), LocationFactory.mexico(), LocationFactory.canada(), LocationFactory.germany())

    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val currentLocation = remember { mutableStateOf(selectedLocation) }

    KSTheme {
        Surface(
            color = colors.backgroundSurfacePrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = backgroundDisabledColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = dimensions.dividerThickness.toPx()
                            )
                        }
                        .padding(
                            top = dimensions.paddingLarge,
                            bottom = dimensions.paddingLarge,
                            end = dimensions.paddingMediumSmall
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KSIconButton(
                        modifier = Modifier
                            .padding(start = dimensions.paddingSmall)
                            .testTag(SearchScreenTestTag.BACK_BUTTON.name),
                        onClick = onNavigate,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.Back)
                    )

                    Text(
                        text = stringResource(R.string.Location),
                        style = typographyV2.headingXL,
                        modifier = Modifier.weight(1f),
                        color = colors.textPrimary
                    )

                    KSIconButton(
                        modifier = Modifier.testTag(CategorySelectionSheetTestTag.DISMISS_BUTTON.name),
                        onClick = onDismiss,
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.accessibility_discovery_buttons_close)
                    )
                }

                val isFocused = remember { mutableStateOf(false) }
                val inputValue = remember { mutableStateOf("") }

                InputSearchComposable(
                    isFocused = isFocused,
                    input = inputValue,
                    searchCallback = { term -> viewModel.updateQuery(term) },
                    cancelCallback = {
                        currentLocation.value = null
                        inputValue.value = ""
                        viewModel.clearQuery()
                    }
                )

                if (!isFocused.value && inputValue.value.isEmpty()) {
                    DefaultLocationComposable(
                        modifier = Modifier.weight(1f),
                        defaultLocations = defaultLocations,
                        currentLocation = currentLocation,
                        onclickCallback = { locationClicked ->
                            if (locationClicked.isNotNull()) {
                                currentLocation.value = locationClicked
                            } else {
                                currentLocation.value = null
                                inputValue.value = ""
                            }
                        }
                    )
                }

                if (isFocused.value && inputValue.value.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f)
                    )
                }

                if (inputValue.value.isNotEmpty()) {
                    SuggestedLocationsComposable(
                        modifier = Modifier.weight(1f),
                        suggestedLocations = searched,
                        currentLocation = currentLocation,
                        onclickCallback = { clickedLocation ->
                            currentLocation.value = clickedLocation
                            inputValue.value = clickedLocation?.displayableName() ?: ""
                        }
                    )
                }

                KSSearchBottomSheetFooter(
                    leftButtonIsEnabled = currentLocation.value.isNotNull(),
                    leftButtonClickAction = {
                        currentLocation.value = null
                        inputValue.value = ""
                        onApply(currentLocation.value, false)
                    },
                    rightButtonIsEnabled = currentLocation.value.isNotNull() || (currentLocation.value.isNull() && inputValue.value.isNullOrEmpty()),
                    rightButtonOnClickAction = {
                        onApply(currentLocation.value, true)
                    }
                )
            }
        }
    }
}
