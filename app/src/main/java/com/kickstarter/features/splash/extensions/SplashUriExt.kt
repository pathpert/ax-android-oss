package com.kickstarter.features.splash.extensions

import android.net.Uri
import androidx.core.net.toUri
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.UrlUtils.appendRefTag
import com.kickstarter.libs.utils.UrlUtils.refTag
import com.kickstarter.libs.utils.extensions.isCheckoutUri
import com.kickstarter.libs.utils.extensions.isDiscoverUri
import com.kickstarter.libs.utils.extensions.isEmailDomain
import com.kickstarter.libs.utils.extensions.isMainPage
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isPMOrderEditUri
import com.kickstarter.libs.utils.extensions.isPMUri
import com.kickstarter.libs.utils.extensions.isProjectCommentUri
import com.kickstarter.libs.utils.extensions.isProjectPreviewUri
import com.kickstarter.libs.utils.extensions.isProjectSaveUri
import com.kickstarter.libs.utils.extensions.isProjectSurveyUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateCommentsUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.libs.utils.extensions.isRewardFulfilledDl
import com.kickstarter.libs.utils.extensions.isSettingsUrl
import com.kickstarter.libs.utils.extensions.isVideoFeedUri

/**
 * Feature component for Uri in splash module.
 *
 * Negative constraints:
 * - Do not bypass feature-scoped boundary contracts or leak internal UI state.
 */
fun Uri.isEligibleForProjectPage(webEndpoint: String): Boolean =
    isNotNull() &&
        !isProjectSaveUri(webEndpoint) &&
        !isCheckoutUri(webEndpoint) &&
        !isProjectPreviewUri(webEndpoint) &&
        !isProjectCommentUri(webEndpoint) &&
        !isProjectUpdateUri(webEndpoint) &&
        !isProjectUpdateCommentsUri(webEndpoint) &&
        !isRewardFulfilledDl() &&
        isProjectUri(webEndpoint)

fun Uri.isUnsupportedDeepLink(webEndpoint: String, ffClient: FeatureFlagClientType): Boolean =
    isNotNull() &&
        !isMainPage() &&
        !isDiscoverUri(webEndpoint) &&
        lastPathSegment != "projects" &&
        !isSettingsUrl() &&
        !isProjectSaveUri(webEndpoint) &&
        !isCheckoutUri(webEndpoint) &&
        !isProjectCommentUri(webEndpoint) &&
        !isProjectUpdateUri(webEndpoint) &&
        !isProjectUpdateCommentsUri(webEndpoint) &&
        !isProjectUri(webEndpoint) &&
        !isRewardFulfilledDl() &&
        !isEmailDomain() &&
        !isProjectSurveyUri(webEndpoint) &&
        !isPMUri(webEndpoint) &&
        !isPMOrderEditUri(webEndpoint, ffClient.getBoolean(FlagKey.ANDROID_EDIT_ORDER)) &&
        !isVideoFeedUri(webEndpoint)

fun Uri.appendRefTagIfNone(): Uri {
    val url = toString()
    val ref = refTag(url)
    return if (ref.isNull()) {
        appendRefTag(url, RefTag.deepLink().tag()).toUri()
    } else this
}
