package com.kickstarter.libs.utils.extensions

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility and helper extensions for Double.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
fun Double.round(scale: Int = 2): Double {
    return BigDecimal(this).setScale(scale, RoundingMode.HALF_UP).toDouble()
}
