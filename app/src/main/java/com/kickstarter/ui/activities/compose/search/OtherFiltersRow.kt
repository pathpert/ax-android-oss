package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.activities.compose.search.FilterMenuTestTags.OTHERS_ROW
import com.kickstarter.ui.activities.compose.search.FilterMenuTestTags.switchTag
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSSwitch
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
internal fun OtherFiltersRow(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.Show_only),
    selectedStaffPicked: MutableState<Boolean> = mutableStateOf(false),
    callbackStaffPicked: (Boolean) -> Unit = {},
    selectedStarred: MutableState<Boolean> = mutableStateOf(false),
    callbackStarred: (Boolean) -> Unit = {},
    selectedSocial: MutableState<Boolean> = mutableStateOf(false),
    callbackSocial: (Boolean) -> Unit = {},
    selectedRecommended: MutableState<Boolean> = mutableStateOf(false),
    callbackRecommended: (Boolean) -> Unit = {},
    isUserloggedIn: Boolean,
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
    ) {
        Column(
            modifier = Modifier.testTag(OTHERS_ROW)
        ) {
            Text(
                text = text,
                style = typographyV2.headingLG,
                color = colors.textPrimary
            )

            if (isUserloggedIn) {
                Row(
                    modifier = Modifier.testTag(DiscoveryParams::recommended.name),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        text = stringResource(R.string.Recommended),
                        style = typographyV2.bodyMD,
                        color = colors.textSecondary
                    )

                    KSSwitch(
                        modifier = Modifier.testTag(switchTag(DiscoveryParams::recommended.name)),
                        checked = selectedRecommended.value,
                        onCheckChanged = {
                            selectedRecommended.value = it
                            if (it)
                                callbackRecommended(it)
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.testTag(DiscoveryParams::staffPicks.name),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.Projects_We_Love),
                    style = typographyV2.bodyMD,
                    color = colors.textSecondary
                )

                KSSwitch(
                    modifier = Modifier.testTag(switchTag(DiscoveryParams::staffPicks.name)),
                    checked = selectedStaffPicked.value.isTrue(),
                    onCheckChanged = {
                        selectedStaffPicked.value = it
                        callbackStaffPicked(it)
                    }
                )
            }
            if (isUserloggedIn) {
                Row(
                    modifier = Modifier.testTag(DiscoveryParams::starred.name),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        text = stringResource(R.string.Saved_projects),
                        style = typographyV2.bodyMD,
                        color = colors.textSecondary
                    )

                    KSSwitch(
                        modifier = Modifier.testTag(switchTag(DiscoveryParams::starred.name)),
                        checked = selectedStarred.value,
                        onCheckChanged = {
                            selectedStarred.value = it
                            callbackStarred(it)
                        }
                    )
                }
                Row(
                    modifier = Modifier.testTag(DiscoveryParams::social.name),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        text = stringResource(R.string.Following),
                        style = typographyV2.bodyMD,
                        color = colors.textSecondary
                    )

                    KSSwitch(
                        modifier = Modifier.testTag(switchTag(DiscoveryParams::social.name)),
                        checked = selectedSocial.value.isTrue(),
                        onCheckChanged = {
                            selectedSocial.value = it
                            callbackSocial(it)
                        }
                    )
                }
            }
        }
    }
}
