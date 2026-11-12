package com.kickstarter.features.cancelpledge.data

import android.os.Bundle
import androidx.core.os.BundleCompat
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.safety.IdempotencyKey
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.libs.KSCurrency
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import java.math.RoundingMode

/**
 * Validated, immutable domain model representing a backing eligible for cancellation.
 *
 * Negative constraints:
 * - Do not pass unvalidated `Bundle` extras or legacy `com.kickstarter.models.Backing` into UI/ViewModel state.
 * - Do not pass floating-point currency amounts; use [pledgeAmount] in minor currency units.
 *
 * @property projectId Positive project identifier.
 * @property projectName Non-blank display name of the backed project.
 * @property backingId Positive backing identifier (`> 0`).
 * @property pledgeAmount Monetary pledge amount in integer minor currency units.
 * @property formattedPledgeAmount Localized currency string formatted for display.
 */
data class CancelPledgeTarget(
    val projectId: NonBlankId,
    val projectName: String,
    val backingId: NonBlankId,
    val backingNumericId: Long,
    val pledgeAmount: MinorCurrencyAmount,
    val formattedPledgeAmount: String
)

/**
 * Unidirectional UI state for canceling a pledge.
 */
data class CancelPledgeUiState(
    val formattedPledgeAmount: String = "",
    val projectName: String = "",
    val isSubmitting: Boolean = false,
    val isCancelButtonVisible: Boolean = true,
    val isReady: Boolean = false,
    val problem: ProblemDetails? = null
)

/**
 * One-shot navigation/completion events emitted by `CancelPledgeFeatureViewModel`.
 */
sealed interface CancelPledgeEvent {
    data object Dismiss : CancelPledgeEvent
    data object Succeeded : CancelPledgeEvent
}

/**
 * Strictly-typed repository boundary abstracting pledge cancellation from legacy `ApolloClientTypeV2`.
 *
 * Negative constraints:
 * - Do not omit [idempotencyKey]; mutating operations must carry a unique idempotency token.
 */
interface CancelPledgeRepository {
    suspend fun cancelBacking(
        target: CancelPledgeTarget,
        note: String,
        idempotencyKey: IdempotencyKey
    ): DomainResult<Unit>
}

/**
 * Boundary mapper normalizing Fragment arguments into [CancelPledgeTarget] or [ProblemDetails].
 *
 * Negative constraints:
 * - Do not use unsafe casts (`as`) or `!!` when reading `ArgumentsKey.CANCEL_PLEDGE_PROJECT`.
 */
object CancelPledgeBundleMapper {

    /**
     * Validates [bundle] and formats the pledge amount using [ksCurrency].
     */
    fun fromBundle(
        bundle: Bundle?,
        ksCurrency: KSCurrency?
    ): DomainResult<CancelPledgeTarget> {
        if (bundle == null) {
            return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Missing Cancellation Arguments",
                    status = 400,
                    detail = "CancelPledgeFragment requires project arguments in its Bundle.",
                    suggestedAction = "Open cancellation via CancelPledgeFragment.newInstance(project) with a backed project.",
                    isRetriable = false,
                    invalidParams = listOf(
                        ProblemDetails.InvalidParam(
                            name = ArgumentsKey.CANCEL_PLEDGE_PROJECT,
                            reason = "Fragment arguments Bundle must not be null."
                        )
                    )
                )
            )
        }

        val project = BundleCompat.getParcelable(
            bundle,
            ArgumentsKey.CANCEL_PLEDGE_PROJECT,
            Project::class.java
        ) ?: return DomainResult.Failure(
            ProblemDetails(
                type = ProblemDetails.TYPE_VALIDATION,
                title = "Missing Project Argument",
                status = 400,
                detail = "Required argument '${ArgumentsKey.CANCEL_PLEDGE_PROJECT}' is missing or invalid.",
                suggestedAction = "Pass a valid backed Project via ArgumentsKey.CANCEL_PLEDGE_PROJECT.",
                isRetriable = false,
                invalidParams = listOf(
                    ProblemDetails.InvalidParam(
                        name = ArgumentsKey.CANCEL_PLEDGE_PROJECT,
                        reason = "Must be a non-null Project Parcelable."
                    )
                )
            )
        )

        val backing = project.backing()
            ?: return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Missing Backing Argument",
                    status = 400,
                    detail = "Project ${project.id()} has no active Backing to cancel.",
                    suggestedAction = "Verify the user has an active backing before opening pledge cancellation.",
                    isRetriable = false,
                    invalidParams = listOf(
                        ProblemDetails.InvalidParam(
                            name = "project.backing",
                            reason = "Backing must not be null."
                        )
                    )
                )
            )

        if (backing.id() <= 0L) {
            return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Invalid Backing Identifier",
                    status = 400,
                    detail = "Backing ID must be positive (> 0), received ${backing.id()}.",
                    suggestedAction = "Reload backing details before attempting cancellation.",
                    isRetriable = false,
                    invalidParams = listOf(
                        ProblemDetails.InvalidParam(
                            name = "backing.id",
                            reason = "Must be a positive integer > 0."
                        )
                    )
                )
            )
        }

        val projectId = when (val res = NonBlankId.create(project.id().toString(), "projectId")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }
        val backingId = when (val res = NonBlankId.create(backing.id().toString(), "backingId")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }
        val pledgeAmount = when (
            val res = MinorCurrencyAmount.fromMajorDouble(backing.amount(), fieldName = "backing.amount")
        ) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }

        val formattedAmount = ksCurrency?.format(
            pledgeAmount.toLegacyMajorDouble(),
            project,
            RoundingMode.HALF_UP
        ) ?: "${project.currencySymbol()}${pledgeAmount.toLegacyMajorDouble()}"

        return DomainResult.Success(
            CancelPledgeTarget(
                projectId = projectId,
                projectName = project.name(),
                backingId = backingId,
                backingNumericId = backing.id(),
                pledgeAmount = pledgeAmount,
                formattedPledgeAmount = formattedAmount
            )
        )
    }
}
