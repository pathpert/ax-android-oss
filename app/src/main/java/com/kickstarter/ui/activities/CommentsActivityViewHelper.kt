package com.kickstarter.ui.activities

import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.kickstarter.databinding.ActivityCommentsLayoutBinding
import com.kickstarter.libs.utils.extensions.toVisibility

/**
 * View and insets helper for [CommentsActivity].
 */
internal object CommentsActivityViewHelper {

    fun setupWindowInsets(binding: ActivityCommentsLayoutBinding) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = insets.top
            }

            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = imeInsets.bottom)

            WindowInsetsCompat.CONSUMED
        }
    }

    fun setEmptyState(binding: ActivityCommentsLayoutBinding, visibility: Boolean) {
        binding.commentsSwipeRefreshLayout.visibility = (!visibility).toVisibility()
        binding.noComments.visibility = visibility.toVisibility()
    }
}
