package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.libs.Environment
import com.kickstarter.libs.models.Country
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSTheme

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun CheckoutScreenPreview() {
    KSTheme {
        CheckoutScreen(
            rewardsList = (1..6).map { Pair("Cool Item $it", "$20") },
            environment = Environment.builder().build(),
            shippingAmount = 20.0,
            totalAmount = 120.0,
            totalBonusSupport = 0.0,
            currentShippingRule = ShippingRule.builder().build(),
            project = Project.builder().build(),
            email = "some@email.com",
            onPledgeCtaClicked = { _, _ -> },
            newPaymentMethodClicked = {},
            onDisclaimerItemClicked = {},
            onAccountabilityLinkClicked = {}
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun CheckoutScreenIsPlotEnabledPreview() {
    KSTheme {
        CheckoutScreen(
            rewardsList = (1..6).map { Pair("Cool Item $it", "$20") },
            environment = Environment.builder().build(),
            shippingAmount = 20.0,
            totalAmount = 120.0,
            totalBonusSupport = 0.0,
            currentShippingRule = ShippingRule.builder().build(),
            project = Project.builder().country(Country.US.name).currency(Country.US.currencyCode).build(),
            email = "some@email.com",
            onPledgeCtaClicked = { _, _ -> },
            newPaymentMethodClicked = {},
            onDisclaimerItemClicked = {},
            onAccountabilityLinkClicked = {},
            onChangedPaymentMethod = {},
            plotIsVisible = true,
            showPaymentMethodSelection = true,
        )
    }
}
