package com.kickstarter.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSClickableText
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSFacebookButton
import com.kickstarter.ui.compose.designsystem.KSGooglePayButton
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlueButton
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSearchToolbarButtonsColumn
import com.kickstarter.ui.compose.designsystem.KSSecondaryGreyButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryRedButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryWhiteButton
import com.kickstarter.ui.compose.designsystem.KSSmallBlueButton
import com.kickstarter.ui.compose.designsystem.KSSmallRedButton
import com.kickstarter.ui.compose.designsystem.KSSmallWhiteButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Visual showcase of legacy design system buttons, dividers, and clickable texts.
 *
 * Negative constraints:
 * - Do not use in production runtime flows; internal design gallery showcase only.
 */
@Composable
fun ButtonsVisuals() {
    Column {
        Text(text = "Buttons", style = typographyV2.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSPrimaryGreenButton(onClickAction = { }, text = "Primary Green Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSPrimaryBlueButton(onClickAction = { }, text = "Primary Blue Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSPrimaryBlackButton(onClickAction = { }, text = "Primary Black Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSecondaryWhiteButton(
            onClickAction = { },
            text = "Secondary White Button",
            isEnabled = true
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSecondaryGreyButton(onClickAction = { }, text = "Secondary Grey Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSSecondaryRedButton(onClickAction = { }, text = "Secondary Red Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSFacebookButton(onClickAction = { }, text = "Facebook Button", isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSGooglePayButton(onClickAction = { }, isEnabled = true)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row {
            KSSmallBlueButton(onClickAction = {}, text = "SMALL", isEnabled = true)

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingMediumSmall))

            KSSmallRedButton(onClickAction = {}, text = "SMALL", isEnabled = true)

            Spacer(modifier = Modifier.width(dimensions.listItemSpacingMediumSmall))

            KSSmallWhiteButton(onClickAction = {}, text = "SMALL", isEnabled = true)
        }
    }
}

/**
 * Visual showcase of design system clickable text elements.
 */
@Composable
fun ClickableText() {
    Column {
        Text(
            text = "Clickable Text",
            style = typographyV2.title1Bold,
            color = colors.kds_support_700
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSClickableText(
            resourceId = R.string.Learn_about_AI_policy_on_Kickstarter
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
    }
}

/**
 * Visual showcase of design system dividers.
 */
@Composable
fun Dividers() {
    Column {
        Text(text = "Dividers", style = typographyV2.title1Bold, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSDividerLineGrey()

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
    }
}

/**
 * Visual showcase of design system pill buttons.
 */
@Composable
fun PillButtonVisuals() {
    Text(text = "Pill Buttons", style = typographyV2.title1Bold, color = colors.kds_support_700)
    Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
    KSSearchToolbarButtonsColumn()
}
