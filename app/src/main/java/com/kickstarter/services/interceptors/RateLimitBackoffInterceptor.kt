package com.kickstarter.services.interceptors

import com.kickstarter.ax.safety.IdempotencyGuard
import com.kickstarter.ax.types.RateLimitBackoff
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Network interceptor that parses rate-limit headers (`Retry-After`, `X-RateLimit-*`)
 * and propagates [RateLimitBackoff] cooldown into [IdempotencyGuard] to guard machine-tempo
 * mutating operations across Retrofit and Apollo calls (AX Pillar 5).
 */
class RateLimitBackoffInterceptor(
    private val onBackoffReceived: (RateLimitBackoff) -> Unit = { IdempotencyGuard.applyGlobalBackoff(it) }
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val pairs = response.headers.names().map { name ->
            name to (response.header(name) ?: "")
        }
        val backoff = RateLimitBackoff.fromHeaders(pairs)
        if (backoff.retryAfterSeconds != null && backoff.retryAfterSeconds > 0L) {
            onBackoffReceived(backoff)
        } else if (response.code == 429 || response.code == 503) {
            // Default 5-second backoff fallback for rate-limited / overloaded responses
            onBackoffReceived(RateLimitBackoff(retryAfterSeconds = 5L))
        }
        return response
    }
}
