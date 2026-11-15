package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.DiscoveryOnboardingViewBinding

/**
 * UI binding component for DiscoveryOnboardingViewHolder.
 *
 * Negative constraints:
 * - Do not mutate domain models directly within UI adapters or view holders.
 */
class DiscoveryOnboardingViewHolder(
    private val binding: DiscoveryOnboardingViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {

    interface Delegate {
        fun discoveryOnboardingViewHolderLoginToutClick(viewHolder: DiscoveryOnboardingViewHolder?)
    }
    init {
        binding.loginToutButton.setOnClickListener {
            loginToutClick()
        }
    }

    override fun bindData(data: Any?) {
    }

    private fun loginToutClick() {
        delegate.discoveryOnboardingViewHolderLoginToutClick(this)
    }
}
