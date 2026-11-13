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
import com.kickstarter.features.projectpage.ui.components.SocialShareComposeHost
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
    private lateinit var ksString: KSString

    private lateinit var viewModelFactory: ProjectPageViewModel.Factory
    private val viewModel: ProjectPageViewModel.ProjectPageViewModel by viewModels { viewModelFactory }

    private lateinit var checkoutViewModelFactory: CheckoutFlowViewModel.Factory
    private val checkoutFlowViewModel: CheckoutFlowViewModel by viewModels { checkoutViewModelFactory }

    private lateinit var rewardsSelectionViewModelFactory: RewardsSelectionViewModel.Factory
    private val rewardsSelectionViewModel: RewardsSelectionViewModel by viewModels { rewardsSelectionViewModelFactory }

    private lateinit var latePledgeCheckoutViewModelFactory: LatePledgeCheckoutViewModel.Factory
    private val latePledgeCheckoutViewModel: LatePledgeCheckoutViewModel by viewModels { latePledgeCheckoutViewModelFactory }

    private lateinit var addOnsViewModelFactory: AddOnsViewModel.Factory
    private val addOnsViewModel: AddOnsViewModel by viewModels { addOnsViewModelFactory }

    private lateinit var similarProjectsViewModelFactory: SimilarProjectsViewModel.Factory
    private val similarProjectsViewModel: SimilarProjectsViewModel by viewModels { similarProjectsViewModelFactory }

    private lateinit var experimentsViewModelFactory: ExperimentsViewModel.Factory
    private val experimentsViewModel: ExperimentsViewModel by viewModels { experimentsViewModelFactory }

    private lateinit var stripe: Stripe
    private lateinit var flowController: PaymentSheet.FlowController

    private lateinit var socialShareComposeHost: SocialShareComposeHost
    private lateinit var latePledgesComposeHost: LatePledgesComposeHost
    private lateinit var pledgeSheetController: ProjectPledgeSheetController
    private lateinit var navigationDelegate: ProjectNavigationDelegate
    private lateinit var dialogsAndFeedback: ProjectDialogsAndFeedback
    private lateinit var pagerController: ProjectPagerController
    private lateinit var stripePaymentHandler: ProjectStripePaymentHandler

    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.project_star_confirmation

    private val animDuration = 200L
    private lateinit var binding: ActivityProjectPageBinding

    private lateinit var environment: Environment
    private lateinit var ffClient: FeatureFlagClientType

    private var disposables = CompositeDisposable()

    // Bridges the RxJava social-share output into the Compose host below. Non-null → the sheet is
    // shown for that project; null → hidden.
    private val socialShareData = mutableStateOf<SocialShareData?>(null)

    private val pagerAdapterList = mutableListOf(
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

    private fun showSocialShareSheet(project: Project) =
        socialShareComposeHost.showSocialShareSheet(project)

    private fun socialShareSetUp(composeView: ComposeView) =
        socialShareComposeHost.setup()

    private fun latePledgesSetUp(composeView: ComposeView) =
        latePledgesComposeHost.setup()

    private fun showDisclaimerScreen(disclaimerItem: DisclaimerItems, environment: Environment) =
        navigationDelegate.showDisclaimerScreen(disclaimerItem)

    fun showAccountabilityPage() =
        navigationDelegate.showAccountabilityPage()

    private fun configureTabs(updateTabs: List<PagerTabConfig>) =
        pagerController.configureTabs(updateTabs)

    private fun configurePager(pagerList: List<ProjectPagerTabs>) =
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

    private fun animateScrimVisibility(show: Boolean) =
        pledgeSheetController.animateScrimVisibility(show)

    private fun backingFragment() = pledgeSheetController.backingFragment()

    private fun clearFragmentBackStack(): Boolean = pledgeSheetController.clearFragmentBackStack()

    private fun expandPledgeSheet(expandAndAnimate: Pair<Boolean, Boolean>) =
        pledgeSheetController.expandPledgeSheet(expandAndAnimate)

    private fun setFragmentsState(expand: Boolean) =
        pledgeSheetController.setFragmentsState(expand)

    private fun handleNativeCheckoutBackPress() =
        pledgeSheetController.handleNativeCheckoutBackPress()

    private fun openProjectAndFinish(url: String) =
        navigationDelegate.openProjectAndFinish(url)

    private fun renderProject(
        backingFragment: BackingFragment,
        rewardsFragment: RewardsFragment,
        projectData: ProjectData,
    ) = pledgeSheetController.renderProject(backingFragment, rewardsFragment, projectData)

    private fun revealRewardsFragment() = pledgeSheetController.revealRewardsFragment()

    private fun rewardsFragment() = pledgeSheetController.rewardsFragment()

    private fun rewardsSheetGuideline(): Int = pledgeSheetController.rewardsSheetGuideline()

    private fun setBackingDetailsSubtitle(stringResOrTitle: Either<String, Int>?) {
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

    private fun setInitialRewardsContainerY() = pledgeSheetController.setInitialRewardsContainerY()

    private fun showCancelPledgeFragment(project: Project) = pledgeSheetController.showCancelPledgeFragment(project)

    private fun showCancelPledgeSuccess() = dialogsAndFeedback.showCancelPledgeSuccess()

    private fun showCreatePledgeSuccess(checkoutDataAndProjectData: Pair<CheckoutData, PledgeData>) =
        dialogsAndFeedback.showCreatePledgeSuccess(checkoutDataAndProjectData.first, checkoutDataAndProjectData.second)

    private fun showPledgeNotCancelableDialog() = dialogsAndFeedback.showPledgeNotCancelableDialog()

    private fun showPledgeFragment(pledgeDataAndPledgeReason: Pair<PledgeData, PledgeReason>) =
        pledgeSheetController.showPledgeFragment(
            pledgeDataAndPledgeReason,
            selectPledgeFragment(pledgeDataAndPledgeReason.first, pledgeDataAndPledgeReason.second)
        )

    private fun setPledgeActionButtonCTA(stringRes: Int) {
        binding.pledgeContainerLayout.pledgeActionButton.setText(stringRes)
        binding.pledgeContainerLayout.pledgeActionButton.contentDescription = when (stringRes) {
            R.string.Manage -> getString(R.string.Manage_your_pledge)
            else -> getString(stringRes)
        }
    }

    private fun showStarToast() = dialogsAndFeedback.showStarToast()

    private fun showUpdatePledgeSuccess() {
        clearFragmentBackStack()
        backingFragment()?.pledgeSuccessfullyUpdated()
        val intent = Intent()
            .putExtra(IntentKey.REFRESH_PPO_LIST, REFRESH)
        setResult(Activity.RESULT_OK, intent)
    }

    private fun startShareIntent(projectNameAndShareUrl: Pair<String, String>) =
        navigationDelegate.startShareIntent(projectNameAndShareUrl)

    private fun startLoginToutActivity() = navigationDelegate.startLoginToutActivity()

    private fun startMessagesActivity(project: Project) = navigationDelegate.startMessagesActivity(project)

    private fun styleProjectActionButton(detailsAreVisible: Boolean) {
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
    private fun updateManagePledgeMenuVisibility(options: ManagePledgeMenuOptions) {
        val menu = binding.pledgeContainerLayout.pledgeToolbar.menu
        menu.findItem(R.id.edit_pledge)?.isVisible = options.showEditPledge
        menu.findItem(R.id.choose_another_reward)?.isVisible = options.showChooseAnotherReward
        menu.findItem(R.id.rewards)?.isVisible = options.showSeeRewards
        menu.findItem(R.id.update_payment)?.isVisible = options.showUpdatePayment
        menu.findItem(R.id.cancel_pledge)?.isVisible = options.showCancelPledge
        menu.findItem(R.id.contact_creator)?.isVisible = options.showContactCreator
    }
}
