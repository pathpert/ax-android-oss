package com.kickstarter.features.pledgedprojectsoverview.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.kickstarter.features.pledgedprojectsoverview.data.Flag
import com.kickstarter.features.pledgedprojectsoverview.extensions.isTier1Alert
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.shapes

enum class PPOCardViewType {
    CONFIRM_ADDRESS,
    FIX_PAYMENT,
    AUTHENTICATE_CARD,
    OPEN_SURVEY,
    PLEDGE_COLLECTED_NO_REWARD,
    PLEDGE_COLLECTED_REWARD,
    SURVEY_SUBMITTED_DIGITAL,
    SURVEY_SUBMITTED_NO_REWARD,
    SURVEY_SUBMITTED_SHIPPABLE,
    ADDRESS_CONFIRMED,
    AWAITING_REWARD,
    PLEDGE_MANAGEMENT,
    REWARD_RECEIVED,
    UNKNOWN,
}

enum class PPOCardViewTestTag {
    SHIPPING_ADDRESS_VIEW,
    CONFIRM_ADDRESS_BUTTONS_VIEW,
    FlAG_LIST_VIEW,
    REWARD_RECEIVED_SWITCH,
    ALERT_NOTIFICATION_DOT
}

@Composable
fun PPOCardView(
    viewType: PPOCardViewType,
    onCardClick: () -> Unit,
    onProjectPledgeSummaryClick: () -> Unit,
    projectName: String? = null,
    pledgeAmount: String? = null,
    imageUrl: String? = null,
    imageContentDescription: String? = null,
    creatorName: String? = null,
    sendAMessageClickAction: () -> Unit,
    shippingAddress: String? = null,
    onActionButtonClicked: () -> Unit,
    onSecondaryActionButtonClicked: () -> Unit,
    flags: List<Flag?>? = null,
    onRewardReceivedChanged: ((Boolean) -> Unit) = {},
    rewardReceived: Boolean = false,
) {

    BadgedBox(
        badge = {
            if (viewType.isTier1Alert() || viewType == PPOCardViewType.PLEDGE_MANAGEMENT) {
                Badge(
                    modifier = Modifier.testTag(PPOCardViewTestTag.ALERT_NOTIFICATION_DOT.name),
                    containerColor = colors.backgroundDangerBold
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = shapes.small,
            colors = CardDefaults.cardColors(
                containerColor = colors.backgroundSurfacePrimary,
                contentColor = colors.backgroundSurfacePrimary
            ),
            border = BorderStroke(width = dimensions.borderThickness, color = colors.borderSubtle),
        ) {
            Column(
                Modifier.clickable { onCardClick.invoke() }
            ) {
                if (!flags.isNullOrEmpty()) {
                    AlertFlagsView(flags = flags)
                }

                ProjectPledgeSummaryView(
                    projectName = projectName,
                    pledgeAmount = pledgeAmount,
                    imageUrl = imageUrl,
                    imageContentDescription = imageContentDescription,
                    onProjectPledgeSummaryClick = onProjectPledgeSummaryClick
                )

                CreatorNameSendMessageView(
                    creatorName = creatorName,
                    sendAMessageClickAction = sendAMessageClickAction
                )

                when (viewType) {
                    PPOCardViewType.CONFIRM_ADDRESS,
                    PPOCardViewType.ADDRESS_CONFIRMED,
                    PPOCardViewType.SURVEY_SUBMITTED_SHIPPABLE, -> {
                        Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                        ShippingAddressView(
                            shippingAddress = shippingAddress,
                            onEditAddressClicked = onSecondaryActionButtonClicked
                        )
                    }
                    else -> {}
                }

                if (viewType == PPOCardViewType.SURVEY_SUBMITTED_SHIPPABLE) {
                    KSDividerLineGrey()
                }

                when (viewType) {
                    PPOCardViewType.CONFIRM_ADDRESS -> ConfirmAddressButtonsView(!shippingAddress.isNullOrEmpty(), onActionButtonClicked)
                    PPOCardViewType.FIX_PAYMENT -> FixPaymentButtonView(onActionButtonClicked)
                    PPOCardViewType.AUTHENTICATE_CARD -> AuthenticateCardButtonView(onActionButtonClicked)
                    PPOCardViewType.OPEN_SURVEY -> TakeSurveyButtonView(onActionButtonClicked)
                    PPOCardViewType.PLEDGE_MANAGEMENT -> FinalizePledgeButtonView(onActionButtonClicked)
                    PPOCardViewType.UNKNOWN -> {}
                    else -> {}
                }

                when (viewType) {
                    PPOCardViewType.PLEDGE_COLLECTED_REWARD,
                    PPOCardViewType.SURVEY_SUBMITTED_DIGITAL,
                    PPOCardViewType.SURVEY_SUBMITTED_SHIPPABLE,
                    PPOCardViewType.AWAITING_REWARD,
                    PPOCardViewType.REWARD_RECEIVED,
                    -> RewardReceivedToggleView(checked = rewardReceived, onCheckChanged = onRewardReceivedChanged)
                    else -> {}
                }
            }
        }
    }
}
