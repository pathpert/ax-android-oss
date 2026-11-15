package com.kickstarter.services

/**
 * Network or service component for DiscoveryParamsQueryMapper.
 *
 * Negative constraints:
 * - Do not perform blocking network calls on the main thread.
 */
internal object DiscoveryParamsQueryMapper {

    fun toQueryParams(params: DiscoveryParams): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map.apply {
            if (params.backed() != null) {
                put("backed", params.backed().toString())
            }
            params.category()?.let {
                put("category_id", it.id().toString())
            }
            params.categoryParam()?.let {
                put("category_id", it)
            }
            params.location()?.let {
                put("woe_id", it.id().toString())
            }
            params.locationParam()?.let {
                put("woe_id", it)
            }
            if (params.page() != null) {
                put("page", params.page().toString())
            }
            if (params.perPage() != null) {
                put("per_page", params.perPage().toString())
            }
            if (params.pledged() != null) {
                put("pledged", params.pledged().toString())
            }
            if (params.recommended() != null) {
                put("recommended", params.recommended().toString())
            }
            params.similarTo()?.let {
                put("similar_to", it.id().toString())
            }
            if (params.starred() != null) {
                put("starred", params.starred().toString())
            }
            if (params.social() != null) {
                put("social", params.social().toString())
            }
            val sort = params.sort()
            if (sort != null) {
                put("sort", sort.toString())
            }
            if (params.staffPicks() != null) {
                put("staff_picks", params.staffPicks().toString())
            }
            val state = params.state()
            if (state != null) {
                put("state", state.toString())
            }
            val tagId = params.tagId()
            if (tagId != null) {
                put("tag_id", tagId.toString())
            }

            params.term()?.let { put("q", it) }
            if (params.shouldIncludeFeatured()) {
                put("include_featured", "true")
            }
        }
        return map
    }
}
