package com.kickstarter.ui.fragments.projectpage

import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.SpannableString
import android.text.TextUtils
import android.util.Pair
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.kickstarter.R
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectSocialActivity
import com.kickstarter.ui.extensions.loadCircleImage
import org.joda.time.DateTime

/**
 * Fragment presenting UI and collecting view states for ProjectOverviewFragment.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
internal fun ProjectOverviewFragment.setAvatar(url: String) {
    binding.avatar.loadCircleImage(url)
}

internal fun ProjectOverviewFragment.setConvertedCurrencyView(pledgedAndGoal: Pair<String, String>) {
    context?.let { currentContext ->
        binding.usdConversionTextView.text = ksString.format(
            currentContext.getString(R.string.discovery_baseball_card_stats_convert_from_pledged_of_goal),
            "pledged",
            pledgedAndGoal.first,
            "goal",
            pledgedAndGoal.second
        )
    }
}

internal fun ProjectOverviewFragment.setGoalTextView(goalString: String) {
    context?.let { currentContext ->
        val goalText = if (ViewUtils.isFontScaleLarge(currentContext))
            ksString.format(
                currentContext.getString(R.string.discovery_baseball_card_stats_pledged_of_goal_short),
                "goal",
                goalString
            )
        else
            ksString.format(
                currentContext.getString(R.string.discovery_baseball_card_stats_pledged_of_goal),
                "goal",
                goalString
            )

        binding.statsView.goal.text = goalText
    }
}

internal fun ProjectOverviewFragment.setCanceledProjectStateView() {
    binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
    binding.projectStateSubheadTextView.setText(R.string.project_status_funding_project_canceled_by_creator)
}

internal fun ProjectOverviewFragment.setBlurbTextViews(blurb: String) {
    val blurbHtml =
        Html.fromHtml(TextUtils.htmlEncode(blurb), FROM_HTML_MODE_LEGACY)
    binding.blurb.text = blurbHtml
}

internal fun ProjectOverviewFragment.setProjectDisclaimerGoalReachedString(deadline: DateTime) {
    context?.let { currentContext ->
        binding.projectCreatorInfoLayout.projectDisclaimerTextView.text = ksString.format(
            currentContext.getString(R.string.project_disclaimer_goal_reached),
            "deadline",
            DateTimeUtils.mediumDateShortTimeWithTimeZone(deadline)
        )
    }
}

internal fun ProjectOverviewFragment.setProjectDisclaimerGoalNotReachedString(goalAndDeadline: Pair<String, DateTime>) {
    context?.let { currentContext ->
        binding.projectCreatorInfoLayout.projectDisclaimerTextView.text = ksString.format(
            currentContext.getString(R.string.project_disclaimer_goal_not_reached),
            "goal_currency",
            goalAndDeadline.first,
            "deadline",
            DateTimeUtils.mediumDateShortTimeWithTimeZone(goalAndDeadline.second)
        )
    }
}

internal fun ProjectOverviewFragment.setProjectLaunchDateString(launchDate: String) {
    context?.let { currentContext ->
        val launchedDateSpannableString = SpannableString(
            ksString.format(
                currentContext.getString(R.string.You_launched_this_project_on_launch_date),
                "launch_date",
                launchDate
            )
        )
        ViewUtils.addBoldSpan(launchedDateSpannableString, launchDate)
        binding.projectCreatorDashboardHeader.projectLaunchDate.text =
            launchedDateSpannableString
    }
}

internal fun ProjectOverviewFragment.setProjectSocialClickListener() {
    context?.let { currentContext ->
        binding.projectSocialView.background = ResourcesCompat.getDrawable(
            currentContext.resources,
            R.drawable.click_indicator_light_masked,
            null
        )
        binding.projectSocialView.setOnClickListener { viewModel.inputs.projectSocialViewGroupClicked() }
    }
}

internal fun ProjectOverviewFragment.setSuccessfulProjectStateView(stateChangedAt: DateTime) {
    context?.let { currentContext ->
        binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
        binding.projectStateSubheadTextView.text =
            ksString.format(
                currentContext.getString(R.string.project_status_project_was_successfully_funded_on_deadline),
                "deadline",
                DateTimeUtils.mediumDate(stateChangedAt)
            )
    }
}

internal fun ProjectOverviewFragment.setSuccessfulProjectStillCollectingView(stateChangedAt: DateTime) {
    context?.let { currentContext ->
        binding.projectStateHeaderTextView.setText(R.string.project_status_funded)
        binding.projectStateSubheadTextView.text =
            ksString.format(
                currentContext.getString(R.string.project_status_project_was_successfully_funded_on_deadline_but_you_can_still_pledge_for_available_rewards),
                "deadline",
                DateTimeUtils.mediumDate(stateChangedAt)
            )
    }
}

internal fun ProjectOverviewFragment.setSuspendedProjectStateView() {
    binding.projectStateHeaderTextView.setText(R.string.project_status_funding_suspended)
    binding.projectStateSubheadTextView.setText(R.string.project_status_funding_project_suspended)
}

internal fun ProjectOverviewFragment.setUnsuccessfulProjectStateView(stateChangedAt: DateTime) {
    context?.let { currentContext ->
        binding.projectStateHeaderTextView.text =
            currentContext.getString(R.string.project_status_funding_unsuccessful)
        binding.projectStateSubheadTextView.text = ksString.format(
            currentContext.getString(R.string.project_status_project_funding_goal_not_reached),
            "deadline",
            DateTimeUtils.mediumDate(stateChangedAt)
        )
    }
}

internal fun ProjectOverviewFragment.setStatsMargins(shouldSetDefaultMargins: Boolean) {
    context?.let { currentContext ->
        (binding.projectStatsView as? LinearLayout)?.let { projectStatsView ->
            if (shouldSetDefaultMargins) {
                ViewUtils.setLinearViewGroupMargins(
                    projectStatsView,
                    0, currentContext.resources.getDimension(R.dimen.grid_3).toInt(),
                    0, currentContext.resources.getDimension(R.dimen.grid_2).toInt()
                )
            } else {
                ViewUtils.setLinearViewGroupMargins(
                    projectStatsView,
                    0, currentContext.resources.getDimension(R.dimen.grid_3).toInt(),
                    0, currentContext.resources.getDimension(R.dimen.grid_4).toInt()
                )
            }
        }
    }
}

internal fun ProjectOverviewFragment.startProjectSocialActivity(project: Project) {
    context?.let { currentContext ->
        val intent = Intent(currentContext, ProjectSocialActivity::class.java)
            .putExtra(IntentKey.PROJECT, project)
        activity?.startActivity(intent)
        activity?.overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }
}
