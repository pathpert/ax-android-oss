package com.kickstarter.features.splash.data

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Pair
import androidx.core.net.toUri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.features.splash.extensions.appendRefTagIfNone
import com.kickstarter.features.splash.extensions.isEligibleForProjectPage
import com.kickstarter.features.splash.extensions.isUnsupportedDeepLink
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.canUpdateFulfillment
import com.kickstarter.libs.utils.extensions.isCheckoutUri
import com.kickstarter.libs.utils.extensions.isDiscoverUri
import com.kickstarter.libs.utils.extensions.isEmailDomain
import com.kickstarter.libs.utils.extensions.isKSDomain
import com.kickstarter.libs.utils.extensions.isMainPage
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isPMOrderEditUri
import com.kickstarter.libs.utils.extensions.isPMUri
import com.kickstarter.libs.utils.extensions.isProjectCommentUri
import com.kickstarter.libs.utils.extensions.isProjectPreviewUri
import com.kickstarter.libs.utils.extensions.isProjectSaveUri
import com.kickstarter.libs.utils.extensions.isProjectSurveyUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateCommentsUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.libs.utils.extensions.isSettingsUrl
import com.kickstarter.libs.utils.extensions.isVideoFeedUri
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.util.Optional

class ProcessIntentException(message: String) : Exception(message)

/**
 * Handles intent routing and deep link processing for app launch.
 *
 * Encapsulates URL classification, redirect resolution, and project-page routing.
 *
 * ### Negative Constraints:
 * - Do not block UI thread during network-based email redirection checks.
 * - Do not pass unvalidated raw intent extras without type narrowing.
 */
