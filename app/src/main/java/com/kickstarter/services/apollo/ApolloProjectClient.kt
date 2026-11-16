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
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.services.interceptors.RateLimitBackoffInterceptor
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
import kotlinx.coroutines.CancellationException

/**
 * Apollo GraphQL domain client for project retrieval, story, watching, and similar projects.
 *
 * Negative constraints:
 * - Do not swallow mutation errors; map to [ProblemDetails] carrying suggestedAction and isRetriable flag.
 */
class ApolloProjectClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable,
    val idempotencyGuard: IdempotencyGuard = IdempotencyGuard(),
    val rateLimitInterceptor: RateLimitBackoffInterceptor = RateLimitBackoffInterceptor()
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
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "fetch project")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "fetch project")))
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
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "watch project")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "watch project")))
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
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "unwatch project")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "unwatch project")))
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
        val domainResult = idempotencyGuard.runSingleFlight("watch project") {
            try {
                val mutation = WatchProjectMutation(id = encodeRelayId(project))
                val response = service.mutation(mutation).execute()
                if (response.hasErrors()) {
                    DomainResult.Failure(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "watch project"))
                } else {
                    DomainResult.Success(projectTransformer(response.data?.watchProject?.project?.fullProject))
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                DomainResult.Failure(ProblemDetailsMapper.fromThrowable(throwable, "watch project"))
            }
        }
        return when (domainResult) {
            is DomainResult.Success -> Result.success(domainResult.value)
            is DomainResult.Failure -> Result.failure(ProblemException(domainResult.problem))
        }
    }

    suspend fun unWatchProjectSuspend(project: Project): Result<Project> {
        if (!project.hasValidRelayId()) {
            return Result.failure(invalidProjectException(project, "unWatchProjectSuspend"))
        }
        val domainResult = idempotencyGuard.runSingleFlight("unwatch project") {
            try {
                val mutation = UnwatchProjectMutation(id = encodeRelayId(project))
                val response = service.mutation(mutation).execute()
                if (response.hasErrors()) {
                    DomainResult.Failure(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "unwatch project"))
                } else {
                    DomainResult.Success(projectTransformer(response.data?.watchProject?.project?.fullProject))
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                DomainResult.Failure(ProblemDetailsMapper.fromThrowable(throwable, "unwatch project"))
            }
        }
        return when (domainResult) {
            is DomainResult.Success -> Result.success(domainResult.value)
            is DomainResult.Failure -> Result.failure(ProblemException(domainResult.problem))
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
    ): Result<Project> {
        val domainResult = idempotencyGuard.runSingleFlight("add user to secret reward group") {
            try {
                val mutation = AddUserToSecretRewardGroupMutation(
                    projectId = encodeRelayId(project).toString(),
                    secretRewardToken = secretRewardToken
                )
                val response = service.mutation(mutation).execute()
                if (response.hasErrors()) {
                    DomainResult.Failure(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "add user to secret reward group"))
                } else {
                    val updatedProject = response.data?.addUserToSecretRewardGroup?.project
                    DomainResult.Success(projectTransformer(updatedProject))
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                DomainResult.Failure(ProblemDetailsMapper.fromThrowable(throwable, "add user to secret reward group"))
            }
        }
        return when (domainResult) {
            is DomainResult.Success -> Result.success(domainResult.value)
            is DomainResult.Failure -> Result.failure(ProblemException(domainResult.problem))
        }
    }
}
