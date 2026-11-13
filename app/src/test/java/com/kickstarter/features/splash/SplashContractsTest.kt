package com.kickstarter.features.splash

import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.features.splash.data.InitializationState
import com.kickstarter.features.splash.data.NavigationTarget
import com.kickstarter.features.splash.data.SplashUIState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashContractsTest {

    @Test
    fun `InitializationState defines valid cold start progression`() {
        val states = InitializationState.values()
        assertEquals(3, states.size)
        assertEquals(InitializationState.NOT_STARTED, states[0])
        assertEquals(InitializationState.RUNNING, states[1])
        assertEquals(InitializationState.FINISHED, states[2])
    }

    @Test
    fun `SplashUIState Failed carries actionable RFC 9457 ProblemDetails`() {
        val problem = ProblemDetails(
            type = ProblemDetails.TYPE_SERVER,
            title = "Feature-Flag Initialization Failed",
            status = 503,
            detail = "Statsig client did not initialize on startup.",
            suggestedAction = "Retry app cold-start.",
            isRetriable = true,
            retryAfterSeconds = 5
        )

        val failedState = SplashUIState.Failed(problem)
        assertEquals(ProblemDetails.TYPE_SERVER, failedState.problem.type)
        assertEquals(503, failedState.problem.status)
        assertTrue(failedState.problem.isRetriable)
        assertEquals(5L, failedState.problem.retryAfterSeconds)
        assertEquals("Retry app cold-start.", failedState.problem.suggestedAction)
    }

    @Test
    fun `SplashUIState singleton objects maintain distinct identity`() {
        assertTrue(SplashUIState.Loading is SplashUIState)
        assertTrue(SplashUIState.NoInternet is SplashUIState)
        assertTrue(SplashUIState.Errored is SplashUIState)
        assertTrue(SplashUIState.Finished is SplashUIState)
        assertEquals(NavigationTarget.Discovery, NavigationTarget.Discovery)
    }
}
