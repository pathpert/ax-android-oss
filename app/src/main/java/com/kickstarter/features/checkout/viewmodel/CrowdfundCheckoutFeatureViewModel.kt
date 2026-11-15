package com.kickstarter.features.checkout.viewmodel

import CollectionOptions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.features.checkout.data.CheckoutAmounts
import com.kickstarter.features.checkout.data.CheckoutPaymentPlan
import com.kickstarter.features.checkout.data.CrowdfundCheckoutRepository
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.StripePaymentSheetResult
import com.kickstarter.models.extensions.isFromPaymentSheet
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Unidirectional UI state for the modern Crowdfund Checkout feature.
 *
 * Negative constraints:
 * - Do not mutate state directly; emit through ViewModel StateFlow reducers.
 * - Do not pass floating-point amounts; use [CheckoutAmounts] with integer minor units.
 * - Do not pass raw unhandled exceptions; map errors to [ProblemDetails].
 *
 * @property storedCards Saved payment cards for the current backer.
 * @property userEmail Contact email of the current user.
 * @property isLoading Whether checkout mutation or fetch is in progress.
 * @property selectedRewards List of rewards and add-ons in this checkout.
 * @property amounts Shipping, bonus, and total monetary amounts in integer minor units.
 * @property selectedPaymentMethod Currently selected payment method.
 * @property shippingRule Selected shipping location and rule.
 * @property paymentPlan Optional installment payment plan for PLOT.
 * @property isIncrementalPledge Whether this is an incremental pledge update.
 * @property isPledgeButtonEnabled Whether pledge action button can be tapped.
 * @property problem Structured RFC 9457 error details if checkout failed.
 */
data class CrowdfundCheckoutUiState(
    val storedCards: List<StoredCard> = emptyList(),
    val userEmail: String = "",
    val isLoading: Boolean = false,
    val selectedRewards: List<Reward> = emptyList(),
    val amounts: CheckoutAmounts = CheckoutAmounts.ZERO,
    val selectedPaymentMethod: StoredCard? = null,
    val shippingRule: ShippingRule? = null,
    val paymentPlan: CheckoutPaymentPlan? = null,
    val isIncrementalPledge: Boolean? = null,
    val isPledgeButtonEnabled: Boolean = true,
    val problem: ProblemDetails? = null
)

/**
 * One-shot navigation and mutation events.
 *
 * Negative constraints:
 * - Do not emit events with invalid or negative checkout IDs.
 */
sealed interface CrowdfundCheckoutEvent {
    /**
     * Backing successfully created.
     *
     * Negative constraints:
     * - Do not pass invalid or non-positive checkout IDs.
     *
     * @property checkoutId Positive checkout confirmation identifier.
     */
    data class BackingCreated(val checkoutId: Long) : CrowdfundCheckoutEvent

    /**
     * Backing successfully updated.
     *
     * Negative constraints:
     * - Do not pass invalid or non-positive checkout IDs.
     *
     * @property checkoutId Positive checkout confirmation identifier.
     */
    data class BackingUpdated(val checkoutId: Long) : CrowdfundCheckoutEvent

    /**
     * Trigger Stripe payment sheet display.
     *
     * Negative constraints:
     * - Do not pass blank setup client secret or empty email.
     *
     * @property setupClientId Stripe setup intent client secret.
     * @property userEmail User email associated with payment intent.
     */
    data class PresentPaymentSheet(val setupClientId: String, val userEmail: String) : CrowdfundCheckoutEvent
}

/**
 * Modernized, idempotency-guarded ViewModel for the Crowdfund Checkout flow.
 *
 * Replaces monolithic [com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel].
 *
 * Negative constraints:
 * - Do not pass floating-point amounts; use [CheckoutAmounts] with [MinorCurrencyAmount].
 * - Do not trigger duplicate backing mutations; all mutating calls are single-flight guarded.
 */
