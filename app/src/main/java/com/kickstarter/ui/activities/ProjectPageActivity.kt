package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.databinding.ActivityProjectPageBinding
import com.kickstarter.features.projectpage.ui.components.LatePledgesComposeHost
import com.kickstarter.features.projectpage.ui.components.ProjectDialogsAndFeedback
import com.kickstarter.features.projectpage.ui.components.ProjectNavigationDelegate
import com.kickstarter.features.projectpage.ui.components.ProjectPagerController
import com.kickstarter.features.projectpage.ui.components.ProjectPledgeSheetController
import com.kickstarter.features.projectpage.ui.components.ProjectStripePaymentHandler
import com.kickstarter.features.socialshare.ui.SocialShareComposeHost
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.showLatePledgeFlow
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ProjectPagerAdapter
import com.kickstarter.ui.data.ActivityResult.Companion.create
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.ManagePledgeMenuOptions
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.selectPledgeFragment
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.startBackingDetailsWebViewActivity
import com.kickstarter.ui.extensions.startPledgeRedemption
import com.kickstarter.ui.extensions.startRootCommentsActivity
import com.kickstarter.ui.extensions.startUpdatesActivity
import com.kickstarter.ui.extensions.startVideoActivity
import com.kickstarter.ui.extensions.startWebViewActivity
import com.kickstarter.ui.fragments.BackingFragment
import com.kickstarter.ui.fragments.CancelPledgeFragment
import com.kickstarter.ui.fragments.RewardsFragment
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.projectpage.AddOnsViewModel
import com.kickstarter.viewmodels.projectpage.CheckoutFlowViewModel
import com.kickstarter.viewmodels.projectpage.ExperimentsViewModel
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import com.kickstarter.viewmodels.projectpage.PagerTabConfig
import com.kickstarter.viewmodels.projectpage.ProjectPageViewModel
import com.kickstarter.viewmodels.projectpage.RewardsSelectionViewModel
import com.kickstarter.viewmodels.projectpage.SimilarProjectsViewModel
import com.stripe.android.Stripe
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.model.PaymentOption
import com.stripe.android.view.CardInputWidget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

const val REFRESH = "refresh"

interface PledgeDelegate {
    fun pledgePaymentSuccessfullyUpdated()
    fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>)
    fun pledgeSuccessfullyUpdated()
}

