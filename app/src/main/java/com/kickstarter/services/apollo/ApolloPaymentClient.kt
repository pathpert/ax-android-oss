package com.kickstarter.services.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxSingle
import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.CreatePaymentIntentMutation
import com.kickstarter.CreateSetupIntentMutation
import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.SavePaymentMethodMutation
import com.kickstarter.UserPaymentsQuery
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.services.interceptors.RateLimitBackoffInterceptor
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.type.StripeIntentContextTypes
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Apollo GraphQL domain client for payment intents, setup intents, and stored payment sources.
 */
class ApolloPaymentClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable,
    private val createPaymentIntentGuard: AtomicBoolean,
    val idempotencyGuard: IdempotencyGuard = IdempotencyGuard(),
    val rateLimitInterceptor: RateLimitBackoffInterceptor = RateLimitBackoffInterceptor()
) {
    fun createSetupIntent(project: Project? = null): Observable<String> {
        return Observable.defer {
            val ps = PublishSubject.create<String>()
            val mutation = project?.let {
                CreateSetupIntentMutation(
                    projectId = Optional.present(encodeRelayId(it)),
                    setupIntentContext = Optional.present(StripeIntentContextTypes.CROWDFUNDING_CHECKOUT)
                )
            } ?: CreateSetupIntentMutation(Optional.absent(), Optional.present(StripeIntentContextTypes.PROFILE_SETTINGS))

            service.mutation(mutation)
                .rxSingle()
                .subscribeOn(Schedulers.io())
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "create setup intent")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        val problem = ProblemDetailsMapper.fromDomainMessage(
                            response.errors?.first()?.message,
                            "create setup intent"
                        )
                        ps.onError(ProblemException(problem))
                    } else {
                        ps.onNext(response.data?.createSetupIntent?.clientSecret ?: "")
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard> {
        return Observable.defer {
            val ps = PublishSubject.create<StoredCard>()
            val mutation = SavePaymentMethodMutation(
                paymentType = if (savePaymentMethodData.paymentType.isNotNull()) Optional.present(savePaymentMethodData.paymentType) else Optional.absent(),
                stripeToken = if (savePaymentMethodData.stripeToken.isNotNull()) Optional.present(savePaymentMethodData.stripeToken) else Optional.absent(),
                stripeCardId = if (savePaymentMethodData.stripeCardId.isNotNull()) Optional.present(savePaymentMethodData.stripeCardId) else Optional.absent(),
                reusable = if (savePaymentMethodData.reusable.isNotNull()) Optional.present(savePaymentMethodData.reusable) else Optional.absent(),
                intentClientSecret = if (savePaymentMethodData.intentClientSecret.isNotNull()) Optional.present(savePaymentMethodData.intentClientSecret) else Optional.absent()
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "save payment method")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        val problem = ProblemDetailsMapper.fromDomainMessage(
                            response.errors?.first()?.message,
                            "save payment method"
                        )
                        ps.onError(ProblemException(problem))
                    }

                    val paymentSource = response.data?.createPaymentSource?.paymentSource
                    paymentSource?.let {
                        val storedCard = StoredCard.builder()
                            .expiration(it.expirationDate)
                            .id(it.id)
                            .lastFourDigits(it.lastFour)
                            .type(it.type)
                            .build()
                        ps.onNext(storedCard)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun getStoredCards(): Observable<List<StoredCard>> =
        emitAsObservable { _getStoredCards().getOrThrow() }

    private suspend fun _getStoredCards(): Result<List<StoredCard>> = executeForResult {
        val query = UserPaymentsQuery()
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.me?.storedCards?.nodes?.filterNotNull()?.map { cardData ->
            StoredCard.builder()
                .expiration(cardData.expirationDate)
                .id(cardData.id)
                .lastFourDigits(cardData.lastFour)
                .type(cardData.type)
                .stripeCardId(cardData.stripeCardId)
                .build()
        } ?: listOf()
    }

    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<DeletePaymentSourceMutation.Data>()
            val mutation = DeletePaymentSourceMutation(
                paymentSourceId = paymentSourceId
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "save payment method")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        val problem = ProblemDetailsMapper.fromDomainMessage(
                            response.errors?.first()?.message,
                            "save payment method"
                        )
                        ps.onError(ProblemException(problem))
                    }
                    response.data?.let { ps.onNext(it) }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String> {
        return Observable.defer {
            if (!createPaymentIntentGuard.compareAndSet(false, true)) {
                return@defer Observable.error<String>(
                    ProblemException(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_IN_FLIGHT,
                            title = "Mutation Already In Flight",
                            status = 409,
                            detail = "An operation to create payment intent is already in progress.",
                            suggestedAction = "Wait for active payment intent creation to finish before retrying.",
                            isRetriable = false
                        )
                    )
                )
            }
            val ps = PublishSubject.create<String>()
            val checkoutId = createPaymentIntentInput.checkoutId
            val backingId = encodeRelayId(createPaymentIntentInput.backing)
            val mutation = CreatePaymentIntentMutation(
                projectId = encodeRelayId(createPaymentIntentInput.project),
                amount = createPaymentIntentInput.amount,
                paymentIntentContext = Optional.present(StripeIntentContextTypes.POST_CAMPAIGN_CHECKOUT),
                checkoutId = (Base64Utils.encodeUrlSafe(("Checkout-$checkoutId").toByteArray(Charset.defaultCharset()))),
                backingId = Optional.present(backingId)
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "create payment intent")))
                }
                .doFinally { createPaymentIntentGuard.set(false) }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        val problem = ProblemDetailsMapper.fromDomainMessage(
                            response.errors?.first()?.message,
                            "create payment intent"
                        )
                        ps.onError(ProblemException(problem))
                    } else {
                        response.data?.createPaymentIntent?.clientSecret?.let {
                            ps.onNext(it)
                        } ?: run {
                            val problem = ProblemDetails(
                                type = ProblemDetails.TYPE_UNEXPECTED,
                                title = "Missing Payment Client Secret",
                                status = 502,
                                detail = "Server returned null client secret for payment intent.",
                                suggestedAction = "Retry generating payment intent.",
                                isRetriable = true
                            )
                            ps.onError(ProblemException(problem))
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }
}
