package com.kickstarter.features.pledgedprojectsoverview.extensions

import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PPOCardViewTypeExtTest {

    @Test
    fun `isTier1Alert identifies tier 1 actionable card view types`() {
        assertTrue(PPOCardViewType.CONFIRM_ADDRESS.isTier1Alert())
        assertTrue(PPOCardViewType.AUTHENTICATE_CARD.isTier1Alert())
        assertTrue(PPOCardViewType.OPEN_SURVEY.isTier1Alert())
        assertTrue(PPOCardViewType.FIX_PAYMENT.isTier1Alert())

        assertFalse(PPOCardViewType.ADDRESS_CONFIRMED.isTier1Alert())
        assertFalse(PPOCardViewType.PLEDGE_COLLECTED_REWARD.isTier1Alert())
        assertFalse(PPOCardViewType.REWARD_RECEIVED.isTier1Alert())
    }

    @Test
    fun `isTier2Type identifies tier 2 progression card view types`() {
        assertTrue(PPOCardViewType.ADDRESS_CONFIRMED.isTier2Type())
        assertTrue(PPOCardViewType.PLEDGE_COLLECTED_REWARD.isTier2Type())
        assertTrue(PPOCardViewType.PLEDGE_COLLECTED_NO_REWARD.isTier2Type())
        assertTrue(PPOCardViewType.SURVEY_SUBMITTED_DIGITAL.isTier2Type())
        assertTrue(PPOCardViewType.SURVEY_SUBMITTED_NO_REWARD.isTier2Type())
        assertTrue(PPOCardViewType.SURVEY_SUBMITTED_SHIPPABLE.isTier2Type())
        assertTrue(PPOCardViewType.AWAITING_REWARD.isTier2Type())
        assertTrue(PPOCardViewType.PLEDGE_MANAGEMENT.isTier2Type())
        assertTrue(PPOCardViewType.REWARD_RECEIVED.isTier2Type())

        assertFalse(PPOCardViewType.CONFIRM_ADDRESS.isTier2Type())
        assertFalse(PPOCardViewType.OPEN_SURVEY.isTier2Type())
    }
}

