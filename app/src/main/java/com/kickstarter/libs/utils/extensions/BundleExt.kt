package com.kickstarter.libs.utils.extensions

import android.os.Bundle

/**
 * Utility and helper extensions for Bundle.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
fun Bundle?.maybeGetBundle(key: String): Bundle? {
    return this?.getBundle(key)
}
