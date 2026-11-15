package com.kickstarter.ui.activities.compose.projectpage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.views.compose.checkout.ShippingSelector
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * Activity displaying and coordinating user interactions for RewardCarouselTestTag.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
enum class RewardCarouselTestTag(val tag: String) {
    REWARD_CAROUSEL("REWARD_CAROUSEL"),
    REWARD_CARD("REWARD_CARD_"), // Use with ID
    SHIPPING_SELECTOR("SHIPPING_SELECTOR")
}

@Composable
fun RewardCarouselScreen(
    modifier: Modifier = Modifier,
    lazyRowState: LazyListState,
    environment: Environment,
    rewards: List<Reward>,
    project: Project,
    backing: Backing? = null,
    isLoading: Boolean = false,
    onRewardSelected: (reward: Reward) -> Unit,
    countryList: List<ShippingRule> = emptyList<ShippingRule>(),
    onShippingRuleSelected: (ShippingRule) -> Unit = {},
    currentShippingRule: ShippingRule = ShippingRule.builder().build()
) {
    val context = LocalContext.current
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val hasScrolledToSecretReward = remember { mutableStateOf(false) }

    LaunchedEffect(rewards) {
        if (!hasScrolledToSecretReward.value) {
            val index = rewards.indexOfFirst { it.isSecretReward() == true && it.isAvailable() }

            if (index >= 0) {
                snapshotFlow { lazyRowState.layoutInfo.totalItemsCount }
                    .filter { it > index }
                    .first()

                lazyRowState.animateScrollToItem(index)

                hasScrolledToSecretReward.value = true
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = KSTheme.colors.backgroundAccentGraySubtle,
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                if (countryList.isNotEmpty()) {
                    ShippingSelector(
                        modifier = Modifier
                            .padding(dimensions.paddingMedium)
                            .testTag(RewardCarouselTestTag.SHIPPING_SELECTOR.tag),
                        interactionSource = interactionSource,
                        currentShippingRule = currentShippingRule,
                        countryList = countryList,
                        onShippingRuleSelected = onShippingRuleSelected
                    )
                }

                LazyRow(
                    modifier = Modifier
                        .testTag(RewardCarouselTestTag.REWARD_CAROUSEL.name)
                        .fillMaxWidth()
                        .weight(1f),
                    state = lazyRowState,
                    contentPadding = PaddingValues(
                        start = KSTheme.dimensions.paddingMedium,
                        end = KSTheme.dimensions.paddingMedium,
                        top = KSTheme.dimensions.paddingMedium
                    ),
                    horizontalArrangement = Arrangement.spacedBy(KSTheme.dimensions.paddingMediumLarge)
                ) {
                    items(
                        items = rewards,
                    ) { reward ->
                        CarouselRewardItem(
                            reward = reward,
                            project = project,
                            backing = backing,
                            currentShippingRule = currentShippingRule,
                            environment = environment,
                            onRewardSelected = onRewardSelected
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = KSTheme.dimensions.paddingMediumSmall,
                            top = KSTheme.dimensions.paddingMediumSmall
                        ),
                    text = if (isLoading) "" else environment.ksString()?.let {
                        it.format(
                            "Rewards_count_rewards", rewards.size,
                            "rewards_count", NumberUtils.format(rewards.size)
                        )
                    } ?: "",
                    color = KSTheme.colors.kds_support_400,
                    textAlign = TextAlign.Center
                )
            }
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KSTheme.colors.backgroundAccentGraySubtle.copy(alpha = 0.33f))
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    KSCircularProgressIndicator()
                }
            }
        }
    }
}
