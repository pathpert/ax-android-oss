package com.kickstarter.features.pledgeredemption.viewmodels

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.features.pledgeredemption.data.PledgeRedemptionIntentMapper
import com.kickstarter.features.pledgeredemption.data.PledgeRedemptionUiState
import com.kickstarter.libs.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unidirectional ViewModel for Pledge Redemption.
 *
 * Normalizes Intent extras at the boundary via [PledgeRedemptionIntentMapper] and exposes
 * immutable [PledgeRedemptionUiState] with RFC 9457 [ProblemDetails] on invalid inputs.
 *
 * Negative constraints:
 * - Do not expose raw `Bundle`, `lateinit var` models, or unsafe casts to UI consumers.
 */
class PledgeRedemptionViewModel(
    private val environment: Environment?,
    private val bundle: Bundle? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<PledgeRedemptionUiState>(PledgeRedemptionUiState.Loading)
    val uiState: StateFlow<PledgeRedemptionUiState> = _uiState.asStateFlow()

    /**
     * Validates launch arguments and transitions [uiState] to [PledgeRedemptionUiState.Ready]
     * or [PledgeRedemptionUiState.Error].
     */
    @Suppress("DEPRECATION")
    fun start() {
        if (environment == null) {
            _uiState.value = PledgeRedemptionUiState.Error(
                ProblemDetails(
                    type = ProblemDetails.TYPE_UNEXPECTED,
                    title = "Application Environment Unavailable",
                    detail = "PledgeRedemptionViewModel could not resolve Kickstarter Environment dependencies.",
                    suggestedAction = "Ensure PledgeRedemptionActivity is launched within an initialized KSApplication context.",
                    isRetriable = false
                )
            )
            return
        }

        val userEmail = environment.currentUserV2()?.getUser()?.email()
        _uiState.value = when (val mapped = PledgeRedemptionIntentMapper.fromBundle(bundle, userEmail)) {
            is DomainResult.Success -> PledgeRedemptionUiState.Ready(
                args = mapped.value.first,
                legacySnapshot = mapped.value.second
            )
            is DomainResult.Failure -> PledgeRedemptionUiState.Error(mapped.problem)
        }
    }

    /**
     * Factory producing [PledgeRedemptionViewModel] with injected environment and launch bundle.
     *
     * Negative constraints:
     * - Do not instantiate for unsupported ViewModel classes.
     */
    class Factory(
        private val environment: Environment?,
        private val bundle: Bundle? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PledgeRedemptionViewModel::class.java)) {
                "Unsupported ViewModel class: ${modelClass.name}"
            }
            val instance = PledgeRedemptionViewModel(environment, bundle = bundle)
            return modelClass.cast(instance)
                ?: throw IllegalStateException("Failed to cast ${instance::class.java.name} to ${modelClass.name}")
        }
    }
}
