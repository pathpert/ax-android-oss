package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

/**
 * UI component rendering KSPrimaryButtonsPreview.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSPrimaryButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.backgroundSurfacePrimary)
                .padding(all = dimensions.paddingSmall)
        ) {
            KSPrimaryGreenButton(
                onClickAction = { },
                isEnabled = true,
                text = "Back this project"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSPrimaryBlueButton(
                onClickAction = { },
                isEnabled = true,
                text = "Manage your pledge"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSPrimaryBlackButton(
                onClickAction = { },
                isEnabled = true,
                text = "Share this project"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSSecondaryButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.backgroundSurfacePrimary)
                .padding(all = dimensions.paddingSmall)
        ) {
            KSSecondaryGreyButton(
                onClickAction = { },
                isEnabled = true,
                text = "Fix your pledge"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSecondaryWhiteButton(
                onClickAction = { },
                isEnabled = true,
                text = "Back this project"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSecondaryRedButton(
                onClickAction = { },
                isEnabled = true,
                text = "Cancel my pledge"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSDisabledButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.backgroundSurfacePrimary)
                .padding(all = dimensions.paddingSmall)
        ) {
            KSPrimaryGreenButton(
                onClickAction = { },
                isEnabled = false,
                text = "Back this project"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSOtherButtonsPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.backgroundSurfacePrimary)
                .padding(all = dimensions.paddingSmall)
        ) {
            KSFacebookButton(
                onClickAction = { },
                text = "Continue with Facebook",
                isEnabled = true
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSGooglePayButton(onClickAction = { }, isEnabled = true)
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSSmallButtonsPreview() {
    KSTheme {
        Column {
            KSSmallBlueButton(onClickAction = {}, text = "BLUE", isEnabled = true)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSmallRedButton(onClickAction = {}, text = "RED", isEnabled = true)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSmallWhiteButton(onClickAction = {}, text = "WHITE", isEnabled = true)
        }
    }
}
