package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.databinding.FragmentBackingBinding
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.adapters.RewardAndAddOnsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingFragmentViewModel
import io.reactivex.disposables.CompositeDisposable

@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.backing")
class BackingFragment : Fragment() {

    internal var rewardsAndAddOnsAdapter = RewardAndAddOnsAdapter()

    internal var binding: FragmentBackingBinding? = null

    private lateinit var viewModelFactory: BackingFragmentViewModel.Factory
    internal val viewModel: BackingFragmentViewModel.BackingFragmentViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    interface BackingDelegate {
        fun refreshProject()
        fun showFixPaymentMethod()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBackingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        val env = this.context?.getEnvironment()?.let { env ->
            viewModelFactory = BackingFragmentViewModel.Factory(env)
            env
        }

        binding?.let { b ->
            initViewModelSubscriptions(env, b, disposables)
            initViews(env, b)
        }
    }

    override fun onStart() {
        super.onStart()
        this.setState(activity is BackingActivity)
    }

    fun isRefreshing(isRefreshing: Boolean) {
        binding?.backingSwipeRefreshLayout?.isRefreshing = isRefreshing
    }

    fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
    }

    fun pledgeSuccessfullyUpdated() {
        this.viewModel.inputs.pledgeSuccessfullyUpdated()
    }

    fun setState(state: Boolean?) {
        state?.let {
            viewModel.isExpanded(state)
        }
    }

    private fun setupRecyclerView() {
        binding?.rewardAddOnRecycler?.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.rewardAddOnRecycler?.adapter = rewardsAndAddOnsAdapter
    }

    override fun onDetach() {
        disposables.clear()
        super.onDetach()
        binding?.rewardAddOnRecycler?.adapter = null
    }

    override fun onDestroyView() {
        binding = null
        disposables.clear()
        super.onDestroyView()
    }
}
