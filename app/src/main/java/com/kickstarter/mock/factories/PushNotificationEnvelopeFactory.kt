package com.kickstarter.mock.factories

import com.kickstarter.models.pushdata.GCM.Companion.builder
import com.kickstarter.services.apiresponses.PushNotificationEnvelope

/**
 * Test mock implementation for PushNotificationEnvelopeFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object PushNotificationEnvelopeFactory {
    fun envelope(): PushNotificationEnvelope {
        val gcm = builder()
            .alert("You've received a new push notification")
            .title("Hello")
            .build()
        return PushNotificationEnvelope.builder()
            .gcm(gcm)
            .build()
    }
}
