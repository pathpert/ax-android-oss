package com.kickstarter.ui.fragments

import android.animation.AnimatorSet
import android.app.ActivityOptions
import android.content.Intent
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.FragmentDiscoveryBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.AnimationUtils.crossFadeAndReverse
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.getSetPasswordActivity
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ActivityFeedActivity
import com.kickstarter.ui.activities.EditorialActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.UpdateActivity
import com.kickstarter.ui.adapters.DiscoveryEditorialAdapter
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startVideoFeed
import com.kickstarter.ui.viewholders.EditorialViewHolder

/**
 * Fragment presenting UI and collecting view states for DiscoveryFragment.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
internal fun DiscoveryFragment.createHeartCrossFadeAnimation(binding: FragmentDiscoveryBinding?): AnimatorSet? {
    val outline = binding?.discoveryEmptyHeartOutline ?: return null
    val filled = binding?.discoveryEmptyHeartFilled ?: return null
    return crossFadeAndReverse(outline, filled, 400L)
}

internal fun DiscoveryFragment.findEditorialImageView(
    binding: FragmentDiscoveryBinding?,
    discoveryEditorialAdapter: DiscoveryEditorialAdapter?
): ImageView? {
    val layoutManager = binding?.discoveryRecyclerView?.layoutManager as? LinearLayoutManager
    if (layoutManager != null && discoveryEditorialAdapter != null) {
        for (i in layoutManager.findFirstVisibleItemPosition()..layoutManager.findLastVisibleItemPosition()) {
            val childView = layoutManager.getChildAt(i)
            if (childView != null) {
                val viewHolder = binding?.discoveryRecyclerView?.getChildViewHolder(childView)
                if (viewHolder is EditorialViewHolder) {
                    return childView.findViewById(R.id.editorial_graphic)
                }
            }
        }
    }
    return null
}

internal fun DiscoveryFragment.startActivityFeed() {
    context?.let {
        startActivity(Intent(context, ActivityFeedActivity::class.java))
    }
}

internal fun DiscoveryFragment.startVideoFeed() {
    activity?.startVideoFeed(ThirdPartyEventValues.ScreenName.DISCOVERY.value)
}

internal fun DiscoveryFragment.startSetPassword(email: String) {
    context?.let {
        val intent = Intent().getSetPasswordActivity(requireContext(), email)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(requireContext(), TransitionUtils.fadeIn())
    }
}

internal fun DiscoveryFragment.showStarToastNotification(confirmationResId: Int) {
    context?.let {
        ViewUtils.showToastFromTop(
            requireContext(),
            getString(confirmationResId),
            0,
            resources.getDimensionPixelSize(R.dimen.grid_8)
        )
    }
}

internal fun DiscoveryFragment.startEditorial(editorial: Editorial, editorialImageView: ImageView?) {
    val activity = activity
    if (activity != null && editorialImageView != null) {
        val intent = Intent(activity, EditorialActivity::class.java)
            .putExtra(IntentKey.EDITORIAL, editorial)
        val options = ActivityOptions.makeSceneTransitionAnimation(activity, editorialImageView, "editorial")
        startActivity(intent, options.toBundle())
    }
}

internal fun DiscoveryFragment.startLoginTout() {
    context?.let {
        val intent = Intent(context, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(it, TransitionUtils.fadeIn())
    }
}

internal fun DiscoveryFragment.startPreLaunchProject(project: Project, refTag: RefTag) {
    context?.let {
        val intent = Intent().getPreLaunchProjectActivity(requireContext(), project.slug())
            .putExtra(IntentKey.REF_TAG, refTag)
            .putExtra(
                IntentKey.PREVIOUS_SCREEN,
                ThirdPartyEventValues.ScreenName.DISCOVERY.value
            )
        startActivity(intent)
        TransitionUtils.transition(requireContext(), TransitionUtils.slideInFromRight())
    }
}

internal fun DiscoveryFragment.startProject(project: Project, refTag: RefTag) {
    context?.let {
        val intent = Intent().getProjectIntent(it)
            .putExtra(IntentKey.PROJECT_PARAM, project.slug())
            .putExtra(IntentKey.REF_TAG, refTag)
            .putExtra(IntentKey.PREVIOUS_SCREEN, ThirdPartyEventValues.ScreenName.DISCOVERY.value)
        startActivity(intent)
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}

internal fun DiscoveryFragment.startUpdate(activity: Activity) {
    context?.let {
        val intent = Intent(context, UpdateActivity::class.java)
            .putExtra(IntentKey.PROJECT, activity.project())
            .putExtra(IntentKey.UPDATE, activity.update())
        startActivity(intent)
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}
