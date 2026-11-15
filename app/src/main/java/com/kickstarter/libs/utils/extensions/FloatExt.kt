package com.kickstarter.libs.utils.extensions

/**
 * Utility and helper extensions for Float.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
fun Float.compareDescending(other: Float): Int {
    return other.compareTo(this)
}
