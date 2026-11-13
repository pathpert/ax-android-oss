package com.kickstarter.libs.graphql
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.text.ParseException

class DateTimeAdapter : Adapter<DateTime> {

    override fun fromJson(
        reader: JsonReader,
        customScalarAdapters: CustomScalarAdapters
    ): DateTime {
        try {
            val seconds = reader.nextString()?.toLongOrNull() ?: 0L
            return DateTime(seconds * 1000L, DateTimeZone.UTC)
        } catch (exception: ParseException) {
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
