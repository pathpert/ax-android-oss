package com.kickstarter.features.checkout.data

import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard

/**
 * Immutable unidirectional UI state for the Late Pledge Checkout flow.
 *
 * Negative constraints:
 * - Do not pass raw unvalidated currency values; format via localized currency helpers.
 * - Do not enable pledge submission when [isLoading] is true or [isPledgeButtonEnabled] is false.
 *
 * @property storeCards Saved credit cards for the current user.
 * @property userEmail Contact email of the pledging user.
 * @property isLoading Whether checkout or pledge submission is actively loading.
 * @property selectedRewards List of reward items included in this late pledge.
 * @property shippingAmount Shipping fee major amount.
 * @property checkoutTotal Total checkout amount in major units.
 * @property isPledgeButtonEnabled Whether the pledge action button can be tapped.
 * @property projectCountryCode ISO country code of the project.
 * @property projectCurrency Currency symbol or code for the project.
 */
data class LatePledgeCheckoutUIState(
    val storeCards: List<StoredCard> = listOf(),
    val userEmail: String = "",
    val isLoading: Boolean = false,
    val selectedRewards: List<Reward> = emptyList(),
    val shippingAmount: Double = 0.0,
    val checkoutTotal: Double = 0.0,
    val isPledgeButtonEnabled: Boolean = true,
    val projectCountryCode: String? = null,
    val projectCurrency: String? = null
)
