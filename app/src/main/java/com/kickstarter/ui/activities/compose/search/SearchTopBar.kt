package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

/**
 * Activity displaying and coordinating user interactions for SearchTopBar.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    countApiIsReady: Boolean = false,
    categoryPillText: String = stringResource(R.string.Category),
    projectStatusText: String = stringResource(R.string.Project_status),
    percentageRaisedText: String = stringResource(R.string.Percentage_raised),
    locationText: String = stringResource(R.string.Location),
    amountRaisedText: String = stringResource(R.string.Amount_raised),
    recommendedText: String = stringResource(R.string.Recommended),
    projectsLovedText: String = stringResource(R.string.Projects_We_Love),
    savedProjectsText: String = stringResource(R.string.Saved_projects),
    followingText: String = stringResource(R.string.Following),
    goalText: String = stringResource(R.string.Goal),
    openCallsText: String = stringResource(R.string.fpo_Open_calls),
    isOpenCallsEnabled: Boolean = false,
    isFromDeepLink: Boolean = false,
    onBackPressed: () -> Unit,
    onValueChanged: (String) -> Unit,
    selectedFilterCounts: Map<String, Int>,
    onPillPressedOpensBottomSheet: (FilterRowPillType) -> Unit = {},
    shouldShowPhase: Boolean = true,
    onPillPressedShowOnlyToggles: (FilterRowPillType, Boolean) -> Unit = { a, b -> },
    recommendedStatus: MutableState<Boolean> = remember { mutableStateOf(false) },
    projectsLovedStatus: MutableState<Boolean> = remember { mutableStateOf(false) },
    savedProjects: MutableState<Boolean> = remember { mutableStateOf(false) },
    following: MutableState<Boolean> = remember { mutableStateOf(false) }
) {

    var value by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.backgroundSurfacePrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.searchAppBarHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .testTag(SearchScreenTestTag.BACK_BUTTON.name)
                    .size(dimensions.clickableButtonHeight)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.Back),
                    tint = colors.kds_black
                )
            }

            OutlinedTextField(
                modifier = Modifier
                    .testTag(SearchScreenTestTag.SEARCH_TEXT_INPUT.name)
                    .padding(
                        start = dimensions.appBarSearchPadding,
                        end = dimensions.appBarEndPadding,
                        bottom = dimensions.appBarSearchPadding
                    )
                    .fillMaxSize(),
                value = value,
                onValueChange = {
                    value = it
                    onValueChanged(value)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(
                            modifier = Modifier.fillMaxHeight(),
                            onClick = {
                                value = ""
                                onValueChanged("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(id = R.string.social_buttons_cancel),
                                tint = colors.kds_support_700
                            )
                        }
                    }
                },
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
                    Text(text = stringResource(id = R.string.tabbar_search))
                },
                singleLine = true
            )
        }
        PillBar(
            countApiIsReady = countApiIsReady,
            categoryPillText = categoryPillText,
            projectStatusText = projectStatusText,
            amountRaisedText = amountRaisedText,
            percentageRaisedText = percentageRaisedText,
            locationText = locationText,
            recommendedText = recommendedText,
            projectsLovedText = projectsLovedText,
            savedProjectsText = savedProjectsText,
            followingText = followingText,
            goalText = goalText,
            openCallsText = openCallsText,
            isOpenCallsEnabled = isOpenCallsEnabled,
            isFromDeepLink = isFromDeepLink,
            selectedFilterCounts = selectedFilterCounts,
            onPillPressed = onPillPressedOpensBottomSheet,
            shouldShowPhase = shouldShowPhase,
            onPillPressedShowOnlyToggles = onPillPressedShowOnlyToggles,
            recommendedStatus = recommendedStatus,
            projectsLovedStatus = projectsLovedStatus,
            savedProjects = savedProjects,
            following = following
        )
    }
}

@Composable
fun textForGoalPill(bucket: com.kickstarter.services.DiscoveryParams.GoalBuckets): String = when (bucket) {
    com.kickstarter.services.DiscoveryParams.GoalBuckets.BUCKET_0 -> stringResource(R.string.Goal_pill_bucket_0)
    com.kickstarter.services.DiscoveryParams.GoalBuckets.BUCKET_1 -> stringResource(R.string.Goal_pill_bucket_1)
    com.kickstarter.services.DiscoveryParams.GoalBuckets.BUCKET_2 -> stringResource(R.string.Goal_pill_bucket_2)
    com.kickstarter.services.DiscoveryParams.GoalBuckets.BUCKET_3 -> stringResource(R.string.Goal_pill_bucket_3)
    com.kickstarter.services.DiscoveryParams.GoalBuckets.BUCKET_4 -> stringResource(R.string.Goal_pill_bucket_4)
}

enum class FilterRowPillType {
    SORT,
    CATEGORY,
    FILTER,
    PROJECT_STATUS,
    PERCENTAGE_RAISED,
    LOCATION,
    AMOUNT_RAISED,
    GOAL,
    RECOMMENDED,
    PROJECTS_LOVED,
    SAVED,
    FOLLOWING,
    OPEN_CALLS
}
