/**
 * Activity displaying and coordinating user interactions for OnboardingPage.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
enum class OnboardingPage(val analyticsSectionName: String) {
    WELCOME("welcome"),
    SAVE_PROJECTS("save_projects"),
    ACTIVITY_TRACKING("activity_tracking"),
    ENABLE_NOTIFICATIONS("enable_notifications"),
    SIGNUP_LOGIN("signup_login"),
}

data class OnboardingPageData(
    val page: OnboardingPage,
    val title: String,
    val description: String,
    val animationRes: Int,
    val buttonText: String,
    val secondaryButtonText: String? = null
)

object OnboardingScreenTestTags {
    const val CLOSE_BUTTON = "onboarding_close_button"
    const val PAGE_TITLE = "onboarding_page_title"
    const val PAGE_DESCRIPTION = "onboarding_page_description"
    const val PAGE_ANIMATION = "onboarding_page_animation"
    const val PRIMARY_BUTTON = "onboarding_primary_button"
    const val SECONDARY_BUTTON = "onboarding_secondary_button"
    const val PROGRESS_BAR = "onboarding_progress_bar"
}
