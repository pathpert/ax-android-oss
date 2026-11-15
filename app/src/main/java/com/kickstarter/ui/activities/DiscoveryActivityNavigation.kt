package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.ui.PledgedProjectsOverviewActivity
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.viewmodels.DiscoveryViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * Activity navigation helper for [DiscoveryActivity].
 */
internal object DiscoveryActivityNavigation {

    fun bindNavigationOutputs(
        activity: DiscoveryActivity,
        viewModel: DiscoveryViewModel.DiscoveryViewModel,
        internalTools: InternalToolsType?,
        disposables: CompositeDisposable
    ) {
        viewModel.outputs.showActivityFeed()
            .compose(Transformers.observeForUIV2())
            .subscribe { startActivityFeed(activity) }
            .addToDisposable(disposables)

        viewModel.outputs.showHelp()
            .compose(Transformers.observeForUIV2())
            .subscribe { startHelpSettings(activity) }
            .addToDisposable(disposables)

        viewModel.outputs.showInternalTools()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                internalTools?.maybeStartInternalToolsActivity(activity)
            }
            .addToDisposable(disposables)

        viewModel.outputs.showLoginTout()
            .compose(Transformers.observeForUIV2())
            .subscribe { startLoginTout(activity) }
            .addToDisposable(disposables)

        viewModel.outputs.showMessages()
            .compose(Transformers.observeForUIV2())
            .subscribe { startMessageThreads(activity) }
            .addToDisposable(disposables)

        viewModel.outputs.showProfile()
            .compose(Transformers.observeForUIV2())
            .subscribe { startProfile(activity) }
            .addToDisposable(disposables)

        viewModel.outputs.showPledgedProjects()
            .compose(Transformers.observeForUIV2())
            .subscribe { startPledgedProjectsOverview(activity) }
            .addToDisposable(disposables)

        viewModel.outputs.showSettings()
            .compose(Transformers.observeForUIV2())
            .subscribe { startSettings(activity) }
            .addToDisposable(disposables)
    }

    fun startActivityFeed(activity: Activity) {
        activity.startActivity(Intent(activity, ActivityFeedActivity::class.java))
    }

    fun startHelpSettings(activity: Activity) {
        activity.startActivity(Intent(activity, HelpSettingsActivity::class.java))
    }

    fun startLoginTout(activity: Activity) {
        val intent = Intent(activity, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
        activity.startActivityWithTransition(
            intent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    fun startMessageThreads(activity: Activity) {
        val intent = Intent(activity, MessageThreadsActivity::class.java)
        activity.startActivityWithTransition(
            intent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    fun startProfile(activity: Activity) {
        val intent = Intent(activity, ProfileActivity::class.java)
        activity.startActivityWithTransition(
            intent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    fun startPledgedProjectsOverview(activity: Activity) {
        val intent = Intent(activity, PledgedProjectsOverviewActivity::class.java)
        activity.startActivityWithTransition(
            intent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    fun startSettings(activity: Activity) {
        val intent = Intent(activity, SettingsActivity::class.java)
        activity.startActivityWithTransition(
            intent,
            0,
            0
        )
    }
}
