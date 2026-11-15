package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSSearchBottomSheetFooter
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

/**
 * Activity displaying and coordinating user interactions for FilterMenuTestTags.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
object FilterMenuTestTags {
    const val SHEET = "filter_menu_sheet"
    const val LIST = "filters_list"
    const val DISMISS_ROW = "dismiss_row"
    const val CATEGORY_ROW = "category_filter_row"
    const val PROJECT_STATUS_ROW = "project_status_row"
    const val PERCENTAGE_RAISED_ROW = "percentage_raised_row"
    const val AMOUNT_RAISED_ROW = "amount_raised_row"
    const val LOCATION_ROW = "location_row"
    const val OTHERS_ROW = "others_row"
    const val GOAL_ROW = "goal_row"
    const val OPEN_CALLS_ROW = "open_calls_row"
    const val FOOTER = "footer"

    fun pillTag(state: DiscoveryParams.State?) = "pill_${state?.name ?: "ALL"}"
    fun switchTag(param: String) = "switch_$param"
}

enum class FilterType {
    CATEGORIES,
    PROJECT_STATUS,
    LOCATION,
    PERCENTAGE_RAISED,
    AMOUNT_RAISED,
    OTHERS,
    GOAL,
    OPEN_CALLS,
}

@Composable
fun FilterMenuSheet(
    modifier: Modifier = Modifier,
    selectedProjectStatus: DiscoveryParams.State? = null,
    availableFilters: List<FilterType> = FilterType.values().toList(),
    onDismiss: () -> Unit = {},
    onApply: (DiscoveryParams.State?, Boolean, Boolean, Boolean, Boolean, Boolean?) -> Unit = { _, _, _, _, _, _ -> },
    onNavigate: (FilterType) -> Unit = {},
    selectedLocation: Location? = null,
    selectedPercentage: DiscoveryParams.RaisedBuckets? = null,
    selectedAmount: DiscoveryParams.AmountBuckets? = null,
    selectedCategory: Category? = null,
    selectedRecommended: MutableState<Boolean> = mutableStateOf(false),
    selectedProjectsLoved: MutableState<Boolean> = mutableStateOf(false),
    selectedSaved: MutableState<Boolean> = mutableStateOf(false),
    selectedSocial: MutableState<Boolean> = mutableStateOf(false),
    selectedGoal: DiscoveryParams.GoalBuckets? = null,
    selectedOpenCallTag: Tag? = null
) {
    val viewModel = LocalFilterMenuViewModel.current
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    val projStatus = remember { mutableStateOf(selectedProjectStatus) }

    Surface(
        modifier = modifier
            .testTag(FilterMenuTestTags.SHEET),
        color = colors.backgroundSurfacePrimary
    ) {
        Column(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary)
        ) {
            FilterRow(
                onClickAction = onDismiss,
                icon = Icons.Filled.Close,
                modifier = Modifier.testTag(FilterMenuTestTags.DISMISS_ROW)
            )

            LazyColumn(modifier = Modifier.weight(1f).testTag(FilterMenuTestTags.LIST)) {
                items(availableFilters) { filter ->
                    when (filter) {
                        FilterType.CATEGORIES -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.CATEGORIES) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.CATEGORY_ROW),
                            subText = selectedCategory?.let { it.name() }
                        )
                        FilterType.PROJECT_STATUS -> ProjectStatusRow(
                            text = titleForFilter(filter),
                            callback = { status ->
                                projStatus.value = status
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            },
                            selectedStatus = projStatus,
                            modifier = Modifier.testTag(FilterMenuTestTags.PROJECT_STATUS_ROW)
                        )
                        FilterType.LOCATION -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.LOCATION) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.LOCATION_ROW),
                            subText = selectedLocation?.displayableName()
                        )
                        FilterType.PERCENTAGE_RAISED -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.PERCENTAGE_RAISED) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW),
                            subText = selectedPercentage?.let { textForBucket(it) }
                        )
                        FilterType.AMOUNT_RAISED -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.AMOUNT_RAISED) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.AMOUNT_RAISED_ROW),
                            subText = selectedAmount?.let { textForBucket(it) }
                        )
                        FilterType.OTHERS -> OtherFiltersRow(
                            selectedStaffPicked = selectedProjectsLoved,
                            selectedRecommended = selectedRecommended,
                            selectedStarred = selectedSaved,
                            selectedSocial = selectedSocial,
                            isUserloggedIn = loggedInUser,
                            callbackRecommended = { recommended ->
                                selectedRecommended.value = recommended
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            },
                            callbackStarred = { starred ->
                                selectedSaved.value = starred
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            },
                            callbackStaffPicked = { staffPicked ->
                                selectedProjectsLoved.value = staffPicked
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            },
                            callbackSocial = { social ->
                                selectedSocial.value = social
                                onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, null)
                            }
                        )
                        FilterType.GOAL -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.GOAL) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.GOAL_ROW),
                            subText = selectedGoal?.let { textForBucket(it) }
                        )
                        FilterType.OPEN_CALLS -> FilterRow(
                            text = titleForFilter(filter),
                            onClickAction = { onNavigate(FilterType.OPEN_CALLS) },
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier.testTag(FilterMenuTestTags.OPEN_CALLS_ROW),
                            subText = selectedOpenCallTag?.name()
                        )
                    }
                }
            }

            KSSearchBottomSheetFooter(
                modifier = Modifier.testTag(FilterMenuTestTags.FOOTER),
                leftButtonIsEnabled = true,
                leftButtonClickAction = {
                    projStatus.value = null
                    selectedRecommended.value = false
                    selectedProjectsLoved.value = false
                    selectedSaved.value = false
                    selectedSocial.value = false
                    onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, false)
                },
                rightButtonOnClickAction = {
                    onApply(projStatus.value, selectedRecommended.value, selectedProjectsLoved.value, selectedSaved.value, selectedSocial.value, true)
                },
                leftButtonText = stringResource(R.string.Reset_all_filters)
            )
        }
    }
}
