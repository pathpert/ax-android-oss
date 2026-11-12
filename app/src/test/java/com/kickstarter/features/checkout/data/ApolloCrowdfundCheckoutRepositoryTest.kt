package com.kickstarter.features.checkout.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Checkout
import com.kickstarter.models.PaymentPlan
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApolloCrowdfundCheckoutRepositoryTest {

    private val sampleKey = IdempotencyKey.generate()
    private val sampleProject = ProjectFactory.project()

    @Test
    fun `buildPaymentPlan returns payment plan on success`() = runBlocking {
        val repo = ApolloCrowdfundCheckoutRepository(
            object : MockApolloClientV2() {
                override fun buildPaymentPlan(inputData: com.kickstarter.models.BuildPaymentPlanData): Observable<PaymentPlan> {
                    return Observable.just(
                        PaymentPlan.builder()
                            .amountIsPledgeOverTimeEligible(true)
                            .paymentIncrements(emptyList())
                            .build()
                    )
                }
            }
        )

        val slug = (NonBlankId.create("cool-project") as DomainResult.Success).value
        val amount = (MinorCurrencyAmount.fromMinorUnits(5000L) as DomainResult.Success).value

        val result = repo.buildPaymentPlan(slug, amount)

        assertTrue(result is DomainResult.Success)
        val plan = (result as DomainResult.Success).value
        assertTrue(plan.isPledgeOverTimeEligible)
        assertTrue(plan.paymentIncrements.isEmpty())
    }

    @Test
    fun `createBacking returns checkout id on success`() = runBlocking {
        val repo = ApolloCrowdfundCheckoutRepository(
            object : MockApolloClientV2() {
                override fun createBacking(data: CreateBackingData): Observable<Checkout> {
                    return Observable.just(
                        Checkout.builder()
                            .id(9999L)
                            .build()
                    )
                }
            }
        )

        val backingData = CreateBackingData(
            project = sampleProject,
            amount = "50.00",
            locationId = null,
            refTag = null
        )

        val result = repo.createBacking(backingData, sampleKey)

        assertTrue(result is DomainResult.Success)
        assertEquals(9999L, (result as DomainResult.Success).value.checkoutId)
    }

    @Test
    fun `createBacking returns failure when id is missing`() = runBlocking {
        val repo = ApolloCrowdfundCheckoutRepository(
            object : MockApolloClientV2() {
                override fun createBacking(data: CreateBackingData): Observable<Checkout> {
                    return Observable.just(Checkout.builder().id(-1L).build())
                }
            }
        )

        val backingData = CreateBackingData(
            project = sampleProject,
            amount = "50.00",
            locationId = null,
            refTag = null
        )

        val result = repo.createBacking(backingData, sampleKey)

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_UNEXPECTED, problem.type)
        assertFalse(problem.isRetriable)
    }

    @Test
    fun `updateBacking returns updated checkout id on success`() = runBlocking {
        val repo = ApolloCrowdfundCheckoutRepository(
            object : MockApolloClientV2() {
                override fun updateBacking(data: UpdateBackingData): Observable<Checkout> {
                    return Observable.just(
                        Checkout.builder()
                            .id(8888L)
                            .build()
                    )
                }
            }
        )

        val updateData = UpdateBackingData(
            backing = sampleProject.backing() ?: com.kickstarter.models.Backing.builder().build(),
            amount = "60.00"
        )

        val result = repo.updateBacking(updateData, sampleKey)

        assertTrue(result is DomainResult.Success)
        assertEquals(8888L, (result as DomainResult.Success).value.checkoutId)
    }

    @Test
    fun `createSetupIntent returns client secret on success`() = runBlocking {
        val repo = ApolloCrowdfundCheckoutRepository(
            object : MockApolloClientV2() {
                override fun createSetupIntent(project: com.kickstarter.models.Project?): Observable<String> {
                    return Observable.just("seti_secret_xyz")
                }
            }
        )

        val result = repo.createSetupIntent(sampleProject, sampleKey)

        assertTrue(result is DomainResult.Success)
        assertEquals("seti_secret_xyz", (result as DomainResult.Success).value)
    }
}
