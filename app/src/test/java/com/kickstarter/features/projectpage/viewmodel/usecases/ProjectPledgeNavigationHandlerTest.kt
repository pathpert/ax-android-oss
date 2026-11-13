package com.kickstarter.features.projectpage.viewmodel.usecases

import android.app.Activity
import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.projectpage.viewmodel.ProjectPageStateHolder
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ActivityResult
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class ProjectPledgeNavigationHandlerTest : KSRobolectricTestCase() {

    private class ConcreteProjectPageStateHolder(env: Environment) : ProjectPageStateHolder(env)

    private lateinit var state: ConcreteProjectPageStateHolder
    private lateinit var handler: ProjectPledgeNavigationHandler
    private lateinit var currentUser: MockCurrentUserV2
    private val currentProject = PublishSubject.create<Project>()

    @Before
    override fun setUp() {
        super.setUp()
        currentUser = MockCurrentUserV2()
        val env = environment().toBuilder().currentUserV2(currentUser).build()
        state = ConcreteProjectPageStateHolder(env)
        handler = ProjectPledgeNavigationHandler(env, state)
        handler.wire(currentProject)
    }

    @Test
    fun testActivityResult_showRewards_expandsPledgeSheet() {
        val expandObserver = state.expandPledgeSheet().test()

        state.activityResult(ActivityResult.create(ActivityRequestCodes.SHOW_REWARDS, Activity.RESULT_OK, Intent()))

        expandObserver.assertValue(Pair(true, true))
    }

    @Test
    fun testIntent_withExpandPledgeSheetExtra_expandsPledgeSheet() {
        val expandObserver = state.expandPledgeSheet().test()

        val intent = Intent().putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
        state.intent.onNext(intent)

        expandObserver.assertValue(Pair(true, true))
    }

    @Test
    fun testNativeProjectActionButtonClicked_expandsSheet_whenLoggedIn() {
        currentUser.refresh(UserFactory.user())
        val expandObserver = state.expandPledgeSheet().test()
        val project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build()

        currentProject.onNext(project)
        state.nativeProjectActionButtonClicked()

        expandObserver.assertValue(Pair(true, true))
    }

    @Test
    fun testNativeProjectActionButtonClicked_promptsLogin_whenLoggedOut() {
        val loginObserver = state.startLoginToutActivity().test()
        val project = ProjectFactory.project().toBuilder().state(Project.STATE_LIVE).build()

        currentProject.onNext(project)
        state.nativeProjectActionButtonClicked()

        loginObserver.assertValueCount(1)
    }

    @Test
    fun testPledgeToolbarNavigationClicked_collapsesSheet_whenStackEmpty() {
        val expandObserver = state.expandPledgeSheet().test()

        state.fragmentStackCount(0)
        state.pledgeToolbarNavigationClicked()

        expandObserver.assertValue(Pair(false, true))
    }

    @Test
    fun testPledgeToolbarNavigationClicked_popsFragment_whenStackNotEmpty() {
        val backObserver = state.goBack().test()

        state.fragmentStackCount(2)
        state.pledgeToolbarNavigationClicked()

        backObserver.assertValueCount(1)
    }
}
