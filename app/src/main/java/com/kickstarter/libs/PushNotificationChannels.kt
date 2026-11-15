package com.kickstarter.libs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isNotNull

/**
 * Library utility component for PushNotificationChannels.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
internal object PushNotificationChannels {
    const val CHANNEL_ERRORED_PLEDGES = "ERRORED_PLEDGES"
    const val CHANNEL_FOLLOWING = "FOLLOWING"
    const val CHANNEL_MESSAGES = "MESSAGES"
    const val CHANNEL_PROJECT_ACTIVITY = "PROJECT_ACTIVITY"
    const val CHANNEL_PROJECT_REMINDER = "PROJECT_REMINDER"
    const val CHANNEL_PROJECT_UPDATES = "PROJECT_UPDATES"
    const val CHANNEL_SURVEY = "SURVEY"
    const val CHANNEL_PLEDGE_REDEMPTION = "PLEDGE_REDEMPTION"

    private val NOTIFICATION_CHANNELS = arrayOf(
        CHANNEL_ERRORED_PLEDGES,
        CHANNEL_FOLLOWING,
        CHANNEL_MESSAGES,
        CHANNEL_PROJECT_ACTIVITY,
        CHANNEL_PROJECT_REMINDER,
        CHANNEL_PROJECT_UPDATES,
        CHANNEL_SURVEY,
        CHANNEL_PLEDGE_REDEMPTION
    )

    fun createNotificationChannels(context: Context) {
        val channels = getListOfNotificationChannels(context)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (notificationManager.isNotNull()) {
            notificationManager.createNotificationChannels(channels)
        }
    }

    private fun getListOfNotificationChannels(context: Context): List<NotificationChannel> {
        val channels: MutableList<NotificationChannel> = ArrayList(NOTIFICATION_CHANNELS.size)
        channels.add(
            getNotificationChannel(
                context,
                CHANNEL_ERRORED_PLEDGES,
                R.string.Fix_your_payment_method,
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        channels.add(
            getNotificationChannel(
                context,
                CHANNEL_MESSAGES,
                R.string.Messages,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        channels.add(
            getNotificationChannel(
                context,
                CHANNEL_PROJECT_ACTIVITY,
                R.string.Project_activity,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        channels.add(
            getNotificationChannel(
                context,
                CHANNEL_PROJECT_REMINDER,
                R.string.Project_reminders,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        channels.add(
            getNotificationChannel(
                context,
                CHANNEL_PROJECT_UPDATES,
                R.string.Project_updates,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        channels.add(
            getNotificationChannel(
                context,
                CHANNEL_PLEDGE_REDEMPTION,
                R.string.Pledge_management,
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val followingChannel = getNotificationChannel(
            context,
            CHANNEL_FOLLOWING,
            R.string.Following,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        followingChannel.description =
            context.getString(R.string.When_following_is_on_you_can_follow_the_acticity_of_others)
        channels.add(followingChannel)
        channels.add(
            getNotificationChannel(
                context,
                CHANNEL_SURVEY,
                R.string.Reward_surveys,
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        return channels
    }

    private fun getNotificationChannel(
        context: Context,
        channelId: String,
        nameResId: Int,
        importance: Int
    ): NotificationChannel {
        val name: CharSequence = context.getString(nameResId)
        return NotificationChannel(channelId, name, importance)
    }
}
