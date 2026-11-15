package com.kickstarter.ui.fragments

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Pair
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import coil.Coil
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Reward
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.data.PledgeStatusData
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.ui.extensions.startDisclaimerChromeTab

/**
 * Fragment presenting UI and collecting view states for BackingFragment.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
internal fun BackingFragment.stylizedTextViews(it: String) {
    binding?.receivedSectionLayout?.estimatedDeliveryLabel?.apply {
        val totalCharacters = text.length
        text = viewModel.ksString?.format(
            getString(R.string.estimated_delivery_data),
            "title", text.toString(),
            "estimated_delivery_data", it
        )
        setBoldSpanOnTextView(
            totalCharacters,
            this,
            resources.getColor(R.color.kds_support_400, null)
        )
    }

    binding?.estimatedDeliveryLabel2?.apply {
        text = viewModel.ksString?.format(
            getString(R.string.estimated_delivery_data),
            "title", text.toString(),
            "estimated_delivery_data", it
        )

        val totalCharacters2 = text.length
        setBoldSpanOnTextView(
            totalCharacters2, this, resources.getColor(R.color.kds_support_400, null)
        )
    }
}

internal fun BackingFragment.setBoldSpanOnTextView(numCharacters: Int, textView: TextView, spanColor: Int) {
    val spannable = SpannableString(textView.text)
    spannable.setSpan(
        ForegroundColorSpan(spanColor),
        0,
        numCharacters,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        0, numCharacters,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    textView.text = spannable
}

internal fun BackingFragment.bindDataToRewardViewHolder(projectAndReward: Pair<ProjectData, Reward>) {
    val project = projectAndReward.first
    val reward = projectAndReward.second
    preloadImages(listOf(reward))

    val projectAndRw = Pair(project, reward)
    rewardsAndAddOnsAdapter.populateDataForReward(projectAndRw)
}

internal fun BackingFragment.populateAddOns(projectAndAddOn: Pair<ProjectData, List<Reward>>) {
    val project = projectAndAddOn.first
    val addOns = projectAndAddOn.second
    preloadImages(addOns)

    val listData = addOns.map {
        Pair(project, it)
    }.toList()
    rewardsAndAddOnsAdapter.populateDataForAddOns(listData)
}

internal fun BackingFragment.setBackerImageView(url: String) {
    activity?.runOnUiThread {
        context?.apply {
            binding?.backingAvatar?.loadCircleImage(url)
        }
    }
}

internal fun BackingFragment.setBackerNumberText(it: String?) {
    binding?.backerNumber?.text = this.viewModel.ksString?.format(
        getString(R.string.backer_modal_backer_number),
        "backer_number",
        it
    )
}

internal fun BackingFragment.setCardExpirationText(expiration: String) {
    if (expiration.isNotEmpty()) {
        binding?.rewardCardDetails?.rewardCardExpirationDate?.visibility = View.VISIBLE
        binding?.rewardCardDetails?.rewardCardExpirationDate?.text =
            this.viewModel.ksString?.format(
                getString(R.string.Credit_card_expiration),
                "expiration_date", expiration
            )
    }
}

internal fun BackingFragment.setCardIssuerContentDescription(cardIssuerOrStringRes: Either<String, Int>) {
    val cardIssuer = cardIssuerOrStringRes.left()
    val stringRes = cardIssuerOrStringRes.right()
    binding?.rewardCardDetails?.rewardCardLogo?.contentDescription =
        stringRes?.let { getString(it) } ?: cardIssuer
}

internal fun BackingFragment.setCardLastFourText(lastFour: String) {
    binding?.rewardCardDetails?.rewardCardLastFour?.text = this.viewModel.ksString?.format(
        getString(R.string.payment_method_last_four),
        "last_four",
        lastFour
    )
}

internal fun BackingFragment.setPledgeDateText(pledgeDate: String) {
    binding?.backingDate?.text = this.viewModel.ksString?.format(
        getString(R.string.As_of_pledge_date),
        "pledge_date",
        pledgeDate
    )
}

internal fun BackingFragment.setPledgeStatusText(pledgeStatusData: PledgeStatusData) {
    val pledgeStatusText = pledgeStatusData.statusStringRes?.let {
        when (pledgeStatusData.statusStringRes) {
            R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge -> {
                this.viewModel.ksString?.let { ksString ->
                    ksString.format(
                        getString(it),
                        "total", pledgeStatusData.pledgeTotal,
                        "project_deadline", pledgeStatusData.projectDeadline
                    )
                }
            }

            R.string.If_your_project_reaches_its_funding_goal_the_backer_will_be_charged_total_on_project_deadline -> {
                this.viewModel.ksString?.let { ksString ->
                    ksString.format(
                        getString(it),
                        "total", pledgeStatusData.pledgeTotal,
                        "project_deadline", pledgeStatusData.projectDeadline
                    )
                }
            }

            R.string.You_have_selected_pledge_over_time -> {
                this.viewModel.ksString?.let { ksString ->
                    ksString.format(
                        getString(it),
                        "amount", pledgeStatusData.plotData?.plotAmount,
                        "date", pledgeStatusData.plotData?.plotFirstScheduleCollection
                    )
                }
            }

            R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline -> {
                this.viewModel.ksString?.let { ksString ->
                    ksString.format(
                        getString(it),
                        "total", pledgeStatusData.pledgeTotal,
                        "project_deadline", pledgeStatusData.projectDeadline
                    )
                }
            }

            R.string.We_cant_process_your_Pledge_Over_Time_payment -> {
                val url = pledgeStatusData.plotData?.fixPledgeUrl
                if (url != null) {
                    this.viewModel.ksString?.let { ksString ->
                        ksString.format(
                            getString(it),
                            "view_your_pledge_link", url
                        )
                    }
                } else {
                    getString(it)
                }
            }

            else -> getString(it)
        }
    }

    pledgeStatusText?.let { statusText ->
        if (pledgeStatusData.statusStringRes == R.string.We_cant_process_your_Pledge_Over_Time_payment) {
            binding?.backerPledgeStatus?.text = HtmlCompat.fromHtml(
                statusText, HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            binding?.backerPledgeStatus?.movementMethod = LinkMovementMethod.getInstance()
        } else {
            val spannablePledgeStatus = SpannableString(statusText)

            pledgeStatusData.pledgeTotal?.let { ViewUtils.addBoldSpan(spannablePledgeStatus, it) }
            pledgeStatusData.projectDeadline?.let {
                ViewUtils.addBoldSpan(spannablePledgeStatus, it)
            }
            pledgeStatusData.plotData?.plotAmount?.let { ViewUtils.addBoldSpan(spannablePledgeStatus, it) }
            pledgeStatusData.plotData?.plotFirstScheduleCollection?.let { ViewUtils.addBoldSpan(spannablePledgeStatus, it) }

            binding?.backerPledgeStatus?.text = spannablePledgeStatus
        }
    } ?: run {
        binding?.backerPledgeStatus?.text = null
    }
}

internal fun BackingFragment.openDisclaimerScreen(disclaimerItem: DisclaimerItems, environment: Environment?) {
    environment?.let {
        activity?.startDisclaimerChromeTab(disclaimerItem, it)
    } ?: run {
        context?.let {
            activity?.runOnUiThread {
                binding?.root?.let { rootView ->
                    showErrorToast(
                        it,
                        rootView,
                        getString(R.string.general_error_something_wrong)
                    )
                }
            }
        }
    }
}

internal fun BackingFragment.preloadImages(rewards: List<Reward>) {
    val rewardsIterator = rewards.iterator()
    while (rewardsIterator.hasNext()) {
        val reward = rewardsIterator.next()
        reward.image()?.let {
            val request = ImageRequest.Builder(requireContext())
                .data(reward.image()?.full())
                .build()
            Coil.imageLoader(requireContext()).enqueue(request)
        }
    }
}
