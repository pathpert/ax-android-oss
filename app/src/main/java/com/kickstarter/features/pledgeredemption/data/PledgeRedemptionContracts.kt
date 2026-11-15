package com.kickstarter.features.pledgeredemption.data

import android.os.Bundle
import androidx.core.os.BundleCompat
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.IntentKey

/**
 * Immutable line item representing a pledged reward or add-on in pledge redemption.
 *
 * Negative constraints:
 * - Do not pass floating-point currency into [pledgeAmount]; use integer minor units via [MinorCurrencyAmount].
 *
 * @property title Display title of the reward or add-on; falls back to `"Reward"` if untitled.
 * @property pledgeAmount Monetary amount for this item in integer minor currency units.
 * @property formattedAmount Decimal string representation for legacy `CheckoutScreen` display.
 */
data class RedemptionRewardItem(
    val title: String,
    val pledgeAmount: MinorCurrencyAmount,
    val formattedAmount: String
)

/**
 * Validated, immutable domain arguments required to render pledge redemption.
 *
 * Negative constraints:
 * - Do not construct directly from unvalidated `Bundle` extras or pass raw `com.kickstarter.models.Project`
 *   into ViewModel state reducers; always normalize through [PledgeRedemptionIntentMapper].
 * - Do not pass floating-point currency amounts.
 *
 * @property projectId Validated non-blank project ID.
 * @property projectName Display name of the backed project.
 * @property backingId Validated non-blank backing ID.
 * @property shippingAmount Shipping fee in integer minor units.
 * @property totalAmount Total pledge amount in integer minor units.
 * @property bonusAmount Bonus amount in integer minor units.
 * @property rewardLines Line items representing rewards and add-ons.
 * @property userEmail Contact email of the redeeming user.
 */
data class PledgeRedemptionArgs(
    val projectId: NonBlankId,
    val projectName: String,
    val backingId: NonBlankId,
    val shippingAmount: MinorCurrencyAmount,
    val totalAmount: MinorCurrencyAmount,
    val bonusAmount: MinorCurrencyAmount,
    val rewardLines: List<RedemptionRewardItem>,
    val userEmail: String?
)

/**
 * Temporary bridge carrying legacy models strictly for `CheckoutScreen` until `CheckoutScreen`
 * is migrated to AX contracts in Wave 3.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null project, reward, or shipping rule instances.
 *
 * @property project Legacy Project Parcelable model.
 * @property selectedReward Legacy Reward Parcelable model.
 * @property shippingRule Legacy ShippingRule Parcelable model.
 */
@Deprecated(
    message = "Legacy UI bridge for CheckoutScreen; remove once CheckoutScreen accepts PledgeRedemptionArgs directly."
)
data class PledgeRedemptionLegacySnapshot(
    val project: Project,
    val selectedReward: Reward,
    val shippingRule: ShippingRule
)

/**
 * Unidirectional UI state for Pledge Redemption.
 *
 * Negative constraints:
 * - Do not expose raw unhandled exceptions; all failures MUST resolve to [ProblemDetails].
 */
sealed interface PledgeRedemptionUiState {
    /**
     * Arguments extraction and validation in progress.
     *
     * Negative constraints:
     * - Do not render checkout form while in loading state.
     */
    data object Loading : PledgeRedemptionUiState

    /**
     * Arguments validated and ready to display redemption checkout screen.
     *
     * Negative constraints:
     * - Do not emit with invalid or negative currency amounts.
     *
     * @property args Normalized domain redemption arguments.
     * @property legacySnapshot Legacy UI snapshot bridge.
     */
    data class Ready(
        val args: PledgeRedemptionArgs,
        @Suppress("DEPRECATION")
        val legacySnapshot: PledgeRedemptionLegacySnapshot
    ) : PledgeRedemptionUiState

    /**
     * Actionable error state with RFC 9457 details.
     *
     * Negative constraints:
     * - Do not omit suggested remediation actions.
     *
     * @property problem Structured actionable error details.
     */
    data class Error(val problem: ProblemDetails) : PledgeRedemptionUiState
}

/**
 * Boundary mapper normalizing Intent/Bundle extras into [PledgeRedemptionArgs] or actionable [ProblemDetails].
 *
 * Negative constraints:
 * - Do not silently fabricate empty `Project` or `Backing` builders when required extras are missing.
 * - Do not use unsafe casts (`as`) or `!!` on Intent extras.
 */
