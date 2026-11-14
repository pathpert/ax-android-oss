package com.kickstarter.ui.compose.designsystem.videoplayer

import android.graphics.Matrix
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.initializeExoplayer
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay

enum class KSVideoPlayerTestTag {
    VIDEO_PLAYER_SURFACE,
    VIDEO_PLAYER_CONTROLS,
    VIDEO_PLAYER_PLAY_BUTTON,
    VIDEO_PLAYER_MUTE_BUTTON,
    VIDEO_PLAYER_FORWARD_BUTTON,
    VIDEO_PLAYER_REWIND_BUTTON,
    VIDEO_PLAYER_PROGRESS_BAR,
    VIDEO_PLAYER_POSTER
}

/**
 * A full-screen video player component that utilizes [ExoPlayer] to render video content.
 */
@Composable
fun KSVideoPlayer(
    videoUrl: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    hideUi: Boolean = false,
    previewImageUrl: String? = null,
    player: ExoPlayer? = null,
    isMuted: Boolean = false,
    overlayContent: @Composable BoxScope.(HazeState) -> Unit = {},
    onMuteToggle: (isMuted: Boolean) -> Unit = {},
    onPlayPauseToggle: (isPlaying: Boolean) -> Unit = {},
    onProgressBarInteraction: (currentProgress: Float) -> Unit = {},
    onBecameInactive: (watchTimeMs: Long, videoDurationMs: Long) -> Unit = { _, _ -> },
    onPlaybackError: (error: PlaybackException, isActive: Boolean) -> Unit = { _, _ -> }
) {
    if (videoUrl.isEmpty()) return
    val context = LocalContext.current

    val exoPlayer = remember(player ?: videoUrl) {
        player ?: context.initializeExoplayer().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    val firstFrameRenderedState = remember(player ?: videoUrl) { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isScrubbing by remember { mutableStateOf(false) }

    val videoWidthState = remember(player ?: videoUrl) { mutableIntStateOf(0) }
    val videoHeightState = remember(player ?: videoUrl) { mutableIntStateOf(0) }
    val hideUiState = rememberUpdatedState(hideUi)
    val transformMatrix = remember { Matrix() }

    var showControls by remember { mutableStateOf(false) }
    val hazeState = rememberHazeState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAppInForeground by remember { mutableStateOf(true) }

    val onPlayPauseToggleState = rememberUpdatedState(onPlayPauseToggle)
    val onProgressBarInteractionState = rememberUpdatedState(onProgressBarInteraction)
    val onBecameInactiveState = rememberUpdatedState(onBecameInactive)
    val onPlaybackErrorState = rememberUpdatedState(onPlaybackError)
    val isActiveState = rememberUpdatedState(isActive)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isAppInForeground = true
                Lifecycle.Event.ON_PAUSE -> isAppInForeground = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isActive, isAppInForeground) {
        val shouldPlay = isActive && isAppInForeground
        exoPlayer.playWhenReady = shouldPlay
        if (shouldPlay) {
            showControls = false
            while (true) {
                if (!isScrubbing) {
                    val duration = exoPlayer.duration
                    if (duration > 0) {
                        progress = exoPlayer.currentPosition.toFloat() / duration
                    }
                }
                delay(500)
            }
        }
    }

    LaunchedEffect(exoPlayer, isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    val onToggleControls = remember(exoPlayer) {
        {
            showControls = !showControls
            if (showControls) exoPlayer.pause()
            else exoPlayer.play()
            onPlayPauseToggleState.value(!showControls)
        }
    }

    val onSeek = remember(exoPlayer) {
        { newProgress: Float ->
            progress = newProgress
            val duration = exoPlayer.duration
            if (duration > 0) {
                exoPlayer.seekTo((duration * newProgress).toLong())
            }
        }
    }

    val onScrubStart = remember(exoPlayer) {
        {
            isScrubbing = true
            exoPlayer.pause()
            val progressAtInteraction = if (exoPlayer.duration > 0L) {
                exoPlayer.currentPosition.toFloat() / exoPlayer.duration
            } else 0f
            onProgressBarInteractionState.value(progressAtInteraction)
        }
    }

    val onScrubEnd = remember(exoPlayer) {
        {
            isScrubbing = false
            if (!showControls) exoPlayer.play()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = stringResource(id = if (showControls) R.string.accessibility_discovery_buttons_close else R.string.Play)
            ) {
                onToggleControls()
            }
    ) {
        VideoSurfaceView(
            hideUi = hideUi,
            hideUiState = hideUiState,
            previewImageUrl = previewImageUrl,
            exoPlayer = exoPlayer,
            hazeState = hazeState,
            transformMatrix = transformMatrix,
            videoWidthState = videoWidthState,
            videoHeightState = videoHeightState,
            firstFrameRenderedState = firstFrameRenderedState
        )

        ControlsContainer(
            modifier = Modifier.align(Alignment.Center),
            showControls = showControls,
            hazeState = hazeState,
            playPauseCallback = onToggleControls
        )

        MuteButton(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = dimensions.videoFeedMuteButtonCenterOffset),
            visible = showControls,
            isMuted = isMuted,
            hazeState = hazeState,
            onClick = { onMuteToggle(!isMuted) }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                overlayContent(hazeState)
            }

            if (hideUi) {
                Spacer(modifier = Modifier.height(dimensions.videoFeedHiddenModeScrubBarSpacing))
            }

            ProgressBarContainer(
                modifier = Modifier,
                progressProvider = { progress },
                onSeek = onSeek,
                onScrubStart = onScrubStart,
                onScrubEnd = onScrubEnd,
                showThumb = showControls
            )
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { if (player == null) exoPlayer.release() }
    }

    DisposableEffect(exoPlayer) {
        var recoveryAttempts = 0
        val errorListener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                onPlaybackErrorState.value(error, isActiveState.value)
                if (isActiveState.value && error.isRecoverable() && recoveryAttempts < MAX_PLAYBACK_RECOVERY_ATTEMPTS) {
                    recoveryAttempts++
                    exoPlayer.prepare()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) recoveryAttempts = 0
            }
        }
        exoPlayer.addListener(errorListener)
        onDispose { exoPlayer.removeListener(errorListener) }
    }

    DisposableEffect(isActive) {
        onDispose {
            if (isActive) {
                onBecameInactiveState.value(
                    exoPlayer.currentPosition,
                    exoPlayer.duration.coerceAtLeast(0L)
                )
            }
        }
    }
}
