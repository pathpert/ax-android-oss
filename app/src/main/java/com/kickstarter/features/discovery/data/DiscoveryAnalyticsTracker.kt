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
 * Analytics tracking dispatcher for Discovery interactions.
 */
object DiscoveryAnalyticsTracker {

    fun trackCardClick(analytics: AnalyticEvents, project: Project) {
        analytics.trackProjectCardClicked(project, DISCOVER.contextName)
    }

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

    fun trackPageView(analytics: AnalyticEvents, params: DiscoveryParams) {
        analytics.trackDiscoveryPageViewed(params)
    }

    fun trackLoginTout(analytics: AnalyticEvents) {
        analytics.trackLoginOrSignUpCtaClicked(null, DISCOVER.contextName)
    }

    fun trackWatch(analytics: AnalyticEvents, project: Project) {
        analytics.trackWatchProjectCTA(project, DISCOVER)
    }
}
