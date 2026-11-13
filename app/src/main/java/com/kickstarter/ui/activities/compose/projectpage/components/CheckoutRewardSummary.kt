package com.kickstarter.ui.activities.compose.projectpage.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.format
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.views.compose.checkout.ItemizedRewardListContainer

@Composable
fun CheckoutRewardSummarySection(
    environment: Environment,
    ksString: KSString?,
    project: Project,
    selectedReward: Reward?,
    rewardsList: List<Pair<String, String>>,
    shippingAmount: Double,
    shippingAmountString: String,
    shippingLocation: String,
    totalAmountString: String,
    aboutTotalString: String,
    initialBonusSupportString: String,
    totalBonusSupportString: String,
    deliveryDateString: String,
    rewardsHaveShippables: Boolean,
    isIncrementalPledge: Boolean,
    paymentIncrements: List<PaymentIncrement>?
) {
    val resourceString =
        stringResource(R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge)
    val disclaimerText = environment.ksString()?.format(
        resourceString,
        "total", totalAmountString,
        "project_deadline", project.deadline()?.let { DateTimeUtils.longDate(it) }
    ) ?: ""
    val plotDisclaimerText =
        stringResource(R.string.If_the_project_reaches_its_funding_goal_the_first_charge_will_be_collected_on_project_deadline).format(
            key1 = "amount",
            paymentIncrements?.first()?.paymentIncrementAmount?.formattedAmount,
            key2 = "project_deadline",
            paymentIncrements?.first()?.scheduledCollection?.let {
                DateTimeUtils.mediumDate(it)
            }
        )
    val isNoReward = selectedReward?.let { RewardUtils.isNoReward(it) } ?: false

    if (!isNoReward) {
        ItemizedRewardListContainer(
            ksString = ksString,
            rewardsList = rewardsList,
            shippingAmount = shippingAmount,
            shippingAmountString = shippingAmountString,
            initialShippingLocation = shippingLocation,
            totalAmount = totalAmountString,
            totalAmountCurrencyConverted = aboutTotalString,
            initialBonusSupport = initialBonusSupportString,
            totalBonusSupport = totalBonusSupportString,
            deliveryDateString = deliveryDateString,
            rewardsHaveShippables = rewardsHaveShippables,
            disclaimerText = if (isIncrementalPledge) plotDisclaimerText else disclaimerText,
            plotSelected = isIncrementalPledge,
            pledgeOverTimeCollectionPlanChargedAsNPayments = project.pledgeOverTimeCollectionPlanChargedAsNPayments(),
        )
    } else {
        ItemizedRewardListContainer(
            totalAmount = totalAmountString,
            totalAmountCurrencyConverted = aboutTotalString,
            initialBonusSupport = initialBonusSupportString,
            totalBonusSupport = totalAmountString,
            shippingAmount = shippingAmount,
            disclaimerText = if (isIncrementalPledge) plotDisclaimerText else disclaimerText,
            plotSelected = isIncrementalPledge,
            pledgeOverTimeCollectionPlanChargedAsNPayments = project.pledgeOverTimeCollectionPlanChargedAsNPayments(),
        )
    }
}
