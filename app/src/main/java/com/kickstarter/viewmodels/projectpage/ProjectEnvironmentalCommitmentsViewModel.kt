package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

@Deprecated(
    message = "Legacy monolithic path replaced by ProjectFeatureViewModel under com.kickstarter.features.projectpage.viewmodel.",
    replaceWith = ReplaceWith("ProjectFeatureViewModel", "com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel")
)
class ProjectEnvironmentalCommitmentsViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
        fun onVisitOurEnvironmentalResourcesCenterClicked()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        /** Emits the current list [EnvironmentalCommitment]. */
        fun projectEnvironmentalCommitment(): Observable<List<EnvironmentalCommitment>>

        fun openVisitOurEnvironmentalResourcesCenter(): Observable<String>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class ProjectEnvironmentalCommitmentsViewModel(private val environment: Environment) :
        ViewModel(),
        Inputs,
        Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val onVisitOurEnvironmentalResourcesCenterClicked = BehaviorSubject.create<Unit>()

        private val projectEnvironmentalCommitment = BehaviorSubject.create<List<EnvironmentalCommitment>>()
        private val openVisitOurEnvironmentalResourcesCenter = BehaviorSubject.create<String>()

        private val disposables = CompositeDisposable()

        init {
            val project = projectDataInput
                .map { it.project() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            project.map { pj ->
                pj.envCommitments()?.sortedBy { it.id }
            }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .subscribe { this.projectEnvironmentalCommitment.onNext(it) }
                .addToDisposable(disposables)

            onVisitOurEnvironmentalResourcesCenterClicked
                .subscribe {
                    this.openVisitOurEnvironmentalResourcesCenter.onNext(
                        UrlUtils
                            .appendPath(
                                environment.webEndpoint(),
                                ENVIROMENT
                            )
                    )
                }
                .addToDisposable(disposables)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)

        override fun onVisitOurEnvironmentalResourcesCenterClicked() =
            this.onVisitOurEnvironmentalResourcesCenterClicked.onNext(Unit)

        override fun projectEnvironmentalCommitment(): Observable<List<EnvironmentalCommitment>> = this.projectEnvironmentalCommitment
        override fun openVisitOurEnvironmentalResourcesCenter(): Observable<String> = this.openVisitOurEnvironmentalResourcesCenter
    }

    @Suppress("UNCHECKED_CAST")
    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectEnvironmentalCommitmentsViewModel(
                environment
            ) as T
        }
    }

    companion object {
        const val ENVIROMENT = "environment"
    }
}
