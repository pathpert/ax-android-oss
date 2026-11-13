package com.kickstarter.features.discovery.data

import android.content.SharedPreferences
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.DISCOVER
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.ProjectData
import java.net.CookieManager

/**
 * Isolated analytics tracking dispatcher for Project Discovery interactions.
 *
 * Negative constraints:
 * - Do not mutate project, params, or cookie state within tracking dispatchers.
 * - Do not dispatch events with uninitialized analytics instances.
 */
object DiscoveryAnalyticsTracker {

    /**
     * Tracks when a discovery project card is tapped.
     */
    fun trackCardClick(analytics: AnalyticEvents, project: Project) {
        analytics.trackProjectCardClicked(project, DISCOVER.contextName)
    }

    /**
     * Tracks discovery CTA button clicks with resolved intent and cookie ref-tags.
     */
    fun trackCtaClick(
        analytics: AnalyticEvents,
        params: DiscoveryParams,
        project: Project,
        cookieManager: CookieManager,
        sharedPreferences: SharedPreferences
    ) {
        val refTag = RefTagUtils.projectAndRefTagFromParamsAndProject(params, project)
        val cookieRefTag = RefTagUtils.storedCookieRefTagForProject(project, cookieManager, sharedPreferences)
        val projectData = ProjectData.builder()
            .refTagFromIntent(refTag.second)
            .refTagFromCookie(cookieRefTag)
            .project(project)
            .build()
        analytics.trackDiscoverProjectCtaClicked(params, projectData)
    }

    /**
     * Tracks the discovery screen page view with current filter params.
     */
    fun trackPageView(analytics: AnalyticEvents, params: DiscoveryParams) {
        analytics.trackDiscoveryPageViewed(params)
    }

    /**
     * Tracks click on the login tout within discovery feed.
     */
    fun trackLoginTout(analytics: AnalyticEvents) {
        analytics.trackLoginOrSignUpCtaClicked(null, DISCOVER.contextName)
    }

    /**
     * Tracks project bookmark/watch action from discovery.
     */
    fun trackWatch(analytics: AnalyticEvents, project: Project) {
        analytics.trackWatchProjectCTA(project, DISCOVER)
    }
}
