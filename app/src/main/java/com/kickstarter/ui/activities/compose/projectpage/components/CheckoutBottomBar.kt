package com.kickstarter.ui.activities.compose.projectpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.acceptedCardType
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.isFromPaymentSheet
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.data.PledgeReason

@Composable
fun CheckoutBottomBar(
    totalAmountString: String,
    project: Project,
    selectedOption: StoredCard?,
    isPledgeButtonEnabled: Boolean,
    isIncrementalPledge: Boolean,
    pledgeReason: PledgeReason,
    email: String?,
    ksString: KSString?,
    onPledgeCtaClicked: (selectedCard: StoredCard?, isIncrementalPledge: Boolean) -> Unit,
    onDisclaimerItemClicked: (disclaimerItem: DisclaimerItems) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = dimensions.radiusLarge, topEnd = dimensions.radiusLarge),
        color = colors.backgroundSurfacePrimary,
        tonalElevation = dimensions.elevationLarge,
    ) {
        Column(
            modifier = Modifier
                .background(colors.kds_white)
                .padding(dimensions.paddingMediumLarge)
        ) {
            val isCardAccepted = (project.acceptedCardType(selectedOption?.type()) || selectedOption?.isFromPaymentSheet() ?: false)
            KSPrimaryGreenButton(
                modifier = Modifier.padding(bottom = dimensions.paddingMediumSmall).fillMaxWidth(),
                onClickAction = { onPledgeCtaClicked(selectedOption, isIncrementalPledge) },
                isEnabled = isCardAccepted && isPledgeButtonEnabled,
                text = if (pledgeReason == PledgeReason.PLEDGE || pledgeReason == PledgeReason.LATE_PLEDGE) {
                    stringResource(id = R.string.Pledge) + " $totalAmountString"
                } else {
                    stringResource(id = R.string.Confirm)
                }
            )

            val formattedEmailDisclaimerString = ksString?.let {
                email?.let { e ->
                    ksString.format(
                        stringResource(id = R.string.Your_payment_method_will_be_charged_immediately),
                        "user_email", e
                    )
                }
            }

            if (!formattedEmailDisclaimerString.isNullOrEmpty() && project.isInPostCampaignPledgingPhase() == true) {
                Text(
                    text = formattedEmailDisclaimerString,
                    textAlign = TextAlign.Center,
                    style = typographyV2.bodyXS,
                    color = colors.kds_support_400
                )
            }

            Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

            TermsOfUseClickableText(
                onPrivacyPolicyClicked = { onDisclaimerItemClicked.invoke(DisclaimerItems.PRIVACY) },
                onCookiePolicyClicked = { onDisclaimerItemClicked.invoke(DisclaimerItems.COOKIES) },
                onTermsOfUseClicked = { onDisclaimerItemClicked.invoke(DisclaimerItems.TERMS) }
            )
        }
    }
}
