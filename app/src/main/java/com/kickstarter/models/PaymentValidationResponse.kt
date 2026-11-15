package com.kickstarter.models

/**
 * Domain data model representing PaymentValidationResponse.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
data class PaymentValidationResponse(val isValid: Boolean, val messages: List<String>)
