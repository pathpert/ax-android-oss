package com.kickstarter.features.discovery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.features.discovery.data.DiscoveryEnvelopeContract
import com.kickstarter.features.discovery.data.DiscoveryProjectCard
import com.kickstarter.features.discovery.data.DiscoveryQuery
import com.kickstarter.features.discovery.data.DiscoveryRepository
import com.kickstarter.models.Category
import com.kickstarter.type.ProjectSort
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Unidirectional UI state for the modern Discovery feature.
 */
data class DiscoveryUiState(
    val projects: List<DiscoveryProjectCard> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val selectedSort: ProjectSort = ProjectSort.MAGIC,
    val isLoading: Boolean = false,
    val hasMorePages: Boolean = false,
    val problem: ProblemDetails? = null
)

/**
 * Modernized ViewModel for Project Discovery.
 *
 * Replaces monolithic legacy [com.kickstarter.viewmodels.DiscoveryViewModel] and [com.kickstarter.viewmodels.DiscoveryFragmentViewModel].
 *
 * Negative constraints:
 * - Do not mutate [uiState] outside the unidirectional flow reducer.
 */
class DiscoveryFeatureViewModel(
    private val repository: DiscoveryRepository,
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private var nextCursor: String? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, problem = null) }
            when (val catResult = repository.getCategories()) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(categories = catResult.value) }
                }
                is DomainResult.Failure -> {
                    // Non-fatal, proceed to load projects
                }
            }
            fetchProjects(reset = true)
        }
    }

    fun refresh() {
        scope.launch {
            fetchProjects(reset = true)
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isLoading || !_uiState.value.hasMorePages || nextCursor == null) return
        scope.launch {
            fetchProjects(reset = false)
        }
    }

    fun categorySelected(category: Category?) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        refresh()
    }

    fun sortSelected(sort: ProjectSort) {
        if (_uiState.value.selectedSort == sort) return
        _uiState.update { it.copy(selectedSort = sort) }
        refresh()
    }

    private suspend fun fetchProjects(reset: Boolean) {
        _uiState.update { it.copy(isLoading = true, problem = null) }
        val cursor = if (reset) null else nextCursor
        val query = DiscoveryQuery(
            category = _uiState.value.selectedCategory,
            sort = _uiState.value.selectedSort,
            cursor = cursor
        )

        when (val result = repository.getProjects(query)) {
            is DomainResult.Success -> {
                val envelope: DiscoveryEnvelopeContract = result.value
                nextCursor = envelope.endCursor
                _uiState.update { current ->
                    val combined = if (reset) envelope.projects else current.projects + envelope.projects
                    current.copy(
                        projects = combined,
                        hasMorePages = envelope.hasMorePages,
                        isLoading = false,
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

    fun clearProblem() {
        _uiState.update { it.copy(problem = null) }
    }
}

