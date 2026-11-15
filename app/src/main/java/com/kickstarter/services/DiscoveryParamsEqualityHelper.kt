package com.kickstarter.services

import java.util.Objects

/**
 * Equality and hashing helper for [DiscoveryParams].
 */
object DiscoveryParamsEqualityHelper {

    fun areEqual(first: DiscoveryParams, other: Any?): Boolean {
        if (first === other) return true
        if (other !is DiscoveryParams) return false

        return first.backed() == other.backed() &&
            first.category() == other.category() &&
            first.categoryParam() == other.categoryParam() &&
            first.location() == other.location() &&
            first.locationParam() == other.locationParam() &&
            first.page() == other.page() &&
            first.perPage() == other.perPage() &&
            first.staffPicks() == other.staffPicks() &&
            first.starred() == other.starred() &&
            first.social() == other.social() &&
            first.sort() == other.sort() &&
            first.recommended() == other.recommended() &&
            first.similarTo() == other.similarTo() &&
            first.state() == other.state() &&
            first.tagId() == other.tagId() &&
            first.term() == other.term() &&
            first.raisedBucket() == other.raisedBucket() &&
            first.amountBucket() == other.amountBucket() &&
            first.goalBucket() == other.goalBucket()
    }

    fun hash(params: DiscoveryParams): Int {
        return Objects.hash(
            params.backed(),
            params.category(),
            params.categoryParam(),
            params.location(),
            params.locationParam(),
            params.page(),
            params.perPage(),
            params.staffPicks(),
            params.starred(),
            params.social(),
            params.sort(),
            params.recommended(),
            params.similarTo(),
            params.state(),
            params.tagId(),
            params.term(),
            params.raisedBucket(),
            params.amountBucket(),
            params.goalBucket()
        )
    }
}
