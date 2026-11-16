package com.kickstarter.ui.activities

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.FBLoginButton
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSButtonType
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Preview showcase for modern design system button variants.
 *
 * Negative constraints:
 * - Internal showcase component; do not reference in production transaction flows.
 */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DesignSystemButtonsV2Preview() {
    val currentTheme = isSystemInDarkTheme()
    val darkMode = remember { mutableStateOf(currentTheme) }
    KSTheme(
        useDarkTheme = darkMode.value
    ) {
        Column(
            Modifier
                .background(color = colors.backgroundSurfacePrimary)
                .padding(all = dimensions.paddingSmall)
        ) {
            NewDesignSystemButtonsVisuals()
        }
    }
}

/**
 * Visual matrix of all new design system button styles across states.
 */
@Composable
fun NewDesignSystemButtonsVisuals() {
    Column {
        Text(
            text = "New Design System Buttons",
            style = typographyV2.title1Bold,
            color = colors.kds_support_700
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        DemoButtonVariant(type = KSButtonType.FILLED, label = "Filled")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        DemoButtonVariant(type = KSButtonType.GREEN, label = "Green")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        DemoButtonVariant(type = KSButtonType.FILLED_INVERTED, label = "Inverted")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        DemoButtonVariant(type = KSButtonType.FILLED_DESTRUCTIVE, label = "Destructive")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        DemoButtonVariant(type = KSButtonType.BORDERLESS, label = "Borderless")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        DemoButtonVariant(type = KSButtonType.OUTLINED, label = "Outlined")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        DemoButtonVariant(type = KSButtonType.OUTLINED_DESTRUCTIVE, label = "Outlined Destructive")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        DemoButtonVariant(type = KSButtonType.BORDERLESS_DESTRUCTIVE, label = "Borderless Destructive")

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        FBLoginButton(
            modifier = Modifier.fillMaxWidth(),
            onClickAction = {},
            text = "Continue with Facebook"
        )
        FBLoginButton(
            modifier = Modifier.fillMaxWidth(),
            onClickAction = {},
            text = "Continue with Facebook",
            isPressed = true
        )
    }
}

@Composable
private fun DemoButtonVariant(type: KSButtonType, label: String) {
    KSButton(
        modifier = Modifier.fillMaxWidth(),
        onClickAction = {},
        type = type,
        text = label,
        imageId = R.drawable.icon_eye_gray
    )
    KSButton(
        modifier = Modifier.fillMaxWidth(),
        onClickAction = {},
        type = type,
        text = "Pressed",
        isPressed = true,
        imageId = R.drawable.icon_eye_gray
    )
    KSButton(
        modifier = Modifier.fillMaxWidth(),
        onClickAction = {},
        type = type,
        text = "Disabled",
        isEnabled = false,
        imageId = R.drawable.icon_eye_gray
    )
    KSButton(
        modifier = Modifier.fillMaxWidth(),
        onClickAction = {},
        type = type,
        text = "Loading",
        isLoading = true,
        imageId = R.drawable.icon_eye_gray
    )
}
