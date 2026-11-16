package com.kickstarter.ax.errors

import com.kickstarter.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProblemDetailsCopyMapperTest {

    @Test
    fun `messageRes selects connectivity copy for network and timeout problems`() {
        val network = ProblemDetailsMapper.fromThrowable(java.io.IOException("offline"), operation = "load comments")
        val timeout = ProblemDetails(
            type = ProblemDetails.TYPE_TIMEOUT,
            title = "Request Timed Out",
            status = 504,
            detail = "Timed out while trying to load comments.",
            suggestedAction = "Wait briefly and retry.",
            isRetriable = true
        )

        assertEquals(R.string.social_error_no_internet_connection, ProblemDetailsCopyMapper.messageRes(network))
        assertEquals(R.string.social_error_no_internet_connection, ProblemDetailsCopyMapper.messageRes(timeout))
    }

    @Test
    fun `messageRes falls back to generic localized copy for non-connectivity problems`() {
        val validation = ProblemDetailsMapper.fromDomainMessage("Invalid amount", operation = "create checkout")
        val server = ProblemDetailsMapper.fromHttpStatus(status = 502, operation = "create checkout")

        assertEquals(R.string.general_error_something_wrong, ProblemDetailsCopyMapper.messageRes(validation))
        assertEquals(R.string.general_error_something_wrong, ProblemDetailsCopyMapper.messageRes(server))
    }

    @Test
    fun `displayMessage combines detail with suggestedAction and normalizes punctuation`() {
        val problem = ProblemDetails(
            type = ProblemDetails.TYPE_NETWORK,
            title = "Network Unavailable",
            detail = "Unable to reach Kickstarter servers to load comments.",
            suggestedAction = "Check network connectivity and retry when online.",
            isRetriable = true
        )

        val message = ProblemDetailsCopyMapper.displayMessage(problem)

        assertEquals(
            "Unable to reach Kickstarter servers to load comments. Check network connectivity and retry when online.",
            message
        )
    }

    @Test
    fun `displayMessageFor maps throwables and returns null when absent`() {
        val mapped = ProblemDetailsCopyMapper.displayMessageFor(
            RuntimeException("boom"),
            operation = "present payment sheet"
        )

        assertTrue(mapped?.contains("boom") == true)
        assertTrue(mapped?.contains(".") == true)
        assertNull(ProblemDetailsCopyMapper.displayMessageFor(null, operation = "present payment sheet"))
    }
}
