package com.kickstarter.features.projectstory

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.libs.utils.extensions.isProjectUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext

/**
 * UI State for the modern Project Story feature.
 *
 * Negative constraints:
 * - Do not pass unhandled errors without an actionable RFC 9457 [problem].
 *
 * @property isLoading Whether the project story is actively loading from the API.
 * @property error Optional raw failure throwable for backward compatibility.
 * @property storiedProject Project entity and parsed rich text elements, or null.
 * @property problem Structured RFC 9457 actionable error details.
 */
data class ProjectStoryUiState(
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val storiedProject: StoriedProject? = null,
    val problem: ProblemDetails? = null,
)

private object StatsigGateException : Exception()

/**
 * ViewModel responsible for fetching and parsing project campaign story content.
 *
 * Negative constraints:
 * - Do not mutate [projectStoryUiState] directly from external callers.
 */
class ProjectStoryViewModel(
    private val environment: Environment,
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val statsigClient = requireNotNull(environment.statsigClient())

    private val _projectStoryUiState = MutableStateFlow(ProjectStoryUiState())
    val projectStoryUiState = _projectStoryUiState.asStateFlow()

    private var projectSlug: String? = null
    private var job: Job? = null

    fun provideProjectSlug(slug: String) {
        if (projectSlug != slug) {
            projectSlug = slug
        }
    }

    fun _provideProjectUrl(url: String) {
        val uri = url.toUri()
        val parsedSlug =
            if (uri.isProjectUri(environment.webEndpoint()))
                uri.path?.substringAfter("/projects/")
            else
                null
        parsedSlug?.let { provideProjectSlug(it) }
    }

    fun fetchProject() {
        if (!statsigClient.isReady.value || !statsigClient.checkGate(StatsigGateKey.ANDROID_PRELAUNCH_PROJECT_STORY.key)) {
            val gateProblem = ProblemDetails(
                type = "urn:kickstarter:error:feature-gate-disabled",
                title = "Project Story Disabled",
                detail = "The project story feature gate is not enabled for the current configuration.",
                suggestedAction = "Enable ANDROID_PRELAUNCH_PROJECT_STORY gate in Statsig.",
                isRetriable = false
            )
            _projectStoryUiState.value = ProjectStoryUiState(isLoading = false, error = StatsigGateException, problem = gateProblem)
            return
        }

        if (projectStoryUiState.value.storiedProject?.project?.slug() == projectSlug) {
            Timber.d("Project already fetched for slug: $projectSlug")
            return
        }

        val slug = projectSlug.takeIf { !it.isNullOrBlank() } ?: run {
            Timber.d("Project slug is null, cannot fetch project.")
            return
        }

        job?.cancel()
        job = scope.launch {
            _projectStoryUiState.value = ProjectStoryUiState(isLoading = true)
            apolloClient
                .fetchProjectStory(slug)
                .onSuccess {
                    _projectStoryUiState.value = ProjectStoryUiState(
                        isLoading = false,
                        error = null,
                        problem = null,
                        storiedProject = it
                    )
                    Timber.d("storiedProject: ${projectStoryUiState.value.storiedProject}")
                }
                .onFailure {
                    val problem = ProblemDetailsMapper.fromThrowable(it)
                    _projectStoryUiState.value = ProjectStoryUiState(
                        isLoading = false,
                        error = it,
                        problem = problem,
                        storiedProject = null
                    )
                }
        }
    }

    /**
     * Factory producing [ProjectStoryViewModel] with environment dependencies.
     *
     * Negative constraints:
     * - Do not instantiate for unsupported ViewModel classes.
     */
    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectStoryViewModel(environment, testDispatcher) as T
        }
    }
}
