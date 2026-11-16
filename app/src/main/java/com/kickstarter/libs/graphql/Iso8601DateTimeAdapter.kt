package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import org.joda.time.DateTime

/**
 * Library utility component for Iso8601DateTimeAdapter.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 * - Do not flatten parse failures into plain string exceptions; throw [ProblemException] so
 *   [ProblemDetails] structure survives the parsing boundary.
 */
class Iso8601DateTimeAdapter : Adapter<DateTime> {

    override fun fromJson(
        reader: JsonReader,
        customScalarAdapters: CustomScalarAdapters
    ): DateTime {
        try {
            return DateTime.parse(reader.nextString())
        } catch (exception: Exception) {
            val problem = ProblemDetailsMapper.fromDomainMessage("Failed to parse ISO-8601 DateTime scalar: ${exception.message}", "parse iso8601 datetime scalar", status = 422)
            throw ProblemException(problem, exception)
        }
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: DateTime
    ) {
        writer.value(value.toString())
    }
}
