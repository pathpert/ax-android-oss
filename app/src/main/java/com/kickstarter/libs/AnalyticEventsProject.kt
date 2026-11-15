package com.kickstarter.libs

import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.socialshare.data.analyticsContextType
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_BODY
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_CHARACTER_COUNT
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_ID
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_ROOT_ID
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_LOCATION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_SECTION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_TYPE
import com.kickstarter.libs.utils.ContextPropertyKeyName.PROJECT_UPDATE_ID
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.VIDEO_FEED
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.REPLY
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.ROOT
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.UNWATCH
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.WATCH
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CAMPAIGN_DETAILS
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.COMMENT_POST
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SHARE_PLATFORM_CLICKED
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.VIDEO_PROGRESS_BAR
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.WATCH_PROJECT
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.VIDEO_FEED as VIDEO_FEED_LOCATION
import com.kickstarter.libs.utils.EventName.CARD_CLICKED
import com.kickstarter.libs.utils.EventName.CTA_CLICKED
import com.kickstarter.libs.utils.EventName.PAGE_VIEWED
import com.kickstarter.libs.utils.EventName.VIDEO_PLAYBACK_COMPLETED
import com.kickstarter.libs.utils.EventName.VIDEO_PLAYBACK_STARTED
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData

internal class AnalyticEventsProject(private val client: AnalyticEvents.ProxyClient) {

