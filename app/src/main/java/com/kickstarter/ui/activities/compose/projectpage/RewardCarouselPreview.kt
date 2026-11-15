package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.RewardsItemFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.joda.time.DateTime

/**
 * Activity displaying and coordinating user interactions for RewardCarouselScreenPreview.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RewardCarouselScreenPreview() {
    KSTheme {
        Scaffold(
            containerColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            RewardCarouselScreen(
                modifier = Modifier
                    .padding(padding)
                    .systemBarsPadding(),
                lazyRowState = rememberLazyListState(),
                environment = Environment.Builder().build(),
                rewards = (0..10).map {
                    Reward.builder()
                        .title("Item Number $it")
                        .description("This is a description for item $it")
                        .id(it.toLong())
                        .minimum(20.0)
                        .backersCount(2)
                        .estimatedDeliveryOn(DateTime.now())
                        .convertedMinimum((100 * (it + 1)).toDouble())
                        .isAvailable(it != 0)
                        .hasAddons(true)
                        .limit(if (it == 0) 1 else 10)
                        .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS)
                        .rewardsItems(
                            listOf(
                                RewardsItemFactory.rewardsItem(),
                                RewardsItemFactory.rewardsItem2(),
                                RewardsItemFactory.rewardsItem3()
                            )
                        )
                        .build()
                },
                project =
                Project.builder()
                    .currency("USD")
                    .currentCurrency("USD")
                    .state(Project.STATE_LIVE)
                    .build(),
                onRewardSelected = {},
                currentShippingRule = ShippingRuleFactory.usShippingRule(),
                countryList = listOf(
                    ShippingRuleFactory.usShippingRule(),
                    ShippingRuleFactory.germanyShippingRule()
                ),
                onShippingRuleSelected = {}
            )
        }
    }
}

