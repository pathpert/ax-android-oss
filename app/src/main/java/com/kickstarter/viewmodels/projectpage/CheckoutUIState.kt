package com.kickstarter.viewmodels.projectpage

import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard

/**
 * Legacy UI state container for checkout screen presentation.
 *
 * Negative Constraints:
 * - Do NOT pass floating point values without validation against minor currency rules.
 */
@Deprecated(
    message = "Legacy monolithic path replaced by CrowdfundCheckoutContracts under com.kickstarter.features.checkout.data.",
    replaceWith = ReplaceWith("CrowdfundCheckoutContracts", "com.kickstarter.features.checkout.data.CrowdfundCheckoutContracts")
)
data class CheckoutUIState(
    val storeCards: List<StoredCard> = listOf(),
    val userEmail: String = "",
    val isLoading: Boolean = false,
    val selectedRewards: List<Reward> = emptyList(),
    val shippingAmount: Double = 0.0,
    val checkoutTotal: Double = 0.0,
    val isPledgeButtonEnabled: Boolean = true,
    val selectedPaymentMethod: StoredCard = StoredCard.builder().build(),
    val bonusAmount: Double = 0.0,
    val shippingRule: ShippingRule? = null,
    val showPlotWidget: Boolean = false,
    val plotEligible: Boolean = false,
    val isIncrementalPledge: Boolean? = null,
    val paymentIncrements: List<PaymentIncrement>? = null
)

/**
 * State representing Stripe PaymentSheet presentation intent.
 */
data class PaymentSheetPresenterState(val setupClientId: String = "")
