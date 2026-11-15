package com.kickstarter.models

/**
 * Domain data model representing BuildPaymentPlanData.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
data class BuildPaymentPlanData(
    val slug: String,
    val amount: String,
)
