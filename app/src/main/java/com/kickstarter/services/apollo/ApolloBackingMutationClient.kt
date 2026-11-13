package com.kickstarter.services.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxSingle
import com.kickstarter.CancelBackingMutation
import com.kickstarter.CreateBackingMutation
import com.kickstarter.UpdateBackingMutation
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Backing
import com.kickstarter.models.Checkout
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.type.BackingState
import com.kickstarter.type.PaymentTypes
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Apollo GraphQL domain client for backing mutation operations (create, update, cancel).
 */
class ApolloBackingMutationClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable,
    private val createBackingGuard: AtomicBoolean,
    private val updateBackingGuard: AtomicBoolean,
    private val cancelBackingGuard: AtomicBoolean
) {
    fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> {
        return Observable.defer {
            if (!createBackingGuard.compareAndSet(false, true)) {
                return@defer Observable.error<Checkout>(
                    ProblemException(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_IN_FLIGHT,
                            title = "Mutation Already In Flight",
                            status = 409,
                            detail = "An operation to create backing is already in progress.",
                            suggestedAction = "Wait for active mutation to finish before creating backing again.",
                            isRetriable = false
                        )
                    )
                )
            }
            val ps = PublishSubject.create<Checkout>()
            val mutation = CreateBackingMutation(
                projectId = encodeRelayId(createBackingData.project),
                amount = createBackingData.amount,
                paymentType = PaymentTypes.CREDIT_CARD.rawValue,
                paymentSourceId = if (createBackingData.paymentSourceId.isNotNull()) Optional.present(createBackingData.paymentSourceId) else Optional.absent(),
                incremental = if (createBackingData.incremental.isNotNull()) Optional.present(createBackingData.incremental) else Optional.absent(),
                setupIntentClientSecret = if (createBackingData.setupIntentClientSecret.isNotNull()) Optional.present(createBackingData.setupIntentClientSecret) else Optional.absent(),
                locationId = if (createBackingData.locationId.isNotNull()) Optional.present(createBackingData.locationId) else Optional.absent(),
                rewardIds = if (createBackingData.rewardsIds.isNotNull()) Optional.present(createBackingData.rewardsIds?.let { list -> list.map { encodeRelayId(it) } }) else Optional.absent(),
                refParam = if (createBackingData.refTag?.tag().isNotNull()) Optional.present(createBackingData.refTag?.tag()) else Optional.absent()
            )

            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "create backing")))
                }
                .doFinally { createBackingGuard.set(false) }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        val problem = ProblemDetailsMapper.fromDomainMessage(
                            response.errors?.first()?.message,
                            "create backing"
                        )
                        ps.onError(ProblemException(problem))
                    } else {
                        val checkoutPayload = response.data?.createBacking?.checkout
                        val backing = Checkout.Backing.builder()
                            .clientSecret(checkoutPayload?.backing?.checkoutBacking?.clientSecret)
                            .requiresAction(checkoutPayload?.backing?.checkoutBacking?.requiresAction ?: false)
                            .build()
                        val checkout = Checkout.builder()
                            .id(decodeRelayId(checkoutPayload?.id))
                            .backing(backing)
                            .build()
                        ps.onNext(checkout)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
        return Observable.defer {
            if (!updateBackingGuard.compareAndSet(false, true)) {
                return@defer Observable.error<Checkout>(
                    ProblemException(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_IN_FLIGHT,
                            title = "Mutation Already In Flight",
                            status = 409,
                            detail = "An operation to update backing is already in progress.",
                            suggestedAction = "Wait for active mutation to finish before updating backing again.",
                            isRetriable = false
                        )
                    )
                )
            }
            val mutation = UpdateBackingMutation(
                backingId = encodeRelayId(updateBackingData.backing),
                amount = if (updateBackingData.amount.isNotNull()) Optional.present(updateBackingData.amount) else Optional.absent(),
                locationId = if (updateBackingData.locationId.isNotNull()) Optional.present(updateBackingData.locationId) else Optional.absent(),
                rewardIds = if (updateBackingData.rewardsIds.isNotNull()) Optional.present(updateBackingData.rewardsIds?.let { list -> list.map { encodeRelayId(it) } }) else Optional.absent(),
                paymentSourceId = if (updateBackingData.paymentSourceId.isNotNull()) Optional.present(updateBackingData.paymentSourceId) else Optional.absent(),
                intentClientSecret = if (updateBackingData.intentClientSecret.isNotNull()) Optional.present(updateBackingData.intentClientSecret) else Optional.absent(),
                incremental = if (updateBackingData.incremental.isNotNull()) Optional.present(updateBackingData.incremental) else Optional.absent()
            )
            val ps = PublishSubject.create<Checkout>()
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "update backing")))
                }
                .doFinally { updateBackingGuard.set(false) }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        val problem = ProblemDetailsMapper.fromDomainMessage(
                            response.errors?.first()?.message,
                            "update backing"
                        )
                        ps.onError(ProblemException(problem))
                    } else {
                        val checkoutPayload = response.data?.updateBacking?.checkout
                        val backing = Checkout.Backing.builder()
                            .clientSecret(checkoutPayload?.backing?.checkoutBacking?.clientSecret)
                            .requiresAction(checkoutPayload?.backing?.checkoutBacking?.requiresAction ?: false)
                            .build()
                        val checkout = Checkout.builder()
                            .id(decodeRelayId(checkoutPayload?.id))
                            .backing(backing)
                            .build()
                        ps.onNext(checkout)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun cancelBacking(backing: Backing, note: String): Observable<Any> {
        return Observable.defer {
            if (!cancelBackingGuard.compareAndSet(false, true)) {
                return@defer Observable.error<Any>(
                    ProblemException(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_IN_FLIGHT,
                            title = "Mutation Already In Flight",
                            status = 409,
                            detail = "An operation to cancel backing is already in progress.",
                            suggestedAction = "Wait for active cancellation to finish before retrying.",
                            isRetriable = false
                        )
                    )
                )
            }
            val ps = PublishSubject.create<Any>()
            val mutation = CancelBackingMutation(
                backingId = encodeRelayId(backing),
                note = Optional.present(note)
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "cancel backing")))
                }
                .doFinally { cancelBackingGuard.set(false) }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        val problem = ProblemDetailsMapper.fromDomainMessage(
                            response.errors?.first()?.message,
                            "cancel backing"
                        )
                        ps.onError(ProblemException(problem))
                    } else {
                        val state = response.data?.cancelBacking?.backing?.status
                        val success = state == BackingState.canceled
                        ps.onNext(success)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }
}
