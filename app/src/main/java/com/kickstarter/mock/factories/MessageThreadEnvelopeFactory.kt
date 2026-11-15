package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.MessageFactory.message
import com.kickstarter.mock.factories.MessageThreadFactory.messageThread
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.services.apiresponses.MessageThreadEnvelope.Companion.builder

/**
 * Test mock implementation for MessageThreadEnvelopeFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object MessageThreadEnvelopeFactory {
    @JvmStatic
    fun empty(): MessageThreadEnvelope {
        return messageThreadEnvelope()
            .toBuilder()
            .messages(null)
            .build()
    }

    @JvmStatic
    fun messageThreadEnvelope(): MessageThreadEnvelope {
        return builder()
            .messages(listOf(message()))
            .messageThread(messageThread())
            .participants(listOf(user()))
            .build()
    }
}
