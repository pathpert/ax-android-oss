package com.kickstarter.features.pledgedprojectsoverview.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryRedButton
import com.kickstarter.ui.compose.designsystem.KSSwitch
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
fun ConfirmAddressButtonsView(isConfirmButtonEnabled: Boolean, onConfirmAddressClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(PPOCardViewTestTag.CONFIRM_ADDRESS_BUTTONS_VIEW.name)
    ) {
        KSPrimaryGreenButton(
            modifier = Modifier
                .weight(0.5f)
                .padding(
                    start = dimensions.paddingMediumSmall,
                    end = dimensions.paddingMediumSmall,
                    bottom = dimensions.paddingMediumSmall
                ),
            onClickAction = { onConfirmAddressClicked.invoke() },
            text = stringResource(id = R.string.Confirm),
            isEnabled = isConfirmButtonEnabled,
            textStyle = typographyV2.buttonLabel
        )
    }
}

@Composable
fun FixPaymentButtonView(onFixPaymentClicked: () -> Unit) {
    KSSecondaryRedButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onFixPaymentClicked.invoke() },
        text = stringResource(id = R.string.Fix_payment),
        isEnabled = true,
        textStyle = typographyV2.buttonLabel
    )
}

@Composable
fun AuthenticateCardButtonView(onAuthenticateCardClicked: () -> Unit) {
    KSSecondaryRedButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onAuthenticateCardClicked.invoke() },
        text = stringResource(id = R.string.Authenticate_card),
        isEnabled = true,
        textStyle = typographyV2.buttonLabel
    )
}

@Composable
fun TakeSurveyButtonView(onTakeSurveyButtonClick: () -> Unit) {
    KSPrimaryGreenButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onTakeSurveyButtonClick.invoke() },
        text = stringResource(id = R.string.Take_survey),
        isEnabled = true,
        textStyle = typographyV2.buttonLabel
    )
}

@Composable
fun FinalizePledgeButtonView(onFinalizePledgeButtonClick: () -> Unit) {
    KSPrimaryGreenButton(
        modifier = Modifier.padding(
            start = dimensions.paddingMediumSmall,
            end = dimensions.paddingMediumSmall,
            top = dimensions.paddingMediumSmall,
        ),
        onClickAction = { onFinalizePledgeButtonClick.invoke() },
        text = stringResource(id = R.string.Finalize_pledge),
        isEnabled = true,
        textStyle = typographyV2.buttonLabel
    )

    Text(
        modifier = Modifier.padding(
            top = dimensions.paddingSmall,
            start = dimensions.paddingMediumSmall,
            end = dimensions.paddingMediumSmall,
            bottom = dimensions.paddingMediumSmall,
        ),
        text = stringResource(id = R.string.This_may_involve_submitting_a_delivery_address),
        style = typographyV2.bodyXS,
        color = colors.textSecondary
    )
}

@Composable
fun RewardReceivedToggleView(
    checked: Boolean,
    onCheckChanged: ((Boolean) -> Unit),
) {
    var currentSelection by rememberSaveable { mutableStateOf(checked) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimensions.paddingMediumSmall,
                end = dimensions.paddingXSmall
            )
    ) {
        Text(
            text = stringResource(R.string.Reward_delivered),
            style = typographyV2.bodyBoldLG,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.weight(1f))

        KSSwitch(
            modifier = Modifier.padding(end = dimensions.paddingXSmall).testTag(PPOCardViewTestTag.REWARD_RECEIVED_SWITCH.name),
            checked = currentSelection,
            onCheckChanged = {
                currentSelection = it
                onCheckChanged.invoke(it)
            },
            enabled = true,
        )
    }
}

