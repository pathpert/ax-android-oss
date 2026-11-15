package com.kickstarter.ui.fragments

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.core.net.toUri
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.extensions.startDisclaimerChromeTab

/**
 * Navigation and web browser helper for [CrowdfundCheckoutFragment].
 */
internal object CrowdfundCheckoutNavigationHelper {

    fun openDisclaimerScreen(
        activity: Activity?,
        context: Context?,
        view: ViewGroup?,
        disclaimerItem: DisclaimerItems,
        environment: Environment?
    ) {
        activity?.let { act ->
            environment?.let {
                act.startDisclaimerChromeTab(disclaimerItem, environment)
            } ?: view?.let { v ->
                context?.let { ctx ->
                    act.runOnUiThread {
                        showErrorToast(ctx, v, act.getString(R.string.general_error_something_wrong))
                    }
                }
            }
        }
    }

    fun showAccountabilityPage(
        activity: Activity?,
        context: Context?,
        view: ViewGroup?,
        environment: Environment?
    ) {
        activity?.let { act ->
            context?.let { ctx ->
                environment?.let { env ->
                    val endpoint = env.webEndpoint()
                    val trustUrl = UrlUtils.appendPath(endpoint, "trust")
                    ChromeTabsHelperActivity.openCustomTab(
                        act,
                        UrlUtils.baseCustomTabsIntent(ctx),
                        trustUrl.toUri(),
                        null
                    )
                } ?: view?.let { v ->
                    act.runOnUiThread {
                        showErrorToast(ctx, v, act.getString(R.string.general_error_something_wrong))
                    }
                }
            }
        }
    }
}
