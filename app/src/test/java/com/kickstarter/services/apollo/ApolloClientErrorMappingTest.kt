package com.kickstarter.services.apollo

import com.apollographql.apollo3.api.http.HttpHeader
import com.apollographql.apollo3.exception.ApolloHttpException
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unit tests verifying that Apollo client error mapping preserves RFC 9457 structured
 * semantics (suggestedAction, isRetriable, status) across all read and mutation failure classes.
 *
 * Negative constraints:
 * - Do not allow unclassified exceptions to cross Apollo client boundaries.
 * - Do not drop suggestedAction remediation instructions.
 */
class ApolloClientErrorMappingTest {

    @Test
    fun `network transport error maps to retriable network problem across read operations`() {
        val operations = listOf(
            "fetch project",
            "fetch user privacy",
            "fetch shipping rules",
            "fetch project add-ons",
            "fetch comments",
            "fetch backing",
            "fetch discovery",
            "watch project",
            "unwatch project"
        )

        for (op in operations) {
            val networkException = ApolloNetworkException("Connection reset by peer", IOException("reset"))
            val problem = ProblemDetailsMapper.fromThrowable(networkException, op)
            val exception = ProblemException(problem, networkException)

            assertEquals(ProblemDetails.TYPE_NETWORK, exception.problem.type)
            assertTrue(exception.problem.isRetriable)
            assertTrue(exception.problem.suggestedAction.isNotBlank())
            assertTrue(exception.problem.detail.contains(op))
        }
    }

    @Test
    fun `socket timeout maps to retriable timeout problem across operations`() {
        val timeoutException = SocketTimeoutException("Read timed out")
        val problem = ProblemDetailsMapper.fromThrowable(timeoutException, "fetch backing")
        val exception = ProblemException(problem, timeoutException)

        assertEquals(ProblemDetails.TYPE_TIMEOUT, exception.problem.type)
        assertEquals(504, exception.problem.status)
        assertTrue(exception.problem.isRetriable)
        assertTrue(exception.problem.suggestedAction.isNotBlank())
        assertTrue(exception.problem.detail.contains("fetch backing"))
    }

    @Test
    fun `unknown host maps to retriable network problem`() {
        val hostException = UnknownHostException("api.kickstarter.com")
        val problem = ProblemDetailsMapper.fromThrowable(hostException, "fetch discovery")
        val exception = ProblemException(problem, hostException)

        assertEquals(ProblemDetails.TYPE_NETWORK, exception.problem.type)
        assertTrue(exception.problem.isRetriable)
        assertTrue(exception.problem.suggestedAction.isNotBlank())
    }

    @Test
    fun `rate limit 429 maps to retriable rate-limited problem with backoff`() {
        val http429 = ApolloHttpException(
            statusCode = 429,
            headers = listOf(
                HttpHeader("Retry-After", "15"),
                HttpHeader("X-RateLimit-Remaining", "0")
            ),
            body = null,
            message = "Rate limit exceeded"
        )
        val problem = ProblemDetailsMapper.fromThrowable(http429, "watch project")
        val exception = ProblemException(problem, http429)

        assertEquals(ProblemDetails.TYPE_RATE_LIMITED, exception.problem.type)
        assertEquals(429, exception.problem.status)
        assertEquals(15L, exception.problem.retryAfterSeconds)
        assertTrue(exception.problem.isRetriable)
        assertTrue(exception.problem.suggestedAction.isNotBlank())
    }

    @Test
    fun `graphql domain message maps to non-blank suggestedAction`() {
        val rawMessage = "Project has ended and can no longer be backed or watched."
        val problem = ProblemDetailsMapper.fromDomainMessage(rawMessage, "watch project")
        val exception = ProblemException(problem)

        assertNotNull(exception.problem)
        assertTrue(exception.problem.detail.contains(rawMessage))
        assertEquals("Failed to watch project: $rawMessage", exception.problem.detail)
        assertTrue(exception.problem.suggestedAction.isNotBlank())
        assertFalse(exception.problem.isRetriable)
    }

    @Test
    fun `graphql validation message correctly flags non-retriable`() {
        val rawMessage = "Invalid cursor provided for pagination."
        val problem = ProblemDetailsMapper.fromDomainMessage(rawMessage, "fetch comments")

        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertFalse(problem.isRetriable)
        assertTrue(problem.suggestedAction.isNotBlank())
    }

    @Test
    fun `problem exception unwraps without loss of remediation metadata`() {
        val originalProblem = ProblemDetails(
            type = ProblemDetails.TYPE_FORBIDDEN,
            title = "Forbidden",
            status = 403,
            detail = "User is not allowed to perform action.",
            suggestedAction = "Check user permissions before retrying.",
            isRetriable = false
        )
        val exception = ProblemException(originalProblem)
        val mapped = ProblemDetailsMapper.fromThrowable(exception, "unwatch project")

        assertEquals(originalProblem, mapped)
        assertEquals(originalProblem.suggestedAction, mapped.suggestedAction)
        assertEquals(originalProblem.isRetriable, mapped.isRetriable)
    }
}
