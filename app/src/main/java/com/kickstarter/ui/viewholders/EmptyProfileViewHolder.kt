package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ProfileEmptyStateViewBinding

/**
 * UI binding component for EmptyProfileViewHolder.
 *
 * Negative constraints:
 * - Do not mutate domain models directly within UI adapters or view holders.
 */
class EmptyProfileViewHolder(
    private val binding: ProfileEmptyStateViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    interface Delegate {
        fun emptyProfileViewHolderExploreProjectsClicked(viewHolder: EmptyProfileViewHolder)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
    }

    override fun onBind() {
        binding.exploreProjectsButton.setOnClickListener {
            exploreProjectsClicked()
        }
    }

    private fun exploreProjectsClicked() {
        delegate.emptyProfileViewHolderExploreProjectsClicked(this)
    }
}
