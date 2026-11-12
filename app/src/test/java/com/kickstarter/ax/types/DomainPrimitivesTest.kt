package com.kickstarter.ax.types

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainPrimitivesTest {

    @Test
    fun `MinorCurrencyAmount converts major double accurately and rejects negative or NaN`() {
        val valid = MinorCurrencyAmount.fromMajorDouble(29.99, fieldName = "pledgeAmount")
        assertTrue(valid is DomainResult.Success)
        assertEquals(2999L, (valid as DomainResult.Success).value.minorUnits)
        assertEquals(29.99, valid.value.toLegacyMajorDouble(), 0.0001)

        val negative = MinorCurrencyAmount.fromMinorUnits(-1L, fieldName = "pledgeAmount")
        assertTrue(negative is DomainResult.Failure)
        val problem = (negative as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertFalse(problem.isRetriable)
        assertEquals("pledgeAmount", problem.invalidParams.single().name)

        val nan = MinorCurrencyAmount.fromMajorDouble(Double.NaN, fieldName = "pledgeAmount")
        assertTrue(nan is DomainResult.Failure)
    }

    @Test
    fun `NonBlankId trims valid input and rejects blank strings`() {
        val ok = NonBlankId.create("  backing-123  ", fieldName = "backingId")
        assertTrue(ok is DomainResult.Success)
        assertEquals("backing-123", (ok as DomainResult.Success).value.value)

        val blank = NonBlankId.create("   ", fieldName = "backingId")
        assertTrue(blank is DomainResult.Failure)
        val problem = (blank as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals("backingId", problem.invalidParams.single().name)
    }

    @Test
    fun `RateLimitBackoff parses case-insensitive headers`() {
        val backoff = RateLimitBackoff.fromHeaders(
            listOf(
                "x-ratelimit-limit" to "100",
                "X-RateLimit-Remaining" to "0",
                "retry-after" to "45"
            )
        )

        assertEquals(100, backoff.limit)
        assertEquals(0, backoff.remaining)
        assertEquals(45L, backoff.retryAfterSeconds)
    }
}
