package com.kickstarter.ui.activities

import android.app.Activity
import com.kickstarter.R
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.showAlertDialog
import com.kickstarter.viewmodels.LoginToutViewModel

/**
 * Dialog helper methods for [LoginToutActivity].
 */
internal object LoginToutDialogHelper {

    fun showFacebookAuthorizationError(activity: Activity) {
        ViewUtils.showDialog(
            activity,
            activity.getString(R.string.general_error_oops),
            activity.getString(R.string.login_tout_errors_facebook_authorization_exception_message),
            activity.getString(R.string.login_tout_errors_facebook_authorization_exception_button)
        )
    }

    fun showUnauthorizedError(activity: Activity, message: String) {
        ViewUtils.showDialog(
            activity,
            activity.getString(R.string.login_tout_navbar_title),
            message
        )
    }

    fun showFacebookError(activity: Activity, viewModel: LoginToutViewModel.LoginToutViewmodel) {
        activity.showAlertDialog(
            message = activity.getString(R.string.We_can_no_longer_log_you_in_through_Facebook),
            positiveActionTitle = activity.getString(R.string.Set_new_password),
            negativeActionTitle = activity.getString(R.string.accessibility_discovery_buttons_log_in),
            isCancelable = false,
            positiveAction = {
                viewModel.inputs.onResetPasswordFacebookErrorDialogClicked()
            },
            negativeAction = {
                viewModel.inputs.onLoginFacebookErrorDialogClicked()
            }
        )
    }
}
