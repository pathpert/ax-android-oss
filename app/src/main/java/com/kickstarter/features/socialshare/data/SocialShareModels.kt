package com.kickstarter.features.socialshare.data

import android.graphics.Bitmap
import android.net.Uri
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.EventContextValues.SharePlatformContextType

/**
 * Snapshot of the project information used across every social sharing action.
 *
 * Negative constraints:
 * - Do not pass blank [projectName] or [projectUrl].
 *
 * @property projectName Non-blank name of the shared project.
 * @property projectUrl Canonical URL to the project campaign page.
 * @property imageUrl URL to the primary project header photo.
 * @property creatorName Display name of the project creator.
 * @property projectId Optional non-blank project ID.
 */
data class SocialShareData(
    val projectName: String,
    val projectUrl: String,
    val imageUrl: String,
    val creatorName: String,
    val projectId: NonBlankId? = null
)

/**
 * Supported social sharing destinations and system share targets.
 *
 * Negative constraints:
 * - Do not trigger platform-specific intents without checking if the package is installed.
 *
 * @property targetPackage Optional Android package name targeting a specific social application.
 */
enum class SocialSharePlatform(val targetPackage: String?) {
    /** Copy campaign URL to clipboard. */
    COPY_LINK(null),
    /** Share to Instagram feed via app intent. */
    INSTAGRAM_FEED("com.instagram.android"),
    /** Share sticker/story asset to Instagram Stories. */
    INSTAGRAM_STORIES("com.instagram.android"),
    /** Share tweet/post to X (Twitter). */
    X("com.twitter.android"),
    /** Share link to Facebook feed. */
    FACEBOOK_FEED("com.facebook.katana"),
    /** Share story card to Facebook Stories. */
    FACEBOOK_STORIES("com.facebook.katana"),
    /** Share link directly to WhatsApp conversation. */
    WHATSAPP("com.whatsapp"),
    /** Share via system SMS/MMS messaging app. */
    MESSAGES(null),
    /** Share via email client intent. */
    EMAIL(null),
    /** Open system generic share sheet chooser. */
    MORE(null);

    fun requiresImage(): Boolean = when (this) {
        INSTAGRAM_FEED,
        INSTAGRAM_STORIES,
        FACEBOOK_FEED,
        FACEBOOK_STORIES -> true
        else -> false
    }
}

fun SocialSharePlatform.refTag(): RefTag = when (this) {
    SocialSharePlatform.COPY_LINK -> RefTag.projectShareCopyLink()
    SocialSharePlatform.INSTAGRAM_FEED -> RefTag.projectShareInstagramFeed()
    SocialSharePlatform.INSTAGRAM_STORIES -> RefTag.projectShareInstagramStories()
    SocialSharePlatform.X -> RefTag.projectShareX()
    SocialSharePlatform.FACEBOOK_FEED -> RefTag.projectShareFacebookFeed()
    SocialSharePlatform.FACEBOOK_STORIES -> RefTag.projectShareFacebookStories()
    SocialSharePlatform.WHATSAPP -> RefTag.projectShareWhatsApp()
    SocialSharePlatform.MESSAGES -> RefTag.projectShareMessages()
    SocialSharePlatform.EMAIL -> RefTag.projectShareEmail()
    SocialSharePlatform.MORE -> RefTag.projectShareMore()
}

fun SocialSharePlatform.analyticsContextType(): String = when (this) {
    SocialSharePlatform.COPY_LINK -> SharePlatformContextType.COPY_LINK.contextName
    SocialSharePlatform.INSTAGRAM_FEED -> SharePlatformContextType.INSTAGRAM_FEED.contextName
    SocialSharePlatform.INSTAGRAM_STORIES -> SharePlatformContextType.INSTAGRAM_STORIES.contextName
    SocialSharePlatform.X -> SharePlatformContextType.X.contextName
    SocialSharePlatform.FACEBOOK_FEED -> SharePlatformContextType.FACEBOOK_FEED.contextName
    SocialSharePlatform.FACEBOOK_STORIES -> SharePlatformContextType.FACEBOOK_STORIES.contextName
    SocialSharePlatform.WHATSAPP -> SharePlatformContextType.WHATSAPP.contextName
    SocialSharePlatform.MESSAGES -> SharePlatformContextType.MESSAGES.contextName
    SocialSharePlatform.EMAIL -> SharePlatformContextType.EMAIL.contextName
    SocialSharePlatform.MORE -> SharePlatformContextType.MORE.contextName
}

/**
 * Immutable UI State for Social Share bottom sheet.
 *
 * Negative constraints:
 * - Do not mutate state directly; emit through ViewModel StateFlow reducers.
 *
 * @property availablePlatforms List of sharing destinations available on current device.
 * @property heroBitmap Cached project hero bitmap for preview display.
 * @property shareImageUri FileProvider content URI pointing to generated sharing card image.
 * @property isGeneratingImage True while sharing asset card image is being rendered to disk.
 * @property copiedToClipboard True when project link was successfully copied to clipboard.
 * @property problem Structured RFC 9457 error details carrying suggestedAction and isRetriable flag if an action failed.
 */
data class SocialShareUIState(
    val availablePlatforms: List<SocialSharePlatform> = emptyList(),
    val heroBitmap: Bitmap? = null,
    val shareImageUri: Uri? = null,
    val isGeneratingImage: Boolean = false,
    val copiedToClipboard: Boolean = false,
    val problem: ProblemDetails? = null
)
