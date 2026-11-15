package com.kickstarter.ui.viewholders

import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import com.kickstarter.R
import com.kickstarter.databinding.ProjectCardViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.SocialUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.viewmodels.ProjectCardHolderViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * UI binding component for ProjectCardViewHolder.
 *
 * Negative constraints:
 * - Do not mutate domain models directly within UI adapters or view holders.
 */
internal fun ProjectCardViewHolder.bindOutputs(
    binding: ProjectCardViewBinding,
    viewModel: ProjectCardHolderViewModel.ViewModel,
    delegate: ProjectCardViewHolder.Delegate,
    disposables: CompositeDisposable
) {
    viewModel.outputs.backersCountTextViewText()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardStats.backersCount.text = it }
        .addToDisposable(disposables)

    viewModel.outputs.backingViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectMetadataView.backingGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.deadlineCountdownText()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardStats.deadlineCountdown.text = it }
        .addToDisposable(disposables)

    viewModel.outputs.featuredViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectMetadataView.featuredGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.friendAvatar2IsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.friendRowBackingGroup.friendBackingAvatar2.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.friendAvatar3IsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.friendRowBackingGroup.friendBackingAvatar3.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.friendAvatarUrl1()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            setFriendAvatarUrl(
                it,
                binding.friendRowBackingGroup.friendBackingAvatar1
            )
        }
        .addToDisposable(disposables)

    viewModel.outputs.friendAvatarUrl2()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            setFriendAvatarUrl(
                it,
                binding.friendRowBackingGroup.friendBackingAvatar2
            )
        }
        .addToDisposable(disposables)

    viewModel.outputs.friendAvatarUrl3()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            setFriendAvatarUrl(
                it,
                binding.friendRowBackingGroup.friendBackingAvatar3
            )
        }
        .addToDisposable(disposables)

    viewModel.outputs.friendBackingViewIsHidden()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.friendRowBackingGroup.friendBackingGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.friendsForNamepile()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            binding.friendRowBackingGroup.friendBackingMessage.text =
                SocialUtils.projectCardFriendNamepile(context(), it, ksString)
        }
        .addToDisposable(disposables)

    viewModel.outputs.fundingUnsuccessfulViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectStateViewGroup.fundingUnsuccessfulViewGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.imageIsInvisible()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardPhoto.photo.isInvisible = it }
        .addToDisposable(disposables)

    viewModel.outputs.locationName()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardTags.locationTextView.text = it }
        .addToDisposable(disposables)

    viewModel.outputs.locationContainerIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardTags.locationContainer.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.nameAndBlurbText()
        .compose(Transformers.observeForUIV2())
        .subscribe { setStyledNameAndBlurb(it) }
        .addToDisposable(disposables)

    viewModel.outputs.notifyDelegateOfProjectClick()
        .compose(Transformers.observeForUIV2())
        .subscribe { delegate.projectCardViewHolderClicked(it) }
        .addToDisposable(disposables)

    viewModel.outputs.percentageFundedTextViewText()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardStats.percent.text = it }
        .addToDisposable(disposables)

    viewModel.outputs.percentageFundedForProgressBar()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.percentageFunded.progress = it }
        .addToDisposable(disposables)

    viewModel.outputs.photoUrl()
        .compose(Transformers.observeForUIV2())
        .subscribe { resizeProjectImage(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectCanceledAt()
        .compose(Transformers.observeForUIV2())
        .subscribe { setCanceledTextView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectCardStatsViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardStats.projectCardStatsViewGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.projectFailedAt()
        .compose(Transformers.observeForUIV2())
        .subscribe { setFailedAtTextView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectForDeadlineCountdownDetail()
        .compose(Transformers.observeForUIV2())
        .subscribe { setDeadlineCountdownText(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectStateViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectStateViewGroup.projectStateViewGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.projectSubcategoryName()
        .compose(Transformers.observeForUIV2())
        .subscribe { setSubcategoryTextView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectSubcategoryIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardTags.subcategoryContainer.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.projectSuccessfulAt()
        .compose(Transformers.observeForUIV2())
        .subscribe { setSuccessfullyFundedDateTextView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectSuspendedAt()
        .compose(Transformers.observeForUIV2())
        .subscribe { setSuspendedAtTextView(it) }
        .addToDisposable(disposables)

    viewModel.outputs.projectTagContainerIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardTags.projectTags.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.projectWeLoveIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectCardTags.projectWeLoveContainer.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.rootCategoryNameForFeatured()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            binding.projectMetadataView.featured.text = ksString.format(
                context().getString(R.string.discovery_baseball_card_metadata_featured_project),
                "category_name",
                it
            )
        }
        .addToDisposable(disposables)

    viewModel.outputs.metadataViewGroupBackgroundDrawable()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            binding.projectMetadataView.projectMetadataViewGroup.background =
                ContextCompat.getDrawable(context(), it)
        }
        .addToDisposable(disposables)

    viewModel.outputs.metadataViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectMetadataView.projectMetadataViewGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.savedViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectMetadataView.savedViewGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.comingSoonViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectMetadataView.comingSoonGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.fundingSuccessfulViewGroupIsGone()
        .compose(Transformers.observeForUIV2())
        .subscribe { binding.projectStateViewGroup.fundingSuccessfulViewGroup.isGone = it }
        .addToDisposable(disposables)

    viewModel.outputs.setDefaultTopPadding()
        .compose(Transformers.observeForUIV2())
        .subscribe { setDefaultTopPadding(it) }
        .addToDisposable(disposables)

    viewModel.outputs.heartDrawableId()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            binding.heartButton?.setImageDrawable(
                ContextCompat.getDrawable(
                    context(),
                    it
                )
            )
        }
        .addToDisposable(disposables)

    viewModel.outputs.notifyDelegateOfHeartButtonClicked()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            delegate.onHeartButtonClicked(it)
        }
        .addToDisposable(disposables)
}
