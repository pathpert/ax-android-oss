package com.kickstarter.services

import android.content.Context
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.isTrue

internal object DiscoveryParamsFilterFormatter {

    fun filterString(
        params: DiscoveryParams,
        context: Context,
        ksString: KSString,
        isToolbar: Boolean = false,
        isParentFilter: Boolean = false
    ): String {
        return if (params.staffPicks().isTrue()) {
            context.getString(R.string.Projects_We_Love)
        } else if (params.starred() != null && params.starred() == 1) {
            context.getString(R.string.Saved)
        } else if (params.backed() != null && params.backed() == 1) {
            context.getString(R.string.discovery_backing)
        } else if (params.social() != null && params.social() == 1) {
            if (isToolbar) context.getString(R.string.Following) else context.getString(R.string.Backed_by_people_you_follow)
        } else if (params.category() != null) {
            if (params.category()?.isRoot == true && !isParentFilter && !isToolbar) ksString.format(
                context.getString(
                    R.string.All_category_name_Projects
                ),
                "category_name", params.category()?.name()
            ) else params.category()?.name() ?: ""
        } else if (params.location() != null) {
            params.location()?.displayableName() ?: ""
        } else if (params.recommended().isTrue()) {
            if (isToolbar) context.getString(R.string.Recommended) else context.getString(R.string.discovery_recommended_for_you)
        } else {
            context.getString(R.string.All_Projects)
        }
    }
}
