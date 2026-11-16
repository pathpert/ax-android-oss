package com.kickstarter.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kickstarter.ui.activities.compose.search.CustomSwitch
import com.kickstarter.ui.compose.designsystem.KSCheckbox
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSCoralBadge
import com.kickstarter.ui.compose.designsystem.KSFullButtonFooter
import com.kickstarter.ui.compose.designsystem.KSGreenBadge
import com.kickstarter.ui.compose.designsystem.KSHiddenTextInput
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSProjectCardsColumn
import com.kickstarter.ui.compose.designsystem.KSRadioButton
import com.kickstarter.ui.compose.designsystem.KSSmallButtonFooter
import com.kickstarter.ui.compose.designsystem.KSStepper
import com.kickstarter.ui.compose.designsystem.KSStringDropdown
import com.kickstarter.ui.compose.designsystem.KSSwitch
import com.kickstarter.ui.compose.designsystem.KSTextInput
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Visual showcase of badges in design system.
 */
@Composable
fun BadgesVisuals() {
    Column {
        Text(text = "Badges", style = typographyV2.title1Bold, color = colors.kds_support_700)
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        KSGreenBadge(text = "Green Badge")
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        KSCoralBadge(text = "Coral Badge")
    }
}

/**
 * Visual showcase of interactive controls (switches, radio buttons, checkboxes, steppers, dropdowns).
 */
@Composable
fun ControlsVisuals() {
    Column {
        Text(text = "Controls", style = typographyV2.title1Bold, color = colors.kds_support_700)
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row {
            var switch1Checked by remember { mutableStateOf(true) }
            var switch2Checked by remember { mutableStateOf(false) }
            var switch3Checked by remember { mutableStateOf(false) }
            KSSwitch(checked = switch1Checked, onCheckChanged = { switch1Checked = it })
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSSwitch(checked = switch2Checked, onCheckChanged = { switch2Checked = it })
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSSwitch(checked = switch1Checked, onCheckChanged = {}, enabled = false)
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSSwitch(checked = switch2Checked, onCheckChanged = {}, enabled = false)
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            CustomSwitch(checked = switch3Checked, onCheckedChange = { switch3Checked = it })
        }

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row {
            var radioButtonSelected by remember { mutableStateOf(1) }
            KSRadioButton(selected = radioButtonSelected == 1, onClick = { radioButtonSelected = 1 })
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSRadioButton(selected = radioButtonSelected == 2, onClick = { radioButtonSelected = 2 })
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSRadioButton(selected = radioButtonSelected == 3, onClick = { radioButtonSelected = 3 })
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSRadioButton(selected = true, onClick = {}, enabled = false)
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSRadioButton(selected = false, onClick = {}, enabled = false)
        }

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row {
            var checkBox1 by remember { mutableStateOf(false) }
            var checkBox2 by remember { mutableStateOf(false) }
            var checkBox3 by remember { mutableStateOf(false) }
            KSCheckbox(checked = checkBox1, onCheckChanged = { checkBox1 = it })
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSCheckbox(checked = checkBox2, onCheckChanged = { checkBox2 = it })
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSCheckbox(checked = checkBox3, onCheckChanged = { checkBox3 = it })
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSCheckbox(checked = false, onCheckChanged = {}, enabled = false)
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            KSCheckbox(checked = true, onCheckChanged = {}, enabled = false)
        }

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Row(verticalAlignment = Alignment.CenterVertically) {
            var count by remember { mutableStateOf(0) }
            KSStepper(
                onPlusClicked = { count++ },
                isPlusEnabled = count < 10,
                onMinusClicked = { count-- },
                isMinusEnabled = count > 0
            )
            Spacer(modifier = Modifier.width(dimensions.listItemSpacingSmall))
            Text(text = "$$count", style = typographyV2.body)
        }

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSStringDropdown(
            items = arrayOf("Coffee", "Soda", "Water", "Other"),
            onItemSelected = { _, _ -> }
        )
    }
}

/**
 * Visual showcase of text and hidden input fields.
 */
@Composable
fun InputsVisuals() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Inputs", style = typographyV2.title1Bold, color = colors.kds_support_700)
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        val errorText = "ERROR"
        var errorState by remember { mutableStateOf(false) }
        var currentInput by remember { mutableStateOf("") }
        KSTextInput(
            modifier = Modifier.fillMaxWidth(),
            label = "Input Here",
            onValueChanged = { input ->
                errorState = input == errorText
                currentInput = input
            },
            isError = errorState,
            assistiveText = if (errorState) "This is an error!" else "Input ERROR to see an error",
            showAssistiveText = errorState || currentInput.isEmpty()
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSHiddenTextInput(modifier = Modifier.fillMaxWidth(), label = "Password")
    }
}

/**
 * Visual showcase of linear and circular progress indicators.
 */
@Composable
fun ProgressIndicatorsVisuals() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Progress Indicators", style = typographyV2.title1Bold, color = colors.kds_support_700)
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        KSLinearProgressIndicator(Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        KSCircularProgressIndicator()
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        var progress by remember { mutableStateOf(0.0f) }
        KSStepper(
            onPlusClicked = { progress += 0.1f },
            isPlusEnabled = progress < 1f,
            onMinusClicked = { progress -= 0.1f },
            isMinusEnabled = progress > 0f
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        KSLinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = progress)
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        KSCircularProgressIndicator(progress = progress)
    }
}

/**
 * Visual showcase of footer components.
 */
@Composable
fun FootersVisuals() {
    Column {
        Text(text = "Footers", style = typographyV2.title1Bold, color = colors.kds_support_700)
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        KSFullButtonFooter(buttonText = "Back this project", onClickAction = {})
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        KSSmallButtonFooter(
            buttonText = "Manage",
            onClickAction = {},
            titleText = "You're a backer",
            subtitleText = "$24 Committed"
        )
    }
}

/**
 * Visual showcase of project card components.
 */
@Composable
fun ProjectCardVisuals() {
    Column {
        Text(text = "Project Cards", style = typographyV2.title1Bold, color = colors.kds_support_700)
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
        KSProjectCardsColumn()
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))
    }
}
