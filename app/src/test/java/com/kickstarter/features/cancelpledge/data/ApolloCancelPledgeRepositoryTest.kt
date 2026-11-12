package com.kickstarter.features.cancelpledge.data

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Backing
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException

class ApolloCancelPledgeRepositoryTest : KSRobolectricTestCase() {

    private val target = CancelPledgeTarget(
        projectId = (NonBlankId.create("101") as DomainResult.Success).value,
        projectName = "Test Project",
        backingId = (NonBlankId.create("202") as DomainResult.Success).value,
        backingNumericId = 202L,
        pledgeAmount = (MinorCurrencyAmount.fromMinorUnits(3000L) as DomainResult.Success).value,
        formattedPledgeAmount = "$30"
    )

    @Test
    fun `cancelBacking returns Success when Apollo emits true`() = runBlocking {
        val repo = ApolloCancelPledgeRepository(
            object : MockApolloClientV2() {
                override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                    assertEquals(202L, backing.id())
                    assertEquals("Changed mind", note)
                    return Observable.just(true)
                }
            }
        )

        val result = repo.cancelBacking(target, "  Changed mind  ", IdempotencyKey.generate())

        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `cancelBacking maps false to retriable 502 ProblemDetails`() = runBlocking {
        val repo = ApolloCancelPledgeRepository(
            object : MockApolloClientV2() {
                override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                    return Observable.just(false)
                }
            }
        )

        val result = repo.cancelBacking(target, "", IdempotencyKey.generate())

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_SERVER, problem.type)
        assertEquals(502, problem.status)
        assertTrue(problem.isRetriable)
    }

    @Test
    fun `cancelBacking maps GraphQL String or Exception payloads into actionable ProblemDetails`() = runBlocking {
        val stringRepo = ApolloCancelPledgeRepository(
            object : MockApolloClientV2() {
                override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                    return Observable.just("Campaign already ended")
                }
            }
        )
        val stringResult = stringRepo.cancelBacking(target, "", IdempotencyKey.generate())
        assertTrue(stringResult is DomainResult.Failure)
        val stringProblem = (stringResult as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, stringProblem.type)
        assertFalse(stringProblem.isRetriable)
        assertTrue(stringProblem.detail.contains("Campaign already ended"))

        val timeoutRepo = ApolloCancelPledgeRepository(
            object : MockApolloClientV2() {
                override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                    return Observable.error(SocketTimeoutException("Timed out"))
                }
            }
        )
        val timeoutResult = timeoutRepo.cancelBacking(target, "", IdempotencyKey.generate())
        assertTrue(timeoutResult is DomainResult.Failure)
        val timeoutProblem = (timeoutResult as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_TIMEOUT, timeoutProblem.type)
        assertTrue(timeoutProblem.isRetriable)
    }
}
