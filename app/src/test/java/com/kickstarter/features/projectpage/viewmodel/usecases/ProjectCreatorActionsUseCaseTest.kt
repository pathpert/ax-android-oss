package com.kickstarter.features.projectpage.viewmodel.usecases

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.projectpage.data.ProjectPageRepository
import com.kickstarter.features.projectpage.viewmodel.ProjectPageStateHolder
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectCreatorActionsUseCaseTest : KSRobolectricTestCase() {

    private class ConcreteProjectPageStateHolder(env: Environment) : ProjectPageStateHolder(env)

    private lateinit var state: ConcreteProjectPageStateHolder
    private lateinit var useCase: ProjectCreatorActionsUseCase
    private lateinit var currentUser: MockCurrentUserV2
    private val disposables = CompositeDisposable()
    private val currentProject = PublishSubject.create<Project>()
    private val currentProjectData = PublishSubject.create<ProjectData>()

    @Before
    override fun setUp() {
        super.setUp()
        currentUser = MockCurrentUserV2()
        val env = environment().toBuilder().currentUserV2(currentUser).build()
        state = ConcreteProjectPageStateHolder(env)
        val repo = ProjectPageRepository(env, TestScope(), disposables)
        useCase = ProjectCreatorActionsUseCase(env, state, repo)
        useCase.wire(currentProject, currentProjectData)
    }

    @After
    override fun tearDown() {
        disposables.clear()
        super.tearDown()
    }

    @Test
    fun testHeartDrawableId_updatesWithProjectStarredState() {
        val heartObserver = state.heartDrawableId().test()

        val unstarredProject = ProjectFactory.project().toBuilder().isStarred(false).build()
        currentProject.onNext(unstarredProject)
        heartObserver.assertValue(R.drawable.icon__heart_outline)

        val starredProject = ProjectFactory.project().toBuilder().isStarred(true).build()
        currentProject.onNext(starredProject)
        heartObserver.assertValues(R.drawable.icon__heart_outline, R.drawable.icon__heart)
    }

    @Test
    fun testContactCreatorClicked_startsMessagesActivity() {
        val messagesObserver = state.startMessagesActivity().test()
        val project = ProjectFactory.project()

        currentProject.onNext(project)
        state.contactCreatorClicked()

        messagesObserver.assertValue(project)
    }

    @Test
    fun testShareButtonClicked_triggersShareSheet() {
        val shareSheetObserver = state.showShareSheet().test()
        val project = ProjectFactory.project().toBuilder().name("Test Project").build()

        currentProject.onNext(project)
        state.shareButtonClicked()

        shareSheetObserver.assertValueCount(1)
        val sharePair = shareSheetObserver.values().first()
        assertEquals("Test Project", sharePair.first)
    }

    @Test
    fun testHeartButtonClicked_promptsLogin_whenLoggedOut() {
        val loginObserver = state.startLoginToutActivity().test()
        val project = ProjectFactory.project()

        useCase.createProjectSaveStreams(currentProject)
        currentProject.onNext(project)
        state.heartButtonClicked()

        loginObserver.assertValueCount(1)
    }
}
