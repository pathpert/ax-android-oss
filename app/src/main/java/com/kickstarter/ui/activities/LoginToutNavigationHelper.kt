package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getResetPasswordIntent
import com.kickstarter.models.chrome.ChromeTabsHelper
import com.kickstarter.services.apiresponses.ErrorEnvelope.FacebookUser
import com.kickstarter.ui.IntentKey

/**
 * Navigation and external launcher helper for [LoginToutActivity].
 */
internal object LoginToutNavigationHelper {

    fun openChromeTabOrWebViewWithUrl(
        activity: Activity,
        url: String,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val authorizationUri = url.toUri()
        val tabIntent = CustomTabsIntent.Builder().build()
        val packageName = ChromeTabsHelper.getPackageNameToUse(activity)
        if (packageName == "com.android.chrome") {
            tabIntent.intent.setPackage(packageName)
            tabIntent.launchUrl(activity, authorizationUri)
        } else {
            val intent = Intent(activity, OAuthWebViewActivity::class.java)
                .putExtra(IntentKey.URL, authorizationUri.toString())
            launcher.launch(intent)
            activity.overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
        }
    }

    fun startResetActivity(activity: Activity) {
        val intent = Intent().getResetPasswordIntent(activity, isResetPasswordFacebook = true)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    fun startFacebookConfirmationActivity(
        activity: Activity,
        facebookUser: FacebookUser,
        accessTokenString: String
    ) {
        val intent = Intent(activity, FacebookConfirmationActivity::class.java)
            .putExtra(IntentKey.FACEBOOK_USER, facebookUser)
            .putExtra(IntentKey.FACEBOOK_TOKEN, accessTokenString)
        activity.startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(activity, TransitionUtils.fadeIn())
    }

    fun goToSurveyIfSurveyPresent(activity: Activity, intent: Intent) {
        val surveyResponseDeeplink = IntentCompat.getParcelableExtra(
            intent,
            IntentKey.DEEPLINK_SURVEY_RESPONSE,
            String::class.java
        )

        surveyResponseDeeplink?.let {
            ApplicationUtils.startNewDiscoveryActivity(activity)
            val surveyIntent = Intent(activity, SurveyResponseActivity::class.java)
                .putExtra(IntentKey.DEEPLINK_SURVEY_RESPONSE, it)
            activity.startActivity(surveyIntent)
            activity.finish()
        }
    }
}
