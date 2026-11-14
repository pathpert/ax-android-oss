package com.kickstarter.ui.compose.designsystem.videoplayer

import android.graphics.Matrix
import android.view.TextureView
import android.view.View
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

/** How many times the on-screen player will re-prepare after a *recoverable* error before giving up.
 * Bounded so a device that is genuinely over its decoder budget. */
internal const val MAX_PLAYBACK_RECOVERY_ATTEMPTS = 3

/** A reclaimed decoder or a transient network failure can succeed on a re-prepare;
 * format/source/DRM errors cannot, so we never retry (or loop on) those.*/
internal fun PlaybackException.isRecoverable(): Boolean = when (errorCode) {
    PlaybackException.ERROR_CODE_DECODING_RESOURCES_RECLAIMED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> true
    else -> false
}

/**
 * Applies a transformation matrix to the [TextureView] to emulate a "Center Crop" (RESIZE_MODE_ZOOM)
 * aspect ratio. This ensures the video fills the entire view area by scaling the smaller dimension
 * to fit, while cropping the overflow.
 *
 * @this textureView The [TextureView] to which the transformation matrix will be applied.
 * @param videoWidth The intrinsic width of the video source.
 * @param videoHeight The intrinsic height of the video source.
 * @param matrix A [Matrix] instance to write the transform into. Callers on a hot path (e.g. per
 * layout frame during the HideUI animation) should pass a reused instance to avoid per-frame
 * allocations; the [TextureView] copies the values internally, so the same instance is safe to reuse.
 */
fun TextureView.applyZoomMatrix(videoWidth: Int, videoHeight: Int, matrix: Matrix = Matrix()) {
    // - Guard against invalid dimensions to prevent division by zero
    if (videoWidth <= 0 || videoHeight <= 0 || width <= 0 || height <= 0) return

    val viewWidth = width.toFloat()
    val viewHeight = height.toFloat()

    val videoRatio = videoWidth.toFloat() / videoHeight
    val viewRatio = viewWidth / viewHeight

    val (scaleX, scaleY) = if (videoRatio > viewRatio) {
        // - Video is wider than the view: scale X to overflow (crop sides)
        (videoRatio / viewRatio) to 1f
    } else {
        // - Video is taller than the view: scale Y to overflow (crop top/bottom)
        1f to (viewRatio / videoRatio)
    }

    // - Optimization: Only apply if a transformation is actually needed
    if (scaleX == 1f && scaleY == 1f) {
        setTransform(null)
        return
    }

    // - Use internal center point for scaling. setScale replaces the matrix contents, so a reused
    //   instance needs no explicit reset.
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)

    setTransform(matrix)
}

/**
 * Applies a transformation matrix to the [TextureView] to emulate a "Fit / Contain"
 * (RESIZE_MODE_FIT) aspect ratio. The whole video frame stays visible, preserving its aspect
 * ratio, and any leftover space becomes letterbox bars. Used by the video feed's HideUI mode so
 * captions baked into the edges of the frame are never cropped.
 *
 * @this textureView The [TextureView] to which the transformation matrix will be applied.
 * @param videoWidth The intrinsic width of the video source.
 * @param videoHeight The intrinsic height of the video source.
 * @param matrix A [Matrix] instance to write the transform into. Callers on a hot path (e.g. per
 * layout frame during the HideUI animation) should pass a reused instance to avoid per-frame
 * allocations; the [TextureView] copies the values internally, so the same instance is safe to reuse.
 */
fun TextureView.applyFitMatrix(videoWidth: Int, videoHeight: Int, matrix: Matrix = Matrix()) {
    // - Guard against invalid dimensions to prevent division by zero
    if (videoWidth <= 0 || videoHeight <= 0 || width <= 0 || height <= 0) return

    val viewWidth = width.toFloat()
    val viewHeight = height.toFloat()

    val videoRatio = videoWidth.toFloat() / videoHeight
    val viewRatio = viewWidth / viewHeight

    // A [TextureView] stretches its surface to fill the view, so we scale the overflowing dimension
    // back down to restore the video's real aspect ratio (letterboxing the remainder).
    val (scaleX, scaleY) = if (videoRatio > viewRatio) {
        // - Video is wider than the view: keep full width, shrink height (letterbox top/bottom)
        1f to (viewRatio / videoRatio)
    } else {
        // - Video is taller than the view: keep full height, shrink width (letterbox sides)
        (videoRatio / viewRatio) to 1f
    }

    // - Optimization: Only apply if a transformation is actually needed
    if (scaleX == 1f && scaleY == 1f) {
        setTransform(null)
        return
    }

    // setScale replaces the matrix contents, so a reused instance needs no explicit reset.
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)

    setTransform(matrix)
}

/**
 * Holds the listeners attached to the player's [TextureView] so both can be detached together when
 * the [AndroidView] is released, avoiding a dangling [View.OnLayoutChangeListener].
 */
internal class TextureViewListeners(
    val playerListener: Player.Listener,
    val layoutListener: View.OnLayoutChangeListener
)
