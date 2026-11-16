package com.kickstarter.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kickstarter.ui.compose.designsystem.KSAlertDialog
import com.kickstarter.ui.compose.designsystem.KSAlertDialogNoHeadline
import com.kickstarter.ui.compose.designsystem.KSErrorRoundedText
import com.kickstarter.ui.compose.designsystem.KSHeadsUpRoundedText
import com.kickstarter.ui.compose.designsystem.KSIntercept
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryGreyButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryWhiteButton
import com.kickstarter.ui.compose.designsystem.KSSuccessRoundedText
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.KsTooltip

/**
 * Visual showcase of design system alert texts, dialogs, tooltips, and intercept sheets.
 *
 * Negative constraints:
 * - Do not invoke directly from production business logic.
 */
@Composable
fun AlertsVisuals() {
    Column {
        Text(text = "Alerts", style = typographyV2.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSErrorRoundedText(text = "This is some sort of error, better do something about it.  Or don't, im just a text box!")

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSHeadsUpRoundedText(text = "Heads up, something is going on that needs your attention.  Maybe its important, maybe its informational.")

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSuccessRoundedText(text = "Hey, something went right and all is good!")

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

        var showNoHeaderDialog by remember { mutableStateOf(false) }

        KSPrimaryGreenButton(
            onClickAction = { showNoHeaderDialog = true },
            text = "Show no header dialog",
            isEnabled = true
        )

        if (showNoHeaderDialog) {
            KSAlertDialogNoHeadline(
                setShowDialog = { showNoHeaderDialog = it },
                bodyText = "This is an example dialog with no header",
                leftButtonText = "Left Button",
                rightButtonText = "Right Button"
            )
        }
        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

        var showHeaderDialog by remember { mutableStateOf(false) }

        KSPrimaryGreenButton(
            onClickAction = { showHeaderDialog = true },
            text = "Show header dialog",
            isEnabled = true
        )

        if (showHeaderDialog) {
            KSAlertDialog(
                setShowDialog = { showHeaderDialog = it },
                headlineText = "Headline Here",
                bodyText = "This is an example dialog with a headline",
                leftButtonText = "Left Button",
                rightButtonText = "Right Button"
            )
        }

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

        var showTooltip by remember { mutableStateOf(false) }

        KSSecondaryWhiteButton(
            onClickAction = { showTooltip = true },
            text = "Show tooltip",
            isEnabled = true
        )

        if (showTooltip) {
            KsTooltip(
                setShowDialog = { showTooltip = it },
                headlineText = "Tooltip header",
                bodyText = "This is the tooltip!  Cool huh?"
            )
        }

        Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))

        var showIntercept by remember { mutableStateOf(false) }

        KSSecondaryGreyButton(
            onClickAction = { showIntercept = true },
            text = "Show Intercept",
            isEnabled = true
        )

        if (showIntercept) {
            KSIntercept(
                setShowDialog = { showIntercept = it },
                bodyText = "This is an intercept message",
                leftButtonText = "Left Button",
                rightButtonText = "Right Button"
            )
        }
    }
}
