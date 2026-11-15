package com.kickstarter.ui.itemdecorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * UI component for RewardCardItemDecoration.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
class RewardCardItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.apply {
            bottom = margin
        }
    }
}
