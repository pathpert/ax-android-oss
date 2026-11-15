package com.kickstarter.mock.factories

import com.kickstarter.services.apiresponses.ShippingRulesEnvelope

/**
 * Test mock implementation for ShippingRulesEnvelopeFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object ShippingRulesEnvelopeFactory {
    @JvmStatic
    fun shippingRules(): ShippingRulesEnvelope {
        return ShippingRulesEnvelope.builder()
            .shippingRules(
                listOf(
                    ShippingRuleFactory.usShippingRule(),
                    ShippingRuleFactory.germanyShippingRule(),
                    ShippingRuleFactory.mexicoShippingRule(),
                    ShippingRuleFactory.canadaShippingRule()
                )
            )
            .build()
    }

    @JvmStatic
    fun emptyShippingRules(): ShippingRulesEnvelope {
        return ShippingRulesEnvelope.builder()
            .shippingRules(listOf())
            .build()
    }
}
