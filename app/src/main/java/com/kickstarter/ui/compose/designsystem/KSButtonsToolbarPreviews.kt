package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.activities.compose.search.FilterRowPillType
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Bookmark
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Ellipsis
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Forward
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Play
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Rewind
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Share

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSSearchToolbarButtons() {
    KSTheme {
        KSSearchToolbarButtonsColumn()
    }
}

@Composable
fun KSSearchToolbarButtonsColumn() {
    Column(
        modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
    ) {
        Row {
            KSIconButton(onClick = {}, imageVector = Icons.Filled.Close)
        }
        Row {
            KSIconPillButton(type = FilterRowPillType.SORT, isSelected = false)
            KSIconPillButton(type = FilterRowPillType.SORT, isSelected = true)
            KSIconPillButton(type = FilterRowPillType.FILTER, isSelected = true)
            KSIconPillButton(type = FilterRowPillType.FILTER, isSelected = false)
        }
        Row {
            KSIconPillButton(type = FilterRowPillType.SORT, isSelected = true, count = 3)
            KSIconPillButton(type = FilterRowPillType.SORT, isSelected = false, count = 3)
            KSIconPillButton(type = FilterRowPillType.FILTER, isSelected = true, count = 3)
            KSIconPillButton(type = FilterRowPillType.FILTER, isSelected = false, count = 3)
        }
        Row {
            KSPillButton(countApiIsReady = false, text = "Category", isSelected = false, count = 0, onClick = {})
            KSPillButton(countApiIsReady = false, text = "Art", isSelected = true, count = 0, onClick = {})
        }
        Row {
            KSPillButton(countApiIsReady = true, text = "Late Pledges", isSelected = false, count = 30, onClick = {}, shouldShowTrailingIcon = false)
            KSPillButton(countApiIsReady = true, text = "Late Pledges", isSelected = true, count = 30, onClick = {}, shouldShowTrailingIcon = false)
        }
        Row {
            KSPillButton(countApiIsReady = false, text = "Late Pledges", isSelected = true, count = 0, onClick = {}, shouldShowTrailingIcon = false)
            KSPillButton(countApiIsReady = false, text = "Late Pledges", isSelected = false, count = 0, onClick = {}, shouldShowTrailingIcon = false)
        }
        Row {
            KSPillButton(countApiIsReady = false, text = "Late Pledges", isSelected = true, count = 0, onClick = {}, shouldShowTrailingIcon = true)
            KSPillButton(countApiIsReady = false, text = "Late Pledges", isSelected = false, count = 0, onClick = {}, shouldShowTrailingIcon = true)
        }
        Row {
            KSPillButton(countApiIsReady = false, text = "Projects We Love", isSelected = true, count = 0, onClick = {}, shouldShowLeadingIcon = true)
            KSPillButton(countApiIsReady = false, text = "Projects We Love", isSelected = false, count = 0, onClick = {}, shouldShowLeadingIcon = true)
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSOutlinedButtons() {
    KSTheme {
        Column(
            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
        ) {
            KSOutlinedButton(
                backgroundColor = colors.backgroundSurfacePrimary,
                textColor = colors.textPrimary,
                onClickAction = {},
                text = "OutlinedButton",
                isEnabled = true
            )

            KSOutlinedButton(
                backgroundColor = colors.backgroundSurfacePrimary,
                textColor = colors.textPrimary,
                onClickAction = {},
                text = "OutlinedButton Disabled",
                isEnabled = false
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSPlayerControlButtonsPreview() {
    KSTheme {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.pointerInput(Unit) {} // Stops click propagation
        ) {
            KSControlIcon(
                icon = Rewind,
                size = 36.dp,
                onClick = {
                }
            )

            KSControlIcon(
                icon = Play,
                size = 62.dp,
                onClick = {
                }
            )

            KSControlIcon(
                icon = Forward,
                size = 36.dp,
                onClick = {
                }
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSVideoPlayerButtonsPreview() {
    KSTheme {
        Column(
            modifier = Modifier
                .background(colors.backgroundSurfaceRaised.copy(alpha = 0.5f))
                .padding(dimensions.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
        ) {
            KSProfileButton(
                imageUrl = "https://www.kickstarter.com/assets/default/user_default-738555160848037617b84803d360098f99.png",
                onClick = {}
            )

            KSVideoPlayerIconButton(
                icon = Bookmark,
                text = "1k",
                onClick = {},
                contentDescription = "Bookmark"
            )

            KSVideoPlayerIconButton(
                icon = Share,
                text = "50",
                onClick = {},
                contentDescription = "Share"
            )

            KSVideoPlayerIconButton(
                icon = Ellipsis,
                onClick = {},
                contentDescription = "More options"
            )
        }
    }
}
