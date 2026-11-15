package com.kickstarter.services

import android.net.Uri
import com.kickstarter.libs.utils.extensions.isDiscoverCategoriesPath
import com.kickstarter.libs.utils.extensions.isDiscoverPlacesPath
import com.kickstarter.libs.utils.extensions.isDiscoverScopePath
import com.kickstarter.libs.utils.extensions.isDiscoverSortParam
import com.kickstarter.libs.utils.extensions.toInteger

internal object DiscoveryParamsUriParser {

    fun fromUri(uri: Uri): DiscoveryParams {
        var builder = DiscoveryParams.builder()
        if (uri.isDiscoverCategoriesPath()) {
            builder = builder.categoryParam(uri.lastPathSegment)
        }
        if (uri.isDiscoverPlacesPath()) {
            builder = builder.locationParam(uri.lastPathSegment)
        }
        if (uri.isDiscoverScopePath("ending-soon")) {
            builder = builder.sort(DiscoveryParams.Sort.ENDING_SOON)
        }
        if (uri.isDiscoverScopePath("newest")) {
            builder = builder.sort(DiscoveryParams.Sort.NEWEST).staffPicks(true)
        }
        if (uri.isDiscoverSortParam()) {
            builder = builder.sort(
                uri.getQueryParameter("sort")?.let {
                    DiscoveryParams.Sort.fromString(it)
                }
            )
        }
        if (uri.isDiscoverScopePath("popular")) {
            builder = builder.sort(DiscoveryParams.Sort.POPULAR)
        }
        if (uri.isDiscoverScopePath("recently-launched")) {
            builder = builder.sort(DiscoveryParams.Sort.NEWEST)
        }
        if (uri.isDiscoverScopePath("small-projects")) {
            builder = builder.pledged(0)
        }
        if (uri.isDiscoverScopePath("social")) {
            builder = builder.social(0)
        }
        if (uri.isDiscoverScopePath("successful")) {
            builder = builder.sort(DiscoveryParams.Sort.ENDING_SOON).state(DiscoveryParams.State.SUCCESSFUL)
        }
        val backed = uri.getQueryParameter("backed").toInteger()
        if (backed != null) {
            builder = builder.backed(backed)
        }
        val categoryParam = uri.getQueryParameter("category_id")
        if (categoryParam != null) {
            builder = builder.categoryParam(categoryParam)
        }
        val locationParam = uri.getQueryParameter("woe_id")
        if (locationParam != null) {
            builder = builder.locationParam(locationParam)
        }
        val page = uri.getQueryParameter("page").toInteger()
        if (page != null) {
            builder = builder.page(page)
        }
        val perPage = uri.getQueryParameter("per_page").toInteger()
        if (perPage != null) {
            builder = builder.perPage(perPage)
        }
        val pledged = uri.getQueryParameter("pledged").toInteger()
        if (pledged != null) {
            builder = builder.pledged(pledged)
        }
        val recommended = uri.getBooleanQueryParameter("recommended", false)
        if (recommended) {
            builder = builder.recommended(recommended)
        }
        val sort = uri.getQueryParameter("sort")
        if (sort != null) {
            builder = builder.sort(DiscoveryParams.Sort.fromString(sort))
        }
        val staffPicks = uri.getBooleanQueryParameter("staff_picks", false)
        if (staffPicks) {
            builder = builder.staffPicks(staffPicks)
        }
        val starred = uri.getQueryParameter("starred").toInteger()
        if (starred != null) {
            builder = builder.starred(starred)
        }
        val social = uri.getQueryParameter("social").toInteger()
        if (social != null) {
            builder = builder.social(social)
        }
        val state = uri.getQueryParameter("state")
        if (state != null) {
            builder = builder.state(DiscoveryParams.State.fromString(state))
        }
        val term = uri.getQueryParameter("term")
        if (term != null) {
            builder = builder.term(term)
        }
        return builder.build()
    }
}
