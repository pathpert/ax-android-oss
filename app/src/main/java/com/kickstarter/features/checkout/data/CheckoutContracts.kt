package com.kickstarter.features.checkout.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.models.Reward

/**
 * Immutable, strictly-typed add-on contract for checkout UI and ViewModels.
 *
 * Negative constraints:
 * - Do not pass legacy `com.kickstarter.models.Reward` or raw Apollo generated models into
 *   checkout Compose screens.
 * - Do not pass floating-point currency amounts; use [pledgeAmount] in minor currency units.
 *
 * @property id Validated non-blank add-on identifier.
 * @property title Display title of the add-on.
 * @property description Optional backer-facing description.
 * @property pledgeAmount Unit price of the add-on in integer minor currency units.
 * @property isAvailable Whether the add-on is currently available for selection.
 * @property limitPerBacker Optional maximum quantity per backer, or `null` when unlimited.
 */
data class AddOnContract(
    val id: NonBlankId,
    val title: String,
    val description: String?,
    val pledgeAmount: MinorCurrencyAmount,
    val isAvailable: Boolean,
    val limitPerBacker: Int?
)

/**
 * Typed GraphQL cursor pagination contract decoupled from legacy `PageInfoEnvelope`.
 *
 * Negative constraints:
 * - Do not pass blank strings to [endCursor] when [hasNextPage] is true.
 *
 * @property endCursor Opaque pagination cursor for fetching next page.
 * @property hasNextPage Whether more pages exist.
 */
data class CursorPageContract(
    val endCursor: String? = null,
    val hasNextPage: Boolean = false
)

/**
 * Typed add-ons page contract replacing legacy [AddOnsEnvelope].
 *
 * Negative constraints:
 * - Do not mutate [items] list after instantiation.
 *
 * @property items List of validated add-on contracts.
 * @property pageInfo Pagination cursor and next page indicator.
 */
data class AddOnsPageContract(
    val items: List<AddOnContract>,
    val pageInfo: CursorPageContract
)

/**
 * Boundary mapper normalizing legacy [AddOnsEnvelope] into [AddOnsPageContract].
 *
 * Negative constraints:
 * - Do not silently skip malformed negative-amount or invalid-ID add-ons; fail loudly with
 *   [ProblemDetails] containing `invalidParams`.
 */
object AddOnBoundaryMapper {

    /**
     * Converts a legacy [AddOnsEnvelope] into a validated [AddOnsPageContract].
     */
    @Suppress("DEPRECATION")
    fun fromLegacy(envelope: AddOnsEnvelope): DomainResult<AddOnsPageContract> {
        val mapped = mutableListOf<AddOnContract>()
        for ((index, reward) in envelope.addOnsList.withIndex()) {
            when (val item = mapReward(reward, index)) {
                is DomainResult.Success -> mapped.add(item.value)
                is DomainResult.Failure -> return item
            }
        }

        val pageInfo = CursorPageContract(
            endCursor = envelope.pageInfo?.endCursor,
            hasNextPage = envelope.pageInfo?.hasNextPage == true
        )
        return DomainResult.Success(
            AddOnsPageContract(
                items = mapped,
                pageInfo = pageInfo
            )
        )
    }

    private fun mapReward(reward: Reward, index: Int): DomainResult<AddOnContract> {
        if (reward.id() <= 0L) {
            return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_VALIDATION,
                    title = "Invalid Add-On Identifier",
                    status = 400,
                    detail = "Add-on at index $index has non-positive id ${reward.id()}.",
                    suggestedAction = "Reload available add-ons for the selected reward.",
                    isRetriable = false,
                    invalidParams = listOf(
                        ProblemDetails.InvalidParam(
                            name = "addOnsList[$index].id",
                            reason = "Must be a positive integer > 0."
                        )
                    )
                )
            )
        }

        val id = when (val res = NonBlankId.create(reward.id().toString(), "addOnsList[$index].id")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }
        val pledgeAmount = when (
            val res = MinorCurrencyAmount.fromMajorDouble(
                majorAmount = reward.pledgeAmount(),
                fieldName = "addOnsList[$index].pledgeAmount"
            )
        ) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return res
        }

        return DomainResult.Success(
            AddOnContract(
                id = id,
                title = reward.title()?.trim()?.takeIf { it.isNotEmpty() } ?: "Add-on",
                description = reward.description()?.trim()?.takeIf { it.isNotEmpty() },
                pledgeAmount = pledgeAmount,
                isAvailable = reward.isAvailable(),
                limitPerBacker = reward.limit()
            )
        )
    }
}
