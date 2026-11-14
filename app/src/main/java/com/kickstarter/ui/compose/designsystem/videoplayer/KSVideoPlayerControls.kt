package com.kickstarter.ui.compose.designsystem.videoplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSControlIcon
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSVideoScrubBar
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Mute
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Play
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.VolumeUp
import dev.chrisbanes.haze.HazeState

/**
 * A composable that displays a scrub bar for the video player with a draggable playhead.
 *
 * It uses [KSVideoScrubBar] to provide a progress track with a visible thumb circle
 * that supports both tap-to-seek and drag-to-scrub gestures.
 *
 * @param modifier The [Modifier] to be applied to the container.
 * @param progressProvider A lambda that returns the current video progress as a [Float] between 0.0 and 1.0.
 * @param onSeek A callback invoked when the user seeks to a new position, providing the new progress value.
 */
@Composable
internal fun ProgressBarContainer(
    modifier: Modifier,
    progressProvider: () -> Float,
    onSeek: (Float) -> Unit = {},
    onScrubStart: () -> Unit = {},
    onScrubEnd: () -> Unit = {},
    showThumb: Boolean = true
) {
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    KSVideoScrubBar(
        progress = progressProvider(),
        onSeek = onSeek,
        onScrubStart = onScrubStart,
        onScrubEnd = onScrubEnd,
        modifier = modifier
            .padding(bottom = maxOf(dimensions.videoFeedScrubBarBottomPadding, navBarInset))
            .padding(horizontal = dimensions.paddingMedium)
            .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name),
        activeColor = Color.White,
        trackColor = KSTheme.colors.grey_05,
        showThumb = showThumb
    )
}

@Composable
internal fun ControlsContainer(
    modifier: Modifier,
    showControls: Boolean,
    hazeState: HazeState? = null,
    playPauseCallback: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = showControls,
        enter = fadeIn() + scaleIn(
            initialScale = 0.6f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            )
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_CONTROLS.name)
                .pointerInput(Unit) {} // Stops click propagation
        ) {
            KSControlIcon(
                icon = Play,
                size = 80.dp,
                onClick = {
                    playPauseCallback.invoke()
                },
                hazeState = hazeState,
                modifier = Modifier.testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PLAY_BUTTON.name),
                contentDescription = stringResource(id = R.string.Play)
            )
        }
    }
}

/**
 * Glassmorphism mute/unmute toggle shown over the video. The icon reflects the current audio state
 * ([Mute] when muted, [VolumeUp] when unmuted) and the accessibility label describes the action the
 * tap performs. It animates in/out via [visible] using the same transition as the play/pause
 * control, so the two appear and disappear together.
 */
@Composable
internal fun MuteButton(
    visible: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(
            initialScale = 0.6f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            )
        ),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        val label = stringResource(id = if (isMuted) R.string.fpo_Unmute else R.string.fpo_Mute)
        KSControlIcon(
            icon = if (isMuted) Mute else VolumeUp,
            size = dimensions.videoFeedMuteButtonSize,
            onClick = onClick,
            hazeState = hazeState,
            contentDescription = label,
            onClickLabel = label,
            modifier = Modifier.testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_MUTE_BUTTON.name)
        )
    }
}

@Composable
@Preview(widthDp = 300, heightDp = 200)
fun ControlsPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KSTheme.colors.kds_black),
    ) {
        ControlsContainer(
            modifier = Modifier.align(Alignment.Center),
            showControls = true
        )
    }
}

@Composable
@Preview(widthDp = 300, heightDp = 200)
fun ProgressBarPreview() {
    KSTheme {
        Column(
            modifier = Modifier.padding(dimensions.paddingSmall),
        ) {
            ProgressBarContainer(
                modifier = Modifier.fillMaxWidth(),
                progressProvider = { 0.1f }
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            ProgressBarContainer(
                modifier = Modifier.fillMaxWidth(),
                progressProvider = { 0.5f }
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            ProgressBarContainer(
                modifier = Modifier.fillMaxWidth(),
                progressProvider = { 0.9f }
            )
        }
    }
}
