package com.kickstarter.libs

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Pair
import androidx.annotation.VisibleForTesting
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.MessageThread
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.Update
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Library utility component for PushNotifications.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.notifications")
class PushNotifications(
    @field:ApplicationContext @param:ApplicationContext private val context: Context,
    private val client: ApiClientTypeV2
) {
    private val notifications: PublishSubject<PushNotificationEnvelope> = PublishSubject.create()
    private val subscriptions = CompositeDisposable()

    @VisibleForTesting
    var messageThreadIntent: Intent? = null

    fun initialize() {
        PushNotificationChannels.createNotificationChannels(context)

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isErroredPledge() }
                .subscribe { envelope: PushNotificationEnvelope ->
                    PushNotificationDisplayHelper.displayNotificationFromErroredPledge(context, envelope)
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isFriendFollow() }
                .subscribeOn(Schedulers.io())
                .subscribe { envelope: PushNotificationEnvelope ->
                    PushNotificationDisplayHelper.displayNotificationFromFriendFollowActivity(context, envelope)
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isMessage() }
                .flatMap { envelope: PushNotificationEnvelope ->
                    fetchMessageThreadWithEnvelope(envelope)
                }
                .subscribe { envelopeAndThread: Pair<PushNotificationEnvelope, MessageThread?> ->
                    val pendingIntent = envelopeAndThread.second?.let {
                        messageThreadIntent(envelopeAndThread.first, it)
                    }
                    if (pendingIntent != null) {
                        PushNotificationDisplayHelper.displayNotificationFromMessageActivity(
                            context, envelopeAndThread.first, pendingIntent
                        )
                    }
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isProjectActivity() }
                .subscribeOn(Schedulers.io())
                .subscribe { envelope: PushNotificationEnvelope ->
                    PushNotificationDisplayHelper.displayNotificationFromProjectActivity(context, envelope)
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isProjectReminder() }
                .subscribeOn(Schedulers.io())
                .subscribe { envelope: PushNotificationEnvelope ->
                    PushNotificationDisplayHelper.displayNotificationFromProjectReminder(context, envelope)
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isProjectUpdateActivity() }
                .flatMap { envelope: PushNotificationEnvelope ->
                    fetchUpdateWithEnvelope(envelope)
                }
                .subscribeOn(Schedulers.io())
                .subscribe { envelopeAndUpdate: Pair<PushNotificationEnvelope, Update> ->
                    PushNotificationDisplayHelper.displayNotificationFromUpdateActivity(
                        context, envelopeAndUpdate.first, envelopeAndUpdate.second
                    )
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isSurvey() }
                .flatMap { envelope: PushNotificationEnvelope ->
                    fetchSurveyResponseWithEnvelope(envelope)
                }
                .subscribe { envelopeAndResponse: Pair<PushNotificationEnvelope, SurveyResponse> ->
                    PushNotificationDisplayHelper.displayNotificationFromSurveyResponseActivity(
                        context, envelopeAndResponse.first
                    )
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isPledgeRedemption() }
                .subscribe { envelope: PushNotificationEnvelope ->
                    PushNotificationDisplayHelper.displayNotificationFromPledgeRedemption(
                        context, envelope
                    )
                }
        )
    }

    fun add(envelope: PushNotificationEnvelope) {
        notifications.onNext(envelope)
    }

    @VisibleForTesting
    fun messageThreadIntent(
        envelope: PushNotificationEnvelope,
        messageThread: MessageThread
    ): PendingIntent {
        val (intent, pendingIntent) = PushNotificationIntentBuilder.createMessageThreadIntent(
            context, envelope, messageThread
        )
        this.messageThreadIntent = intent
        return pendingIntent
    }

    private fun fetchMessageThreadWithEnvelope(
        envelope: PushNotificationEnvelope
    ): Observable<Pair<PushNotificationEnvelope, MessageThread?>> {
        val message = envelope.message()?.messageThreadId()

        val messageThread = message?.let {
            client.fetchMessagesForThread(it)
                .compose(Transformers.neverErrorV2())
                .map { obj: MessageThreadEnvelope -> obj.messageThread() }
        } ?: Observable.empty()

        return Observable.just(envelope)
            .compose(Transformers.combineLatestPair(messageThread))
    }

    private fun fetchSurveyResponseWithEnvelope(
        envelope: PushNotificationEnvelope
    ): Observable<Pair<PushNotificationEnvelope, SurveyResponse>>? {
        val survey = envelope.survey() ?: return null

        val surveyResponse = client.fetchSurveyResponse(survey.id())
            .compose(Transformers.neverErrorV2())

        return Observable.just(envelope)
            .compose(Transformers.combineLatestPair(surveyResponse))
    }

    private fun fetchUpdateWithEnvelope(
        envelope: PushNotificationEnvelope
    ): Observable<Pair<PushNotificationEnvelope, Update>>? {
        val activity = envelope.activity() ?: return null
        val updateId = activity.updateId() ?: return null
        val projectId = activity.projectId() ?: return null

        val projectParam = projectId.toString()
        val updateParam = updateId.toString()

        val update = client.fetchUpdate(projectParam, updateParam)
            .compose(Transformers.neverErrorV2())

        return Observable.just(envelope)
            .compose(Transformers.combineLatestPair(update))
    }
}
