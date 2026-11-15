package com.kickstarter.ui.activities.compose.projectpage

import CollectionOptions
import CollectionPlan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.acceptedCardType
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.isFromPaymentSheet
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.compose.projectpage.components.CheckoutBottomBar
import com.kickstarter.ui.activities.compose.projectpage.components.CheckoutEstimatedShippingContainer
import com.kickstarter.ui.activities.compose.projectpage.components.CheckoutNotAStoreCard
import com.kickstarter.ui.activities.compose.projectpage.components.CheckoutPaymentMethodSelector
import com.kickstarter.ui.activities.compose.projectpage.components.CheckoutRewardSummarySection
import com.kickstarter.ui.activities.compose.projectpage.components.UpdateSelectedCardIfNewCardAdded
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.kds_white
import com.kickstarter.ui.data.PledgeReason
import java.math.RoundingMode
import com.kickstarter.ui.activities.compose.projectpage.components.KSCardElement as ComponentsKSCardElement
import com.kickstarter.ui.activities.compose.projectpage.components.KSEstimatedShippingCheckoutView as ComponentsKSEstimatedShippingCheckoutView
import com.kickstarter.ui.activities.compose.projectpage.components.TermsOfUseClickableText as ComponentsTermsOfUseClickableText
import com.kickstarter.ui.activities.compose.projectpage.components.TextWithClickableAccountabilityLink as ComponentsTextWithClickableAccountabilityLink

