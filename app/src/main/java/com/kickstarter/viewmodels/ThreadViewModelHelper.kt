package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.libs.utils.extensions.toCommentCardList
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime

/**
 * ViewModel managing state transitions and user intents for ThreadViewModelHelper.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
internal object ThreadViewModelHelper {

    fun mapListToData(envelope: CommentEnvelope, project: Project, currentUser: User?): List<CommentCardData>? =
        envelope.comments?.toCommentCardList(project, currentUser)

    fun loadWithProjectReplies(
        apolloClient: ApolloClientTypeV2,
        comment: Observable<Comment>,
        cursor: String?,
        internalError: BehaviorSubject<Throwable>
    ): Observable<CommentEnvelope> {
        return comment.switchMap {
            apolloClient.getRepliesForComment(
                it,
                if (cursor.isNullOrEmpty()) null else cursor
            )
        }.doOnError {
            internalError.onNext(it)
        }.onErrorResumeNext(Observable.empty())
    }

    fun buildReplyBody(it: Pair<Pair<CommentCardData, User>, Pair<String, DateTime>>): Comment {
        return Comment.builder()
            .body(it.second.first)
            .parentId(it.first.first.comment?.id() ?: -1)
            .authorBadges(listOf())
            .createdAt(it.second.second)
            .cursor("")
            .deleted(false)
            .id(-1)
            .repliesCount(0)
            .authorCanceledPledge(false)
            .author(it.first.second)
            .build()
    }

    fun getCommentComposerStatus(projectAndUser: Pair<Project, User?>): CommentComposerStatus =
        when {
            projectAndUser.second == null -> CommentComposerStatus.GONE
            projectAndUser.first.isBacking() || projectAndUser.first.userIsCreator(
                projectAndUser.second
            ) -> CommentComposerStatus.ENABLED
            else -> CommentComposerStatus.DISABLED
        }

    fun getCommentCardDataFromIntent(intent: Observable<Intent>): Observable<CommentCardData> = intent
        .map { it.getParcelableExtra<CommentCardData>(IntentKey.COMMENT_CARD_DATA) }
        .ofType(CommentCardData::class.java)

    fun getProjectUpdateId(intent: Observable<Intent>): Observable<String> = intent
        .map { it.getStringExtra(IntentKey.UPDATE_POST_ID) ?: "" }
}
