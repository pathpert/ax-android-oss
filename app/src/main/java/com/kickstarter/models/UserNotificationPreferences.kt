package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Mobile push and email notification settings for a Kickstarter user.
 *
 * Negative Constraints:
 * - Do not assume all notification channels default to true; verify server-provided state.
 */
@Parcelize
data class UserNotificationPreferences(
    val mobileBackings: Boolean = false,
    val mobileComments: Boolean = false,
    val mobileCreatorEdu: Boolean = false,
    val mobileFollower: Boolean = false,
    val mobileFriendActivity: Boolean = false,
    val mobileMessages: Boolean = false,
    val mobilePostLikes: Boolean = false,
    val mobileUpdates: Boolean = false,
    val mobileMarketing: Boolean = false,
    val emailBackings: Boolean = false,
    val emailComments: Boolean = false,
    val emailCommentReplies: Boolean = false,
    val emailCreatorDigest: Boolean = false,
    val emailCreatorEdu: Boolean = false,
    val emailFollower: Boolean = false,
    val emailFriendActivity: Boolean = false,
    val emailMessages: Boolean = false,
    val emailUpdates: Boolean = false
) : Parcelable
