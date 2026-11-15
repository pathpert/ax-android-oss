package com.kickstarter.libs.utils.extensions

import android.view.View
import androidx.constraintlayout.widget.Group

/**
 * Utility and helper extensions for Group.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
fun Group.setAllOnClickListener(listener: (View) -> Unit) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}
