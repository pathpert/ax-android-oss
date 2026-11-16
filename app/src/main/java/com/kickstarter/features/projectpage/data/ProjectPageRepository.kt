package com.kickstarter.features.projectpage.data

import android.content.Intent
import android.net.Uri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.hasSecretRewardToken
import com.kickstarter.libs.utils.extensions.secretRewardToken
import com.kickstarter.libs.utils.extensions.updateProjectWith
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

/**
 * Repository coordinating remote Apollo GraphQL calls, project updates,
 * cookie ref-tags, and 3rd party attribution events.
 *
 * Negative constraints:
 * - Do not run network mutations directly without single-flight protection.
 * - Always clean disposables upon ViewModel teardown.
 */
class ProjectPageRepository(
    private val environment: Environment,
    private val scope: CoroutineScope,
    private val disposables: CompositeDisposable
) {
    private val cookieManager = requireNotNull(environment.cookieManager())
    private val currentUser = requireNotNull(environment.currentUserV2())
    private val ffClient = requireNotNull(environment.featureFlagClient())
    private val sharedPreferences = requireNotNull(environment.sharedPreferences())
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentConfig = requireNotNull(environment.currentConfigV2())

    val progressBarIsGone: PublishSubject<Boolean> = PublishSubject.create()

    fun mapProjectNotification(
        intent: Observable<Intent>,
        reloadClicked: Observable<Unit>
    ): Observable<Notification<Project>> {
        return Observable.merge(
            intent,
            intent
                .compose(takeWhenV2(reloadClicked))
                .doOnError { throwable ->
                    val problem = ProblemDetailsMapper.fromThrowable(throwable, "map project notification")
                    FirebaseCrashlytics.getInstance().setCustomKey("mappedProjectNotification", "${problem.title}: ${problem.detail}")
                    FirebaseCrashlytics.getInstance().recordException(ProblemException(problem, throwable))
                }
        ).switchMap {
            ProjectIntentMapper.project(it, this.apolloClient)
                .doOnSubscribe { progressBarIsGone.onNext(false) }
                .doAfterTerminate { progressBarIsGone.onNext(true) }
                .withLatestFrom(
                    currentConfig.observable(),
                    currentUser.observable()
                ) { project, config, user ->
                    runCatching {
                        project.updateProjectWith(config, user.getValue())
                    }.getOrDefault(project)
                }
                .doOnError { throwable ->
                    val problem = ProblemDetailsMapper.fromThrowable(throwable, "load intent project")
                    FirebaseCrashlytics.getInstance().setCustomKey("ProjectIntentMapper.project", "${problem.title}: ${problem.detail}")
                    FirebaseCrashlytics.getInstance().recordException(ProblemException(problem, throwable))
                }
                .materialize()
        }.share()
    }

    fun refreshProjectNotification(
        initialProject: Observable<Project>,
        refreshEvent: Observable<Unit>
    ): Observable<Notification<Project>> {
        return initialProject
            .compose(takeWhenV2(refreshEvent))
            .switchMap {
                it.slug()?.let { slug ->
                    this.apolloClient.getProject(slug)
                        .doOnSubscribe { progressBarIsGone.onNext(false) }
                        .doAfterTerminate { progressBarIsGone.onNext(true) }
                        .withLatestFrom(
                            currentConfig.observable(),
                            currentUser.observable()
                        ) { project, config, user ->
                            project.updateProjectWith(config, user.getValue())
                        }
                        .doOnError { throwable ->
                            val problem = ProblemDetailsMapper.fromThrowable(throwable, "refresh project notification")
                            FirebaseCrashlytics.getInstance().setCustomKey("refreshedProjectNotification", "${problem.title}: ${problem.detail}")
                            FirebaseCrashlytics.getInstance().recordException(ProblemException(problem, throwable))
                        }
                        .materialize()
                }
            }.share()
    }

    fun cookieRefTag(initialProject: Observable<Project>): Observable<KsOptional<RefTag?>> {
        return initialProject
            .take(1)
            .map { p ->
                KsOptional.of(
                    RefTagUtils.storedCookieRefTagForProject(
                        p,
                        this.cookieManager,
                        this.sharedPreferences
                    )
                )
            }
    }

    fun saveProject(project: Project): Observable<Project> {
        return this.apolloClient.watchProject(project).compose(neverErrorV2())
    }

    fun unSaveProject(project: Project): Observable<Project> {
        return this.apolloClient.unWatchProject(project).compose(neverErrorV2())
    }

    fun toggleProjectSave(project: Project): Observable<Project> {
        return if (project.isStarred()) {
            unSaveProject(project)
        } else {
            saveProject(project)
        }
    }

    fun buildProjectData(
        refTag: Observable<KsOptional<RefTag?>>,
        cookieRefTag: Observable<KsOptional<RefTag?>>,
        fullDeeplink: Observable<KsOptional<Uri?>>,
        currentProject: Observable<Project>
    ): Observable<ProjectData> {
        return Observable.combineLatest(
            refTag,
            cookieRefTag,
            fullDeeplink,
            currentProject
        ) { refTagFromIntent, refTagFromCookie, deeplink, project ->
            ProjectData.builder()
                .refTagFromIntent(refTagFromIntent.getValue())
                .refTagFromCookie(refTagFromCookie.getValue())
                .fullDeeplink(deeplink.getValue())
                .project(project)
                .build()
        }
    }

    fun storeCurrentCookieRefTag(data: ProjectData): ProjectData {
        return data.toBuilder()
            .refTagFromCookie(
                RefTagUtils.storedCookieRefTagForProject(
                    data.project(),
                    cookieManager,
                    sharedPreferences
                )
            ).build()
    }

    fun bindSecretRewardGroup(currentProjectData: Observable<ProjectData>) {
        scope.launch {
            runCatching {
                currentUser.observable()
                    .asFlow()
                    .combine(currentProjectData.asFlow()) { user, projectData ->
                        if (user.isPresent()) {
                            val deeplink = projectData.fullDeeplink()
                            val token = deeplink?.takeIf { it.hasSecretRewardToken() }
                                ?.secretRewardToken()
                            token?.let { android.util.Pair(projectData.project(), token) }
                        } else {
                            null
                        }
                    }
                    .filterNotNull()
                    .take(1)
                    .distinctUntilChanged()
                    .collectLatest { pair ->
                        apolloClient.addUserToSecretRewardGroup(pair.first, pair.second)
                    }
            }
        }
    }

    fun bindThirdPartyEvent(
        currentProject: Observable<Project>,
        intent: Observable<Intent>,
        onThirdPartyEventSent: BehaviorSubject<Boolean?>
    ) {
        var previousScreen = ""
        intent
            .subscribe { previousScreen = it.getStringExtra(IntentKey.PREVIOUS_SCREEN) ?: "" }
            .addToDisposable(disposables)

        SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
            .sendThirdPartyEvent(
                project = currentProject,
                apolloClient = apolloClient,
                currentUser = currentUser,
                eventName = ThirdPartyEventValues.EventName.SCREEN_VIEW,
                firebaseScreen = ThirdPartyEventValues.ScreenName.PROJECT.value,
                firebasePreviousScreen = previousScreen
            )
            .compose(neverErrorV2())
            .subscribe { onThirdPartyEventSent.onNext(it.first) }
            .addToDisposable(disposables)
    }

    fun storeCookieIfAbsent(data: ProjectData) {
        if (data.refTagFromCookie() == null) {
            data.refTagFromIntent()?.let {
                RefTagUtils.storeCookie(it, data.project(), cookieManager, sharedPreferences)
            }
        }
    }
}
