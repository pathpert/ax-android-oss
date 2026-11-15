package com.kickstarter.ui.viewholders.discoverydrawer

import com.kickstarter.databinding.DiscoveryDrawerHeaderBinding
import com.kickstarter.ui.viewholders.KSViewHolder

/**
 * UI binding component for HeaderViewHolder.
 *
 * Negative constraints:
 * - Do not mutate domain models directly within UI adapters or view holders.
 */
class HeaderViewHolder(private val binding: DiscoveryDrawerHeaderBinding) : KSViewHolder(binding.root) {
    private var item: Int = 0

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        this.item = requireNotNull(data as? Int) { Int::class.java.toString() + " required to be non-null." }
        binding.discoveryDrawerHeaderTitle.setText(item)
    }
}
