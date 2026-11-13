package com.kickstarter.features.projectpage.viewmodel

import androidx.lifecycle.viewModelScope
import com.kickstarter.features.projectpage.data.ProjectPageRepository
import com.kickstarter.features.projectpage.viewmodel.usecases.FetchProjectStoryUseCase
import com.kickstarter.features.projectpage.viewmodel.usecases.ProjectCreatorActionsUseCase
import com.kickstarter.features.projectpage.viewmodel.usecases.ProjectPledgeCtaUseCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.Observable

/**
 * Modern Feature ViewModel for the Project Page, delegating specialized
 * operations to autonomous use cases while managing lifecycle and state dispatch.
 */
open class ProjectFeatureViewModel(
    environment: Environment
) : ProjectPageStateHolder(environment) {

    private val repository = ProjectPageRepository(environment, viewModelScope, disposables)
    private val fetchProjectStoryUseCase = FetchProjectStoryUseCase(environment, this)
    private val projectPledgeCtaUseCase = ProjectPledgeCtaUseCase(environment, this)
    private val projectCreatorActionsUseCase = ProjectCreatorActionsUseCase(environment, this, repository)

    init {
        repository.progressBarIsGone
            .subscribe { retryProgressBarIsGone.onNext(it) }
            .addToDisposable(disposables)

        val mappedProjectNotification = repository.mapProjectNotification(
            intent = intent,
            reloadClicked = reloadProjectContainerClicked
        )

        val mappedProjectValues = mappedProjectNotification.compose(valuesV2())
        val mappedProjectErrors = mappedProjectNotification.compose(errorsV2())

        val initialProject = mappedProjectValues
            .filter { it.displayPrelaunch().isFalse() }

        val refreshProjectEvent = Observable.mergeArray(
            pledgeSuccessfullyCancelled,
            pledgeSuccessfullyCreated.compose(ignoreValuesV2()),
            pledgeSuccessfullyUpdated,
            pledgePaymentSuccessfullyUpdated,
            refreshProject
        )

        val refreshedProjectNotification = repository.refreshProjectNotification(
            initialProject = initialProject,
            refreshEvent = refreshProjectEvent
        )

        val projectSaveStreams = projectCreatorActionsUseCase.createProjectSaveStreams(initialProject)

        val currentProject = Observable.mergeArray(
            initialProject,
            refreshedProjectNotification.compose(valuesV2()),
            projectSaveStreams
        )

        repository.bindThirdPartyEvent(currentProject, intent, onThirdPartyEventSent)

        val cookieRefTag = repository.cookieRefTag(initialProject)
        val refTag = intent.flatMap { ProjectIntentMapper.refTag(it) }
        val fullDeeplink = intent.flatMap { Observable.just(KsOptional.of(it.data)) }

        val currentProjectData = repository.buildProjectData(
            refTag = refTag,
            cookieRefTag = cookieRefTag,
            fullDeeplink = fullDeeplink,
            currentProject = currentProject
        )

        currentProjectData
            .distinctUntilChanged()
            .subscribe { projectData.onNext(it) }
            .addToDisposable(disposables)

        repository.bindSecretRewardGroup(currentProjectData)

        fetchProjectStoryUseCase.wire(
            currentProject = currentProject,
            currentProjectData = currentProjectData
        )

        projectPledgeCtaUseCase.wire(
            currentProject = currentProject,
            currentProjectData = currentProjectData,
            mappedProjectErrors = mappedProjectErrors,
            mappedProjectValues = mappedProjectValues
        )

        projectCreatorActionsUseCase.wire(
            currentProject = currentProject,
            currentProjectData = currentProjectData
        )
    }

    override fun onCleared() {
        environment.apolloClientV2()?.cleanDisposables()
        disposables.clear()
        super.onCleared()
    }
}
