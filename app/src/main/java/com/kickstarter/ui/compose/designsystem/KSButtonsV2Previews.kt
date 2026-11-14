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
fun KSFilledButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(color = colors.backgroundSurfacePrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED,
                text = "Filled",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED,
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
fun KSGreenButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(color = colors.backgroundSurfacePrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.GREEN,
                text = "Green",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.GREEN,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.GREEN,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.GREEN,
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
fun KSFilledInvertedButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(color = colors.backgroundSurfacePrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_INVERTED,
                text = "Inverted",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_INVERTED,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_INVERTED,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_INVERTED,
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
fun KSFilledDestructiveButtonPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(all = dimensions.paddingSmall)
                .fillMaxWidth()
                .background(color = colors.backgroundSurfacePrimary),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_DESTRUCTIVE,
                text = "Destructive",
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_DESTRUCTIVE,
                text = "Pressed",
                isPressed = true,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_DESTRUCTIVE,
                text = "Disabled",
                isEnabled = false,
                imageId = R.drawable.icon_eye_gray
            )
            KSButton(
                onClickAction = {},
                type = KSButtonType.FILLED_DESTRUCTIVE,
                text = "Loading",
                isLoading = true,
                imageId = R.drawable.icon_eye_gray
            )
        }
    }
}
