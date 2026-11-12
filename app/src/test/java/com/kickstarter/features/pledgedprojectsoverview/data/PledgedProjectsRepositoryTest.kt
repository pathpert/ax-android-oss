package com.kickstarter.features.pledgedprojectsoverview.data

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.UpdateBackerCompletedData
import io.reactivex.Observable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException

class PledgedProjectsRepositoryTest : KSRobolectricTestCase() {

    @Test
    fun `confirmAddress rejects blank addressID or backingID with invalidParams`() = runBlocking {
        val repo = ApolloPledgedProjectsRepository(MockApolloClientV2())

        val badAddress = repo.confirmAddress(addressId = "  ", backingId = "backing-1")
        assertTrue(badAddress is DomainResult.Failure)
        val addressProblem = (badAddress as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, addressProblem.type)
        assertEquals("addressID", addressProblem.invalidParams.single().name)

        val badBacking = repo.confirmAddress(addressId = "addr-1", backingId = "")
        assertTrue(badBacking is DomainResult.Failure)
        assertEquals("backingID", (badBacking as DomainResult.Failure).problem.invalidParams.single().name)
    }

    @Test
    fun `confirmAddress rejects overlapping calls via IdempotencyGuard`() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val guard = IdempotencyGuard()

        val first = async {
            guard.runSingleFlight("confirm backing address") {
                started.complete(Unit)
                release.await()
                DomainResult.Success(Unit)
            }
        }
        started.await()

        val repo = ApolloPledgedProjectsRepository(
            apolloClient = MockApolloClientV2(),
            addressGuard = guard
        )
        val duplicate = repo.confirmAddress("addr-1", "backing-1")
        assertTrue(duplicate is DomainResult.Failure)
        val problem = (duplicate as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_IN_FLIGHT, problem.type)
        assertEquals(409, problem.status)
        assertFalse(problem.isRetriable)

        release.complete(Unit)
        assertTrue(first.await() is DomainResult.Success)
    }

    @Test
    fun `updateRewardReceived maps false and timeout into ProblemDetails`() = runBlocking {
        val falseRepo = ApolloPledgedProjectsRepository(
            object : MockApolloClientV2() {
                override fun updateBackerCompleted(input: UpdateBackerCompletedData): Observable<Boolean> {
                    return Observable.just(false)
                }
            }
        )
        val falseResult = falseRepo.updateRewardReceived("backing-1", true)
        assertTrue(falseResult is DomainResult.Failure)
        assertEquals(ProblemDetails.TYPE_SERVER, (falseResult as DomainResult.Failure).problem.type)

        val timeoutRepo = ApolloPledgedProjectsRepository(
            object : MockApolloClientV2() {
                override fun createOrUpdateBackingAddress(
                    eventInput: CreateOrUpdateBackingAddressData
                ): Observable<Boolean> {
                    return Observable.error(SocketTimeoutException("Timeout"))
                }
            }
        )
        val timeoutResult = timeoutRepo.confirmAddress("addr-1", "backing-1")
        assertTrue(timeoutResult is DomainResult.Failure)
        val timeoutProblem = (timeoutResult as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_TIMEOUT, timeoutProblem.type)
        assertTrue(timeoutProblem.isRetriable)
    }
}
