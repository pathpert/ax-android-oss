package com.kickstarter.features.cancelpledge.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.features.cancelpledge.data.CancelPledgeEvent
import com.kickstarter.features.cancelpledge.data.CancelPledgeRepository
import com.kickstarter.features.cancelpledge.data.CancelPledgeTarget
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CancelPledgeFeatureViewModelTest : KSRobolectricTestCase() {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val validTarget = CancelPledgeTarget(
        projectId = (NonBlankId.create("1") as DomainResult.Success).value,
        projectName = "Some Name",
        backingId = (NonBlankId.create("10") as DomainResult.Success).value,
        backingNumericId = 10L,
        pledgeAmount = (MinorCurrencyAmount.fromMinorUnits(3000L) as DomainResult.Success).value,
        formattedPledgeAmount = "$30"
    )

    @Before
    fun setUpMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDownMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `confirmCancellationClicked succeeds and emits Succeeded event`() = runBlocking {
        var capturedKey: IdempotencyKey? = null
        val repo = object : CancelPledgeRepository {
            override suspend fun cancelBacking(
                target: CancelPledgeTarget,
                note: String,
                idempotencyKey: IdempotencyKey
            ): DomainResult<Unit> {
                capturedKey = idempotencyKey
                return DomainResult.Success(Unit)
            }
        }
        val vm = CancelPledgeFeatureViewModel(
            repository = repo,
            initialTargetResult = DomainResult.Success(validTarget),
            testDispatcher = testDispatcher
        )

        vm.confirmCancellationClicked("Cannot afford right now")

        assertEquals(CancelPledgeEvent.Succeeded, vm.events.first())
        assertTrue(capturedKey?.value?.isNotBlank() == true)
        assertFalse(vm.uiState.value.isSubmitting)
        assertNull(vm.uiState.value.problem)
    }

    @Test
    fun `overlapping confirmCancellationClicked is rejected with TYPE_IN_FLIGHT`() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        var callCount = 0
        val repo = object : CancelPledgeRepository {
            override suspend fun cancelBacking(
                target: CancelPledgeTarget,
                note: String,
                idempotencyKey: IdempotencyKey
            ): DomainResult<Unit> {
                callCount++
                gate.await()
                return DomainResult.Success(Unit)
            }
        }
        val vm = CancelPledgeFeatureViewModel(
            repository = repo,
            initialTargetResult = DomainResult.Success(validTarget),
            idempotencyGuard = IdempotencyGuard(),
            testDispatcher = testDispatcher
        )

        vm.confirmCancellationClicked("first")
        assertTrue(vm.uiState.value.isSubmitting)

        vm.confirmCancellationClicked("second")
        assertEquals(1, callCount)
        assertEquals(ProblemDetails.TYPE_IN_FLIGHT, vm.uiState.value.problem?.type)

        gate.complete(Unit)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    @Test
    fun `repository failure surfaces actionable ProblemDetails`() = runBlocking {
        val failure = ProblemDetails(
            type = ProblemDetails.TYPE_RATE_LIMITED,
            title = "Rate Limit Exceeded",
            status = 429,
            detail = "Too many cancellation attempts.",
            suggestedAction = "Wait 15 seconds before retrying.",
            isRetriable = true,
            retryAfterSeconds = 15L
        )
        val repo = object : CancelPledgeRepository {
            override suspend fun cancelBacking(
                target: CancelPledgeTarget,
                note: String,
                idempotencyKey: IdempotencyKey
            ): DomainResult<Unit> = DomainResult.Failure(failure)
        }
        val vm = CancelPledgeFeatureViewModel(
            repository = repo,
            initialTargetResult = DomainResult.Success(validTarget),
            testDispatcher = testDispatcher
        )

        vm.confirmCancellationClicked("")

        assertEquals(failure, vm.uiState.value.problem)
        assertFalse(vm.uiState.value.isSubmitting)
        assertTrue(vm.uiState.value.isCancelButtonVisible)
    }

    @Test
    fun `goBackButtonClicked emits Dismiss event`() = runBlocking {
        val vm = CancelPledgeFeatureViewModel(
            repository = object : CancelPledgeRepository {
                override suspend fun cancelBacking(
                    target: CancelPledgeTarget,
                    note: String,
                    idempotencyKey: IdempotencyKey
                ): DomainResult<Unit> = DomainResult.Success(Unit)
            },
            initialTargetResult = DomainResult.Success(validTarget),
            testDispatcher = testDispatcher
        )

        vm.goBackButtonClicked()

        assertEquals(CancelPledgeEvent.Dismiss, vm.events.first())
    }
}
