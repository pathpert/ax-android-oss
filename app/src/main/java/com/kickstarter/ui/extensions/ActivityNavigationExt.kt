package com.kickstarter.ui.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import com.kickstarter.R
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
import com.kickstarter.libs.utils.extensions.getUpdatesActivityIntent
import com.kickstarter.libs.utils.extensions.getVideoActivityIntent
import com.kickstarter.libs.utils.extensions.getVideoFeedIntent
import com.kickstarter.libs.utils.extensions.isPMOrderEditUri
import com.kickstarter.libs.utils.extensions.isPMUri
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.features.pledgedprojectsoverview.ui.BackingDetailsActivity
import com.kickstarter.models.Project
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.activities.WebViewActivity
import com.kickstarter.ui.data.ProjectData

fun Activity.startPledgeRedemption(project: Project) {
    startActivity(
        Intent().getPledgeRedemptionIntent(this, project)
    )

    this.let {
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}

fun Activity.startVideoFeed(entrySurface: String) {
    startActivity(
        Intent().getVideoFeedIntent(this, entrySurface)
    )

    this.let {
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}

fun Activity.startShareIntent(projectNameAndShareUrl: Pair<String, String>) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "${projectNameAndShareUrl.first} ${projectNameAndShareUrl.second}")
    }
    startActivity(Intent.createChooser(shareIntent, getString(R.string.project_accessibility_button_share_label)))
}

/**
 * This function starts the RootCommentActivity with Transition animation included
 * @param projectData
 * @param commentableId -> specific for deeplinking to a concrete thread
 */
