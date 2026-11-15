package com.kickstarter.libs.utils

/**
 * Utility and helper extensions for ThirdPartyEventValues.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
class ThirdPartyEventValues {
    enum class EventName(val value: String) {
        ADD_PAYMENT_INFO("add_payment_info"),
        PURCHASE("purchase"),
        SCREEN_VIEW("screen_view"),
        VIEW_ITEM("view_item"),
    }

    enum class ScreenName(val value: String) {
        PROJECT("project"),
        SEARCH("search"),
        PRELAUNCH("prelaunch"),
        DEEPLINK("deeplink"),
        DISCOVERY("discovery"),
        REWARDS("rewards")
    }
}