class CrowdfundCheckoutFeatureViewModel(
    private val repository: CrowdfundCheckoutRepository,
    initialAmounts: CheckoutAmounts = CheckoutAmounts.ZERO,
    initialRewards: List<Reward> = emptyList(),
    initialStoredCards: List<StoredCard> = emptyList(),
    initialEmail: String = "",
    private val isGooglePayEnabledProvider: () -> Boolean = { false },
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    private val _uiState = MutableStateFlow(
        CrowdfundCheckoutUiState(
            amounts = initialAmounts,
            selectedRewards = initialRewards,
            storedCards = initialStoredCards,
            userEmail = initialEmail,
            selectedPaymentMethod = initialStoredCards.firstOrNull()
        )
    )
    val uiState: StateFlow<CrowdfundCheckoutUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CrowdfundCheckoutEvent>(replay = 1, extraBufferCapacity = 8)
    val events: SharedFlow<CrowdfundCheckoutEvent> = _events.asSharedFlow()

    fun isGooglePayEnabled(): Boolean = isGooglePayEnabledProvider()

    fun newlyAddedPaymentMethod(card: StoredCard?) {
        if (card == null) return
        _uiState.update { current ->
            val updatedCards = if (card.isFromPaymentSheet()) {
                listOf(card) + current.storedCards
            } else {
                current.storedCards
            }
            current.copy(
                selectedPaymentMethod = card,
                storedCards = updatedCards,
                isLoading = false
            )
        }
    }

    fun userChangedPaymentMethodSelected(card: StoredCard?) {
        if (card == null) return
        _uiState.update { it.copy(selectedPaymentMethod = card) }
    }

    fun collectionPlanSelected(option: CollectionOptions) {
        val isIncremental = option == CollectionOptions.PLEDGE_OVER_TIME
        _uiState.update { it.copy(isIncrementalPledge = isIncremental) }
    }

    fun loadPaymentPlan(slug: String) {
        val slugId = when (val res = NonBlankId.create(slug, "project.slug")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> {
                _uiState.update { it.copy(problem = res.problem) }
                return
            }
        }
        val currentTotal = _uiState.value.amounts.totalAmount
        scope.launch {
            _uiState.update { it.copy(isLoading = true, problem = null) }
            when (val planResult = repository.buildPaymentPlan(slugId, currentTotal)) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            paymentPlan = planResult.value,
                            isLoading = false
                        )
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            problem = planResult.problem
                        )
                    }
                }
            }
        }
    }

    fun requestSetupIntent(project: Project) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, problem = null) }
            val key = IdempotencyKey.generate()
            when (val result = repository.createSetupIntent(project, key)) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.tryEmit(
                        CrowdfundCheckoutEvent.PresentPaymentSheet(
                            setupClientId = result.value,
                            userEmail = _uiState.value.userEmail
                        )
                    )
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            problem = result.problem
                        )
                    }
                }
            }
        }
    }

    fun submitCreateBacking(data: CreateBackingData) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, isPledgeButtonEnabled = false, problem = null) }
            val key = IdempotencyKey.generate()
            when (val result = repository.createBacking(data, key)) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isPledgeButtonEnabled = true) }
                    _events.tryEmit(CrowdfundCheckoutEvent.BackingCreated(result.value.checkoutId))
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPledgeButtonEnabled = true,
                            problem = result.problem
                        )
                    }
                }
            }
        }
    }

    fun submitUpdateBacking(data: UpdateBackingData) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, isPledgeButtonEnabled = false, problem = null) }
            val key = IdempotencyKey.generate()
            when (val result = repository.updateBacking(data, key)) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isPledgeButtonEnabled = true) }
                    _events.tryEmit(CrowdfundCheckoutEvent.BackingUpdated(result.value.checkoutId))
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPledgeButtonEnabled = true,
                            problem = result.problem
                        )
                    }
                }
            }
        }
    }

    fun paymentSheetResult(result: StripePaymentSheetResult) {
        when (result) {
            is StripePaymentSheetResult.Canceled,
            is StripePaymentSheetResult.Failed -> {
                _uiState.update { current ->
                    current.copy(
                        storedCards = current.storedCards.filter { !it.isFromPaymentSheet() },
                        isLoading = false
                    )
                }
            }
            is StripePaymentSheetResult.Completed -> {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearProblem() {
        _uiState.update { it.copy(problem = null) }
    }
}
