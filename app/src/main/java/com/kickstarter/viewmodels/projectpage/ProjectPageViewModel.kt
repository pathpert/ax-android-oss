package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.features.projectpage.data.ProjectPageInputs
import com.kickstarter.features.projectpage.data.ProjectPageOutputs
import com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel
import com.kickstarter.libs.Environment

typealias PagerTabConfig = com.kickstarter.features.projectpage.data.PagerTabConfig

/**
 * Legacy ProjectPageViewModel interface maintained for backward compatibility.
 * Replaced by [ProjectFeatureViewModel] under `com.kickstarter.features.projectpage.viewmodel`.
 */
@Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
interface ProjectPageViewModel {
    interface Inputs : ProjectPageInputs
    interface Outputs : ProjectPageOutputs

    @Deprecated(
        message = "Replaced by ProjectFeatureViewModel in com.kickstarter.features.projectpage.viewmodel",
        replaceWith = ReplaceWith(
            expression = "ProjectFeatureViewModel(environment)",
            imports = ["com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel"]
        )
    )
    open class ProjectPageViewModel(environment: Environment) :
        ProjectFeatureViewModel(environment),
        Inputs,
        Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectPageViewModel(environment) as T
        }
    }
}
