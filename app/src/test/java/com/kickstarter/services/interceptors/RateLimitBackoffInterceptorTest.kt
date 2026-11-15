package com.kickstarter.services.interceptors

import com.kickstarter.ax.types.RateLimitBackoff
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RateLimitBackoffInterceptorTest {

    @Test
    fun `intercept captures Retry-After header and triggers backoff callback`() {
        var capturedBackoff: RateLimitBackoff? = null
        val interceptor = RateLimitBackoffInterceptor { capturedBackoff = it }

        val request = Request.Builder().url("https://api.kickstarter.com/v1/test").build()
        val mockResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .header("Retry-After", "45")
            .header("X-RateLimit-Limit", "100")
            .header("X-RateLimit-Remaining", "0")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()

        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response = mockResponse
            override fun connection() = null
            override fun call() = throw UnsupportedOperationException()
            override fun connectTimeoutMillis() = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun readTimeoutMillis() = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun writeTimeoutMillis() = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        }

        val result = interceptor.intercept(chain)
        assertEquals(429, result.code)
        assertEquals(45L, capturedBackoff?.retryAfterSeconds)
        assertEquals(100, capturedBackoff?.limit)
        assertEquals(0, capturedBackoff?.remaining)
    }

    @Test
    fun `intercept applies default 5s backoff on 429 without Retry-After header`() {
        var capturedBackoff: RateLimitBackoff? = null
        val interceptor = RateLimitBackoffInterceptor { capturedBackoff = it }

        val request = Request.Builder().url("https://api.kickstarter.com/v1/test").build()
        val mockResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()

        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response = mockResponse
            override fun connection() = null
            override fun call() = throw UnsupportedOperationException()
            override fun connectTimeoutMillis() = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun readTimeoutMillis() = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun writeTimeoutMillis() = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        }

        interceptor.intercept(chain)
        assertEquals(5L, capturedBackoff?.retryAfterSeconds)
    }

    @Test
    fun `intercept does not trigger backoff callback on successful 200 without cooldown`() {
        var capturedBackoff: RateLimitBackoff? = null
        val interceptor = RateLimitBackoffInterceptor { capturedBackoff = it }

        val request = Request.Builder().url("https://api.kickstarter.com/v1/test").build()
        val mockResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()

        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response = mockResponse
            override fun connection() = null
            override fun call() = throw UnsupportedOperationException()
            override fun connectTimeoutMillis() = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun readTimeoutMillis() = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun writeTimeoutMillis() = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        }

        interceptor.intercept(chain)
        assertNull(capturedBackoff)
    }
}
