package com.kickstarter.ui.fragments

import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kickstarter.R
import com.kickstarter.databinding.FragmentCancelPledgeBinding
import com.kickstarter.features.cancelpledge.data.CancelPledgeEvent
import com.kickstarter.features.cancelpledge.viewmodel.CancelPledgeFeatureViewModel
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.extensions.snackbar
import com.kickstarter.ui.extensions.text
import kotlinx.coroutines.launch

class CancelPledgeFragment : Fragment() {

    interface CancelPledgeDelegate {
        fun pledgeSuccessfullyCancelled()
    }

    private var binding: FragmentCancelPledgeBinding? = null

    private lateinit var viewModelFactory: CancelPledgeFeatureViewModel.Factory
    private val viewModel: CancelPledgeFeatureViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentCancelPledgeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val env = this.context?.getEnvironment()
        viewModelFactory = CancelPledgeFeatureViewModel.Factory(env, bundle = arguments)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        if (state.isReady) {
                            setPromptText(state.formattedPledgeAmount, state.projectName)
                        }
                        binding?.progressBar?.let { progressView ->
                            ViewUtils.setGone(progressView, !state.isSubmitting)
                        }
                        binding?.yesCancelPledgeButton?.let { button ->
                            ViewUtils.setGone(button, !state.isCancelButtonVisible)
                            button.isEnabled = !state.isSubmitting
                        }
                        state.problem?.let { problem ->
                            binding?.cancelPledgeRoot?.let { root ->
                                snackbar(root, "${problem.detail} ${problem.suggestedAction}").show()
                            }
                            viewModel.clearProblem()
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            CancelPledgeEvent.Dismiss -> dismiss()
                            CancelPledgeEvent.Succeeded -> {
                                (context as? CancelPledgeDelegate)?.pledgeSuccessfullyCancelled()
                            }
                        }
                    }
                }
            }
        }

        binding?.yesCancelPledgeButton?.setOnClickListener {
            val note = binding?.cancellationNote?.text().orEmpty()
            viewModel.confirmCancellationClicked(note)
        }

        binding?.noCancelPledgeButton?.setOnClickListener {
            viewModel.goBackButtonClicked()
        }
    }

    private fun dismiss() {
        parentFragmentManager.popBackStack(
            CancelPledgeFragment::class.java.simpleName,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun setPromptText(amount: String, projectName: String) {
        val ksString = context?.getEnvironment()?.ksString() ?: return
        val formattedString = ksString.format(
            getString(R.string.Are_you_sure_you_wish_to_cancel_your_amount_pledge_to_project_name),
            "amount",
            amount,
            "project_name",
            projectName
        )

        val spannableString = SpannableString(formattedString)
        ViewUtils.addBoldSpan(spannableString, amount)
        ViewUtils.addBoldSpan(spannableString, projectName)

        binding?.cancelPrompt?.text = spannableString
    }

    companion object {

        fun newInstance(project: Project): CancelPledgeFragment {
            val fragment = CancelPledgeFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT, project)
            fragment.arguments = argument
            return fragment
        }
    }
}
