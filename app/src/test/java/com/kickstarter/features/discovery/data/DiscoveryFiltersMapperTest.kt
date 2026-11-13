package com.kickstarter.features.discovery.data

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockStatsigClient
import com.kickstarter.mock.factories.ActivityFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.services.DiscoveryParams
import org.junit.Test

class DiscoveryFiltersMapperTest : KSRobolectricTestCase() {

    @Test
    fun testIsDefaultParams() {
        val user = UserFactory.user()
        val defaultParams = DiscoveryParams.getDefaultParams(user)
        val nonDefaultParams = DiscoveryParams.builder().term("art").build()

        assertTrue(DiscoveryFiltersMapper.isDefaultParams(Pair(user, defaultParams)))
        assertFalse(DiscoveryFiltersMapper.isDefaultParams(Pair(user, nonDefaultParams)))
    }

    @Test
    fun testIsOnboardingVisible() {
        val magicHomeParams = DiscoveryParams.builder()
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()

        assertTrue(DiscoveryFiltersMapper.isOnboardingVisible(magicHomeParams, isLoggedIn = false))
        assertFalse(DiscoveryFiltersMapper.isOnboardingVisible(magicHomeParams, isLoggedIn = true))

        val popularParams = DiscoveryParams.builder()
            .sort(DiscoveryParams.Sort.POPULAR)
            .build()
        assertFalse(DiscoveryFiltersMapper.isOnboardingVisible(popularParams, isLoggedIn = false))
    }

    @Test
    fun testIsSavedVisible() {
        val savedParams = DiscoveryParams.builder().starred(1).build()
        val notSavedParams = DiscoveryParams.builder().build()

        assertTrue(DiscoveryFiltersMapper.isSavedVisible(savedParams))
        assertFalse(DiscoveryFiltersMapper.isSavedVisible(notSavedParams))
    }

    @Test
    fun testActivityHasNotBeenSeen() {
        val activity = ActivityFactory.activity()
        val activityId = activity.id().toInt()

        assertTrue(DiscoveryFiltersMapper.activityHasNotBeenSeen(activity, lastSeenId = activityId + 1))
        assertFalse(DiscoveryFiltersMapper.activityHasNotBeenSeen(activity, lastSeenId = activityId))
        assertFalse(DiscoveryFiltersMapper.activityHasNotBeenSeen(null, lastSeenId = null))
    }

    @Test
    fun testVideoFeedBannerVisibility() {
        val statsigClient = MockStatsigClient(context())
        val homeParams = DiscoveryParams.builder()
            .sort(DiscoveryParams.Sort.MAGIC)
            .build()

        // When config is not ready
        val notReady = DiscoveryFiltersMapper.videoFeedBannerVisibility(homeParams, configReady = false, statsigClient)
        assertNull(notReady)

        // When not a valid surface
        val popularParams = DiscoveryParams.builder()
            .sort(DiscoveryParams.Sort.POPULAR)
            .build()
        val popularResult = DiscoveryFiltersMapper.videoFeedBannerVisibility(popularParams, configReady = true, statsigClient)
        assertEquals(false, popularResult)
    }
}
