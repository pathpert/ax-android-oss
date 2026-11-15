package com.kickstarter.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentManager
import com.kickstarter.libs.utils.extensions.checkPermissions
import com.kickstarter.ui.fragments.ConsentManagementDialogFragment
import com.kickstarter.viewmodels.DiscoveryViewModel

/**
 * Dialogs and system permission prompt helper for [DiscoveryActivity].
 */
internal object DiscoveryActivityDialogsHelper {

    fun requestNotificationPermission(
        activity: DiscoveryActivity,
        requestPermissionLauncher: ActivityResultLauncher<String>,
        viewModel: DiscoveryViewModel.DiscoveryViewModel
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.inputs.hasSeenNotificationsPermission(true)
    }

    fun showConsentManagementDialog(fragmentManager: FragmentManager) {
        val dialog = ConsentManagementDialogFragment()
        dialog.isCancelable = false
        dialog.show(fragmentManager, "consentManagementDialogFragment")
    }

    fun showOnboarding(
        activity: DiscoveryActivity,
        onboardingResult: ActivityResultLauncher<Intent>,
        viewModel: DiscoveryViewModel.DiscoveryViewModel
    ) {
        val intent = Intent(activity, OnboardingFlowActivity::class.java)
        onboardingResult.launch(intent)
        viewModel.inputs.hasSeenOnboarding(true)
    }
}
