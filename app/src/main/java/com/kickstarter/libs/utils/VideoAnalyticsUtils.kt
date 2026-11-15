package com.kickstarter.libs.utils

import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.libs.utils.EventContextValues.VideoContextName.LENGTH
import com.kickstarter.libs.utils.EventContextValues.VideoContextName.POSITION
import java.util.HashMap

/**
 * Analytics property builders for video playback and video feed events.
 */
object VideoAnalyticsUtils {

    @JvmOverloads
    fun videoProperties(videoLength: Long, videoPosition: Long, prefix: String = "video_"): Map<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put(LENGTH.contextName, videoLength)
            put(POSITION.contextName, videoPosition)
        }
        return MapUtils.prefixKeys(properties, prefix)
    }

    fun videoFeedItemProperties(item: VideoFeedItem, position: Int): Map<String, Any> {
        return hashMapOf(
            "video_feed_video_id" to item.videoId.toString(),
            "video_feed_project_id" to item.project.id().toString(),
            "video_feed_position_in_session" to position
        )
    }

    fun videoFeedPageViewedProperties(
        item: VideoFeedItem,
        toPosition: Int,
        fromVideoFeedItem: VideoFeedItem? = null,
        watchTimeMs: Long? = null,
        videoDurationMs: Long? = null,
        entrySurface: String = ""
    ): Map<String, Any> {
        val props = HashMap<String, Any>()
        props.putAll(videoFeedItemProperties(item, toPosition))
        fromVideoFeedItem?.let { props["video_feed_from_video_id"] = it.videoId.toString() }
        watchTimeMs?.let { props["video_feed_total_watch_time"] = it }
        videoDurationMs?.let { props["video_feed_total_video_duration"] = it }
        if (entrySurface.isNotEmpty()) props["video_feed_entry_surface"] = entrySurface
        return props
    }
}
