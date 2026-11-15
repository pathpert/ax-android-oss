package com.kickstarter.ui.fragments

/**
 * Fragment presenting UI and collecting view states for Callbacks.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
interface Callbacks {
    fun onDismiss()
}
