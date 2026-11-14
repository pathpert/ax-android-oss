package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

// Snackbars
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SnackbarsPreview() {
    KSTheme {
        Column {
            KSErrorRoundedText(text = "This is some sort of error, better do something about it.  Or don't, im just a text box!")
            Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))
            KSHeadsUpRoundedText(text = "Heads up, something is going on that needs your attention.  Maybe its important, maybe its informational.")
            Spacer(Modifier.height(dimensions.listItemSpacingMediumSmall))
            KSSuccessRoundedText(text = "Hey, something went right and all is good!")
        }
    }
}

enum class KSSnackbarTypes {
    KS_ERROR,
    KS_HEADS_UP,
    KS_SUCCESS
}

@Composable
fun KSErrorSnackbar(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.none)
) {
    Snackbar(
        containerColor = colors.backgroundDangerBold,
        content = {
            KSErrorRoundedText(text = text, padding = padding)
        }
    )
}

@Composable
fun KSHeadsupSnackbar(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.none)
) {
    Snackbar(
        containerColor = colors.backgroundActionPressed,
        content = {
            KSHeadsUpRoundedText(text = text, padding = padding)
        }
    )
}

@Composable
fun KSSuccessSnackbar(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.none)
) {
    Snackbar(
        containerColor = colors.backgroundAccentGreenSubtle,
        content = {
            KSSuccessRoundedText(text = text, padding = padding)
        }
    )
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSVideoFeedSnackbarPreview() {
    KSTheme {
        KSVideoFeedSnackbar(text = "Couldn't load video")
    }
}

@Composable
fun KSVideoFeedSnackbar(
    text: String,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val baseColor = Color(0xFF2B2B2D).copy(alpha = 0.45f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.radiusMedium))
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState) {
                        blurRadius = 28.dp
                        noiseFactor = 0.05f
                        backgroundColor = baseColor
                        tints = listOf(HazeTint(baseColor))
                    }
                } else {
                    Modifier.background(baseColor)
                }
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(dimensions.radiusMedium)
            )
            .padding(all = dimensions.paddingMedium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = typographyV2.bodyMD,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun KSRoundedPaddedText(
    background: Color,
    textColor: Color,
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.paddingMedium)
) {
    Text(
        modifier = Modifier
            .background(background, shape = shapes.small)
            .fillMaxWidth()
            .padding(padding),
        text = text,
        color = textColor,
    )
}

@Composable
fun KSErrorRoundedText(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.paddingMedium)
) {
    KSRoundedPaddedText(
        background = colors.backgroundDangerBold,
        textColor = colors.textInversePrimary,
        text = text,
        padding = padding
    )
}

@Composable
fun KSHeadsUpRoundedText(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.paddingMedium)
) {
    KSRoundedPaddedText(
        background = colors.backgroundActionPressed,
        textColor = colors.textInversePrimary,
        text = text,
        padding = padding
    )
}

@Composable
fun KSSuccessRoundedText(
    text: String,
    padding: PaddingValues = PaddingValues(dimensions.paddingMedium)
) {
    KSRoundedPaddedText(
        background = colors.backgroundAccentGreenSubtle,
        textColor = colors.textPrimary,
        text = text,
        padding = padding
    )
}
