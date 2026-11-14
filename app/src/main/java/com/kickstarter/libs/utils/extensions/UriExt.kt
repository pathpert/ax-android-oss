@file:JvmName("UriExt")
package com.kickstarter.libs.utils.extensions

import android.net.Uri
import com.kickstarter.libs.utils.Secrets
import java.util.regex.Pattern

fun Uri.host(): String {
    return this.host ?: ""
}

fun Uri.lastPathSegment(): String {
    return this.lastPathSegment ?: ""
}

fun Uri.path(): String {
    return this.path ?: ""
}

fun Uri.query(): String {
    return this.query ?: ""
}

const val SCHEME_KSR = "ksr"
const val SCHEME_HTTPS = "https"

/**
 * Get token from Uri query params
 * From "at={TOKEN}&ref=ksr_email_user_email_verification" to "{TOKEN}"
 */
fun Uri.getTokenFromQueryParams(): String {
    return this.getQueryParameter("at") ?: ""
}

fun Uri.isSettingsUrl(): Boolean {
    return this.toString().contains("/settings/notify_mobile_of_marketing_update/true")
}

/**
 * Identify valid SCHEMAS "https:" or "ksr:"
 */
fun Uri.isKSScheme(): Boolean {
    return (scheme == SCHEME_KSR || scheme == SCHEME_HTTPS)
}

/**
 * Identify the Reward Fulfilled Deeplink
 */
fun Uri.isRewardFulfilledDl(): Boolean {
    return isKSScheme() && isKickstarterUri(this.toString()) && PROJECT_REWARD_FULFILLMENT.matcher(path()).matches()
}

fun Uri.isVerificationEmailUrl(): Boolean {
    return this.toString().contains(VERIFICATION)
}

fun Uri.isApiUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && Secrets.RegExpPattern.API.matcher(host())
        .matches()
}

fun Uri.isCheckoutUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && NATIVE_CHECKOUT_PATTERN.matcher(path())
        .matches()
}

fun Uri.isDiscoverCategoriesPath(): Boolean {
    return DISCOVER_CATEGORIES_PATTERN.matcher(path()).matches()
}

fun Uri.isDiscoverScopePath(scope: String): Boolean {
    val matcher = DISCOVER_SCOPE_PATTERN.matcher(path())
    return matcher.matches() && scope == matcher.group(1)
}

fun Uri.isDiscoverPlacesPath(): Boolean {
    return DISCOVER_PLACES_PATTERN.matcher(path()).matches()
}

fun Uri.isDiscoverUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && DISCOVER_PATTERN.matcher(path()).matches()
}

fun Uri.isHivequeenUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && Secrets.RegExpPattern.HIVEQUEEN.matcher(host()).matches()
}

fun Uri.isKickstarterUri(webEndpoint: String): Boolean {
    return host() == Uri.parse(webEndpoint).host()
}

fun Uri.isKSFavIcon(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && lastPathSegment() == "favicon.ico"
}

fun Uri.isWebViewUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && !isKSFavIcon(webEndpoint)
}

fun Uri.isNewGuestCheckoutUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && NEW_GUEST_CHECKOUT_PATTERN.matcher(path())
        .matches()
}

/**
 * For URI on Main Page Open button ksr://www.kickstarter.com/?app_banner=1&ref=nav
 *  matches domain, and query params
 */
fun Uri.isMainPage(): Boolean {
    return host().contains(KSDOMAIN) && MAIN_PAGE_OPEN_BUTTON_QUERYPARAMS.matcher(query()).matches()
}

/**
 * Given URI, host must contain `kickstarter.com` as domain
 *
 * Ignores the current API endpoint used on the app.
 */
fun Uri.isKSDomain(): Boolean {
    return host().contains(KSDOMAIN)
}

/**
 * Given URI, host must contain `kickstarter.com` as domain, and math the
 * Project URL regex.
 *
 * Ignores the current API endpoint used on the app.
 */
