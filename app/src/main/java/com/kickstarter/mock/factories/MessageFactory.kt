package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.UserFactory.creator
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.models.Message
import com.kickstarter.models.Message.Companion.builder
import org.joda.time.DateTime

/**
 * Test mock implementation for MessageFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object MessageFactory {
    @JvmStatic
    fun message(): Message {
        return builder()
            .body("")
            .createdAt(DateTime.now())
            .id(123943059L)
            .recipient(creator())
            .sender(user())
            .build()
    }
}
