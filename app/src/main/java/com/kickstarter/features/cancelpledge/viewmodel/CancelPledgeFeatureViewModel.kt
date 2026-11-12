package com.kickstarter.features.cancelpledge.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.features.cancelpledge.data.ApolloCancelPledgeRepository
import com.kickstarter.features.cancelpledge.data.CancelPledgeBundleMapper
import com.kickstarter.features.cancelpledge.data.CancelPledgeEvent
import com.kickstarter.features.cancelpledge.data.CancelPledgeRepository
import com.kickstarter.features.cancelpledge.data.CancelPledgeTarget
import com.kickstarter.features.cancelpledge.data.CancelPledgeUiState
import com.kickstarter.libs.Environment
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
 * Unidirectional, idempotency-guarded ViewModel for Pledge Cancellation.
 *
 * Replaces legacy `com.kickstarter.viewmodels.CancelPledgeViewModel` with:
 * - Validated [CancelPledgeTarget] extraction via [CancelPledgeBundleMapper].
 * - Single-flight + rate-limit protection via [IdempotencyGuard].
 * - Structured RFC 9457 [ProblemDetails] instead of raw strings or opaque `Unit` server errors.
 */
class CancelPledgeFeatureViewModel(
    private val repository: CancelPledgeRepository,
    private val initialTargetResult: DomainResult<CancelPledgeTarget>,
    private val idempotencyGuard: IdempotencyGuard = IdempotencyGuard(),
    private val onDisposeLegacyClient: () -> Unit = {},
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    private val _uiState = MutableStateFlow(buildInitialState(initialTargetResult))
    val uiState: StateFlow<CancelPledgeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CancelPledgeEvent>(replay = 1, extraBufferCapacity = 4)
    val events: SharedFlow<CancelPledgeEvent> = _events.asSharedFlow()

    private val target: CancelPledgeTarget? =
        (initialTargetResult as? DomainResult.Success)?.value

    /**
     * Submits a pledge cancellation request guarded by [IdempotencyGuard].
     *
     * Negative constraints:
     * - Do not call concurrently while another cancellation request is in flight; overlapping
     *   invocations are rejected with [ProblemDetails.TYPE_IN_FLIGHT].
     */
    fun confirmCancellationClicked(note: String) {
        val validatedTarget = target
        if (validatedTarget == null) {
            val problem = (initialTargetResult as? DomainResult.Failure)?.problem
                ?: ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Missing Backing Target",
                    status = 400,
                    detail = "Cannot cancel pledge because backing arguments failed validation.",
                    suggestedAction = "Close this screen and reopen pledge cancellation from a backed project.",
                    isRetriable = false
                )
            _uiState.update { it.copy(problem = problem) }
            return
        }

        if (idempotencyGuard.isInFlight()) {
            _uiState.update {
                it.copy(
                    problem = ProblemDetails(
                        type = ProblemDetails.TYPE_IN_FLIGHT,
                        title = "Cancellation In Progress",
                        status = 409,
                        detail = "A pledge cancellation request is already in flight.",
                        suggestedAction = "Wait for the current cancellation request to finish.",
                        isRetriable = false
                    )
                )
            }
            return
        }

        scope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    isCancelButtonVisible = false,
                    problem = null
                )
            }

            val result = idempotencyGuard.runSingleFlight("cancel pledge") { key ->
                repository.cancelBacking(validatedTarget, note, key)
            }

            when (result) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isCancelButtonVisible = true,
                            problem = null
                        )
                    }
                    _events.tryEmit(CancelPledgeEvent.Succeeded)
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isCancelButtonVisible = true,
                            problem = result.problem
                        )
                    }
                }
            }
        }
    }

    /**
     * Dismisses the cancellation sheet without mutating backing state.
     */
    fun goBackButtonClicked() {
        _events.tryEmit(CancelPledgeEvent.Dismiss)
    }

    /**
     * Clears the current displayed [ProblemDetails] after it has been surfaced to the user.
     */
    fun clearProblem() {
        _uiState.update { it.copy(problem = null) }
    }

    override fun onCleared() {
        onDisposeLegacyClient()
        super.onCleared()
    }

    private fun buildInitialState(result: DomainResult<CancelPledgeTarget>): CancelPledgeUiState {
        return when (result) {
            is DomainResult.Success -> CancelPledgeUiState(
                formattedPledgeAmount = result.value.formattedPledgeAmount,
                projectName = result.value.projectName,
                isSubmitting = false,
                isCancelButtonVisible = true,
                isReady = true,
                problem = null
            )
            is DomainResult.Failure -> CancelPledgeUiState(
                isSubmitting = false,
                isCancelButtonVisible = false,
                isReady = false,
                problem = result.problem
            )
        }
    }

    class Factory(
        private val environment: Environment?,
        private val bundle: Bundle? = null,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CancelPledgeFeatureViewModel::class.java)) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }

            val apolloClient = environment?.apolloClientV2()
            val mappedTarget = CancelPledgeBundleMapper.fromBundle(
                bundle = bundle,
                ksCurrency = environment?.ksCurrency()
            )
            val repository: CancelPledgeRepository = if (apolloClient != null) {
                ApolloCancelPledgeRepository(apolloClient)
            } else {
                MissingEnvironmentCancelPledgeRepository
            }
            val effectiveTarget = if (apolloClient == null && mappedTarget is DomainResult.Success) {
                DomainResult.Failure(missingEnvironmentProblem())
            } else {
                mappedTarget
            }

            val vm = CancelPledgeFeatureViewModel(
                repository = repository,
                initialTargetResult = effectiveTarget,
                onDisposeLegacyClient = { apolloClient?.cleanDisposables() },
                testDispatcher = testDispatcher
            )
            return modelClass.cast(vm)
                ?: throw IllegalStateException("Failed to cast ${vm::class.java.name} to ${modelClass.name}")
        }
    }

    private object MissingEnvironmentCancelPledgeRepository : CancelPledgeRepository {
        override suspend fun cancelBacking(
            target: CancelPledgeTarget,
            note: String,
            idempotencyKey: com.kickstarter.ax.safety.IdempotencyKey
        ): DomainResult<Unit> = DomainResult.Failure(missingEnvironmentProblem())
    }

    private companion object {
        fun missingEnvironmentProblem() = ProblemDetails(
            type = ProblemDetails.TYPE_UNEXPECTED,
            title = "Apollo Client Unavailable",
            detail = "CancelPledgeFeatureViewModel requires an initialized ApolloClientTypeV2.",
            suggestedAction = "Ensure CancelPledgeFragment is attached to an initialized KSApplication environment.",
            isRetriable = false
        )
    }
}
