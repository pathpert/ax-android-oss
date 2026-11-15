package com.kickstarter.ui.activities

import androidx.annotation.StringRes
import com.kickstarter.R

/**
 * Items selectable from the login/signup tout disclaimer footer.
 */
enum class DisclaimerItems(@StringRes val itemName: Int) {
    TERMS(R.string.login_tout_help_sheet_terms),
    COOKIES(R.string.login_tout_help_sheet_cookie),
    PRIVACY(R.string.login_tout_help_sheet_privacy),
    HELP(R.string.general_navigation_buttons_help)
}