class DeepLinkProcessor(
    private val webEndpoint: String,
    private val apolloClient: ApolloClientTypeV2,
    private val apiClientType: ApiClientTypeV2,
    private val currentUser: CurrentUserTypeV2,
    private val ffClient: FeatureFlagClientType,
    private val externalCall: CustomNetworkClient,
    private val disposables: CompositeDisposable
) : SplashOutputs {

    private val startBrowserSubject = BehaviorSubject.create<String>()
    private val startDiscoveryActivitySubject = BehaviorSubject.create<Optional<Uri>>()
    private val startProjectActivitySubject = BehaviorSubject.create<Uri>()
    private val startProjectActivityForCommentSubject = BehaviorSubject.create<Uri>()
    private val startProjectActivityForUpdateSubject = BehaviorSubject.create<Uri>()
    private val startProjectActivityForCommentToUpdateSubject = BehaviorSubject.create<Uri>()
    private val startProjectActivityWithCheckoutSubject = BehaviorSubject.create<Uri>()
    private val startProjectActivityToSaveSubject = BehaviorSubject.create<Uri>()
    private val startProjectSurveySubject = BehaviorSubject.create<Pair<Uri, Boolean>>()
    private val startPMWebviewSubject = BehaviorSubject.create<Uri>()
    private val startVideoFeedActivitySubject = BehaviorSubject.create<Unit>()
    private val updateUserPreferencesSubject = BehaviorSubject.create<Boolean>()
    private val finishDeeplinkActivitySubject = BehaviorSubject.create<Unit>()
    private val startPreLaunchProjectActivitySubject = BehaviorSubject.create<Pair<Uri, Project>>()

    override fun startBrowser(): Observable<String> = startBrowserSubject
    override fun startDiscoveryActivity(): Observable<Optional<Uri>> = startDiscoveryActivitySubject
    override fun startProjectActivity(): Observable<Uri> = startProjectActivitySubject
    override fun startProjectActivityForComment(): Observable<Uri> = startProjectActivityForCommentSubject
    override fun startProjectActivityForUpdate(): Observable<Uri> = startProjectActivityForUpdateSubject
    override fun startProjectActivityForCommentToUpdate(): Observable<Uri> = startProjectActivityForCommentToUpdateSubject
    override fun startProjectActivityForCheckout(): Observable<Uri> = startProjectActivityWithCheckoutSubject
    override fun finishDeeplinkActivity(): Observable<Unit> = finishDeeplinkActivitySubject
    override fun startProjectActivityToSave(): Observable<Uri> = startProjectActivityToSaveSubject
    override fun startProjectSurvey(): Observable<Pair<Uri, Boolean>> = startProjectSurveySubject
    override fun startPMWebview(): Observable<Uri> = startPMWebviewSubject
    override fun startPreLaunchProjectActivity(): Observable<Pair<Uri, Project>> = startPreLaunchProjectActivitySubject
    override fun startVideoFeedActivity(): Observable<Unit> = startVideoFeedActivitySubject

    fun processIntent(intentObservable: Observable<Intent>) {
        intentObservable
            .filter { it.action == Intent.ACTION_MAIN }
            .subscribe { intent ->
                runCatching {
                    if (intent.categories.isNullOrEmpty()) {
                        val strIntentUri = intent.toUri(Intent.URI_INTENT_SCHEME)
                        FirebaseCrashlytics.getInstance().setCustomKey("SplashScreenViewModel.processIntent", strIntentUri)
                        FirebaseCrashlytics.getInstance().log(strIntentUri)
                        FirebaseCrashlytics.getInstance().recordException(ProcessIntentException(strIntentUri))
                    }
                }
                startDiscoveryActivitySubject.onNext(Optional.empty<Uri>())
            }
            .addToDisposable(disposables)

        val uriFromIntent = intentObservable
            .filter { it.data.isNotNull() }
            .map { it.data }
            .ofType(Uri::class.java)

        val uriFromEmailDomain = uriFromIntent
            .filter { it.isNotNull() && it.isEmailDomain() }
            .switchMap { externalCall.obtainUriFromRedirection(it) }
            .filter { it.priorResponse?.code == 302 }

        val isKSDomainUriFromEmail = uriFromEmailDomain
            .map { it.request.url.toString().toUri() }
            .filter { !it.isProjectUri() && it.isKSDomain() }

        val projectFromEmail = uriFromEmailDomain
            .filter { it.request.url.toString().toUri().isProjectUri() }
            .map { it.request.url.toString().toUri() }

        uriFromIntent.filter { it.isMainPage() }.subscribe {
            startDiscoveryActivitySubject.onNext(Optional.empty<Uri>())
        }.addToDisposable(disposables)

        projectFromEmail.subscribe {
            startProjectActivitySubject.onNext(it)
        }.addToDisposable(disposables)

        isKSDomainUriFromEmail.subscribe {
            startDiscoveryActivitySubject.onNext(Optional.empty<Uri>())
        }.addToDisposable(disposables)

        uriFromIntent.filter { it.isDiscoverUri(webEndpoint) }.subscribe {
            startDiscoveryActivitySubject.onNext(Optional.of(it))
        }.addToDisposable(disposables)

        uriFromIntent
            .filter { it.isNotNull() && !it.isDiscoverUri(webEndpoint) && it.lastPathSegment == "projects" }
            .compose(Transformers.ignoreValuesV2())
            .subscribe {
                startDiscoveryActivitySubject.onNext(Optional.empty<Uri>())
            }.addToDisposable(disposables)

        val projectObservable: Observable<Project> = uriFromIntent
            .filter { it.isNotNull() && ProjectIntentMapper.paramFromUri(it).isNotNull() }
            .map { ProjectIntentMapper.paramFromUri(it) }
            .switchMap { param ->
                apolloClient.getProject(param)
                    .materialize()
                    .share()
                    .distinctUntilChanged()
                    .doOnError { finishDeeplinkActivitySubject.onNext(Unit) }
            }
            .filter { it.value.isNotNull() }
            .map { it.value }

        uriFromIntent
            .filter { it.isEligibleForProjectPage(webEndpoint) }
            .map { it.appendRefTagIfNone() }
            .compose(Transformers.combineLatestPair(projectObservable))
            .subscribe { pair ->
                if (pair.second.displayPrelaunch() == true) {
                    startPreLaunchProjectActivitySubject.onNext(Pair(pair.first, pair.second))
                } else {
                    startProjectActivitySubject.onNext(pair.first)
                }
            }.addToDisposable(disposables)

        uriFromIntent
            .filter { it.isNotNull() && it.isProjectSaveUri(webEndpoint) }
            .map { it.appendRefTagIfNone() }
            .compose(Transformers.combineLatestPair(projectObservable))
            .subscribe { pair ->
                if (pair.second.displayPrelaunch() == true) {
                    startPreLaunchProjectActivitySubject.onNext(Pair(pair.first, pair.second))
                } else {
                    startProjectActivityToSaveSubject.onNext(pair.first)
                }
            }.addToDisposable(disposables)

        uriFromIntent.filter { it.isNotNull() && it.isProjectCommentUri(webEndpoint) }
            .map { it.appendRefTagIfNone() }
            .subscribe { startProjectActivityForCommentSubject.onNext(it) }
            .addToDisposable(disposables)

        uriFromIntent.filter { it.isNotNull() && it.isProjectUpdateUri(webEndpoint) && !it.isProjectUpdateCommentsUri(webEndpoint) }
            .map { it.appendRefTagIfNone() }
            .subscribe { startProjectActivityForUpdateSubject.onNext(it) }
            .addToDisposable(disposables)

        uriFromIntent.filter { it.isNotNull() && it.isProjectUpdateCommentsUri(webEndpoint) }
            .map { it.appendRefTagIfNone() }
            .subscribe { startProjectActivityForCommentToUpdateSubject.onNext(it) }
            .addToDisposable(disposables)

        uriFromIntent.filter { it.isNotNull() && it.isSettingsUrl() }
            .subscribe { updateUserPreferencesSubject.onNext(true) }
            .addToDisposable(disposables)

        uriFromIntent.filter { it.isNotNull() && it.isProjectSurveyUri(webEndpoint) }
            .map { it.appendRefTagIfNone() }
            .withLatestFrom(currentUser.isLoggedIn) { url, isLoggedIn -> Pair(url, isLoggedIn) }
            .subscribe { startProjectSurveySubject.onNext(it) }
            .addToDisposable(disposables)

        uriFromIntent
            .filter { it.isPMUri(webEndpoint) || it.isPMOrderEditUri(webEndpoint, ffClient.getBoolean(FlagKey.ANDROID_EDIT_ORDER)) }
            .map { it.appendRefTagIfNone() }
            .subscribe { startPMWebviewSubject.onNext(it) }
            .addToDisposable(disposables)

        uriFromIntent.filter { it.isNotNull() && it.isVideoFeedUri(webEndpoint) }
            .subscribe { startVideoFeedActivitySubject.onNext(Unit) }
            .addToDisposable(disposables)

        currentUser.observable()
            .filter { it.isPresent() }
            .map { it.getValue() }
            .compose<Pair<User, Boolean>>(combineLatestPair(updateUserPreferencesSubject))
            .switchMap { pair ->
                val updatedUser = pair.first.toBuilder().notifyMobileOfMarketingUpdate(true).build()
                apiClientType.updateUserSettings(updatedUser).materialize().share()
            }
            .compose(Transformers.valuesV2())
            .distinctUntilChanged()
            .subscribe { user ->
                currentUser.refresh(user)
                finishDeeplinkActivitySubject.onNext(Unit)
            }.addToDisposable(disposables)

        projectObservable
            .filter { it.backing() == null || !it.canUpdateFulfillment() }
            .subscribe { finishDeeplinkActivitySubject.onNext(Unit) }
            .addToDisposable(disposables)

        projectObservable
            .filter { it.isNotNull() && it.canUpdateFulfillment() }
            .switchMap { project ->
                apiClientType.postBacking(project, requireNotNull(project.backing()), true)
                    .compose(Transformers.neverErrorV2())
                    .distinctUntilChanged()
                    .doOnError { finishDeeplinkActivitySubject.onNext(Unit) }
            }
            .subscribe { finishDeeplinkActivitySubject.onNext(Unit) }
            .addToDisposable(disposables)

        uriFromIntent.filter { it.isCheckoutUri(webEndpoint) }
            .map { it.appendRefTagIfNone() }
            .subscribe { startProjectActivityWithCheckoutSubject.onNext(it) }
            .addToDisposable(disposables)

        val projectPreview = uriFromIntent.filter { it.isNotNull() && it.isProjectPreviewUri(webEndpoint) }
        val unsupportedDeepLink = uriFromIntent.filter { it.isUnsupportedDeepLink(webEndpoint, ffClient) }

        Observable.merge(projectPreview, unsupportedDeepLink)
            .map { it.toString() }
            .filter { !TextUtils.isEmpty(it) }
            .subscribe { startBrowserSubject.onNext(it) }
            .addToDisposable(disposables)
    }
}
