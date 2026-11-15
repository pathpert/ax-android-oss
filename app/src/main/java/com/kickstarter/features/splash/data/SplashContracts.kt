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
 *
 * Negative constraints:
 * - Do not assume services are available while in [NOT_STARTED] or [RUNNING].
 */
enum class InitializationState {
    /** Bootstrap routines have not yet been triggered. */
    NOT_STARTED,
    /** Bootstrap and remote configuration sync currently executing. */
    RUNNING,
    /** App initialization finished successfully. */
    FINISHED
}

/**
 * Immutable UI state emitted by the splash screen during cold start.
 *
 * Adheres to RFC 9457 Problem Details for actionable startup failure propagation.
 *
 * Negative constraints:
 * - Do not swallow initialization errors; propagate as [SplashUIState.Failed] with remediation guidance.
 * - Do not pass unvalidated exception messages directly into [Failed]; wrap via [ProblemDetails].
 */
sealed class SplashUIState {
    /**
     * Initial startup and bootstrap loading state.
     *
     * Negative constraints:
     * - Do not dismiss splash screen while loading.
     */
    object Loading : SplashUIState()

    /**
     * Network connectivity unavailable during bootstrap.
     *
     * Negative constraints:
     * - Do not proceed to main app without user acknowledgment or offline fallback.
     */
    object NoInternet : SplashUIState()

    /**
     * Generic initialization error occurred.
     *
     * Negative constraints:
     * - Do not emit without logging underlying error.
     */
    object Errored : SplashUIState()

    /**
     * Cold start initialization completed.
     *
     * Negative constraints:
     * - Do not retain splash UI after transition.
     */
    object Finished : SplashUIState()

    /**
     * Actionable startup failure with RFC 9457 Problem Details.
     *
     * Negative constraints:
     * - Do not pass blank detail or suggested actions.
     *
     * @property problem Structured actionable failure description carrying suggestedAction and isRetriable flag.
     */
    data class Failed(val problem: ProblemDetails) : SplashUIState()
}

/**
 * Resolved top-level navigation destination following splash screen initialization.
 *
 * Negative constraints:
 * - Do not navigate without a resolved destination.
 */
sealed class NavigationTarget {
    /**
     * Primary discovery feed destination.
     *
     * Negative constraints:
     * - Do not pass invalid navigation arguments.
     */
    object Discovery : NavigationTarget()
}

/**
 * Network abstraction for executing marketing email redirection checks.
 *
 * Negative constraints:
 * - Do not invoke on the main thread; execute on IO dispatcher.
 */
interface CustomNetworkClient {
    fun obtainUriFromRedirection(uri: Uri): Observable<Response>
}

/**
 * Outputs for Splash and DeepLink resolution.
 *
 * Negative constraints:
 * - Do not emit navigation events with empty or malformed URIs.
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
