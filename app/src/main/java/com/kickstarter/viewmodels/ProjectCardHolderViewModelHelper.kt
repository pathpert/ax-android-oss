package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Category
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.Editorial

internal object ProjectCardHolderViewModelHelper {

    fun shouldShowLocationTag(params: DiscoveryParams?): Boolean {
        if (params == null) return false
        return params.tagId() != null && params.tagId() == Editorial.LIGHTS_ON.tagId
    }

    fun areParamsAllOrSameCategoryAsProject(categoryPair: Pair<Category, Category>): Boolean {
        return categoryPair.first.isNotNull() && categoryPair.first?.id() == categoryPair.second.id()
    }
}
