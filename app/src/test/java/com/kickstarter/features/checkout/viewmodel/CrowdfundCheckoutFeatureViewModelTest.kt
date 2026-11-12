package com.kickstarter.features.checkout.viewmodel

import CollectionOptions
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.features.checkout.data.CheckoutAmounts
import com.kickstarter.features.checkout.data.CheckoutBackingResult
import com.kickstarter.features.checkout.data.CheckoutPaymentPlan
import com.kickstarter.features.checkout.data.CrowdfundCheckoutRepository
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StripePaymentSheetResult
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CrowdfundCheckoutFeatureViewModelTest : KSRobolectricTestCase() {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val sampleCard = StoredCardFactory.visa()
    private val sampleProject = ProjectFactory.project()
    private val sampleAmounts = CheckoutAmounts.fromDoubles(shipping = 10.0, bonus = 5.0, total = 65.0)

    @Before
    fun setUpMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDownMainDispatcher() {
        Dispatchers.resetMain()
    }

    private fun createStubRepo(
        createResult: DomainResult<CheckoutBackingResult> = DomainResult.Success(CheckoutBackingResult(101L)),
        updateResult: DomainResult<CheckoutBackingResult> = DomainResult.Success(CheckoutBackingResult(202L)),
        setupResult: DomainResult<String> = DomainResult.Success("seti_secret_stub"),
        planResult: DomainResult<CheckoutPaymentPlan> = DomainResult.Success(
            CheckoutPaymentPlan(isPledgeOverTimeEligible = true, paymentIncrements = emptyList())
        )
    ): CrowdfundCheckoutRepository {
        return object : CrowdfundCheckoutRepository {
            override suspend fun buildPaymentPlan(
                projectSlug: NonBlankId,
                amount: MinorCurrencyAmount
            ): DomainResult<CheckoutPaymentPlan> = planResult

            override suspend fun createBacking(
                data: CreateBackingData,
                idempotencyKey: IdempotencyKey
            ): DomainResult<CheckoutBackingResult> = createResult

            override suspend fun updateBacking(
                data: UpdateBackingData,
                idempotencyKey: IdempotencyKey
            ): DomainResult<CheckoutBackingResult> = updateResult

            override suspend fun createSetupIntent(
                project: Project,
                idempotencyKey: IdempotencyKey
            ): DomainResult<String> = setupResult
        }
    }

    @Test
    fun `initial state reflects provided parameters`() {
        val vm = CrowdfundCheckoutFeatureViewModel(
            repository = createStubRepo(),
            initialAmounts = sampleAmounts,
            initialRewards = listOf(Reward.builder().id(1L).build()),
            initialStoredCards = listOf(sampleCard),
            initialEmail = "backer@kickstarter.com",
            isGooglePayEnabledProvider = { true },
            testDispatcher = testDispatcher
        )

        val state = vm.uiState.value
        assertEquals(6500L, state.amounts.totalAmount.minorUnits)
        assertEquals(1, state.selectedRewards.size)
        assertEquals(1, state.storedCards.size)
        assertEquals(sampleCard, state.selectedPaymentMethod)
        assertEquals("backer@kickstarter.com", state.userEmail)
        assertTrue(vm.isGooglePayEnabled())
    }

    @Test
    fun `newlyAddedPaymentMethod prepends card when added from payment sheet`() {
        val vm = CrowdfundCheckoutFeatureViewModel(
            repository = createStubRepo(),
            initialStoredCards = listOf(sampleCard),
            testDispatcher = testDispatcher
        )

        val sheetCard = StoredCardFactory.fromPaymentSheetCard()
        vm.newlyAddedPaymentMethod(sheetCard)

        val state = vm.uiState.value
        assertEquals(2, state.storedCards.size)
        assertEquals(sheetCard, state.storedCards.first())
        assertEquals(sheetCard, state.selectedPaymentMethod)
    }

    @Test
    fun `collectionPlanSelected updates incremental pledge flag`() {
        val vm = CrowdfundCheckoutFeatureViewModel(
            repository = createStubRepo(),
            testDispatcher = testDispatcher
        )

        vm.collectionPlanSelected(CollectionOptions.PLEDGE_OVER_TIME)
        assertTrue(vm.uiState.value.isIncrementalPledge == true)

        vm.collectionPlanSelected(CollectionOptions.PLEDGE_IN_FULL)
        assertTrue(vm.uiState.value.isIncrementalPledge == false)
    }

    @Test
    fun `requestSetupIntent emits PresentPaymentSheet event on success`() = runBlocking {
        val vm = CrowdfundCheckoutFeatureViewModel(
            repository = createStubRepo(setupResult = DomainResult.Success("seti_secret_live")),
            initialEmail = "test@kickstarter.com",
            testDispatcher = testDispatcher
        )

        vm.requestSetupIntent(sampleProject)

        val event = vm.events.first()
        assertTrue(event is CrowdfundCheckoutEvent.PresentPaymentSheet)
        val sheetEvent = event as CrowdfundCheckoutEvent.PresentPaymentSheet
        assertEquals("seti_secret_live", sheetEvent.setupClientId)
        assertEquals("test@kickstarter.com", sheetEvent.userEmail)
    }

    @Test
    fun `submitCreateBacking emits BackingCreated on success`() = runBlocking {
        val vm = CrowdfundCheckoutFeatureViewModel(
            repository = createStubRepo(createResult = DomainResult.Success(CheckoutBackingResult(777L))),
            testDispatcher = testDispatcher
        )

        val backingData = CreateBackingData(
            project = sampleProject,
            amount = "10.00",
            locationId = null,
            refTag = null
        )

        vm.submitCreateBacking(backingData)

        val event = vm.events.first()
        assertTrue(event is CrowdfundCheckoutEvent.BackingCreated)
        assertEquals(777L, (event as CrowdfundCheckoutEvent.BackingCreated).checkoutId)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isPledgeButtonEnabled)
    }

    @Test
    fun `submitCreateBacking surfaces ProblemDetails on repository failure`() = runBlocking {
        val failure = ProblemDetails(
            type = ProblemDetails.TYPE_RATE_LIMITED,
            title = "Rate Limit",
            status = 429,
            detail = "Too many attempts",
            suggestedAction = "Wait 10 seconds",
            isRetriable = true
        )
        val vm = CrowdfundCheckoutFeatureViewModel(
            repository = createStubRepo(createResult = DomainResult.Failure(failure)),
            testDispatcher = testDispatcher
        )

        val backingData = CreateBackingData(
            project = sampleProject,
            amount = "10.00",
            locationId = null,
            refTag = null
        )

        vm.submitCreateBacking(backingData)

        assertEquals(failure, vm.uiState.value.problem)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isPledgeButtonEnabled)
    }

    @Test
    fun `paymentSheetResult filters out payment sheet cards on cancellation`() {
        val vm = CrowdfundCheckoutFeatureViewModel(
            repository = createStubRepo(),
            initialStoredCards = listOf(sampleCard),
            testDispatcher = testDispatcher
        )

        vm.newlyAddedPaymentMethod(StoredCardFactory.fromPaymentSheetCard())
        assertEquals(2, vm.uiState.value.storedCards.size)

        vm.paymentSheetResult(StripePaymentSheetResult.Canceled)
        assertEquals(1, vm.uiState.value.storedCards.size)
        assertEquals(sampleCard, vm.uiState.value.storedCards.single())
    }
}
