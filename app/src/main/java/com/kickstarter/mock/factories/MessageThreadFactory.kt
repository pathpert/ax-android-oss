package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.MessageFactory.message
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.models.MessageThread
import com.kickstarter.models.MessageThread.Companion.builder

/**
 * Test mock implementation for MessageThreadFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object MessageThreadFactory {
    @JvmStatic
    fun messageThread(): MessageThread {
        return builder()
            .closed(false)
            .id(123455)
            .lastMessage(message())
            .participant(user())
            .project(project())
            .unreadMessagesCount(0)
            .build()
    }
}
