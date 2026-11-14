package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSBorderlessButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS,
                text = "Borderless",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSOutlinedButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED,
                text = "Outlined",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSOutlinedDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED_DESTRUCTIVE,
                text = "Outlined Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED_DESTRUCTIVE,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED_DESTRUCTIVE,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.OUTLINED_DESTRUCTIVE,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSBorderlessDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS_DESTRUCTIVE,
                text = "Borderless Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS_DESTRUCTIVE,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS_DESTRUCTIVE,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.BORDERLESS_DESTRUCTIVE,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FBLoginButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .background(colors.kds_white)
                .padding(all = dimensions.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook")
            FBLoginButton(onClickAction = {}, text = "Continue with Facebook", isPressed = true)
        }
    }
}