fun Uri.isProjectUri(): Boolean {
    return host().contains(KSDOMAIN) && PROJECT_PATTERN.matcher(path()).matches()
}
fun Uri.isProjectUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && PROJECT_PATTERN.matcher(path()).matches()
}

fun Uri.isProjectPreviewUri(webEndpoint: String): Boolean {
    return isProjectUri(
        webEndpoint
    ) && getQueryParameter("token").isNotNull()
}

fun Uri.isSignupUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && path() == "/signup"
}

fun Uri.isStagingUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && Secrets.RegExpPattern.STAGING.matcher(host()).matches()
}

fun Uri.isCheckoutThanksUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && CHECKOUT_THANKS_PATTERN.matcher(path())
        .matches()
}

/**
 * Takes an URI and matches over the list of Domains provided by marketing
 */
fun Uri.isEmailDomain(): Boolean {
    return isKSScheme() && EMAIL_DOMAINS.matcher(this.host)
        .matches()
}

fun Uri.isModalUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && getQueryParameter("modal") != null && getQueryParameter("modal") == "true"
}

fun Uri.isProjectSurveyUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && (
        PROJECT_SURVEY.matcher(path()).matches() || PROJECT_SURVEY_EDIT.matcher(path())
            .matches() || PROJECT_SURVEY_EDIT_ADDRESS.matcher(path())
            .matches() || PROJECT_SURVEY_RESPONSE.matcher(path())
            .matches() || PROJECT_SURVEY_BACKING_PLEDGE_REDEMPTION.matcher(path()).matches()
        )
}

fun Uri.isPMUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && PROJECT_BACKING_REDEEM.matcher(path()).matches()
}

fun Uri.isProjectCommentUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && PROJECT_COMMENTS_PATTERN.matcher(path())
        .matches()
}

fun Uri.isProjectSaveUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) &&
        PROJECT_PATTERN.matcher(path()).matches() &&
        PROJECT_SAVE_QUERY_PATTERN.matcher(query()).matches()
}

fun Uri.isProjectUpdateCommentsUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && PROJECT_UPDATE_COMMENTS_PATTERN.matcher(path()).matches()
}

fun Uri.isProjectUpdateUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && PROJECT_UPDATE_PATTERN.matcher(path())
        .matches()
}

fun Uri.isProjectUpdatesUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && PROJECT_UPDATES_PATTERN.matcher(path())
        .matches()
}

fun Uri.isUserSurveyUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && USER_SURVEY.matcher(path()).matches()
}

fun Uri.isVideoFeedUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && VIDEO_FEED_PATTERN.matcher(path()).matches()
}

fun Uri.isWebUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && !isApiUri(webEndpoint)
}

fun Uri.isDiscoverSortParam(): Boolean {
    return DISCOVER_SORT_PATTERN.matcher(path()).matches() &&
        getQueryParameter("sort").isNotNull()
}

fun Uri.isBackingDetailsUri(webEndpoint: String): Boolean {
    return isKickstarterUri(webEndpoint) && PROJECT_BACKING_DETAILS_URL.matcher(path()).matches()
}

/**
 * Returns `true` if the URI is a post-campaign checkout URL for editing an order and the corresponding feature flag is enabled,
 * `false` otherwise.
 *
 * This will match URIs with the format: `/projects/creator/project/order_edits/order_edit_id/checkout`
 */
fun Uri.isPMOrderEditUri(webEndpoint: String, isFFEnabled: Boolean = false): Boolean {
    return isKickstarterUri(webEndpoint) &&
        PM_ORDER_EDIT_CHECKOUT_PATTERN.matcher(path()).matches() &&
        isFFEnabled
}

fun Uri.hasSecretRewardToken(): Boolean {
    return this.getQueryParameter("secret_reward_token")?.isNotEmpty() == true
}

fun Uri.secretRewardToken(): String {
    return this.getQueryParameter("secret_reward_token") ?: ""
}

