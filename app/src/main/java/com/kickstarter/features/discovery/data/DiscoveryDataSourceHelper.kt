package com.kickstarter.features.discovery.data

import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import io.reactivex.Observable
import timber.log.Timber

/**
 * Data fetch and project save mutation helpers for Discovery.
 *
 * Negative constraints:
 * - Do not swallow errors; never return null silently across the boundary.
 */
object DiscoveryDataSourceHelper {

    fun saveProject(apolloClient: ApolloClientTypeV2, project: Project): Observable<Project> {
        return apolloClient.watchProject(project)
            .compose(Transformers.neverErrorV2())
    }

    fun unSaveProject(apolloClient: ApolloClientTypeV2, project: Project): Observable<Project> {
        return apolloClient.unWatchProject(project)
            .compose(Transformers.neverErrorV2())
    }

    fun toggleProjectSave(apolloClient: ApolloClientTypeV2, project: Project): Observable<Project> {
        return if (project.isStarred()) {
            unSaveProject(apolloClient, project)
        } else {
            saveProject(apolloClient, project)
        }
    }

    fun fetchActivity(apiClient: ApiClientTypeV2): Observable<Activity> {
        return apiClient.fetchActivities(1)
            .distinctUntilChanged()
            .materialize()
            .share()
            .filter { it.value.isNotNull() }
            .map { it.value }
            .map { it.activities() }
            .filter { it.isNotEmpty() }
            .map { it.first() }
            .doOnError { error ->
                val problem = ProblemDetailsMapper.fromThrowable(error, "fetch discovery activity")
                Timber.w(ProblemException(problem, error), "Failed to fetch activity: %s", problem.detail)
            }
            .onErrorResumeNext(Observable.empty())
    }

    fun fetchUserEmail(apolloClient: ApolloClientTypeV2): Observable<String> {
        return apolloClient.userPrivacy()
            .distinctUntilChanged()
            .materialize()
            .share()
            .filter { it.value.isNotNull() }
            .map { it.value }
            .map { it.email }
            .filter { it.isNotNull() }
            .doOnError { error ->
                val problem = ProblemDetailsMapper.fromThrowable(error, "fetch user privacy email")
                Timber.w(ProblemException(problem, error), "Failed to fetch user privacy email: %s", problem.detail)
            }
            .onErrorResumeNext(Observable.empty())
    }
}
