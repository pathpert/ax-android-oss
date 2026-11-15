package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.exception.JsonDataException
import org.joda.time.DateTime
import java.text.ParseException

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
            throw JsonDataException("Failed to parse ISO-8601 DateTime scalar from JSON value: ${exception.message}")
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
