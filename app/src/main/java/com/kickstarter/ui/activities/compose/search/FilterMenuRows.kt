package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSPillButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Activity displaying and coordinating user interactions for ProjectStatusRow.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProjectStatusRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Filter),
    selectedStatus: MutableState<DiscoveryParams.State?> = mutableStateOf(null),
    callback: (DiscoveryParams.State?) -> Unit = {},
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    var switchChecked by remember { mutableStateOf(false) }

    val pillOptions = listOf(
        null to stringResource(R.string.Project_status_all),
        DiscoveryParams.State.LIVE to stringResource(R.string.Project_status_live),
        DiscoveryParams.State.LATE_PLEDGES to stringResource(R.string.Project_status_late_pledge),
        DiscoveryParams.State.UPCOMING to stringResource(R.string.Project_status_upcoming),
        DiscoveryParams.State.SUCCESSFUL to stringResource(R.string.Project_status_successful)
    )

    Row(
        modifier = modifier
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
                start = dimensions.paddingLarge,
                top = dimensions.paddingLarge,
                bottom = dimensions.paddingLarge,
                end = dimensions.paddingMediumSmall
            )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(KSTheme.dimensions.listItemSpacingSmall)) {
            Text(
                text = text,
                style = typographyV2.headingLG,
                color = colors.textPrimary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pillOptions.forEach { (state, label) ->
                    KSPillButton(
                        text = label,
                        isSelected = selectedStatus.value == state,
                        modifier = Modifier.testTag(FilterMenuTestTags.pillTag(state)),
                        onClick = {
                            val newSelection = if (selectedStatus.value == state) null else state
                            selectedStatus.value = newSelection
                            callback(newSelection)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.height(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.Include_ended_projects),
                    style = typographyV2.body,
                    color = colors.textPrimary
                )

                CustomSwitch(
                    checked = switchChecked,
                    onCheckedChange = {
                        switchChecked = it
                        if (switchChecked) {
                            callback(DiscoveryParams.State.SUCCESSFUL)
                        }
                    }
                )
            }
        }
    }
}

@Composable
internal fun FilterRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Filter),
    onClickAction: () -> Unit,
    icon: ImageVector,
    subText: String? = null
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions
    Row(
        modifier = modifier
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
                start = dimensions.paddingLarge,
                top = dimensions.paddingLarge,
                bottom = dimensions.paddingLarge,
                end = dimensions.paddingMediumSmall
            )
            .clickable { onClickAction.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val style = if (text == stringResource(R.string.Filter)) {
            typographyV2.headingXL
        } else {
            typographyV2.headingLG
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                style = style,
                color = colors.textPrimary
            )

            if (subText != null) {
                Text(
                    modifier = Modifier.padding(
                        top = dimensions.paddingSmall
                    ),
                    text = subText,
                    style = typographyV2.bodyMD,
                    color = colors.textSecondary
                )
            }
        }

        KSIconButton(
            modifier = Modifier.testTag(text),
            onClick = {
                onClickAction.invoke()
            },
            imageVector = icon
        )
    }
}

@Composable
internal fun titleForFilter(filter: FilterType): String {
    return when (filter) {
        FilterType.CATEGORIES -> stringResource(R.string.Category)
        FilterType.PROJECT_STATUS -> stringResource(R.string.Project_status)
        FilterType.LOCATION -> stringResource(R.string.Location)
        FilterType.PERCENTAGE_RAISED -> stringResource(R.string.Percentage_raised)
        FilterType.AMOUNT_RAISED -> stringResource(R.string.Amount_raised)
        FilterType.GOAL -> stringResource(R.string.Goal)
        FilterType.OTHERS -> stringResource(R.string.Show_only)
        FilterType.OPEN_CALLS -> stringResource(R.string.fpo_Open_calls)
    }
}
