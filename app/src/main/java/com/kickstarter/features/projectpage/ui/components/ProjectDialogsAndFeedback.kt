package com.kickstarter.features.projectpage.ui.components

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.kickstarter.R
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.libs.utils.extensions.showLatePledgeFlow
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThanksActivity
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.extensions.showSnackbar

/**
 * Handles presentation of success dialogs, star confirmation toasts, error alerts, and snackbars.
 */
class ProjectDialogsAndFeedback(
    private val activity: Activity,
    private val snackbarAnchor: View,
    private val errorAnchor: View,
    private val clearFragmentBackStack: () -> Boolean
) {
    fun showCancelPledgeSuccess() {
        clearFragmentBackStack()
        showSnackbar(snackbarAnchor, activity.getString(R.string.Youve_canceled_your_pledge))
    }

    fun showCreatePledgeSuccess(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val projectData = pledgeData.projectData()
        if (clearFragmentBackStack() || projectData.project().showLatePledgeFlow()) {
            activity.startActivity(
                Intent(activity, ThanksActivity::class.java)
                    .putExtra(IntentKey.PROJECT, projectData.project().reduceProjectPayload())
                    .putExtra(IntentKey.CHECKOUT_DATA, checkoutData)
                    .putExtra(IntentKey.PLEDGE_DATA, pledgeData)
            )
        }
    }

    fun showPledgeNotCancelableDialog() {
        AlertDialog.Builder(activity, R.style.Dialog)
            .setMessage(R.string.We_dont_allow_cancelations_that_will_cause_a_project_to_fall_short_of_its_goal_within_the_last_24_hours)
            .setPositiveButton(activity.getString(R.string.general_alert_buttons_ok)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun showStarToast() {
        ViewUtils.showToastFromTop(
            activity,
            activity.getString(R.string.project_star_confirmation),
            0,
            activity.resources.getDimensionPixelSize(R.dimen.grid_8)
        )
    }

    fun showToastError(message: String? = null) {
        activity.runOnUiThread {
            showErrorToast(
                activity.applicationContext,
                errorAnchor as ViewGroup,
                message ?: activity.getString(R.string.general_error_something_wrong)
            )
        }
    }
}
