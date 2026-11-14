package com.kickstarter.ui.viewholders

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.kickstarter.R
import com.kickstarter.databinding.ProjectCardViewBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.libs.utils.extensions.isProjectNamePunctuated
import com.kickstarter.libs.utils.extensions.photoHeightFromWidthRatio
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.loadImageWithResize
import com.kickstarter.viewmodels.ProjectCardHolderViewModel
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime

class ProjectCardViewHolder(
    private val binding: ProjectCardViewBinding,
    delegate: Delegate
) : KSViewHolder(binding.root) {
    private val viewModel = ProjectCardHolderViewModel.ViewModel()
    internal val ksString: KSString = requireNotNull(environment().ksString())
    private val build = requireNotNull(environment().build())
    private val disposables = CompositeDisposable()

    interface Delegate {
        fun projectCardViewHolderClicked(project: Project)
        fun onHeartButtonClicked(project: Project)
    }

    init {
        bindOutputs(binding, viewModel, delegate, disposables)

        binding.heartButton?.setOnClickListener {
            this.viewModel.inputs.heartButtonClicked()
        }
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndParams = requireNotNull(data as? Pair<Project, DiscoveryParams>)
        viewModel.inputs.configureWith(projectAndParams)
    }

    internal fun setSubcategoryTextView(subcategory: String) {
        binding.projectCardTags.subcategoryTextView.text = subcategory
    }

    internal fun setStyledNameAndBlurb(nameAndBlurb: Pair<String, String>) {
        val nameString =
            if (isProjectNamePunctuated(nameAndBlurb.first)) nameAndBlurb.first.toString() + " " else nameAndBlurb.first.toString() + ": "
        val blurbString = nameAndBlurb.second
        val styledString = SpannableString(nameString + blurbString)
        styledString.setSpan(
            ForegroundColorSpan(context().getColor(R.color.kds_support_700)),
            0,
            nameString.length,
            0
        )
        styledString.setSpan(
            ForegroundColorSpan(context().getColor(R.color.kds_support_400)),
            nameString.length,
            nameString.length + blurbString.length,
            0
        )
        binding.nameAndBlurbTextView.text = styledString
    }

    internal fun resizeProjectImage(avatarUrl: String?) {
        val targetImageWidth = getProjectImageWidth()
        val targetImageHeight = photoHeightFromWidthRatio(targetImageWidth)

        binding.projectCardPhoto.photo.maxHeight = targetImageHeight
        avatarUrl?.let {
            ResourcesCompat.getDrawable(context().resources, R.drawable.gray_gradient, null)
                ?.let { placeholder ->
                    binding.projectCardPhoto.photo.loadImageWithResize(it, targetImageWidth, targetImageHeight, placeholder)
                }
        }
    }

    private fun getProjectImageWidth(): Int {
        val screenWidthDp = ViewUtils.getScreenWidthDp(context())
        val screenDensityDp = ViewUtils.getScreenDensity(context())
        val reducedSizeDp = context().resources.getDimension(R.dimen.grid_4)

        return (screenWidthDp * screenDensityDp - reducedSizeDp).toInt()
    }

    internal fun setDeadlineCountdownText(project: Project) {
        binding.projectCardStats.deadlineCountdownUnit.text =
            project.deadlineCountdownDetail(context(), ksString)
    }

    internal fun setFriendAvatarUrl(avatarUrl: String, imageView: ImageView) {
        imageView.loadCircleImage(avatarUrl)
    }

    internal fun setDefaultTopPadding(setDefaultPadding: Boolean) {
        binding.landCardViewGroup?.let {
            if (setDefaultPadding) {
                adjustLandscapeTopPadding(
                    it,
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt()
                )
                adjustViewGroupTopMargin(binding.projectCardViewGroup, 0)
            } else {
                adjustLandscapeTopPadding(
                    it,
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_3).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt(),
                    context().resources.getDimension(R.dimen.grid_2).toInt()
                )
                adjustViewGroupTopMargin(
                    binding.projectCardViewGroup,
                    this.context().resources.getDimension(R.dimen.grid_1).toInt()
                )
            }
        }
    }

    override fun onClick(view: View) {
        viewModel.inputs.projectCardClicked()
    }

    /**
     * Adjust spacing between cards when metadata label is present.
     */
    private fun adjustViewGroupTopMargin(viewGroup: ViewGroup, topMargin: Int) {
        val marginParams = MarginLayoutParams(
            viewGroup.layoutParams
        )
        marginParams.setMargins(0, topMargin, 0, 0)
        viewGroup.layoutParams = marginParams
    }

    /**
     * Adjust card content spacing when metadata label is present.
     */
    private fun adjustLandscapeTopPadding(
        landscapeViewGroup: ViewGroup?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        landscapeViewGroup?.setPadding(left, top, right, bottom)
    }

    internal fun setCanceledTextView(projectCanceledAt: DateTime) {
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text =
            DateTimeUtils.relative(context(), ksString, projectCanceledAt)
        binding.projectStateViewGroup.fundingUnsuccessfulTextView.setText(R.string.discovery_baseball_card_status_banner_canceled)
    }

    internal fun setSuccessfullyFundedDateTextView(projectSuccessfulAt: DateTime) {
        binding.projectStateViewGroup.fundingSuccessfulDateTextView.text =
            DateTimeUtils.relative(context(), ksString, projectSuccessfulAt)
    }

    internal fun setFailedAtTextView(projectFailedAt: DateTime) {
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text =
            DateTimeUtils.relative(context(), ksString, projectFailedAt)
        binding.projectStateViewGroup.fundingUnsuccessfulTextView.setText(R.string.dashboard_creator_project_funding_unsuccessful)
    }

    internal fun setSuspendedAtTextView(projectSuspendedAt: DateTime) {
        binding.projectStateViewGroup.fundingUnsuccessfulDateTextView.text =
            DateTimeUtils.relative(context(), ksString, projectSuspendedAt)
    }

    override fun destroy() {
        disposables.clear()
        viewModel.onCleared()
        super.destroy()
    }
}
