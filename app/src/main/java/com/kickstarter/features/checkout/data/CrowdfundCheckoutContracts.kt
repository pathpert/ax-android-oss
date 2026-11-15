package com.kickstarter.features.checkout.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.models.BuildPaymentPlanData
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.awaitFirstOrNull

private val ZERO_MINOR: MinorCurrencyAmount = MinorCurrencyAmount.ZERO

/**
 * Immutable container for checkout currency amounts in integer minor units.
 *
 * Negative constraints:
 * - Do not pass floating-point currency (Double or Float); always use [MinorCurrencyAmount].
 *
 * @property shippingAmount Shipping cost in minor currency units (e.g., cents).
 * @property bonusAmount Optional bonus support amount in minor currency units.
 * @property totalAmount Total pledge transaction amount in minor currency units.
 */
data class CheckoutAmounts(
    val shippingAmount: MinorCurrencyAmount,
    val bonusAmount: MinorCurrencyAmount,
    val totalAmount: MinorCurrencyAmount
) {
    companion object {
        val ZERO = CheckoutAmounts(
            shippingAmount = ZERO_MINOR,
            bonusAmount = ZERO_MINOR,
            totalAmount = ZERO_MINOR
        )

        /**
         * Safely creates [CheckoutAmounts] from legacy Double amounts.
         */
        fun fromDoubles(
            shipping: Double,
            bonus: Double,
            total: Double
        ): CheckoutAmounts {
            val s = when (val res = MinorCurrencyAmount.fromMajorDouble(shipping)) {
                is DomainResult.Success -> res.value
                is DomainResult.Failure -> ZERO_MINOR
            }
            val b = when (val res = MinorCurrencyAmount.fromMajorDouble(bonus)) {
                is DomainResult.Success -> res.value
                is DomainResult.Failure -> ZERO_MINOR
            }
            val t = when (val res = MinorCurrencyAmount.fromMajorDouble(total)) {
                is DomainResult.Success -> res.value
                is DomainResult.Failure -> ZERO_MINOR
            }
            return CheckoutAmounts(
                shippingAmount = s,
                bonusAmount = b,
                totalAmount = t
            )
        }
    }
}

/**
 * Normalized payment plan information for Pledge Over Time (PLOT).
 *
 * Negative constraints:
 * - Do not pass negative installment counts or unvalidated payment increments.
 *
 * @property isPledgeOverTimeEligible Whether this pledge is eligible for installment payments.
 * @property paymentIncrements List of scheduled installment payments.
 */
data class CheckoutPaymentPlan(
    val isPledgeOverTimeEligible: Boolean,
    val paymentIncrements: List<PaymentIncrement> = emptyList()
)

/**
 * Successful checkout result identifier.
 *
 * Negative constraints:
 * - Do not construct with negative or zero [checkoutId].
 *
 * @property checkoutId Non-zero positive checkout confirmation identifier.
 */
data class CheckoutBackingResult(
    val checkoutId: Long
)

/**
 * Strictly-typed repository boundary for pledge and checkout operations.
 *
 * Negative constraints:
 * - Do not bypass [IdempotencyKey] on mutating calls ([createBacking], [updateBacking], [createSetupIntent]).
 * - Do not expose raw Apollo exceptions across this boundary; all failures must resolve to [ProblemDetails].
 */
interface CrowdfundCheckoutRepository {
    suspend fun buildPaymentPlan(
        projectSlug: NonBlankId,
        amount: MinorCurrencyAmount
    ): DomainResult<CheckoutPaymentPlan>

    suspend fun createBacking(
        data: CreateBackingData,
        idempotencyKey: IdempotencyKey
    ): DomainResult<CheckoutBackingResult>

    suspend fun updateBacking(
        data: UpdateBackingData,
        idempotencyKey: IdempotencyKey
    ): DomainResult<CheckoutBackingResult>

    suspend fun createSetupIntent(
        project: Project,
        idempotencyKey: IdempotencyKey
    ): DomainResult<String>
}

/**
 * Apollo-backed implementation of [CrowdfundCheckoutRepository] enforcing single-flight execution
 * and RFC 9457 error normalization.
 *
 * Negative constraints:
 * - Do not instantiate directly in UI classes; inject via ViewModel Factory.
 */
