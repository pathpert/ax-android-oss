package com.kickstarter.ax.types

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToLong

/**
 * Monetary value represented strictly in integer minor currency units (e.g., cents for USD).
 *
 * Negative constraints:
 * - Do not pass floating-point (`Double` or `Float`) currency values across feature boundaries;
 *   convert at the legacy/API boundary via [fromMajorDouble] or [fromMinorUnits].
 * - Do not pass negative minor units; monetary amounts must be >= 0.
 *
 * @property minorUnits Non-negative amount in the currency's smallest integer unit (e.g., `1500` = `$15.00`).
 */
@JvmInline
value class MinorCurrencyAmount private constructor(val minorUnits: Long) {

    /**
     * Converts minor units to a major-unit [Double] solely when calling legacy formatters such as `KSCurrency`.
     */
    fun toLegacyMajorDouble(fractionDigits: Int = DEFAULT_FRACTION_DIGITS): Double {
        return BigDecimal.valueOf(minorUnits)
            .movePointLeft(fractionDigits)
            .toDouble()
    }

    companion object {
        const val DEFAULT_FRACTION_DIGITS = 2

        /**
         * Creates a validated [MinorCurrencyAmount] from integer minor units.
         *
         * Negative constraint: Do not pass negative numbers.
         */
        fun fromMinorUnits(
            minorUnits: Long,
            fieldName: String = "amount"
        ): DomainResult<MinorCurrencyAmount> {
            if (minorUnits < 0L) {
                return DomainResult.Failure(
                    ProblemDetails(
                        type = ProblemDetails.TYPE_VALIDATION,
                        title = "Invalid Monetary Amount",
                        status = 400,
                        detail = "Monetary amount for '$fieldName' must be >= 0 minor units, received $minorUnits.",
                        suggestedAction = "Provide a non-negative integer amount in minor currency units.",
                        isRetriable = false,
                        invalidParams = listOf(
                            ProblemDetails.InvalidParam(
                                name = fieldName,
                                reason = "Must be an integer >= 0 minor currency units."
                            )
                        )
                    )
                )
            }
            return DomainResult.Success(MinorCurrencyAmount(minorUnits))
        }

        /**
         * Normalizes a legacy floating-point major amount (e.g., `25.50`) at the boundary into [MinorCurrencyAmount].
         *
         * Negative constraint: Rejects `NaN`, infinite, or negative values.
         */
        fun fromMajorDouble(
            majorAmount: Double,
            fractionDigits: Int = DEFAULT_FRACTION_DIGITS,
            fieldName: String = "amount"
        ): DomainResult<MinorCurrencyAmount> {
            if (majorAmount.isNaN() || majorAmount.isInfinite() || majorAmount < 0.0) {
                return DomainResult.Failure(
                    ProblemDetails(
                        type = ProblemDetails.TYPE_VALIDATION,
                        title = "Invalid Monetary Amount",
                        status = 400,
                        detail = "Monetary amount for '$fieldName' must be a finite non-negative number.",
                        suggestedAction = "Supply a finite non-negative amount and convert to integer minor units.",
                        isRetriable = false,
                        invalidParams = listOf(
                            ProblemDetails.InvalidParam(
                                name = fieldName,
                                reason = "Must be finite and >= 0.0."
                            )
                        )
                    )
                )
            }
            val scaled = BigDecimal.valueOf(majorAmount)
                .movePointRight(fractionDigits)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
            return DomainResult.Success(MinorCurrencyAmount(scaled))
        }
    }
}

/**
 * Validated non-blank identifier (e.g., backing ID, project ID, Relay global ID).
 *
 * Negative constraints:
 * - Do not pass empty strings, whitespace-only strings, or `"0"` when a positive/non-blank identifier is required.
 */
@JvmInline
value class NonBlankId private constructor(val value: String) {
    companion object {
        /**
         * Validates and trims [raw] into a [NonBlankId].
         */
        fun create(
            raw: String?,
            fieldName: String = "id"
        ): DomainResult<NonBlankId> {
            val normalized = raw?.trim().orEmpty()
            if (normalized.isEmpty()) {
                return DomainResult.Failure(
                    ProblemDetails(
                        type = ProblemDetails.TYPE_VALIDATION,
                        title = "Missing Required Identifier",
                        status = 400,
                        detail = "Required identifier '$fieldName' is missing or blank.",
                        suggestedAction = "Provide a non-blank '$fieldName' before invoking this operation.",
                        isRetriable = false,
                        invalidParams = listOf(
                            ProblemDetails.InvalidParam(
                                name = fieldName,
                                reason = "Must be a non-blank string."
                            )
                        )
                    )
                )
            }
            return DomainResult.Success(NonBlankId(normalized))
        }
    }
}

/**
 * Parsed server rate-limit and backoff metadata from HTTP response headers.
 *
 * Negative constraints:
 * - Do not ignore [retryAfterSeconds] on HTTP 429/503 responses or retry in a tight loop.
 *
 * @property limit Total request quota reported by `X-RateLimit-Limit`, or `null` if absent.
 * @property remaining Remaining quota reported by `X-RateLimit-Remaining`, or `null` if absent.
 * @property retryAfterSeconds Cooldown duration in seconds parsed from `Retry-After`, or `null` if absent.
 */
data class RateLimitBackoff(
    val limit: Int? = null,
    val remaining: Int? = null,
    val retryAfterSeconds: Long? = null
) {
    companion object {
        /**
         * Extracts rate-limit headers case-insensitively from header pairs.
         */
        fun fromHeaders(headers: List<Pair<String, String>>): RateLimitBackoff {
            fun findHeader(target: String): String? =
                headers.firstOrNull { it.first.equals(target, ignoreCase = true) }?.second?.trim()

            val limit = findHeader("X-RateLimit-Limit")?.toIntOrNull()?.takeIf { it >= 0 }
            val remaining = findHeader("X-RateLimit-Remaining")?.toIntOrNull()?.takeIf { it >= 0 }
            val retryRaw = findHeader("Retry-After")
            val retryAfterSeconds = retryRaw?.toDoubleOrNull()?.roundToLong()?.takeIf { it >= 0L }

            return RateLimitBackoff(
                limit = limit,
                remaining = remaining,
                retryAfterSeconds = retryAfterSeconds
            )
        }
    }
}