class ProjectPageActivity :
    AppCompatActivity(),
    CancelPledgeFragment.CancelPledgeDelegate,
    PledgeDelegate,
    BackingFragment.BackingDelegate {
    internal lateinit var ksString: KSString

    internal lateinit var viewModelFactory: ProjectPageViewModel.Factory
    internal val viewModel: ProjectPageViewModel.ProjectPageViewModel by viewModels { viewModelFactory }

    internal lateinit var checkoutViewModelFactory: CheckoutFlowViewModel.Factory
    internal val checkoutFlowViewModel: CheckoutFlowViewModel by viewModels { checkoutViewModelFactory }

    internal lateinit var rewardsSelectionViewModelFactory: RewardsSelectionViewModel.Factory
    internal val rewardsSelectionViewModel: RewardsSelectionViewModel by viewModels { rewardsSelectionViewModelFactory }

    internal lateinit var latePledgeCheckoutViewModelFactory: LatePledgeCheckoutViewModel.Factory
    internal val latePledgeCheckoutViewModel: LatePledgeCheckoutViewModel by viewModels { latePledgeCheckoutViewModelFactory }

    internal lateinit var addOnsViewModelFactory: AddOnsViewModel.Factory
    internal val addOnsViewModel: AddOnsViewModel by viewModels { addOnsViewModelFactory }

    internal lateinit var similarProjectsViewModelFactory: SimilarProjectsViewModel.Factory
    internal val similarProjectsViewModel: SimilarProjectsViewModel by viewModels { similarProjectsViewModelFactory }

    internal lateinit var experimentsViewModelFactory: ExperimentsViewModel.Factory
    internal val experimentsViewModel: ExperimentsViewModel by viewModels { experimentsViewModelFactory }

    internal lateinit var stripe: Stripe
    internal lateinit var flowController: PaymentSheet.FlowController

    internal lateinit var socialShareComposeHost: SocialShareComposeHost
    internal lateinit var latePledgesComposeHost: LatePledgesComposeHost
    internal lateinit var pledgeSheetController: ProjectPledgeSheetController
    internal lateinit var navigationDelegate: ProjectNavigationDelegate
    internal lateinit var dialogsAndFeedback: ProjectDialogsAndFeedback
    internal lateinit var pagerController: ProjectPagerController
    internal lateinit var stripePaymentHandler: ProjectStripePaymentHandler

    internal val projectShareLabelString = R.string.project_accessibility_button_share_label
    internal val projectShareCopyString = R.string.project_share_twitter_message
    internal val projectStarConfirmationString = R.string.project_star_confirmation

    internal val animDuration = 200L
    internal lateinit var binding: ActivityProjectPageBinding

    internal lateinit var environment: Environment
    internal lateinit var ffClient: FeatureFlagClientType

    internal var disposables = CompositeDisposable()

    // Bridges the RxJava social-share output into the Compose host below. Non-null → the sheet is
    // shown for that project; null → hidden.
    internal val socialShareData = mutableStateOf<SocialShareData?>(null)

    internal val pagerAdapterList = mutableListOf(
        ProjectPagerTabs.OVERVIEW,
        ProjectPagerTabs.CAMPAIGN,
        ProjectPagerTabs.FAQS,
        ProjectPagerTabs.RISKS,
    )

    var startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data = result.data?.getLongExtra(IntentKey.VIDEO_SEEK_POSITION, 0)
                data?.let {
                    viewModel.inputs.closeFullScreenVideo(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectPageBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        setContentView(binding.root)
        setUpConnectivityStatusCheck(lifecycle)

        val toolbar = binding.pledgeContainerLayout.pledgeToolbar

        toolbar.inflateMenu(R.menu.manage_pledge)

        this.getEnvironment()?.let { env ->
            environment = env
            ffClient = requireNotNull(env.featureFlagClient())
            viewModelFactory = ProjectPageViewModel.Factory(env)
            checkoutViewModelFactory = CheckoutFlowViewModel.Factory(env)
            rewardsSelectionViewModelFactory = RewardsSelectionViewModel.Factory(env)
            addOnsViewModelFactory = AddOnsViewModel.Factory(env)
            latePledgeCheckoutViewModelFactory = LatePledgeCheckoutViewModel.Factory(env)
            similarProjectsViewModelFactory = SimilarProjectsViewModel.Factory(env)
            experimentsViewModelFactory = ExperimentsViewModel.Factory(env)
            stripe = requireNotNull(env.stripe())
        }

        ffClient.activate(this)

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

        viewModel.configureWith(intent)
        experimentsViewModel

        // Do not configure the pager at other lifecycle events apart from OnCreate
        if (savedInstanceState == null) {
            // - Configure pager on load, otherwise the first fragment on the pager gets no data
            configurePager(pagerAdapterList)
        }

        val viewTreeObserver = binding.pledgeContainerLayout.pledgeContainerRoot.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        this@ProjectPageActivity.viewModel.inputs.onGlobalLayout()
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
                    fragment.view?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                } else {
                    fragment.view?.importantForAccessibility =
                        View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                }
            }
        }

        bindPledgeAndUiSubscriptions()
        bindNavigationAndMediaSubscriptions()

        binding.backIcon.setOnClickListener {
            if (binding.pledgeContainerLayout.pledgeContainerRoot.isGone) {
                onBackPressedDispatcher.onBackPressed()
            } else {
                handleNativeCheckoutBackPress()
            }
        }

        setClickListeners()

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.projectAppBarLayout.setExpanded(false)
        }

        binding.projectAppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (verticalOffset != 0) {
                binding.mediaHeader.inputs.pausePlayer()
            }
        }

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    internal fun showSocialShareSheet(project: Project) =
        socialShareComposeHost.showSocialShareSheet(project)

    internal fun socialShareSetUp(composeView: ComposeView) =
        socialShareComposeHost.setup()

    internal fun latePledgesSetUp(composeView: ComposeView) =
        latePledgesComposeHost.setup()

    internal fun showDisclaimerScreen(disclaimerItem: DisclaimerItems, environment: Environment) =
        navigationDelegate.showDisclaimerScreen(disclaimerItem)

    fun showAccountabilityPage() =
        navigationDelegate.showAccountabilityPage()

    internal fun configureTabs(updateTabs: List<PagerTabConfig>) =
        pagerController.configureTabs(updateTabs)

    internal fun configurePager(pagerList: List<ProjectPagerTabs>) =
        pagerController.configurePager(pagerList)

    override fun onResume() {
        super.onResume()
        binding.mediaHeader.inputs.initializePlayer()
        this.viewModel.outputs.updateFragments()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateFragments(it) }
            .addToDisposable(disposables)
    }

    public override fun onPause() {
        super.onPause()
        binding.mediaHeader.inputs.releasePlayer()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText || view?.parent is CardInputWidget) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun pledgePaymentSuccessfullyUpdated() {
        this.viewModel.inputs.pledgePaymentSuccessfullyUpdated()
    }

    override fun pledgeSuccessfullyCancelled() {
        this.viewModel.inputs.pledgeSuccessfullyCancelled()
    }

    override fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>) {
        this.viewModel.inputs.pledgeSuccessfullyCreated(checkoutDataAndPledgeData)
    }

    override fun pledgeSuccessfullyUpdated() {
        this.viewModel.inputs.pledgeSuccessfullyUpdated()
    }

    override fun refreshProject() {
        this.viewModel.inputs.refreshProject()
    }

    override fun showFixPaymentMethod() {
        this.viewModel.inputs.fixPaymentMethodButtonClicked()
    }

    fun exitTransition(): Pair<Int, Int>? {
        return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

    private fun getTabTitle(position: Int, pagerList: List<ProjectPagerTabs>) =
        pagerController.getTabTitle(position, pagerList)

    internal fun animateScrimVisibility(show: Boolean) =
        pledgeSheetController.animateScrimVisibility(show)

    private fun backingFragment() = pledgeSheetController.backingFragment()

    private fun clearFragmentBackStack(): Boolean = pledgeSheetController.clearFragmentBackStack()

    internal fun expandPledgeSheet(expandAndAnimate: Pair<Boolean, Boolean>) =
        pledgeSheetController.expandPledgeSheet(expandAndAnimate)

    private fun setFragmentsState(expand: Boolean) =
        pledgeSheetController.setFragmentsState(expand)

    private fun handleNativeCheckoutBackPress() =
        pledgeSheetController.handleNativeCheckoutBackPress()

    internal fun openProjectAndFinish(url: String) =
        navigationDelegate.openProjectAndFinish(url)

    private fun renderProject(
        backingFragment: BackingFragment,
        rewardsFragment: RewardsFragment,
        projectData: ProjectData,
    ) = pledgeSheetController.renderProject(backingFragment, rewardsFragment, projectData)

    internal fun revealRewardsFragment() = pledgeSheetController.revealRewardsFragment()

    private fun rewardsFragment() = pledgeSheetController.rewardsFragment()

    private fun rewardsSheetGuideline(): Int = pledgeSheetController.rewardsSheetGuideline()

    internal fun setBackingDetailsSubtitle(stringResOrTitle: Either<String, Int>?) {
        stringResOrTitle?.let { either ->
            @StringRes val stringRes = either.right()
            val title = either.left()
            binding.pledgeContainerLayout.backingDetailsSubtitle.text =
                stringRes?.let { getString(it) } ?: title
        }
    }

    private fun setClickListeners() {
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

    internal fun setInitialRewardsContainerY() = pledgeSheetController.setInitialRewardsContainerY()

    internal fun showCancelPledgeFragment(project: Project) = pledgeSheetController.showCancelPledgeFragment(project)

    internal fun showCancelPledgeSuccess() = dialogsAndFeedback.showCancelPledgeSuccess()

    internal fun showCreatePledgeSuccess(checkoutDataAndProjectData: Pair<CheckoutData, PledgeData>) =
        dialogsAndFeedback.showCreatePledgeSuccess(checkoutDataAndProjectData.first, checkoutDataAndProjectData.second)

    internal fun showPledgeNotCancelableDialog() = dialogsAndFeedback.showPledgeNotCancelableDialog()

    internal fun showPledgeFragment(pledgeDataAndPledgeReason: Pair<PledgeData, PledgeReason>) =
        pledgeSheetController.showPledgeFragment(
            pledgeDataAndPledgeReason,
            selectPledgeFragment(pledgeDataAndPledgeReason.first, pledgeDataAndPledgeReason.second)
        )

    internal fun setPledgeActionButtonCTA(stringRes: Int) {
        binding.pledgeContainerLayout.pledgeActionButton.setText(stringRes)
        binding.pledgeContainerLayout.pledgeActionButton.contentDescription = when (stringRes) {
            R.string.Manage -> getString(R.string.Manage_your_pledge)
            else -> getString(stringRes)
        }
    }

    internal fun showStarToast() = dialogsAndFeedback.showStarToast()

    internal fun showUpdatePledgeSuccess() {
        clearFragmentBackStack()
        backingFragment()?.pledgeSuccessfullyUpdated()
        val intent = Intent()
            .putExtra(IntentKey.REFRESH_PPO_LIST, REFRESH)
        setResult(Activity.RESULT_OK, intent)
    }

    internal fun startShareIntent(projectNameAndShareUrl: Pair<String, String>) =
        navigationDelegate.startShareIntent(projectNameAndShareUrl)

    internal fun startLoginToutActivity() = navigationDelegate.startLoginToutActivity()

    internal fun startMessagesActivity(project: Project) = navigationDelegate.startMessagesActivity(project)

    internal fun styleProjectActionButton(detailsAreVisible: Boolean) {
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

    private fun updateFragments(projectData: ProjectData) {
        try {
            // - Every time the ProjectData gets updated
            // - the fragments on the viewPager are updated as well
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

    // Update the UI with the returned PaymentOption
    private fun onPaymentOption(paymentOption: PaymentOption?) =
        stripePaymentHandler.onPaymentOption(paymentOption)

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) =
        stripePaymentHandler.onPaymentSheetResult(paymentSheetResult)

    private fun stripeNextAction(it: String) =
        stripePaymentHandler.stripeNextAction(it)

    private fun flowControllerPresentPaymentOption(
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

    private fun showToastError(message: String? = null) =
        dialogsAndFeedback.showToastError(message)

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        viewModel.activityResult(create(requestCode, resultCode, intent))
        stripePaymentHandler.handleActivityResult(requestCode, resultCode, intent)
    }

    override fun onDestroy() {
        binding.projectPager.adapter = null
        binding.mediaHeader.inputs.releasePlayer()
        disposables.clear()
        super.onDestroy()
    }

    /**
     * Updates the visibility of the items in the Manage Pledge menu based on the provided options.
     *
     * @param options The options for managing the pledge.
     */
    internal fun updateManagePledgeMenuVisibility(options: ManagePledgeMenuOptions) {
        val menu = binding.pledgeContainerLayout.pledgeToolbar.menu
        menu.findItem(R.id.edit_pledge)?.isVisible = options.showEditPledge
        menu.findItem(R.id.choose_another_reward)?.isVisible = options.showChooseAnotherReward
        menu.findItem(R.id.rewards)?.isVisible = options.showSeeRewards
        menu.findItem(R.id.update_payment)?.isVisible = options.showUpdatePayment
        menu.findItem(R.id.cancel_pledge)?.isVisible = options.showCancelPledge
        menu.findItem(R.id.contact_creator)?.isVisible = options.showContactCreator
    }
}
