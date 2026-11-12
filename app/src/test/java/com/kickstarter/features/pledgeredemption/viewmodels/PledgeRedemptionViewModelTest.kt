package com.kickstarter.features.pledgeredemption.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.features.pledgeredemption.data.PledgeRedemptionUiState
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.IntentKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PledgeRedemptionViewModelTest : KSRobolectricTestCase() {

    @Test
    fun `start emits Ready state for valid backed project`() {
        val project = ProjectFactory.backedProject()
        val bundle = Bundle().apply {
            putParcelable(IntentKey.PROJECT, project)
        }
        val vm = PledgeRedemptionViewModel.Factory(environment(), bundle)
            .create(PledgeRedemptionViewModel::class.java)

        vm.start()

        val state = vm.uiState.value
        assertTrue(state is PledgeRedemptionUiState.Ready)
        val ready = state as PledgeRedemptionUiState.Ready
        assertEquals(project.id().toString(), ready.args.projectId.value)
        assertEquals(project.name(), ready.args.projectName)
    }

    @Test
    fun `start emits Error state with ProblemDetails when bundle is missing`() {
        val vm = PledgeRedemptionViewModel.Factory(environment(), bundle = null)
            .create(PledgeRedemptionViewModel::class.java)

        vm.start()

        val state = vm.uiState.value
        assertTrue(state is PledgeRedemptionUiState.Error)
        val problem = (state as PledgeRedemptionUiState.Error).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals(IntentKey.PROJECT, problem.invalidParams.single().name)
    }

    @Test
    fun `start emits Error state when environment is null`() {
        val vm = PledgeRedemptionViewModel.Factory(environment = null, bundle = null)
            .create(PledgeRedemptionViewModel::class.java)

        vm.start()

        val state = vm.uiState.value
        assertTrue(state is PledgeRedemptionUiState.Error)
        assertEquals(ProblemDetails.TYPE_UNEXPECTED, (state as PledgeRedemptionUiState.Error).problem.type)
    }
}
