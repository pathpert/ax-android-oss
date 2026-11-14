package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
fun KSSecondaryGreyButton(
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
        backgroundColor = colors.backgroundAccentGrayBold,
        text = text,
        textStyle = textStyle,
        textColor = colors.textPrimary
    )
}

@Composable
fun KSSecondaryRedButton(
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
        backgroundColor = colors.backgroundDangerBold,
        text = text,
        textStyle = textStyle,
        textColor = colors.textInversePrimary
    )
}

@Composable
fun KSSecondaryWhiteButton(
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
        backgroundColor = colors.backgroundSurfacePrimary,
        text = text,
        textStyle = textStyle,
        textColor = colors.textAccentGreenBold
    )
}

@Composable
fun KSFacebookButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean
) {
    KSIconTextButton(
        modifier = modifier,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.facebook_blue,
        imageId = R.drawable.com_facebook_button_icon,
        text = text,
        textColor = colors.kds_white
    )
}

@Composable
fun KSGooglePayButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    isEnabled: Boolean
) {
    KSIconTextButton(
        modifier = modifier,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.kds_black,
        imageId = R.drawable.googlepay_button_content
    )
}

@Composable
fun KSIconTextButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    isEnabled: Boolean,
    backgroundColor: Color,
    imageId: Int,
    imageContentDescription: String? = null,
    text: String? = null,
    textColor: Color? = null,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.buttonElevation(),
        enabled = isEnabled,
        shape = shapes.medium
    ) {
        Row {
            Image(
                modifier = Modifier.defaultMinSize(
                    minHeight = dimensions.imageSizeMedium,
                    minWidth = dimensions.imageSizeMedium
                ),
                painter = painterResource(id = imageId),
                contentDescription = imageContentDescription
            )

            if (text != null && textColor != null) {
                Spacer(modifier = Modifier.width(dimensions.listItemSpacingMediumSmall))

                Text(
                    text = text,
                    color = if (isEnabled) textColor else colors.textAccentGrey,
                    style = typographyV2.body
                )
            }
        }
    }
}

@Composable
fun KSSmallBlueButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean,
    radius: Dp? = null
) {
    KSSmallButton(
        modifier = modifier,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.backgroundAccentBlueBold,
        text = text,
        textColor = colors.textInversePrimary,
        radius = radius
    )
}

@Composable
fun KSSmallRedButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean,
    radius: Dp? = null
) {
    KSSmallButton(
        modifier = modifier,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.backgroundDangerBold,
        text = text,
        textColor = colors.textInversePrimary,
        radius = radius
    )
}

@Composable
fun KSSmallWhiteButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    text: String,
    isEnabled: Boolean,
    radius: Dp? = null
) {
    KSSmallButton(
        modifier = modifier,
        onClickAction = onClickAction,
        isEnabled = isEnabled,
        backgroundColor = colors.backgroundInverse,
        text = text,
        textColor = colors.textAccentGreenBold,
        radius = radius
    )
}

@Composable
fun KSSmallButton(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit,
    isEnabled: Boolean,
    backgroundColor: Color,
    text: String,
    textColor: Color,
    radius: Dp? = null
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = colors.backgroundActionDisabled
        ),
        onClick = { onClickAction.invoke() },
        elevation = ButtonDefaults.buttonElevation(),
        enabled = isEnabled,
        shape = radius?.let { RoundedCornerShape(radius) } ?: shapes.medium
    ) {
        Text(
            text = text,
            color = if (isEnabled) textColor else colors.textAccentGrey,
            style = typographyV2.buttonLabel
        )
    }
}
