package com.kickstarter.features.checkout.data

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PledgeInfoExtractorTest : KSRobolectricTestCase() {

    @Test
    fun `test fromBacking extracts rewards, amounts, and checkoutData`() {
        val reward = RewardFactory.reward()
        val backing = BackingFactory.backing().toBuilder()
            .reward(reward)
            .amount(120.0)
            .shippingAmount(15.0f)
            .bonusAmount(5.0)
            .build()

        val extracted = PledgeInfoExtractor.fromBacking(backing)

        assertEquals(1, extracted.selectedRewards.size)
        assertEquals(reward, extracted.selectedRewards[0])
        assertEquals(120.0, extracted.totalAmount, 0.001)
        assertEquals(15.0, extracted.shippingAmount, 0.001)
        assertEquals(5.0, extracted.bonusAmount, 0.001)
        assertNotNull(extracted.checkoutData)
        assertEquals(120.0, extracted.checkoutData?.amount() ?: 0.0, 0.001)
    }

    @Test
    fun `test fromPledgeData with standard rewards and shipping rule`() {
        val reward = RewardFactory.reward().toBuilder().minimum(30.0).build()
        val shippingRule = ShippingRuleFactory.usShippingRule()
        val project = ProjectFactory.project()
        val projectData = ProjectDataFactory.project(project)
        val pledgeData = PledgeData.builder()
            .pledgeFlowContext(PledgeFlowContext.NEW_PLEDGE)
            .projectData(projectData)
            .reward(reward)
            .shippingRule(shippingRule)
            .build()

        val extracted = PledgeInfoExtractor.fromPledgeData(pledgeData)

        assertTrue(extracted.selectedRewards.isNotEmpty())
        assertEquals(shippingRule, extracted.shippingRule)
        assertNotNull(extracted.checkoutData)
    }
}
