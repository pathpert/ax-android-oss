package com.kickstarter.ui.data

/**
 * UI component for ManagePledgeMenuOptions.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
data class ManagePledgeMenuOptions(
    val showEditPledge: Boolean,
    val showChooseAnotherReward: Boolean,
    val showUpdatePayment: Boolean,
    val showSeeRewards: Boolean,
    val showCancelPledge: Boolean,
    val showContactCreator: Boolean = true
)
