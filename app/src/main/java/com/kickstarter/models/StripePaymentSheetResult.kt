package com.kickstarter.models

import com.stripe.android.paymentsheet.PaymentSheetResult

/**
 * Domain data model representing StripePaymentSheetResult.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
sealed class StripePaymentSheetResult {
    object Completed : StripePaymentSheetResult()
    object Canceled : StripePaymentSheetResult()
    data class Failed(val error: Throwable) : StripePaymentSheetResult()
}

fun PaymentSheetResult.toStripePaymentSheetResult() =
    when (this) {
        is PaymentSheetResult.Canceled -> StripePaymentSheetResult.Canceled
        is PaymentSheetResult.Failed -> StripePaymentSheetResult.Failed(error)
        is PaymentSheetResult.Completed -> StripePaymentSheetResult.Completed
    }
