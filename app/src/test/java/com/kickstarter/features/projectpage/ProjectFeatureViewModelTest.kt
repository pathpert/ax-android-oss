package com.kickstarter.features.projectpage

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.projectpage.data.PagerTabConfig
import com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.IntentKey
import io.reactivex.observers.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Test

class ProjectFeatureViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: ProjectFeatureViewModel
    private lateinit var updateTabs: TestObserver<List<PagerTabConfig>>
    private lateinit var pledgeActionButtonText: TestObserver<Int>
    private lateinit var reloadProjectContainerIsGone: TestObserver<Boolean>
    private lateinit var heartDrawableId: TestObserver<Int>
    private lateinit var playButtonIsVisible: TestObserver<Boolean>

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = ProjectFeatureViewModel(environment())
        updateTabs = viewModel.updateTabs().test()
        pledgeActionButtonText = viewModel.pledgeActionButtonText().test()
        reloadProjectContainerIsGone = viewModel.reloadProjectContainerIsGone().test()
        heartDrawableId = viewModel.heartDrawableId().test()
        playButtonIsVisible = viewModel.playButtonIsVisible().test()
    }

    @After
    override fun tearDown() {
        updateTabs.dispose()
        pledgeActionButtonText.dispose()
        reloadProjectContainerIsGone.dispose()
        heartDrawableId.dispose()
        playButtonIsVisible.dispose()
        super.tearDown()
    }

    @Test
    fun testConfigureWithProject_emitsTabsAndPledgeButton() {
        val project = ProjectFactory.project()
        val intent = Intent().putExtra(IntentKey.PROJECT, project)

        viewModel.configureWith(intent)

        updateTabs.assertValueCount(1)
        assertEquals(2, updateTabs.values().first().size)
        pledgeActionButtonText.assertValueCount(1)
        reloadProjectContainerIsGone.assertValueCount(1)
        assertTrue(reloadProjectContainerIsGone.values().first())
    }

    @Test
    fun testPlayButtonVisibility_whenProjectHasVideo() {
        val project = ProjectFactory.project().toBuilder().video(null).build()
        val intent = Intent().putExtra(IntentKey.PROJECT, project)

        viewModel.configureWith(intent)

        assertTrue(playButtonIsVisible.values().isNotEmpty())
        assertFalse(playButtonIsVisible.values().first())
    }

    @Test
    fun testHeartDrawable_togglesWithProjectStarredState() {
        val starredProject = ProjectFactory.project().toBuilder().isStarred(true).build()
        val intent = Intent().putExtra(IntentKey.PROJECT, starredProject)

        viewModel.configureWith(intent)

        assertTrue(heartDrawableId.values().isNotEmpty())
        assertEquals(R.drawable.icon__heart, heartDrawableId.values().first())
    }
}
