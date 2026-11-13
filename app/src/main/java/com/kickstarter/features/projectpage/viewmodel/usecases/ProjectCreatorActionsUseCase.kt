package com.kickstarter.features.projectpage.viewmodel.usecases

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.features.projectpage.data.ProjectBackingDetailsMapper
import com.kickstarter.features.projectpage.data.ProjectPageRepository
import com.kickstarter.features.projectpage.viewmodel.ProjectPageStateHolder
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.OVERVIEW
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.metadataForProject
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Use case coordinating project save/watch interactions, creator messaging,
 * deep linking (comments and updates), native/social share sheets, and analytics.
 */
class ProjectCreatorActionsUseCase(
    private val environment: Environment,
    private val state: ProjectPageStateHolder,
    private val repo: ProjectPageRepository
) {
    private val currentUser = requireNotNull(environment.currentUserV2())
    private val analyticEvents = requireNotNull(environment.analytics())
    private val attributionEvents = requireNotNull(environment.attributionEvents())

    fun createProjectSaveStreams(initialProject: Observable<Project>): Observable<Project> {
        val disposables = state.disposables

        val loggedInUserOnHeartClick = currentUser.observable()
            .filter { it.isPresent() }
            .map { it.getValue() }
            .compose<User>(takeWhenV2(state.heartButtonClicked))

        val loggedOutUserOnHeartClick = currentUser.observable()
            .compose(takeWhenV2(state.heartButtonClicked))
            .filter { !it.isPresent() }

        loggedOutUserOnHeartClick
            .compose(ignoreValuesV2())
            .subscribe { state.startLoginToutActivity.onNext(it) }
            .addToDisposable(disposables)

        val projectOnUserChangeSave = initialProject
            .compose(takeWhenV2<Project, User>(loggedInUserOnHeartClick))
            .withLatestFrom(state.projectData) { initProject, latestProjectData ->
                if (latestProjectData.project().isStarred() != initProject.isStarred()) {
                    latestProjectData.project()
                } else {
                    initProject
                }
            }
            .switchMap { repo.toggleProjectSave(it) }
            .share()

        val savedProjectOnLoginSuccess = state.startLoginToutActivity
            .compose<Pair<Unit, KsOptional<User>>>(combineLatestPair(currentUser.observable()))
            .filter { su -> su.second.isPresent() }
            .withLatestFrom<Project, Project>(initialProject) { _, p -> p }
            .take(1)
            .switchMap { repo.saveProject(it) }
            .share()

        val saveProjectFromDeepLinkActivity = state.intent
            .take(1)
            .delay(3, TimeUnit.SECONDS, environment.schedulerV2())
            .filter { it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_SAVE, false) }
            .flatMap { ProjectIntentMapper.deepLinkSaveFlag(it) }

        val saveProjectFromDeepUrl = state.intent
            .take(1)
            .delay(3, TimeUnit.SECONDS, environment.schedulerV2())
            .filter { it.data.isNotNull() }
            .map { requireNotNull(it.data) }
            .filter { ProjectIntentMapper.hasSaveQueryFromUri(it) }
            .map { UrlUtils.saveFlag(it.toString()) }
            .filter { it.isNotNull() }
            .map { requireNotNull(it) }

        val projectOnDeepLinkChangeSave =
            Observable.merge(saveProjectFromDeepLinkActivity, saveProjectFromDeepUrl)
                .compose(combineLatestPair(currentUser.observable()))
                .filter { it.second.isPresent() }
                .withLatestFrom(initialProject) { userAndFlag, p -> Pair(userAndFlag, p) }
                .take(1)
                .filter { it.second.isStarred() != it.first.first }
                .switchMap {
                    if (it.first.first) repo.saveProject(it.second) else repo.unSaveProject(it.second)
                }
                .share()

        val projectSavedStatus = Observable.merge(
            projectOnUserChangeSave,
            savedProjectOnLoginSuccess,
            projectOnDeepLinkChangeSave
        )

        projectSavedStatus
            .subscribe { analyticEvents.trackWatchProjectCTA(it, PROJECT) }
            .addToDisposable(disposables)

        projectSavedStatus
            .filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
            .compose(ignoreValuesV2())
            .subscribe { state.showSavedPrompt.onNext(it) }
            .addToDisposable(disposables)

        return projectSavedStatus
    }

    fun wire(
        currentProject: Observable<Project>,
        currentProjectData: Observable<ProjectData>
    ) {
        val disposables = state.disposables

        currentProject
            .map { p -> if (p.isStarred()) R.drawable.icon__heart else R.drawable.icon__heart_outline }
            .distinctUntilChanged()
            .subscribe { state.heartDrawableId.onNext(it) }
            .addToDisposable(disposables)

        currentProject
            .compose(takeWhenV2(state.contactCreatorClicked))
            .subscribe { state.startMessagesActivity.onNext(it) }
            .addToDisposable(disposables)

        currentProject
            .compose(takeWhenV2(state.shareButtonClicked))
            .subscribe { project ->
                if (state.isNewSocialShareEnabled()) {
                    state.showSocialShareSheet.onNext(project)
                } else {
                    state.showShareSheet.onNext(
                        Pair(
                            project.name(),
                            UrlUtils.appendRefTag(project.webProjectUrl(), RefTag.projectShare().tag())
                        )
                    )
                }
            }
            .addToDisposable(disposables)

        val latestProjectAndProjectData =
            currentProject.compose<Pair<Project, ProjectData>>(combineLatestPair(state.projectData))

        state.intent
            .take(1)
            .delay(3, TimeUnit.SECONDS, environment.schedulerV2())
            .filter {
                it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, false) &&
                    it.getStringExtra(IntentKey.COMMENT)?.isEmpty() ?: true
            }
            .withLatestFrom(latestProjectAndProjectData) { _, project -> project }
            .map { it.second }
            .subscribe { state.startRootCommentsActivity.onNext(it) }
            .addToDisposable(disposables)

        state.intent
            .take(1)
            .delay(3, TimeUnit.SECONDS, environment.schedulerV2())
            .filter {
                it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, false) &&
                    it.getStringExtra(IntentKey.COMMENT)?.isNotEmpty() ?: false
            }
            .withLatestFrom(latestProjectAndProjectData) { intent, project ->
                Pair(intent.getStringExtra(IntentKey.COMMENT) ?: "", project)
            }
            .map { Pair(it.first, it.second.second) }
            .subscribe { state.startRootCommentsForCommentsThreadActivity.onNext(it) }
            .addToDisposable(disposables)

        state.intent
            .take(1)
            .delay(3, TimeUnit.SECONDS, environment.schedulerV2())
            .filter {
                it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE)?.isNotEmpty() ?: false &&
                    it.getStringExtra(IntentKey.COMMENT)?.isEmpty() ?: true
            }
            .map {
                Pair(
                    it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE) ?: "",
                    it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE_COMMENT, false)
                )
            }
            .withLatestFrom(latestProjectAndProjectData) { updateId, project ->
                Pair(updateId, project)
            }
            .subscribe { state.startProjectUpdateActivity.onNext(it) }
            .addToDisposable(disposables)

        state.intent
            .take(1)
            .delay(3, TimeUnit.SECONDS, environment.schedulerV2())
            .filter {
                it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE)?.isNotEmpty() ?: false &&
                    it.getStringExtra(IntentKey.COMMENT)?.isNotEmpty() ?: false
            }
            .map {
                Pair(
                    it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE) ?: "",
                    it.getStringExtra(IntentKey.COMMENT) ?: ""
                )
            }
            .withLatestFrom(latestProjectAndProjectData) { updateId, project ->
                Pair(updateId, project)
            }
            .subscribe { state.startProjectUpdateToRepliesDeepLinkActivity.onNext(it) }
            .addToDisposable(disposables)

        val currentFullProjectData = currentProjectData.filter { it.project().hasRewards() }

        val fullProjectDataAndCurrentUser = currentFullProjectData
            .compose<Pair<ProjectData, KsOptional<User>>>(combineLatestPair(currentUser.observable()))

        val fullProjectDataAndPledgeFlowContext = fullProjectDataAndCurrentUser
            .map { Pair(it.first, ProjectBackingDetailsMapper.pledgeFlowContext(it.first.project(), it.second.getValue())) }

        fullProjectDataAndPledgeFlowContext
            .take(1)
            .subscribe { projectDataAndPledgeFlowContext ->
                val data = projectDataAndPledgeFlowContext.first
                repo.storeCookieIfAbsent(data)
                val dataWithStoredCookieRefTag = repo.storeCurrentCookieRefTag(data)
                analyticEvents.trackProjectScreenViewed(dataWithStoredCookieRefTag, OVERVIEW.contextName)
                attributionEvents.trackProjectPageViewed(dataWithStoredCookieRefTag)
            }
            .addToDisposable(disposables)

        fullProjectDataAndPledgeFlowContext
            .map { it.first }
            .take(1)
            .compose(takePairWhenV2(state.tabSelected))
            .distinctUntilChanged()
            .subscribe {
                analyticEvents.trackProjectPageTabChanged(
                    it.first,
                    ProjectBackingDetailsMapper.getSelectedTabContextName(it.second)
                )
            }
            .addToDisposable(disposables)

        fullProjectDataAndPledgeFlowContext
            .compose<Pair<ProjectData, PledgeFlowContext?>>(takeWhenV2(state.nativeProjectActionButtonClicked))
            .filter { it.first.project().isLive && !it.first.project().isBacking() }
            .subscribe {
                analyticEvents.trackPledgeInitiateCTA(it.first)
            }
            .addToDisposable(disposables)

        currentProject
            .map { it.metadataForProject() }
            .map { com.kickstarter.libs.utils.extensions.ProjectMetadata.BACKING == it }
            .distinctUntilChanged()
            .subscribe { state.backingViewGroupIsVisible.onNext(it) }
            .addToDisposable(disposables)
    }
}
