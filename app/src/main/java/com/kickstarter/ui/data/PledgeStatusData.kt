package com.kickstarter.ui.data

/**
 * UI component for PledgeStatusData.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
data class PledgeStatusData(val statusStringRes: Int?, val pledgeTotal: String?, val projectDeadline: String?, val plotData: PlotData?)
