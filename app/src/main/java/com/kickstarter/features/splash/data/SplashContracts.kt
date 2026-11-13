package com.kickstarter.features.splash.data

import android.net.Uri
import android.util.Pair
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.models.Project
import io.reactivex.Observable
import okhttp3.Response
import java.util.Optional

/**
 * Startup initialization lifecycle state of the app.
 *
 * Tracks remote configuration, feature-flag activations, and essential bootstrap routines.
 */
enum class InitializationState {
    NOT_STARTED,
    RUNNING,
    FINISHED
}

/**
 * Immutable UI state emitted by the splash screen during cold start.
 *
 * Adheres to RFC 9457 Problem Details for actionable startup failure propagation.
 *
 * ### Negative Constraints:
 * - Do not swallow initialization errors; propagate as [SplashUIState.Failed] with remediation guidance.
 * - Do not pass unvalidated exception messages directly into [Failed]; wrap via [ProblemDetails].
 */
sealed class SplashUIState {
    object Loading : SplashUIState()
    object NoInternet : SplashUIState()
    object Errored : SplashUIState()
    object Finished : SplashUIState()
    data class Failed(val problem: ProblemDetails) : SplashUIState()
}

/**
 * Resolved top-level navigation destination following splash screen initialization.
 */
sealed class NavigationTarget {
    object Discovery : NavigationTarget()
}

/**
 * Network abstraction for executing marketing email redirection checks.
 */
interface CustomNetworkClient {
    fun obtainUriFromRedirection(uri: Uri): Observable<Response>
}

/**
 * Outputs for Splash and DeepLink resolution.
 */
interface SplashOutputs {
    fun startBrowser(): Observable<String>
    fun startDiscoveryActivity(): Observable<Optional<Uri>>
    fun startProjectActivity(): Observable<Uri>
    fun startProjectActivityForComment(): Observable<Uri>
    fun startProjectActivityForUpdate(): Observable<Uri>
    fun startProjectActivityForCommentToUpdate(): Observable<Uri>
    fun startProjectActivityForCheckout(): Observable<Uri>
    fun finishDeeplinkActivity(): Observable<Unit>
    fun startProjectActivityToSave(): Observable<Uri>
    fun startProjectSurvey(): Observable<Pair<Uri, Boolean>>
    fun startPMWebview(): Observable<Uri>
    fun startPreLaunchProjectActivity(): Observable<Pair<Uri, Project>>
    fun startVideoFeedActivity(): Observable<Unit>
}
