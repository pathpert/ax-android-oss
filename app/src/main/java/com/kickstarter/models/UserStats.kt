package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Activity, backing, and creator statistics for a Kickstarter user account.
 *
 * Negative Constraints:
 * - Do not pass negative counts for user activity or backings.
 *
 * @param backedProjectsCount Total number of projects backed by this user.
 * @param backingActionCount Number of active backings requiring user action (e.g. fix payment).
 * @param createdProjectsCount Total number of projects launched by this user.
 * @param draftProjectsCount Number of unlaunched draft projects.
 * @param erroredBackingsCount Number of backings with payment errors.
 * @param memberProjectsCount Number of projects the user is a team member on.
 * @param starredProjectsCount Number of saved/starred projects.
 * @param unreadMessagesCount Number of unread direct messages.
 * @param unseenActivityCount Number of unseen activity notifications.
 */
@Parcelize
data class UserStats(
    val backedProjectsCount: Int = 0,
    val backingActionCount: Int = 0,
    val createdProjectsCount: Int = 0,
    val draftProjectsCount: Int = 0,
    val erroredBackingsCount: Int = 0,
    val memberProjectsCount: Int = 0,
    val starredProjectsCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val unseenActivityCount: Int = 0
) : Parcelable

