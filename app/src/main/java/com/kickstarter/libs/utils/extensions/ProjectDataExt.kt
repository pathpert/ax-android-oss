package com.kickstarter.libs.utils.extensions

import android.content.SharedPreferences
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.ui.data.ProjectData
import java.net.CookieManager

/**
 * Utility and helper extensions for ProjectData.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
fun ProjectData.storeCurrentCookieRefTag(cookieManager: CookieManager, sharedPreferences: SharedPreferences): ProjectData {
    return this
        .toBuilder()
        .refTagFromCookie(RefTagUtils.storedCookieRefTagForProject(this.project(), cookieManager, sharedPreferences))
        .build()
}
