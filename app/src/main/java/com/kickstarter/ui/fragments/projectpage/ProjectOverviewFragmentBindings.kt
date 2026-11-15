package com.kickstarter.ui.fragments.projectpage

import android.util.Pair
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.SocialUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.startCreatorBioWebViewActivity
import com.kickstarter.ui.extensions.startLoginActivity
import com.kickstarter.ui.extensions.startProjectUpdatesActivity
import com.kickstarter.ui.extensions.startReportProjectActivity
import com.kickstarter.ui.extensions.startRootCommentsActivity
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Fragment presenting UI and collecting view states for ProjectOverviewFragment.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
internal fun ProjectOverviewFragment.initViewModelSubscriptions() {
    viewModel.outputs.avatarPhotoUrl()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setAvatar(it) }
        .addToDisposable(disposables)

    viewModel.outputs.backersCountTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(binding.statsView.backersCount::setText)
        .addToDisposable(disposables)

    viewModel.outputs.blurbTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setBlurbTextViews(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.categoryTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.category.text = it
        }
        .addToDisposable(disposables)

    viewModel.outputs.commentsCountTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.projectCreatorInfoLayout.commentsCount.text = it }
        .addToDisposable(disposables)

    viewModel.outputs.creatorDetailsLoadingContainerIsVisible()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.loadingPlaceholderCreatorInfoLayout.creatorInfoLoadingContainer.isGone = !it
        }
        .addToDisposable(disposables)

    viewModel.outputs.creatorDetailsIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.creatorInfo.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.creatorNameTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.creatorName.text = it
        }
        .addToDisposable(disposables)

    viewModel.outputs.deadlineCountdownTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(binding.statsView.deadlineCountdownTextView::setText)
        .addToDisposable(disposables)

    viewModel.outputs.goalStringForTextView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setGoalTextView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.locationTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.location.text = it
        }
        .addToDisposable(disposables)

    viewModel.outputs.projectOutput()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            context?.let { currentContext ->
                binding.statsView.deadlineCountdownUnitTextView.text =
                    it.deadlineCountdownDetail(currentContext, ksString)
            }
        }
        .addToDisposable(disposables)

    viewModel.outputs.percentageFundedProgress()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(binding.percentageFunded::setProgress)
        .addToDisposable(disposables)

    viewModel.outputs.percentageFundedProgressBarIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.percentageFunded.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.pledgedTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(binding.statsView.pledged::setText)
        .addToDisposable(disposables)

    viewModel.outputs.projectDisclaimerGoalNotReachedString()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setProjectDisclaimerGoalNotReachedString(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectDisclaimerGoalReachedDateTime()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setProjectDisclaimerGoalReachedString(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectDisclaimerTextViewIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.projectCreatorInfoLayout.projectDisclaimerTextView.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.projectLaunchDate()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setProjectLaunchDateString(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectLaunchDateIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.projectCreatorDashboardHeader.projectLaunchDate.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.projectNameTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.projectName.text = it
        }
        .addToDisposable(disposables)

    viewModel.outputs.projectSocialTextViewFriends()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            context?.let { currentContext ->
                binding.projectSocialText.text =
                    SocialUtils.projectCardFriendNamepile(currentContext, it, ksString)
            }
        }
        .addToDisposable(disposables)

    viewModel.outputs.projectSocialImageViewIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.projectSocialImage.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.projectSocialImageViewUrl()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { url: String? ->
            url?.let {
                binding.projectSocialImage.loadCircleImage(it)
            }
        }
        .addToDisposable(disposables)

    viewModel.outputs.projectSocialViewGroupIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.projectSocialView.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.projectStateViewGroupBackgroundColorInt()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            context?.let { currentContext ->
                binding.projectStateViewGroup.setBackgroundColor(
                    ContextCompat.getColor(currentContext, it)
                )
            }
        }
        .addToDisposable(disposables)

    viewModel.outputs.projectStateViewGroupIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.projectStateViewGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.setCanceledProjectStateView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setCanceledProjectStateView() }
        .addToDisposable(disposables)

    viewModel.outputs.setProjectSocialClickListener()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setProjectSocialClickListener() }
        .addToDisposable(disposables)

    viewModel.outputs.setSuccessfulProjectStateView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setSuccessfulProjectStateView(it) }
        .addToDisposable(disposables)

    viewModel.setSuccessfulProjectStillCollectingView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setSuccessfulProjectStillCollectingView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.setSuspendedProjectStateView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setSuspendedProjectStateView() }
        .addToDisposable(disposables)

    viewModel.outputs.setUnsuccessfulProjectStateView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setUnsuccessfulProjectStateView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.shouldSetDefaultStatsMargins()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setStatsMargins(it) }
        .addToDisposable(disposables)

    viewModel.outputs.startProjectSocialActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startProjectSocialActivity(it) }
        .addToDisposable(disposables)

    viewModel.outputs.updatesCountTextViewText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(binding.projectCreatorInfoLayout.updatesCount::setText)
        .addToDisposable(disposables)

    viewModel.outputs.conversionPledgedAndGoalText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { pledgedAndGoal: Pair<String, String> -> setConvertedCurrencyView(pledgedAndGoal) }
        .addToDisposable(disposables)

    viewModel.outputs.conversionTextViewIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.usdConversionTextView.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.startCommentsView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            activity?.startRootCommentsActivity(it)
        }
        .addToDisposable(disposables)

    viewModel.outputs.startUpdatesView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            activity?.startProjectUpdatesActivity(it)
        }
        .addToDisposable(disposables)
    viewModel.outputs.startCreatorView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            activity?.startCreatorBioWebViewActivity(it.project())
        }
        .addToDisposable(disposables)
    viewModel.outputs.startReportProjectView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            activity?.startReportProjectActivity(it.project(), startForResult)
        }
        .addToDisposable(disposables)
    viewModel.outputs.startLoginView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            activity?.startLoginActivity()
        }
        .addToDisposable(disposables)
    viewModel.outputs.shouldShowReportProject()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.projectCreatorInfoLayout.reportProject.isGone = !it
        }
        .addToDisposable(disposables)
    viewModel.outputs.shouldShowProjectFlagged()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.projectCreatorInfoLayout.projectFlagged.isGone = !it
        }
        .addToDisposable(disposables)
    viewModel.outputs.openExternallyWithUrl()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            context?.let { it1 -> ApplicationUtils.openUrlExternally(it1, it) }
        }
        .addToDisposable(disposables)
}
