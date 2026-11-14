package com.kickstarter.features.socialshare.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isGone
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kickstarter.features.socialshare.AndroidSocialShareService
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.viewmodel.SocialShareViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KickstarterApp

/**
 * Encapsulates the Compose host view and ViewModel lifecycle for in-app social sharing bottom sheets.
 */
class SocialShareComposeHost(
    private val composeView: ComposeView,
    private val socialShareData: MutableState<SocialShareData?>,
    private val environment: Environment,
    private val context: Context,
    private val onStartActivity: (Intent) -> Unit
) {
    fun setup() {
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KickstarterApp {
                    val data = socialShareData.value
                    if (data != null) {
                        val shareViewModel: SocialShareViewModel = viewModel(
                            key = data.projectUrl,
                            factory = SocialShareViewModel.Factory(
                                environment = environment,
                                service = AndroidSocialShareService(context),
                                shareData = data,
                                contextPage = ContextPageName.PROJECT
                            )
                        )
                        CompositionLocalProvider(LocalSocialShareViewModel provides shareViewModel) {
                            SocialShareSheet(
                                shareData = data,
                                isVisible = true,
                                onDismiss = {
                                    socialShareData.value = null
                                    composeView.isGone = true
                                },
                                onIntentReady = onStartActivity
                            )
                        }
                    }
                }
            }
        }
    }

    fun showSocialShareSheet(project: Project) {
        socialShareData.value = SocialShareData(
            projectName = project.name(),
            projectUrl = project.webProjectUrl(),
            imageUrl = project.photo()?.full() ?: "",
            creatorName = project.creator().name()
        )
        composeView.isGone = false
    }
}

