package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.ui.activities.compose.search.FilterRowPillType
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * UI component rendering KSIconPillButton.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Composable
fun KSIconPillButton(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit = {},
    type: FilterRowPillType,
    count: Int = 0
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .width(if (count > 0) dimensions.iconPillButtonSizeLarge else dimensions.iconPillButtonSize)
            .border(
                if (isSelected) dimensions.strokeWidth else dimensions.borderThickness,
                if (isSelected) colors.borderActive else colors.borderBold,
                RoundedCornerShape(dimensions.pillButtonShapeSize)
            )
            .size(dimensions.iconPillButtonSize)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterForFilterType(type),
                contentDescription = descriptionForFilterType(type),
                tint = colors.icon
            )
            if (count > 0) {
                Spacer(Modifier.padding(start = dimensions.paddingXSmall))
                KSCountBadge(count)
            }
        }
    }
}

@Composable
fun descriptionForFilterType(type: FilterRowPillType): String {
    return when (type) {
        FilterRowPillType.SORT -> stringResource(R.string.Sort_by)
        FilterRowPillType.FILTER -> stringResource(R.string.Filter)
        else -> stringResource(R.string.Sort_by)
    }
}

@Composable
fun painterForFilterType(type: FilterRowPillType): Painter {
    return when (type) {
        FilterRowPillType.SORT -> painterResource(id = R.drawable.ic_sort)
        FilterRowPillType.FILTER -> painterResource(id = R.drawable.ic_filter)
        else -> painterResource(id = R.drawable.ic_sort)
    }
}

@Composable
fun KSPillButton(
    modifier: Modifier = Modifier,
    countApiIsReady: Boolean = false,
    text: String,
    isSelected: Boolean = false,
    count: Int = 0,
    onClick: () -> Unit,
    iconTrailing: ImageVector = Icons.Filled.KeyboardArrowDown,
    shouldShowTrailingIcon: Boolean = false,
    shouldShowLeadingIcon: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.textAccentGrey
        ),
        border = if (isSelected) BorderStroke(
            dimensions.strokeWidth,
            colors.borderActive
        ) else BorderStroke(dimensions.borderThickness, colors.borderBold),
        shape = RoundedCornerShape(dimensions.pillButtonShapeSize),
        elevation = ButtonDefaults.buttonElevation(dimensions.none, dimensions.none, dimensions.none)
    ) {
        if (shouldShowLeadingIcon) {
            Icon(
                modifier = Modifier.padding(end = dimensions.paddingSmall),
                contentDescription = null,
                painter = painterResource(id = R.drawable.projectswelove),
                tint = Color.Unspecified
            )
        }
        Text(
            modifier = Modifier.padding(end = dimensions.paddingSmall),
            text = text,
            style = typographyV2.buttonLabel,
            color = colors.textAccentGrey
        )
        if (countApiIsReady && count > 0) {
            KSCountBadge(count)
        }
        if (shouldShowTrailingIcon) {
            Icon(
                imageVector = iconTrailing,
                contentDescription = text,
                tint = colors.icon
            )
        }
    }
}
