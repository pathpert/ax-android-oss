package com.kickstarter.features.videofeed.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.PlaybackException
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSVideoFeedSnackbar
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

enum class VideoFeedScreenTestTag {
    VIDEO_FEED_PAGER,
    VIDEO_FEED_OVERLAY_CONTAINER,
    VIDEO_FEED_CLOSE_BUTTON,
    VIDEO_FEED_HIDE_UI_BUTTON,
    VIDEO_FEED_LOADING_PAGE
}

// Stable pager key for the trailing pagination loading page. Project ids are positive,
// so this sentinel never collides with a real item key.
private const val LOADING_PAGE_KEY = Long.MIN_VALUE

@Composable
fun VideoFeedScreen(
    items: List<VideoFeedItem>,
    environment: Environment,
    errorSnackBarHostState: SnackbarHostState = SnackbarHostState(),
    hasMore: Boolean = true,
    isLoading: Boolean = false,
    onLoadMore: () -> Unit = {},
    onReachedLastVideo: () -> Unit = {},
    onClose: () -> Unit = {},
    onProfileClick: (project: Project, videoId: Long) -> Unit = { _, _ -> },
    onBookmarkClick: (project: Project, videoId: Long, index: Int) -> Unit = { _, _, _ -> },
    onShareIntentReady: (Intent) -> Unit = {},
    preLaunchedCallback: (project: Project, refTag: RefTag) -> Unit = { _, _ -> },
    projectCallback: (project: Project, refTag: RefTag) -> Unit = { _, _ -> },
    onVideoImpression: (item: VideoFeedItem, position: Int) -> Unit = { _, _ -> },
    onVideoPageSettled: (videoFeedItem: VideoFeedItem, toPosition: Int, fromVideoFeedItem: VideoFeedItem, watchTimeMs: Long?, videoDurationMs: Long?) -> Unit = { _, _, _, _, _ -> },
    onPlayPauseTap: (project: Project, videoId: Long, isPlaying: Boolean) -> Unit = { _, _, _ -> },
    onMuteToggleTap: (project: Project, videoId: Long, isMuted: Boolean) -> Unit = { _, _, _ -> },
    onProgressBarTap: (item: VideoFeedItem, progress: Float) -> Unit = { _, _ -> },
    onShareCTAClick: (project: Project, videoId: Long) -> Unit = { _, _ -> },
    shareSheetContent: (@Composable (project: Project, onDismiss: () -> Unit) -> Unit)? = null,
    onVideoPlaybackError: (item: VideoFeedItem, position: Int, error: PlaybackException, isActive: Boolean) -> Unit = { _, _, _, _ -> }
) {
    // Append a trailing loading page while the next page is being fetched. The prefetch in the
    // pagination LaunchedEffect usually completes before the user reaches the end, so this is only
    // seen when the user out-swipes the request (or during the very first load).
    val showLoadingPage = isLoading && hasMore
    val pagerState = rememberPagerState(pageCount = { items.size + if (showLoadingPage) 1 else 0 })

    var previousSettledPage by remember { mutableStateOf(-1) }
    var hasTriggeredReview by remember { mutableStateOf(false) }
    // Stores (watchTimeMs, videoDurationMs) per page index as each player deactivates.
    // Written by KSVideoPlayer.onBecameInactive during the swipe animation; read when
    // settledPage fires after the animation completes, so the data is always ready.
    val watchTimeByPage = remember { mutableMapOf<Int, Pair<Long, Long>>() }
    var shareProject: Project? by remember { mutableStateOf<Project?>(null) }

    var isMuted by rememberSaveable { mutableStateOf(false) }

    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val iconTopPadding = maxOf(dimensions.videoFeedCloseButtonTopPadding, statusBarInset + dimensions.paddingSmall)

    // - Pagination:
    // Threshold: items.size - (beyondViewportPageCount + 2)
    // Triggers before the pager pre-renders the last page, keeping at least one rendered while the next page loads.
    LaunchedEffect(pagerState.currentPage, items.size) {
        if (items.isNotEmpty() && pagerState.currentPage >= items.size - 3) {
            onLoadMore()
        }
    }

    // - In-app review: fire once when the user settles on the last video and there are no more pages.
    LaunchedEffect(pagerState.settledPage, items.size, hasMore) {
        if (!hasTriggeredReview && items.isNotEmpty() && !hasMore && pagerState.settledPage == items.size - 1) {
            hasTriggeredReview = true
            onReachedLastVideo()
        }
    }

    // - Analytics:
    // - Impression fires on every settled page, including first load.
    // - Page-viewed (swipe + watch data) fires only when navigating from a previous video.
    LaunchedEffect(pagerState.settledPage, items.isEmpty()) {
        val currentPage = pagerState.settledPage
        if (items.isEmpty() || currentPage >= items.size) return@LaunchedEffect

        onVideoImpression(items[currentPage], currentPage)

        if (previousSettledPage in items.indices && previousSettledPage != currentPage) {
            val watchData = watchTimeByPage.remove(previousSettledPage)
            onVideoPageSettled(
                items[currentPage],
                currentPage,
                items[previousSettledPage],
                watchData?.first,
                watchData?.second
            )
        }

        previousSettledPage = currentPage
    }

    val screenHazeState = rememberHazeState()

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = screenHazeState)
                .testTag(VideoFeedScreenTestTag.VIDEO_FEED_PAGER.name),
            state = pagerState,
            beyondViewportPageCount = 1,
            key = { index -> if (index < items.size) items[index].project.id() else LOADING_PAGE_KEY }
        ) { page ->

            if (page >= items.size) {
                VideoFeedLoadingPage()
                return@VerticalPager
            }

            val item = items[page]
            VideoFeedPageItem(
                item = item,
                page = page,
                isCurrentPage = pagerState.currentPage == page,
                isSettledPage = pagerState.settledPage == page,
                isMuted = isMuted,
                iconTopPadding = iconTopPadding,
                onMuteToggle = { muted ->
                    isMuted = muted
                    onMuteToggleTap(item.project, item.videoId, muted)
                },
                onPlayPauseTap = onPlayPauseTap,
                onProgressBarTap = onProgressBarTap,
                onBecameInactive = { watchTimeMs, videoDurationMs ->
                    watchTimeByPage[page] = Pair(watchTimeMs, videoDurationMs)
                },
                onPlaybackError = onVideoPlaybackError,
                onProfileClick = onProfileClick,
                onBookmarkClick = onBookmarkClick,
                onShareRequested = { proj -> shareProject = proj },
                onShareCTAClick = onShareCTAClick,
                onClose = onClose,
                preLaunchedCallback = preLaunchedCallback,
                projectCallback = projectCallback
            )
        }

        shareProject?.let { project ->
            shareSheetContent?.invoke(project) { shareProject = null }
        }

        SnackbarHost(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = dimensions.videoFeedSnackbarTopPadding,
                    start = dimensions.videoFeedSnackbarHorizontalMargin,
                    end = dimensions.videoFeedSnackbarHorizontalMargin
                ),
            hostState = errorSnackBarHostState,
            snackbar = { data ->
                KSVideoFeedSnackbar(text = data.visuals.message, hazeState = screenHazeState)
            }
        )
    }
}

@Preview
@Composable
fun VideoFeedScreenPreview() {
    val previewEnv = Environment.builder().build()
    KSTheme {
        VideoFeedScreen(
            environment = previewEnv,
            items = listOf(
                VideoFeedItem(
                    badges = listOf(KSVideoBadgeType.ProjectWeLove, KSVideoBadgeType.DaysLeft("3 days left")),
                    project = ProjectFactory.project(),
                    hlsUrl = null
                ),
                VideoFeedItem(
                    badges = listOf(KSVideoBadgeType.JustLaunched),
                    project = ProjectFactory.caProject(),
                    hlsUrl = null
                ),
                VideoFeedItem(
                    badges = listOf(KSVideoBadgeType.Trending),
                    project = ProjectFactory.ukProject(),
                    hlsUrl = null
                )
            )
        )
    }
}
