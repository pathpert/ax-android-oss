package com.kickstarter.ui.views

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * UI component rendering CenterSpan.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
class CenterSpan : MetricAffectingSpan() {
    internal var ratio = 0.5f

    override fun updateDrawState(paint: TextPaint) {
        paint.baselineShift += (paint.ascent() * ratio).toInt()
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.baselineShift += (paint.ascent() * ratio).toInt()
    }
}
