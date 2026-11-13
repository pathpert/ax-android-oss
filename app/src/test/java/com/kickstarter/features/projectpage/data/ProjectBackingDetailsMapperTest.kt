package com.kickstarter.features.projectpage.data

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Either
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.AI
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.CAMPAIGN
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ENVIRONMENT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.FAQS
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.OVERVIEW
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.RISKS
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.data.PledgeFlowContext
import org.junit.Test

class ProjectBackingDetailsMapperTest : KSRobolectricTestCase() {

    @Test
    fun testBackingDetailsIsVisible() {
        val backing = BackingFactory.backing()
        val liveBackedProject = ProjectFactory.project().toBuilder()
            .state(Project.STATE_LIVE)
            .isBacking(true)
            .backing(backing)
            .build()
        assertTrue(ProjectBackingDetailsMapper.backingDetailsIsVisible(liveBackedProject))

        val liveUnbackedProject = ProjectFactory.project().toBuilder()
            .state(Project.STATE_LIVE)
            .isBacking(false)
            .backing(null)
            .build()
        assertFalse(ProjectBackingDetailsMapper.backingDetailsIsVisible(liveUnbackedProject))

        val erroredBacking = BackingFactory.backing().toBuilder()
            .status(Backing.STATUS_ERRORED)
            .build()
        val nonLiveErroredProject = ProjectFactory.project().toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .isBacking(true)
            .backing(erroredBacking)
            .build()
        assertTrue(ProjectBackingDetailsMapper.backingDetailsIsVisible(nonLiveErroredProject))

        val nonLiveSuccessfulProject = ProjectFactory.project().toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .isBacking(true)
            .backing(backing)
            .build()
        assertFalse(ProjectBackingDetailsMapper.backingDetailsIsVisible(nonLiveSuccessfulProject))
    }

    @Test
    fun testBackingDetailsTitle() {
        val erroredBacking = BackingFactory.backing().toBuilder()
            .status(Backing.STATUS_ERRORED)
            .build()
        val erroredProject = ProjectFactory.project().toBuilder()
            .backing(erroredBacking)
            .build()
        assertEquals(R.string.Payment_failure, ProjectBackingDetailsMapper.backingDetailsTitle(erroredProject))

        val normalBacking = BackingFactory.backing()
        val normalProject = ProjectFactory.project().toBuilder()
            .backing(normalBacking)
            .build()
        assertEquals(R.string.Youre_a_backer, ProjectBackingDetailsMapper.backingDetailsTitle(normalProject))
    }

    @Test
    fun testBackingDetailsSubtitle() {
        val ksCurrency = requireNotNull(environment().ksCurrency())

        val unbackedProject = ProjectFactory.project().toBuilder().backing(null).build()
        assertNull(ProjectBackingDetailsMapper.backingDetailsSubtitle(unbackedProject, ksCurrency))

        val erroredBacking = BackingFactory.backing().toBuilder()
            .status(Backing.STATUS_ERRORED)
            .build()
        val erroredProject = ProjectFactory.project().toBuilder().backing(erroredBacking).build()
        val erroredSubtitle = ProjectBackingDetailsMapper.backingDetailsSubtitle(erroredProject, ksCurrency)
        assertTrue(erroredSubtitle is Either.Right)
        assertEquals(R.string.We_cant_process_your_pledge, (erroredSubtitle as Either.Right).right())

        val reward = RewardFactory.reward().toBuilder().id(123L).title("T-Shirt").build()
        val normalBacking = BackingFactory.backing().toBuilder()
            .status(Backing.STATUS_PLEDGED)
            .rewardId(123L)
            .amount(50.0)
            .build()
        val normalProject = ProjectFactory.project().toBuilder()
            .rewards(listOf(reward))
            .backing(normalBacking)
            .build()

        val normalSubtitle = ProjectBackingDetailsMapper.backingDetailsSubtitle(normalProject, ksCurrency)
        assertTrue(normalSubtitle is Either.Left)
        assertTrue((normalSubtitle as Either.Left).left()?.contains("T-Shirt") == true)
    }

    @Test
    fun testGetSelectedTabContextName() {
        assertEquals(OVERVIEW.contextName, ProjectBackingDetailsMapper.getSelectedTabContextName(ProjectPagerTabs.OVERVIEW.ordinal))
        assertEquals(CAMPAIGN.contextName, ProjectBackingDetailsMapper.getSelectedTabContextName(ProjectPagerTabs.CAMPAIGN.ordinal))
        assertEquals(FAQS.contextName, ProjectBackingDetailsMapper.getSelectedTabContextName(ProjectPagerTabs.FAQS.ordinal))
        assertEquals(RISKS.contextName, ProjectBackingDetailsMapper.getSelectedTabContextName(ProjectPagerTabs.RISKS.ordinal))
        assertEquals(AI.contextName, ProjectBackingDetailsMapper.getSelectedTabContextName(ProjectPagerTabs.USE_OF_AI.ordinal))
        assertEquals(ENVIRONMENT.contextName, ProjectBackingDetailsMapper.getSelectedTabContextName(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT.ordinal))
        assertEquals(OVERVIEW.contextName, ProjectBackingDetailsMapper.getSelectedTabContextName(999))
    }

    @Test
    fun testPledgeFlowContext() {
        val creator = UserFactory.creator()
        val creatorProject = ProjectFactory.project().toBuilder().creator(creator).build()
        assertNull(ProjectBackingDetailsMapper.pledgeFlowContext(creatorProject, creator))

        val liveUnbackedProject = ProjectFactory.project().toBuilder()
            .state(Project.STATE_LIVE)
            .isBacking(false)
            .build()
        assertEquals(PledgeFlowContext.NEW_PLEDGE, ProjectBackingDetailsMapper.pledgeFlowContext(liveUnbackedProject, UserFactory.user()))

        val erroredBacking = BackingFactory.backing().toBuilder().status(Backing.STATUS_ERRORED).build()
        val nonLiveErroredProject = ProjectFactory.project().toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .backing(erroredBacking)
            .build()
        assertEquals(PledgeFlowContext.FIX_ERRORED_PLEDGE, ProjectBackingDetailsMapper.pledgeFlowContext(nonLiveErroredProject, UserFactory.user()))
    }
}
