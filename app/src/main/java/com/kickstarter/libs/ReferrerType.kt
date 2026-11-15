package com.kickstarter.libs

import com.kickstarter.R

/**
 * Describes the source of the referrer for project discovery and analytics.
 *
 * Negative constraints:
 * - Do not pass arbitrary string constants; strictly consume [ReferrerType] instances.
 */
enum class ReferrerType(val referrerType: String, val referrerColorId: Int) {
    CUSTOM("custom", R.color.kds_create_300),
    EXTERNAL("external", R.color.kds_create_700),
    KICKSTARTER("kickstarter", R.color.kds_create_700)
}
