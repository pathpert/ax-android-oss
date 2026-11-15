package com.kickstarter.features.projectpage.ui.components

import android.app.Activity
import android.content.Intent
import android.util.Pair
import androidx.core.net.toUri
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.models.Project
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startDisclaimerChromeTab

/**
 * Handles external navigation and intents from the project page (social share, login, messages, custom tabs).
 *
 * Negative constraints:
 * - Do not trigger navigation without an active Activity context.
 */
class ProjectNavigationDelegate(
    private val activity: Activity,
    private val environment: Environment,
    private val ksString: KSString
) {
    fun startShareIntent(projectNameAndShareUrl: Pair<String, String>) {
        val name = projectNameAndShareUrl.first
        val shareMessage = ksString.format(
            activity.getString(R.string.project_checkout_share_twitter_I_just_backed_project_on_kickstarter),
            "project_name",
            name
        )
        val url = projectNameAndShareUrl.second
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "$shareMessage $url")
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.project_accessibility_button_share_label)))
    }

    fun startLoginToutActivity() {
        val intent = Intent(activity, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.STAR_PROJECT)
        activity.startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
    }

    fun startMessagesActivity(project: Project) {
        activity.startActivity(
            Intent(activity, MessagesActivity::class.java)
                .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.PROJECT_PAGE)
                .putExtra(IntentKey.PROJECT, project.reduceProjectPayload())
                .putExtra(IntentKey.BACKING, project.backing())
        )
    }

    fun openProjectAndFinish(url: String) {
        ApplicationUtils.openUrlExternally(activity, url)
        activity.finish()
    }

    fun showDisclaimerScreen(disclaimerItem: DisclaimerItems) {
        activity.startDisclaimerChromeTab(disclaimerItem, environment)
    }

    fun showAccountabilityPage() {
        val endpoint = environment.webEndpoint()
        val trustUrl = UrlUtils.appendPath(endpoint, "trust")
        ChromeTabsHelperActivity.openCustomTab(
            activity,
            UrlUtils.baseCustomTabsIntent(activity),
            trustUrl.toUri(),
            null
        )
    }
}
