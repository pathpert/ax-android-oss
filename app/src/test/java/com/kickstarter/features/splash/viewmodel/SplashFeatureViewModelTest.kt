package com.kickstarter.features.splash.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.kickstarter.KSApplication
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.features.splash.data.CustomNetworkClient
import com.kickstarter.features.splash.data.InitializationState
import com.kickstarter.features.splash.data.SplashUIState
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.mock.MockFeatureFlagClient
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Optional

@OptIn(ExperimentalCoroutinesApi::class)
class SplashFeatureViewModelTest : KSRobolectricTestCase() {

    private val mockCustomClient = object : CustomNetworkClient {
        override fun obtainUriFromRedirection(uri: Uri): Observable<Response> {
            return Observable.empty()
        }
    }

    private fun createViewModel(
        environment: Environment = environment(),
        intent: Intent? = null,
        initializationState: InitializationState = InitializationState.NOT_STARTED
    ): Pair<SplashFeatureViewModel, KSApplication> {
        val app = application() as KSApplication
        app.mutableInitializationState.value = initializationState

        val extras = MutableCreationExtras().apply {
            set(APPLICATION_KEY, app)
        }

        val factory = SplashFeatureViewModel.Factory(
            environment = environment,
            intent = intent,
            customNetworkClient = mockCustomClient
        )

        val vm = factory.create(SplashFeatureViewModel::class.java, extras)
        return Pair(vm, app)
    }

    @Test
    fun `runInitializations transitions to Finished immediately if app already initialized`() = runTest {
        val (vm, app) = createViewModel(initializationState = InitializationState.FINISHED)

        val uiStates = mutableListOf<SplashUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.toList(uiStates)
        }

        vm.runInitializations()

        assertEquals(SplashUIState.Finished, vm.uiState.value)
        assertEquals(InitializationState.FINISHED, app.initializationState.value)
    }

    @Test
    fun `runInitializations emits Failed with ProblemDetails when feature flag fails`() = runTest {
        val mockFF = object : MockFeatureFlagClient() {
            override suspend fun fetchAndActivate(): Boolean {
                return false
            }
        }

        val env = environment().toBuilder()
            .featureFlagClient(mockFF)
            .build()

        val (vm, app) = createViewModel(environment = env, initializationState = InitializationState.NOT_STARTED)

        val uiStates = mutableListOf<SplashUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.toList(uiStates)
        }

        FirebaseHelper.mutableIdentifier().value = "test-user-id"
        vm.runInitializations()

        val lastState = vm.uiState.value
        assertTrue(lastState is SplashUIState.Failed)
        val problem = (lastState as SplashUIState.Failed).problem
        assertEquals(ProblemDetails.TYPE_SERVER, problem.type)
        assertEquals(503, problem.status)
        assertTrue(problem.isRetriable)
        assertEquals(InitializationState.FINISHED, app.initializationState.value)
    }

    @Test
    fun `intent with ACTION_MAIN triggers discovery navigation`() = runTest {
        val mainIntent = Intent(Intent.ACTION_MAIN)
        val (vm, _) = createViewModel(intent = mainIntent, initializationState = InitializationState.FINISHED)

        val discoveryObserver = TestObserver<Optional<Uri>>()
        vm.outputs.startDiscoveryActivity().subscribe(discoveryObserver)

        vm.runInitializations()

        discoveryObserver.assertValueCount(1)
        assertEquals(Optional.empty<Uri>(), discoveryObserver.values().first())
    }
}
