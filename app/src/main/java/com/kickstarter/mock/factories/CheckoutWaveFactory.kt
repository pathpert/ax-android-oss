package com.kickstarter.mock.factories

import com.kickstarter.models.CheckoutWave

/**
 * Test mock implementation for CheckoutWaveFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
class CheckoutWaveFactory {
    companion object {
        fun checkoutWaveActive(): CheckoutWave {
            return CheckoutWave.builder()
                .id(-1)
                .active(true)
                .build()
        }
        fun checkoutWaveInactive(): CheckoutWave {
            return CheckoutWave.builder()
                .id(-1)
                .active(false)
                .build()
        }
    }
}
