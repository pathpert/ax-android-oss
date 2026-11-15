package com.kickstarter.ui.activities

import android.content.Intent
import android.net.Uri
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.UrlUtils.commentId
import com.kickstarter.libs.utils.UrlUtils.refTag
import com.kickstarter.libs.utils.UrlUtils.saveFlag
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.path
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startVideoFeed
import com.kickstarter.ui.extensions.startWebViewActivity

/**
 * Activity displaying and coordinating user interactions for SplashScreenActivity.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
internal fun SplashScreenActivity.projectIntent(uri: Uri): Intent {
    val projectIntent = Intent().getProjectIntent(this)
        .setData(uri)
        .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.DEEPLINK.value)
    val ref = refTag(uri.toString())
    if (ref != null) {
        projectIntent.putExtra(IntentKey.REF_TAG, RefTag.from(ref))
    }
    return projectIntent
}

internal fun SplashScreenActivity.startDiscoveryActivity() {
    ApplicationUtils.startNewDiscoveryActivity(this)
    finish()
}

internal fun SplashScreenActivity.startNewDiscoveryActivityWithData(data: Uri) {
    val intent = Intent(this, DiscoveryActivity::class.java)
        .setAction(Intent.ACTION_VIEW)
        .setData(data)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    finish()
}

internal fun SplashScreenActivity.startProjectActivity(uri: Uri) {
    startActivity(projectIntent(uri))
    finish()
}

internal fun SplashScreenActivity.startProjectActivityForSave(uri: Uri) {
    val projectIntent = Intent().getProjectIntent(this)
        .setData(uri)
        .putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_SAVE, true)
        .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.DEEPLINK.value)

    saveFlag(uri.toString())?.let {
        projectIntent.putExtra(IntentKey.SAVE_FLAG_VALUE, it)
    }

    startActivity(projectIntent)
    finish()
}

internal fun SplashScreenActivity.startProjectActivityForComment(uri: Uri) {
    val projectIntent = projectIntent(uri)
        .putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, true)

    commentId(uri.toString())?.let {
        projectIntent.putExtra(IntentKey.COMMENT, it)
    }

    startActivity(projectIntent)
    finish()
}

internal fun SplashScreenActivity.startProjectActivityForCommentToUpdate(uri: Uri) {
    val path = uri.path().split("/")

    val projectIntent = projectIntent(uri)
        .putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE, path[path.lastIndex - 1])
        .putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE_COMMENT, true)

    commentId(uri.toString())?.let {
        projectIntent.putExtra(IntentKey.COMMENT, it)
    }

    startActivity(projectIntent)
    finish()
}

internal fun SplashScreenActivity.startProjectActivityForUpdate(uri: Uri) {
    val projectIntent = projectIntent(uri)
        .putExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE, uri.lastPathSegment)

    startActivity(projectIntent)
    finish()
}

internal fun SplashScreenActivity.startProjectActivityForCheckout(uri: Uri) {
    val projectIntent = projectIntent(uri)
        .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
    startActivity(projectIntent)
    finish()
}

internal fun SplashScreenActivity.startBrowser(url: String) {
    ApplicationUtils.openUrlExternally(this, url)
    finish()
}

internal fun SplashScreenActivity.startLoginForSurveys(surveyResponseUrl: String) {
    val intent = Intent(this, LoginToutActivity::class.java)
        .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
        .putExtra(IntentKey.DEEPLINK_SURVEY_RESPONSE, surveyResponseUrl)
    @Suppress("DEPRECATION")
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
}

internal fun SplashScreenActivity.startSurveyResponseActivity(surveyResponseUrl: String) {
    ApplicationUtils.startNewDiscoveryActivity(this)
    val intent = Intent(this, SurveyResponseActivity::class.java)
        .putExtra(IntentKey.DEEPLINK_SURVEY_RESPONSE, surveyResponseUrl)
    startActivity(intent)
    finish()
}

internal fun SplashScreenActivity.startPMActivity(url: String) {
    ApplicationUtils.startNewDiscoveryActivity(this)
    startWebViewActivity(url)
    finish()
}

internal fun SplashScreenActivity.startVideoFeedActivity() {
    ApplicationUtils.startNewDiscoveryActivity(this)
    startVideoFeed(ThirdPartyEventValues.ScreenName.DEEPLINK.value)
    finish()
}

