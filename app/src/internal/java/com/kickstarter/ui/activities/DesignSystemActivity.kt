package com.kickstarter.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.toolbars.compose.TopToolBar
import com.kickstarter.utils.WindowInsetsUtil

/**
 * Internal developer Activity showcasing the design system catalog.
 *
 * Negative constraints:
 * - Do not launch or expose in production release workflows.
 * - Do not mutate global theme state outside this activity.
 */
class DesignSystemActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        WindowInsetsUtil.manageEdgeToEdge(window, rootView)

        setContent {
            val darkMode = remember { mutableStateOf(false) }
            KickstarterApp(useDarkTheme = darkMode.value) {
                DesignSystemView(
                    darkMode = darkMode,
                    onBackClicked = { finish() }
                )
            }
        }
    }
}

/**
 * Preview showcase for the main design system view.
 */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DesignSystemViewPreview() {
    val currentTheme = isSystemInDarkTheme()
    val darkMode = remember { mutableStateOf(currentTheme) }
    KSTheme(
        useDarkTheme = darkMode.value
    ) {
        Column(
            Modifier
                .background(color = colors.backgroundSurfacePrimary)
                .padding(all = dimensions.paddingSmall)
        ) {
            DesignSystemView(
                darkMode = darkMode,
                onBackClicked = {}
            )
        }
    }
}

/**
 * Main scrollable container rendering all design system sections.
 */
@Composable
fun DesignSystemView(darkMode: MutableState<Boolean>, onBackClicked: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            TopToolBar(
                title = "Design System",
                titleColor = colors.kds_black,
                leftIconColor = colors.kds_black,
                leftOnClickAction = onBackClicked,
                right = {
                    IconButton(
                        onClick = { darkMode.value = !darkMode.value },
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sun),
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.imageSizeLarge),
                            colorFilter = ColorFilter.tint(
                                color = colors.kds_black
                            )
                        )
                    }
                },
                backgroundColor = colors.backgroundSurfaceSecondary
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .background(color = colors.backgroundSurfacePrimary)
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) { keyboardController?.hide() },
            contentPadding = PaddingValues(dimensions.paddingSmall)
        ) {
            item {
                AlertsVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                ButtonsVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                NewDesignSystemButtonsVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                BadgesVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                ControlsVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                InputsVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                ProgressIndicatorsVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                FootersVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

                TypographyVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingLarge))

                ClickableText()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingLarge))

                Dividers()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingLarge))

                PillButtonVisuals()
                Spacer(modifier = Modifier.height(dimensions.listItemSpacingLarge))

                ProjectCardVisuals()
            }
        }
    }
}
