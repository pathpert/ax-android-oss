package com.kickstarter.features.projectpage.ui.components

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.viewmodels.projectpage.PagerTabConfig
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric

class ProjectPagerControllerTest : KSRobolectricTestCase() {

    private lateinit var activity: FragmentActivity
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var controller: ProjectPagerController
    private var selectedPosition = -1

    @Before
    override fun setUp() {
        super.setUp()
        activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        viewPager = ViewPager2(activity)
        tabLayout = TabLayout(activity)
        controller = ProjectPagerController(
            context = activity,
            fragmentManager = activity.supportFragmentManager,
            lifecycle = activity.lifecycle,
            viewPager = viewPager,
            tabLayout = tabLayout,
            onTabSelected = { selectedPosition = it }
        )
    }

    @Test
    fun testDefaultPagerAdapterList() {
        assertEquals(4, controller.pagerAdapterList.size)
        assertEquals(ProjectPagerTabs.OVERVIEW, controller.pagerAdapterList[0])
        assertEquals(ProjectPagerTabs.CAMPAIGN, controller.pagerAdapterList[1])
        assertEquals(ProjectPagerTabs.FAQS, controller.pagerAdapterList[2])
        assertEquals(ProjectPagerTabs.RISKS, controller.pagerAdapterList[3])
    }

    @Test
    fun testConfigureTabs_addsActiveTabs() {
        val configs = listOf(
            PagerTabConfig(ProjectPagerTabs.USE_OF_AI, isActive = true),
            PagerTabConfig(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT, isActive = false)
        )

        controller.configureTabs(configs)

        assertEquals(5, controller.pagerAdapterList.size)
        assertTrue(controller.pagerAdapterList.contains(ProjectPagerTabs.USE_OF_AI))
        assertFalse(controller.pagerAdapterList.contains(ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT))
    }

    @Test
    fun testConfigureTabs_doesNotDuplicateExistingTabs() {
        val configs = listOf(
            PagerTabConfig(ProjectPagerTabs.OVERVIEW, isActive = true),
            PagerTabConfig(ProjectPagerTabs.CAMPAIGN, isActive = true)
        )

        controller.configureTabs(configs)

        assertEquals(4, controller.pagerAdapterList.size)
    }

    @Test
    fun testGetTabTitle() {
        val allTabs = listOf(
            ProjectPagerTabs.OVERVIEW,
            ProjectPagerTabs.CAMPAIGN,
            ProjectPagerTabs.FAQS,
            ProjectPagerTabs.RISKS,
            ProjectPagerTabs.USE_OF_AI,
            ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT
        )

        assertEquals(activity.getString(R.string.Overview), controller.getTabTitle(0, allTabs))
        assertEquals(activity.getString(R.string.Campaign), controller.getTabTitle(1, allTabs))
        assertEquals(activity.getString(R.string.Faq), controller.getTabTitle(2, allTabs))
        assertEquals(activity.getString(R.string.Risks), controller.getTabTitle(3, allTabs))
        assertEquals(activity.getString(R.string.Use_of_ai), controller.getTabTitle(4, allTabs))
        assertEquals(activity.getString(R.string.Environmental_commitments), controller.getTabTitle(5, allTabs))
    }
}
