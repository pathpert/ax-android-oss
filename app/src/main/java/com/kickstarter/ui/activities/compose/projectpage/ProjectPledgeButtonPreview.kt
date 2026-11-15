package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlinx.coroutines.launch

/**
 * Activity displaying and coordinating user interactions for ProjectPledgeButtonAndContainerPreview.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ProjectPledgeButtonAndContainerPreview() {
    KSTheme {
        var expanded by remember {
            mutableStateOf(false)
        }
        val pagerState = rememberPagerState(initialPage = 1, pageCount = { 4 })

        val coroutineScope = rememberCoroutineScope()
        ProjectPledgeButtonAndFragmentContainer(
            expanded = expanded,
            onContinueClicked = { expanded = !expanded },
            onBackClicked = {
                if (pagerState.currentPage > 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage - 1,
                            animationSpec = tween(
                                durationMillis = 150,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                } else {
                    expanded = !expanded
                }
            },
            isLoading = false,
            pagerState = pagerState,
            onAddOnsContinueClicked = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = 2,
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                    )
                }
            },
            environment = Environment.builder().build(),
            rewardsList = listOf(),
            addOns = listOf(),
            project = Project.builder().build(),
            onRewardSelected = {},
            onAddOnAddedOrRemoved = { _, _ -> },
            totalSelectedAddOn = 0,
            totalPledgeAmount = 0.0,
            totalBonusAmount = 0.0,
            bonusAmountChanged = { _ -> },
            currentShippingRule = ShippingRule.builder().build(),
            onShippingRuleSelected = {},
            selectedRewardAndAddOnList = listOf(),
            storedCards = listOf(),
            userEmail = "test@test.test",
            shippingAmount = 0.0,
            checkoutTotal = 20.0,
            onPledgeCtaClicked = { _, _ -> },
            onAddPaymentMethodClicked = {},
            onDisclaimerItemClicked = {},
            onAccountabilityLinkClicked = {}
        )
    }
}

