package com.kickstarter.models

/**
 * Domain data model representing CreatePaymentIntentInput.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
data class CreatePaymentIntentInput(val project: Project, val amount: String, val checkoutId: String, val backing: Backing)
