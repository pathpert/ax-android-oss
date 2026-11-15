package com.kickstarter.services.mutations

import com.kickstarter.type.PaymentTypes

/**
 * Network or service component for SavePaymentMethodData.
 *
 * Negative constraints:
 * - Do not perform blocking network calls on the main thread.
 */
data class SavePaymentMethodData(val paymentType: PaymentTypes = PaymentTypes.CREDIT_CARD, val stripeToken: String? = null, val stripeCardId: String? = null, val reusable: Boolean, val intentClientSecret: String? = null)
