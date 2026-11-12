package com.kickstarter.features.pledgeredemption.data

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.IntentKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PledgeRedemptionIntentMapperTest : KSRobolectricTestCase() {

    @Test
    fun `fromBundle returns validated PledgeRedemptionArgs with minor units on valid backed project`() {
        val project = ProjectFactory.backedProject()
        val backing = requireNotNull(project.backing())
        val bundle = Bundle().apply {
            putParcelable(IntentKey.PROJECT, project)
        }

        val result = PledgeRedemptionIntentMapper.fromBundle(bundle, userEmail = "backer@kickstarter.com")

        assertTrue(result is DomainResult.Success)
        val args = (result as DomainResult.Success).value.first
        assertEquals(project.id().toString(), args.projectId.value)
        assertEquals(backing.id().toString(), args.backingId.value)
        assertEquals(project.name(), args.projectName)
        assertEquals("backer@kickstarter.com", args.userEmail)
        assertTrue(args.totalAmount.minorUnits > 0L)
    }

    @Test
    fun `fromBundle rejects null bundle with actionable ProblemDetails`() {
        val result = PledgeRedemptionIntentMapper.fromBundle(bundle = null)

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals(400, problem.status)
        assertFalse(problem.isRetriable)
        assertEquals(IntentKey.PROJECT, problem.invalidParams.single().name)
        assertTrue(problem.suggestedAction.isNotBlank())
    }

    @Test
    fun `fromBundle rejects unbacked project with missing backing invalidParam`() {
        val projectWithoutBacking = ProjectFactory.project()
        val bundle = Bundle().apply {
            putParcelable(IntentKey.PROJECT, projectWithoutBacking)
        }

        val result = PledgeRedemptionIntentMapper.fromBundle(bundle)

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals("project.backing", problem.invalidParams.single().name)
    }

    @Test
    fun `fromBundle rejects project with non-positive id`() {
        val invalidProject = ProjectFactory.backedProject().toBuilder().id(0L).build()
        val bundle = Bundle().apply {
            putParcelable(IntentKey.PROJECT, invalidProject)
        }

        val result = PledgeRedemptionIntentMapper.fromBundle(bundle)

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals("project.id", problem.invalidParams.single().name)
    }
}
