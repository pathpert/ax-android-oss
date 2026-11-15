package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.libs.Either
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProjectPagerAdapter
import com.kickstarter.ui.data.ManagePledgeMenuOptions
import com.kickstarter.ui.data.ProjectData

/**
 * Activity displaying and coordinating user interactions for ProjectPageActivity.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
internal fun ProjectPageActivity.setClickListeners() {
    binding.pledgeContainerLayout.pledgeActionButton.setOnClickListener {
        this.viewModel.inputs.nativeProjectActionButtonClicked()
    }

    binding.pledgeContainerLayout.pledgeToolbar.setNavigationOnClickListener {
        this.viewModel.inputs.pledgeToolbarNavigationClicked()
    }

    binding.pledgeContainerLayout.pledgeToolbar.setOnMenuItemClickListener {
        when (it.itemId) {
            R.id.rewards,
            R.id.edit_pledge,
            R.id.choose_another_reward -> {
                this.viewModel.inputs.viewRewardsClicked()
                true
            }
            R.id.update_payment -> {
                this.viewModel.inputs.updatePaymentClicked()
                true
            }
            R.id.cancel_pledge -> {
                this.viewModel.inputs.cancelPledgeClicked()
                true
            }
            R.id.contact_creator -> {
                this.viewModel.inputs.contactCreatorClicked()
                true
            }
            else -> false
        }
    }

    binding.pledgeContainerLayout.projectRetryLayout.pledgeSheetRetryContainer.setOnClickListener {
        this.viewModel.inputs.reloadProjectContainerClicked()
    }

    binding.heartIcon.setOnClickListener {
        this.viewModel.inputs.heartButtonClicked()
    }

    binding.shareIcon.setOnClickListener {
        this.viewModel.inputs.shareButtonClicked()
    }
}

internal fun ProjectPageActivity.setBackingDetailsSubtitle(stringResOrTitle: Either<String, Int>?) {
    stringResOrTitle?.let { either ->
        @StringRes val stringRes = either.right()
        val title = either.left()
        binding.pledgeContainerLayout.backingDetailsSubtitle.text =
            stringRes?.let { getString(it) } ?: title
    }
}

internal fun ProjectPageActivity.setPledgeActionButtonCTA(stringRes: Int) {
    binding.pledgeContainerLayout.pledgeActionButton.setText(stringRes)
    binding.pledgeContainerLayout.pledgeActionButton.contentDescription = when (stringRes) {
        R.string.Manage -> getString(R.string.Manage_your_pledge)
        else -> getString(stringRes)
    }
}

internal fun ProjectPageActivity.showUpdatePledgeSuccess() {
    clearFragmentBackStack()
    backingFragment()?.pledgeSuccessfullyUpdated()
    val intent = Intent()
        .putExtra(IntentKey.REFRESH_PPO_LIST, REFRESH)
    setResult(Activity.RESULT_OK, intent)
}

internal fun ProjectPageActivity.styleProjectActionButton(detailsAreVisible: Boolean) {
    val buttonParams =
        binding.pledgeContainerLayout.pledgeActionButton.layoutParams as LinearLayout.LayoutParams
    when {
        detailsAreVisible -> {
            binding.pledgeContainerLayout.backingDetails.visibility = View.VISIBLE
            buttonParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            binding.pledgeContainerLayout.pledgeActionButton.cornerRadius =
                resources.getDimensionPixelSize(R.dimen.grid_2)
        }

        else -> {
            binding.pledgeContainerLayout.backingDetails.visibility = View.GONE
            buttonParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            binding.pledgeContainerLayout.pledgeActionButton.cornerRadius =
                resources.getDimensionPixelSize(R.dimen.fab_radius)
        }
    }
    binding.pledgeContainerLayout.pledgeActionButton.layoutParams = buttonParams
}

internal fun ProjectPageActivity.updateFragments(projectData: ProjectData) {
    try {
        (binding.projectPager.adapter as? ProjectPagerAdapter)?.updatedWithProjectData(
            projectData
        )

        val rewardsFragment = rewardsFragment()
        val backingFragment = backingFragment()
        if (rewardsFragment != null && backingFragment != null) {
            when (supportFragmentManager.backStackEntryCount) {
                0 -> when {
                    projectData.project().isBacking() -> if (!rewardsFragment.isHidden) {
                        supportFragmentManager.beginTransaction()
                            .show(backingFragment)
                            .hide(rewardsFragment)
                            .commitNow()
                    }

                    else -> if (!backingFragment.isHidden) {
                        supportFragmentManager.beginTransaction()
                            .show(rewardsFragment)
                            .hide(backingFragment)
                            .commitNow()
                    }
                }
            }
            renderProject(backingFragment, rewardsFragment, projectData)
        }
    } catch (e: IllegalStateException) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

internal fun ProjectPageActivity.updateManagePledgeMenuVisibility(options: ManagePledgeMenuOptions) {
    val menu = binding.pledgeContainerLayout.pledgeToolbar.menu
    menu.findItem(R.id.edit_pledge)?.isVisible = options.showEditPledge
    menu.findItem(R.id.choose_another_reward)?.isVisible = options.showChooseAnotherReward
    menu.findItem(R.id.rewards)?.isVisible = options.showSeeRewards
    menu.findItem(R.id.update_payment)?.isVisible = options.showUpdatePayment
    menu.findItem(R.id.cancel_pledge)?.isVisible = options.showCancelPledge
    menu.findItem(R.id.contact_creator)?.isVisible = options.showContactCreator
}
