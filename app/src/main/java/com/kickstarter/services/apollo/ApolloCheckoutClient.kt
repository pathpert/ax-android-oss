package com.kickstarter.services.apollo

import android.util.Pair
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxFlowable
import com.apollographql.apollo3.rx2.rxSingle
import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.BuildPaymentPlanQuery
import com.kickstarter.CompleteOnSessionCheckoutMutation
import com.kickstarter.CompleteOrderMutation
import com.kickstarter.CreateCheckoutMutation
import com.kickstarter.ValidateCheckoutQuery
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.services.interceptors.RateLimitBackoffInterceptor
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Backing
import com.kickstarter.models.BuildPaymentPlanData
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.models.CompleteOrderPayload
import com.kickstarter.models.PaymentPlan
import com.kickstarter.models.PaymentValidationResponse
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.paymentPlanTransformer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Apollo GraphQL domain client for checkout creation, validation, completion, and payment plans.
 */
class ApolloCheckoutClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable,
    private val completeCheckoutGuard: AtomicBoolean,
    val idempotencyGuard: IdempotencyGuard = IdempotencyGuard(),
    val rateLimitInterceptor: RateLimitBackoffInterceptor = RateLimitBackoffInterceptor()
) {
    fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> {
        return Observable.defer {
            val ps = PublishSubject.create<CheckoutPayment>()
            val mutation = CreateCheckoutMutation(
                projectId = encodeRelayId(createCheckoutData.project),
                amount = createCheckoutData.amount,
                rewardIds = Optional.present(
                    createCheckoutData.rewardsIds?.let { list ->
                        list.map { encodeRelayId(it) }
                    }
                ),
                locationId = if (createCheckoutData.locationId.isNotNull()) Optional.present(createCheckoutData.locationId) else Optional.absent(),
                refParam = if (createCheckoutData.refTag?.tag().isNotNull()) Optional.present(createCheckoutData.refTag?.tag()) else Optional.absent()
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "create checkout")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "create checkout")))
                    } else {
                        val checkoutObj = response.data?.createCheckout?.checkout
                        if (checkoutObj == null) {
                            ps.onError(ProblemException(boundaryProblem("Missing Checkout", "CreateCheckout returned no checkout object.")))
                        } else {
                            val decodedId = decodeRelayId(checkoutObj.id)
                            if (decodedId == null) {
                                ps.onError(
                                    ProblemException(
                                        boundaryProblem(
                                            "Checkout ID Decode Failure",
                                            "CreateCheckout returned a checkout ID that could not be decoded.",
                                            type = ProblemDetails.TYPE_DECODE
                                        )
                                    )
                                )
                            } else {
                                val backing = Backing.builder().id(decodedId).build()
                                ps.onNext(CheckoutPayment(decodedId, checkoutObj.paymentUrl, backing = backing))
                            }
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun validateCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String
    ): Observable<PaymentValidationResponse> {
        return Observable.defer {
            val ps = PublishSubject.create<PaymentValidationResponse>()
            val query = ValidateCheckoutQuery(
                checkoutId = checkoutId,
                paymentIntentClientSecret = paymentIntentClientSecret,
                paymentSourceId = paymentSourceId
            )
            service.query(query)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "validate checkout")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "validate checkout")))
                    } else {
                        response.data?.let { data ->
                            val validation = PaymentValidationResponse(
                                data.checkout?.isValidForOnSessionCheckout?.valid ?: false,
                                data.checkout?.isValidForOnSessionCheckout?.messages ?: listOf()
                            )
                            ps.onNext(validation)
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun completeOnSessionCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String?,
        paymentSourceReusable: Boolean
    ): Observable<Pair<String, Boolean>> {
        return Observable.defer {
            if (!completeCheckoutGuard.compareAndSet(false, true)) {
                return@defer Observable.error<Pair<String, Boolean>>(
                    ProblemException(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_IN_FLIGHT,
                            title = "Mutation Already In Flight",
                            status = 409,
                            detail = "An operation to complete checkout is already in progress.",
                            suggestedAction = "Wait for active on-session checkout completion before retrying.",
                            isRetriable = false
                        )
                    )
                )
            }
            val ps = PublishSubject.create<Pair<String, Boolean>>()
            val mutation = CompleteOnSessionCheckoutMutation(
                checkoutId = Base64Utils.encodeUrlSafe(("Checkout-$checkoutId").toByteArray(Charset.defaultCharset())),
                paymentIntentClientSecret = paymentIntentClientSecret,
                paymentSourceId = if (paymentSourceId.isNotNull()) Optional.present(paymentSourceId) else Optional.absent(),
                paymentSourceReusable = Optional.present(paymentSourceReusable)
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "complete checkout")))
                }
                .doFinally { completeCheckoutGuard.set(false) }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        val problem = ProblemDetailsMapper.fromDomainMessage(
                            response.errors?.first()?.message,
                            "complete checkout"
                        )
                        ps.onError(ProblemException(problem))
                    } else {
                        response.data?.completeOnSessionCheckout?.checkout?.id?.let { resCheckoutId ->
                            response.data?.completeOnSessionCheckout?.checkout?.backing?.requiresAction?.let { requiresAction ->
                                ps.onNext(Pair(resCheckoutId, requiresAction))
                            }
                        } ?: run {
                            val problem = ProblemDetails(
                                type = ProblemDetails.TYPE_UNEXPECTED,
                                title = "Missing Checkout ID",
                                status = 502,
                                detail = "Server returned null checkout ID on checkout completion.",
                                suggestedAction = "Reload checkout state and retry.",
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

    fun completeOrder(orderInput: CompleteOrderInput): Observable<CompleteOrderPayload> {
        return Observable.defer {
            val ps = PublishSubject.create<CompleteOrderPayload>()
            val mutation = CompleteOrderMutation(
                orderId = "",
                stripePaymentMethodId = Optional.present(orderInput.stripePaymentMethodId),
                paymentSourceReusable = Optional.present(orderInput.paymentSourceReusable)
            )
            service.mutation(mutation)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(throwable, "complete order")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "complete order")))
                    } else {
                        val payload = response.data?.completeOrder?.let {
                            CompleteOrderPayload(
                                status = it.status.toString(),
                                clientSecret = it.clientSecret ?: ""
                            )
                        }
                        if (payload == null) {
                            ps.onError(
                                ProblemException(
                                    boundaryProblem(
                                        "Missing Order Payload",
                                        "CompleteOrder returned no order payload.",
                                        "Reload checkout state and retry the order completion."
                                    )
                                )
                            )
                        } else {
                            ps.onNext(payload)
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    fun buildPaymentPlan(input: BuildPaymentPlanData): Observable<PaymentPlan> {
        return Observable.defer {
            val ps = PublishSubject.create<PaymentPlan>()
            val query = BuildPaymentPlanQuery(
                slug = input.slug,
                amount = input.amount,
                includeRefundedAmount = false
            )
            service.query(query)
                .rxFlowable()
                .subscribeOn(Schedulers.io())
                .doOnError {
                    ps.onError(ProblemException(ProblemDetailsMapper.fromThrowable(it, "build payment plan")))
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(ProblemException(ProblemDetailsMapper.fromDomainMessage(response.errors?.first()?.message, "build payment plan")))
                    } else {
                        val paymentPlan = response.data?.project?.paymentPlan?.let { paymentPlanTransformer(it) }
                        if (paymentPlan == null) {
                            ps.onError(
                                ProblemException(
                                    boundaryProblem(
                                        "Missing Payment Plan",
                                        "BuildPaymentPlan returned no payment plan for the project.",
                                        "Reload the project details and retry building the payment plan."
                                    )
                                )
                            )
                        } else {
                            ps.onNext(paymentPlan)
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    private fun boundaryProblem(
        title: String,
        detail: String,
        suggestedAction: String = "Re-fetch the checkout and retry with a fresh Idempotency-Key.",
        type: String = ProblemDetails.TYPE_UNEXPECTED
    ): ProblemDetails = ProblemDetails(
        type = type,
        title = title,
        status = 502,
        detail = detail,
        suggestedAction = suggestedAction,
        isRetriable = true
    )
}
