package com.kickstarter.features.paymentmethods.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.features.paymentmethods.data.PaymentMethodCard
import com.kickstarter.features.paymentmethods.data.PaymentMethodsEvent
import com.kickstarter.features.paymentmethods.data.PaymentMethodsRepository
import com.kickstarter.type.PaymentTypes
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
class PaymentMethodsFeatureViewModelTest : KSRobolectricTestCase() {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleCard = PaymentMethodCard(
        id = (NonBlankId.create("card-101") as DomainResult.Success).value,
        lastFour = "4242",
        issuer = "Visa",
        issuerDrawableRes = 1,
        expirationFormatted = "12/28"
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
    fun `init loads stored cards and updates uiState`() = runBlocking {
        val repo = object : PaymentMethodsRepository {
            override suspend fun getStoredCards(): DomainResult<List<PaymentMethodCard>> =
                DomainResult.Success(listOf(sampleCard))

            override suspend fun deletePaymentSource(cardId: NonBlankId, idempotencyKey: IdempotencyKey): DomainResult<String> =
                DomainResult.Success("mut-1")

            override suspend fun createSetupIntent(idempotencyKey: IdempotencyKey): DomainResult<String> =
                DomainResult.Success("seti_secret")

            override suspend fun savePaymentMethod(
                setupClientId: String,
                paymentType: PaymentTypes,
                idempotencyKey: IdempotencyKey
            ): DomainResult<PaymentMethodCard> = DomainResult.Success(sampleCard)
        }

        val vm = PaymentMethodsFeatureViewModel(
            repository = repo,
            userEmailProvider = { "backer@kickstarter.com" },
            isGooglePayEnabledProvider = { true },
            testDispatcher = testDispatcher
        )

        assertEquals(1, vm.uiState.value.cards.size)
        assertEquals("Visa", vm.uiState.value.cards.single().issuer)
        assertTrue(vm.uiState.value.isDividerVisible)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.isPaymentSheetGooglePayEnabled())
    }

    @Test
    fun `newCardButtonClicked creates setup intent and emits PresentPaymentSheet`() = runBlocking {
        val repo = object : PaymentMethodsRepository {
            override suspend fun getStoredCards(): DomainResult<List<PaymentMethodCard>> =
                DomainResult.Success(emptyList())

            override suspend fun deletePaymentSource(cardId: NonBlankId, idempotencyKey: IdempotencyKey): DomainResult<String> =
                DomainResult.Success("")

            override suspend fun createSetupIntent(idempotencyKey: IdempotencyKey): DomainResult<String> =
                DomainResult.Success("seti_secret_123")

            override suspend fun savePaymentMethod(
                setupClientId: String,
                paymentType: PaymentTypes,
                idempotencyKey: IdempotencyKey
            ): DomainResult<PaymentMethodCard> = DomainResult.Success(sampleCard)
        }

        val vm = PaymentMethodsFeatureViewModel(
            repository = repo,
            userEmailProvider = { "backer@kickstarter.com" },
            testDispatcher = testDispatcher
        )

        vm.newCardButtonClicked()

        val event = vm.events.first()
        assertTrue(event is PaymentMethodsEvent.PresentPaymentSheet)
        val sheetEvent = event as PaymentMethodsEvent.PresentPaymentSheet
        assertEquals("seti_secret_123", sheetEvent.setupClientId)
        assertEquals("backer@kickstarter.com", sheetEvent.userEmail)
    }

    @Test
    fun `confirmDeleteCardClicked deletes card and refreshes list`() = runBlocking {
        var deleteCalled = false
        val repo = object : PaymentMethodsRepository {
            override suspend fun getStoredCards(): DomainResult<List<PaymentMethodCard>> =
                DomainResult.Success(if (deleteCalled) emptyList() else listOf(sampleCard))

            override suspend fun deletePaymentSource(cardId: NonBlankId, idempotencyKey: IdempotencyKey): DomainResult<String> {
                deleteCalled = true
                return DomainResult.Success("deleted-mut")
            }

            override suspend fun createSetupIntent(idempotencyKey: IdempotencyKey): DomainResult<String> =
                DomainResult.Success("")

            override suspend fun savePaymentMethod(
                setupClientId: String,
                paymentType: PaymentTypes,
                idempotencyKey: IdempotencyKey
            ): DomainResult<PaymentMethodCard> = DomainResult.Success(sampleCard)
        }

        val vm = PaymentMethodsFeatureViewModel(
            repository = repo,
            userEmailProvider = { "" },
            testDispatcher = testDispatcher
        )

        vm.deleteCardClicked(sampleCard)
        assertEquals(sampleCard, vm.uiState.value.pendingDeleteCard)

        vm.confirmDeleteCardClicked()

        val event = vm.events.first()
        assertTrue(event is PaymentMethodsEvent.SuccessMessage)
        assertEquals("deleted-mut", (event as PaymentMethodsEvent.SuccessMessage).message)
        assertTrue(vm.uiState.value.cards.isEmpty())
        assertNull(vm.uiState.value.pendingDeleteCard)
    }

    @Test
    fun `repository error surfaces ProblemDetails in uiState`() = runBlocking {
        val failure = ProblemDetails(
            type = ProblemDetails.TYPE_RATE_LIMITED,
            title = "Rate Limit Exceeded",
            status = 429,
            detail = "Too many attempts to add cards.",
            suggestedAction = "Wait 30 seconds before retrying.",
            isRetriable = true,
            retryAfterSeconds = 30L
        )

        val repo = object : PaymentMethodsRepository {
            override suspend fun getStoredCards(): DomainResult<List<PaymentMethodCard>> =
                DomainResult.Failure(failure)

            override suspend fun deletePaymentSource(cardId: NonBlankId, idempotencyKey: IdempotencyKey): DomainResult<String> =
                DomainResult.Failure(failure)

            override suspend fun createSetupIntent(idempotencyKey: IdempotencyKey): DomainResult<String> =
                DomainResult.Failure(failure)

            override suspend fun savePaymentMethod(
                setupClientId: String,
                paymentType: PaymentTypes,
                idempotencyKey: IdempotencyKey
            ): DomainResult<PaymentMethodCard> = DomainResult.Failure(failure)
        }

        val vm = PaymentMethodsFeatureViewModel(
            repository = repo,
            userEmailProvider = { "" },
            testDispatcher = testDispatcher
        )

        assertEquals(failure, vm.uiState.value.problem)
        assertFalse(vm.uiState.value.isLoading)
    }
}
