package com.kickstarter.mock.factories

import com.kickstarter.ui.data.CommentCardData

/**
 * Test mock implementation for CommentCardDataFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
class CommentCardDataFactory {
    companion object {
        fun commentCardDataBacked(): CommentCardData = CommentCardData.builder()
            .comment(CommentFactory.comment())
            .project(ProjectFactory.backedProject())
            .commentableId(null)
            .build()

        fun commentCardData(): CommentCardData = CommentCardData.builder()
            .comment(CommentFactory.comment())
            .project(ProjectFactory.project())
            .commentCardState(0)
            .commentableId(null)
            .build()
    }
}
