package com.kickstarter.features.discovery.data

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.services.DiscoveryParams
import org.junit.Test
import java.net.CookieManager

class DiscoveryAnalyticsTrackerTest : KSRobolectricTestCase() {

    @Test
    fun testTrackCardClick() {
        val analytics = requireNotNull(environment().analytics())
        val project = ProjectFactory.project()

        DiscoveryAnalyticsTracker.trackCardClick(analytics, project)
        segmentTrack.assertValue(EventName.CARD_CLICKED.eventName)
    }

    @Test
    fun testTrackPageView() {
        val analytics = requireNotNull(environment().analytics())
        val params = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()

        DiscoveryAnalyticsTracker.trackPageView(analytics, params)
        segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testTrackLoginTout() {
        val analytics = requireNotNull(environment().analytics())

        DiscoveryAnalyticsTracker.trackLoginTout(analytics)
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testTrackWatch() {
        val analytics = requireNotNull(environment().analytics())
        val project = ProjectFactory.project()

        DiscoveryAnalyticsTracker.trackWatch(analytics, project)
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testTrackCtaClick() {
        val analytics = requireNotNull(environment().analytics())
        val params = DiscoveryParams.builder().build()
        val project = ProjectFactory.project()
        val cookieManager = CookieManager()
        val sharedPreferences = requireNotNull(environment().sharedPreferences())

        DiscoveryAnalyticsTracker.trackCtaClick(analytics, params, project, cookieManager, sharedPreferences)
        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }
}