    fun trackVideoStarted(project: Project, videoLength: Long, videoPosition: Long) {
        val props: HashMap<String, Any> = HashMap()
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.videoProperties(videoLength, videoPosition))
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(VIDEO_PLAYBACK_STARTED.eventName, props)
    }

    fun trackVideoCompleted(project: Project, videoLength: Long, videoPosition: Long) {
        val props: HashMap<String, Any> = HashMap()
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.videoProperties(videoLength, videoPosition))
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(VIDEO_PLAYBACK_COMPLETED.eventName, props)
    }

    fun trackProjectScreenViewed(projectData: ProjectData, pageSectionContext: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to PROJECT.contextName)
        props[CONTEXT_SECTION.contextName] = pageSectionContext
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackProjectCardClicked(project: Project, pageContext: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_TYPE.contextName to PROJECT.contextName)
        props[CONTEXT_PAGE.contextName] = pageContext
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CARD_CLICKED.eventName, props)
    }

    fun trackCampaignDetailsCTAClicked(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to CAMPAIGN_DETAILS.contextName)
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackProjectPageTabChanged(projectData: ProjectData, pageSectionContext: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to PROJECT.contextName)
        props[CONTEXT_SECTION.contextName] = pageSectionContext
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackWatchProjectCTA(project: Project, contextPageName: EventContextValues.ContextPageName) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to WATCH_PROJECT.contextName)
        props[CONTEXT_TYPE.contextName] = if (project.isStarred()) WATCH.contextName else UNWATCH.contextName
        props[CONTEXT_PAGE.contextName] = contextPageName.contextName
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackRootCommentPageViewed(project: Project, projectUpdateId: String? = null) {
        val props: HashMap<String, Any> = HashMap()
        props.putAll(createCommentPagePropMap(projectUpdateId))
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackThreadCommentPageViewed(project: Project, rootCommentId: String, projectUpdateId: String? = null) {
        val props: HashMap<String, Any> = HashMap()
        props.putAll(createCommentPagePropMap(projectUpdateId))
        props[COMMENT_ROOT_ID.contextName] = rootCommentId
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackCommentCTA(project: Project, commentId: String, comment: String, projectUpdateId: String? = null) {
        val props: HashMap<String, Any> = createCommentPropMap(projectUpdateId, comment)
        props[CONTEXT_TYPE.contextName] = ROOT.contextName
        props[COMMENT_ID.contextName] = commentId
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackRootCommentReplyCTA(
        project: Project,
        commentId: String,
        commentReply: String,
        rootCommentId: String,
        projectUpdateId: String? = null
    ) {
        val props: HashMap<String, Any> = createCommentPropMap(projectUpdateId, commentReply)
        props[CONTEXT_TYPE.contextName] = REPLY.contextName
        props[COMMENT_ID.contextName] = commentId
        props[COMMENT_ROOT_ID.contextName] = rootCommentId
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    private fun createCommentPropMap(projectUpdateId: String?, commentReply: String): HashMap<String, Any> {
        return hashMapOf<String, Any>(CONTEXT_CTA.contextName to COMMENT_POST.contextName).also { map ->
            map.putAll(createCommentPagePropMap(projectUpdateId))
            map[COMMENT_BODY.contextName] = commentReply
            map[COMMENT_CHARACTER_COUNT.contextName] = commentReply.length
        }
    }

    private fun createCommentPagePropMap(projectUpdateId: String?): HashMap<String, Any> {
        return hashMapOf<String, Any>().also { map ->
            map[CONTEXT_PAGE.contextName] = PROJECT.contextName
            map[CONTEXT_SECTION.contextName] = EventContextValues.ContextSectionName.COMMENTS.contextName

            projectUpdateId?.let {
                map[CONTEXT_SECTION.contextName] = EventContextValues.ContextSectionName.UPDATES.contextName
                map[CONTEXT_LOCATION.contextName] = EventContextValues.LocationContextName.COMMENTS.contextName
                map[PROJECT_UPDATE_ID.contextName] = projectUpdateId
            }
        }
    }

    fun trackVideoFeedImpression(item: VideoFeedItem, position: Int, entrySurface: String) {
        val props = HashMap<String, Any>()
        props[CONTEXT_PAGE.contextName] = VIDEO_FEED.contextName
        props[CONTEXT_LOCATION.contextName] = VIDEO_FEED_LOCATION.contextName
        props.putAll(AnalyticEventsUtils.videoFeedItemProperties(item, position))
        props["video_feed_entry_surface"] = entrySurface
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackVideoFeedPageViewed(
        videoFeedItem: VideoFeedItem,
        toPosition: Int,
        fromVideoFeedItem: VideoFeedItem,
        watchTimeMs: Long?,
        videoDurationMs: Long?,
        entrySurface: String
    ) {
        val props = HashMap<String, Any>()
        props[CONTEXT_PAGE.contextName] = VIDEO_FEED.contextName
        props[CONTEXT_LOCATION.contextName] = VIDEO_FEED_LOCATION.contextName
        props.putAll(
            AnalyticEventsUtils.videoFeedPageViewedProperties(
                videoFeedItem, toPosition, fromVideoFeedItem, watchTimeMs, videoDurationMs, entrySurface
            )
        )
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackVideoFeedProgressBarTap(item: VideoFeedItem, percentageWatched: Float, watchTimeAtClick: Long? = null) {
        val props = HashMap<String, Any>()
        props[CONTEXT_PAGE.contextName] = VIDEO_FEED.contextName
        props[CONTEXT_CTA.contextName] = VIDEO_PROGRESS_BAR.contextName
        props.putAll(AnalyticEventsUtils.videoFeedItemProperties(item, 0))
        props["video_feed_percentage_watched"] = percentageWatched
        watchTimeAtClick?.let { props["video_feed_watch_time_at_click"] = it }
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackSharePlatformCTAClicked(platform: SocialSharePlatform, contextPage: EventContextValues.ContextPageName) {
        val props = HashMap<String, Any>()
        props[CONTEXT_CTA.contextName] = SHARE_PLATFORM_CLICKED.contextName
        props[CONTEXT_PAGE.contextName] = contextPage.contextName
        props[CONTEXT_TYPE.contextName] = platform.analyticsContextType()
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackVideoFeedCTAClicked(
        project: Project,
        videoId: Long,
        ctaType: EventContextValues.CtaContextName,
        watchTimeAtClick: Long? = null
    ) {
        val props = HashMap<String, Any>()
        props[CONTEXT_PAGE.contextName] = VIDEO_FEED.contextName
        props[CONTEXT_CTA.contextName] = ctaType.contextName
        props["video_feed_video_id"] = videoId.toString()
        props["video_feed_project_id"] = project.id().toString()
        watchTimeAtClick?.let { props["video_feed_watch_time_at_click"] = it }
        client.track(CTA_CLICKED.eventName, props)
    }
}
