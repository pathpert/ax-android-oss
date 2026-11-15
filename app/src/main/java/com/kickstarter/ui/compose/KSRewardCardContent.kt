package com.kickstarter.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSGreenBadge
import com.kickstarter.ui.compose.designsystem.KSSubtleWarningBadge
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * UI component rendering RewardCardDetails.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RewardCardDetails(
    modifier: Modifier = Modifier,
    amount: String? = null,
    conversion: String? = null,
    title: String? = null,
    backerCountBadgeText: String? = null,
    description: String? = null,
    includes: List<String> = emptyList(),
    estimatedDelivery: String? = null,
    expirationDateText: String? = null,
    localPickup: String? = null,
    shippingSummaryText: String? = null,
    addonsPillVisible: Boolean = false,
    remainingText: String? = null,
    estimatedShippingCost: String? = null
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(KSTheme.dimensions.paddingMediumLarge)
    ) {
        if (!amount.isNullOrEmpty()) {
            Text(text = amount, style = typographyV2.heading2XL, color = colors.textAccentGreenBold)
        }

        if (!conversion.isNullOrEmpty()) {
            Text(text = conversion, style = typographyV2.footNote, color = colors.textAccentGreenBold)
        }

        Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumSmall))

        if (!title.isNullOrEmpty()) {
            Text(
                text = title,
                style = typographyV2.titleRewardBold,
                color = colors.kds_black
            )
            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumSmall))
        }

        if (!backerCountBadgeText.isNullOrEmpty()) {
            KSGreenBadge(text = backerCountBadgeText)
            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumLarge))
        }

        if (!description.isNullOrEmpty()) {
            Text(
                text = stringResource(id = R.string.Description),
                color = colors.kds_support_400,
                style = typographyV2.headingLG
            )
            Text(
                modifier = Modifier.padding(top = KSTheme.dimensions.textInputTopPadding),
                text = description,
                color = colors.kds_support_700,
                style = typographyV2.bodyMD
            )
            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumLarge))
        }

        if (includes.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.rewards_info_includes),
                color = colors.kds_support_400,
                style = typographyV2.headingLG
            )
            includes.forEach { item ->
                Row(
                    modifier = Modifier.padding(
                        top = KSTheme.dimensions.radiusSmall,
                        bottom = KSTheme.dimensions.radiusSmall
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(KSTheme.dimensions.paddingMediumSmall))
                    Box(
                        modifier = Modifier
                            .padding(end = KSTheme.dimensions.paddingSmall)
                            .size(KSTheme.dimensions.paddingXSmall)
                            .background(
                                color = colors.kds_support_400,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = item,
                        style = typographyV2.bodyMD,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(KSTheme.dimensions.paddingMediumSmall))
                }
            }
            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumLarge))
        }

        if (!estimatedShippingCost.isNullOrEmpty()) {
            Text(
                text = stringResource(id = R.string.Estimated_Shipping),
                color = colors.kds_support_400,
                style = typographyV2.headingLG
            )
            Text(
                modifier = Modifier.padding(top = KSTheme.dimensions.radiusSmall),
                text = estimatedShippingCost,
                color = colors.kds_support_700,
                style = typographyV2.bodyMD
            )
            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumLarge))
        }

        if (!estimatedDelivery.isNullOrEmpty()) {
            Text(
                text = stringResource(id = R.string.Estimated_delivery),
                color = colors.kds_support_400,
                style = typographyV2.headingLG
            )
            Text(
                modifier = Modifier.padding(top = KSTheme.dimensions.radiusSmall),
                text = estimatedDelivery,
                color = colors.kds_support_700,
                style = typographyV2.bodyMD
            )
            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumLarge))
        }

        if (!localPickup.isNullOrEmpty()) {
            Text(
                text = stringResource(id = R.string.Reward_location),
                color = colors.kds_support_400,
                style = typographyV2.headingLG
            )
            Text(
                modifier = Modifier.padding(top = KSTheme.dimensions.radiusSmall),
                text = localPickup,
                color = colors.kds_support_700,
                style = typographyV2.bodyMD
            )
            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumLarge))
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!expirationDateText.isNullOrEmpty()) {
                KSGreenBadge(text = expirationDateText)
            }
            if (!remainingText.isNullOrEmpty()) {
                KSSubtleWarningBadge(text = remainingText)
            }
            if (!shippingSummaryText.isNullOrEmpty()) {
                KSGreenBadge(text = shippingSummaryText)
            }
            if (addonsPillVisible) {
                KSGreenBadge(text = stringResource(id = R.string.Add_ons))
                Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMediumLarge))
            }
        }
    }
}

@Composable
fun YourSelectionTag() {
    Box(
        modifier = Modifier
            .background(
                color = colors.kds_trust_500,
                shape = RoundedCornerShape(
                    topStart = dimensions.radiusMediumSmall,
                    bottomEnd = dimensions.radiusMediumSmall
                )
            )
            .padding(
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                start = dimensions.paddingMediumLarge,
                end = dimensions.paddingMediumLarge
            ),
    ) {
        Text(
            text = stringResource(id = R.string.Your_selection),
            style = typographyV2.subHeadline,
            color = colors.kds_white,
        )
    }
}
