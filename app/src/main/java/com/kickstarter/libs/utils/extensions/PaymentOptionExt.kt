package com.kickstarter.libs.utils.extensions

import com.stripe.android.model.PaymentMethod
import com.stripe.android.paymentsheet.model.PaymentOption

const val PAYMENT_METHOD_TYPE_GOOGLE_PAY = "google_pay"

/**
 * Utility and helper extensions for PaymentOption.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
fun PaymentOption.isGooglePay(): Boolean =
    this.paymentMethodType == PAYMENT_METHOD_TYPE_GOOGLE_PAY

fun PaymentOption.isLink(): Boolean =
    this.paymentMethodType == PaymentMethod.Type.Link.code
