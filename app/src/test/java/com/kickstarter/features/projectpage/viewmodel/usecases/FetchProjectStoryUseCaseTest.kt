package com.kickstarter.features.projectpage.viewmodel.usecases

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.projectpage.viewmodel.ProjectPageStateHolder
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.PhotoFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.VideoFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class FetchProjectStoryUseCaseTest : KSRobolectricTestCase() {

    private class ConcreteProjectPageStateHolder(env: Environment) : ProjectPageStateHolder(env)

    private lateinit var state: ConcreteProjectPageStateHolder
    private lateinit var useCase: FetchProjectStoryUseCase
    private val currentProject = PublishSubject.create<Project>()
    private val currentProjectData = PublishSubject.create<ProjectData>()

    @Before
    override fun setUp() {
        super.setUp()
        state = ConcreteProjectPageStateHolder(environment())
        useCase = FetchProjectStoryUseCase(environment(), state)
        useCase.wire(currentProject, currentProjectData)
    }

    @Test
    fun testProjectMedia_whenVideoAndPhotoPresent() {
        val mediaObserver = state.projectMedia().test()
        val playButtonObserver = state.playButtonIsVisible().test()

        val photo = PhotoFactory.photo()
        val video = VideoFactory.video()
        val project = ProjectFactory.project().toBuilder()
            .photo(photo)
            .video(video)
            .build()

        currentProject.onNext(project)

        mediaObserver.assertValueCount(1)
        val media = mediaObserver.values().first()
        assertEquals(photo.full(), media.thumbnailUrl)
        playButtonObserver.assertValue(true)
    }

    @Test
    fun testPlayButtonVisibility_whenNoVideo() {
        val playButtonObserver = state.playButtonIsVisible().test()
        val project = ProjectFactory.project().toBuilder()
            .video(null)
            .build()

        currentProject.onNext(project)

        playButtonObserver.assertValue(false)
    }

    @Test
    fun testOnVideoPlayButtonClicked_hidesBackingViewGroup() {
        val backingViewObserver = state.backingViewGroupIsVisible().test()

        state.onVideoPlayButtonClicked()

        backingViewObserver.assertValue(false)
    }

    @Test
    fun testTabSelected_hidesVideoPlayerWhenNotOverview() {
        val hideVideoObserver = state.hideVideoPlayer().test()

        state.tabSelected(0) // Overview
        hideVideoObserver.assertValue(false)

        state.tabSelected(1) // Campaign
        hideVideoObserver.assertValues(false, true)
    }

    @Test
    fun testFullScreenVideoInteractions() {
        val openVideoObserver = state.onOpenVideoInFullScreen().test()
        val closeSeekObserver = state.updateVideoCloseSeekPosition().test()

        state.fullScreenVideoButtonClicked(kotlin.Pair("url", 5000L))
        state.closeFullScreenVideo(5000L)

        openVideoObserver.assertValue(kotlin.Pair("url", 5000L))
        closeSeekObserver.assertValue(5000L)
    }
}
