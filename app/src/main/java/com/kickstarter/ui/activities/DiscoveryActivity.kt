package com.kickstarter.ui.activities

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryLayoutBinding
import com.kickstarter.features.pledgedprojectsoverview.ui.PledgedProjectsOverviewActivity
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.featureflag.StatsigClientType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.checkPermissions
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.showErrorSnackBar
import com.kickstarter.ui.extensions.showSuccessSnackBar
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.ui.fragments.ConsentManagementDialogFragment
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.ui.fragments.DiscoveryFragment.Companion.newInstance
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.DiscoveryViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.discovery")
class DiscoveryActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var drawerAdapter: DiscoveryDrawerAdapter
    private lateinit var drawerLayoutManager: LinearLayoutManager
    private lateinit var pagerAdapter: DiscoveryPagerAdapter
    private lateinit var consentManagementDialogFragment: ConsentManagementDialogFragment
    private lateinit var statsigClient: StatsigClientType
    private var internalTools: InternalToolsType? = null
    private lateinit var binding: DiscoveryLayoutBinding
    private lateinit var viewModelFactory: DiscoveryViewModel.Factory
    private val viewModel: DiscoveryViewModel.DiscoveryViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setUpConnectivityStatusCheck(lifecycle)

        binding = DiscoveryLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        getEnvironment()?.let { env ->
            viewModelFactory = DiscoveryViewModel.Factory(env)

            viewModel.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

            internalTools = env.internalTools()
            statsigClient = requireNotNull(env.statsigClient())
        }

//        val gateValueExtended = statsigClient.getFeatureGate(StatsigGateKey.ANDROID_VIDEO_FEED.key)
//        val gateValue = statsigClient.checkGate(StatsigGateKey.ANDROID_VIDEO_FEED.key)
//
//        if (gateValue) Timber.d("*** statsig gate open!")
//        else Timber.d("*** statsig gate closed!")
//
//        if (gateValueExtended.getValue()) Timber.d("*** statsig gate open with group=${gateValueExtended.getName()} and rule${gateValueExtended.getRuleID()}")
//        else Timber.d("*** statsig gate closed with group=${gateValueExtended.getName()} and rule${gateValueExtended.getRuleID()}")

        /*
         * Provide the Intent _after_ the view tree (most importantly the ViewPager)
         * has completed a first-pass render, ensuring that the Intent workflows begin
         * after those associated with the `pagerSetPrimaryPage` Subject et al. in the VM.
         */
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                viewModel.provideIntent(intent)
            }
        })

        // TODO: Replace with compose implementation
        val nightModeFlags =
            this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        viewModel.setDarkTheme(
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    true
                }

                else -> false
            }
        )
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

        val onboardingResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                viewModel.inputs.hasExitedOnboarding()
            }
        }

        drawerLayoutManager = LinearLayoutManager(this)

        binding.discoveryDrawerRecyclerView.layoutManager = drawerLayoutManager
        drawerAdapter = DiscoveryDrawerAdapter(
            viewModel.inputs,
        )
        binding.discoveryDrawerRecyclerView.adapter = drawerAdapter

        val viewPagerTitles = listOf(
            getString(R.string.discovery_sort_types_magic),
            getString(R.string.Popular),
            getString(R.string.discovery_sort_types_newest),
            getString(R.string.Ending_soon)
        )

        pagerAdapter = DiscoveryPagerAdapter(
            supportFragmentManager,
            DiscoveryActivityUIHelper.createFragments(viewPagerTitles.size).toMutableList(),
            viewPagerTitles,
            viewModel.inputs
        )

        binding.discoveryViewPager.adapter = pagerAdapter
        binding.discoveryTabLayout.setupWithViewPager(binding.discoveryViewPager)

        DiscoveryActivityUIHelper.setupTabSelectedListener(binding, pagerAdapter, viewModel)

        viewModel.outputs.expandSortTabLayout()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.discoverySortAppBarLayout.setExpanded(it) }
            .addToDisposable(disposables)

        viewModel.outputs.updateToolbarWithParams()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.discoveryToolbar.discoveryToolbar.loadParams(it) }
            .addToDisposable(disposables)

        viewModel.outputs.updateParamsForPage()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.discoveryViewPager.currentItem = it.sort().positionFromSort()
                pagerAdapter.takeParams(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.showNotifPermissionsRequest()
            .distinctUntilChanged()
            .filter {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    this.checkPermissions(Manifest.permission.POST_NOTIFICATIONS)
            }
            .delay(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                DiscoveryActivityDialogsHelper.requestNotificationPermission(
                    this,
                    requestPermissionLauncher,
                    viewModel
                )
            }
            .addToDisposable(disposables)

        viewModel.outputs.showConsentManagementDialog()
            .distinctUntilChanged()
            .subscribe {
                DiscoveryActivityDialogsHelper.showConsentManagementDialog(supportFragmentManager)
            }
            .addToDisposable(disposables)

        viewModel.outputs.showOnboardingFlow()
            .distinctUntilChanged()
            .subscribe {
                DiscoveryActivityDialogsHelper.showOnboarding(this, onboardingResult, viewModel)
            }
            .addToDisposable(disposables)

        viewModel.outputs.clearPages()
            .compose<List<Int?>>(Transformers.observeForUIV2())
            .subscribe { pagerAdapter.clearPages(it) }
            .addToDisposable(disposables)

        viewModel.outputs.rootCategoriesAndPosition()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pagerAdapter.takeCategoriesForPosition(it.first, it.second) }
            .addToDisposable(disposables)

        DiscoveryActivityNavigation.bindNavigationOutputs(
            this,
            viewModel,
            internalTools,
            disposables
        )

        viewModel.outputs.navigationDrawerData()
            .compose(Transformers.observeForUIV2())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { drawerAdapter.takeData(it) }
            .addToDisposable(disposables)

        viewModel.closeDrawer()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.discoveryDrawerLayout.closeDrawer(GravityCompat.START) }
            .addToDisposable(disposables)

        viewModel.outputs.drawerMenuIcon()
            .compose(Transformers.observeForUIV2())
            .subscribe { drawerMenuIcon: Int -> DiscoveryActivityUIHelper.updateDrawerMenuIcon(binding, drawerMenuIcon) }
            .addToDisposable(disposables)

        viewModel.outputs.showSuccessMessage()
            .compose(Transformers.observeForUIV2())
            .subscribe { this@DiscoveryActivity.showSuccessSnackBar(binding.discoveryAnchorView, it) }
            .addToDisposable(disposables)

        viewModel.outputs.showErrorMessage()
            .compose(Transformers.observeForUIV2())
            .subscribe { this@DiscoveryActivity.showErrorSnackBar(binding.discoveryAnchorView, it ?: "") }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    fun discoveryLayout(): DrawerLayout {
        return binding.discoveryDrawerLayout
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        if (key.equals(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE)) {
            viewModel.inputs.hasSeenConsentManagement(true)
        }
    }
}
