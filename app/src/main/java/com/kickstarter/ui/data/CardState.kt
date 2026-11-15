package com.kickstarter.ui.data

/**
 * UI component for CardState.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
enum class CardState {
    SELECTED, UNSELECTED
}
