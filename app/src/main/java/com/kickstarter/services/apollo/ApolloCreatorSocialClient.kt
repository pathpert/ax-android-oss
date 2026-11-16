package com.kickstarter.services.apollo

import android.util.Pair
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxFlowable
import com.apollographql.apollo3.rx2.rxSingle
import com.google.gson.Gson
import com.kickstarter.CreateFlaggingMutation
import com.kickstarter.FlaggingOptionsQuery
import com.kickstarter.GetProjectUpdatesQuery
import com.kickstarter.ProjectCreatorDetailsQuery
import com.kickstarter.SendMessageMutation
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.FlaggingOption
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.APOLLO_PAGE_SIZE
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.flaggingOptionTransformer
import com.kickstarter.services.transformers.getCreateAttributionEventMutation
import com.kickstarter.services.transformers.getTriggerThirdPartyEventMutation
import com.kickstarter.services.transformers.updateTransformer
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.services.interceptors.RateLimitBackoffInterceptor
import com.kickstarter.type.FlaggingContent
import com.kickstarter.type.NonDeprecatedFlaggingKind
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Apollo GraphQL domain client for creator details, project updates, messaging, flagging, and attribution events.
 */
class ApolloCreatorSocialClient(
    private val service: ApolloClient,
    private val gson: Gson,
    private val disposables: CompositeDisposable,
    val idempotencyGuard: IdempotencyGuard = IdempotencyGuard(),
    val rateLimitInterceptor: RateLimitBackoffInterceptor = RateLimitBackoffInterceptor()
) {
    fun createFlagging(
        project: Project?,
        details: String,
        flaggingKind: String
    ): Observable<String> {
        return Observable.defer {
            project?.let {
                val ps = PublishSubject.create<String>()
                val flagging = NonDeprecatedFlaggingKind.safeValueOf(flaggingKind)
                val mutation = CreateFlaggingMutation(
                    contentId = encodeRelayId(it),
                    details = Optional.present(details),
                    kind = flagging
                )
                service.mutation(mutation)
                    .rxSingle()
                    .doOnError { throwable ->
                        ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "create flagging")))
                    }
                    .subscribe { response ->
                        if (response.hasErrors()) {
                            ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "create flagging")))
                        }
                        response.data?.let { data ->
                            data.createFlagging?.flagging?.kind?.name?.let { kindString ->
                                ps.onNext(kindString)
                            }
                        }
                        ps.onComplete()
                    }.addToDisposable(disposables)
                return@defer ps
            }
        }
    }

    fun flaggingOptions(contentType: FlaggingContent = FlaggingContent.PROJECT): Observable<List<FlaggingOption>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<FlaggingOption>>()
            val query = FlaggingOptionsQuery(contentType = contentType)
            service.query(query = query)
                .rxSingle()
                .subscribe(
                    { response ->
                        if (response.hasErrors()) {
                            ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "fetch flagging options")))
                        } else {
                            ps.onNext(
                                response.data?.flaggingOptions?.map { flaggingOptionTransformer(it) } ?: emptyList()
                            )
                            ps.onComplete()
                        }
                    },
                    { throwable ->
                        ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "fetch flagging options")))
                    }
                ).addToDisposable(disposables)
            return@defer ps
        }
    }

    fun creatorDetails(slug: String): Observable<CreatorDetails> {
        return Observable.defer {
            val ps = PublishSubject.create<CreatorDetails>()
            val query = ProjectCreatorDetailsQuery(slug = slug)
            service.query(query)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "fetch creator details")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "fetch creator details")))
                    }
                    response.data?.project?.creator?.let {
                        ps.onNext(
                            CreatorDetails.builder()
                                .backingsCount(it.backingsCount)
                                .launchedProjectsCount(it.launchedProjects?.totalCount ?: 1)
                                .build()
                        )
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
        return Observable.defer {
            val ps = PublishSubject.create<Long>()
            val mutation = SendMessageMutation(
                projectId = encodeRelayId(project),
                recipientId = encodeRelayId(recipient),
                body = body
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "send message")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "send message")))
                    }
                    response.data?.let {
                        decodeRelayId(
                            response.data?.sendMessage?.conversation?.id
                        )?.let { conversationId ->
                            ps.onNext(conversationId)
                        } ?: ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage("Failed to decode conversation ID", "send message")))
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun getProjectUpdates(
        slug: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<UpdatesGraphQlEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdatesGraphQlEnvelope>()
            val query = GetProjectUpdatesQuery(
                cursor = Optional.present(cursor),
                slug = slug,
                limit = limit
            )
            service.query(query)
                .rxFlowable()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "fetch project updates")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "fetch project updates")))
                    } else {
                        response.data?.let { data ->
                            val updates = data.project?.posts?.edges?.map {
                                updateTransformer(it?.node?.post)
                            }
                            val envelope = UpdatesGraphQlEnvelope.builder()
                                .updates(updates)
                                .totalCount(data.project?.posts?.totalCount ?: 0)
                                .pageInfoEnvelope(
                                    createPageInfoObject(
                                        data.project?.posts?.pageInfo?.pageInfo
                                    )
                                )
                                .build()
                            ps.onNext(envelope)
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
        return Observable.defer {
            val ps = PublishSubject.create<Pair<Boolean, String>>()
            val mutation = getTriggerThirdPartyEventMutation(eventInput)
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "trigger third party event")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "trigger third party event")))
                    } else {
                        val message = response.data?.triggerThirdPartyEvent?.message
                        val isSuccess = response.data?.triggerThirdPartyEvent?.success ?: false
                        ps.onNext(Pair(isSuccess, message))
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun createAttributionEvent(eventInput: CreateAttributionEventData): Observable<Boolean> {
        return Observable.defer {
            val ps = PublishSubject.create<Boolean>()
            val mutation = getCreateAttributionEventMutation(eventInput, gson)
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "create attribution event")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "create attribution event")))
                    }
                    response.data?.let {
                        val isSuccess = it.createAttributionEvent?.successful ?: false
                        ps.onNext(isSuccess)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }
}
