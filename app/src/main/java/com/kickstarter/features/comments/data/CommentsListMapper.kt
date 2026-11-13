package com.kickstarter.features.comments.data

import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.cardStatus
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime

/**
 * Pure mapping and domain transformation functions for Comment list entries and composer states.
 *
 * Negative Constraints:
 * - Do NOT perform network I/O or mutate shared mutable collections in these mappers.
 */
object CommentsListMapper {

    fun mapToCommentCardDataList(
        envelope: CommentEnvelope,
        project: Project,
        currentUser: User?
    ): List<CommentCardData> {
        return envelope.comments?.filter { item ->
            filterCancelledPledgeWithoutRepliesComment(item)
        }?.map { comment: Comment ->
            CommentCardData.builder()
                .comment(comment)
                .project(project)
                .commentCardState(comment.cardStatus(currentUser))
                .commentableId(envelope.commentableId)
                .build()
        } ?: emptyList()
    }

    fun filterCancelledPledgeWithoutRepliesComment(item: Comment): Boolean {
        return !(item.authorCanceledPledge() && item.repliesCount() == 0)
    }

    fun buildComment(
        user: User,
        commentBody: String,
        createdAt: DateTime
    ): Comment {
        return Comment.builder()
            .body(commentBody)
            .parentId(-1)
            .authorBadges(listOf())
            .createdAt(createdAt)
            .cursor("")
            .deleted(false)
            .id(-1)
            .authorCanceledPledge(false)
            .repliesCount(0)
            .author(user)
            .build()
    }

    fun getCommentComposerStatus(
        project: Project,
        user: KsOptional<User>
    ): CommentComposerStatus {
        return when {
            user.getValue() == null -> CommentComposerStatus.GONE
            project.canComment() ?: false -> CommentComposerStatus.ENABLED
            else -> CommentComposerStatus.DISABLED
        }
    }
}
