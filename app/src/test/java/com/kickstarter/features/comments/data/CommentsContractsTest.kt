package com.kickstarter.features.comments.data

import com.kickstarter.ax.errors.ProblemDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CommentsContractsTest {

    @Test
    fun `test CommentBody accepts valid comment strings`() {
        val valid = CommentBody("Great project, keep up the amazing work!")
        assertEquals("Great project, keep up the amazing work!", valid.value)
    }

    @Test
    fun `test CommentBody rejects blank or empty strings`() {
        try {
            CommentBody("   ")
            fail("Expected IllegalArgumentException for blank body")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Negative Constraint Violated") == true)
        }
    }

    @Test
    fun `test CommentBody rejects strings exceeding 9000 characters`() {
        val hugeString = "a".repeat(9001)
        try {
            CommentBody(hugeString)
            fail("Expected IllegalArgumentException for oversized body")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Negative Constraint Violated") == true)
        }
    }

    @Test
    fun `test CommentsUIState Error wraps ProblemDetails`() {
        val problem = ProblemDetails(
            type = ProblemDetails.TYPE_NETWORK,
            title = "Network Timeout",
            status = 504,
            detail = "Failed to reach server while loading comments",
            suggestedAction = "Check your connection and retry",
            isRetriable = true
        )
        val state: CommentsUIState = CommentsUIState.Error(problem)
        assertTrue(state is CommentsUIState.Error)
        val errorState = state as CommentsUIState.Error
        assertEquals(504, errorState.problem.status)
        assertTrue(errorState.problem.isRetriable)
        assertNotNull(errorState.throwable)
    }
}
