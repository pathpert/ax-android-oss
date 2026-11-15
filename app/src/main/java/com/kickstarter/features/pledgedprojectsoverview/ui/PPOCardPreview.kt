package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.features.pledgedprojectsoverview.data.Flag
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

/**
 * Feature component for PPOCardPreview in pledgedprojectsoverview module.
 *
 * Negative constraints:
 * - Do not bypass feature-scoped boundary contracts or leak internal UI state.
 */
@Composable
@Preview(showSystemUi = true, showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showSystemUi = true, showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PPOCardPreview() {
    KSTheme {
        LazyColumn(
            Modifier
                .background(color = colors.backgroundSurfacePrimary),
            contentPadding = PaddingValues(dimensions.paddingMedium)
        ) {
            item {
                PPOCardView(
                    viewType = PPOCardViewType.AWAITING_REWARD,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Awaiting reward type").type("info").icon(null).build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.SURVEY_SUBMITTED_SHIPPABLE,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Survey submitted shippable").type("info").icon(null).build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.SURVEY_SUBMITTED_DIGITAL,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Survey Submitted Digital type").type("info").icon(null).build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.SURVEY_SUBMITTED_SHIPPABLE,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    shippingAddress = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",

                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Survey Submitted Shippable type").type("info").icon(null).build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }
            item {
                PPOCardView(
                    viewType = PPOCardViewType.PLEDGE_MANAGEMENT,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Finalize pledge").type("warning").icon("time").build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.PLEDGE_COLLECTED_REWARD,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Pledge collected reward type").type("info").icon(null).build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.PLEDGE_COLLECTED_NO_REWARD,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Pledge collected no reward type").type("info").icon(null).build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.REWARD_RECEIVED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Reward Received type").type("info").icon(null).build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.ADDRESS_CONFIRMED,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    shippingAddress = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Address confirmed type").type("warning").icon("time").build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.CONFIRM_ADDRESS,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    shippingAddress = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Confirm address").type("info").icon(null).build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }
            item {
                PPOCardView(
                    viewType = PPOCardViewType.FIX_PAYMENT,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address").type("warning").icon("time").build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.AUTHENTICATE_CARD,
                    onCardClick = { },
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$60.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.OPEN_SURVEY,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address").type("warning").icon("time").build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }
        }
    }
}

