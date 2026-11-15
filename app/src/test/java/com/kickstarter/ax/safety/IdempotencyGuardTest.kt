package com.kickstarter.ax.safety

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdempotencyGuardTest {

    @After
    fun tearDown() {
        IdempotencyGuard.resetGlobalCooldown()
    }

    @Test
    fun `IdempotencyKey rejects blank input`() {
        val invalid = IdempotencyKey.create("   ")
        assertTrue(invalid is DomainResult.Failure)
        val problem = (invalid as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals("Idempotency-Key", problem.invalidParams.single().name)
    }

    @Test
    fun `runSingleFlight rejects overlapping concurrent mutations`() = runBlocking {
        val guard = IdempotencyGuard()
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()

        val first = async {
            guard.runSingleFlight("cancel pledge") {
                started.complete(Unit)
                release.await()
                DomainResult.Success("canceled")
            }
        }

        started.await()
        assertTrue(guard.isInFlight())

        val duplicate = guard.runSingleFlight("cancel pledge") {
            DomainResult.Success("should-not-run")
        }

        assertTrue(duplicate is DomainResult.Failure)
        val duplicateProblem = (duplicate as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_IN_FLIGHT, duplicateProblem.type)
        assertEquals(409, duplicateProblem.status)
        assertFalse(duplicateProblem.isRetriable)

        release.complete(Unit)
        val firstResult = first.await()
        assertTrue(firstResult is DomainResult.Success)
        assertFalse(guard.isInFlight())
    }

    @Test
    fun `runSingleFlight enforces Retry-After cooldown window`() = runBlocking {
        var nowMs = 1_000L
        val guard = IdempotencyGuard(clockMillis = { nowMs })

        val rateLimited = guard.runSingleFlight<Unit>("cancel pledge") {
            DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_RATE_LIMITED,
                    title = "Rate Limited",
                    status = 429,
                    detail = "Server requested backoff.",
                    suggestedAction = "Wait 10 seconds.",
                    isRetriable = true,
                    retryAfterSeconds = 10L
                )
            )
        }
        assertTrue(rateLimited is DomainResult.Failure)

        nowMs = 5_000L
        val duringCooldown = guard.runSingleFlight("cancel pledge") {
            DomainResult.Success(Unit)
        }
        assertTrue(duringCooldown is DomainResult.Failure)
        val cooldownProblem = (duringCooldown as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_RATE_LIMITED, cooldownProblem.type)
        assertEquals(6L, cooldownProblem.retryAfterSeconds)

        nowMs = 11_000L
        val afterCooldown = guard.runSingleFlight("cancel pledge") {
            DomainResult.Success("ok")
        }
        assertTrue(afterCooldown is DomainResult.Success)
    }
}
