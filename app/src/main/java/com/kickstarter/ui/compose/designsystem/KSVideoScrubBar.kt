package com.kickstarter.ui.compose.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

enum class KSVideoScrubBarTestTag {
    SCRUB_BAR_CONTAINER,
    SCRUB_BAR_THUMB
}

/**
 * A video scrub bar with a draggable playhead thumb for seeking through video content.
 *
 * Features:
 * - Displays a linear progress track with a circular thumb indicator
 * - Supports tap-to-seek and horizontal drag-to-scrub gestures
 * - The thumb scales up with a spring animation while actively scrubbing for tactile feedback
 * - All motion uses spring-based physics for smooth, fluid interactions
 *
 * @param progress The current video progress as a [Float] between 0.0 and 1.0.
 * @param onSeek A callback invoked when the user seeks to a new position, providing the new progress value.
 * @param onScrubStart A callback invoked when the user begins a scrub gesture (touch down).
 * @param onScrubEnd A callback invoked when the user finishes a scrub gesture (touch released).
 * @param modifier The [Modifier] to be applied to the scrub bar container.
 * @param trackHeight The height of the progress track.
 * @param thumbSize The diameter of the playhead thumb circle.
 * @param thumbScaleOnDrag The scale multiplier applied to the thumb while actively dragging.
 * @param activeColor The color of the filled progress track and the thumb.
 * @param trackColor The color of the unfilled portion of the track.
 */
@Composable
fun KSVideoScrubBar(
    modifier: Modifier = Modifier,
    progress: Float,
    onSeek: (Float) -> Unit,
    onScrubStart: () -> Unit = {},
    onScrubEnd: () -> Unit = {},
    trackHeight: Dp = dimensions.videoFeedScrubBarTrackHeight,
    thumbSize: Dp = dimensions.videoFeedScrubBarThumbSize,
    thumbScaleOnDrag: Float = 1.4f,
    activeColor: Color = Color.White,
    trackColor: Color = colors.grey_05,
    showThumb: Boolean = true
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    var trackWidthPx by remember { mutableFloatStateOf(0f) }

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) thumbScaleOnDrag else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "thumbScale"
    )

    val thumbAlpha by animateFloatAsState(
        targetValue = if (showThumb || isDragging) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "thumbAlpha"
    )

    val displayProgress = if (isDragging) dragProgress else progress

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.clickableButtonHeight) // Standard touch target
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .testTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(displayProgress, 0f..1f)
                setProgress { targetProgress ->
                    onSeek(targetProgress)
                    true
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    // - detect touch down for scale animation
                    val down = awaitFirstDown()
                    down.consume()
                    isDragging = true
                    onScrubStart()
                    dragProgress = (down.position.x / size.width).coerceIn(0f, 1f)
                    onSeek(dragProgress)

                    // - Track drag movement until pointer is released
                    drag(down.id) { change ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek(dragProgress)
                    }

                    isDragging = false
                    onScrubEnd()
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // - Progress track
        KSLinearProgressIndicator(
            progress = displayProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(CircleShape),
            color = activeColor,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )

        // playhead
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .testTag(KSVideoScrubBarTestTag.SCRUB_BAR_THUMB.name)
                    .size(thumbSize)
                    .graphicsLayer {
                        scaleX = thumbScale
                        scaleY = thumbScale
                        alpha = thumbAlpha
                        translationX = displayProgress * trackWidthPx - (size.width / 2f)
                    }
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(activeColor)
            )
        }
    }
}

@Composable
@Preview(name = "Scrub Bar States", widthDp = 300)
fun KSVideoScrubBarPreview() {
    KSTheme {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(dimensions.paddingMedium)
        ) {
            KSVideoScrubBar(
                progress = 0.0f,
                onSeek = {}
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoScrubBar(
                progress = 0.35f,
                onSeek = {}
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoScrubBar(
                progress = 0.75f,
                onSeek = {}
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoScrubBar(
                progress = 1.0f,
                onSeek = {}
            )
        }
    }
}
