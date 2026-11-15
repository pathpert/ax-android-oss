package com.kickstarter.ui.data

/**
 * UI component for PlotData.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
data class PlotData(val plotAmount: String?, val plotFirstScheduleCollection: String?, val fixPledgeUrl: String?,)
