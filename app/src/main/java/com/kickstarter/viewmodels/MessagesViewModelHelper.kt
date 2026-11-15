package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.ui.data.MessagesData
import io.reactivex.Observable

/**
 * ViewModel managing state transitions and user intents for MessagesViewModelHelper.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
internal object MessagesViewModelHelper {
    fun projectAndBacker(envelopeAndData: Pair<MessageThreadEnvelope, MessagesData>): Pair<Project, User> {
        val project = envelopeAndData.second.project
        val backer =
            if (project.isBacking()) envelopeAndData.second.currentUser else envelopeAndData.first.messageThread()
                ?.participant()
        return Pair.create(project, backer)
    }

    fun backingAndProjectFromData(
        data: MessagesData,
        apiClient: ApiClientTypeV2,
        apolloClient: ApolloClientTypeV2
    ): Observable<Pair<Backing?, Project>> {
        val backingAndProjectObs = data.backingOrThread.either(
            ifLeft = {
                return@either Observable.just(Pair.create<Backing, Project>(it, data.project))
            },
            ifRight = {
                val backingNotification =
                    if (data.project.isBacking()) apolloClient.getProjectBacking(
                        data.project.slug() ?: ""
                    ).materialize().share()
                    else apiClient.fetchProjectBacking(
                        data.project,
                        data.participant
                    ).materialize().share()

                return@either backingNotification
                    .filter { data.project.isNotNull() }
                    .map {
                        Pair.create(it.value, data.project)
                    }
                    .take(1)
            }
        )

        return backingAndProjectObs
    }
}
