package com.kickstarter.features.projectpage.ui.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.util.Pair
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivityProjectPageBinding
import com.kickstarter.models.Project
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.BackingFragment
import com.kickstarter.ui.fragments.CancelPledgeFragment
import com.kickstarter.ui.fragments.RewardsFragment

/**
 * Manages pledge sheet expand/collapse animations, scrim transitions, and fragment stack navigation.
 *
 * Negative constraints:
 * - Do not trigger animations while fragment transaction is currently committing.
 */
class ProjectPledgeSheetController(
    private val activity: Activity,
    private val binding: ActivityProjectPageBinding,
    private val fragmentManager: FragmentManager,
    private val onPledgeToolbarNavigationClicked: () -> Unit,
    private val onBackPressed: () -> Unit
) {
    private val animDuration = 200L

    fun backingFragment(): BackingFragment? =
        fragmentManager.findFragmentById(R.id.fragment_backing) as? BackingFragment

    fun rewardsFragment(): RewardsFragment? =
        fragmentManager.findFragmentById(R.id.fragment_rewards) as? RewardsFragment

    fun clearFragmentBackStack(): Boolean {
        return fragmentManager.popBackStackImmediate(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    fun animateScrimVisibility(show: Boolean) {
        val shouldAnimateIn = show && binding.pledgeContainerLayout.scrim.alpha <= 1f
        val shouldAnimateOut = !show && binding.pledgeContainerLayout.scrim.alpha >= 0f
        if (shouldAnimateIn || shouldAnimateOut) {
            val finalAlpha = if (show) 1f else 0f
            binding.pledgeContainerLayout.scrim.animate()
                .alpha(finalAlpha)
                .setDuration(200L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (!show) {
                            binding.pledgeContainerLayout.scrim.visibility = View.GONE
                        }
                    }

                    override fun onAnimationStart(animation: Animator) {
                        if (show) {
                            binding.pledgeContainerLayout.scrim.visibility = View.VISIBLE
                        }
                    }
                })
        }
    }

    fun rewardsSheetGuideline(): Int =
        activity.resources.getDimensionPixelSize(R.dimen.reward_fragment_guideline_constraint_end)

    fun setInitialRewardsContainerY() {
        val guideline = rewardsSheetGuideline()
        binding.pledgeContainerLayout.pledgeContainerRoot.y =
            (binding.root.height - guideline).toFloat()
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun expandPledgeSheet(expandAndAnimate: Pair<Boolean, Boolean>) {
        var statusBarHeight = 0

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            insets
        }

        val expand = expandAndAnimate.first
        val animate = expandAndAnimate.second
        val targetToShow =
            if (!expand) binding.pledgeContainerLayout.pledgeActionButtonsLayout else binding.pledgeContainerLayout.pledgeContainer
        val showRewardsFragmentAnimator = ObjectAnimator.ofFloat(targetToShow, View.ALPHA, 0f, 1f)

        val targetToHide =
            if (!expand) binding.pledgeContainerLayout.pledgeContainer else binding.pledgeContainerLayout.pledgeActionButtonsLayout
        val hideRewardsFragmentAnimator = ObjectAnimator.ofFloat(targetToHide, View.ALPHA, 1f, 0f)

        val guideline = rewardsSheetGuideline()
        val initialValue =
            (if (expand) binding.pledgeContainerLayout.pledgeContainerRoot.height - guideline else 0).toFloat()
        val finalValue =
            ((if (expand) 0 else binding.pledgeContainerLayout.pledgeContainerRoot.height - guideline) + statusBarHeight).toFloat()
        val initialRadius = activity.resources.getDimensionPixelSize(R.dimen.fab_radius).toFloat()

        val pledgeContainerYAnimator = ObjectAnimator.ofFloat(
            binding.pledgeContainerLayout.pledgeContainerRoot,
            View.Y,
            initialValue,
            finalValue
        ).apply {
            addUpdateListener { valueAnim ->
                val radius =
                    initialRadius * if (expand) 1 - valueAnim.animatedFraction else valueAnim.animatedFraction
                binding.pledgeContainerLayout.pledgeContainerRoot.radius = radius
            }
        }

        AnimatorSet().apply {
            playTogether(
                showRewardsFragmentAnimator,
                hideRewardsFragmentAnimator,
                pledgeContainerYAnimator
            )
            duration = animDuration

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    setFragmentsState(expand)
                    if (expand) {
                        binding.pledgeContainerLayout.pledgeActionButtonsLayout.visibility = View.GONE
                        binding.projectActivityToolbar.importantForAccessibility =
                            View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        binding.pledgeContainerLayout.pledgeToolbar.requestFocus()
                    } else {
                        binding.pledgeContainerLayout.pledgeContainer.visibility = View.GONE
                        binding.projectActivityToolbar.importantForAccessibility =
                            View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        if (animate) {
                            binding.projectActivityToolbar.requestFocus()
                        }
                    }
                }

                override fun onAnimationStart(animation: Animator) {
                    if (expand) {
                        binding.pledgeContainerLayout.pledgeContainer.visibility = View.VISIBLE
                    } else if (animate) {
                        binding.pledgeContainerLayout.pledgeActionButtonsLayout.visibility = View.VISIBLE
                    }
                }
            })

            start()
        }
    }

    fun setFragmentsState(expand: Boolean) {
        fragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is RewardsFragment -> fragment.setState(expand && fragment.isVisible)
                is BackingFragment -> fragment.setState(expand && fragment.isVisible)
            }
        }
    }

    fun handleNativeCheckoutBackPress() {
        val retryPadding = activity.resources.getDimensionPixelSize(R.dimen.grid_4)
        val pledgeSheetIsExpanded = binding.pledgeContainerLayout.pledgeContainerRoot.y <= retryPadding

        when {
            fragmentManager.backStackEntryCount > 0 -> fragmentManager.popBackStack()
            pledgeSheetIsExpanded -> onPledgeToolbarNavigationClicked()
            else -> onBackPressed()
        }
    }

    fun revealRewardsFragment() {
        rewardsFragment()?.let {
            fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .show(it)
                .addToBackStack(RewardsFragment::class.java.simpleName)
                .commit()
        }
    }

    fun showCancelPledgeFragment(project: Project) {
        val cancelPledgeFragment = CancelPledgeFragment.newInstance(project)
        val tag = CancelPledgeFragment::class.java.simpleName
        fragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
            .add(R.id.fragment_container, cancelPledgeFragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    fun showPledgeFragment(
        pledgeDataAndPledgeReason: Pair<PledgeData, PledgeReason>,
        pledgeFragment: Fragment
    ) {
        val tag = pledgeFragment::class.java.simpleName
        fragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
            .add(R.id.fragment_container, pledgeFragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    fun renderProject(
        backingFragment: BackingFragment,
        rewardsFragment: RewardsFragment,
        projectData: ProjectData
    ) {
        rewardsFragment.configureWith(projectData)
        backingFragment.configureWith(projectData)
    }
}
