package com.kickstarter.viewmodels.projectpage

import android.content.Intent
import android.net.Uri
import android.util.Pair
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.TakeWhenTransformerV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.updateProjectWith
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

/**
 * Rx streams initialization and wiring for [PrelaunchProjectViewModel.PrelaunchProjectViewModel].
 */
internal fun PrelaunchProjectViewModel.PrelaunchProjectViewModel.initPipelines(
    disposables: CompositeDisposable
) {
    val reducedProject = this.intent
        .map { it.getParcelableExtra<Project>(IntentKey.PROJECT) }
        .ofType(Project::class.java)
        .withLatestFrom(
            currentConfig.observable(),
            currentUser.observable(),
        ) { project, config, user ->
            if (user.isPresent()) {
                project.updateProjectWith(config, user.getValue())
            } else {
                project.updateProjectWith(config, null)
            }
        }.delay(1000, TimeUnit.MILLISECONDS, environment.schedulerV2())

    val loadedProject = intent
        .filter {
            it.getParcelableExtra<Project>(IntentKey.PROJECT).isNull()
        }.switchMap { intent ->
            PrelaunchProjectHelper.loadProject(intent, apolloClient, currentConfig, currentUser)
        }
        .share()

    val initialProject = Observable.merge(reducedProject, loadedProject)

    val cookieRefTag = initialProject
        .take(1)
        .map { p ->
            KsOptional.of(RefTagUtils.storedCookieRefTagForProject(p, this.cookieManager, this.sharedPreferences))
        }

    val refTag = intent
        .flatMap { ProjectIntentMapper.refTag(it) }

    val fullDeeplink = intent
        .flatMap { Observable.just(KsOptional.of(it.data)) }

    val loggedInUserOnHeartClick = this.currentUser.observable()
        .compose(Transformers.takeWhenV2(this.bookmarkButtonClicked))
        .filter { it.isPresent() }

    val loggedOutUserOnHeartClick = this.currentUser.observable()
        .compose(Transformers.takeWhenV2(this.bookmarkButtonClicked))
        .filter { !it.isPresent() }
        .map { !it.isPresent() }

    val savedProjectOnLoginSuccess = this.startLoginToutActivity
        .compose(Transformers.takePairWhenV2(this.currentUser.observable()))
        .doOnError { println(it.message) }
        .filter { su -> su.second != null }
        .withLatestFrom<Project, Project>(initialProject) { _, p -> p }
        .take(1)
        .switchMap {
            PrelaunchProjectHelper.saveProject(apolloClient, it)
        }

    val projectOnUserChangeSave = initialProject
        .compose(Transformers.takePairWhenV2(loggedInUserOnHeartClick))
        .withLatestFrom(projectData) { initProject, latestProjectData ->
            PrelaunchProjectHelper.mapProject(latestProjectData, initProject)
        }
        .switchMap {
            PrelaunchProjectHelper.toggleProjectSave(apolloClient, it)
        }
        .share()
        .filter { it.isNotNull() }
        .map { it }

    val currentProject = Observable.mergeArray(
        initialProject,
        savedProjectOnLoginSuccess,
        projectOnUserChangeSave,
    )

    val currentProjectData = Observable.combineLatest<KsOptional<RefTag?>, KsOptional<RefTag?>, KsOptional<Uri?>, Project, ProjectData>(
        refTag,
        cookieRefTag,
        fullDeeplink,
        currentProject
    ) { refTagFromIntent, refTagFromCookie, fullDeeplink, project ->
        PrelaunchProjectHelper.buildProjectData(refTagFromIntent, refTagFromCookie, fullDeeplink, project)
    }

    initialProject
        .subscribe {
            project.onNext(it)
        }.addToDisposable(disposables)

    initialProject
        .compose(TakeWhenTransformerV2(this.shareButtonClicked))
        .map {
            Pair(
                it.name(),
                UrlUtils.appendRefTag(
                    it.webProjectUrl(),
                    RefTag.projectShare().tag(),
                ),
            )
        }
        .subscribe {
            this.showShareSheet.onNext(it)
        }.addToDisposable(disposables)

    currentProjectData
        .distinctUntilChanged()
        .subscribe {
            this.projectData.onNext(it)
        }.addToDisposable(disposables)

    currentProject
        .compose(Transformers.takePairWhenV2(creatorInfoClicked))
        .map { it.first }
        .subscribe {
            startCreatorView.onNext(it)
        }.addToDisposable(disposables)

    loggedOutUserOnHeartClick
        .subscribe {
            this.startLoginToutActivity.onNext(Unit)
        }.addToDisposable(disposables)

    savedProjectOnLoginSuccess
        .doOnError { println(it.message) }
        .filter { it.isNotNull() }
        .map { it }
        .subscribe {
            this.project.onNext(it)
        }.addToDisposable(disposables)

    projectOnUserChangeSave
        .filter { it.isNotNull() }
        .map { it }
        .subscribe {
            this.project.onNext(it)
        }.addToDisposable(disposables)

    projectOnUserChangeSave
        .filter { p -> p.isStarred() }
        .subscribe {
            this.showSavedPrompt.onNext(Unit)
        }.addToDisposable(disposables)

    var previousScreen = ""
    this.intent
        .subscribe { previousScreen = it.getStringExtra(IntentKey.PREVIOUS_SCREEN) ?: "" }
        .addToDisposable(disposables)

    SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
        .sendThirdPartyEvent(
            loadedProject,
            apolloClient,
            currentUser = currentUser,
            eventName = ThirdPartyEventValues.EventName.SCREEN_VIEW,
            firebaseScreen = ThirdPartyEventValues.ScreenName.PRELAUNCH.value,
            firebasePreviousScreen = previousScreen,
        )
        .subscribe {
            onThirdPartyEventSent.onNext(it.first)
        }.addToDisposable(disposables)

    // Tracking
    currentProjectData
        .take(1)
        .subscribe { data ->
            if (data.refTagFromCookie() == null) {
                data.refTagFromIntent()?.let {
                    RefTagUtils.storeCookie(it, data.project(), this.cookieManager, this.sharedPreferences)
                }
            }
            val dataWithStoredCookieRefTag = PrelaunchProjectHelper.storeCurrentCookieRefTag(
                data,
                this.cookieManager,
                this.sharedPreferences
            )
            this.attributionEvents.trackProjectPageViewed(dataWithStoredCookieRefTag)
        }.addToDisposable(disposables)
}
