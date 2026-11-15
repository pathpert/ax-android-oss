package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.exception.JsonDataException
import com.kickstarter.ax.errors.ProblemDetailsMapper
import org.joda.time.DateTime

/**
 * Library utility component for Iso8601DateTimeAdapter.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
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
            throw JsonDataException("${problem.title}: ${problem.detail} (${problem.suggestedAction})")
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
