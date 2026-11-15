package com.kickstarter.mock.factories

import com.kickstarter.models.CreatorDetails

/**
 * Test mock implementation for CreatorDetailsFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object CreatorDetailsFactory {
    @JvmStatic
    fun creatorDetails(): CreatorDetails {
        return CreatorDetails.builder()
            .backingsCount(3)
            .launchedProjectsCount(2)
            .build()
    }
}
