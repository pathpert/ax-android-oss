package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.ui.IntentKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing state transitions and user intents for BackingDetailsViewModel.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
@Deprecated(
    message = "Legacy monolithic path replaced by BackingFeatureViewModel under com.kickstarter.features.backing.viewmodel.",
    replaceWith = ReplaceWith("BackingFeatureViewModel", "com.kickstarter.features.backing.viewmodel.BackingFeatureViewModel")
)
class BackingDetailsViewModel(environment: Environment, private val intent: Intent? = null) : ViewModel() {

    private var mutableUrl = MutableStateFlow<String>("")
    val url: StateFlow<String>
        get() = mutableUrl.asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = ""
            )
    init {
        viewModelScope.launch {
            intent?.getStringExtra(IntentKey.URL)?.let { url ->
                mutableUrl.emit(url)
            }
        }
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackingDetailsViewModel(environment, intent) as T
        }
    }
}
