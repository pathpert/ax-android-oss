package com.kickstarter.libs

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import com.kickstarter.libs.RefTag.Companion.push
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Update
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ActivityFeedActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.activities.ProjectPageActivity
import com.kickstarter.ui.activities.SurveyResponseActivity
import com.kickstarter.ui.activities.UpdateActivity

internal object PushNotificationIntentBuilder {

    fun friendFollowActivityIntent(context: Context, envelope: PushNotificationEnvelope): PendingIntent {
        val messageThreadIntent = Intent(context, ActivityFeedActivity::class.java)

        val taskStackBuilder = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(messageThreadIntent)

        return checkNotNull(
            taskStackBuilder.getPendingIntent(
                envelope.signature(),
                PendingIntent.FLAG_IMMUTABLE
            )
        ) { "Failed to obtain pending intent for friend follow activity" }
    }

    fun createMessageThreadIntent(
        context: Context,
        envelope: PushNotificationEnvelope,
        messageThread: MessageThread
    ): Pair<Intent, PendingIntent> {
        val intent = Intent(context, MessagesActivity::class.java)
            .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
            .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.PUSH)

        val taskStackBuilder = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(intent)

        val pendingIntent = checkNotNull(
            taskStackBuilder.getPendingIntent(
                envelope.signature(),
                PendingIntent.FLAG_IMMUTABLE
            )
        ) { "Failed to obtain pending intent for message thread" }

        return Pair(intent, pendingIntent)
    }

    fun projectContentIntent(
        context: Context,
        envelope: PushNotificationEnvelope,
        projectIntent: Intent
    ): PendingIntent {
        val taskStackBuilder = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(projectIntent)

        return checkNotNull(
            taskStackBuilder.getPendingIntent(
                envelope.signature(),
                PendingIntent.FLAG_IMMUTABLE
            )
        ) { "Failed to obtain pending intent for project content" }
    }

    fun projectUpdateContentIntent(
        context: Context,
        envelope: PushNotificationEnvelope,
        update: Update,
        projectParam: String
    ): PendingIntent {
        val projectIntent = Intent().getProjectIntent(context)
            .putExtra(IntentKey.PROJECT_PARAM, projectParam)
            .putExtra(IntentKey.REF_TAG, push())

        val updateIntent = Intent(context, UpdateActivity::class.java)
            .putExtra(IntentKey.PROJECT_PARAM, projectParam)
            .putExtra(IntentKey.UPDATE, update)
            .putExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE, envelope)

        val taskStackBuilder = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(projectIntent)
            .addNextIntent(updateIntent)

        return checkNotNull(
            taskStackBuilder.getPendingIntent(
                envelope.signature(),
                PendingIntent.FLAG_IMMUTABLE
            )
        ) { "Failed to obtain pending intent for project update content" }
    }

    fun surveyResponseContentIntent(
        context: Context,
        envelope: PushNotificationEnvelope,
        urlPath: String,
        intentKey: String
    ): PendingIntent {
        val activityFeedIntent = Intent(context, ActivityFeedActivity::class.java)
        val surveyResponseIntent = Intent(context, SurveyResponseActivity::class.java)
            .putExtra(intentKey, urlPath)

        val taskStackBuilder = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(activityFeedIntent)
            .addNextIntent(surveyResponseIntent)

        return checkNotNull(
            taskStackBuilder.getPendingIntent(
                envelope.signature(),
                PendingIntent.FLAG_IMMUTABLE
            )
        ) { "Failed to obtain pending intent for survey response" }
    }

    fun projectIntent(context: Context, envelope: PushNotificationEnvelope, projectParam: String): Intent {
        val intent = Intent(context, ProjectPageActivity::class.java)
        return intent
            .putExtra(IntentKey.PROJECT_PARAM, projectParam)
            .putExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE, envelope)
            .putExtra(IntentKey.REF_TAG, push())
    }
}
