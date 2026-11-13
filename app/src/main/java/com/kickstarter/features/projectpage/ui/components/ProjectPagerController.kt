package com.kickstarter.features.projectpage.ui.components

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kickstarter.R
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.ui.adapters.ProjectPagerAdapter
import com.kickstarter.viewmodels.projectpage.PagerTabConfig

/**
 * Encapsulates dynamic tab configuration and ViewPager2 attachment for the project detail tabs.
 */
class ProjectPagerController(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val lifecycle: Lifecycle,
    private val viewPager: ViewPager2,
    private val tabLayout: TabLayout,
    private val onTabSelected: (position: Int) -> Unit
) {
    val pagerAdapterList = mutableListOf(
        ProjectPagerTabs.OVERVIEW,
        ProjectPagerTabs.CAMPAIGN,
        ProjectPagerTabs.FAQS,
        ProjectPagerTabs.RISKS
    )

    fun configureTabs(updateTabs: List<PagerTabConfig>) {
        updateTabs.forEach { tabConfig ->
            if (tabConfig.isActive) {
                if (!pagerAdapterList.contains(tabConfig.tab)) {
                    pagerAdapterList.add(tabConfig.tab)
                }
            }
        }
    }

    fun configurePager(pagerList: List<ProjectPagerTabs> = pagerAdapterList) {
        viewPager.adapter = ProjectPagerAdapter(fragmentManager, pagerList, lifecycle)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position, pagerList)
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    onTabSelected(it.position)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
    }

    fun getTabTitle(position: Int, pagerList: List<ProjectPagerTabs>): String =
        when (pagerList[position]) {
            ProjectPagerTabs.OVERVIEW -> context.getString(R.string.Overview)
            ProjectPagerTabs.CAMPAIGN -> context.getString(R.string.Campaign)
            ProjectPagerTabs.FAQS -> context.getString(R.string.Faq)
            ProjectPagerTabs.RISKS -> context.getString(R.string.Risks)
            ProjectPagerTabs.USE_OF_AI -> context.getString(R.string.Use_of_ai)
            ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT -> context.getString(R.string.Environmental_commitments)
        }
}
