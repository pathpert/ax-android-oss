package com.kickstarter.ui.activities

import android.util.Pair
import androidx.compose.ui.platform.ComposeView
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.models.Project
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.selectPledgeFragment
import com.kickstarter.ui.fragments.BackingFragment
import com.kickstarter.ui.fragments.RewardsFragment
import com.kickstarter.viewmodels.projectpage.PagerTabConfig
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.model.PaymentOption

internal fun ProjectPageActivity.showSocialShareSheet(project: Project) =
    socialShareComposeHost.showSocialShareSheet(project)

internal fun ProjectPageActivity.socialShareSetUp(composeView: ComposeView) =
    socialShareComposeHost.setup()

internal fun ProjectPageActivity.latePledgesSetUp(composeView: ComposeView) =
    latePledgesComposeHost.setup()

internal fun ProjectPageActivity.showDisclaimerScreen(disclaimerItem: DisclaimerItems, environment: Environment) =
    navigationDelegate.showDisclaimerScreen(disclaimerItem)


internal fun ProjectPageActivity.configureTabs(updateTabs: List<PagerTabConfig>) =
    pagerController.configureTabs(updateTabs)

internal fun ProjectPageActivity.configurePager(pagerList: List<ProjectPagerTabs>) =
    pagerController.configurePager(pagerList)

internal fun ProjectPageActivity.getTabTitle(position: Int, pagerList: List<ProjectPagerTabs>) =
    pagerController.getTabTitle(position, pagerList)

internal fun ProjectPageActivity.animateScrimVisibility(show: Boolean) =
    pledgeSheetController.animateScrimVisibility(show)

internal fun ProjectPageActivity.backingFragment() = pledgeSheetController.backingFragment()

internal fun ProjectPageActivity.clearFragmentBackStack(): Boolean = pledgeSheetController.clearFragmentBackStack()

internal fun ProjectPageActivity.expandPledgeSheet(expandAndAnimate: Pair<Boolean, Boolean>) =
    pledgeSheetController.expandPledgeSheet(expandAndAnimate)

internal fun ProjectPageActivity.setFragmentsState(expand: Boolean) =
    pledgeSheetController.setFragmentsState(expand)

internal fun ProjectPageActivity.handleNativeCheckoutBackPress() =
    pledgeSheetController.handleNativeCheckoutBackPress()

internal fun ProjectPageActivity.openProjectAndFinish(url: String) =
    navigationDelegate.openProjectAndFinish(url)

internal fun ProjectPageActivity.renderProject(
    backingFragment: BackingFragment,
    rewardsFragment: RewardsFragment,
    projectData: ProjectData,
) = pledgeSheetController.renderProject(backingFragment, rewardsFragment, projectData)

internal fun ProjectPageActivity.revealRewardsFragment() = pledgeSheetController.revealRewardsFragment()

internal fun ProjectPageActivity.rewardsFragment() = pledgeSheetController.rewardsFragment()

internal fun ProjectPageActivity.rewardsSheetGuideline(): Int = pledgeSheetController.rewardsSheetGuideline()

internal fun ProjectPageActivity.setInitialRewardsContainerY() = pledgeSheetController.setInitialRewardsContainerY()

internal fun ProjectPageActivity.showCancelPledgeFragment(project: Project) = pledgeSheetController.showCancelPledgeFragment(project)

internal fun ProjectPageActivity.showCancelPledgeSuccess() = dialogsAndFeedback.showCancelPledgeSuccess()

internal fun ProjectPageActivity.showCreatePledgeSuccess(checkoutDataAndProjectData: Pair<CheckoutData, PledgeData>) =
    dialogsAndFeedback.showCreatePledgeSuccess(checkoutDataAndProjectData.first, checkoutDataAndProjectData.second)

internal fun ProjectPageActivity.showPledgeNotCancelableDialog() = dialogsAndFeedback.showPledgeNotCancelableDialog()

internal fun ProjectPageActivity.showPledgeFragment(pledgeDataAndPledgeReason: Pair<PledgeData, PledgeReason>) =
    pledgeSheetController.showPledgeFragment(
        pledgeDataAndPledgeReason,
        selectPledgeFragment(pledgeDataAndPledgeReason.first, pledgeDataAndPledgeReason.second)
    )

internal fun ProjectPageActivity.showStarToast() = dialogsAndFeedback.showStarToast()

internal fun ProjectPageActivity.startShareIntent(projectNameAndShareUrl: Pair<String, String>) =
    navigationDelegate.startShareIntent(projectNameAndShareUrl)

internal fun ProjectPageActivity.startLoginToutActivity() = navigationDelegate.startLoginToutActivity()

internal fun ProjectPageActivity.startMessagesActivity(project: Project) = navigationDelegate.startMessagesActivity(project)

internal fun ProjectPageActivity.onPaymentOption(paymentOption: PaymentOption?) =
    stripePaymentHandler.onPaymentOption(paymentOption)

internal fun ProjectPageActivity.onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) =
    stripePaymentHandler.onPaymentSheetResult(paymentSheetResult)

internal fun ProjectPageActivity.stripeNextAction(it: String) =
    stripePaymentHandler.stripeNextAction(it)

internal fun ProjectPageActivity.flowControllerPresentPaymentOption(
    clientSecret: String,
    userEmail: String,
    googlePayCountryCode: String? = null,
    googlePayCurrencyCode: String? = null
) = stripePaymentHandler.flowControllerPresentPaymentOption(
    clientSecret,
    userEmail,
    googlePayCountryCode,
    googlePayCurrencyCode
)

internal fun ProjectPageActivity.showToastError(message: String? = null) =
    dialogsAndFeedback.showToastError(message)
