package com.kickstarter.services.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxSingle
import com.kickstarter.AddUserToSecretRewardGroupMutation
import com.kickstarter.FetchProjectQuery
import com.kickstarter.FetchProjectStoryQuery
import com.kickstarter.FetchSimilarProjectsQuery
import com.kickstarter.UnwatchProjectMutation
import com.kickstarter.WatchProjectMutation
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.hasValidRelayId
import com.kickstarter.models.Project
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.extensions.toStoriedProject
import com.kickstarter.services.transformers.projectTransformer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Apollo GraphQL domain client for project retrieval, story, watching, and similar projects.
 */
class ApolloProjectClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable
) {
    fun getProject(project: Project): Observable<Project> {
        return getProject(project.slug() ?: "")
    }

    fun getProject(slug: String): Observable<Project> {
        return Observable.defer {
            val ps = PublishSubject.create<Project>()
            val query = FetchProjectQuery(slug)
            service.query(query)
                .rxSingle()
                .subscribeOn(Schedulers.io())
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    } else {
                        val project = projectTransformer(response.data?.project?.fullProject)
                        if (project.hasValidRelayId()) {
                            ps.onNext(project)
                        } else {
                            ps.onError(invalidProjectException(project, "getProject($slug)"))
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun watchProject(project: Project): Observable<Project> {
        return Observable.defer {
            if (!project.hasValidRelayId()) {
                return@defer Observable.error<Project>(invalidProjectException(project, "watchProject"))
            }
            val ps = PublishSubject.create<Project>()
            val mutation = WatchProjectMutation(id = encodeRelayId(project))
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    }
                    ps.onNext(projectTransformer(response.data?.watchProject?.project?.fullProject))
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun unWatchProject(project: Project): Observable<Project> {
        return Observable.defer {
            if (!project.hasValidRelayId()) {
                return@defer Observable.error<Project>(invalidProjectException(project, "unWatchProject"))
            }
            val ps = PublishSubject.create<Project>()
            val mutation = UnwatchProjectMutation(id = encodeRelayId(project))
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(java.lang.Exception(response.errors?.first()?.message))
                    }
                    ps.onNext(projectTransformer(response.data?.watchProject?.project?.fullProject))
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    suspend fun watchProjectSuspend(project: Project): Result<Project> {
        if (!project.hasValidRelayId()) {
            return Result.failure(invalidProjectException(project, "watchProjectSuspend"))
        }
        return executeForResult {
            val mutation = WatchProjectMutation(id = encodeRelayId(project))
            val response = service.mutation(mutation).execute()
            if (response.hasErrors()) {
                throw buildClientException(response.errors)
            }
            projectTransformer(response.data?.watchProject?.project?.fullProject)
        }
    }

    suspend fun unWatchProjectSuspend(project: Project): Result<Project> {
        if (!project.hasValidRelayId()) {
            return Result.failure(invalidProjectException(project, "unWatchProjectSuspend"))
        }
        return executeForResult {
            val mutation = UnwatchProjectMutation(id = encodeRelayId(project))
            val response = service.mutation(mutation).execute()
            if (response.hasErrors()) {
                throw buildClientException(response.errors)
            }
            projectTransformer(response.data?.watchProject?.project?.fullProject)
        }
    }

    suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>> = executeForResult {
        val query = FetchSimilarProjectsQuery(
            first = Optional.present(4),
            similarToPid = pid.toString(),
            excludePid = pid.toInt(),
            recommended = Optional.present(true),
            seed = Optional.present(pid.toInt())
        )
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.let { responseData ->
            responseData.projects?.nodes?.map {
                projectTransformer(it?.similarProject)
            }
        } ?: emptyList()
    }

    suspend fun fetchProjectStory(slug: String): Result<StoriedProject> = executeForResult {
        val query = FetchProjectStoryQuery(slug = slug)
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.project?.projectStoryFragment.toStoriedProject()
    }

    suspend fun addUserToSecretRewardGroup(
        project: Project,
        secretRewardToken: String
    ): Result<Project> = executeForResult {
        val mutation = AddUserToSecretRewardGroupMutation(
            projectId = encodeRelayId(project).toString(),
            secretRewardToken = secretRewardToken
        )
        val response = service.mutation(mutation).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        val updatedProject = response.data?.addUserToSecretRewardGroup?.project
        projectTransformer(updatedProject)
    }
}
