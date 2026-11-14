package com.kickstarter.features.videofeed.data

import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

sealed class KSVideoBadgeType {
    object ProjectWeLove : KSVideoBadgeType()
    /**
     * Badge displaying days left for campaign.
     *
     * Negative constraints:
     * - Do not pass blank text.
     */
    data class DaysLeft(val text: String) : KSVideoBadgeType()
    object JustLaunched : KSVideoBadgeType()
    object FirstTimeCreator : KSVideoBadgeType()
    object ByASuperbacker : KSVideoBadgeType()
    object NearYou : KSVideoBadgeType()
    object NSFW : KSVideoBadgeType()
    object AlmostThere : KSVideoBadgeType()
    object Trending : KSVideoBadgeType()
    object Popular : KSVideoBadgeType()
    object HotRightNow : KSVideoBadgeType()
}

/**
 * Immutable domain representation of a video feed item.
 *
 * Negative constraints:
 * - Do not pass blank video IDs or null [project].
 * - Do not construct with invalid HLS stream URLs.
 */
data class VideoFeedItem(
    val badges: List<KSVideoBadgeType>,
    val project: Project,
    val hlsUrl: String?,
    val previewImageUrl: String? = null,
    val videoId: Long = 0,
    val itemId: NonBlankId? = null
)

/**
 * Envelope carrying paginated video feed items.
 *
 * Negative constraints:
 * - Do not mutate [items] list after instantiation.
 */
data class VideoFeedEnvelope(
    val items: List<VideoFeedItem> = emptyList(),
    val pageInfo: PageInfoEnvelope? = null
)
