package com.kickstarter.ax.safety

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.types.RateLimitBackoff
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Validated idempotency token sent with mutating operations to prevent duplicate server execution.
 *
 * Negative constraints:
 * - Do not reuse the same [IdempotencyKey] across logically distinct user mutations.
 * - Do not pass an empty or blank key.
 */
@JvmInline
value class IdempotencyKey private constructor(val value: String) {
    companion object {
        /**
         * Generates a fresh random UUID idempotency key.
         */
        fun generate(): IdempotencyKey = IdempotencyKey(UUID.randomUUID().toString())

        /**
         * Validates an externally supplied key string.
         */
        fun create(raw: String?): DomainResult<IdempotencyKey> {
            val trimmed = raw?.trim().orEmpty()
            if (trimmed.isEmpty()) {
                return DomainResult.Failure(
                    ProblemDetails(
                        type = ProblemDetails.TYPE_VALIDATION,
                        title = "Invalid Idempotency Key",
                        status = 400,
                        detail = "Idempotency-Key must not be blank for mutating operations.",
                        suggestedAction = "Generate a non-blank IdempotencyKey via IdempotencyKey.generate().",
                        isRetriable = false,
                        invalidParams = listOf(
                            ProblemDetails.InvalidParam(
                                name = "Idempotency-Key",
                                reason = "Must be a non-blank UUID or unique token."
                            )
                        )
                    )
                )
            }
            return DomainResult.Success(IdempotencyKey(trimmed))
        }
    }
}

/**
 * Single-flight execution lock and rate-limit cooldown guard for mutating feature operations.
 *
 * Prevents machine-tempo double-taps or overlapping retries from executing the same mutation
 * concurrently, and honors server `Retry-After` cooldown windows when rate-limited.
 *
 * Negative constraints:
 * - Do not bypass this guard for pledge, checkout, cancellation, or payment mutations.
 *
 * @param clockMillis Monotonic or wall-clock time source in milliseconds for deterministic tests.
 */
class IdempotencyGuard(
    private val clockMillis: () -> Long = { System.currentTimeMillis() }
) {
    companion object {
        @Volatile
        private var globalBlockedUntilMillis: Long = 0L

        /**
         * Propagates a server [RateLimitBackoff] cooldown globally to protect all mutation call sites.
         */
        fun applyGlobalBackoff(
            backoff: RateLimitBackoff,
            clockMillis: () -> Long = { System.currentTimeMillis() }
        ) {
            val retryAfter = backoff.retryAfterSeconds
            if (retryAfter != null && retryAfter > 0L) {
                val until = clockMillis() + (retryAfter * 1000L)
                if (until > globalBlockedUntilMillis) {
                    globalBlockedUntilMillis = until
                }
            }
        }

        fun getGlobalBlockedUntilMillis(): Long = globalBlockedUntilMillis

        fun resetGlobalCooldown() {
            globalBlockedUntilMillis = 0L
        }
    }

    private val inFlight = AtomicBoolean(false)

    @Volatile
    private var blockedUntilMillis: Long = 0L

    /**
     * Applies a server [RateLimitBackoff] cooldown to this guard instance.
     */
    fun applyBackoff(backoff: RateLimitBackoff) {
        val retryAfter = backoff.retryAfterSeconds
        if (retryAfter != null && retryAfter > 0L) {
            val until = clockMillis() + (retryAfter * 1000L)
            if (until > blockedUntilMillis) {
                blockedUntilMillis = until
            }
        }
    }

    fun getBlockedUntilMillis(): Long = maxOf(blockedUntilMillis, globalBlockedUntilMillis)

    /**
     * Returns `true` if a mutation is currently executing inside this guard.
     */
    fun isInFlight(): Boolean = inFlight.get()

    /**
     * Executes [block] exclusively under an in-flight lock with a generated [IdempotencyKey].
     *
     * Rejects overlapping calls with [ProblemDetails.TYPE_IN_FLIGHT] and rejects calls during an
     * active `Retry-After` cooldown with [ProblemDetails.TYPE_RATE_LIMITED].
     */
    suspend fun <T> runSingleFlight(
        operation: String,
        idempotencyKey: IdempotencyKey = IdempotencyKey.generate(),
        block: suspend (IdempotencyKey) -> DomainResult<T>
    ): DomainResult<T> {
        val now = clockMillis()
        val effectiveBlockedUntil = maxOf(blockedUntilMillis, globalBlockedUntilMillis)
        val remainingCooldownMs = effectiveBlockedUntil - now
        if (remainingCooldownMs > 0L) {
            val waitSeconds = ((remainingCooldownMs + 999L) / 1000L).coerceAtLeast(1L)
            return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_RATE_LIMITED,
                    title = "Rate-Limit Cooldown Active",
                    status = 429,
                    detail = "Cannot $operation while server Retry-After cooldown is active ($waitSeconds s remaining).",
                    suggestedAction = "Wait $waitSeconds seconds before retrying $operation.",
                    isRetriable = true,
                    retryAfterSeconds = waitSeconds
                )
            )
        }

        if (!inFlight.compareAndSet(false, true)) {
            return DomainResult.Failure(
                ProblemDetails(
                    type = ProblemDetails.TYPE_IN_FLIGHT,
                    title = "Mutation Already In Flight",
                    status = 409,
                    detail = "An operation to $operation is already in progress.",
                    suggestedAction = "Wait for the active request to finish before triggering $operation again.",
                    isRetriable = false
                )
            )
        }

        return try {
            val result = block(idempotencyKey)
            if (result is DomainResult.Failure) {
                val retryAfter = result.problem.retryAfterSeconds
                if (retryAfter != null && retryAfter > 0L) {
                    val until = clockMillis() + (retryAfter * 1000L)
                    blockedUntilMillis = until
                    if (until > globalBlockedUntilMillis) {
                        globalBlockedUntilMillis = until
                    }
                }
            }
            result
        } finally {
            inFlight.set(false)
        }
    }
}
