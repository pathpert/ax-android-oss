package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import com.kickstarter.R
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.viewmodels.CommentsViewModel

/**
 * Navigation and back-action helper for [CommentsActivity].
 */
internal object CommentsActivityNavigation {

    fun startThreadActivity(
        activity: Activity,
        commentData: CommentCardData,
        openKeyboard: Boolean,
        projectUpdateId: String? = null
    ) {
        val reducedProject = commentData.project?.reduceProjectPayload()
        val threadIntent = Intent(activity, ThreadActivity::class.java).apply {
            putExtra(IntentKey.COMMENT_CARD_DATA, commentData.toBuilder().project(reducedProject).build())
            putExtra(IntentKey.REPLY_EXPAND, openKeyboard)
            putExtra(IntentKey.UPDATE_POST_ID, projectUpdateId)
        }

        activity.startActivity(threadIntent)
        TransitionUtils.transition(activity, TransitionUtils.slideInFromRight())
    }

    fun startThreadActivityFromDeepLink(
        activity: Activity,
        commentData: CommentCardData,
        projectUpdateId: String? = null
    ) {
        val reducedProject = commentData.project?.reduceProjectPayload()
        val threadIntent = Intent(activity, ThreadActivity::class.java).apply {
            putExtra(IntentKey.COMMENT_CARD_DATA, commentData.toBuilder().project(reducedProject).build())
            putExtra(IntentKey.REPLY_EXPAND, false)
            putExtra(IntentKey.REPLY_SCROLL_BOTTOM, true)
            putExtra(IntentKey.UPDATE_POST_ID, projectUpdateId)
        }

        activity.startActivity(threadIntent)
        TransitionUtils.transition(activity, TransitionUtils.slideInFromRight())
    }

    fun promptUnsavedCommentBack(
        activity: Activity,
        isBackAction: Boolean,
        viewModel: CommentsViewModel.CommentsViewModel,
        onCancelRefreshing: () -> Unit
    ) {
        activity.showAlertDialog(
            activity.getString(R.string.Your_comment_wasnt_posted),
            activity.getString(R.string.You_will_lose_the_comment_if_you_leave_this_page),
            activity.getString(R.string.Cancel),
            activity.getString(R.string.Leave_page),
            false,
            positiveAction = {
                if (!isBackAction) {
                    onCancelRefreshing()
                }
            },
            negativeAction = {
                executeActions(activity, isBackAction, viewModel)
            }
        )
    }

    fun executeActions(
        activity: Activity,
        isBackAction: Boolean,
        viewModel: CommentsViewModel.CommentsViewModel
    ) {
        if (!isBackAction) {
            viewModel.inputs.refresh()
        } else {
            activity.finishWithAnimation()
        }
    }
}
