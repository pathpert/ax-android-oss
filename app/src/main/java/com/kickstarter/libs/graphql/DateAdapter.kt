package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.exception.JsonDataException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Library utility component for DateAdapter.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
class DateAdapter : Adapter<Date> {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Date {
        return try {
            reader.nextString()?.let { DATE_FORMAT.parse(it) } ?: Date()
        } catch (exception: ParseException) {
            throw JsonDataException("Failed to parse Date scalar from JSON value: ${exception.message}")
        }
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Date) {
        writer.value(DATE_FORMAT.format(value))
    }
}
