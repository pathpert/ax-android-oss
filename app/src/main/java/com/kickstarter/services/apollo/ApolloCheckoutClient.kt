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
    private val completeCheckoutGuard: AtomicBoolean
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
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { data ->
                            data.createCheckout?.checkout?.let { checkoutObj ->
                                val backingId = decodeRelayId(checkoutObj.id) ?: 0L
                                val backing = Backing.builder().id(backingId).build()
                                decodeRelayId(checkoutObj.id)?.let { id ->
                                    val checkout = CheckoutPayment(
                                        id,
                                        checkoutObj.paymentUrl,
                                        backing = backing
                                    )
                                    ps.onNext(checkout)
                                } ?: ps.onError(Exception("CreateCheckout could not decode ID"))
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
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
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
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message ?: ""))
                    }
                    response.data?.completeOrder?.let {
                        val payload = CompleteOrderPayload(
                            status = it.status.toString(),
                            clientSecret = it.clientSecret ?: ""
                        )
                        ps.onNext(payload)
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
                    ps.onError(it)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let { data ->
                        data.project?.paymentPlan?.let {
                            paymentPlanTransformer(it)
                        }?.let {
                            ps.onNext(it)
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }
}
