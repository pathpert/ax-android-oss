package com.kickstarter.ui.compose

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.models.Photo
import com.kickstarter.ui.compose.designsystem.KSTheme

/**
 * UI component rendering KSRewardCardPreviewImageSelectedNoSecret.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSRewardCardPreviewImageSelectedNoSecret() {
    KSTheme {
        KSRewardCard(
            amount = "$20",
            conversion = "about $400",
            title = "Deck of cards",
            backerCountBadgeText = "23 backers",
            image = Photo.builder().altText("").full("").build(),
            description = "this is a description",
            isCTAButtonEnabled = true,
            isSecret = false,
            estimatedDelivery = "June 10th, 2026",
            includes = listOf("1 Comic Book", "2 pins", "3 happy meals"),
            yourSelectionIsVisible = true,
            ctaButtonText = "Select",
            expirationDateText = "4 Days",
            shippingSummaryText = "Anywhere",
            addonsPillVisible = true,
            remainingText = "5 left",
            estimatedShippingCost = "About $10-$15",
            onRewardSelectClicked = { }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSRewardCardPreviewImageSelectedSecret() {
    KSTheme {
        KSRewardCard(
            amount = "$20",
            conversion = "about $400",
            title = "Deck of cards",
            backerCountBadgeText = "23 backers",
            image = Photo.builder().altText("").full("").build(),
            description = "this is a description",
            isCTAButtonEnabled = true,
            isSecret = true,
            estimatedDelivery = "June 10th, 2026",
            includes = listOf("1 Comic Book", "2 pins", "3 happy meals"),
            yourSelectionIsVisible = true,
            ctaButtonText = "Select",
            expirationDateText = "4 Days",
            shippingSummaryText = "Anywhere",
            addonsPillVisible = true,
            remainingText = "5 left",
            estimatedShippingCost = "About $10-$15",
            onRewardSelectClicked = { }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSRewardCardPreviewImageSelectedFeatured() {
    KSTheme {
        KSRewardCard(
            amount = "$20",
            conversion = "about $400",
            title = "Featured reward",
            backerCountBadgeText = "23 backers",
            image = Photo.builder().altText("").full("").build(),
            description = "this is a description",
            isCTAButtonEnabled = true,
            isSecret = false,
            isFeatured = true,
            estimatedDelivery = "June 10th, 2026",
            includes = listOf("1 Comic Book", "2 pins", "3 happy meals"),
            yourSelectionIsVisible = true,
            ctaButtonText = "Select",
            expirationDateText = "4 Days",
            shippingSummaryText = "Anywhere",
            addonsPillVisible = true,
            remainingText = "5 left",
            estimatedShippingCost = "About $10-$15",
            onRewardSelectClicked = { }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSRewardCardPreviewImageNoSelectedSecret() {
    KSTheme {
        KSRewardCard(
            amount = "$20",
            conversion = "about $400",
            title = "Deck of cards",
            backerCountBadgeText = "23 backers",
            image = Photo.builder().altText("").full("").build(),
            description = "this is a description",
            isCTAButtonEnabled = true,
            isSecret = true,
            estimatedDelivery = "June 10th, 2026",
            includes = listOf("1 Comic Book", "2 pins", "3 happy meals"),
            yourSelectionIsVisible = false,
            ctaButtonText = "Select",
            expirationDateText = "4 Days",
            shippingSummaryText = "Anywhere",
            addonsPillVisible = true,
            remainingText = "5 left",
            estimatedShippingCost = "About $10-$15",
            onRewardSelectClicked = { }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSRewardCardPreviewImageNoSelectedFeatured() {
    KSTheme {
        KSRewardCard(
            amount = "$20",
            conversion = "about $400",
            title = "Featured reward",
            backerCountBadgeText = "23 backers",
            image = Photo.builder().altText("").full("").build(),
            description = "this is a description",
            isCTAButtonEnabled = true,
            isSecret = false,
            isFeatured = true,
            estimatedDelivery = "June 10th, 2026",
            includes = listOf("1 Comic Book", "2 pins", "3 happy meals"),
            yourSelectionIsVisible = false,
            ctaButtonText = "Select",
            expirationDateText = "4 Days",
            shippingSummaryText = "Anywhere",
            addonsPillVisible = true,
            remainingText = "5 left",
            estimatedShippingCost = "About $10-$15",
            onRewardSelectClicked = { }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSRewardCardPreviewImageNoSelectedNoSecret() {
    KSTheme {
        KSRewardCard(
            amount = "$20",
            conversion = "about $400",
            title = "Deck of cards",
            backerCountBadgeText = "23 backers",
            image = Photo.builder().altText("").full("").build(),
            description = "this is a description",
            isCTAButtonEnabled = true,
            isSecret = false,
            estimatedDelivery = "June 10th, 2026",
            includes = listOf("1 Comic Book", "2 pins", "3 happy meals"),
            yourSelectionIsVisible = false,
            ctaButtonText = "Select",
            expirationDateText = "4 Days",
            shippingSummaryText = "Anywhere",
            addonsPillVisible = true,
            remainingText = "5 left",
            estimatedShippingCost = "About $10-$15",
            onRewardSelectClicked = { }
        )
    }
}
