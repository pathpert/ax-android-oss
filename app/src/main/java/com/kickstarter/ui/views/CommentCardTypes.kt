package com.kickstarter.ui.views

import android.view.View

interface OnCommentCardClickedListener {
    fun onRetryViewClicked(view: View)
    fun onReplyButtonClicked(view: View)
    fun onFlagButtonClicked(view: View)
    fun onViewRepliesButtonClicked(view: View)
    fun onCommentGuideLinesClicked(view: View)
    fun onShowCommentClicked(view: View)
}

enum class CommentCardBadge(val commentCardBadge: Int) {
    NO_BADGE(0),
    YOU(1),
    CREATOR(2),
    SUPERBACKER(3),
    COLLABORATOR(4),
}

enum class CommentCardStatus(val commentCardStatus: Int) {
    COMMENT_FOR_LOGIN_BACKED_USERS(0), // comments without reply view
    COMMENT_WITH_REPLIES(1), // comments with reply view
    FAILED_TO_SEND_COMMENT(2), // pending comment
    DELETED_COMMENT(3), // Deleted comment
    RE_TRYING_TO_POST(4), // trying to post comment
    POSTING_COMMENT_COMPLETED_SUCCESSFULLY(5), // trying to post comment,
    TRYING_TO_POST(6), // comments without reply view,
    CANCELED_PLEDGE_MESSAGE(7),
    CANCELED_PLEDGE_COMMENT(8),
    FLAGGED_COMMENT(9),
}

