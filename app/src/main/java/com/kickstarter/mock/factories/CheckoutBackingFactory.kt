package com.kickstarter.mock.factories

import com.kickstarter.models.Checkout

/**
 * Test mock implementation for CheckoutBackingFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object CheckoutBackingFactory {
    @JvmStatic
    fun requiresAction(requiresAction: Boolean): Checkout.Backing {
        return Checkout.Backing.builder()
            .clientSecret(if (requiresAction) "boop" else null)
            .requiresAction(requiresAction)
            .build()
    }
}
