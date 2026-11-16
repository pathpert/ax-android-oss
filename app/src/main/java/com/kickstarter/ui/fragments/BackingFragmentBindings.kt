package com.kickstarter.ui.fragments

import PaymentSchedule
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.ax.errors.ProblemDetailsCopyMapper
import com.kickstarter.databinding.FragmentBackingBinding
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.ui.compose.designsystem.KSBetaBadge
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.extensions.showSnackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

/**
 * Fragment presenting UI and collecting view states for BackingFragment.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
internal fun BackingFragment.initViewModelSubscriptions(
    env: Environment?,
    binding: FragmentBackingBinding,
    disposables: CompositeDisposable
) {
    this.viewModel.outputs.backerAvatar()
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setBackerImageView(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.backerName()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.backerName.text = it }
        .addToDisposable(disposables)

    this.viewModel.outputs.backerNumber()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setBackerNumberText(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.cardLogo()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.rewardCardDetails.rewardCardLogo.setImageResource(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.cardExpiration()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setCardExpirationText(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.cardIssuer()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setCardIssuerContentDescription(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.cardLastFour()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setCardLastFourText(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.fixPaymentMethodButtonIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.fixPaymentMethodButton.isGone = it }
        .addToDisposable(disposables)

    this.viewModel.outputs.fixPaymentMethodButtonIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.fixPaymentMethodMessage.isGone = it }
        .addToDisposable(disposables)

    this.viewModel.outputs.betaBadgeIsGone().observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.betaBadgeContainer.isGone = it
        }.addToDisposable(disposables)

    this.viewModel.outputs.notifyDelegateToRefreshProject()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { (activity as BackingFragment.BackingDelegate?)?.refreshProject() }
        .addToDisposable(disposables)

    this.viewModel.outputs.notifyDelegateToShowFixPledge()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { (activity as BackingFragment.BackingDelegate?)?.showFixPaymentMethod() }
        .addToDisposable(disposables)

    this.viewModel.outputs.paymentMethodIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.paymentMethod.isGone = it }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeAmount()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.fragmentPledgeSectionSummaryPledge.pledgeSummaryAmount.text = it
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeDate()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setPledgeDateText(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeStatusData()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setPledgeStatusText(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.pledgeSummaryIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.fragmentPledgeSectionSummaryPledge.pledgeSummary.isGone = it }
        .addToDisposable(disposables)

    this.viewModel.outputs.projectDataAndReward()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { bindDataToRewardViewHolder(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.receivedCheckboxChecked()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.receivedSectionLayout.estimatedDeliveryCheckbox.isChecked = it }
        .addToDisposable(disposables)

    this.viewModel.outputs.receivedSectionIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.receivedSectionLayout.receivedSectionLayoutContainer.isGone = it
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.receivedSectionCreatorIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.estimatedDeliveryLabel2.isGone = it }
        .addToDisposable(disposables)

    this.viewModel.outputs.shippingAmount()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.fragmentPledgeSectionSummaryShipping.shippingSummaryAmount.text = it
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.shippingLocation()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.fragmentPledgeSectionSummaryShipping.shippingLabel.text =
                String.format("%s: %s", getString(R.string.Shipping), it)
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.shippingSummaryIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.fragmentPledgeSectionSummaryShipping.shippingSummary.isGone = it
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.showUpdatePledgeSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            showSnackbar(
                binding.root,
                getString(R.string.Got_it_your_changes_have_been_saved)
            )
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.totalAmount()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.fragmentBackingSectionSummaryTotal.totalSummaryAmount.text = it
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.projectDataAndAddOns()
        .filter { it.isNotNull() }
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { populateAddOns(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.bonusSupport()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { binding.sectionBonusSupport.bonusSummaryAmount.text = it }
        .addToDisposable(disposables)

    this.viewModel.outputs.estimatedDelivery()
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { stylizedTextViews(it) }
        .addToDisposable(disposables)

    this.viewModel.outputs.deliveryDisclaimerSectionIsGone()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            binding.pledgeDetailsLabel.text = getString(R.string.Pledge_details)
            binding.deliveryDisclaimerSection.root.isGone = it
            binding.estimatedDeliveryLabel2.isGone = true
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.swipeRefresherProgressIsVisible()
        .compose(Transformers.observeForUIV2())
        .subscribe {
            binding.backingSwipeRefreshLayout.isRefreshing = it
        }
        .addToDisposable(disposables)

    this.viewModel.outputs.backingError()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { problem ->
            context?.let {
                showErrorToast(
                    it,
                    binding.root,
                    ProblemDetailsCopyMapper.displayMessage(problem)
                )
            }
        }
        .addToDisposable(disposables)
}

internal fun BackingFragment.initViews(
    env: Environment?,
    binding: FragmentBackingBinding
) {
    binding.deliveryDisclaimerSection.deliveryReminderLabel.apply {
        val sb = StringBuilder(text.toString())
        sb.append(" " + resources.getString(R.string.Delays_or_changes_are_possible))
        text = sb.toString()

        val boldPortionLength = text.toString().split(".").first().length
        setBoldSpanOnTextView(
            boldPortionLength,
            this,
            resources.getColor(R.color.kds_support_400, null)
        )
    }

    binding.betaBadge.setContent {
        KSTheme {
            KSBetaBadge()
        }
    }

    binding.paymentScheduleComposeView.setContent {
        KSTheme {
            val pledgeIsPlot by viewModel.outputs.pledgeIsPlot()
                .subscribeAsState(initial = false)
            if (pledgeIsPlot) {
                val paymentIncrements by viewModel.outputs.paymentIncrements()
                    .subscribeAsState(initial = emptyList())
                val isExpanded = remember { mutableStateOf(false) }

                PaymentSchedule(
                    isExpanded = isExpanded.value,
                    onExpandChange = { isExpanded.value = it },
                    paymentIncrements = paymentIncrements,
                    ksCurrency = env?.ksCurrency(),
                    onDisclaimerClicked = { disclaimerItem ->
                        openDisclaimerScreen(disclaimerItem, env)
                    },
                )
            }
        }
    }
    binding.backingSwipeRefreshLayout.setColorSchemeResources(
        R.color.kds_create_700,
        R.color.kds_create_500,
        R.color.kds_create_300
    )
    binding.backingSwipeRefreshLayout.setOnRefreshListener {
        this.viewModel.inputs.refreshProject()
    }
    binding.fixPaymentMethodButton.setOnClickListener {
        this.viewModel.inputs.fixPaymentMethodButtonClicked()
    }
    binding.receivedSectionLayout.estimatedDeliveryCheckbox.apply {
        viewModel.inputs.receivedCheckboxToggled(this.isChecked)
    }
    binding.receivedSectionLayout.estimatedDeliveryCheckbox.setOnCheckedChangeListener { _, isChecked ->
        viewModel.inputs.receivedCheckboxToggled(isChecked)
    }
}
