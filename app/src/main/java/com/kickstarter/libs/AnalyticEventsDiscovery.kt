package com.kickstarter.libs

import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_LOCATION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_TYPE
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.DISCOVER
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.SEARCH
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_FILTER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_SORT
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.ALL
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.CATEGORY_NAME
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.PWL
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.RECOMMENDED
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.RESULTS
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.SOCIAL
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.SUBCATEGORY_NAME
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.WATCHED
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.DISCOVER_ADVANCED
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.DISCOVER_OVERLAY
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.GLOBAL_NAV
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.SEARCH_RESULTS
import com.kickstarter.libs.utils.EventName.CTA_CLICKED
import com.kickstarter.libs.utils.EventName.PAGE_VIEWED
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.ProjectData
import java.util.Locale

internal class AnalyticEventsDiscovery(private val client: AnalyticEvents.ProxyClient) {

    fun trackDiscoverSortCTA(currentSort: DiscoveryParams.Sort, discoverParams: DiscoveryParams) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to DISCOVER_SORT.contextName)
        props[CONTEXT_LOCATION.contextName] = DISCOVER_ADVANCED.contextName
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        props[CONTEXT_TYPE.contextName] = discoverParams.sort()?.let {
            when (it) {
                DiscoveryParams.Sort.POPULAR -> "popular"
                DiscoveryParams.Sort.ENDING_SOON -> "ending_soon"
                else -> it.toString()
            }
        } ?: ""
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoverParams, currentSort))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackDiscoverSearchResultProjectCATClicked(
        discoveryParams: DiscoveryParams,
        projectData: ProjectData,
        count: Int,
        sort: DiscoveryParams.Sort
    ) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PROJECT.contextName)
        props[CONTEXT_PAGE.contextName] = SEARCH.contextName
        props[CONTEXT_LOCATION.contextName] = SEARCH_RESULTS.contextName
        props[CONTEXT_TYPE.contextName] = EventContextValues.ContextTypeName.RESULTS.contextName
        discoveryParams.term()?.let { props["discover_search_term"] = it }
        props["discover_search_results_count"] = count
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoveryParams, sort).toMutableMap())
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackDiscoverProjectCtaClicked(discoveryParams: DiscoveryParams, projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PROJECT.contextName)
        props[CONTEXT_LOCATION.contextName] = DISCOVER_ADVANCED.contextName
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        props[CONTEXT_TYPE.contextName] = when {
            discoveryParams.recommended().isTrue() -> RECOMMENDED.contextName
            discoveryParams.category()?.isRoot.isTrue() ||
                discoveryParams.category() != null ||
                discoveryParams.staffPicks().isTrue() ||
                discoveryParams.isAllProjects.isTrue() -> RESULTS.contextName
            else -> ""
        }

        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoveryParams))

        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackDiscoveryPageViewed(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams).toMutableMap()
        props[DISCOVER_SORT.contextName] = discoveryParams.sort()?.name?.lowercase(Locale.ROOT) ?: ""
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackDiscoverFilterCTA(discoveryParams: DiscoveryParams) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to DISCOVER_FILTER.contextName)
        props[CONTEXT_LOCATION.contextName] = DISCOVER_OVERLAY.contextName
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        props[CONTEXT_TYPE.contextName] = when {
            discoveryParams.staffPicks().isTrue() -> PWL.contextName
            discoveryParams.recommended().isTrue() -> RECOMMENDED.contextName
            discoveryParams.starred().isNonZero() -> WATCHED.contextName
            discoveryParams.social().isNonZero() -> SOCIAL.contextName
            discoveryParams.category()?.isRoot.isTrue() -> CATEGORY_NAME.contextName
            discoveryParams.category() != null -> SUBCATEGORY_NAME.contextName
            discoveryParams.isAllProjects.isTrue() -> ALL.contextName
            else -> ALL.contextName
        }
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoveryParams))

        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackSearchCTAButtonClicked(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams).toMutableMap()
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        props[CONTEXT_CTA.contextName] = com.kickstarter.libs.utils.EventContextValues.CtaContextName.SEARCH.contextName
        props[CONTEXT_LOCATION.contextName] = GLOBAL_NAV.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackSearchResultPageViewed(discoveryParams: DiscoveryParams, count: Int, sort: DiscoveryParams.Sort) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams, sort).toMutableMap()
        props[CONTEXT_PAGE.contextName] = SEARCH.contextName
        discoveryParams.term()?.let { props["discover_search_term"] = it }
        props["discover_search_results_count"] = count
        client.track(PAGE_VIEWED.eventName, props)
    }
}
