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
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityProjectPageBinding
import com.kickstarter.features.projectpage.ui.components.LatePledgesComposeHost
import com.kickstarter.features.projectpage.ui.components.ProjectDialogsAndFeedback
import com.kickstarter.features.projectpage.ui.components.ProjectNavigationDelegate
import com.kickstarter.features.projectpage.ui.components.ProjectPagerController
import com.kickstarter.features.projectpage.ui.components.ProjectPledgeSheetController
import com.kickstarter.features.projectpage.ui.components.ProjectStripePaymentHandler
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.ui.SocialShareComposeHost
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ActivityResult.Companion.create
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.fragments.BackingFragment
import com.kickstarter.ui.fragments.CancelPledgeFragment
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.projectpage.AddOnsViewModel
import com.kickstarter.viewmodels.projectpage.CheckoutFlowViewModel
import com.kickstarter.viewmodels.projectpage.ExperimentsViewModel
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import com.kickstarter.viewmodels.projectpage.ProjectPageViewModel
import com.kickstarter.viewmodels.projectpage.RewardsSelectionViewModel
import com.kickstarter.viewmodels.projectpage.SimilarProjectsViewModel
import com.stripe.android.Stripe
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.view.CardInputWidget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

const val REFRESH = "refresh"

/**
 * Activity displaying and coordinating user interactions for PledgeDelegate.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Deprecated("Legacy monolithic delegate replaced by com.kickstarter.features.projectpage")
interface PledgeDelegate {
    fun pledgePaymentSuccessfullyUpdated()
    fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>)
    fun pledgeSuccessfullyUpdated()
}

@Deprecated(
    message = "Legacy monolithic Activity being strangler-figged by com.kickstarter.features.projectpage",
    replaceWith = ReplaceWith(
        "ProjectPageActivity",
        "com.kickstarter.features.projectpage"
    )
)
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
        initControllers()

        viewModel.configureWith(intent)
        experimentsViewModel

        if (savedInstanceState == null) {
            configurePager(pagerAdapterList)
        }

        setupLayoutAndBackStackListeners()

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

    fun showAccountabilityPage() = navigationDelegate.showAccountabilityPage()

    fun exitTransition(): Pair<Int, Int>? {
        return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right)
    }

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
}
