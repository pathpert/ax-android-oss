package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.BufferedSourceJsonReader
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies GraphQL custom scalar date adapters carry structured [ProblemDetails] through the
 * parsing boundary instead of flattening parse failures into opaque string exceptions (Rule 4).
 */
class GraphQLScalarAdapterTest {

    private fun reader(json: String): BufferedSourceJsonReader =
        BufferedSourceJsonReader(Buffer().writeUtf8(json))

    @Test
    fun `Iso8601DateTimeAdapter throws ProblemException carrying structured ProblemDetails`() {
        val thrown = try {
            Iso8601DateTimeAdapter().fromJson(reader("\"definitely-not-a-timestamp\""), CustomScalarAdapters.Empty)
            null
        } catch (e: ProblemException) {
            e
        }

        assertNotNull("Expected ProblemException, not a flattened JsonDataException", thrown)
        val problem = thrown!!.problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals(422, problem.status)
        assertTrue(problem.detail.contains("ISO-8601 DateTime"))
        assertTrue(problem.suggestedAction.isNotBlank())
        assertFalse(problem.isRetriable)
        assertNotNull("Original parse cause must be preserved", thrown.cause)
    }

    @Test
    fun `DateAdapter throws ProblemException carrying structured ProblemDetails`() {
        val thrown = try {
            DateAdapter().fromJson(reader("\"not-a-date\""), CustomScalarAdapters.Empty)
            null
        } catch (e: ProblemException) {
            e
        }

        assertNotNull("Expected ProblemException, not a flattened JsonDataException", thrown)
        val problem = thrown!!.problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertEquals(422, problem.status)
        assertTrue(problem.detail.contains("Date scalar"))
        assertTrue(problem.suggestedAction.isNotBlank())
        assertFalse(problem.isRetriable)
        assertNotNull("Original parse cause must be preserved", thrown.cause)
    }

    @Test
    fun `ProblemDetailsMapper restores structure from adapter ProblemExceptions`() {
        val isoProblem = try {
            Iso8601DateTimeAdapter().fromJson(reader("\"bad\""), CustomScalarAdapters.Empty)
            null
        } catch (e: ProblemException) {
            ProblemDetailsMapper.fromThrowable(e, operation = "parse scalar")
        }

        assertEquals(ProblemDetails.TYPE_VALIDATION, isoProblem?.type)
        assertTrue(isoProblem?.suggestedAction?.isNotBlank() == true)
    }

    @Test
    fun `adapters still parse well-formed scalars`() {
        val dateTime = Iso8601DateTimeAdapter().fromJson(reader("\"2026-11-16T12:00:00Z\""), CustomScalarAdapters.Empty)
        assertEquals(2026, dateTime.year)

        val date = DateAdapter().fromJson(reader("\"2026-11-16\""), CustomScalarAdapters.Empty)
        val formatted = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
        assertEquals("2026-11-16", formatted)
    }
}
