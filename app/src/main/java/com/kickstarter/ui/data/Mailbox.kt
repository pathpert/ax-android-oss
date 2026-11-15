package com.kickstarter.ui.data

/**
 * UI component for Mailbox.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
enum class Mailbox(val type: String) {
    INBOX("inbox"),
    SENT("sent")
}
