package com.kickstarter.viewmodels

import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.models.User

/**
 * ViewModel managing state transitions and user intents for DiscoveryViewModelHelper.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
internal object DiscoveryViewModelHelper {

    fun currentDrawerMenuIcon(user: User?, isDarkTheme: Boolean): Int {
        if (user.isNull()) {
            return if (isDarkTheme) R.drawable.ic_menu_dark else R.drawable.ic_menu
        }
        val erroredBackingsCount = user?.erroredBackingsCount().intValueOrZero()
        val unreadMessagesCount = user?.unreadMessagesCount().intValueOrZero()
        val unseenActivityCount = user?.unseenActivityCount().intValueOrZero()

        val ppoHasActions = when (user?.ppoHasAction()) {
            true -> 1
            false, null -> 0
        }

        return when {
            (erroredBackingsCount.isNonZero() || ppoHasActions.isNonZero()) -> {
                if (isDarkTheme) R.drawable.ic_menu_error_indicator_dark else R.drawable.ic_menu_error_indicator
            }

            (unreadMessagesCount + unseenActivityCount + erroredBackingsCount + ppoHasActions).isNonZero() -> {
                if (isDarkTheme) R.drawable.ic_menu_indicator_dark else R.drawable.ic_menu_indicator
            }

            else -> if (isDarkTheme) R.drawable.ic_menu_dark else R.drawable.ic_menu
        }
    }
}
