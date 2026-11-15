package com.kickstarter.mock.factories

import com.kickstarter.models.Activity
import com.kickstarter.services.apiresponses.ActivityEnvelope

/**
 * Test mock implementation for ActivityEnvelopeFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object ActivityEnvelopeFactory {
    @JvmStatic
    fun activityEnvelope(activities: List<Activity>): ActivityEnvelope {
        return ActivityEnvelope.builder()
            .activities(activities)
            .urls(
                ActivityEnvelope.UrlsEnvelope.builder()
                    .api(
                        ActivityEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreActivities("")
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
