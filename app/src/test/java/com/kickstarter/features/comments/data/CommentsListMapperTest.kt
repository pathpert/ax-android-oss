package com.kickstarter.features.comments.data

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommentsListMapperTest : KSRobolectricTestCase() {

    @Test
    fun `test getCommentComposerStatus when user is logged out`() {
        val project = ProjectFactory.project()
        val status = CommentsListMapper.getCommentComposerStatus(project, KsOptional.empty())
        assertEquals(CommentComposerStatus.GONE, status)
    }

    @Test
    fun `test getCommentComposerStatus when user is logged in and can comment`() {
        val project = ProjectFactory.backedProject()
        val user = UserFactory.user()
        val status = CommentsListMapper.getCommentComposerStatus(project, KsOptional.of(user))
        assertEquals(CommentComposerStatus.ENABLED, status)
    }

    @Test
    fun `test filterCancelledPledgeWithoutRepliesComment`() {
        val cancelledNoReplies = CommentFactory.comment().toBuilder()
            .authorCanceledPledge(true)
            .repliesCount(0)
            .build()
        assertFalse(CommentsListMapper.filterCancelledPledgeWithoutRepliesComment(cancelledNoReplies))

        val cancelledWithReplies = CommentFactory.comment().toBuilder()
            .authorCanceledPledge(true)
            .repliesCount(2)
            .build()
        assertTrue(CommentsListMapper.filterCancelledPledgeWithoutRepliesComment(cancelledWithReplies))

        val regularComment = CommentFactory.comment().toBuilder()
            .authorCanceledPledge(false)
            .repliesCount(0)
            .build()
        assertTrue(CommentsListMapper.filterCancelledPledgeWithoutRepliesComment(regularComment))
    }

    @Test
    fun `test buildComment sets expected fields`() {
        val user = UserFactory.user()
        val now = DateTime.now()
        val comment = CommentsListMapper.buildComment(user, "Test comment", now)

        assertEquals("Test comment", comment.body())
        assertEquals(user, comment.author())
        assertEquals(now, comment.createdAt())
        assertEquals(-1L, comment.id())
        assertEquals(-1, comment.parentId())
    }

    @Test
    fun `test mapToCommentCardDataList filters cancelled pledges and maps status`() {
        val project = ProjectFactory.project()
        val user = UserFactory.user()
        val comments = listOf(
            CommentFactory.comment().toBuilder().authorCanceledPledge(false).repliesCount(1).build(),
            CommentFactory.comment().toBuilder().authorCanceledPledge(true).repliesCount(0).build()
        )
        val envelope = CommentEnvelopeFactory.emptyCommentsEnvelope().toBuilder()
            .comments(comments)
            .commentableId("123")
            .build()

        val cardList = CommentsListMapper.mapToCommentCardDataList(envelope, project, user)
        assertEquals(1, cardList.size)
        assertEquals("123", cardList[0].commentableId)
    }
}
