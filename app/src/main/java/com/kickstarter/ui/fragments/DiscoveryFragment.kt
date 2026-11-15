package com.kickstarter.ui.fragments

import android.animation.AnimatorSet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.FragmentDiscoveryBinding
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Category
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.DiscoveryActivitySampleAdapter
import com.kickstarter.ui.adapters.DiscoveryEditorialAdapter
import com.kickstarter.ui.adapters.DiscoveryOnboardingAdapter
import com.kickstarter.ui.adapters.DiscoveryProjectCardAdapter
import com.kickstarter.ui.adapters.DiscoveryVideoFeedBannerAdapter
import com.kickstarter.viewmodels.DiscoveryFragmentViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

/**
 * Fragment presenting UI and collecting view states for DiscoveryFragment.
 *
 * Negative constraints:
 * - Do not execute network calls directly; collect state flows from ViewModel.
 */
@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.discovery")
class DiscoveryFragment : Fragment() {
    private var heartsAnimation: AnimatorSet? = null
    private var recyclerViewPaginator: RecyclerViewPaginatorV2? = null

    private var binding: FragmentDiscoveryBinding? = null
    internal var discoveryEditorialAdapter: DiscoveryEditorialAdapter? = null
    private val projectStarConfirmationString = R.string.project_star_confirmation
    private val disposables = CompositeDisposable()

    private lateinit var viewModelFactory: DiscoveryFragmentViewModel.Factory
    private val viewModel: DiscoveryFragmentViewModel.DiscoveryFragmentViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = DiscoveryFragmentViewModel.Factory(env)
        }
        binding = FragmentDiscoveryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                activity?.runOnUiThread { viewModel.inputs.fragmentLifeCycle(Lifecycle.State.RESUMED) }
            }
        }

        val activitySampleAdapter = DiscoveryActivitySampleAdapter(this.viewModel.inputs)
        val editorialAdapter = DiscoveryEditorialAdapter(this.viewModel.inputs)
        val onboardingAdapter = DiscoveryOnboardingAdapter(this.viewModel.inputs)
        val videoFeedBannerAdapter = DiscoveryVideoFeedBannerAdapter(this.viewModel.inputs)
        val projectCardAdapter = DiscoveryProjectCardAdapter(this.viewModel.inputs)

        val discoveryAdapter = ConcatAdapter(
            videoFeedBannerAdapter,
            onboardingAdapter,
            editorialAdapter,
            activitySampleAdapter,
            projectCardAdapter
        )
        this.discoveryEditorialAdapter = editorialAdapter

        binding?.discoveryRecyclerView?.apply {
            adapter = discoveryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerViewPaginator = RecyclerViewPaginatorV2(
                this,
                { this@DiscoveryFragment.viewModel.inputs.nextPage() },
                this@DiscoveryFragment.viewModel.outputs.isFetchingProjects()
            )
        }

        setupSwipeRefresh(binding, viewModel, disposables)

        bindAdaptersAndAnimations(
            binding = binding,
            viewModel = viewModel,
            disposables = disposables,
            activitySampleAdapter = activitySampleAdapter,
            editorialAdapter = editorialAdapter,
            onboardingAdapter = onboardingAdapter,
            videoBannerAdapter = videoFeedBannerAdapter,
            projectCardAdapter = projectCardAdapter,
            onHeartAnimationRequest = {
                val anim = lazyHeartCrossFadeAnimation()
                if (anim?.isRunning != true) {
                    anim?.start()
                }
            }
        )

        binding?.discoveryHeartsContainer?.setOnClickListener {
            this.viewModel.inputs.heartContainerClicked()
        }

        bindNavigationOutputs(binding, viewModel, disposables, projectStarConfirmationString)
    }

    private fun lazyHeartCrossFadeAnimation(): AnimatorSet? {
        if (heartsAnimation == null) {
            heartsAnimation = createHeartCrossFadeAnimation(binding)
        }
        return heartsAnimation
    }

    override fun onPause() {
        super.onPause()
        heartsAnimation = null
    }

    override fun onDetach() {
        super.onDetach()
        binding?.discoveryRecyclerView?.adapter = null
        recyclerViewPaginator?.stop()
    }

    val isAttached: Boolean
        get() = viewModel != null

    val isInstantiated: Boolean
        get() = binding?.discoveryRecyclerView != null

    fun refresh() {
        this.viewModel.inputs.refresh()
    }

    fun takeCategories(categories: List<Category>) {
        this.viewModel.inputs.rootCategories(categories)
    }

    fun updateParams(params: DiscoveryParams) {
        this.viewModel.inputs.paramsFromActivity(params)
    }

    fun clearPage() {
        this.viewModel.inputs.clearPage()
    }

    fun scrollToTop() {
        binding?.discoveryRecyclerView?.smoothScrollToPosition(0)
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): DiscoveryFragment {
            val fragment = DiscoveryFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.DISCOVERY_SORT_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
