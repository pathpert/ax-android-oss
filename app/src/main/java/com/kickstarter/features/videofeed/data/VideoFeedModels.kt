package com.kickstarter.features.videofeed.data

import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

/**
 * Badges displayed overlaid on video feed player items.
 *
 * Negative constraints:
 * - Do not pass unlocalized strings or blank text.
 */
sealed class KSVideoBadgeType {
    /**
     * Project We Love curated badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object ProjectWeLove : KSVideoBadgeType()

    /**
     * Badge displaying days left for campaign.
     *
     * Negative constraints:
     * - Do not pass blank text.
     *
     * @property text Localized countdown text string.
     */
    data class DaysLeft(val text: String) : KSVideoBadgeType()

    /**
     * Just launched campaign badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object JustLaunched : KSVideoBadgeType()

    /**
     * First time creator badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object FirstTimeCreator : KSVideoBadgeType()

    /**
     * Backed by a superbacker badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object ByASuperbacker : KSVideoBadgeType()

    /**
     * Projects near user location badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object NearYou : KSVideoBadgeType()

    /**
     * NSFW content warning badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object NSFW : KSVideoBadgeType()

    /**
     * Almost reached funding goal badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object AlmostThere : KSVideoBadgeType()

    /**
     * Trending campaign badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object Trending : KSVideoBadgeType()

    /**
     * Popular campaign badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object Popular : KSVideoBadgeType()

    /**
     * Hot right now velocity badge.
     *
     * Negative constraints:
     * - Do not use outside video feed overlays.
     */
    object HotRightNow : KSVideoBadgeType()
}

/**
 * Immutable domain representation of a video feed item.
 *
 * Negative constraints:
 * - Do not pass blank video IDs or null [project].
 * - Do not construct with invalid HLS stream URLs.
 *
 * @property badges Badges displayed on top of the video card.
 * @property project Associated Kickstarter project entity.
 * @property hlsUrl HLS video streaming URL.
 * @property previewImageUrl Thumbnail cover image URL.
 * @property videoId Unique video identifier.
 * @property itemId Validated non-blank item ID.
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
 *
 * @property items List of video feed items on current page.
 * @property pageInfo Pagination cursor and page metadata.
 */
data class VideoFeedEnvelope(
    val items: List<VideoFeedItem> = emptyList(),
    val pageInfo: PageInfoEnvelope? = null
)