object PledgeRedemptionIntentMapper {

    /**
     * Extracts and validates [PledgeRedemptionArgs] from [bundle] and optional [userEmail].
     */
    @Suppress("DEPRECATION")
    fun fromBundle(
        bundle: Bundle?,
        userEmail: String? = null
    ): DomainResult<Pair<PledgeRedemptionArgs, PledgeRedemptionLegacySnapshot>> {
        if (bundle == null) {
            return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Missing Pledge Redemption Arguments",
                    status = 400,
                    detail = "PledgeRedemptionActivity was launched without Intent extras.",
                    suggestedAction = "Launch PledgeRedemptionActivity using Intent.getPledgeRedemptionIntent(context, project) with a backed project.",
                    isRetriable = false,
                    invalidParams = listOf(ProblemDetails.InvalidParam(IntentKey.PROJECT, "Required Parcelable Project extra is missing."))
                )
            )
        }

        val project = BundleCompat.getParcelable(bundle, IntentKey.PROJECT, Project::class.java)
            ?: return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Missing Project Extra",
                    status = 400,
                    detail = "Required Intent extra '${IntentKey.PROJECT}' is missing or has an unexpected type.",
                    suggestedAction = "Pass a valid backed Project via IntentKey.PROJECT.",
                    isRetriable = false,
                    invalidParams = listOf(ProblemDetails.InvalidParam(IntentKey.PROJECT, "Must be a non-null Project Parcelable."))
                )
            )

        if (project.id() <= 0L) {
            return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Invalid Project Identifier",
                    status = 400,
                    detail = "Project ID must be positive (> 0), received ${project.id()}.",
                    suggestedAction = "Reload the project before opening pledge redemption.",
                    isRetriable = false,
                    invalidParams = listOf(
                        ProblemDetails.InvalidParam(
                            name = "project.id",
                            reason = "Must be a positive integer > 0."
                        )
                    )
                )
            )
        }

        val backing = project.backing()
            ?: return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Missing Backing Information",
                    status = 400,
                    detail = "Project ${project.id()} does not include an active Backing for pledge redemption.",
                    suggestedAction = "Ensure the project has a loaded Backing before opening pledge redemption.",
                    isRetriable = false,
                    invalidParams = listOf(
                        ProblemDetails.InvalidParam(
                            name = "project.backing",
                            reason = "Backing must be present for pledge redemption."
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
                    suggestedAction = "Refresh the backing details before opening pledge redemption.",
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
        val shippingAmount = when (
            val res = MinorCurrencyAmount.fromMajorDouble(backing.shippingAmount().toDouble(), fieldName = "shippingAmount")
        ) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }
        val totalAmount = when (
            val res = MinorCurrencyAmount.fromMajorDouble(backing.amount(), fieldName = "totalAmount")
        ) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }
        val bonusAmount = when (
            val res = MinorCurrencyAmount.fromMajorDouble(backing.bonusAmount(), fieldName = "bonusAmount")
        ) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }

        val rewardModels = buildList {
            backing.reward()?.let(::add)
            backing.addOns()?.let(::addAll)
        }
        val rewardLines = mutableListOf<RedemptionRewardItem>()
        for (reward in rewardModels) {
            val itemAmount = when (
                val res = MinorCurrencyAmount.fromMajorDouble(reward.pledgeAmount(), fieldName = "reward.pledgeAmount")
            ) {
                is DomainResult.Success -> res.value
                is DomainResult.Failure -> return res
            }
            rewardLines.add(
                RedemptionRewardItem(
                    title = reward.title()?.trim()?.takeIf { it.isNotEmpty() } ?: "Reward",
                    pledgeAmount = itemAmount,
                    formattedAmount = reward.pledgeAmount().toString()
                )
            )
        }

        val args = PledgeRedemptionArgs(
            projectId = projectId,
            projectName = project.name(),
            backingId = backingId,
            shippingAmount = shippingAmount,
            totalAmount = totalAmount,
            bonusAmount = bonusAmount,
            rewardLines = rewardLines,
            userEmail = userEmail?.trim()?.takeIf { it.isNotEmpty() }
        )

        val selectedReward = backing.reward() ?: RewardFactory.rewardWithShipping()
        val legacySnapshot = PledgeRedemptionLegacySnapshot(
            project = project,
            selectedReward = selectedReward,
            shippingRule = ShippingRule.builder().build()
        )

        return DomainResult.Success(args to legacySnapshot)
    }
}