fun Activity.startRootCommentsActivity(projectData: ProjectData, commentableId: String? = null) {
    val reducedProject = projectData.project().reduceProjectPayload()
    startActivity(
        Intent().getRootCommentsActivityIntent(this, projectData.toBuilder().project(reducedProject).build(), commentableId)
    )

    this.let {
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}

fun Activity.startLoginActivity() {
    startActivity(Intent(this, LoginToutActivity::class.java))
}

fun Activity.startReportProjectActivity(
    project: Project,
    startForResult: ActivityResultLauncher<Intent>
) {
    startForResult.launch(Intent().getReportProjectActivityIntent(this, project = project.reduceProjectPayload()))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.startCreatorBioWebViewActivity(project: Project) {
    val reducedProject = project.reduceProjectPayload().toBuilder()
        .deadline(project.deadline())
        .backing(project.backing())
        .build()
    startActivity(Intent().getCreatorBioWebViewActivityIntent(this, reducedProject))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.startVideoActivity(
    startForResult: ActivityResultLauncher<Intent>,
    videoSource: String,
    videoSeekPosition: Long
) {
    startForResult.launch(Intent().getVideoActivityIntent(this, videoSource, videoSeekPosition))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

/**
 * This function starts the General Updates activity with Transition animation included
 * the Transition occurs slide_in_right -> fade_out_slide_out_left
 * @param project
 * @param updatePostId -> for deeplink to an specific post update
 * @param isUpdateComment -> for deeplink navigation into a comment for a concrete post
 * @param comment -> to open the comments activity to a specific thread
 */
fun Activity.startUpdatesActivity(
    project: Project,
    updatePostId: String? = null,
    isUpdateComment: Boolean? = null,
    comment: String? = null
) {
    startActivity(
        Intent().getUpdatesActivityIntent(
            this,
            project,
            updatePostId,
            isUpdateComment,
            comment
        )
    )
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

/**
 * This function starts the Project Updates activity with Transition animation included
 * the Transition occurs slide_in_right -> fade_out_slide_out_left
 * @param projectAndData
 */
fun Activity.startProjectUpdatesActivity(projectAndData: ProjectData) {
    val reducedProject = projectAndData.project().reduceProjectPayload()
    startActivity(Intent().getProjectUpdatesActivityIntent(this, projectAndData.toBuilder().project(reducedProject).build()))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

@SuppressLint("IntentWithNullActionLaunch") // Lint bug: https://issuetracker.google.com/issues/294200850
fun Activity.startPreLaunchProjectActivity(uri: Uri? = null, project: Project, previousScreen: String? = null, refTag: RefTag? = null) {
    val intent = Intent().getPreLaunchProjectActivity(
        this,
        project.slug(),
        project.reduceProjectPayload()
    )
    uri?.let {
        // Pass full deeplink for attribution tracking purposes when launching from deeplink
        intent.setData(uri)
    }

    val ref = refTag ?: UrlUtils.refTag(uri.toString())
    ref?.let {
        if (ref is String?) intent.putExtra(IntentKey.REF_TAG, RefTag.from(ref))
        else if (ref is RefTag) intent.putExtra(IntentKey.REF_TAG, ref)
    }
    previousScreen?.let { intent.putExtra(IntentKey.PREVIOUS_SCREEN, it) }
    startActivity(intent)
    TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
}

fun Activity.startProjectActivity(project: Project, refTag: RefTag, previousScreen: String?) {
    val intent = Intent().getProjectIntent(this)

    intent.putExtra(IntentKey.PROJECT, project)
    intent.putExtra(IntentKey.REF_TAG, refTag)
    previousScreen?.let {
        intent.putExtra(IntentKey.PREVIOUS_SCREEN, previousScreen)
    }

    startActivity(intent)
    TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
}

fun Activity.startDisclaimerChromeTab(disclaimerItem: DisclaimerItems, environment: Environment?) {
    val path = when (disclaimerItem) {
        DisclaimerItems.TERMS -> HelpActivity.TERMS_OF_USE
        DisclaimerItems.PRIVACY -> HelpActivity.PRIVACY
        DisclaimerItems.COOKIES -> HelpActivity.COOKIES
        DisclaimerItems.HELP -> Secrets.HelpCenter.ENDPOINT
    }

    val url = if (disclaimerItem == DisclaimerItems.HELP) {
        path
    } else environment?.let {
        UrlUtils.appendPath(it.webEndpoint(), path)
    } ?: run {
        ""
    }

    val intent = when (disclaimerItem) {
        DisclaimerItems.TERMS -> Intent(this, HelpActivity.Terms::class.java)
        DisclaimerItems.PRIVACY -> Intent(this, HelpActivity.Privacy::class.java)
        DisclaimerItems.COOKIES -> Intent(this, HelpActivity.CookiePolicy::class.java)
        DisclaimerItems.HELP -> Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.HelpCenter.ENDPOINT))
    }

    val uri = Uri.parse(url)

    val fallback = object : ChromeTabsHelperActivity.CustomTabFallback {
        override fun openUri(activity: Activity, uri: Uri) {
            activity.startActivity(intent)
        }
    }

    ChromeTabsHelperActivity.openCustomTab(this, UrlUtils.baseCustomTabsIntent(this), uri, fallback)
}

fun Activity.startCreatorMessageActivity(project: Project, previousScreen: MessagePreviousScreenType) {
    startActivity(
        Intent(this, MessagesActivity::class.java)
            .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, previousScreen)
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.BACKING, project.backing())
    )
}

fun Activity.startBackingDetailsWebViewActivity(url: String) {
    startActivity(
        Intent(this, BackingDetailsActivity::class.java)
            .putExtra(IntentKey.URL, url)
    )
}

fun Activity.startWebViewActivity(url: String) {
    val uri = url.toUri()
    val toolbarTitle = when {
        uri.isPMUri(url) -> getString(R.string.Pledge_manager)
        uri.isPMOrderEditUri(url, isFFEnabled = true) -> getString(R.string.Review_edits_made_to_your_order)
        else -> getString(R.string.Pledge_manager)
    }

    startActivity(
        Intent(this, WebViewActivity::class.java)
            .putExtra(IntentKey.URL, url)
            .putExtra(IntentKey.TOOLBAR_TITLE, toolbarTitle)
    )
}
