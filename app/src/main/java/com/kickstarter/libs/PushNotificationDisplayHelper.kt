package com.kickstarter.libs

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.PushNotificationChannels.CHANNEL_FOLLOWING
import com.kickstarter.libs.PushNotificationChannels.CHANNEL_MESSAGES
import com.kickstarter.libs.PushNotificationChannels.CHANNEL_PLEDGE_REDEMPTION
import com.kickstarter.libs.PushNotificationChannels.CHANNEL_PROJECT_ACTIVITY
import com.kickstarter.libs.PushNotificationChannels.CHANNEL_PROJECT_REMINDER
import com.kickstarter.libs.PushNotificationChannels.CHANNEL_PROJECT_UPDATES
import com.kickstarter.libs.PushNotificationChannels.CHANNEL_SURVEY
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Update
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import com.kickstarter.ui.IntentKey
import java.util.concurrent.ExecutionException

/**
 * Library utility component for PushNotificationDisplayHelper.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
internal object PushNotificationDisplayHelper {

    fun displayNotificationFromErroredPledge(context: Context, envelope: PushNotificationEnvelope) {
        val gcm = envelope.gcm()
        val erroredPledge = envelope.erroredPledge() ?: return

        val projectId = erroredPledge.projectId()
        val projectIntent = PushNotificationIntentBuilder.projectIntent(context, envelope, projectId.toString())
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
        val notification = notificationBuilder(context, gcm.title(), gcm.alert(), CHANNEL_PROJECT_REMINDER)
            .setContentIntent(PushNotificationIntentBuilder.projectContentIntent(context, envelope, projectIntent))
            .build()

        notificationManager(context)?.notify(envelope.signature(), notification)
    }

    fun displayNotificationFromFriendFollowActivity(context: Context, envelope: PushNotificationEnvelope) {
        val gcm = envelope.gcm()
        val activity = envelope.activity() ?: return

        val notification = notificationBuilder(context, gcm.title(), gcm.alert(), CHANNEL_FOLLOWING)
            .setLargeIcon(fetchBitmap(context, activity.userPhoto(), true))
            .setContentIntent(PushNotificationIntentBuilder.friendFollowActivityIntent(context, envelope))
            .build()
        notificationManager(context)?.notify(envelope.signature(), notification)
    }

    fun displayNotificationFromMessageActivity(
        context: Context,
        envelope: PushNotificationEnvelope,
        pendingIntent: PendingIntent
    ) {
        val gcm = envelope.gcm()
        val notification = notificationBuilder(context, gcm.title(), gcm.alert(), CHANNEL_MESSAGES)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager(context)?.notify(envelope.signature(), notification)
    }

    fun displayNotificationFromProjectActivity(context: Context, envelope: PushNotificationEnvelope) {
        val gcm = envelope.gcm()
        val activity = envelope.activity() ?: return
        val projectId = activity.projectId() ?: return
        val projectPhoto = activity.projectPhoto()

        var notificationBuilder =
            notificationBuilder(context, gcm.title(), gcm.alert(), CHANNEL_PROJECT_ACTIVITY)
                .setContentIntent(
                    PushNotificationIntentBuilder.projectContentIntent(
                        context,
                        envelope,
                        PushNotificationIntentBuilder.projectIntent(context, envelope, projectId.toString())
                    )
                )
        if (projectPhoto != null) {
            notificationBuilder = notificationBuilder.setLargeIcon(fetchBitmap(context, projectPhoto, false))
        }
        val notification = notificationBuilder.build()

        notificationManager(context)?.notify(envelope.signature(), notification)
    }

    fun displayNotificationFromProjectReminder(context: Context, envelope: PushNotificationEnvelope) {
        val gcm = envelope.gcm()
        val project = envelope.project() ?: return

        val projectIntent = PushNotificationIntentBuilder.projectIntent(context, envelope, project.id().toString())
        val notification = notificationBuilder(context, gcm.title(), gcm.alert(), CHANNEL_PROJECT_REMINDER)
            .setContentIntent(PushNotificationIntentBuilder.projectContentIntent(context, envelope, projectIntent))
            .setLargeIcon(fetchBitmap(context, project.photo(), false))
            .build()

        notificationManager(context)?.notify(envelope.signature(), notification)
    }

    fun displayNotificationFromSurveyResponseActivity(
        context: Context,
        envelope: PushNotificationEnvelope,
    ) {
        val gcm = envelope.gcm()
        val surveyUrlPath = envelope.survey()?.urls()?.web()?.survey() ?: return

        val notification = notificationBuilder(context, gcm.title(), gcm.alert(), CHANNEL_SURVEY)
            .setContentIntent(
                PushNotificationIntentBuilder.surveyResponseContentIntent(
                    context, envelope, surveyUrlPath, IntentKey.NOTIFICATION_SURVEY_RESPONSE
                )
            )
            .build()
        notificationManager(context)?.notify(envelope.signature(), notification)
    }

    fun displayNotificationFromPledgeRedemption(
        context: Context,
        envelope: PushNotificationEnvelope,
    ) {
        val gcm = envelope.gcm()
        val pledgeRedemptionPath = envelope.pledgeRedemption()?.pledgeRedemptionPath() ?: return

        val notification = notificationBuilder(context, gcm.title(), gcm.alert(), CHANNEL_PLEDGE_REDEMPTION)
            .setContentIntent(
                PushNotificationIntentBuilder.surveyResponseContentIntent(
                    context, envelope, pledgeRedemptionPath, IntentKey.NOTIFICATION_PLEDGE_REDEMPTION
                )
            )
            .build()
        notificationManager(context)?.notify(envelope.signature(), notification)
    }

    fun displayNotificationFromUpdateActivity(
        context: Context,
        envelope: PushNotificationEnvelope,
        update: Update
    ) {
        val gcm = envelope.gcm()
        val activity = envelope.activity() ?: return
        val updateId = activity.updateId() ?: return
        val projectId = activity.projectId() ?: return

        val projectParam = projectId.toString()

        val notification = notificationBuilder(context, gcm.title(), gcm.alert(), CHANNEL_PROJECT_UPDATES)
            .setContentIntent(PushNotificationIntentBuilder.projectUpdateContentIntent(context, envelope, update, projectParam))
            .setLargeIcon(fetchBitmap(context, activity.projectPhoto(), false))
            .build()
        notificationManager(context)?.notify(envelope.signature(), notification)
    }

    private fun notificationBuilder(
        context: Context,
        title: String,
        text: String,
        channelId: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_kickstarter_micro_k)
            .setColor(ContextCompat.getColor(context, R.color.kds_create_700))
            .setContentText(text)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
    }

    private fun fetchBitmap(context: Context, url: String?, transformIntoCircle: Boolean): Bitmap? {
        if (url == null) {
            return null
        }

        try {
            if (transformIntoCircle) {
                val circleCrop = Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .error(R.drawable.logo)
                    .apply(RequestOptions.circleCropTransform())
                    .submit()
                return circleCrop.get()
            } else {
                val squareRoundCorners = Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .error(R.drawable.logo)
                    .transform(MultiTransformation(CenterCrop(), RoundedCorners(10)))
                    .submit()
                return squareRoundCorners.get()
            }
        } catch (e: ExecutionException) {
            val problem = ProblemDetailsMapper.fromThrowable(e, "load push notification image")
            FirebaseCrashlytics.getInstance().recordException(ProblemException(problem, e))
            return BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        } catch (e: InterruptedException) {
            val problem = ProblemDetailsMapper.fromThrowable(e, "load push notification image")
            FirebaseCrashlytics.getInstance().recordException(ProblemException(problem, e))
            return BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        }
    }

    private fun notificationManager(context: Context): NotificationManager? {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }
}
