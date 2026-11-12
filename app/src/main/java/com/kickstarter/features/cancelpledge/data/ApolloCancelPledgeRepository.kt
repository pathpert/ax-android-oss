package com.kickstarter.features.cancelpledge.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.models.Backing
import com.kickstarter.services.ApolloClientTypeV2
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.awaitFirstOrNull

/**
 * Legacy-bridging implementation of [CancelPledgeRepository] backed by [ApolloClientTypeV2].
 *
 * Isolates the untyped `Observable<Any>` contract of [ApolloClientTypeV2.cancelBacking] at the
 * adapter boundary, normalizing `Boolean`, `String`, `Throwable`, and empty completions into
 * deterministic [DomainResult] outcomes.
 */
class ApolloCancelPledgeRepository(
    private val apolloClient: ApolloClientTypeV2
) : CancelPledgeRepository {

    override suspend fun cancelBacking(
        target: CancelPledgeTarget,
        note: String,
        idempotencyKey: IdempotencyKey
    ): DomainResult<Unit> {
        val legacyBacking = Backing.builder()
            .id(target.backingNumericId)
            .build()

        return try {
            val rawResponse = apolloClient.cancelBacking(legacyBacking, note.trim())
                .awaitFirstOrNull()
            mapRawResponse(rawResponse)
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            DomainResult.Failure(
                ProblemDetailsMapper.fromThrowable(
                    throwable = throwable,
                    operation = "cancel pledge"
                )
            )
        }
    }

    private fun mapRawResponse(rawResponse: Any?): DomainResult<Unit> {
        return when (rawResponse) {
            true -> DomainResult.Success(Unit)
            false -> DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_SERVER,
                    title = "Pledge Cancellation Not Confirmed",
                    status = 502,
                    detail = "The server did not confirm that the pledge transitioned to canceled state.",
                    suggestedAction = "Refresh your backing status and retry if the pledge is still active.",
                    isRetriable = true
                )
            )
            is String -> DomainResult.Failure(
                ProblemDetailsMapper.fromDomainMessage(
                    rawMessage = rawResponse,
                    operation = "cancel pledge"
                )
            )
            is Throwable -> DomainResult.Failure(
                ProblemDetailsMapper.fromThrowable(
                    throwable = rawResponse,
                    operation = "cancel pledge"
                )
            )
            null -> DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_UNEXPECTED,
                    title = "Empty Cancellation Response",
                    status = 502,
                    detail = "The server completed the cancel pledge request without returning a confirmation.",
                    suggestedAction = "Refresh your backing status before attempting to cancel again.",
                    isRetriable = true
                )
            )
            else -> DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_UNEXPECTED,
                    title = "Unexpected Cancellation Response",
                    status = 502,
                    detail = "Received unexpected response type '${rawResponse::class.java.simpleName}' while canceling pledge.",
                    suggestedAction = "Refresh the backing status and contact support if the pledge remains active.",
                    isRetriable = false
                )
            )
        }
    }
}
