package com.kickstarter.features.splash.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.kickstarter.KSApplication
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.features.splash.data.CustomNetworkClient
import com.kickstarter.features.splash.data.DeepLinkProcessor
import com.kickstarter.features.splash.data.InitializationState
import com.kickstarter.features.splash.data.SplashOutputs
import com.kickstarter.features.splash.data.SplashUIState
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FirebaseHelper
import com.kickstarter.libs.utils.extensions.isTrue
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Modernized ViewModel driving cold-start initialization and deep-link routing.
 *
 * Emits unidirectional [SplashUIState] (including RFC 9457 [ProblemDetails] on failures)
 * and exposes reactive navigation [SplashOutputs].
 *
 * ### Negative Constraints:
 * - Do not perform network calls directly on main thread; use [Schedulers.io] or [viewModelScope].
 * - Do not mutate [SplashUIState] outside reducer routines.
 */
open class SplashFeatureViewModel(
    environment: Environment,
    private val application: KSApplication,
    private val intent: Intent?,
    private val externalCall: CustomNetworkClient
) : ViewModel(), SplashOutputs {

    private val disposables = CompositeDisposable()
    private val mutableUiState = MutableStateFlow<SplashUIState>(SplashUIState.Loading)
    val uiState: StateFlow<SplashUIState> = mutableUiState.asStateFlow()

    private val ffClient = requireNotNull(environment.featureFlagClient())
    private val webEndpoint = requireNotNull(environment.webEndpoint())
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val apiClientType = requireNotNull(environment.apiClientV2())
    private val currentUser = requireNotNull(environment.currentUserV2())

    private val deepLinkProcessor = DeepLinkProcessor(
        webEndpoint = webEndpoint,
        apolloClient = apolloClient,
        apiClientType = apiClientType,
        currentUser = currentUser,
        ffClient = ffClient,
        externalCall = externalCall,
        disposables = disposables
    )

    val outputs: SplashOutputs = this

    fun runInitializations() {
        when (application.initializationState.value) {
            InitializationState.FINISHED,
            InitializationState.RUNNING -> {
                processIntent()
                mutableUiState.value = SplashUIState.Finished
                return
            }
            else -> {}
        }

        application.mutableInitializationState.value = InitializationState.RUNNING

        viewModelScope.launch {
            val remoteConfig = runCatching { Firebase.remoteConfig }.getOrNull()
            ffClient.initialize(remoteConfig)

            FirebaseHelper.identifier
                .filter { it.isNotBlank() }
                .collect {
                    try {
                        val ffClientInitialization = async(SupervisorJob()) {
                            ffClient.fetchAndActivate()
                        }
                        val isInitialized = awaitAll(ffClientInitialization)

                        if (isInitialized.isNotEmpty() && isInitialized.all { it.isTrue() }) {
                            mutableUiState.emit(SplashUIState.Finished)
                            application.mutableInitializationState.value = InitializationState.FINISHED
                            processIntent()
                        } else {
                            val problem = ProblemDetails(
                                type = ProblemDetails.TYPE_SERVER,
                                title = "Feature-Flag Initialization Failed",
                                status = 503,
                                detail = "Statsig client did not initialize on startup.",
                                suggestedAction = "Retry app cold-start.",
                                isRetriable = true,
                                retryAfterSeconds = 5
                            )
                            mutableUiState.emit(SplashUIState.Failed(problem))
                            application.mutableInitializationState.value = InitializationState.FINISHED
                            processIntent()
                        }
                    } catch (e: Exception) {
                        val problem = ProblemDetails(
                            type = ProblemDetails.TYPE_UNEXPECTED,
                            title = "Startup Initialization Error",
                            status = 500,
                            detail = e.message ?: "Unexpected startup initialization error.",
                            suggestedAction = "Retry app cold-start.",
                            isRetriable = true
                        )
                        mutableUiState.emit(SplashUIState.Failed(problem))
                        application.mutableInitializationState.value = InitializationState.FINISHED
                        processIntent()
                    }
                }
        }
    }

    private fun processIntent() {
        val intentObservable = intent?.let { Observable.just(it) } ?: Observable.empty()
        deepLinkProcessor.processIntent(intentObservable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    override fun startBrowser() = deepLinkProcessor.startBrowser()
    override fun startDiscoveryActivity() = deepLinkProcessor.startDiscoveryActivity()
    override fun startProjectActivity() = deepLinkProcessor.startProjectActivity()
    override fun startProjectActivityForComment() = deepLinkProcessor.startProjectActivityForComment()
    override fun startProjectActivityForUpdate() = deepLinkProcessor.startProjectActivityForUpdate()
    override fun startProjectActivityForCommentToUpdate() = deepLinkProcessor.startProjectActivityForCommentToUpdate()
    override fun startProjectActivityForCheckout() = deepLinkProcessor.startProjectActivityForCheckout()
    override fun finishDeeplinkActivity() = deepLinkProcessor.finishDeeplinkActivity()
    override fun startProjectActivityToSave() = deepLinkProcessor.startProjectActivityToSave()
    override fun startProjectSurvey() = deepLinkProcessor.startProjectSurvey()
    override fun startPMWebview() = deepLinkProcessor.startPMWebview()
    override fun startPreLaunchProjectActivity() = deepLinkProcessor.startPreLaunchProjectActivity()
    override fun startVideoFeedActivity() = deepLinkProcessor.startVideoFeedActivity()

    class Factory(
        private val environment: Environment,
        private val intent: Intent? = null,
        private val customNetworkClient: CustomNetworkClient? = null
    ) : ViewModelProvider.Factory {

        private val defaultExternalCall = object : CustomNetworkClient {
            override fun obtainUriFromRedirection(uri: Uri): Observable<Response> {
                val httpClient: OkHttpClient = OkHttpClient.Builder().build()
                return Observable.fromCallable {
                    httpClient.newCall(Request.Builder().url(uri.toString()).build()).execute()
                }.subscribeOn(Schedulers.io())
            }
        }

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val ksApplication = requireNotNull(extras[APPLICATION_KEY] as? KSApplication) {
                "KSApplication must be provided in CreationExtras"
            }
            val vm = SplashFeatureViewModel(
                environment = environment,
                application = ksApplication,
                intent = intent,
                externalCall = customNetworkClient ?: defaultExternalCall
            )
            return modelClass.cast(vm)
                ?: throw IllegalStateException("Failed to cast ${vm::class.java.name} to ${modelClass.name}")
        }
    }
}
