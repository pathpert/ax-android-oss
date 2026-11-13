package com.kickstarter.features.backing.data

import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.mock.factories.BackingFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackingContractsTest {

    @Test
    fun `test BackingState Loading invariant`() {
        val state: BackingState = BackingState.Loading
        assertTrue(state is BackingState.Loading)
    }

    @Test
    fun `test BackingState Success invariant`() {
        val backing = BackingFactory.backing()
        val state: BackingState = BackingState.Success(backing)
        assertTrue(state is BackingState.Success)
        assertEquals(backing, (state as BackingState.Success).backing)
    }

    @Test
    fun `test BackingState Error encapsulates RFC 9457 ProblemDetails`() {
        val problem = ProblemDetails(
            type = ProblemDetails.TYPE_SERVER,
            title = "Server Error",
            status = 500,
            detail = "Internal server failure while fetching backing",
            suggestedAction = "Please retry after a moment",
            isRetriable = true
        )
        val state: BackingState = BackingState.Error(problem)
        assertTrue(state is BackingState.Error)
        val errorState = state as BackingState.Error
        assertEquals(problem, errorState.problem)
        assertEquals(500, errorState.problem.status)
        assertTrue(errorState.problem.isRetriable)
        assertNotNull(errorState.throwable)
        assertEquals("Internal server failure while fetching backing", errorState.throwable.message)
    }
}
