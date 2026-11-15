package com.kickstarter.libs

import android.app.Activity

/**
 * Library utility component for InternalToolsType.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
interface InternalToolsType {
    fun maybeStartInternalToolsActivity(activity: Activity)
    fun basicAuthorizationHeader(): String?
}
