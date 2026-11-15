package com.kickstarter.features.pledgedprojectsoverview.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.UpdateBackerCompletedData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.awaitFirstOrNull

/**
 * Strictly-typed repository boundary for Pledged Projects Overview mutations and lookups.
 *
 * Validates identifiers with [NonBlankId], prevents duplicate mutation execution via
 * [IdempotencyGuard], and normalizes failures into RFC 9457 [ProblemDetails].
 *
 * Negative constraints:
 * - Do not invoke mutations without valid non-blank identifiers.
 * - Do not swallow errors; map through [DomainResult].
 */
interface PledgedProjectsRepository {
    suspend fun confirmAddress(addressId: String, backingId: String): DomainResult<Unit>
    suspend fun updateRewardReceived(backingId: String, received: Boolean): DomainResult<Unit>
    suspend fun fetchProjectBySlug(slug: String): DomainResult<Project>
}

/**
 * Apollo GraphQL implementation of [PledgedProjectsRepository].
 *
 * Negative constraints:
 * - Do not instantiate directly in UI classes; inject via ViewModel Factory.
 */
class ApolloPledgedProjectsRepository(
    private val apolloClient: ApolloClientTypeV2,
    private val addressGuard: IdempotencyGuard = IdempotencyGuard(),
    private val rewardReceivedGuard: IdempotencyGuard = IdempotencyGuard()
) : PledgedProjectsRepository {

    override suspend fun confirmAddress(addressId: String, backingId: String): DomainResult<Unit> {
        val validAddressId = when (val res = NonBlankId.create(addressId, "addressID")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }
        val validBackingId = when (val res = NonBlankId.create(backingId, "backingID")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }

        return addressGuard.runSingleFlight("confirm backing address") {
            val input = CreateOrUpdateBackingAddressData(
                backingID = validBackingId.value,
                addressID = validAddressId.value
            )
            executeBooleanMutation(
                operation = "confirm backing address",
                block = { apolloClient.createOrUpdateBackingAddress(input).awaitFirstOrNull() }
            )
        }
    }

    override suspend fun updateRewardReceived(backingId: String, received: Boolean): DomainResult<Unit> {
        val validBackingId = when (val res = NonBlankId.create(backingId, "backingID")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }

        return rewardReceivedGuard.runSingleFlight("update reward received") {
            val input = UpdateBackerCompletedData(
                backingID = validBackingId.value,
                backerCompleted = received
            )
            executeBooleanMutation(
                operation = "update reward received",
                block = { apolloClient.updateBackerCompleted(input).awaitFirstOrNull() }
            )
        }
    }

    override suspend fun fetchProjectBySlug(slug: String): DomainResult<Project> {
        val validSlug = when (val res = NonBlankId.create(slug, "projectSlug")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }

        return try {
            val project = apolloClient.getProject(slug = validSlug.value).awaitFirstOrNull()
            if (project != null) {
                DomainResult.Success(project)
            } else {
                DomainResult.Failure(
                    ProblemDetails(
                        type = ProblemDetails.TYPE_NOT_FOUND,
                        title = "Project Not Found",
                        status = 404,
                        detail = "Could not load project '${validSlug.value}'.",
                        suggestedAction = "Refresh the Pledged Projects Overview list and try again.",
                        isRetriable = false
                    )
                )
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            DomainResult.Failure(
                ProblemDetailsMapper.fromThrowable(throwable, operation = "load project details")
            )
        }
    }

    private suspend fun executeBooleanMutation(
        operation: String,
        block: suspend () -> Boolean?
    ): DomainResult<Unit> {
        return try {
            val confirmed = block()
            if (confirmed.isTrue()) {
                DomainResult.Success(Unit)
            } else {
                DomainResult.Failure(
                    ProblemDetails(
                        type = ProblemDetails.TYPE_SERVER,
                        title = "Operation Not Confirmed",
                        status = 502,
                        detail = "Server did not confirm completion of $operation.",
                        suggestedAction = "Refresh your pledged projects list and retry if needed.",
                        isRetriable = true
                    )
                )
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            DomainResult.Failure(
                ProblemDetailsMapper.fromThrowable(throwable, operation = operation)
            )
        }
    }
}
