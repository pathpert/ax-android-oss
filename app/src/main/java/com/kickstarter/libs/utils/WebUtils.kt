package com.kickstarter.libs.utils

import com.kickstarter.libs.Build

/**
 * Utility and helper extensions for WebUtils.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
object WebUtils {
    fun userAgent(build: Build): String {
        return StringBuilder()
            .append("Kickstarter Android Mobile Variant/")
            .append(build.variant())
            .append(" Code/")
            .append(build.versionCode())
            .append(" Version/")
            .append(build.versionName())
            .toString()
    }
}
