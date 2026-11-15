package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCardFactory
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import kotlinx.coroutines.flow.flowOf

/**
 * Feature component for PledgedProjectsOverviewScreenPreview in pledgedprojectsoverview module.
 *
 * Negative constraints:
 * - Do not bypass feature-scoped boundary contracts or leak internal UI state.
 */
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenPreview() {
    KSTheme {
        Scaffold(
            containerColor = colors.backgroundSurfacePrimary
        ) { padding ->
            val ppoCardList1 = (0..10).map {
                PPOCardFactory.fixPaymentCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList1)).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardPagingList,
                totalAlerts = 10,
                onBackPressed = {},
                onPrimaryActionButtonClicked = {},
                onSecondaryActionButtonClicked = {},
                onAddressConfirmed = { _, _ -> },
                onRewardReceivedChanged = { _, _ -> },
                onProjectPledgeSummaryClick = { _ -> },
                onSendMessageClick = { _, _, _, _, _ -> },
                onSeeAllBackedProjectsClick = {},
                errorSnackBarHostState = SnackbarHostState(),
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenErrorPreview() {
    KSTheme {
        Scaffold(
            containerColor = colors.backgroundSurfacePrimary
        ) { padding ->
            val ppoCardList1 = (0..10).map {
                PPOCardFactory.confirmAddressCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList1)).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardPagingList,
                totalAlerts = 10,
                onBackPressed = {},
                onPrimaryActionButtonClicked = {},
                onSecondaryActionButtonClicked = {},
                onAddressConfirmed = { _, _ -> },
                onRewardReceivedChanged = { _, _ -> },
                onProjectPledgeSummaryClick = { _ -> },
                onSendMessageClick = { _, _, _, _, _ -> },
                onSeeAllBackedProjectsClick = {},
                isErrored = true,
                errorSnackBarHostState = SnackbarHostState(),
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenEmptyPreview() {
    KSTheme {
        Scaffold(
            containerColor = colors.backgroundSurfacePrimary
        ) { padding ->
            val ppoCardPagingList = flowOf(PagingData.from(listOf<PPOCard>())).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardPagingList,
                totalAlerts = 0,
                onBackPressed = {},
                onPrimaryActionButtonClicked = {},
                onSecondaryActionButtonClicked = {},
                onAddressConfirmed = { _, _ -> },
                onRewardReceivedChanged = { _, _ -> },
                onProjectPledgeSummaryClick = { _ -> },
                onSendMessageClick = { _, _, _, _, _ -> },
                errorSnackBarHostState = SnackbarHostState(),
                onSeeAllBackedProjectsClick = {}
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenV2Preview() {
    KSTheme {
        Scaffold(
            containerColor = colors.backgroundSurfacePrimary
        ) { padding ->
            val ppoCardList1 = (0..10).map {
                PPOCardFactory.fixPaymentCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList1)).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardPagingList,
                totalAlerts = 10,
                onBackPressed = {},
                onPrimaryActionButtonClicked = {},
                onSecondaryActionButtonClicked = {},
                onAddressConfirmed = { _, _ -> },
                onRewardReceivedChanged = { _, _ -> },
                onProjectPledgeSummaryClick = { _ -> },
                onSendMessageClick = { _, _, _, _, _ -> },
                onSeeAllBackedProjectsClick = {},
                errorSnackBarHostState = SnackbarHostState(),
                v2Enabled = true,
            )
        }
    }
}

