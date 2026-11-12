package com.kickstarter.features.pledgedprojectsoverview.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PledgeTierTypeTest {

    @Test
    fun `isTier1Type returns true for tier 1 alert statuses`() {
        assertTrue(PledgeTierType.FAILED_PAYMENT.isTier1Type())
        assertTrue(PledgeTierType.ADDRESS_LOCK.isTier1Type())
        assertTrue(PledgeTierType.SURVEY_OPEN.isTier1Type())
        assertTrue(PledgeTierType.PAYMENT_AUTHENTICATION.isTier1Type())

        assertFalse(PledgeTierType.FAILED_PAYMENT.isTier2Type())
        assertFalse(PledgeTierType.ADDRESS_LOCK.isTier2Type())
        assertFalse(PledgeTierType.SURVEY_OPEN.isTier2Type())
        assertFalse(PledgeTierType.PAYMENT_AUTHENTICATION.isTier2Type())
    }

    @Test
    fun `isTier2Type returns true for tier 2 informational progression statuses`() {
        assertTrue(PledgeTierType.ADDRESS_CONFIRMED.isTier2Type())
        assertTrue(PledgeTierType.PLEDGE_COLLECTED.isTier2Type())
        assertTrue(PledgeTierType.SURVEY_SUBMITTED.isTier2Type())
        assertTrue(PledgeTierType.AWAITING_REWARD.isTier2Type())
        assertTrue(PledgeTierType.PLEDGE_MANAGEMENT.isTier2Type())
        assertTrue(PledgeTierType.REWARD_RECEIVED.isTier2Type())

        assertFalse(PledgeTierType.ADDRESS_CONFIRMED.isTier1Type())
        assertFalse(PledgeTierType.PLEDGE_COLLECTED.isTier1Type())
        assertFalse(PledgeTierType.SURVEY_SUBMITTED.isTier1Type())
        assertFalse(PledgeTierType.AWAITING_REWARD.isTier1Type())
        assertFalse(PledgeTierType.PLEDGE_MANAGEMENT.isTier1Type())
        assertFalse(PledgeTierType.REWARD_RECEIVED.isTier1Type())
    }

    @Test
    fun `tierType string matches GraphQL contract schema`() {
        assertEquals("Tier1PaymentFailed", PledgeTierType.FAILED_PAYMENT.tierType)
        assertEquals("Tier1AddressLockingSoon", PledgeTierType.ADDRESS_LOCK.tierType)
        assertEquals("Tier1OpenSurvey", PledgeTierType.SURVEY_OPEN.tierType)
        assertEquals("Tier1PaymentAuthenticationRequired", PledgeTierType.PAYMENT_AUTHENTICATION.tierType)
        assertEquals("PledgeCollected", PledgeTierType.PLEDGE_COLLECTED.tierType)
        assertEquals("SurveySubmitted", PledgeTierType.SURVEY_SUBMITTED.tierType)
        assertEquals("AddressConfirmed", PledgeTierType.ADDRESS_CONFIRMED.tierType)
        assertEquals("AwaitingReward", PledgeTierType.AWAITING_REWARD.tierType)
        assertEquals("PledgeManagement", PledgeTierType.PLEDGE_MANAGEMENT.tierType)
        assertEquals("RewardReceived", PledgeTierType.REWARD_RECEIVED.tierType)
    }
}

