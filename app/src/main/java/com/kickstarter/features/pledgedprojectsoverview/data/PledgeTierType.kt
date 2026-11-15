package com.kickstarter.features.pledgedprojectsoverview.data

/**
 * Enumerates the prioritization tiers for backer actions in the Pledged Projects Overview.
 *
 * - Tier 1: Urgent actionable alerts requiring immediate user intervention (failed payment, survey open, address locking).
 * - Tier 2: Informational post-campaign progression states (pledge collected, reward received, address confirmed).
 *
 * Negative constraints:
 * - Do not pass arbitrary strings to [tierType]; adhere strictly to GraphQL schema enum values.
 * - Do not treat Tier 2 states as blocking user mutations.
 *
 * @property tierType Raw GraphQL schema enum string constant.
 */
enum class PledgeTierType(val tierType: String) {
    /** Tier 1: Backer payment collection failed and requires immediate card fix. */
    FAILED_PAYMENT("Tier1PaymentFailed"),
    /** Tier 1: Reward shipping address is locking soon. */
    ADDRESS_LOCK("Tier1AddressLockingSoon"),
    /** Tier 1: Creator survey is open and requires response. */
    SURVEY_OPEN("Tier1OpenSurvey"),
    /** Tier 1: 3D-Secure or bank SCA payment authentication required. */
    PAYMENT_AUTHENTICATION("Tier1PaymentAuthenticationRequired"),
    /** Tier 2: Pledge funds successfully collected. */
    PLEDGE_COLLECTED("PledgeCollected"),
    /** Tier 2: Creator survey successfully submitted by backer. */
    SURVEY_SUBMITTED("SurveySubmitted"),
    /** Tier 2: Delivery address confirmed by backer. */
    ADDRESS_CONFIRMED("AddressConfirmed"),
    /** Tier 2: Backer awaiting physical reward fulfillment. */
    AWAITING_REWARD("AwaitingReward"),
    /** Tier 2: Post-campaign pledge manager link available. */
    PLEDGE_MANAGEMENT("PledgeManagement"),
    /** Tier 2: Reward marked received. */
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
