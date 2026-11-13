package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.features.backing.data.BackingFeatureInputs
import com.kickstarter.features.backing.data.BackingFeatureOutputs
import com.kickstarter.features.backing.viewmodel.BackingFeatureViewModel
import com.kickstarter.libs.Environment

typealias BackingState = com.kickstarter.features.backing.data.BackingState

/**
 * Legacy monolithic ViewModel contract for Backing details.
 *
 * @deprecated Replaced by [BackingFeatureViewModel] under `com.kickstarter.features.backing.viewmodel`.
 */
@Deprecated(
    message = "Replaced by BackingFeatureViewModel under com.kickstarter.features.backing.viewmodel",
    replaceWith = ReplaceWith("BackingFeatureViewModel", "com.kickstarter.features.backing.viewmodel.BackingFeatureViewModel")
)
interface BackingFragmentViewModel {
    interface Inputs : BackingFeatureInputs
    interface Outputs : BackingFeatureOutputs

    open class BackingFragmentViewModel(environment: Environment) :
        com.kickstarter.features.backing.viewmodel.BackingFeatureViewModel(environment),
        Inputs,
        Outputs {
        override val inputs: Inputs = this
        override val outputs: Outputs = this
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BackingFragmentViewModel(environment) as T
        }
    }
}
