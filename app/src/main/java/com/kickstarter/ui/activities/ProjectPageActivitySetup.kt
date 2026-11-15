package com.kickstarter.ui.activities

import com.kickstarter.features.projectpage.ui.components.LatePledgesComposeHost
import com.kickstarter.features.projectpage.ui.components.ProjectDialogsAndFeedback
import com.kickstarter.features.projectpage.ui.components.ProjectNavigationDelegate
import com.kickstarter.features.projectpage.ui.components.ProjectPagerController
import com.kickstarter.features.projectpage.ui.components.ProjectPledgeSheetController
import com.kickstarter.features.projectpage.ui.components.ProjectStripePaymentHandler
import com.kickstarter.features.socialshare.ui.SocialShareComposeHost
import com.kickstarter.ui.extensions.finishWithAnimation
import com.stripe.android.paymentsheet.PaymentSheet

internal fun ProjectPageActivity.initControllers() {
    flowController = PaymentSheet.FlowController.create(
        activity = this,
        paymentOptionCallback = ::onPaymentOption,
        paymentResultCallback = ::onPaymentSheetResult
    )
    this.ksString = requireNotNull(environment.ksString())

    dialogsAndFeedback = ProjectDialogsAndFeedback(
        activity = this,
        snackbarAnchor = binding.snackbarAnchor,
        errorAnchor = binding.pledgeContainerCompose,
        clearFragmentBackStack = { clearFragmentBackStack() }
    )

    navigationDelegate = ProjectNavigationDelegate(
        activity = this,
        environment = environment,
        ksString = ksString
    )

    socialShareComposeHost = SocialShareComposeHost(
        composeView = binding.socialShareComposeView,
        socialShareData = socialShareData,
        environment = environment,
        context = applicationContext,
        onStartActivity = { startActivity(it) }
    )
    socialShareComposeHost.setup()

    stripePaymentHandler = ProjectStripePaymentHandler(
        activity = this,
        stripe = stripe,
        flowController = flowController,
        latePledgeCheckoutViewModel = latePledgeCheckoutViewModel,
        showToastError = { showToastError(it) }
    )

    pledgeSheetController = ProjectPledgeSheetController(
        activity = this,
        binding = binding,
        fragmentManager = supportFragmentManager,
        onPledgeToolbarNavigationClicked = { viewModel.inputs.pledgeToolbarNavigationClicked() },
        onBackPressed = { onBackPressedDispatcher.onBackPressed() }
    )

    pagerController = ProjectPagerController(
        context = this,
        fragmentManager = supportFragmentManager,
        lifecycle = lifecycle,
        viewPager = binding.projectPager,
        tabLayout = binding.projectDetailTabs,
        onTabSelected = { viewModel.inputs.tabSelected(it) }
    )

    latePledgesComposeHost = LatePledgesComposeHost(
        composeView = binding.pledgeContainerCompose,
        binding = binding,
        environment = environment,
        checkoutFlowViewModel = checkoutFlowViewModel,
        rewardsSelectionViewModel = rewardsSelectionViewModel,
        addOnsViewModel = addOnsViewModel,
        latePledgeCheckoutViewModel = latePledgeCheckoutViewModel,
        onBackPressedDispatcher = onBackPressedDispatcher,
        startLoginToutActivity = { startLoginToutActivity() },
        showDisclaimerScreen = { showDisclaimerScreen(it, environment) },
        showAccountabilityPage = { showAccountabilityPage() },
        showCreatePledgeSuccess = { showCreatePledgeSuccess(it) },
        showToastError = { showToastError(it) },
        flowControllerPresentPaymentOption = { secret, email, country, currency ->
            flowControllerPresentPaymentOption(secret, email, country, currency)
        },
        stripeNextAction = { stripeNextAction(it) },
        refreshProject = { refreshProject() },
        finishWithAnimation = { finishWithAnimation() }
    )
}

internal fun ProjectPageActivity.setupLayoutAndBackStackListeners() {
    val viewTreeObserver = binding.pledgeContainerLayout.pledgeContainerRoot.viewTreeObserver
    if (viewTreeObserver.isAlive) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
                android.view.ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    this@setupLayoutAndBackStackListeners.viewModel.inputs.onGlobalLayout()
                    binding.pledgeContainerLayout.pledgeContainerRoot.viewTreeObserver.removeOnGlobalLayoutListener(
                        this
                    )
                }
            })
    }

    this.supportFragmentManager.addOnBackStackChangedListener {
        this.viewModel.inputs.fragmentStackCount(this.supportFragmentManager.backStackEntryCount)
        val fragments = this.supportFragmentManager.fragments
        val lastFragmentWithView = fragments.last { it.view != null }
        for (fragment in fragments) {
            if (fragment == lastFragmentWithView) {
                fragment.view?.importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
            } else {
                fragment.view?.importantForAccessibility =
                    android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            }
        }
    }
}
