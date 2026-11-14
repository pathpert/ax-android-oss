package com.kickstarter.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import com.kickstarter.models.Photo
import com.kickstarter.ui.compose.designsystem.KSFeaturedRewardBadge
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSecretRewardBadge
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
fun KSRewardCard(
    modifier: Modifier = Modifier,
    amount: String? = null,
    conversion: String? = null,
    title: String? = null,
    backerCountBadgeText: String? = null,
    image: Photo? = null,
    description: String? = null,
    includes: List<String> = emptyList(),
    estimatedDelivery: String? = null,
    yourSelectionIsVisible: Boolean = false,
    isCTAButtonEnabled: Boolean,
    isCTAButtonVisible: Boolean = true,
    ctaButtonText: String,
    expirationDateText: String? = null,
    localPickup: String? = null,
    shippingSummaryText: String? = null,
    addonsPillVisible: Boolean = false,
    remainingText: String? = null,
    estimatedShippingCost: String? = null,
    onRewardSelectClicked: () -> Unit,
    isSecret: Boolean = false,
    isFeatured: Boolean = false,
) {
    Box(modifier = modifier.width(KSTheme.dimensions.cardWidth)) {
        if ((isSecret || isFeatured) && image == null && !yourSelectionIsVisible) {
            Box(
                modifier = Modifier
                    .offset(x = dimensions.paddingMedium, y = -(dimensions.secretRewardBadgeOffsetY))
                    .zIndex(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSecret) KSSecretRewardBadge()
                    if (isFeatured) KSFeaturedRewardBadge()
                }
            }
        }
        val cardShape = RoundedCornerShape(KSTheme.dimensions.radiusMediumSmall)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = KSTheme.colors.kds_white),
            border = null,
        ) {
            Column(
                modifier = Modifier
                    .clip(cardShape)
                    .background(KSTheme.colors.kds_white)
            ) {
                Box(
                    modifier = Modifier.zIndex(1f)
                ) {
                    if (image != null) {
                        KSRewardAsyncImage(image = image)
                        if (isSecret || isFeatured) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(
                                        x = dimensions.paddingMedium,
                                        y = dimensions.secretRewardBadgeOffsetY
                                    )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSecret) KSSecretRewardBadge()
                                    if (isFeatured) KSFeaturedRewardBadge()
                                }
                            }
                        }
                    }

                    if (yourSelectionIsVisible) {
                        YourSelectionTag()
                        if ((isSecret || isFeatured) && image == null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(
                                        start = dimensions.paddingMedium,
                                        top = dimensions.paddingXXXLarge
                                    )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSecret) KSSecretRewardBadge()
                                    if (isFeatured) KSFeaturedRewardBadge()
                                }
                            }
                        }
                    }
                }

                RewardCardDetails(
                    modifier = Modifier.weight(1f, fill = false),
                    amount = amount,
                    conversion = conversion,
                    title = title,
                    backerCountBadgeText = backerCountBadgeText,
                    description = description,
                    includes = includes,
                    estimatedDelivery = estimatedDelivery,
                    expirationDateText = expirationDateText,
                    localPickup = localPickup,
                    shippingSummaryText = shippingSummaryText,
                    addonsPillVisible = addonsPillVisible,
                    remainingText = remainingText,
                    estimatedShippingCost = estimatedShippingCost
                )

                if (isCTAButtonVisible) {
                    KSPrimaryGreenButton(
                        modifier = Modifier
                            .padding(
                                bottom = KSTheme.dimensions.paddingMediumLarge,
                                start = KSTheme.dimensions.paddingMediumLarge,
                                end = KSTheme.dimensions.paddingMediumLarge
                            )
                            .fillMaxWidth(),
                        onClickAction = onRewardSelectClicked,
                        isEnabled = isCTAButtonEnabled,
                        text = ctaButtonText
                    )
                }
            }
        }
    }
}
