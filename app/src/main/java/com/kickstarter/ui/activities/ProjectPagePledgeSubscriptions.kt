package com.kickstarter.ui.activities

import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.showLatePledgeFlow
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.ui.adapters.ProjectPagerAdapter
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Binds pledge flow, backing details, and sheet-state outputs to [ProjectPageActivity].
 * Extracted to keep [ProjectPageActivity] within AX size constraints (< 300 LOC guidelines).
 */
internal fun ProjectPageActivity.bindPledgeAndUiSubscriptions() {
    this.viewModel.outputs.showLatePledgeFlow()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showLatePledgeFlow ->
            if (showLatePledgeFlow) {
                binding.pledgeContainerLayout.pledgeContainerRoot.isGone = true
                latePledgesSetUp(binding.pledgeContainerCompose)
            } else {
                binding.pledgeContainerCompose.isGone = true
                binding.pledgeContainerLayout.pledgeContainerRoot.isGone = false
            }
        }.addToDisposable(disposables)

    this.viewModel.outputs.projectData()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            (binding.projectPager.adapter as? ProjectPagerAdapter)?.updatedWithProjectData(it)

            if (it.project().showLatePledgeFlow()) {
                rewardsSelectionViewModel.provideProjectData(it)
                addOnsViewModel.provideProjectData(it)
            }
        }.addToDisposable(disposables)

    this.viewModel.outputs.updateTabs()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { updateTabs ->
            configureTabs(updateTabs)
            configurePager(pagerAdapterList)
        }.addToDisposable(disposables)

    this.viewModel.outputs.backingDetailsSubtitle()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setBackingDetailsSubtitle(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.backingDetailsTitle()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.pledgeContainerLayout.backingDetailsTitle.setText(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.backingDetailsIsVisible()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { styleProjectActionButton(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.expandPledgeSheet()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { expandPledgeSheet(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.goBack()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { onBackPressedDispatcher.onBackPressed() }
        .addToDisposable(disposables)

    this.viewModel.outputs.heartDrawableId()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.heartIcon.setImageDrawable(ContextCompat.getDrawable(this, it)) }
        .addToDisposable(disposables)

    viewModel.managePledgeMenuOptions()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { updateManagePledgeMenuVisibility(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeActionButtonColor()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.pledgeContainerLayout.pledgeActionButton.backgroundTintList =
                ContextCompat.getColorStateList(this, it)
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeActionButtonContainerIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.pledgeContainerLayout.pledgeActionButtonsLayout.visibility =
                (!it).toVisibility()
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeActionButtonText()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setPledgeActionButtonCTA(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeToolbarNavigationIcon()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.pledgeContainerLayout.pledgeToolbar.navigationIcon =
                ContextCompat.getDrawable(this, it)
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeToolbarTitle()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.pledgeContainerLayout.pledgeToolbar.title = getString(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.prelaunchUrl()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { openProjectAndFinish(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.reloadProjectContainerIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.pledgeContainerLayout.projectRetryLayout.pledgeSheetRetryContainer.visibility =
                (!it).toVisibility()
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.reloadProgressBarIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.pledgeContainerLayout.projectRetryLayout.pledgeSheetProgressBar.visibility =
                (!it).toVisibility()
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.scrimIsVisible()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { animateScrimVisibility(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.setInitialRewardsContainerY()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setInitialRewardsContainerY() }
        .addToDisposable(disposables)

    this.viewModel.outputs.showCancelPledgeSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showCancelPledgeSuccess() }
        .addToDisposable(disposables)

    this.viewModel.outputs.showUpdatePledgeSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showUpdatePledgeSuccess() }
        .addToDisposable(disposables)

    this.viewModel.outputs.showCancelPledgeFragment()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showCancelPledgeFragment(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.showPledgeNotCancelableDialog()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showPledgeNotCancelableDialog() }
        .addToDisposable(disposables)

    this.viewModel.outputs.revealRewardsFragment()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { revealRewardsFragment() }
        .addToDisposable(disposables)

    this.viewModel.outputs.showSavedPrompt()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { this.showStarToast() }
        .addToDisposable(disposables)

    this.viewModel.outputs.showShareSheet()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startShareIntent(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.showSocialShareSheet()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showSocialShareSheet(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.showUpdatePledge()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showPledgeFragment(it) }
        .addToDisposable(disposables)
}
