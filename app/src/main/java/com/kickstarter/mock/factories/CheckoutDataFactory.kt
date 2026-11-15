package com.kickstarter.mock.factories

import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.data.CheckoutData

/**
 * Test mock implementation for CheckoutDataFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object CheckoutDataFactory {
    @JvmStatic
    fun checkoutData(shippingAmount: Double, totalAmount: Double): CheckoutData {
        return CheckoutData.builder()
            .amount(totalAmount)
            .paymentType(CreditCardPaymentType.CREDIT_CARD)
            .shippingAmount(shippingAmount)
            .build()
    }

    @JvmStatic
    fun checkoutData(id: Long, shippingAmount: Double, totalAmount: Double): CheckoutData {
        return checkoutData(shippingAmount, totalAmount)
            .toBuilder()
            .id(id)
            .build()
    }
}
