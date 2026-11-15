package com.kickstarter.ui.viewholders

import androidx.viewbinding.ViewBinding

/**
 * UI binding component for EmptyViewHolder.
 *
 * Negative constraints:
 * - Do not mutate domain models directly within UI adapters or view holders.
 */
class EmptyViewHolder(binding: ViewBinding) : KSViewHolder(binding.root) {
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
    }

    override fun onBind() {}
}
