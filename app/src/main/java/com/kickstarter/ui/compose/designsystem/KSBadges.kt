package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
fun KSGreenBadge(
    modifier: Modifier = Modifier,
    text: String,
    leadingIcon: (@Composable (iconTint: Color) -> Unit)? = null,
    iconTint: Color = colors.textAccentGreen,
    textColor: Color = colors.textAccentGreen,
    textStyle: TextStyle = typographyV2.footNoteMedium
) {
    Row(
        modifier
            .background(
                color = colors.backgroundAccentGreenSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        leadingIcon?.invoke(iconTint)
        Text(
            text = text,
            color = textColor,
            style = textStyle
        )
    }
}

@Composable
fun KSCoralBadge(
    leadingIcon: @Composable () -> Unit = {},
    text: String,
    textColor: Color = colors.textSecondary
) {
    Row(
        modifier = Modifier
            .background(
                color = colors.backgroundDangerSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        leadingIcon()

        Text(
            text = text,
            color = textColor,
            style = typographyV2.footNoteMedium
        )
    }
}

@Composable
fun KSAlertBadge(
    icon: ImageVector?,
    message: String?
) {
    if (!message.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .background(
                    color = colors.backgroundDangerSubtle,
                    shape = shapes.small
                )
                .padding(
                    start = dimensions.paddingMediumSmall,
                    top = dimensions.paddingSmall,
                    bottom = dimensions.paddingSmall,
                    end = dimensions.paddingMediumSmall
                )
        ) {
            if (icon != null) {
                Image(
                    modifier = Modifier
                        .padding(end = dimensions.paddingXSmall)
                        .size(dimensions.alertIconSize),
                    imageVector = icon,
                    contentDescription = message,
                    colorFilter = ColorFilter.tint(colors.textAccentRedBold)
                )
            }

            Text(
                text = message,
                color = colors.textAccentRedBold,
                style = typographyV2.footNoteMedium
            )
        }
    }
}

@Composable
fun KSWarningBadge(
    icon: ImageVector?,
    message: String?
) {
    if (!message.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .background(
                    color = colors.backgroundAccentOrangeSubtle,
                    shape = shapes.small
                )
                .padding(
                    start = dimensions.paddingMediumSmall,
                    top = dimensions.paddingSmall,
                    bottom = dimensions.paddingSmall,
                    end = dimensions.paddingMediumSmall
                )
        ) {
            if (icon != null) {
                Image(
                    modifier = Modifier
                        .padding(end = dimensions.paddingXSmall)
                        .size(dimensions.alertIconSize),
                    imageVector = icon,
                    contentDescription = message,
                    colorFilter = ColorFilter.tint(colors.textSecondary)
                )
            }
            Text(
                text = message,
                color = colors.textSecondary,
                style = typographyV2.footNoteMedium
            )
        }
    }
}

@Composable
fun KSSubtleWarningBadge(
    modifier: Modifier = Modifier,
    text: String,
    leadingIcon: (@Composable (iconTint: Color) -> Unit)? = null,
    iconTint: Color = colors.textAccentYellowBold,
    textColor: Color = colors.textAccentYellowBold,
    textStyle: TextStyle = typographyV2.footNoteMedium
) {
    Row(
        modifier
            .background(
                color = colors.backgroundWarningSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        leadingIcon?.invoke(iconTint)
        Text(
            text = text,
            color = textColor,
            style = textStyle
        )
    }
}

@Composable
fun KSBetaBadge() {
    Text(
        modifier = Modifier
            .background(
                color = colors.backgroundAccentGreenSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingXSmall,
                top = dimensions.paddingXSmall,
                bottom = dimensions.paddingXSmall,
                end = dimensions.paddingXSmall
            ),
        text = stringResource(R.string.Beta).uppercase(),
        color = colors.textAccentGreen,
        style = typographyV2.headingXS
    )
}

@Composable
fun KSCountBadge(
    count: Int
) {
    Box(
        modifier = Modifier
            .background(colors.backgroundAccentGraySubtle, RoundedCornerShape(50))
            .padding(horizontal = dimensions.radiusSmall, vertical = dimensions.strokeWidth),
        contentAlignment = Alignment.Center
    ) {
        Text(text = count.toString(), color = colors.textAccentGrey, fontSize = 12.sp)
    }
}

@Composable
fun KSSecretRewardBadge(
    modifier: Modifier = Modifier,
    iconTint: Color = colors.textAccentGreenBold
) {
    KSGreenBadge(
        modifier = modifier,
        text = stringResource(R.string.Secret_reward),
        textColor = colors.textAccentGreenBold,
        iconTint = iconTint,
        leadingIcon = { tint ->
            Image(
                modifier = Modifier
                    .padding(end = dimensions.paddingXSmall)
                    .size(dimensions.alertIconSize),
                imageVector = secretRewardLock,
                contentDescription = stringResource(R.string.Secret_reward),
                colorFilter = ColorFilter.tint(tint)
            )
        },
        textStyle = typographyV2.headingSM
    )
}

@Composable
fun KSFeaturedRewardBadge(
    modifier: Modifier = Modifier,
    iconTint: Color = purple_08
) {
    Row(
        modifier
            .background(
                color = purple_02,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        Image(
            modifier = Modifier
                .padding(end = dimensions.paddingXSmall)
                .size(dimensions.alertIconSize),
            imageVector = featuredRewardStar,
            contentDescription = stringResource(R.string.Featured),
            colorFilter = ColorFilter.tint(iconTint)
        )
        Text(
            text = stringResource(R.string.Featured),
            color = purple_08,
            style = typographyV2.headingSM
        )
    }
}
