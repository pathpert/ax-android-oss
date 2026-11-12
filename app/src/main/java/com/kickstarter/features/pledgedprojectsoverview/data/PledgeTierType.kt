package com.kickstarter.features.pledgedprojectsoverview.data

/**
 * Enumerates the prioritization tiers for backer actions in the Pledged Projects Overview.
 *
 * - Tier 1: Urgent actionable alerts requiring immediate user intervention (failed payment, survey open, address locking).
 * - Tier 2: Informational post-campaign progression states (pledge collected, reward received, address confirmed).
 *
 * ### Negative Constraints:
 * - Do not pass arbitrary strings to [tierType]; adhere strictly to GraphQL schema enum values.
 * - Do not treat Tier 2 states as blocking user mutations.
 */
enum class PledgeTierType(val tierType: String) {
    FAILED_PAYMENT("Tier1PaymentFailed"),
    ADDRESS_LOCK("Tier1AddressLockingSoon"),
    SURVEY_OPEN("Tier1OpenSurvey"),
    PAYMENT_AUTHENTICATION("Tier1PaymentAuthenticationRequired"),
    PLEDGE_COLLECTED("PledgeCollected"),
    SURVEY_SUBMITTED("SurveySubmitted"),
    ADDRESS_CONFIRMED("AddressConfirmed"),
    AWAITING_REWARD("AwaitingReward"),
    PLEDGE_MANAGEMENT("PledgeManagement"),
    REWARD_RECEIVED("RewardReceived")
}

fun PledgeTierType.isTier2Type(): Boolean {
    return when (this) {
        PledgeTierType.ADDRESS_CONFIRMED,
        PledgeTierType.PLEDGE_COLLECTED,
        PledgeTierType.SURVEY_SUBMITTED,
        PledgeTierType.AWAITING_REWARD,
        PledgeTierType.PLEDGE_MANAGEMENT,
        PledgeTierType.REWARD_RECEIVED -> true
        else -> false
    }
}

fun PledgeTierType.isTier1Type(): Boolean {
    return when (this) {
        PledgeTierType.FAILED_PAYMENT,
        PledgeTierType.ADDRESS_LOCK,
        PledgeTierType.SURVEY_OPEN,
        PledgeTierType.PAYMENT_AUTHENTICATION -> true
        else -> false
    }
}
