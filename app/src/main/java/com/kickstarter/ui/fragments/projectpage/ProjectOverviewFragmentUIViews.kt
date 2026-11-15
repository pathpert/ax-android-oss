package com.kickstarter.ui.fragments.projectpage

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectPageActivity
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.extensions.setClickableHtml
import com.kickstarter.ui.fragments.projectpage.ui.SimilarProjectsComponent
import com.kickstarter.ui.views.KSBottomSheetDialogFragment
import com.kickstarter.ui.views.compose.KSColorAccentedBanner

internal fun ProjectOverviewFragment.initComposeViews() {
    binding.composeViewBanner.setContent {
        KSTheme {
            val projectNotice by viewModel.outputs.projectProjectNotice()
                .subscribeAsState(initial = null)

            if (projectNotice.isNotNull()) {
                val modalBottomSheet = KSBottomSheetDialogFragment(
                    titleText = stringResource(R.string.project_project_notices_header),
                    bodyText = projectNotice ?: "",
                    linkText = stringResource(R.string.project_project_notices_notice_sheet_cta),
                    onCtaClicked = {
                        (activity as? ProjectPageActivity)?.showAccountabilityPage()
                    }
                )
                val openBottomSheet = {
                    modalBottomSheet.show(parentFragmentManager, KSBottomSheetDialogFragment.TAG)
                }
                Column {
                    KSColorAccentedBanner(
                        imageResToDisplay = R.drawable.ic_alert_diamond,
                        titleResToDisplay = R.string.project_project_notices_header,
                        textResToDisplay = R.string.project_project_notices_notice_intro,
                        buttonTextResToDisplay = R.string.project_project_notices_notice_cta,
                        textColor = colors.textPrimary,
                        backgroundColor = colors.backgroundDangerSubtle,
                        iconColor = colors.iconDanger,
                        accentColor = colors.backgroundDangerBoldPressed,
                        onClickAction = openBottomSheet
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                }
            }
        }
    }

    binding.composeViewSpc.apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            KSTheme {
                val context = LocalContext.current
                val uiState = similarProjectsViewModel.similarProjectsUiState.collectAsState()
                SimilarProjectsComponent(
                    uiState = uiState,
                    onTouchChange = { touching ->
                        similarProjectsViewModel.setParentScrollable(!touching)
                    },
                    onClick = { project ->
                        val intent = Intent().getProjectIntent(context).apply {
                            putExtra(IntentKey.PROJECT_PARAM, project.slug())
                            putExtra(IntentKey.REF_TAG, RefTag.similarProjects())
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

internal fun ProjectOverviewFragment.initClickListeners() {
    binding.creatorInfo.setOnClickListener {
        this.viewModel.inputs.creatorInfoButtonClicked()
    }

    binding.blurbView.setOnClickListener {
        this.viewModel.inputs.campaignButtonClicked()
    }

    binding.projectCreatorInfoLayout.comments.setOnClickListener {
        this.viewModel.inputs.commentsButtonClicked()
    }

    binding.projectCreatorInfoLayout.updates.setOnClickListener {
        this.viewModel.inputs.updatesButtonClicked()
    }

    binding.projectCreatorInfoLayout.reportProject.setOnClickListener {
        this.viewModel.inputs.reportProjectButtonClicked()
    }

    binding.projectCreatorInfoLayout.textReported.setClickableHtml { url ->
        this.viewModel.inputs.linkClicked(url)
    }
}