/**
 * Activity displaying and coordinating user interactions for CheckoutScreen.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun CheckoutScreen(
    storedCards: List<StoredCard> = listOf(),
    environment: Environment,
    selectedReward: Reward? = null,
    project: Project,
    email: String?,
    ksString: KSString? = null,
    ksCurrency: KSCurrency? = null,
    selectedRewardsAndAddOns: List<Reward> = listOf(),
    rewardsList: List<Pair<String, String>> = listOf(),
    shippingAmount: Double = 0.0,
    totalAmount: Double = 0.0,
    totalBonusSupport: Double = 0.0,
    currentShippingRule: ShippingRule? = null,
    isLoading: Boolean = false,
    pledgeReason: PledgeReason = PledgeReason.PLEDGE,
    rewardsHaveShippables: Boolean = false,
    onPledgeCtaClicked: (selectedCard: StoredCard?, isIncrementalPledge: Boolean) -> Unit,
    newPaymentMethodClicked: () -> Unit,
    onDisclaimerItemClicked: (disclaimerItem: DisclaimerItems) -> Unit,
    onAccountabilityLinkClicked: () -> Unit,
    onChangedPaymentMethod: (StoredCard?) -> Unit = {},
    onCollectionPlanSelected: (CollectionOptions) -> Unit = {},
    plotIsVisible: Boolean = false,
    isPlotEligible: Boolean = false,
    isIncrementalPledge: Boolean = false,
    paymentIncrements: List<PaymentIncrement>? = null,
    isPledgeButtonEnabled: Boolean = true,
    showPaymentMethodSelection: Boolean = true
) {
    val selectedOption = remember {
        mutableStateOf(
            storedCards.firstOrNull { project.acceptedCardType(it.type()) || it.isFromPaymentSheet() }
        )
    }

    val onOptionSelected: (StoredCard?) -> Unit = {
        selectedOption.value = it
        onChangedPaymentMethod.invoke(it)
    }

    val totalAmountString = environment.ksCurrency()?.let {
        RewardViewUtils.styleCurrency(totalAmount, project, it).toString()
    } ?: ""

    UpdateSelectedCardIfNewCardAdded(
        remember { mutableStateOf(storedCards.size) },
        storedCards,
        onOptionSelected
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Scaffold(
            containerColor = colors.backgroundAccentGraySubtle,
            modifier = Modifier.background(kds_white).fillMaxWidth(),
            bottomBar = {
                CheckoutBottomBar(
                    totalAmountString = totalAmountString,
                    project = project,
                    selectedOption = selectedOption.value,
                    isPledgeButtonEnabled = isPledgeButtonEnabled,
                    isIncrementalPledge = isIncrementalPledge,
                    pledgeReason = pledgeReason,
                    email = email,
                    ksString = ksString,
                    onPledgeCtaClicked = onPledgeCtaClicked,
                    onDisclaimerItemClicked = onDisclaimerItemClicked
                )
            }
        ) { padding ->
            val totalAmountConvertedString = if (project.currentCurrency() == project.currency()) "" else {
                environment.ksCurrency()?.formatWithUserPreference(totalAmount, project, RoundingMode.UP, 2) ?: ""
            }

            val shippingAmountString = environment.ksCurrency()?.let {
                RewardViewUtils.styleCurrency(shippingAmount, project, it).toString()
            } ?: ""

            val initialBonusSupportString = environment.ksCurrency()?.let {
                RewardViewUtils.styleCurrency(0.0, project, it).toString()
            } ?: ""

            val totalBonusSupportString = environment.ksCurrency()?.let {
                RewardViewUtils.styleCurrency(totalBonusSupport, project, it).toString()
            } ?: ""

            val aboutTotalString = if (totalAmountConvertedString.isEmpty()) "" else environment.ksString()?.format(
                stringResource(id = R.string.About_reward_amount),
                "reward_amount",
                totalAmountConvertedString
            ) ?: "About $totalAmountConvertedString"

            val shippingLocation = currentShippingRule?.location()?.displayableName() ?: ""

            val deliveryDateString = if (selectedReward?.estimatedDeliveryOn().isNotNull()) {
                stringResource(id = R.string.Estimated_delivery) + " " + DateTimeUtils.estimatedDeliveryOn(
                    requireNotNull(selectedReward?.estimatedDeliveryOn())
                )
            } else ""

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = padding.calculateBottomPadding()),
            ) {
                Text(
                    modifier = Modifier.padding(start = dimensions.paddingMediumLarge, top = dimensions.paddingMediumLarge),
                    text = stringResource(id = R.string.Checkout),
                    style = typographyV2.headingXL,
                    color = colors.kds_black,
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                if (plotIsVisible) {
                    Text(
                        modifier = Modifier.padding(start = dimensions.paddingMediumLarge, end = dimensions.paddingMediumLarge),
                        text = stringResource(id = R.string.Collection_plan),
                        style = typographyV2.headLine,
                        color = colors.kds_black,
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                    CollectionPlan(
                        isEligible = isPlotEligible && !project.pledgeOverTimeMinimumExplanation().isNullOrEmpty(),
                        changeCollectionPlan = onCollectionPlanSelected,
                        paymentIncrements = paymentIncrements,
                        ksCurrency = ksCurrency,
                        projectCurrency = project.currency(),
                        projectCurrentCurrency = project.currentCurrency(),
                        termsOfUseCallback = onDisclaimerItemClicked,
                        plotMinimum = project.pledgeOverTimeMinimumExplanation(),
                        pledgeOverTimeCollectionPlanChargeExplanation = project.pledgeOverTimeCollectionPlanChargeExplanation(),
                        pledgeOverTimeShortPitch = project.pledgeOverTimeCollectionPlanShortPitch(),
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
                }

                if (showPaymentMethodSelection) {
                    CheckoutPaymentMethodSelector(
                        storedCards = storedCards,
                        selectedCard = selectedOption.value,
                        project = project,
                        ksString = environment.ksString(),
                        onOptionSelected = onOptionSelected,
                        newPaymentMethodClicked = newPaymentMethodClicked
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingLarge))
                }

                CheckoutNotAStoreCard(
                    modifier = Modifier.padding(start = dimensions.paddingMedium, end = dimensions.paddingMedium),
                    onAccountabilityLinkClicked = onAccountabilityLinkClicked
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                CheckoutRewardSummarySection(
                    environment = environment,
                    ksString = ksString,
                    project = project,
                    selectedReward = selectedReward,
                    rewardsList = rewardsList,
                    shippingAmount = shippingAmount,
                    shippingAmountString = shippingAmountString,
                    shippingLocation = shippingLocation,
                    totalAmountString = totalAmountString,
                    aboutTotalString = aboutTotalString,
                    initialBonusSupportString = initialBonusSupportString,
                    totalBonusSupportString = totalBonusSupportString,
                    deliveryDateString = deliveryDateString,
                    rewardsHaveShippables = rewardsHaveShippables,
                    isIncrementalPledge = isIncrementalPledge,
                    paymentIncrements = paymentIncrements
                )

                CheckoutEstimatedShippingContainer(
                    environment = environment,
                    currentShippingRule = currentShippingRule,
                    project = project,
                    selectedRewardsAndAddOns = selectedRewardsAndAddOns
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.backgroundAccentGraySubtle.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                KSCircularProgressIndicator()
            }
        }
    }
}

// Backward-compatible delegates
@Composable
fun TermsOfUseClickableText(
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onCookiePolicyClicked: () -> Unit
) = ComponentsTermsOfUseClickableText(onTermsOfUseClicked, onPrivacyPolicyClicked, onCookiePolicyClicked)

@Composable
fun TextWithClickableAccountabilityLink(
    padding: Dp,
    html: String,
    onClickCallback: (String?) -> Unit = {}
) = ComponentsTextWithClickableAccountabilityLink(padding, html, onClickCallback)

@Composable
fun KSCardElement(card: StoredCard, ksString: KSString?, isAvailable: Boolean) =
    ComponentsKSCardElement(card, ksString, isAvailable)

@Composable
fun KSEstimatedShippingCheckoutView(
    estimatedShippingRange: String,
    estimatedShippingRangeConversion: String?,
) = ComponentsKSEstimatedShippingCheckoutView(estimatedShippingRange, estimatedShippingRangeConversion)
