package com.kickstarter.viewmodels.projectpage

import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule

/**
 * UI State for [AddOnsViewModel].
 */
data class AddOnsUIState(
    val addOns: List<Reward> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val shippingRule: ShippingRule = ShippingRule.builder().build(),
    val totalPledgeAmount: Double = 0.0,
    val totalBonusAmount: Double = 0.0
)
