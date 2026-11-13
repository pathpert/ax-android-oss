package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.features.comments.data.CommentsFeatureInputs
import com.kickstarter.features.comments.data.CommentsFeatureOutputs
import com.kickstarter.features.comments.viewmodel.CommentsFeatureViewModel
import com.kickstarter.libs.Environment

/**
 * Legacy monolithic ViewModel contract for Comments.
 *
 * @deprecated Replaced by [CommentsFeatureViewModel] under `com.kickstarter.features.comments.viewmodel`.
 */
@Deprecated(
    message = "Replaced by CommentsFeatureViewModel under com.kickstarter.features.comments.viewmodel",
    replaceWith = ReplaceWith("CommentsFeatureViewModel", "com.kickstarter.features.comments.viewmodel.CommentsFeatureViewModel")
)
interface CommentsViewModel {
    interface Inputs : CommentsFeatureInputs
    interface Outputs : CommentsFeatureOutputs

    open class CommentsViewModel(
        environment: Environment,
        intent: Intent? = null
    ) : com.kickstarter.features.comments.viewmodel.CommentsFeatureViewModel(environment, intent),
        Inputs,
        Outputs {
        override val inputs: Inputs = this
        override val outputs: Outputs = this
    }

    class Factory(
        private val environment: Environment,
        private val intent: Intent? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CommentsViewModel(environment, intent) as T
        }
    }
}
