package com.kickstarter.ui.compose.designsystem.videoplayer

import android.graphics.Matrix
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * UI component rendering VideoSurfaceView.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Composable
internal fun VideoSurfaceView(
    hideUi: Boolean,
    hideUiState: State<Boolean>,
    previewImageUrl: String?,
    exoPlayer: ExoPlayer,
    hazeState: HazeState,
    transformMatrix: Matrix,
    videoWidthState: MutableIntState,
    videoHeightState: MutableIntState,
    firstFrameRenderedState: MutableState<Boolean>
) {
    val context = LocalContext.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val hideUiProgress by animateFloatAsState(
            targetValue = if (hideUi) 1f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "hideUiProgress"
        )

        val fullWidth = maxWidth
        val fullHeight = maxHeight

        val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val iconRowTop = maxOf(dimensions.videoFeedCloseButtonTopPadding, statusBarInset + dimensions.paddingSmall)
        val topReserve = iconRowTop + (dimensions.videoFeedHiddenModeTopReserve - dimensions.videoFeedCloseButtonTopPadding)
        val bottomReserve = maxOf(dimensions.videoFeedScrubBarBottomPadding, navBarInset) +
            (dimensions.videoFeedHiddenModeBottomReserve - dimensions.videoFeedScrubBarBottomPadding)
        val availableHeight = (fullHeight - topReserve - bottomReserve).coerceAtLeast(0.dp)
        val inset = dimensions.videoFeedHiddenModeHorizontalInset
        val widthConstrained = (fullWidth - inset * 2).coerceAtLeast(0.dp)
        val nineToSixteenHeight = widthConstrained * (16f / 9f)
        val (cardWidth, cardHeight) = if (nineToSixteenHeight <= availableHeight) {
            widthConstrained to nineToSixteenHeight
        } else {
            (availableHeight * (9f / 16f)) to availableHeight
        }

        val surfaceWidth = lerp(fullWidth, cardWidth, hideUiProgress)
        val surfaceHeight = lerp(fullHeight, cardHeight, hideUiProgress)
        val cornerRadius = lerp(0.dp, dimensions.videoFeedHiddenModeCornerRadius, hideUiProgress)
        val cardCenterY = topReserve + availableHeight / 2f
        val verticalShift = lerp(0.dp, cardCenterY - fullHeight / 2f, hideUiProgress)

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = verticalShift)
                .size(width = surfaceWidth, height = surfaceHeight)
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        val playerListener = object : Player.Listener {
                            override fun onVideoSizeChanged(videoSize: VideoSize) {
                                if (videoSize.width > 0 && videoSize.height > 0) {
                                    videoWidthState.intValue = videoSize.width
                                    videoHeightState.intValue = videoSize.height
                                }
                            }

                            override fun onRenderedFirstFrame() {
                                firstFrameRenderedState.value = true
                            }
                        }
                        val layoutListener = View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                            val textureView = v as? TextureView ?: return@OnLayoutChangeListener
                            val w = videoWidthState.intValue
                            val h = videoHeightState.intValue
                            if (w > 0 && h > 0) {
                                if (hideUiState.value) textureView.applyFitMatrix(w, h, transformMatrix)
                                else textureView.applyZoomMatrix(w, h, transformMatrix)
                            }
                        }
                        tag = TextureViewListeners(playerListener, layoutListener)
                        exoPlayer.setVideoTextureView(this)
                        exoPlayer.addListener(playerListener)
                        addOnLayoutChangeListener(layoutListener)
                    }
                },
                update = { view ->
                    val w = videoWidthState.intValue
                    val h = videoHeightState.intValue
                    if (w > 0 && h > 0) {
                        if (hideUi) view.applyFitMatrix(w, h, transformMatrix)
                        else view.applyZoomMatrix(w, h, transformMatrix)
                    }
                },
                onRelease = { view ->
                    (view.tag as? TextureViewListeners)?.let {
                        exoPlayer.removeListener(it.playerListener)
                        view.removeOnLayoutChangeListener(it.layoutListener)
                    }
                    exoPlayer.clearVideoTextureView(view)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            )

            AnimatedVisibility(
                visible = !firstFrameRenderedState.value && !previewImageUrl.isNullOrEmpty(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = remember(previewImageUrl) {
                        ImageRequest.Builder(context)
                            .data(previewImageUrl)
                            .crossfade(true)
                            .build()
                    },
                    contentDescription = null,
                    contentScale = if (hideUi) ContentScale.Fit else ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(KSVideoPlayerTestTag.VIDEO_PLAYER_POSTER.name)
                )
            }
        }
    }
}
