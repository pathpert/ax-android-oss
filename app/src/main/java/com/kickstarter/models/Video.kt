package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Immutable domain model representing video pitch stream and thumbnail URLs.
 *
 * ### Negative Constraints:
 * - Prefer [hls] adaptive streaming URL when available.
 */
@Parcelize
class Video private constructor(
    private val base: String?,
    private val frame: String?,
    private val high: String?,
    private val hls: String?,
    private val webm: String?
) : Parcelable {
    /** Base progressive stream URL. */
    fun base() = this.base

    /** Static preview frame image URL. */
    fun frame() = this.frame

    /** High-definition MP4 progressive stream URL. */
    fun high() = this.high

    /** HLS (HTTP Live Streaming) adaptive bitrate playlist URL. */
    fun hls() = this.hls

    /** WebM format stream URL. */
    fun webm() = this.webm

    @Parcelize
    data class Builder(
        private var base: String? = null,
        private var frame: String? = null,
        private var high: String? = null,
        private var hls: String? = null,
        private var webm: String? = null,
    ) : Parcelable {
        fun base(base: String?) = apply { this.base = base }
        fun frame(frame: String?) = apply { this.frame = frame }
        fun high(high: String?) = apply { this.high = high }
        fun hls(hls: String?) = apply { this.hls = hls }
        fun webm(webm: String?) = apply { this.webm = webm }

        fun build() = Video(
            base = base,
            frame = frame,
            high = high,
            hls = hls,
            webm = webm

        )
    }

    fun toBuilder() = Builder(
        base = base,
        frame = frame,
        high = high,
        hls = hls,
        webm = webm
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Video) {
            equals = base() == obj.base() &&
                frame() == obj.frame() &&
                high() == obj.high() &&
                hls() == obj.hls() &&
                webm() == obj.webm()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
