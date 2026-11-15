package com.kickstarter.ui.activities

import com.kickstarter.models.Comment
import com.kickstarter.ui.adapters.RepliesAdapter
import com.kickstarter.ui.adapters.RepliesStatusAdapter
import com.kickstarter.viewmodels.ThreadViewModel

/**
 * Activity displaying and coordinating user interactions for ThreadAdapterDelegate.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
class ThreadAdapterDelegate(
    private val viewModelProvider: () -> ThreadViewModel.ThreadViewModel
) : RepliesStatusAdapter.Delegate, RepliesAdapter.Delegate {

    override fun onRetryViewClicked(comment: Comment) {}
    override fun onReplyButtonClicked(comment: Comment) {}
    override fun onFlagButtonClicked(comment: Comment) {}

    override fun onCommentGuideLinesClicked(comment: Comment) {
        viewModelProvider().inputs.onShowGuideLinesLinkClicked()
    }

    override fun onCommentRepliesClicked(comment: Comment) {}

    override fun onCommentPostedFailed(comment: Comment, position: Int) {
        viewModelProvider().inputs.refreshCommentCardInCaseFailedPosted(comment, position)
    }

    override fun onCommentPostedSuccessFully(comment: Comment, position: Int) {
        viewModelProvider().inputs.refreshCommentCardInCaseSuccessPosted(comment, position)
    }

    override fun loadMoreCallback() {
        viewModelProvider().inputs.reloadRepliesPage()
    }

    override fun retryCallback() {
        viewModelProvider().inputs.reloadRepliesPage()
    }

    override fun onShowCommentClicked(comment: Comment) {
        viewModelProvider().inputs.onShowCanceledPledgeComment(comment)
    }
}

