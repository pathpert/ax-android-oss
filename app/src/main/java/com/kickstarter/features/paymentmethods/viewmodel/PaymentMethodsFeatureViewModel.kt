package com.kickstarter.features.paymentmethods.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.features.paymentmethods.data.ApolloPaymentMethodsRepository
import com.kickstarter.features.paymentmethods.data.PaymentMethodCard
import com.kickstarter.features.paymentmethods.data.PaymentMethodsEvent
import com.kickstarter.features.paymentmethods.data.PaymentMethodsRepository
import com.kickstarter.features.paymentmethods.data.PaymentMethodsUiState
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.type.PaymentTypes
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
 * Unidirectional ViewModel for managing stored payment methods.
 *
 * Enforces:
 * - Single-flight guards and idempotency on deletion and setup intent mutations.
 * - Actionable RFC 9457 ProblemDetails error states.
 * - Concise, testable coroutines architecture (< 300 lines).
 */
class PaymentMethodsFeatureViewModel(
    private val repository: PaymentMethodsRepository,
    private val userEmailProvider: () -> String?,
    private val isGooglePayEnabledProvider: () -> Boolean = { false },
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PaymentMethodsEvent>(replay = 1, extraBufferCapacity = 8)
    val events: SharedFlow<PaymentMethodsEvent> = _events.asSharedFlow()

    private var lastSetupClientId: String? = null

    init {
        refreshCards()
    }

    fun isPaymentSheetGooglePayEnabled(): Boolean = isGooglePayEnabledProvider()

    fun refreshCards() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, problem = null) }
            when (val result = repository.getStoredCards()) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            cards = result.value,
                            isLoading = false,
                            isDividerVisible = result.value.isNotEmpty(),
                            problem = null
                        )
                    }
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

    fun deleteCardClicked(card: PaymentMethodCard) {
        _uiState.update { it.copy(pendingDeleteCard = card) }
        _events.tryEmit(PaymentMethodsEvent.ShowDeleteConfirmation(card))
    }

    fun confirmDeleteCardClicked() {
        val target = _uiState.value.pendingDeleteCard ?: return
        scope.launch {
            _uiState.update { it.copy(isLoading = true, problem = null) }
            when (val result = repository.deletePaymentSource(target.id, com.kickstarter.ax.safety.IdempotencyKey.generate())) {
                is DomainResult.Success -> {
                    _events.tryEmit(PaymentMethodsEvent.SuccessMessage(result.value))
                    _uiState.update { it.copy(pendingDeleteCard = null) }
                    refreshCards()
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingDeleteCard = null,
                            problem = result.problem
                        )
                    }
                }
            }
        }
    }

    fun newCardButtonClicked() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, problem = null) }
            when (val result = repository.createSetupIntent(com.kickstarter.ax.safety.IdempotencyKey.generate())) {
                is DomainResult.Success -> {
                    val clientSecret = result.value
                    lastSetupClientId = clientSecret
                    _uiState.update { it.copy(isLoading = false) }
                    val email = userEmailProvider()?.trim().orEmpty()
                    _events.tryEmit(
                        PaymentMethodsEvent.PresentPaymentSheet(
                            setupClientId = clientSecret,
                            userEmail = email
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

    fun savePaymentOption(paymentType: PaymentTypes = PaymentTypes.CREDIT_CARD) {
        val setupClientId = lastSetupClientId ?: return
        scope.launch {
            _uiState.update { it.copy(isLoading = true, problem = null) }
            val result = repository.savePaymentMethod(
                setupClientId = setupClientId,
                paymentType = paymentType,
                idempotencyKey = com.kickstarter.ax.safety.IdempotencyKey.generate()
            )
            when (result) {
                is DomainResult.Success -> {
                    _events.tryEmit(PaymentMethodsEvent.SuccessMessage(result.value.id.value))
                    refreshCards()
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

    fun clearProblem() {
        _uiState.update { it.copy(problem = null) }
    }

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PaymentMethodsFeatureViewModel::class.java)) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }
            val apolloClient = requireNotNull(environment.apolloClientV2())
            val repository = ApolloPaymentMethodsRepository(apolloClient)
            val statsigClient = environment.statsigClient()
            val isGooglePayEnabled = {
                statsigClient != null && statsigClient.configReady.value &&
                    statsigClient.checkGate(StatsigGateKey.ANDROID_PAYMENTSHEET_GOOGLE_PAY.key)
            }
            val userEmailProvider = {
                environment.currentUserV2()?.getUser()?.email()
            }
            val vm = PaymentMethodsFeatureViewModel(
                repository = repository,
                userEmailProvider = userEmailProvider,
                isGooglePayEnabledProvider = isGooglePayEnabled,
                testDispatcher = testDispatcher
            )
            return modelClass.cast(vm)
                ?: throw IllegalStateException("Failed to cast ${vm::class.java.name} to ${modelClass.name}")
        }
    }
}
