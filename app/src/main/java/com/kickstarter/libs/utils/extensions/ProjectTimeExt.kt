@file:JvmMultifileClass
@file:JvmName("ProjectExt")

package com.kickstarter.libs.utils.extensions

import android.content.Context
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.models.Project
import org.joda.time.DateTime
import org.joda.time.Duration

/**
 * Returns time until project reaches deadline along with the unit,
 * e.g. `25 minutes`, `8 days`.
 */
fun Project.deadlineCountdown(context: Context): String {
    return StringBuilder().append(this.deadlineCountdownValue())
        .append(" ")
        .append(this.deadlineCountdownUnit(context))
        .toString()
}

/**
 * Returns unit of time remaining in a readable string, e.g. `days to go`, `hours to go`.
 *
 * @param context an Android context.
 * @param ksString the KSString.
 * @return the resulting String.
 */
fun Project.deadlineCountdownDetail(context: Context, ksString: KSString): String =
    ksString.format(
        context.getString(R.string.discovery_baseball_card_time_left_to_go),
        "time_left",
        this.deadlineCountdownUnit(context),
    )

/**
 * Returns the most appropriate unit for the time remaining until the project
 * reaches its deadline.
 *
 * @param context an Android context.
 * @return the String unit.
 */
fun Project.deadlineCountdownUnit(context: Context): String {
    val seconds = this.timeInSecondsUntilDeadline()

    return when {
        seconds <= 1L -> context.getString(R.string.discovery_baseball_card_deadline_units_secs)
        seconds <= 120L -> context.getString(R.string.discovery_baseball_card_deadline_units_secs)
        seconds <= 120L * 60L -> context.getString(R.string.discovery_baseball_card_deadline_units_mins)
        seconds <= 72L * 60L * 60L -> context.getString(R.string.discovery_baseball_card_deadline_units_hours)
        else -> context.getString(R.string.discovery_baseball_card_deadline_units_days)
    }
}

/**
 * Returns time remaining until project reaches deadline in either seconds,
 * minutes, hours or days. A time unit is chosen such that the number is
 * readable, e.g. 5 minutes would be preferred to 300 seconds.
 *
 * @return the Integer time remaining.
 */
fun Project.deadlineCountdownValue(): Int {
    val seconds = this.timeInSecondsUntilDeadline()

    return when {
        seconds <= 120L -> seconds.toInt() // seconds
        seconds <= 120L * 60L -> (seconds / 60L).toInt() // minutes
        seconds < 72L * 60L * 60L -> (seconds / 3600L).toInt() // hours
        else -> (seconds / 86400L).toInt() // days
    }
}

/**
 * Returns time between project launch and deadline in seconds.
 */
fun Project.timeInSecondsOfDuration(): Long = Duration(this.launchedAt(), this.deadline()).standardSeconds

/**
 * Returns time between project launch and deadline in days.
 */
fun Project.timeInDaysOfDuration(): Long = Duration(this.launchedAt(), this.deadline()).standardDays

/**
 * Returns time until project reaches deadline in seconds, or 0 if the
 * project has already finished.
 */
fun Project.timeInSecondsUntilDeadline(): Long =
    0L.coerceAtLeast(Duration(DateTime(), this.deadline()).standardSeconds)
