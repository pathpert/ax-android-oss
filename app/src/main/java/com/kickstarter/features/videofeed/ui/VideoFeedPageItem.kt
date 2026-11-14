package com.kickstarter.features.videofeed.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.media3.common.PlaybackException
import com.kickstarter.R
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.features.videofeed.ui.components.KSVideoActionsColumn
import com.kickstarter.features.videofeed.ui.components.KSVideoBadgesRow
import com.kickstarter.features.videofeed.ui.components.KSVideoCampaignCard
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.toCompactFormat
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Close
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Collapse
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Expand

/**
 * Renders an individual video item within the full-screen vertical pager feed.
 *
 * Encapsulates the video player, overlay chrome (badges, creator profile, bookmark, share,
 * and campaign backer stats), dismiss button, and UI hide/show toggle.
 */
@Composable
fun VideoFeedPageItem(
    item: VideoFeedItem,
    page: Int,
    isCurrentPage: Boolean,
    isSettledPage: Boolean,
    isMuted: Boolean,
    iconTopPadding: Dp,
    onMuteToggle: (Boolean) -> Unit,
    onPlayPauseTap: (project: Project, videoId: Long, isPlaying: Boolean) -> Unit,
    onProgressBarTap: (item: VideoFeedItem, progress: Float) -> Unit,
    onBecameInactive: (watchTimeMs: Long, videoDurationMs: Long) -> Unit,
    onPlaybackError: (item: VideoFeedItem, position: Int, error: PlaybackException, isActive: Boolean) -> Unit,
    onProfileClick: (project: Project, videoId: Long) -> Unit,
    onBookmarkClick: (project: Project, videoId: Long, index: Int) -> Unit,
    onShareRequested: (Project) -> Unit = {},
    onShareCTAClick: (project: Project, videoId: Long) -> Unit,
    onClose: () -> Unit,
    preLaunchedCallback: (project: Project, refTag: RefTag) -> Unit,
    projectCallback: (project: Project, refTag: RefTag) -> Unit,
    modifier: Modifier = Modifier
) {
    val project = item.project
    val videoUrl = item.hlsUrl ?: ""
    val profileImage = project.creator()?.avatar()?.medium() ?: ""
    val projectTitle = project.name()
    val bookmarkCount = remember(project) { project.watchesCount().toCompactFormat() }
    val shareCount = remember(project) { project.sharesCount().toCompactFormat() }
    val subtitle = remember(project) {
        val pledged = "${project.currencySymbol()}${NumberUtils.format(project.pledged().toInt())}"
        val backers = NumberUtils.format(project.backersCount())
        "$pledged pledged • Join $backers backers"
    }
    val percentageFounded by remember(page, isSettledPage) {
        derivedStateOf {
            if (isSettledPage) project.percentFunded()?.toFloat() ?: 0f else 0f
        }
    }

    var hideUi by remember(page) { mutableStateOf(false) }
    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage) hideUi = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        KSVideoPlayer(
            videoUrl = videoUrl,
            isActive = isCurrentPage,
            hideUi = hideUi,
            previewImageUrl = item.previewImageUrl,
            isMuted = isMuted,
            onMuteToggle = { muted ->
                onMuteToggle(muted)
            },
            onPlayPauseToggle = { isPlaying -> onPlayPauseTap(project, item.videoId, isPlaying) },
            onProgressBarInteraction = { currentProgress -> onProgressBarTap(item, currentProgress) },
            onBecameInactive = onBecameInactive,
            onPlaybackError = { error, active -> onPlaybackError(item, page, error, active) },
            overlayContent = { hazeState ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${VideoFeedScreenTestTag.VIDEO_FEED_OVERLAY_CONTAINER.name}_${project.id()}")
                ) {
                    val chromeVisible = remember { MutableTransitionState(!hideUi) }
                    chromeVisible.targetState = !hideUi
                    AnimatedVisibility(
                        visibleState = chromeVisible,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            KSVideoActionsColumn(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(end = dimensions.paddingMediumLarge),
                                profileImageUrl = profileImage,
                                bookmarkCount = bookmarkCount,
                                isBookmarked = project.isStarred(),
                                shareCount = shareCount,
                                onProfileClick = { onProfileClick(project, item.videoId) },
                                onBookmarkClick = { onBookmarkClick(project, item.videoId, page) },
                                onShareClick = {
                                    onShareRequested(project)
                                    onShareCTAClick(project, item.videoId)
                                },
                                onMoreOptionsClick = {}
                            )

                            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                            KSVideoBadgesRow(
                                badges = item.badges,
                                hazeState = hazeState
                            )
                        }
                    }

                    KSVideoCampaignCard(
                        title = projectTitle,
                        subtitle = subtitle,
                        buttonText = stringResource(R.string.project_back_button),
                        onButtonClick = {
                            val refTag = RefTag.videoFeed()
                            if (project.displayPrelaunch().isTrue()) {
                                preLaunchedCallback(project, refTag)
                            } else {
                                projectCallback(project, refTag)
                            }
                        },
                        progress = percentageFounded,
                        detailsVisible = !hideUi
                    )
                }
            }
        )

        Image(
            imageVector = Close,
            contentDescription = stringResource(id = R.string.accessibility_discovery_buttons_close),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = dimensions.paddingMediumSmall, top = iconTopPadding)
                .size(dimensions.videoFeedCloseButtonSize)
                .dropShadow(
                    shape = CircleShape,
                    shadow = Shadow(
                        radius = dimensions.videoPlayerShadowBlur,
                        color = KSTheme.colors.videoPlayer.iconShadow,
                        offset = DpOffset.Zero
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose,
                    onClickLabel = stringResource(id = R.string.accessibility_discovery_buttons_close),
                    role = Role.Button
                )
                .semantics {
                    role = Role.Button
                }
                .testTag("${VideoFeedScreenTestTag.VIDEO_FEED_CLOSE_BUTTON.name}_${project.id()}")
        )

        val hideUiLabel = stringResource(
            id = if (hideUi) R.string.fpo_Show_details else R.string.fpo_Hide_details
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = dimensions.paddingMediumSmall, top = iconTopPadding)
                .size(dimensions.videoFeedCloseButtonSize)
                .dropShadow(
                    shape = CircleShape,
                    shadow = Shadow(
                        radius = dimensions.videoPlayerShadowBlur,
                        color = KSTheme.colors.videoPlayer.iconShadow,
                        offset = DpOffset.Zero
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { hideUi = !hideUi },
                    onClickLabel = hideUiLabel,
                    role = Role.Button
                )
                .semantics(mergeDescendants = true) { }
                .testTag("${VideoFeedScreenTestTag.VIDEO_FEED_HIDE_UI_BUTTON.name}_${project.id()}"),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = if (hideUi) Expand else Collapse,
                contentDescription = hideUiLabel,
                modifier = Modifier.size(dimensions.videoFeedHideUiIconSize)
            )
        }
    }
}

