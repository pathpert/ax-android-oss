package com.kickstarter.features.pledgedprojectsoverview.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProfileActivity
import com.stripe.android.Stripe

/**
 * Feature component for Activity in pledgedprojectsoverview module.
 *
 * Negative constraints:
 * - Do not bypass feature-scoped boundary contracts or leak internal UI state.
 */
fun Activity.openBackingDetailsWebView(
    url: String,
    toolbarTitle: Int,
    resultLauncher: ActivityResultLauncher<Intent>?
) {
    val intent = Intent(this, BackingDetailsActivity::class.java)
        .putExtra(IntentKey.URL, url)
        .putExtra(IntentKey.TOOLBAR_TITLE, this.getString(toolbarTitle))

    if (resultLauncher.isNotNull()) {
        resultLauncher?.launch(intent)
    } else {
        startActivity(intent)
    }

    TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
}

fun Activity.startProfileActivity() {
    startActivity(
        Intent(this, ProfileActivity::class.java)
    )
    TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
}

fun Activity.openManagePledge(
    projectSlug: String,
    resultLauncher: ActivityResultLauncher<Intent>
) {
    resultLauncher.launch(
        Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT_PARAM, projectSlug)
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
            .putExtra(IntentKey.REF_TAG, RefTag.activity())
    )
    TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
}

fun ComponentActivity.stripeNextAction(it: String, stripe: Stripe): DomainResult<Unit> {
    return try {
        if (it.contains("pi_")) {
            stripe.handleNextActionForPayment(this, it)
        } else {
            stripe.handleNextActionForSetupIntent(this, it)
        }
        DomainResult.Success(Unit)
    } catch (exception: Exception) {
        FirebaseCrashlytics.getInstance().recordException(exception)
        DomainResult.Failure(
            ProblemDetails(
                type = ProblemDetails.TYPE_UNEXPECTED,
                title = "Stripe Next Action Failed",
                status = 500,
                detail = exception.message ?: "Failed to execute Stripe next action.",
                suggestedAction = "Verify payment details and try again.",
                isRetriable = true
            )
        )
    }
}

fun Activity.startProjectActivity(projectSlug: String) {
    val intent = Intent().getProjectIntent(this)
        .putExtra(IntentKey.PROJECT_PARAM, projectSlug)
        .putExtra(IntentKey.REF_TAG, RefTag.activity())
    startActivity(intent)
    @Suppress("DEPRECATION")
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}
