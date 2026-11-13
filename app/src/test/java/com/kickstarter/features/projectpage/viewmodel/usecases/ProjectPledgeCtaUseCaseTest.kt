package com.kickstarter.features.projectpage.viewmodel.usecases

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.projectpage.viewmodel.ProjectPageStateHolder
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class ProjectPledgeCtaUseCaseTest : KSRobolectricTestCase() {

    private class ConcreteProjectPageStateHolder(env: Environment) : ProjectPageStateHolder(env)

    private lateinit var state: ConcreteProjectPageStateHolder
    private lateinit var useCase: ProjectPledgeCtaUseCase
    private lateinit var currentUser: MockCurrentUserV2

    private val currentProject = PublishSubject.create<Project>()
    private val currentProjectData = PublishSubject.create<ProjectData>()
    private val mappedProjectErrors = PublishSubject.create<Throwable?>()
    private val mappedProjectValues = PublishSubject.create<Project>()

    @Before
    override fun setUp() {
        super.setUp()
        currentUser = MockCurrentUserV2()
        val env = environment().toBuilder().currentUserV2(currentUser).build()
        state = ConcreteProjectPageStateHolder(env)
        useCase = ProjectPledgeCtaUseCase(env, state)
        useCase.wire(currentProject, currentProjectData, mappedProjectErrors, mappedProjectValues)
    }

    @Test
    fun testReloadProjectContainerIsGone_onErrorsAndReload() {
        val reloadObserver = state.reloadProjectContainerIsGone().test()

        mappedProjectErrors.onNext(Exception("Network error"))
        reloadObserver.assertValue(false)

        state.reloadProjectContainerClicked()
        reloadObserver.assertValues(false, true)
    }

    @Test
    fun testManagePledgeMenuOptions_emittedForProject() {
        val menuOptionsObserver = state.managePledgeMenuOptions().test()
        val project = ProjectFactory.project()

        currentProject.onNext(project)

        menuOptionsObserver.assertValueCount(1)
    }

    @Test
    fun testBackingDetails_emittedWhenProjectBacked() {
        val backingDetailsVisibleObserver = state.backingDetailsIsVisible().test()
        val backingDetailsTitleObserver = state.backingDetailsTitle().test()

        val backing = BackingFactory.backing()
        val backedProject = ProjectFactory.project().toBuilder()
            .state(Project.STATE_LIVE)
            .isBacking(true)
            .backing(backing)
            .build()

        currentProject.onNext(backedProject)

        backingDetailsVisibleObserver.assertValue(true)
        backingDetailsTitleObserver.assertValueCount(1)
    }
}
