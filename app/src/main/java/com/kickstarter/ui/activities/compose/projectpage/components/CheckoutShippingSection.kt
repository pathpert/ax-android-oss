package com.kickstarter.ui.activities.compose.projectpage.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.shapes

/**
 * Activity displaying and coordinating user interactions for KSEstimatedShippingCheckoutViewPreview.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSEstimatedShippingCheckoutViewPreview() {
    KSTheme {
        KSEstimatedShippingCheckoutView(
            estimatedShippingRange = "€5-€10",
            estimatedShippingRangeConversion = "About $6-$11"
        )
    }
}

@Composable
fun KSEstimatedShippingCheckoutView(
    estimatedShippingRange: String,
    estimatedShippingRangeConversion: String?,
) {
    Card(
        modifier = Modifier.padding(dimensions.paddingMediumLarge),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = colors.backgroundSurfacePrimary
        )
    ) {
        Row {
            Column(modifier = Modifier.weight(0.6f)) {
                Text(
                    modifier = Modifier.padding(
                        start = dimensions.paddingLarge,
                        top = dimensions.paddingMedium
                    ),
                    text = stringResource(id = R.string.Estimated_Shipping),
                    style = typographyV2.headingLG,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                Text(
                    modifier = Modifier.padding(
                        start = dimensions.paddingLarge,
                        bottom = dimensions.paddingMedium
                    ),
                    text = stringResource(id = R.string.This_is_meant_to_give_you),
                    style = typographyV2.bodyXS,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(dimensions.paddingMedium))

            Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
                Text(
                    modifier = Modifier.padding(
                        end = dimensions.paddingLarge,
                        top = dimensions.paddingMedium
                    ),
                    text = estimatedShippingRange,
                    style = typographyV2.headingLG,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                if (!estimatedShippingRangeConversion.isNullOrEmpty()) {
                    Text(
                        modifier = Modifier.padding(
                            end = dimensions.paddingLarge,
                            bottom = dimensions.paddingMedium
                        ),
                        text = estimatedShippingRangeConversion,
                        style = typographyV2.bodySM,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutEstimatedShippingContainer(
    environment: Environment,
    currentShippingRule: ShippingRule?,
    project: Project,
    selectedRewardsAndAddOns: List<Reward>
) {
    val ksCurrency = environment.ksCurrency()
    val ksString = environment.ksString()
    val shippingRule = currentShippingRule
    if (ksCurrency != null && ksString != null && shippingRule != null) {
        val estimatedShippingRangeString = RewardViewUtils.getEstimatedShippingRange(
            context = LocalContext.current,
            ksCurrency = ksCurrency,
            ksString = ksString,
            project = project,
            rewards = selectedRewardsAndAddOns,
            selectedShippingRule = shippingRule,
            multipleQuantitiesAllowed = false,
            useUserPreference = false,
            useAbout = false
        )

        val estimatedShippingRangeConversionString =
            if (project.currentCurrency() == project.currency()) null
            else {
                RewardViewUtils.getEstimatedShippingRange(
                    context = LocalContext.current,
                    ksCurrency = ksCurrency,
                    ksString = ksString,
                    project = project,
                    rewards = selectedRewardsAndAddOns,
                    selectedShippingRule = shippingRule,
                    multipleQuantitiesAllowed = false,
                    useUserPreference = true,
                    useAbout = true
                )
            }

        if (estimatedShippingRangeString.isNotEmpty()) {
            KSEstimatedShippingCheckoutView(
                estimatedShippingRange = estimatedShippingRangeString,
                estimatedShippingRangeConversion = estimatedShippingRangeConversionString
            )
        }
    }
}
