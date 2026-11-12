package com.kickstarter.features.cancelpledge.data

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.ArgumentsKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CancelPledgeBundleMapperTest : KSRobolectricTestCase() {

    @Test
    fun `fromBundle maps valid backed project into CancelPledgeTarget`() {
        val project = ProjectFactory.backedProject()
        val backing = requireNotNull(project.backing())
        val bundle = Bundle().apply {
            putParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT, project)
        }

        val result = CancelPledgeBundleMapper.fromBundle(bundle, environment().ksCurrency())

        assertTrue(result is DomainResult.Success)
        val target = (result as DomainResult.Success).value
        assertEquals(project.id().toString(), target.projectId.value)
        assertEquals(backing.id().toString(), target.backingId.value)
        assertEquals(backing.id(), target.backingNumericId)
        assertEquals(project.name(), target.projectName)
        assertTrue(target.formattedPledgeAmount.isNotBlank())
        assertTrue(target.pledgeAmount.minorUnits > 0L)
    }

    @Test
    fun `fromBundle rejects null bundle with invalidParams`() {
        val result = CancelPledgeBundleMapper.fromBundle(bundle = null, ksCurrency = null)

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals(400, problem.status)
        assertFalse(problem.isRetriable)
        assertEquals(ArgumentsKey.CANCEL_PLEDGE_PROJECT, problem.invalidParams.single().name)
    }

    @Test
    fun `fromBundle rejects project without active backing`() {
        val bundle = Bundle().apply {
            putParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT, ProjectFactory.project())
        }

        val result = CancelPledgeBundleMapper.fromBundle(bundle, environment().ksCurrency())

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals("project.backing", problem.invalidParams.single().name)
    }
}
