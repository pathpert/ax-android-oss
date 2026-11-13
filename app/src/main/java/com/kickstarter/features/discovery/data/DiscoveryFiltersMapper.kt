package com.kickstarter.features.discovery.data

import android.util.Pair
import com.kickstarter.libs.featureflag.StatsigClientType
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Activity
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.statsig.androidsdk.EvalReason

/**
 * Pure boundary mapping functions and visibility predicate evaluations for the Discovery feature.
 *
 * Negative constraints:
 * - Do not perform side effects or network requests within these functions.
 * - Do not pass null [params] or uninitialized [statsigClient].
 */
object DiscoveryFiltersMapper {

    /**
     * Determines whether given params match the user's default discovery configuration.
     */
    fun isDefaultParams(userAndParams: Pair<User, DiscoveryParams>): Boolean {
        val user = userAndParams.first
        val discoveryParams = userAndParams.second
        return discoveryParams == DiscoveryParams.getDefaultParams(user)
    }

    /**
     * Determines whether the onboarding banner should be visible.
     */
    fun isOnboardingVisible(params: DiscoveryParams, isLoggedIn: Boolean): Boolean {
        val isSortHome = DiscoveryParams.Sort.MAGIC == params.sort()
        return params.isAllProjects.isTrue() && isSortHome && !isLoggedIn
    }

    /**
     * Single source of truth for the video feed banner's visibility for [params].
     * Returns `true` or `false` when known, or `null` when [StatsigGateKey.ANDROID_VIDEO_FEED] is still loading.
     */
    fun videoFeedBannerVisibility(
        params: DiscoveryParams,
        configReady: Boolean,
        statsigClient: StatsigClientType
    ): Boolean? {
        val isSortHome = DiscoveryParams.Sort.MAGIC == params.sort()
        val isHomeView = params.isAllProjects.isTrue() && isSortHome
        val isPwl = params.staffPicks().isTrue() && isSortHome
        if (!isHomeView && !isPwl) return false
        if (!configReady) return null
        val gate = statsigClient.getFeatureGate(StatsigGateKey.ANDROID_VIDEO_FEED.key)
        if (gate.getEvalDetails().reason == EvalReason.Unrecognized) return null
        return gate.getValue()
    }

    /**
     * Determines if the saved empty view should be visible.
     */
    fun isSavedVisible(params: DiscoveryParams): Boolean = params.isSavedProjects

    /**
     * Checks if an activity sample has not yet been seen by the user.
     */
    fun activityHasNotBeenSeen(activity: Activity?, lastSeenId: Int?): Boolean {
        return activity != null && activity.id() != lastSeenId?.toLong()
    }
}
