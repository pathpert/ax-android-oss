package com.kickstarter.ui.activities

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.kickstarter.databinding.MessagesLayoutBinding
import com.kickstarter.viewmodels.MessagesViewModel
import kotlin.math.abs

/**
 * Window insets and app bar offset helper for [MessagesActivity].
 */
internal object MessagesActivityInsetsHelper {

    fun setupWindowInsets(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
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

    fun setAppBarOffsetChangedListener(
        binding: MessagesLayoutBinding,
        viewModel: MessagesViewModel.MessagesViewModel
    ) {
        binding.messagesAppBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { layout: AppBarLayout, offset: Int ->
                binding.messagesToolbar.messagesProjectNameCollapsedTextView.alpha =
                    (abs(offset).toFloat() / layout.totalScrollRange.toFloat())
                viewModel.inputs.appBarTotalScrollRange(layout.totalScrollRange)
                viewModel.inputs.appBarOffset(offset)
            }
        )
    }
}