class ApolloCrowdfundCheckoutRepository(
    private val apolloClient: ApolloClientTypeV2,
    private val createGuard: IdempotencyGuard = IdempotencyGuard(),
    private val updateGuard: IdempotencyGuard = IdempotencyGuard(),
    private val setupGuard: IdempotencyGuard = IdempotencyGuard()
) : CrowdfundCheckoutRepository {

    override suspend fun buildPaymentPlan(
        projectSlug: NonBlankId,
        amount: MinorCurrencyAmount
    ): DomainResult<CheckoutPaymentPlan> {
        return try {
            val queryData = BuildPaymentPlanData(
                slug = projectSlug.value,
                amount = amount.toLegacyMajorDouble().toString()
            )
            val plan = apolloClient.buildPaymentPlan(queryData).awaitFirstOrNull()
            if (plan != null) {
                DomainResult.Success(
                    CheckoutPaymentPlan(
                        isPledgeOverTimeEligible = plan.amountIsPledgeOverTimeEligible,
                        paymentIncrements = plan.paymentIncrements ?: emptyList()
                    )
                )
            } else {
                DomainResult.Failure(
                    ProblemDetails(
                        type = ProblemDetails.TYPE_UNEXPECTED,
                        title = "Payment Plan Unavailable",
                        status = 502,
                        detail = "Server returned empty payment plan response.",
                        suggestedAction = "Proceed with full pledge or retry.",
                        isRetriable = true
                    )
                )
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (e: Throwable) {
            DomainResult.Failure(ProblemDetailsMapper.fromThrowable(e, "build payment plan"))
        }
    }

    override suspend fun createBacking(
        data: CreateBackingData,
        idempotencyKey: IdempotencyKey
    ): DomainResult<CheckoutBackingResult> {
        return createGuard.runSingleFlight("create backing") {
            try {
                val checkout = apolloClient.createBacking(data).awaitFirstOrNull()
                val id = checkout?.id()
                if (id != null && id > 0L) {
                    DomainResult.Success(CheckoutBackingResult(checkoutId = id))
                } else {
                    DomainResult.Failure(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_UNEXPECTED,
                            title = "Backing Creation Incomplete",
                            status = 502,
                            detail = "Server did not return a valid checkout ID.",
                            suggestedAction = "Check your backed projects before retrying to prevent duplicate charges.",
                            isRetriable = false
                        )
                    )
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (e: Throwable) {
                DomainResult.Failure(ProblemDetailsMapper.fromThrowable(e, "create backing"))
            }
        }
    }

    override suspend fun updateBacking(
        data: UpdateBackingData,
        idempotencyKey: IdempotencyKey
    ): DomainResult<CheckoutBackingResult> {
        return updateGuard.runSingleFlight("update backing") {
            try {
                val checkout = apolloClient.updateBacking(data).awaitFirstOrNull()
                val id = checkout?.id()
                if (id != null && id > 0L) {
                    DomainResult.Success(CheckoutBackingResult(checkoutId = id))
                } else {
                    DomainResult.Failure(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_UNEXPECTED,
                            title = "Backing Update Incomplete",
                            status = 502,
                            detail = "Server did not return a valid updated checkout ID.",
                            suggestedAction = "Refresh your pledge details to confirm whether changes took effect.",
                            isRetriable = false
                        )
                    )
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (e: Throwable) {
                DomainResult.Failure(ProblemDetailsMapper.fromThrowable(e, "update backing"))
            }
        }
    }

    override suspend fun createSetupIntent(
        project: Project,
        idempotencyKey: IdempotencyKey
    ): DomainResult<String> {
        return setupGuard.runSingleFlight("create setup intent") {
            try {
                val secret = apolloClient.createSetupIntent(project).awaitFirstOrNull().orEmpty()
                if (secret.isNotBlank()) {
                    DomainResult.Success(secret)
                } else {
                    DomainResult.Failure(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_UNEXPECTED,
                            title = "Setup Intent Failed",
                            status = 502,
                            detail = "Received empty client secret for Stripe Setup Intent.",
                            suggestedAction = "Select a saved card or try adding the card again.",
                            isRetriable = true
                        )
                    )
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (e: Throwable) {
                DomainResult.Failure(ProblemDetailsMapper.fromThrowable(e, "create setup intent"))
            }
        }
    }
}
