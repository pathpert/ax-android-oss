import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.kickstarter.R
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.utils.Clock
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ACTIVITY_TRACKING_PROMPT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.ALLOW
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DENY
import com.kickstarter.libs.utils.SystemClock
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSButtonType
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.fragments.ConsentManagementDialogFragment

/**
 * Activity displaying and coordinating user interactions for OnboardingScreenPreview.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
@Preview
fun OnboardingScreenPreview() {
    OnboardingScreen()
}

@Composable
fun OnboardingScreen(
    clock: Clock = SystemClock,
    isUserLoggedIn: Boolean = false,
    deviceNeedsNotificationPermissions: Boolean = false,
    onboardingCompleted: () -> Unit = {},
    onboardingCancelled: (onboardingPage: OnboardingPage) -> Unit = {},
    turnOnNotifications: (permissionLauncher: ActivityResultLauncher<String>) -> Unit = {},
    allowTracking: (fragmentManager: FragmentManager?) -> Unit = {},
    signupOrLogin: () -> Unit = {},
    analyticEvents: AnalyticEvents? = null,
) {
    val initialPages: List<OnboardingPageData> = listOf(
        OnboardingPageData(
            page = OnboardingPage.WELCOME,
            title = stringResource(R.string.onboarding_welcome_to_kickstarter_title),
            description = stringResource(R.string.onboarding_welcome_to_kickstarter_subtitle),
            animationRes = R.raw.android_onboarding_flow_welcome,
            buttonText = stringResource(R.string.project_checkout_navigation_next)
        ),
        OnboardingPageData(
            page = OnboardingPage.SAVE_PROJECTS,
            title = stringResource(R.string.onboarding_save_projects_for_later_title),
            description = stringResource(R.string.onboarding_save_projects_for_later_subtitle),
            animationRes = R.raw.android_onboarding_flow_save_projects,
            buttonText = stringResource(R.string.project_checkout_navigation_next)
        ),
        OnboardingPageData(
            page = OnboardingPage.ACTIVITY_TRACKING,
            title = stringResource(R.string.onboarding_personalize_your_experience_title),
            description = stringResource(R.string.onboarding_personalize_your_experience_subtitle),
            animationRes = R.raw.android_onboarding_flow_activity_tracking,
            buttonText = stringResource(R.string.Use_personalization),
            secondaryButtonText = stringResource(R.string.Not_right_now),
        ),
        OnboardingPageData(
            page = OnboardingPage.ENABLE_NOTIFICATIONS,
            title = stringResource(R.string.onboarding_stay_in_the_know_title),
            description = stringResource(R.string.onboarding_stay_in_the_know_subtitle),
            animationRes = R.raw.android_onboarding_flow_enable_notifications,
            buttonText = stringResource(R.string.Get_notified),
            secondaryButtonText = stringResource(R.string.Not_right_now),
        ),
        OnboardingPageData(
            page = OnboardingPage.SIGNUP_LOGIN,
            title = stringResource(R.string.onboarding_join_the_community_title),
            description = stringResource(R.string.onboarding_join_the_community_subtitle),
            animationRes = R.raw.android_onboarding_flow_login_signup,
            buttonText = stringResource(R.string.Sign_up_or_log_in),
            secondaryButtonText = stringResource(R.string.Explore_the_app),
        )
    )

    val filteredPages: List<OnboardingPageData> = initialPages.filterNot { pageData ->
        (isUserLoggedIn && pageData.page == OnboardingPage.SIGNUP_LOGIN) ||
            (!deviceNeedsNotificationPermissions && pageData.page == OnboardingPage.ENABLE_NOTIFICATIONS)
    }

    var currentPage by remember { mutableStateOf(0) }
    LaunchedEffect(currentPage) {
        analyticEvents?.trackOnboardingPageViewed(filteredPages[currentPage].page.analyticsSectionName)
    }

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            analyticEvents?.trackOnboardingAllowDenyPromptCTAClicked(ACTIVITY_TRACKING_PROMPT.contextName, ALLOW.contextName)
        } else {
            analyticEvents?.trackOnboardingAllowDenyPromptCTAClicked(ACTIVITY_TRACKING_PROMPT.contextName, DENY.contextName)
        }
        if (currentPage < filteredPages.lastIndex) currentPage++
    }

    val fragmentManager = remember(context) {
        (context as? FragmentActivity)?.supportFragmentManager
    }
    fragmentManager?.setFragmentResultListener(ConsentManagementDialogFragment.TAG, (context as FragmentActivity)) {
            _, result ->
        val value = result.getString("result") ?: ""
        analyticEvents?.trackOnboardingAllowDenyPromptCTAClicked(ACTIVITY_TRACKING_PROMPT.contextName, value)

        if (isUserLoggedIn) {
            onboardingCompleted()
        } else {
            if (currentPage < filteredPages.lastIndex) currentPage++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.borderAccentGreenSubtle)
            .navigationBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_squiggle),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            OnboardingProgressBar(currentPage, filteredPages.size.toFloat(), filteredPages, onboardingCancelled)

            OnboardingPageAnimation(modifier = Modifier.weight(1.0f), pageData = filteredPages[currentPage])

            Column(
                modifier = Modifier
                    .height(dimensions.footerHeight)
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensions.paddingLarge,
                        vertical = dimensions.paddingSmall
                    )
            ) {
                var lastClickTime by remember { mutableStateOf(0L) }
                val debounceInterval = 500L

                KSPrimaryBlackButton(
                    text = filteredPages[currentPage].buttonText,
                    onClickAction = {
                        val currentTime = clock.now()
                        if (currentTime - lastClickTime > debounceInterval) {
                            lastClickTime = currentTime

                            when (filteredPages[currentPage].page) {
                                OnboardingPage.WELCOME -> {
                                    analyticEvents?.trackOnboardingNextCTAClicked(filteredPages[currentPage].page.analyticsSectionName)
                                    if (currentPage < filteredPages.lastIndex) currentPage++
                                }
                                OnboardingPage.SAVE_PROJECTS -> {
                                    analyticEvents?.trackOnboardingNextCTAClicked(filteredPages[currentPage].page.analyticsSectionName)
                                    if (currentPage < filteredPages.lastIndex) currentPage++
                                }
                                OnboardingPage.ACTIVITY_TRACKING -> {
                                    activity?.let {
                                        allowTracking(fragmentManager)
                                    }
                                }
                                OnboardingPage.ENABLE_NOTIFICATIONS -> {
                                    activity?.let {
                                        turnOnNotifications(permissionLauncher)
                                    }
                                }
                                OnboardingPage.SIGNUP_LOGIN -> {
                                    signupOrLogin()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(OnboardingScreenTestTags.PRIMARY_BUTTON),
                    isEnabled = true
                )

                filteredPages[currentPage].secondaryButtonText?.let { secondaryText ->
                    KSButton(
                        text = secondaryText,
                        onClickAction = {
                            analyticEvents?.trackOnboardingNextCTAClicked(filteredPages[currentPage].page.analyticsSectionName)
                            if (currentPage < filteredPages.lastIndex) {
                                currentPage++
                            } else {
                                onboardingCompleted()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(OnboardingScreenTestTags.SECONDARY_BUTTON),
                        type = KSButtonType.BORDERLESS
                    )
                }
            }
        }
    }
}
