package com.kickstarter.libs.utils

import androidx.work.Constraints
import androidx.work.NetworkType

/**
 * Utility and helper extensions for WorkUtils.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
object WorkUtils {
    val baseConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun uniqueWorkName(tag: String) = tag + System.currentTimeMillis()
}
