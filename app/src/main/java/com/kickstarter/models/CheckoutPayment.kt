package com.kickstarter.models

/**
 * Domain data model representing CheckoutPayment.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
data class CheckoutPayment(val id: Long, val paymentUrl: String?, val backing: Backing? = null)
