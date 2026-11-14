package com.kickstarter.ui.activities

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.extensions.startBackingDetailsWebViewActivity
import com.kickstarter.ui.extensions.startPledgeRedemption
import com.kickstarter.ui.extensions.startRootCommentsActivity
import com.kickstarter.ui.extensions.startUpdatesActivity
import com.kickstarter.ui.extensions.startVideoActivity
import com.kickstarter.ui.extensions.startWebViewActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

/**
 * Binds comments, updates, video playback, and redemption navigation outputs to [ProjectPageActivity].
 * Extracted to keep [ProjectPageActivity] within AX size constraints (< 300 LOC guidelines).
 */
internal fun ProjectPageActivity.bindNavigationAndMediaSubscriptions() {
    this.viewModel.outputs.startRootCommentsActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            startRootCommentsActivity(it)
        }.addToDisposable(disposables)

    this.viewModel.outputs.startRootCommentsForCommentsThreadActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            startRootCommentsActivity(it.second, it.first)
        }.addToDisposable(disposables)

    this.viewModel.outputs.startProjectUpdateActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            startUpdatesActivity(it.second.first, it.first.first, it.first.second)
        }.addToDisposable(disposables)

    this.viewModel.outputs.startProjectUpdateToRepliesDeepLinkActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            startUpdatesActivity(
                it.second.first,
                it.first.first,
                it.first.second.isNotEmpty(),
                it.first.second
            )
        }.addToDisposable(disposables)

    this.viewModel.outputs.startLoginToutActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { this.startLoginToutActivity() }
        .addToDisposable(disposables)

    this.viewModel.outputs.startMessagesActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startMessagesActivity(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.startThanksActivity()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showCreatePledgeSuccess(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.openBackingDetailsWebview()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startBackingDetailsWebViewActivity(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.openPledgeManagerWebview()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { startWebViewActivity(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.projectMedia()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.mediaHeader.inputs.setProjectMedia(it) }
        .addToDisposable(disposables)

    viewModel.outputs.playButtonIsVisible()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.mediaHeader.inputs.setPlayButtonVisibility(it) }
        .addToDisposable(disposables)

    viewModel.outputs.updateVideoCloseSeekPosition()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.mediaHeader.inputs.setPlayerSeekPosition(it) }
        .addToDisposable(disposables)

    binding.mediaHeader.outputs.onFullScreenClicked()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { viewModel.inputs.fullScreenVideoButtonClicked(it) }
        .addToDisposable(disposables)

    binding.mediaHeader.outputs.playButtonClicks()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { viewModel.inputs.onVideoPlayButtonClicked() }
        .addToDisposable(disposables)

    this.viewModel.outputs.onOpenVideoInFullScreen()
        .subscribeOn(Schedulers.io())
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            this.startVideoActivity(startForResult, it.first, it.second)
        }.addToDisposable(disposables)

    viewModel.outputs.backingViewGroupIsVisible()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.backingGroup.visibility = it.toVisibility()
        }.addToDisposable(disposables)

    viewModel.outputs.hideVideoPlayer()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            if (it) {
                binding.projectAppBarLayout.setExpanded(false)
            }
        }.addToDisposable(disposables)

    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            similarProjectsViewModel.parentUiState.collect {
                binding.projectPager.isUserInputEnabled = it.scrollable
            }
        }
    }

    var pBacking: Project? = null
    var user: User? = null
    viewModel.outputs.showPledgeRedemptionScreen()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            pBacking = it.first
            user = it.second
            binding.pledgeRedemptionAlpha.visibility = View.VISIBLE
        }.addToDisposable(disposables)

    binding.pledgeRedemptionAlpha.setOnClickListener {
        pBacking?.let {
            startPledgeRedemption(it)
        }
    }
}
