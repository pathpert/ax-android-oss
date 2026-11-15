package com.kickstarter.libs

/**
 * Library utility component for ActivityRequestCodes.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
object ActivityRequestCodes {
    const val LOGIN_FLOW = 5921
    const val RESET_FLOW = 5922
    const val CHECKOUT_ACTIVITY_WALLET_REQUEST = 59210
    const val CHECKOUT_ACTIVITY_WALLET_CHANGE_REQUEST = 59211
    const val CHECKOUT_ACTIVITY_WALLET_OBTAINED_FULL = 59212
    const val SAVE_NEW_PAYMENT_METHOD = 59213
    const val SHOW_REWARDS = 59214
}
