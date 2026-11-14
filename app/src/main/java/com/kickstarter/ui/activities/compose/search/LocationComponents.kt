package com.kickstarter.ui.activities.compose.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.models.Location
import com.kickstarter.ui.activities.compose.search.LocationTestTags.INPUT_BUTTON
import com.kickstarter.ui.activities.compose.search.LocationTestTags.INPUT_SEARCH
import com.kickstarter.ui.activities.compose.search.LocationTestTags.LOCATION_ANYWHERE
import com.kickstarter.ui.activities.compose.search.LocationTestTags.SUGGESTED_LOCATIONS_LIST
import com.kickstarter.ui.activities.compose.search.LocationTestTags.locationTag
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
internal fun SuggestedLocationsComposable(
    modifier: Modifier = Modifier,
    suggestedLocations: List<Location> = emptyList(),
    currentLocation: MutableState<Location?>,
    onclickCallback: (Location?) -> Unit = {}
) {
    val dimensions: KSDimensions = KSTheme.dimensions

    LazyColumn(
        modifier = modifier
            .testTag(SUGGESTED_LOCATIONS_LIST)
            .fillMaxWidth()
            .padding(top = dimensions.paddingSmall, start = dimensions.paddingMedium),
        horizontalAlignment = Alignment.Start,
    ) {
        items(suggestedLocations) { location ->
            Row(
                modifier = Modifier.testTag(locationTag(location))
                    .animateItem()
                    .padding(
                        top = dimensions.paddingMediumSmall,
                        start = dimensions.paddingMediumSmall,
                        bottom = dimensions.paddingMediumSmall
                    )
                    .clickable {
                        onclickCallback(location)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    color = colors.textPrimary,
                    text = location.displayableName(),
                    style = typographyV2.headingLG
                )
            }
        }
    }
}

@Composable
internal fun InputSearchComposable(
    modifier: Modifier = Modifier,
    isFocused: MutableState<Boolean>,
    input: MutableState<String>,
    searchCallback: (String) -> Unit = { _ -> },
    cancelCallback: () -> Unit = {},
) {
    val dimensions: KSDimensions = KSTheme.dimensions
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium)
            .fillMaxWidth()
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = modifier
                .testTag(INPUT_SEARCH)
                .weight(1f)
                .onFocusChanged { it ->
                    if (it.isFocused) isFocused.value = true
                }
                .testTag(SearchScreenTestTag.SEARCH_TEXT_INPUT.name),
            value = input.value,
            onValueChange = {
                input.value = it
                searchCallback(it)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.backgroundSurfacePrimary,
                unfocusedContainerColor = colors.backgroundSurfacePrimary,
                disabledContainerColor = colors.backgroundSurfacePrimary,
                errorContainerColor = colors.backgroundSurfacePrimary,
                focusedTextColor = colors.textAccentGrey,
                unfocusedTextColor = colors.textAccentGrey,
                disabledTextColor = colors.textDisabled,
                focusedLabelColor = colors.borderActive,
                unfocusedLabelColor = colors.textSecondary,
                errorLabelColor = colors.kds_alert,
                cursorColor = colors.kds_create_700,
                errorCursorColor = colors.kds_alert,
                focusedBorderColor = colors.borderActive,
                unfocusedBorderColor = colors.borderBold,
                disabledBorderColor = colors.borderBold,
                errorBorderColor = colors.kds_alert
            ),
            label = {
                Text(text = stringResource(id = R.string.Location_searchbox_placeholder))
            },
            singleLine = true,
            trailingIcon = {
                if (input.value.isNotEmpty()) {
                    KSIconButton(
                        onClick = {
                            input.value = ""
                            cancelCallback()
                        },
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(id = R.string.social_buttons_cancel)
                    )
                }
            },
        )

        AnimatedVisibility(
            visible = isFocused.value || input.value.isNotEmpty(),
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            TextButton(
                onClick = {
                    input.value = ""
                    keyboardController?.hide()
                    isFocused.value = false
                    focusManager.clearFocus()
                    cancelCallback()
                },
                modifier = modifier
                    .testTag(INPUT_BUTTON)
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.Cancel),
                    color = colors.textAccentGreen,
                    style = typographyV2.bodyLG
                )
            }
        }
    }
}

@Composable
internal fun DefaultLocationComposable(
    modifier: Modifier = Modifier,
    defaultLocations: List<Location>,
    currentLocation: MutableState<Location?>,
    onclickCallback: (Location?) -> Unit = {}
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val isSelectedId = currentLocation.value?.id()

    LazyColumn(
        modifier = modifier
            .testTag(LocationTestTags.DEFAULT_LOCATION_LIST)
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
                horizontal = dimensions.paddingLarge,
                vertical = dimensions.paddingMedium
            ),
    ) {
        itemsIndexed(defaultLocations) { index, location ->
            if (index == 0) {
                Row(
                    modifier = Modifier.testTag(LOCATION_ANYWHERE)
                        .padding(
                            top = dimensions.paddingMediumSmall,
                            bottom = dimensions.paddingMediumSmall
                        )
                        .clickable {
                            onclickCallback(null)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                ) {
                    RadioButton(
                        selected = isSelectedId == null,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = colors.backgroundSelected,
                            selectedColor = colors.backgroundSelected
                        )
                    )
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        color = colors.textPrimary,
                        text = stringResource(R.string.Location_anywhere),
                        style = typographyV2.headingLG
                    )
                }
            }
            Row(
                modifier = Modifier.testTag(locationTag(location))
                    .padding(
                        top = dimensions.paddingMediumSmall,
                        bottom = dimensions.paddingMediumSmall
                    )
                    .clickable {
                        onclickCallback(location)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                RadioButton(
                    selected = isSelectedId == location.id(),
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        unselectedColor = colors.backgroundSelected,
                        selectedColor = colors.backgroundSelected
                    )
                )
                Text(
                    modifier = Modifier
                        .weight(1f),
                    color = colors.textPrimary,
                    text = location.displayableName(),
                    style = typographyV2.headingLG
                )
            }
        }
    }
}
