package com.kickstarter.features.backing.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.backing.data.BackingRepository
import com.kickstarter.features.backing.data.BackingState
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Assert.assertTrue
import org.junit.Test

class BackingFeatureViewModelTest : KSRobolectricTestCase() {

    private fun createRepository(
        backingInfoObservable: Observable<BackingState> = Observable.empty(),
        postBackingObservable: Observable<Backing> = Observable.empty()
    ): BackingRepository {
        return object : BackingRepository {
            override fun getBackingInfo(projectData: ProjectData): Observable<BackingState> = backingInfoObservable
            override fun postBackingReceived(project: Project, backing: Backing, checked: Boolean): Observable<Backing> = postBackingObservable
            override fun cleanDisposables() {}
        }
    }

    @Test
    fun `test configureWith emits backer name and avatar`() {
        val backing = BackingFactory.backing().toBuilder()
            .backerName("Alice Walker")
            .backerUrl("https://example.com/avatar.png")
            .amount(50.0)
            .build()
        val project = ProjectFactory.project().toBuilder().backing(backing).build()
        val projectData = ProjectDataFactory.project(project)

        val repository = createRepository(backingInfoObservable = Observable.just(BackingState.Success(backing)))
        val vm = BackingFeatureViewModel(environment(), repository)

        val nameObserver = TestObserver<String>()
        val avatarObserver = TestObserver<String>()
        val stateObserver = TestObserver<BackingState>()

        vm.outputs.backerName().subscribe(nameObserver)
        vm.outputs.backerAvatar().subscribe(avatarObserver)
        vm.outputs.backingState().subscribe(stateObserver)

        vm.inputs.configureWith(projectData)

        nameObserver.assertValue("Alice Walker")
        avatarObserver.assertValue("https://example.com/avatar.png")
        stateObserver.assertValue(BackingState.Success(backing))
    }

    @Test
    fun `test fixPaymentMethodButtonClicked emits notification`() {
        val vm = BackingFeatureViewModel(environment(), createRepository())
        val fixPledgeObserver = TestObserver<Unit>()

        vm.outputs.notifyDelegateToShowFixPledge().subscribe(fixPledgeObserver)
        vm.inputs.fixPaymentMethodButtonClicked()

        fixPledgeObserver.assertValueCount(1)
    }

    @Test
    fun `test receivedCheckboxToggled calls repository and updates state`() {
        val backing = BackingFactory.backing().toBuilder()
            .completedByBacker(false)
            .build()
        val project = ProjectFactory.project().toBuilder().backing(backing).build()
        val projectData = ProjectDataFactory.project(project)

        var postCalledWithChecked = false
        val repo = object : BackingRepository {
            override fun getBackingInfo(projectData: ProjectData): Observable<BackingState> =
                Observable.just(BackingState.Success(backing))

            override fun postBackingReceived(project: Project, backing: Backing, checked: Boolean): Observable<Backing> {
                postCalledWithChecked = checked
                return Observable.just(backing.toBuilder().completedByBacker(checked).build())
            }

            override fun cleanDisposables() {}
        }

        val vm = BackingFeatureViewModel(environment(), repo)
        vm.inputs.configureWith(projectData)
        vm.inputs.receivedCheckboxToggled(true)

        assertTrue(postCalledWithChecked)
    }
}
