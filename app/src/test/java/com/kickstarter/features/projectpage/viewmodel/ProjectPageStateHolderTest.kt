package com.kickstarter.features.projectpage.viewmodel

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test

class ProjectPageStateHolderTest : KSRobolectricTestCase() {

    private class ConcreteProjectPageStateHolder(env: Environment) : ProjectPageStateHolder(env)

    private lateinit var stateHolder: ConcreteProjectPageStateHolder

    @Before
    override fun setUp() {
        super.setUp()
        stateHolder = ConcreteProjectPageStateHolder(environment())
    }

    @Test
    fun testInputsDispatchesToSubjects() {
        val intentObserver = stateHolder.intent.test()
        val heartObserver = stateHolder.heartButtonClicked.test()
        val tabObserver = stateHolder.tabSelected.test()

        val intent = Intent()
        stateHolder.intent.onNext(intent)
        stateHolder.heartButtonClicked()
        stateHolder.tabSelected(2)

        intentObserver.assertValue(intent)
        heartObserver.assertValueCount(1)
        tabObserver.assertValue(2)
    }

    @Test
    fun testOutputsEmissions() {
        val backingVisibleObserver = stateHolder.backingDetailsIsVisible().test()
        val expandSheetObserver = stateHolder.expandPledgeSheet().test()

        stateHolder.backingDetailsIsVisible.onNext(true)
        stateHolder.expandPledgeSheet.onNext(Pair(true, false))

        backingVisibleObserver.assertValue(true)
        expandSheetObserver.assertValue(Pair(true, false))
    }

    @Test
    fun testDisposablesLifecycle() {
        assertFalse(stateHolder.disposables.isDisposed)
        val project = ProjectFactory.project()
        val observer = TestObserver<Any>()
        stateHolder.showSocialShareSheet().subscribe(observer)

        stateHolder.showSocialShareSheet.onNext(project)
        observer.assertValue(project)
    }
}
