package com.kickstarter.features.paymentmethods.data

import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.StoredCard
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException

class ApolloPaymentMethodsRepositoryTest : KSRobolectricTestCase() {

    @Test
    fun `getStoredCards returns mapped cards on success`() = runBlocking {
        val repo = ApolloPaymentMethodsRepository(
            object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.just(listOf(StoredCardFactory.visa(), StoredCardFactory.discoverCard()))
                }
            }
        )

        val result = repo.getStoredCards()

        assertTrue(result is DomainResult.Success)
        val cards = (result as DomainResult.Success).value
        assertEquals(2, cards.size)
        assertEquals("visa", cards[0].issuer)
        assertEquals("discover", cards[1].issuer)
    }

    @Test
    fun `deletePaymentSource succeeds with mutation id`() = runBlocking {
        val repo = ApolloPaymentMethodsRepository(
            object : MockApolloClientV2() {
                override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
                    assertEquals("card-99", paymentSourceId)
                    return Observable.just(
                        DeletePaymentSourceMutation.Data(
                            DeletePaymentSourceMutation.PaymentSourceDelete(
                                "client-mut-1"
                            )
                        )
                    )
                }
            }
        )

        val id = (NonBlankId.create("card-99") as DomainResult.Success).value
        val result = repo.deletePaymentSource(id, IdempotencyKey.generate())

        assertTrue(result is DomainResult.Success)
        assertEquals("client-mut-1", (result as DomainResult.Success).value)
    }

    @Test
    fun `createSetupIntent rejects empty secret with retriable ProblemDetails`() = runBlocking {
        val repo = ApolloPaymentMethodsRepository(
            object : MockApolloClientV2() {
                override fun createSetupIntent(project: com.kickstarter.models.Project?): Observable<String> {
                    return Observable.just("")
                }
            }
        )

        val result = repo.createSetupIntent(IdempotencyKey.generate())

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_SERVER, problem.type)
        assertTrue(problem.isRetriable)
    }

    @Test
    fun `getStoredCards maps network failure into ProblemDetails`() = runBlocking {
        val repo = ApolloPaymentMethodsRepository(
            object : MockApolloClientV2() {
                override fun getStoredCards(): Observable<List<StoredCard>> {
                    return Observable.error(SocketTimeoutException("Read timed out"))
                }
            }
        )

        val result = repo.getStoredCards()

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_TIMEOUT, problem.type)
        assertTrue(problem.isRetriable)
    }
}
