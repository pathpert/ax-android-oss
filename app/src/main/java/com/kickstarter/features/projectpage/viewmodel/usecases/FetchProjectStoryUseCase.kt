package com.kickstarter.features.projectpage.viewmodel.usecases

import com.kickstarter.features.projectpage.data.PagerTabConfig
import com.kickstarter.features.projectpage.viewmodel.ProjectPageStateHolder
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isUIEmptyValues
import com.kickstarter.models.Project
import com.kickstarter.ui.data.MediaElement
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.data.VideoModelElement
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Use case managing project story elements, photo and video media playback,
 * fullscreen transitions, and dynamic pager tabs (AI Disclosure & Environment).
 */
class FetchProjectStoryUseCase(
    private val environment: Environment,
    private val state: ProjectPageStateHolder
) {
    fun wire(
        currentProject: Observable<Project>,
        currentProjectData: Observable<ProjectData>
    ) {
        val disposables = state.disposables

        val projectPhoto = currentProject
            .filter { it.photo()?.full().isNotNull() }
            .map { requireNotNull(it.photo()?.full()) }

        val projectVideo = currentProject
            .map { it.video()?.hls() ?: it.video()?.high() ?: "" }
            .distinctUntilChanged()
            .take(1)

        projectPhoto
            .compose(combineLatestPair(projectVideo))
            .subscribe {
                state.projectMedia.onNext(MediaElement(VideoModelElement(it.second), it.first))
            }
            .addToDisposable(disposables)

        currentProject
            .map { it.hasVideo() }
            .subscribe { state.playButtonIsVisible.onNext(it) }
            .addToDisposable(disposables)

        state.onVideoPlayButtonClicked
            .distinctUntilChanged()
            .subscribe {
                state.backingViewGroupIsVisible.onNext(false)
            }
            .addToDisposable(disposables)

        state.fullScreenVideoButtonClicked
            .subscribe { state.onOpenVideoInFullScreen.onNext(it) }
            .addToDisposable(disposables)

        state.closeFullScreenVideo
            .subscribe { state.updateVideoCloseSeekPosition.onNext(it) }
            .addToDisposable(disposables)

        state.tabSelected
            .map { it != 0 }
            .subscribe { state.hideVideoPlayer.onNext(it) }
            .addToDisposable(disposables)

        currentProjectData
            .compose(takePairWhenV2(state.tabSelected))
            .distinctUntilChanged()
            .delay(150, TimeUnit.MILLISECONDS, environment.schedulerV2())
            .subscribe {
                state.projectData.onNext(it.first)
            }
            .addToDisposable(disposables)

        currentProjectData
            .distinctUntilChanged()
            .subscribe { data ->
                val showEnvironmentalTab = data.project().envCommitments()?.isNotEmpty() ?: false
                val tabConfigEnv = PagerTabConfig(
                    ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT,
                    showEnvironmentalTab
                )

                val showAiTab = data.project().aiDisclosure()
                    ?.let { disclosure -> !disclosure.isUIEmptyValues() } ?: false
                val tabConfigAi = PagerTabConfig(ProjectPagerTabs.USE_OF_AI, showAiTab)

                state.updateTabs.onNext(listOf(tabConfigAi, tabConfigEnv))
            }
            .addToDisposable(disposables)
    }
}
