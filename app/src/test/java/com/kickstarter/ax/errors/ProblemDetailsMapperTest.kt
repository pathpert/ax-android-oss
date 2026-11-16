package com.kickstarter.ax.errors

import com.apollographql.apollo3.api.http.HttpHeader
import com.apollographql.apollo3.exception.ApolloHttpException
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.kickstarter.services.ApiException
import com.kickstarter.services.KSApolloClientV2.KSApolloClientV2Exception
import com.kickstarter.services.apiresponses.ErrorEnvelope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException

class ProblemDetailsMapperTest {

    @Test
    fun `fromThrowable maps unauthorized ApiException to non-retriable 401 ProblemDetails`() {
        val errorBody = """{"error_messages":["Unauthorized"],"ksr_code":"unauthorized"}"""
            .toResponseBody("application/json".toMediaType())
        val retrofitResponse = Response.error<String>(401, errorBody)
        val envelope = ErrorEnvelope.builder()
            .httpCode(401)
            .ksrCode(ErrorEnvelope.UNAUTHORIZED)
            .errorMessages(listOf("Session expired"))
            .build()

        val problem = ProblemDetailsMapper.fromThrowable(
            throwable = ApiException(envelope, retrofitResponse),
            operation = "cancel pledge"
        )

        assertEquals(ProblemDetails.TYPE_UNAUTHORIZED, problem.type)
        assertEquals(401, problem.status)
        assertEquals("Session expired", problem.detail)
        assertFalse(problem.isRetriable)
        assertTrue(problem.suggestedAction.contains("Re-authenticate"))
    }

    @Test
    fun `fromThrowable maps ApolloHttpException 429 with Retry-After header`() {
        val exception = ApolloHttpException(
            statusCode = 429,
            headers = listOf(
                HttpHeader("Retry-After", "30"),
                HttpHeader("X-RateLimit-Remaining", "0")
            ),
            body = null,
            message = "Too many requests"
        )

        val problem = ProblemDetailsMapper.fromThrowable(exception, operation = "update backing")

        assertEquals(ProblemDetails.TYPE_RATE_LIMITED, problem.type)
        assertEquals(429, problem.status)
        assertEquals(30L, problem.retryAfterSeconds)
        assertTrue(problem.isRetriable)
        assertTrue(problem.suggestedAction.contains("30"))
    }

    @Test
    fun `fromThrowable maps ApolloNetworkException and timeout as retriable`() {
        val networkProblem = ProblemDetailsMapper.fromThrowable(
            ApolloNetworkException("No network"),
            operation = "load redemption"
        )
        val timeoutProblem = ProblemDetailsMapper.fromThrowable(
            SocketTimeoutException("Read timed out"),
            operation = "load redemption"
        )

        assertEquals(ProblemDetails.TYPE_NETWORK, networkProblem.type)
        assertTrue(networkProblem.isRetriable)
        assertEquals(ProblemDetails.TYPE_TIMEOUT, timeoutProblem.type)
        assertEquals(504, timeoutProblem.status)
        assertTrue(timeoutProblem.isRetriable)
    }

    @Test
    fun `fromThrowable maps InvalidProject with invalidParams`() {
        val problem = ProblemDetailsMapper.fromThrowable(
            KSApolloClientV2Exception.InvalidProject("Invalid project id: 0"),
            operation = "watch project"
        )

        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals(400, problem.status)
        assertFalse(problem.isRetriable)
        assertEquals(1, problem.invalidParams.size)
        assertEquals("projectId", problem.invalidParams.first().name)
    }

    @Test
    fun `fromDomainMessage never produces blank detail when message is null`() {
        val problem = ProblemDetailsMapper.fromDomainMessage(
            rawMessage = null,
            operation = "cancel pledge"
        )

        assertEquals(ProblemDetails.TYPE_UNEXPECTED, problem.type)
        assertFalse(problem.detail.contains("Something went wrong", ignoreCase = true))
        assertTrue(problem.suggestedAction.isNotBlank())
    }

    @Test
    fun `fromThrowable restores ProblemDetails from wrapped ProblemException cause chain`() {
        val original = ProblemDetails(
            type = ProblemDetails.TYPE_DECODE,
            title = "Checkout ID Decode Failure",
            status = 502,
            detail = "CreateCheckout returned a checkout ID that could not be decoded.",
            suggestedAction = "Re-fetch the checkout and retry with a fresh Idempotency-Key.",
            isRetriable = true
        )
        val wrapped = RuntimeException(
            "Apollo failed to parse response",
            RuntimeException("intermediate wrapper", ProblemException(original))
        )

        val problem = ProblemDetailsMapper.fromThrowable(wrapped, operation = "create checkout")

        assertEquals(original, problem)
        assertEquals(ProblemDetails.TYPE_DECODE, problem.type)
        assertTrue(problem.isRetriable)
    }

    @Test
    fun `fromThrowable maps unwrapped unknown throwable to domain message without inventing structure`() {
        val problem = ProblemDetailsMapper.fromThrowable(
            RuntimeException("plain failure"),
            operation = "create checkout"
        )

        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertTrue(problem.detail.contains("plain failure"))
        assertFalse(problem.isRetriable)
    }
}
