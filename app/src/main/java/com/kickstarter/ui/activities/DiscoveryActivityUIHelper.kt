package com.kickstarter.ui.activities

import android.graphics.drawable.Animatable
import com.google.android.material.tabs.TabLayout
import com.kickstarter.databinding.DiscoveryLayoutBinding
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.viewmodels.DiscoveryViewModel

/**
 * UI helper for [DiscoveryActivity].
 */
internal object DiscoveryActivityUIHelper {

    fun createFragments(pages: Int): List<DiscoveryFragment> {
        val fragments: MutableList<DiscoveryFragment> = ArrayList(pages)
        for (position in 0..pages) {
            fragments.add(DiscoveryFragment.newInstance(position))
        }
        return fragments
    }

    fun setupTabSelectedListener(
        binding: DiscoveryLayoutBinding,
        pagerAdapter: DiscoveryPagerAdapter,
        viewModel: DiscoveryViewModel.DiscoveryViewModel
    ) {
        binding.discoveryTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.sortClicked(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                pagerAdapter.scrollToTop(tab.position)
                viewModel.sortClicked(tab.position)
            }
        })
    }

    fun updateDrawerMenuIcon(binding: DiscoveryLayoutBinding, drawerMenuIcon: Int) {
        binding.discoveryToolbar.menuButton.setImageResource(drawerMenuIcon)
        if (binding.discoveryToolbar.menuButton.drawable is Animatable) {
            val menuDrawable = binding.discoveryToolbar.menuButton.drawable as Animatable
            menuDrawable.start()
        }
    }
}
