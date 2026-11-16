package com.kickstarter.services.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.rx2.rxFlowable
import com.apollographql.apollo3.rx2.rxSingle
import com.kickstarter.ErroredBackingsQuery
import com.kickstarter.GetBackingQuery
import com.kickstarter.GetProjectBackingQuery
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Backing
import com.kickstarter.models.ErroredBacking
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.UpdateBackerCompletedData
import com.kickstarter.services.interceptors.RateLimitBackoffInterceptor
import com.kickstarter.services.transformers.backingTransformer
import com.kickstarter.services.transformers.getCreateOrUpdateBackingAddressMutation
import com.kickstarter.services.transformers.getPledgedProjectsOverviewQuery
import com.kickstarter.services.transformers.getUpdateBackerCompletedMutation
import com.kickstarter.services.transformers.pledgedProjectsOverviewEnvelopeTransformer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Apollo GraphQL domain client for backing queries, address updates, backer completed updates, and pledged projects overview.
 */
class ApolloBackingQueryClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable,
    val rateLimitInterceptor: RateLimitBackoffInterceptor = RateLimitBackoffInterceptor()
) {
    fun getBacking(backingId: String): Observable<Backing> {
        return Observable.defer {
            val ps = PublishSubject.create<Backing>()
            val query = GetBackingQuery(backingId = backingId)
            service.query(query)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let {
                            it.backing?.let { backingFragments ->
                                backingTransformer(backingFragments.backing)?.let { backingObject ->
                                    ps.onNext(backingObject)
                                }
                            }
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun getProjectBacking(slug: String): Observable<Backing> =
        emitAsObservable { _getProjectBacking(slug).getOrThrow() }

    private suspend fun _getProjectBacking(slug: String): Result<Backing> = executeForResult {
        val query = GetProjectBackingQuery(slug = slug)
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        backingTransformer(response.data?.project?.backing?.backing)
    }

    fun erroredBackings(): Observable<List<ErroredBacking>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<ErroredBacking>>()
            val query = ErroredBackingsQuery()
            service.query(query)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        val erroredBackings = response.data?.me?.backings?.nodes?.map {
                            val project = ErroredBacking.Project.builder()
                                .finalCollectionDate(it?.project?.finalCollectionDate)
                                .name(it?.project?.name)
                                .slug(it?.project?.slug)
                                .build()
                            ErroredBacking.builder()
                                .project(project)
                                .build()
                        } ?: listOf()
                        ps.onNext(erroredBackings)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean> {
        return Observable.defer {
            val ps = PublishSubject.create<Boolean>()
            val mutation = getCreateOrUpdateBackingAddressMutation(eventInput)
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message ?: ""))
                    }
                    response.data?.let {
                        val isSuccess = it.createOrUpdateBackingAddress?.success ?: false
                        ps.onNext(isSuccess)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun updateBackerCompleted(inputData: UpdateBackerCompletedData): Observable<Boolean> {
        return Observable.defer {
            val ps = PublishSubject.create<Boolean>()
            val mutation = getUpdateBackerCompletedMutation(inputData)
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message ?: ""))
                    }
                    response.data?.let {
                        val backerCompleted = it.updateBackerCompleted?.backing?.backerCompleted ?: false
                        ps.onNext(backerCompleted)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<PledgedProjectsOverviewEnvelope>()
            service.query(getPledgedProjectsOverviewQuery(inputData))
                .rxFlowable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message ?: ""))
                    }
                    response.data?.let { data ->
                        data.pledgeProjectsOverview?.let {
                            pledgedProjectsOverviewEnvelopeTransformer(it)
                        }?.let {
                            ps.onNext(it)
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }
}
