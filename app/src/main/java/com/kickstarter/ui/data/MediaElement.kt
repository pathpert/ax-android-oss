package com.kickstarter.ui.data

/**
 * UI component for MediaElement.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
data class MediaElement(
    val videoModelElement: VideoModelElement?,
    val thumbnailUrl: String?
)

data class VideoModelElement(
    val sourceUrl: String?,
    val seekPosition: Long = 0L
)
