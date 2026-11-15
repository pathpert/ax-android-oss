package com.kickstarter.services

internal object DiscoveryParamsMerger {

    fun merge(base: DiscoveryParams.Builder, otherBuilder: DiscoveryParams.Builder): DiscoveryParams.Builder {
        val other = otherBuilder.build()
        var retVal = base
        if (other.backed() != null) retVal = retVal.backed(other.backed())
        if (other.category() != null) retVal = retVal.category(other.category())
        if (other.categoryParam() != null) retVal = retVal.categoryParam(other.categoryParam())
        if (other.location() != null) retVal = retVal.location(other.location())
        if (other.page() != null) retVal = retVal.page(other.page())
        if (other.perPage() != null) retVal = retVal.perPage(other.perPage())
        if (other.pledged() != null) retVal = retVal.pledged(other.pledged())
        if (other.social() != null) retVal = retVal.social(other.social())
        if (other.staffPicks() != null) retVal = retVal.staffPicks(other.staffPicks())
        if (other.starred() != null) retVal = retVal.starred(other.starred())
        if (other.state() != null) retVal = retVal.state(other.state())
        if (other.sort() != null) retVal = retVal.sort(other.sort())
        if (other.recommended() != null) retVal = retVal.recommended(other.recommended())
        if (other.similarTo() != null) retVal = retVal.similarTo(other.similarTo())
        if (other.tagId() != null) retVal = retVal.tagId(other.tagId())
        if (other.term() != null) retVal = retVal.term(other.term())
        if (other.raisedBucket() != null) retVal = retVal.raisedBucket(other.raisedBucket())
        if (other.amountRaisedBucket() != null) retVal = retVal.amountBucket(other.amountRaisedBucket())
        if (other.goalBucket() != null) retVal = retVal.goalBucket(other.goalBucket())
        return retVal
    }
}
