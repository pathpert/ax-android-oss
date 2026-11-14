package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.KSCircleImage
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Glassmorphism-style icon button that blurs the content behind it.
 *
 * When [hazeState] is provided (from a parent [hazeSource]), the button uses a real
 * background blur via the Haze library (API 31+, with tinted-overlay fallback on older devices).
 * When [hazeState] is null, it falls back to a simple semi-transparent tinted overlay.
 */
@Composable
fun KSControlIcon(
    icon: ImageVector,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    contentDescription: String? = null,
    onClickLabel: String? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                onClickLabel = onClickLabel,
                role = Role.Button
            )
    ) {
        val baseColor = colors.videoPlayer.background.copy(alpha = 0.25f)
        val glassModifier = if (hazeState != null) {
            Modifier
                .matchParentSize()
                .hazeEffect(state = hazeState) {
                    blurRadius = 28.dp
                    noiseFactor = 0.05f
                    backgroundColor = baseColor
                    tints = listOf(HazeTint(baseColor))
                }
        } else {
            Modifier
                .matchParentSize()
                .background(colors.videoPlayer.background.copy(alpha = 0.25f))
        }

        Box(modifier = glassModifier)

        // Border: #FFFFFF at 25%, 1.38px
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.38.dp,
                    color = colors.videoPlayer.content.copy(alpha = 0.25f),
                    shape = CircleShape
                )
        )

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.videoPlayer.content,
        )
    }
}

@Composable
fun KSProfileButton(
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClickLabel: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(dimensions.videoPlayerIconSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onClickLabel = onClickLabel,
                role = Role.Button
            )
    ) {
        KSCircleImage(
            url = imageUrl,
            modifier = Modifier.fillMaxSize(),
            contentDescription = contentDescription
        )
    }
}

@Composable
fun KSVideoPlayerIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconTint: Color = Color.Unspecified,
    text: String? = null,
    textIsDecorative: Boolean = false,
    contentDescription: String? = null,
    onClickLabel: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
                onClickLabel = onClickLabel,
                role = Role.Button
            )
    ) {
        Image(
            imageVector = icon,
            contentDescription = contentDescription,
            colorFilter = if (iconTint != Color.Unspecified) ColorFilter.tint(iconTint) else null,
            modifier = Modifier
                .size(dimensions.videoPlayerIconSize)
                .dropShadow(
                    shape = CircleShape,
                    shadow = Shadow(
                        radius = dimensions.videoPlayerShadowBlur,
                        color = colors.videoPlayer.iconShadow,
                        offset = DpOffset.Zero
                    )
                )
        )
        if (!text.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
            Text(
                modifier = if (textIsDecorative) Modifier.clearAndSetSemantics { } else Modifier,
                text = text,
                color = colors.videoPlayer.content,
                style = typographyV2.bodyBoldSM.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = colors.videoPlayer.iconShadow,
                        offset = Offset(0f, 0f),
                        blurRadius = dimensions.videoPlayerShadowBlur.value * 2f
                    )
                )
            )
        }
    }
}
