package com.kickstarter.ui.fragments.projectpage

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.kickstarter.databinding.FragmentProjectOverviewBinding
import com.kickstarter.libs.Configure
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.ProjectOverviewViewModel.ProjectOverviewViewModel
import com.kickstarter.viewmodels.projectpage.SimilarProjectsViewModel
import io.reactivex.disposables.CompositeDisposable

@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.projectpage")
class ProjectOverviewFragment : Fragment(), Configure {

    internal lateinit var binding: FragmentProjectOverviewBinding
    internal lateinit var ksString: KSString

    private lateinit var viewModelFactory: ProjectOverviewViewModel.Factory
    internal val viewModel: ProjectOverviewViewModel by viewModels { viewModelFactory }
    private lateinit var similarProjectsViewModelFactory: SimilarProjectsViewModel.Factory
    internal val similarProjectsViewModel: SimilarProjectsViewModel by activityViewModels { similarProjectsViewModelFactory }

    internal var disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringExtra(IntentKey.FLAGGINGKIND)
            data?.let {
                if (it.isNotEmpty()) {
                    this.viewModel.refreshFlaggedState(it)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ProjectOverviewViewModel.Factory(env)
            similarProjectsViewModelFactory = SimilarProjectsViewModel.Factory(env)
            env
        }

        ksString = viewModel.ksString

        initViewModelSubscriptions()
        initComposeViews()
        initClickListeners()
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
        this.similarProjectsViewModel.provideProject(projectData.project())
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ProjectOverviewFragment {
            val fragment = ProjectOverviewFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
