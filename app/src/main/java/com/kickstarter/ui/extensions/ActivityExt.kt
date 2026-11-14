package com.kickstarter.ui.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Pair
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.AnimRes
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.play.core.review.ReviewManagerFactory
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.ui.BackingDetailsActivity
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.getCreatorBioWebViewActivityIntent
import com.kickstarter.libs.utils.extensions.getPledgeRedemptionIntent
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.getProjectUpdatesActivityIntent
import com.kickstarter.libs.utils.extensions.getReportProjectActivityIntent
import com.kickstarter.libs.utils.extensions.getRootCommentsActivityIntent
import com.kickstarter.libs.utils.extensions.getSearchIntent
import com.kickstarter.libs.utils.extensions.getUpdatesActivityIntent
import com.kickstarter.libs.utils.extensions.getVideoActivityIntent
import com.kickstarter.libs.utils.extensions.getVideoFeedIntent
import com.kickstarter.libs.utils.extensions.isPMOrderEditUri
import com.kickstarter.libs.utils.extensions.isPMUri
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.libs.utils.extensions.withData
import com.kickstarter.models.Project
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.services.ConnectivityReceiver
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.activities.WebViewActivity
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.CrowdfundCheckoutFragment
import timber.log.Timber

fun Activity.startActivityWithTransition(
    intent: Intent,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    startActivity(intent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, enterAnim, exitAnim)
    } else {
        overridePendingTransition(enterAnim, exitAnim)
    }
}

fun Activity.hideKeyboard() {
    val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputManager.isAcceptingText) {
        inputManager.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
        currentFocus?.clearFocus()
    }
}

/**
 * This function starts SearchAndFilter activity with transition animation included
 */
fun Activity.presentSearchActivity() {
    val intent = Intent().getSearchIntent(this)

    startActivity(intent)

    this.let {
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}

fun Activity.selectPledgeFragment(
    pledgeData: PledgeData,
    pledgeReason: PledgeReason
): Fragment {
    val fragment = CrowdfundCheckoutFragment()
    return fragment.withData(pledgeData, pledgeReason)
}

fun Activity.showSnackbar(anchor: View, stringResId: Int) {
    showSnackbar(anchor, getString(stringResId))
}

fun showSnackbar(anchor: View, message: String) {
    snackbar(anchor, message).show()
}

fun Activity.showSuccessSnackBar(anchor: View, message: String) {
    val backgroundColor = this.resources.getColor(R.color.kds_create_300, this.theme)
    val textColor = this.resources.getColor(R.color.kds_support_700, this.theme)
    showSnackbarWithColor(anchor, message, backgroundColor, textColor)
}

fun Activity.showErrorSnackBar(anchor: View, message: String) {
    val backgroundColor = this.resources.getColor(R.color.kds_alert, this.theme)
    val textColor = this.resources.getColor(R.color.kds_white, this.theme)
    showSnackbarWithColor(anchor, message, backgroundColor, textColor)
}

fun Activity.setUpConnectivityStatusCheck(lifecycle: Lifecycle) {
    val callBack = object : ConnectivityReceiver.ConnectivityReceiverListener {
        override fun onNetworkConnectionChanged(isConnected: Boolean) {
            if (!isConnected) {
                showSnackbar(findViewById(android.R.id.content), getString(R.string.Youre_offline))
            }
        }
    }
    val connectivityReceiver = ConnectivityReceiver(callBack, this)
    lifecycle.addObserver(connectivityReceiver)
}

fun Activity.showRatingDialogWidget() {
    if (BuildConfig.DEBUG) {
        AlertDialog.Builder(this)
            .setTitle("In-App Review [DEBUG]")
            .setMessage("This is where the Google Play in-app review dialog would appear in production.")
            .setPositiveButton("OK", null)
            .show()
        return
    }

    val manager = ReviewManagerFactory.create(this)
    val requestReviewTask = manager.requestReviewFlow()

    requestReviewTask.addOnCompleteListener(this) { request ->
        if (request.isSuccessful) {
            Timber.v("${this.localClassName} : showRatingDialogWidget request: ${request.isSuccessful} ")
            val reviewInfo = request.result ?: return@addOnCompleteListener
            val flow = manager.launchReviewFlow(this, reviewInfo)
            flow.addOnSuccessListener {
                Timber.v("${this.localClassName} : showRatingDialogWidget launchReviewFlow: Success")
            }
            flow.addOnFailureListener {
                Timber.v("${this.localClassName} : showRatingDialogWidget launchReviewFlow: Failure")
            }
            flow.addOnCompleteListener {
                Timber.v("${this.localClassName} : showRatingDialogWidget launchReviewFlow: Complete")
            }
        } else {
            Timber.v("${this.localClassName} : showRatingDialogWidget request: ${request.isSuccessful} ")
        }
    }
}

fun Activity.finishWithAnimation(withResult: String? = null, intentKey: String? = null) {
    intentKey?.let { key ->
        withResult?.let { result ->
            val intent = Intent().putExtra(key, result)
            setResult(Activity.RESULT_OK, intent)
        }
    }
    finish()
    TransitionUtils.transition(this, TransitionUtils.slideInFromLeft())
}

fun Activity.transition(transition: Pair<Int, Int>) {
    overridePendingTransition(transition.first, transition.second)
}
