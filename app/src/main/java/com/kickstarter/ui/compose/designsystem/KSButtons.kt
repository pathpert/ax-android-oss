package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons // TODO
import androidx.compose.material.icons.filled.Close // TODO
import androidx.compose.material.icons.filled.KeyboardArrowDown // TODO
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.utils.safeLet
import com.kickstarter.ui.activities.compose.search.FilterRowPillType
import com.kickstarter.ui.compose.KSCircleImage
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Bookmark
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Ellipsis
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Forward
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Play
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Rewind
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Share
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * UI component rendering KSPrimaryGreenButton.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Composable
fun KSPrimaryGreenButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    isEnabled: Boolean
) {
    KSButton(
        modifier = modifier,
        leadingIcon = leadingIcon,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.backgroundAccentGreenBold,
        text = text,
        textStyle = textStyle,
        textColor = colors.textInversePrimary
    )
}

@Composable
fun KSPrimaryBlueButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    isEnabled: Boolean
) {
    KSButton(
        modifier = modifier,
        leadingIcon = leadingIcon,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.backgroundAccentBlueBold,
        text = text,
        textStyle = textStyle,
        textColor = colors.textInversePrimary
    )
}

@Composable
fun KSPrimaryBlackButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    isEnabled: Boolean
) {
    KSButton(
        modifier = modifier,
        leadingIcon = leadingIcon,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.backgroundAction,
        text = text,
        textStyle = textStyle,
        textColor = colors.textInversePrimary
    )
}

@Composable
fun KSIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    imageVector: ImageVector = Icons.Filled.Close,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(imageVector = imageVector, contentDescription = contentDescription, tint = colors.icon)
    }
}

@Composable
fun KSOutlinedButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    backgroundColor: Color,
    text: String,
    textColor: Color = colors.textPrimary,
    textStyle: TextStyle = typographyV2.buttonLabel,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    isEnabled: Boolean = true,
) {
    OutlinedButton(
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        enabled = isEnabled,
        border = BorderStroke(dimensions.borderThickness, colors.borderBold),
        contentPadding = contentPadding,
        onClick = { onClickAction.invoke() }
    ) {
        Text(
            style = textStyle,
            color = if (isEnabled) textColor else textColor.copy(alpha = 0.38f),
            text = text
        )
    }
}

@Composable
fun KSButton(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit = {},
    onClickAction: () -> Unit,
    isEnabled: Boolean,
    backgroundColor: Color,
    text: String,
    textStyle: TextStyle = typographyV2.body,
    textColor: Color,
    shape: RoundedCornerShape? = null,
    shouldWrapContentWidth: Boolean = false
) {
    val mod = if (shouldWrapContentWidth)
        modifier.wrapContentWidth()
    else modifier.fillMaxWidth()
    Button(
        modifier = mod
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.buttonElevation(),
        enabled = isEnabled,
        shape = shape ?: shapes.medium
    ) {
        leadingIcon()

        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = if (isEnabled) textColor else colors.textSecondary,
            style = textStyle
        )
    }
}
