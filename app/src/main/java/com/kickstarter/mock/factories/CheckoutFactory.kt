package com.kickstarter.mock.factories

import com.kickstarter.models.Checkout

/**
 * Test mock implementation for CheckoutFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object CheckoutFactory {
    @JvmStatic
    fun requiresAction(requiresAction: Boolean): Checkout {
        return Checkout.builder()
            .id(IdFactory.id().toLong())
            .backing(CheckoutBackingFactory.requiresAction(requiresAction))
            .build()
    }
}
