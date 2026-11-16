package com.kickstarter.features.pledgedprojectsoverview.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import com.kickstarter.R
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsCopyMapper
import com.kickstarter.ui.compose.designsystem.KSAlertDialog
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
fun PPOScreenEmptyState(
    onSeeAllBackedProjectsClick: () -> Unit,
    v2Enabled: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                start = dimensions.paddingMedium,
                end = dimensions.paddingMedium,
                top = dimensions.paddingMedium
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            color = colors.textPrimary,
            text = if (v2Enabled) stringResource(id = R.string.No_funded_backings) else stringResource(id = R.string.Youre_all_caught_up),
            style = typographyV2.headingXL,
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        Text(
            color = colors.textPrimary,
            text = if (v2Enabled) stringResource(id = R.string.When_projects_youve_backed_have_successfully_funded_youll_see_them_here) else stringResource(id = R.string.When_projects_youve_backed_need_your_attention_youll_see_them_here),
            style = typographyV2.body,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        KSPrimaryGreenButton(
            modifier = Modifier,
            onClickAction = { onSeeAllBackedProjectsClick.invoke() },
            text = stringResource(id = R.string.See_all_backed__projects),
            isEnabled = true
        )
    }
}

@Composable
fun PPOScreenErrorState(problem: ProblemDetails? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                start = dimensions.paddingMedium,
                end = dimensions.paddingMedium,
                top = dimensions.paddingMedium
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_alert),
            modifier = Modifier.size(dimensions.iconSizeLarge),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.icon)
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        Text(
            color = colors.textPrimary,
            text = problem?.let {
                "${stringResource(ProblemDetailsCopyMapper.messageRes(it))} ${it.suggestedAction}"
            } ?: stringResource(id = R.string.Something_went_wrong_pull_to_refresh_no_period),
            style = typographyV2.body,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Test tags identifying Compose nodes on the Pledged Projects Overview screen.
 *
 * Negative constraints:
 * - Do not hardcode tag strings in UI tests; reference constants from this enum.
 */
enum class PledgedProjectsOverviewScreenTestTag {
    BACK_BUTTON,
    INFO_BUTTON,
    BOTTOM_SHEET,
    ALERT_COUNT
}

@Composable
fun PPOConfirmAddressDialog(
    isOpen: Boolean,
    confirmedAddress: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (isOpen) {
        KSAlertDialog(
            setShowDialog = { if (!it) onDismiss() },
            headlineText = stringResource(id = R.string.Confirm_your_address),
            bodyText = confirmedAddress,
            leftButtonText = stringResource(id = R.string.Cancel),
            leftButtonAction = onDismiss,
            rightButtonText = stringResource(id = R.string.Confirm),
            rightButtonAction = onConfirm
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PPOBetaMessagingSheet(
    isOpen: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSeeAllBackedProjectsClick: () -> Unit
) {
    if (isOpen) {
        ModalBottomSheet(
            modifier = Modifier.testTag(PledgedProjectsOverviewScreenTestTag.BOTTOM_SHEET.name),
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(
                topStart = dimensions.paddingLarge,
                topEnd = dimensions.paddingLarge
            ),
            containerColor = colors.backgroundSurfacePrimary
        ) {
            BetaMessagingBottomSheet(
                onSeeAllBackedProjectsClick = onSeeAllBackedProjectsClick,
                dismiss = onDismiss
            )
        }
    }
}

@Composable
fun PPOTopBar(
    v2Enabled: Boolean,
    onBackPressed: () -> Unit,
    onInfoClick: () -> Unit
) {
    com.kickstarter.ui.toolbars.compose.TopToolBar(
        title = if (v2Enabled) stringResource(id = R.string.Backings) else stringResource(id = R.string.Project_alerts),
        titleColor = colors.textPrimary,
        leftOnClickAction = onBackPressed,
        leftIconColor = colors.icon,
        leftIconModifier = Modifier.testTag(PledgedProjectsOverviewScreenTestTag.BACK_BUTTON.name),
        backgroundColor = colors.backgroundSurfacePrimary,
        showBetaPill = true,
        right = {
            if (v2Enabled) {
                androidx.compose.material3.IconButton(
                    modifier = Modifier.testTag(PledgedProjectsOverviewScreenTestTag.INFO_BUTTON.name),
                    onClick = onInfoClick,
                    enabled = true
                ) {
                    androidx.compose.foundation.layout.Box {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = R.drawable.ic_info_new),
                            contentDescription = stringResource(
                                id = R.string.general_navigation_accessibility_button_help_menu_label
                            ),
                            tint = colors.kds_black
                        )
                    }
                }
            }
        },
    )
}

