package com.kickstarter.features.backing.data

import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Repository interface isolating network interactions for backing details and status updates.
 *
 * Negative Constraints:
 * - Do NOT bypass this repository to call raw Apollo or Retrofit services directly in ViewModels.
 */
interface BackingRepository {
    fun getBackingInfo(projectData: ProjectData): Observable<BackingState>
    fun postBackingReceived(project: Project, backing: Backing, checked: Boolean): Observable<Backing>
    suspend fun getBackingInfoSuspend(projectData: ProjectData): BackingState = BackingState.Loading
    fun cleanDisposables()
}

/**
 * Default Apollo GraphQL and Retrofit implementation of [BackingRepository].
 *
 * Negative constraints:
 * - Do not instantiate directly in UI layers; inject via ViewModel Factory.
 * - Map all failures through [ProblemDetailsMapper] with actionable suggestedAction and isRetriable flag.
 */
class BackingRepositoryImpl(
    private val apolloClient: ApolloClientTypeV2,
    private val apiClient: ApiClientTypeV2,
    private val idempotencyGuard: IdempotencyGuard = IdempotencyGuard()
) : BackingRepository {

    override fun getBackingInfo(projectData: ProjectData): Observable<BackingState> {
        val backing = projectData.backing()
        val slug = projectData.project().slug()

        return when {
            backing != null -> Observable.just(BackingState.Success(backing))
            !slug.isNullOrBlank() -> Observable.concat(
                Observable.just(BackingState.Loading),
                apolloClient.getProjectBacking(slug)
                    .timeout(5, TimeUnit.SECONDS)
                    .retry(1)
                    .map<BackingState> { BackingState.Success(it) }
                    .onErrorReturn { error ->
                        val problem = ProblemDetailsMapper.fromThrowable(error)
                        BackingState.Error(problem = problem, throwable = error)
                    }
            )
            else -> Observable.empty()
        }
    }

    override fun postBackingReceived(project: Project, backing: Backing, checked: Boolean): Observable<Backing> {
        return apiClient.postBacking(project, backing, checked)
    }

    override suspend fun getBackingInfoSuspend(projectData: ProjectData): BackingState {
        val backing = projectData.backing()
        val slug = projectData.project().slug()

        return when {
            backing != null -> BackingState.Success(backing)
            !slug.isNullOrBlank() -> {
                runCatching {
                    val remoteBacking = apolloClient.getProjectBacking(slug).blockingFirst()
                    BackingState.Success(remoteBacking)
                }.getOrElse { error ->
                    val problem = ProblemDetailsMapper.fromThrowable(error)
                    BackingState.Error(problem = problem, throwable = error)
                }
            }
            else -> BackingState.Error(
                problem = ProblemDetailsMapper.fromThrowable(
                    IllegalArgumentException("Missing project slug or backing")
                )
            )
        }
    }

    override fun cleanDisposables() {
        apolloClient.cleanDisposables()
    }
}
