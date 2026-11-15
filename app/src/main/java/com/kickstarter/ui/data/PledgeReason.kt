package com.kickstarter.ui.data

/**
 * UI component for PledgeReason.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
enum class PledgeReason {
    FIX_PLEDGE, PLEDGE, UPDATE_PAYMENT, UPDATE_PLEDGE, UPDATE_REWARD, LATE_PLEDGE
}
