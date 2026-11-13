package com.kickstarter.features.checkout.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.type.CreditCardTypes
import com.kickstarter.ui.activities.compose.projectpage.components.CHECKOUT_BOTTOM_BAR_BUTTON_TEST_TAG
import com.kickstarter.ui.activities.compose.projectpage.components.CheckoutBottomBar
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.data.PledgeReason
import org.junit.Test

class CheckoutBottomBarTest : KSRobolectricTestCase() {

    @Test
    fun testCheckoutBottomBar_whenCardAcceptedAndEnabled_buttonIsEnabledAndClickable() {
        var clicked = false
        val project = ProjectFactory.project().toBuilder()
            .availableCardTypes(listOf(CreditCardTypes.VISA.name))
            .build()
        val card = StoredCardFactory.visa()

        composeTestRule.setContent {
            KSTheme {
                CheckoutBottomBar(
                    totalAmountString = "$25",
                    project = project,
                    selectedOption = card,
                    isPledgeButtonEnabled = true,
                    isIncrementalPledge = false,
                    pledgeReason = PledgeReason.PLEDGE,
                    email = "backer@kickstarter.com",
                    ksString = null,
                    onPledgeCtaClicked = { _, _ -> clicked = true },
                    onDisclaimerItemClicked = {}
                )
            }
        }

        val button = composeTestRule.onNodeWithTag(CHECKOUT_BOTTOM_BAR_BUTTON_TEST_TAG)
        button.assertIsEnabled()
        button.performClick()
        assertTrue(clicked)
    }

    @Test
    fun testCheckoutBottomBar_whenButtonDisabled_buttonIsNotEnabled() {
        var clicked = false
        val project = ProjectFactory.project().toBuilder()
            .availableCardTypes(listOf(CreditCardTypes.VISA.name))
            .build()
        val card = StoredCardFactory.visa()

        composeTestRule.setContent {
            KSTheme {
                CheckoutBottomBar(
                    totalAmountString = "$25",
                    project = project,
                    selectedOption = card,
                    isPledgeButtonEnabled = false,
                    isIncrementalPledge = false,
                    pledgeReason = PledgeReason.PLEDGE,
                    email = "backer@kickstarter.com",
                    ksString = null,
                    onPledgeCtaClicked = { _, _ -> clicked = true },
                    onDisclaimerItemClicked = {}
                )
            }
        }

        val button = composeTestRule.onNodeWithTag(CHECKOUT_BOTTOM_BAR_BUTTON_TEST_TAG)
        button.assertIsNotEnabled()
    }
}
