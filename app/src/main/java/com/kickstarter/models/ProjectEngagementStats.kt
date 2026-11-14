package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Community engagement and activity statistics for a Kickstarter project.
 *
 * Negative Constraints:
 * - Do not pass negative counts for any metric.
 *
 * @param backersCount Total number of backers who have pledged to the project.
 * @param commentsCount Total number of comments posted across the project campaign and updates.
 * @param updatesCount Total number of project updates posted by the creator.
 * @param watchesCount Total number of users currently saving/watching the project.
 * @param sharesCount Total number of times the project has been shared.
 * @param unreadMessagesCount Number of unread direct messages from backers or creator.
 * @param unseenActivityCount Number of unseen activity feed items for the project.
 */
@Parcelize
data class ProjectEngagementStats(
    val backersCount: Int = 0,
    val commentsCount: Int = 0,
    val updatesCount: Int = 0,
    val watchesCount: Int = 0,
    val sharesCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val unseenActivityCount: Int = 0
) : Parcelable

