package com.kickstarter.ui.fragments

import androidx.core.view.isGone
import com.kickstarter.databinding.FragmentDiscoveryBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.adapters.DiscoveryActivitySampleAdapter
import com.kickstarter.ui.adapters.DiscoveryEditorialAdapter
import com.kickstarter.ui.adapters.DiscoveryOnboardingAdapter
import com.kickstarter.ui.adapters.DiscoveryProjectCardAdapter
import com.kickstarter.ui.adapters.DiscoveryVideoFeedBannerAdapter
import com.kickstarter.viewmodels.DiscoveryFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Fragment presenting UI and collecting view states for DiscoveryFragment.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
internal fun DiscoveryFragment.setupSwipeRefresh(
    binding: FragmentDiscoveryBinding?,
    viewModel: DiscoveryFragmentViewModel.DiscoveryFragmentViewModel,
    disposables: CompositeDisposable
) {
    binding?.discoverySwipeRefreshLayout?.let { swipeRefreshLayout ->
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.inputs.refresh()
        }

        viewModel.outputs.isFetchingProjects()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                swipeRefreshLayout.isRefreshing = it
            }
            .addToDisposable(disposables)
    }
}

internal fun DiscoveryFragment.bindAdaptersAndAnimations(
    binding: FragmentDiscoveryBinding?,
    viewModel: DiscoveryFragmentViewModel.DiscoveryFragmentViewModel,
    disposables: CompositeDisposable,
    activitySampleAdapter: DiscoveryActivitySampleAdapter,
    editorialAdapter: DiscoveryEditorialAdapter,
    onboardingAdapter: DiscoveryOnboardingAdapter,
    videoBannerAdapter: DiscoveryVideoFeedBannerAdapter,
    projectCardAdapter: DiscoveryProjectCardAdapter,
    onHeartAnimationRequest: () -> Unit
) {
    viewModel.outputs.activity()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { activitySampleAdapter.takeActivity(it) }
        .addToDisposable(disposables)

    viewModel.outputs.clearActivities()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { activitySampleAdapter.takeActivity(null) }
        .addToDisposable(disposables)

    viewModel.outputs.startHeartAnimation()
        .compose(Transformers.observeForUIV2())
        .subscribe { onHeartAnimationRequest() }
        .addToDisposable(disposables)

    viewModel.outputs.projectList()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { projectCardAdapter.takeProjects(it) }
        .addToDisposable(disposables)

    viewModel.outputs.shouldShowEditorial()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { editorialAdapter.setShouldShowEditorial(it) }
        .addToDisposable(disposables)

    viewModel.outputs.shouldShowEmptySavedView()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            binding?.discoveryEmptyView?.isGone = !it
        }
        .addToDisposable(disposables)

    viewModel.outputs.shouldShowOnboardingView()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { onboardingAdapter.setShouldShowOnboardingView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.shouldShowVideoFeedBanner()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { videoBannerAdapter.setShouldShowBanner(it) }
        .addToDisposable(disposables)
}

internal fun DiscoveryFragment.bindNavigationOutputs(
    binding: FragmentDiscoveryBinding?,
    viewModel: DiscoveryFragmentViewModel.DiscoveryFragmentViewModel,
    disposables: CompositeDisposable,
    projectStarConfirmationString: Int
) {
    viewModel.outputs.startVideoFeedActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startVideoFeed() }
        .addToDisposable(disposables)

    viewModel.outputs.showActivityFeed()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startActivityFeed() }
        .addToDisposable(disposables)

    viewModel.outputs.startSetPasswordActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startSetPassword(it) }
        .addToDisposable(disposables)

    viewModel.outputs.startEditorialActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startEditorial(it, findEditorialImageView(binding, discoveryEditorialAdapter)) }
        .addToDisposable(disposables)

    viewModel.outputs.startUpdateActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startUpdate(it) }
        .addToDisposable(disposables)

    viewModel.outputs.startProjectActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startProject(it.first, it.second) }
        .addToDisposable(disposables)

    viewModel.outputs.startPreLaunchProjectActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startPreLaunchProject(it.first, it.second) }
        .addToDisposable(disposables)

    viewModel.outputs.showLoginTout()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startLoginTout() }
        .addToDisposable(disposables)

    viewModel.outputs.startLoginToutActivityToSaveProject()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startLoginTout() }
        .addToDisposable(disposables)

    viewModel.outputs.scrollToSavedProjectPosition()
        .filter { it != -1 }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding?.discoveryRecyclerView?.smoothScrollToPosition(it)
        }
        .addToDisposable(disposables)

    viewModel.outputs.showSavedPrompt()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showStarToastNotification(projectStarConfirmationString) }
        .addToDisposable(disposables)
}
