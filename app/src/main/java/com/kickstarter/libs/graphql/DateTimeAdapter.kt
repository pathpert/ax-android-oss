package com.kickstarter.libs.graphql

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.text.ParseException
import timber.log.Timber

/**
 * Library utility component for DateTimeAdapter.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
class DateTimeAdapter : Adapter<DateTime> {

    override fun fromJson(
        reader: JsonReader,
        customScalarAdapters: CustomScalarAdapters
    ): DateTime {
        try {
            val seconds = reader.nextString()?.toLongOrNull() ?: 0L
            return DateTime(seconds * 1000L, DateTimeZone.UTC)
        } catch (exception: ParseException) {
            val problem = ProblemDetailsMapper.fromDomainMessage("Failed to parse DateTime scalar: ${exception.message}", "parse datetime scalar", status = 422)
            Timber.w(ProblemException(problem, exception), "DateTimeAdapter: %s", problem.detail)
            return DateTime(0L, DateTimeZone.UTC)
        }
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: DateTime
    ) {
        writer.value(value.millis / 1000L)
    }
}
