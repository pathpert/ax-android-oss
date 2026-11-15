package com.kickstarter.ui.views.compose.checkout

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme

/**
 * UI component rendering ItemizedContainerForNoRewardPreview.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ItemizedContainerForNoRewardPreview() {
    KSTheme {
        Scaffold(
            containerColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            ItemizedRewardListContainer(
                modifier = Modifier.padding(paddingValues = padding),
                totalAmount = "US$ 1",
                totalAmountCurrencyConverted = "About CA$ 1.38",
                initialBonusSupport = "US$ 0",
                totalBonusSupport = "US$ 1",
                shippingAmount = -1.0,
                plotSelected = false
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ItemizedRewardListContainerPreview() {
    KSTheme {
        Scaffold(
            containerColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            ItemizedRewardListContainer(
                modifier = Modifier
                    .padding(paddingValues = padding),
                ksString = null,
                rewardsList = emptyList<Pair<String, String>>(),
                shippingAmount = 20.0,
                shippingAmountString = "",
                initialShippingLocation = "",
                totalAmount = "50$",
                totalAmountCurrencyConverted = "About CA\$ 1.38",
                initialBonusSupport = "0",
                totalBonusSupport = "0",
                deliveryDateString = "",
                rewardsHaveShippables = false,
                plotSelected = false,
                disclaimerText = stringResource(id = R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge)
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ItemizedContainerForNoRewardPlotSelectedPreview() {
    KSTheme {
        Scaffold(
            containerColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            ItemizedRewardListContainer(
                modifier = Modifier.padding(paddingValues = padding),
                totalAmount = "US$ 1",
                totalAmountCurrencyConverted = "About CA$ 1.38",
                initialBonusSupport = "US$ 0",
                totalBonusSupport = "US$ 1",
                shippingAmount = -1.0,
                plotSelected = true
            )
        }
    }
}

