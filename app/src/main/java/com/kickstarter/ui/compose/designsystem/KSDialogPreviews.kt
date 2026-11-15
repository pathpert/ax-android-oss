package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.ui.compose.designsystem.KSTheme.colors

/**
 * UI component rendering AlertDialogNoHeadlinePreview.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AlertDialogNoHeadlinePreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_support_400)
                .fillMaxWidth()
        ) {
            val showDialog = remember { mutableStateOf(true) }
            KSAlertDialogNoHeadline(
                setShowDialog = { showDialog.value },
                bodyText = "Alert dialog prompt",
                leftButtonText = "BUTTON",
                rightButtonText = "BUTTON"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AlertDialogPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_support_400)
                .fillMaxWidth()
        ) {
            val showDialog = remember { mutableStateOf(true) }
            KSAlertDialog(
                setShowDialog = { showDialog.value },
                headlineText = "Headline",
                bodyText = "Apparently we had reached a great height in the atmosphere for the...",
                leftButtonText = "BUTTON",
                rightButtonText = "BUTTON"
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TooltipPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_support_400)
                .fillMaxWidth()
        ) {
            val showDialog = remember { mutableStateOf(true) }
            KsTooltip(
                setShowDialog = { showDialog.value },
                headlineText = "Short title",
                bodyText = "This text should be informative copy only. No actions should live in this tooltip.",
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun InterceptPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_support_400)
                .fillMaxWidth()
        ) {
            val showDialog = remember { mutableStateOf(true) }
            KSIntercept(
                setShowDialog = { showDialog.value },
                bodyText = "Take our survey to help us make a better app for you.",
                leftButtonText = "BUTTON",
                rightButtonText = "ACTION"
            )
        }
    }
}

