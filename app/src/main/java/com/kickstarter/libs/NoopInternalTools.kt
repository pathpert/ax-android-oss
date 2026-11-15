package com.kickstarter.libs

import android.app.Activity

/**
 * Library utility component for NoopInternalTools.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
class NoopInternalTools : InternalToolsType {
    override fun maybeStartInternalToolsActivity(activity: Activity) {}

    override fun basicAuthorizationHeader(): String? {
        return null
    }
}
