package com.kickstarter.features.paymentmethods.data

import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.getCardTypeDrawable
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.type.PaymentTypes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.awaitFirstOrNull
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Immutable domain representation of a user's stored payment card.
 *
 * Negative constraints:
 * - Do not pass raw `com.kickstarter.models.StoredCard` or Apollo generated types across the feature UI boundary.
 *
 * @property id Validated non-blank payment source ID.
 * @property lastFour Last four digits of the credit card.
 * @property issuer Card issuer brand name (e.g., "Visa", "Mastercard").
 * @property issuerDrawableRes Drawable resource ID representing the card issuer brand.
 * @property expirationFormatted Expiration date string formatted per [StoredCard.DATE_FORMAT].
 */
data class PaymentMethodCard(
    val id: NonBlankId,
    val lastFour: String,
    val issuer: String,
    val issuerDrawableRes: Int,
    val expirationFormatted: String
)

/**
 * Unidirectional UI state for the payment methods screen.
 *
 * Negative constraints:
 * - Do not mutate state directly; emit through ViewModel StateFlow reducers.
 * - Do not pass raw unhandled exceptions; map errors to [ProblemDetails].
 * - Do not pass mutable lists into [cards].
 */
data class PaymentMethodsUiState(
    val cards: List<PaymentMethodCard> = emptyList(),
    val isLoading: Boolean = false,
    val isDividerVisible: Boolean = false,
    val pendingDeleteCard: PaymentMethodCard? = null,
    val problem: ProblemDetails? = null
)

/**
 * One-shot navigation and user feedback events for payment methods.
 *
 * Negative constraints:
 * - Do not emit events for blank IDs or empty client secrets.
 */
sealed interface PaymentMethodsEvent {
    data class PresentPaymentSheet(val setupClientId: String, val userEmail: String) : PaymentMethodsEvent
    data class SuccessMessage(val message: String) : PaymentMethodsEvent
    data class ShowDeleteConfirmation(val card: PaymentMethodCard) : PaymentMethodsEvent
}

/**
 * Strictly-typed repository boundary for payment source queries and mutations.
 *
 * Negative constraints:
 * - Do not bypass [idempotencyKey] on mutating operations (delete, setup intent, save).
 */
interface PaymentMethodsRepository {
    suspend fun getStoredCards(): DomainResult<List<PaymentMethodCard>>
    suspend fun deletePaymentSource(cardId: NonBlankId, idempotencyKey: IdempotencyKey): DomainResult<String>
    suspend fun createSetupIntent(idempotencyKey: IdempotencyKey): DomainResult<String>
    suspend fun savePaymentMethod(
        setupClientId: String,
        paymentType: PaymentTypes = PaymentTypes.CREDIT_CARD,
        idempotencyKey: IdempotencyKey
    ): DomainResult<PaymentMethodCard>
}

/**
 * Boundary mapper normalizing legacy [StoredCard] models into [PaymentMethodCard].
 */
object PaymentMethodBoundaryMapper {

    private val sdf = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

    fun fromLegacy(storedCard: StoredCard): DomainResult<PaymentMethodCard> {
        val rawId = storedCard.id()
        val nonBlankId = when (val res = NonBlankId.create(rawId, "storedCard.id")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }

        val lastFour = storedCard.lastFourDigits()?.trim().orEmpty()
        val issuer = storedCard.type()?.let { StoredCard.issuer(it) } ?: "Card"
        val expirationFormatted = storedCard.expiration()?.let { sdf.format(it) } ?: ""

        return DomainResult.Success(
            PaymentMethodCard(
                id = nonBlankId,
                lastFour = lastFour,
                issuer = issuer,
                issuerDrawableRes = storedCard.getCardTypeDrawable(),
                expirationFormatted = expirationFormatted
            )
        )
    }
}

/**
 * Apollo-backed implementation of [PaymentMethodsRepository] with idempotency guards and RFC 9457 mapping.
 */
class ApolloPaymentMethodsRepository(
    private val apolloClient: ApolloClientTypeV2,
    private val deleteGuard: IdempotencyGuard = IdempotencyGuard(),
    private val setupGuard: IdempotencyGuard = IdempotencyGuard(),
    private val saveGuard: IdempotencyGuard = IdempotencyGuard()
) : PaymentMethodsRepository {

    override suspend fun getStoredCards(): DomainResult<List<PaymentMethodCard>> {
        return try {
            val legacyCards = apolloClient.getStoredCards().awaitFirstOrNull() ?: emptyList()
            val mapped = mutableListOf<PaymentMethodCard>()
            for (card in legacyCards) {
                when (val result = PaymentMethodBoundaryMapper.fromLegacy(card)) {
                    is DomainResult.Success -> mapped.add(result.value)
                    is DomainResult.Failure -> return result
                }
            }
            DomainResult.Success(mapped)
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            DomainResult.Failure(
                ProblemDetailsMapper.fromThrowable(throwable, operation = "fetch stored cards")
            )
        }
    }

    override suspend fun deletePaymentSource(
        cardId: NonBlankId,
        idempotencyKey: IdempotencyKey
    ): DomainResult<String> {
        return deleteGuard.runSingleFlight("delete payment method") {
            try {
                val data: DeletePaymentSourceMutation.Data? =
                    apolloClient.deletePaymentSource(cardId.value).awaitFirstOrNull()
                val mutationId = data?.paymentSourceDelete?.clientMutationId.orEmpty()
                DomainResult.Success(mutationId)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                DomainResult.Failure(
                    ProblemDetailsMapper.fromThrowable(throwable, operation = "delete payment method")
                )
            }
        }
    }

    override suspend fun createSetupIntent(idempotencyKey: IdempotencyKey): DomainResult<String> {
        return setupGuard.runSingleFlight("prepare payment setup") {
            try {
                val secret = apolloClient.createSetupIntent(null).awaitFirstOrNull().orEmpty()
                if (secret.isNotBlank()) {
                    DomainResult.Success(secret)
                } else {
                    DomainResult.Failure(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_SERVER,
                            title = "Empty Setup Intent Secret",
                            status = 502,
                            detail = "Stripe setup intent secret returned by the server was empty.",
                            suggestedAction = "Retry adding the payment method.",
                            isRetriable = true
                        )
                    )
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                DomainResult.Failure(
                    ProblemDetailsMapper.fromThrowable(throwable, operation = "create setup intent")
                )
            }
        }
    }

    override suspend fun savePaymentMethod(
        setupClientId: String,
        paymentType: PaymentTypes,
        idempotencyKey: IdempotencyKey
    ): DomainResult<PaymentMethodCard> {
        return saveGuard.runSingleFlight("save payment method") {
            try {
                val input = SavePaymentMethodData(
                    paymentType = paymentType,
                    stripeToken = setupClientId,
                    stripeCardId = null,
                    reusable = true,
                    intentClientSecret = setupClientId
                )
                val stored = apolloClient.savePaymentMethod(input).awaitFirstOrNull()
                if (stored != null) {
                    PaymentMethodBoundaryMapper.fromLegacy(stored)
                } else {
                    DomainResult.Failure(
                        ProblemDetails(
                            type = ProblemDetails.TYPE_SERVER,
                            title = "Save Payment Method Failed",
                            status = 502,
                            detail = "Server did not return a saved card after payment setup confirmation.",
                            suggestedAction = "Refresh your payment methods list to confirm if the card was added.",
                            isRetriable = true
                        )
                    )
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (throwable: Throwable) {
                DomainResult.Failure(
                    ProblemDetailsMapper.fromThrowable(throwable, operation = "save payment method")
                )
            }
        }
    }
}
