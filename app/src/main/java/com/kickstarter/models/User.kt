package com.kickstarter.models

import android.content.res.Resources
import android.os.Parcelable
import com.kickstarter.R
import kotlinx.parcelize.Parcelize

/**
 * Immutable domain model representing a registered Kickstarter user or creator.
 *
 * Negative constraints:
 * - Do not assume [email] is present for anonymous or public user cards.
 * - Do not confuse the integer [id] with GraphQL Relay global IDs.
 */
@Deprecated("Legacy monolithic domain model. For new code, consume feature-scoped contracts in com.kickstarter.features.*")
@Parcelize
@androidx.annotation.Keep
class User private constructor(
    private val alumniNewsletter: Boolean,
    private val artsCultureNewsletter: Boolean,
    private val avatar: Avatar,
    private val backedProjectsCount: Int,
    private val backingActionCount: Int,
    private val createdProjectsCount: Int,
    private val draftProjectsCount: Int,
    private val erroredBackingsCount: Int,
    private val facebookConnected: Boolean,
    private val filmNewsletter: Boolean,
    private val gamesNewsletter: Boolean,
    private val happeningNewsletter: Boolean,
    private val id: Long,
    private val inventNewsletter: Boolean,
    private val isAdmin: Boolean,
    private val isEmailVerified: Boolean,
    private val email: String?,
    private val hasPassword: Boolean?,
    private val isCreator: Boolean?,
    private val isDeliverable: Boolean?,
    private val chosenCurrency: String?,
    private val location: Location?,
    private val memberProjectsCount: Int,
    private val musicNewsletter: Boolean,
    private val name: String,
    private val notifyMobileOfBackings: Boolean,
    private val notifyMobileOfComments: Boolean,
    private val notifyMobileOfCreatorEdu: Boolean,
    private val notifyMobileOfFollower: Boolean,
    private val notifyMobileOfFriendActivity: Boolean,
    private val notifyMobileOfMessages: Boolean,
    private val notifyMobileOfPostLikes: Boolean,
    private val notifyMobileOfUpdates: Boolean,
    private val notifyMobileOfMarketingUpdate: Boolean,
    private val notifyOfBackings: Boolean,
    private val notifyOfComments: Boolean,
    private val notifyOfCommentReplies: Boolean,
    private val notifyOfCreatorDigest: Boolean,
    private val notifyOfCreatorEdu: Boolean,
    private val notifyOfFollower: Boolean,
    private val notifyOfFriendActivity: Boolean,
    private val notifyOfMessages: Boolean,
    private val notifyOfUpdates: Boolean,
    private val optedOutOfRecommendations: Boolean,
    private val ppoHasAction: Boolean,
    private val promoNewsletter: Boolean,
    private val publishingNewsletter: Boolean,
    private val showPublicProfile: Boolean,
    private val needsPassword: Boolean?,
    private val social: Boolean,
    private val starredProjectsCount: Int,
    private val unreadMessagesCount: Int,
    private val unseenActivityCount: Int,
    private val weeklyNewsletter: Boolean
) : Parcelable, Relay {

    /** Sub-model grouping user newsletter subscription settings. */
    fun newsletterPreferences() = UserNewsletterPreferences(
        alumni = this.alumniNewsletter,
        artsCulture = this.artsCultureNewsletter,
        film = this.filmNewsletter,
        games = this.gamesNewsletter,
        happening = this.happeningNewsletter,
        invent = this.inventNewsletter,
        music = this.musicNewsletter,
        promo = this.promoNewsletter,
        publishing = this.publishingNewsletter,
        weekly = this.weeklyNewsletter
    )

    /** Sub-model grouping user push and email notification channels. */
    fun notificationPreferences() = UserNotificationPreferences(
        mobileBackings = this.notifyMobileOfBackings,
        mobileComments = this.notifyMobileOfComments,
        mobileCreatorEdu = this.notifyMobileOfCreatorEdu,
        mobileFollower = this.notifyMobileOfFollower,
        mobileFriendActivity = this.notifyMobileOfFriendActivity,
        mobileMessages = this.notifyMobileOfMessages,
        mobilePostLikes = this.notifyMobileOfPostLikes,
        mobileUpdates = this.notifyMobileOfUpdates,
        mobileMarketing = this.notifyMobileOfMarketingUpdate,
        emailBackings = this.notifyOfBackings,
        emailComments = this.notifyOfComments,
        emailCommentReplies = this.notifyOfCommentReplies,
        emailCreatorDigest = this.notifyOfCreatorDigest,
        emailCreatorEdu = this.notifyOfCreatorEdu,
        emailFollower = this.notifyOfFollower,
        emailFriendActivity = this.notifyOfFriendActivity,
        emailMessages = this.notifyOfMessages,
        emailUpdates = this.notifyOfUpdates
    )

    /** Sub-model grouping user activity, backings, and project counts. */
    fun stats() = UserStats(
        backedProjectsCount = this.backedProjectsCount,
        backingActionCount = this.backingActionCount,
        createdProjectsCount = this.createdProjectsCount,
        draftProjectsCount = this.draftProjectsCount,
        erroredBackingsCount = this.erroredBackingsCount,
        memberProjectsCount = this.memberProjectsCount,
        starredProjectsCount = this.starredProjectsCount,
        unreadMessagesCount = this.unreadMessagesCount,
        unseenActivityCount = this.unseenActivityCount
    )

    /** Whether the user is subscribed to the alumni newsletter. */
    fun alumniNewsletter() = this.alumniNewsletter

    /** Whether the user is subscribed to the arts & culture newsletter. */
    fun artsCultureNewsletter() = this.artsCultureNewsletter

    /** Profile avatar images for the user. */
    fun avatar() = this.avatar

    /** Number of campaigns backed by this user. */
    fun backedProjectsCount() = this.backedProjectsCount

    /** Number of pending backing/pledge actions requiring user attention. */
    fun backingActionCount() = this.backingActionCount

    /** Number of campaigns created by this user. */
    fun createdProjectsCount() = this.createdProjectsCount

    /** Number of unlaunched draft campaigns owned by this user. */
    fun draftProjectsCount() = this.draftProjectsCount

    /** Number of backings with billing or payment errors. */
    fun erroredBackingsCount() = this.erroredBackingsCount

    /** Whether the user has linked a Facebook account. */
    fun facebookConnected() = this.facebookConnected

    /** Whether the user is subscribed to the film newsletter. */
    fun filmNewsletter() = this.filmNewsletter

    /** Whether the user is subscribed to the games newsletter. */
    fun gamesNewsletter() = this.gamesNewsletter

    /** Whether the user is subscribed to the Happening newsletter. */
    fun happeningNewsletter() = this.happeningNewsletter

    /**
     * Database identifier (PID) of the user.
     * Negative constraint: Do not confuse with GraphQL Relay global IDs.
     */
    override fun id() = this.id

    /**
     * Whether the user has staff or administrative privileges.
     */
    fun isAdmin() = this.isAdmin

    /**
     * Whether this account has an active password (false for social/OAuth-only accounts).
     */
    fun hasPassword() = this.hasPassword

    /**
     * Whether the primary email address has been verified.
     */
    fun isEmailVerified() = this.isEmailVerified

    /**
     * Whether the user has launched at least one active or historical campaign.
     */
    fun isCreator() = this.isCreator

    /**
     * Whether physical reward delivery is currently supported/active for this user's address.
     */
    fun isDeliverable() = this.isDeliverable

    /**
     * Verified primary email address of the authenticated user.
     * Negative constraint: May be null for unauthenticated public profile queries.
     */
    fun email() = this.email

    /** User's preferred display currency (e.g. "USD", "EUR", "CAD"). */
    fun chosenCurrency() = this.chosenCurrency

    /** Whether the user is subscribed to the invent newsletter. */
    fun inventNewsletter() = this.inventNewsletter

    /** Geographic location associated with the user profile. */
    fun location() = this.location

    /** Number of projects on which the user is a team member/collaborator. */
    fun memberProjectsCount() = this.memberProjectsCount

    /** Whether the user is subscribed to the music newsletter. */
    fun musicNewsletter() = this.musicNewsletter

    /** Full display name of the user. */
    fun name() = this.name

    /** Push notification preference for project backings. */
    fun notifyMobileOfBackings() = this.notifyMobileOfBackings

    /** Push notification preference for project comments. */
    fun notifyMobileOfComments() = this.notifyMobileOfComments

    /** Push notification preference for creator education materials. */
    fun notifyMobileOfCreatorEdu() = this.notifyMobileOfCreatorEdu

    /** Push notification preference when followed by another user. */
    fun notifyMobileOfFollower() = this.notifyMobileOfFollower

    /** Push notification preference for friend activity. */
    fun notifyMobileOfFriendActivity() = this.notifyMobileOfFriendActivity

    /** Push notification preference for direct messages. */
    fun notifyMobileOfMessages() = this.notifyMobileOfMessages

    /** Push notification preference for post likes. */
    fun notifyMobileOfPostLikes() = this.notifyMobileOfPostLikes

    /** Push notification preference for project updates. */
    fun notifyMobileOfUpdates() = this.notifyMobileOfUpdates

    /** Push notification preference for marketing announcements. */
    fun notifyMobileOfMarketingUpdate() = this.notifyMobileOfMarketingUpdate

    /** Email notification preference for project backings. */
    fun notifyOfBackings() = this.notifyOfBackings

    /** Email notification preference for project comments. */
    fun notifyOfComments() = this.notifyOfComments

    /** Email notification preference for comment replies. */
    fun notifyOfCommentReplies() = this.notifyOfCommentReplies

    /** Email preference for creator performance digest. */
    fun notifyOfCreatorDigest() = this.notifyOfCreatorDigest

    /** Email preference for creator education tips. */
    fun notifyOfCreatorEdu() = this.notifyOfCreatorEdu

    /** Email preference when followed by another user. */
    fun notifyOfFollower() = this.notifyOfFollower

    /** Email preference for friend activities. */
    fun notifyOfFriendActivity() = this.notifyOfFriendActivity

    /** Email preference for direct messages. */
    fun notifyOfMessages() = this.notifyOfMessages

    /** Email preference for project updates. */
    fun notifyOfUpdates() = this.notifyOfUpdates

    /** Whether the user has opted out of personalized recommendation algorithms. */
    fun optedOutOfRecommendations() = this.optedOutOfRecommendations

    /** Whether pending actions exist in the Pledged Projects Overview. */
    fun ppoHasAction() = this.ppoHasAction

    /** Whether the user is subscribed to promotional newsletters. */
    fun promoNewsletter() = this.promoNewsletter

    /** Whether the user is subscribed to publishing newsletters. */
    fun publishingNewsletter() = this.publishingNewsletter

    /** Whether the user's backing history is publicly visible. */
    fun showPublicProfile() = this.showPublicProfile

    /** Whether password creation is required for this account. */
    fun needsPassword() = this.needsPassword

    /** Whether social friend discovery features are enabled. */
    fun social() = this.social

    /** Number of projects saved or starred by this user. */
    fun starredProjectsCount() = this.starredProjectsCount

    /** Total count of unread direct messages across all threads. */
    fun unreadMessagesCount() = this.unreadMessagesCount

    /** Total count of unseen activity feed notifications. */
    fun unseenActivityCount() = this.unseenActivityCount

    /** Whether the user is subscribed to the weekly Kickstarter newsletter. */
    fun weeklyNewsletter() = this.weeklyNewsletter

    @Parcelize
    data class Builder(
        private var alumniNewsletter: Boolean = false,
        private var artsCultureNewsletter: Boolean = false,
        private var avatar: Avatar = Avatar.builder().build(),
        private var backedProjectsCount: Int = 0,
        private var backingActionCount: Int = 0,
        private var createdProjectsCount: Int = 0,
        private var draftProjectsCount: Int = 0,
        private var erroredBackingsCount: Int = 0,
        private var facebookConnected: Boolean = false,
        private var filmNewsletter: Boolean = false,
        private var gamesNewsletter: Boolean = false,
        private var happeningNewsletter: Boolean = false,
        private var id: Long = 0L,
        private var inventNewsletter: Boolean = false,
        private var isAdmin: Boolean = false,
        private var isEmailVerified: Boolean = false,
        private var isCreator: Boolean? = false,
        private var isDeliverable: Boolean? = false,
        private var email: String? = null,
        private var chosenCurrency: String? = null,
        private var location: Location? = null,
        private var memberProjectsCount: Int = 0,
        private var musicNewsletter: Boolean = false,
        private var name: String = "",
        private var notifyMobileOfBackings: Boolean = false,
        private var hasPassword: Boolean? = false,
        private var notifyMobileOfComments: Boolean = false,
        private var notifyMobileOfCreatorEdu: Boolean = false,
        private var notifyMobileOfFollower: Boolean = false,
        private var notifyMobileOfFriendActivity: Boolean = false,
        private var notifyMobileOfMessages: Boolean = false,
        private var notifyMobileOfPostLikes: Boolean = false,
        private var notifyMobileOfUpdates: Boolean = false,
        private var notifyMobileOfMarketingUpdate: Boolean = false,
        private var notifyOfBackings: Boolean = false,
        private var notifyOfComments: Boolean = false,
        private var notifyOfCommentReplies: Boolean = false,
        private var notifyOfCreatorDigest: Boolean = false,
        private var notifyOfCreatorEdu: Boolean = false,
        private var notifyOfFollower: Boolean = false,
        private var notifyOfFriendActivity: Boolean = false,
        private var notifyOfMessages: Boolean = false,
        private var notifyOfUpdates: Boolean = false,
        private var optedOutOfRecommendations: Boolean = false,
        private var promoNewsletter: Boolean = false,
        private var ppoHasAction: Boolean = false,
        private var publishingNewsletter: Boolean = false,
        private var showPublicProfile: Boolean = false,
        private var needsPassword: Boolean? = false,
        private var social: Boolean = false,
        private var starredProjectsCount: Int = 0,
        private var unreadMessagesCount: Int = 0,
        private var unseenActivityCount: Int = 0,
        private var weeklyNewsletter: Boolean = false
    ) : Parcelable {

        fun alumniNewsletter(alN: Boolean?) = apply { this.alumniNewsletter = alN ?: false }
        fun artsCultureNewsletter(arN: Boolean?) = apply { this.artsCultureNewsletter = arN ?: false }
        fun avatar(avatar: Avatar?) = apply { avatar?.let { this.avatar = it } }
        fun backedProjectsCount(bPC: Int?) = apply { this.backedProjectsCount = bPC ?: 0 }
        fun backingActionCount(bPC: Int?) = apply { this.backingActionCount = bPC ?: 0 }
        fun draftProjectsCount(dPC: Int?) = apply { this.draftProjectsCount = dPC ?: 0 }
        fun createdProjectsCount(cPC: Int?) = apply { this.createdProjectsCount = cPC ?: 0 }
        fun erroredBackingsCount(eBC: Int?) = apply { this.erroredBackingsCount = eBC ?: 0 }
        fun facebookConnected(facebookConnected: Boolean?) = apply { this.facebookConnected = facebookConnected ?: false }
        fun filmNewsletter(filmNewsletter: Boolean?) = apply { this.filmNewsletter = filmNewsletter ?: false }
        fun gamesNewsletter(gamesNewsletter: Boolean?) = apply { this.gamesNewsletter = gamesNewsletter ?: false }
        fun happeningNewsletter(happeningNewsletter: Boolean?) = apply { this.happeningNewsletter = happeningNewsletter ?: false }
        fun id(id: Long?) = apply { this.id = id ?: 0 }
        fun isAdmin(isAdmin: Boolean?) = apply { this.isAdmin = isAdmin ?: false }
        fun isEmailVerified(isEmailVerified: Boolean?) = apply { this.isEmailVerified = isEmailVerified ?: false }
        fun isCreator(isCreator: Boolean?) = apply { this.isCreator = isCreator ?: false }
        fun isDeliverable(isDeliverable: Boolean?) = apply { this.isDeliverable = isDeliverable ?: false }
        fun email(email: String?) = apply { this.email = email }
        fun chosenCurrency(chosenCurrency: String?) = apply { this.chosenCurrency = chosenCurrency }
        fun inventNewsletter(inventNewsletter: Boolean?) = apply { this.inventNewsletter = inventNewsletter ?: false }
        fun hasPassword(hasPassword: Boolean?) = apply { this.hasPassword = hasPassword ?: false }
        fun location(location: Location?) = apply { this.location = location }
        fun memberProjectsCount(memberProjectsCount: Int?) = apply { this.memberProjectsCount = memberProjectsCount ?: 0 }
        fun musicNewsletter(musicNewsletter: Boolean?) = apply { this.musicNewsletter = musicNewsletter ?: false }
        fun name(name: String?) = apply { name?.let { this.name = it } }
        fun notifyMobileOfBackings(notifyOfFriendActivity: Boolean?) = apply { this.notifyMobileOfBackings = notifyOfFriendActivity ?: false }
        fun notifyMobileOfComments(notifyMobileOfComments: Boolean?) = apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false }
        fun notifyMobileOfCreatorEdu(notifyMobileOfCreatorEdu: Boolean?) = apply { this.notifyMobileOfCreatorEdu = notifyMobileOfCreatorEdu ?: false }
        fun notifyMobileOfFollower(notifyMobileOfFollower: Boolean?) = apply { this.notifyMobileOfFollower = notifyMobileOfFollower ?: false }
        fun notifyMobileOfFriendActivity(notifyMobileOfFriendActivity: Boolean?) = apply { this.notifyMobileOfFriendActivity = notifyMobileOfFriendActivity ?: false }
        fun notifyMobileOfMessages(notifyMobileOfMessages: Boolean?) = apply { this.notifyMobileOfMessages = notifyMobileOfMessages ?: false }
        fun notifyMobileOfPostLikes(notifyMobileOfPostLikes: Boolean?) = apply { this.notifyMobileOfPostLikes = notifyMobileOfPostLikes ?: false }
        fun notifyMobileOfUpdates(notifyMobileOfUpdates: Boolean?) = apply { this.notifyMobileOfUpdates = notifyMobileOfUpdates ?: false }
        fun notifyMobileOfMarketingUpdate(notifyMobileOfMarketingUpdate: Boolean?) = apply { this.notifyMobileOfMarketingUpdate = notifyMobileOfMarketingUpdate ?: false }
        fun notifyOfBackings(notifyOfBackings: Boolean?) = apply { this.notifyOfBackings = notifyOfBackings ?: false }
        fun notifyOfComments(notifyOfComments: Boolean?) = apply { this.notifyOfComments = notifyOfComments ?: false }
        fun notifyOfCommentReplies(notifyOfCommentReplies: Boolean?) = apply { this.notifyOfCommentReplies = notifyOfCommentReplies ?: false }
        fun notifyOfCreatorDigest(notifyOfCreatorDigest: Boolean?) = apply { this.notifyOfCreatorDigest = notifyOfCreatorDigest ?: false }
        fun notifyOfCreatorEdu(notifyOfCreatorEdu: Boolean?) = apply { this.notifyOfCreatorEdu = notifyOfCreatorEdu ?: false }
        fun notifyOfFollower(notifyOfFollower: Boolean?) = apply { this.notifyOfFollower = notifyOfFollower ?: false }
        fun notifyOfFriendActivity(notifyOfFriendActivity: Boolean?) = apply { this.notifyOfFriendActivity = notifyOfFriendActivity ?: false }
        fun notifyOfMessages(notifyOfMessages: Boolean?) = apply { this.notifyOfMessages = notifyOfMessages ?: false }
        fun notifyOfUpdates(notifyOfUpdates: Boolean?) = apply { this.notifyOfUpdates = notifyOfUpdates ?: false }
        fun optedOutOfRecommendations(optedOutOfRecommendations: Boolean?) = apply { this.optedOutOfRecommendations = optedOutOfRecommendations ?: false }
        fun ppoHasAction(ppoHasAction: Boolean?) = apply { this.ppoHasAction = ppoHasAction ?: false }
        fun promoNewsletter(promoNewsletter: Boolean?) = apply { this.promoNewsletter = promoNewsletter ?: false }
        fun publishingNewsletter(publishingNewsletter: Boolean?) = apply { this.publishingNewsletter = publishingNewsletter ?: false }
        fun showPublicProfile(showPublicProfile: Boolean?) = apply { this.showPublicProfile = showPublicProfile ?: false }
        fun social(social: Boolean?) = apply { this.social = social ?: false }
        fun starredProjectsCount(starredProjectsCount: Int?) = apply { starredProjectsCount?.let { this.starredProjectsCount = it } }
        fun unreadMessagesCount(unreadMessagesCount: Int?) = apply { this.unreadMessagesCount = unreadMessagesCount ?: 0 }
        fun unseenActivityCount(unseenActivityCount: Int?) = apply { this.unseenActivityCount = unseenActivityCount ?: 0 }
        fun weeklyNewsletter(weeklyNewsletter: Boolean?) = apply { this.weeklyNewsletter = weeklyNewsletter ?: false }
        fun needsPassword(needsPassword: Boolean?) = apply { this.needsPassword = needsPassword ?: false }

        fun newsletterPreferences(prefs: UserNewsletterPreferences) = apply {
            alumniNewsletter(prefs.alumni)
            artsCultureNewsletter(prefs.artsCulture)
            filmNewsletter(prefs.film)
            gamesNewsletter(prefs.games)
            happeningNewsletter(prefs.happening)
            inventNewsletter(prefs.invent)
            musicNewsletter(prefs.music)
            promoNewsletter(prefs.promo)
            publishingNewsletter(prefs.publishing)
            weeklyNewsletter(prefs.weekly)
        }

        fun notificationPreferences(prefs: UserNotificationPreferences) = apply {
            notifyMobileOfBackings(prefs.mobileBackings)
            notifyMobileOfComments(prefs.mobileComments)
            notifyMobileOfCreatorEdu(prefs.mobileCreatorEdu)
            notifyMobileOfFollower(prefs.mobileFollower)
            notifyMobileOfFriendActivity(prefs.mobileFriendActivity)
            notifyMobileOfMessages(prefs.mobileMessages)
            notifyMobileOfPostLikes(prefs.mobilePostLikes)
            notifyMobileOfUpdates(prefs.mobileUpdates)
            notifyMobileOfMarketingUpdate(prefs.mobileMarketing)
            notifyOfBackings(prefs.emailBackings)
            notifyOfComments(prefs.emailComments)
            notifyOfCommentReplies(prefs.emailCommentReplies)
            notifyOfCreatorDigest(prefs.emailCreatorDigest)
            notifyOfCreatorEdu(prefs.emailCreatorEdu)
            notifyOfFollower(prefs.emailFollower)
            notifyOfFriendActivity(prefs.emailFriendActivity)
            notifyOfMessages(prefs.emailMessages)
            notifyOfUpdates(prefs.emailUpdates)
        }

        fun stats(stats: UserStats) = apply {
            backedProjectsCount(stats.backedProjectsCount)
            backingActionCount(stats.backingActionCount)
            createdProjectsCount(stats.createdProjectsCount)
            draftProjectsCount(stats.draftProjectsCount)
            erroredBackingsCount(stats.erroredBackingsCount)
            memberProjectsCount(stats.memberProjectsCount)
            starredProjectsCount(stats.starredProjectsCount)
            unreadMessagesCount(stats.unreadMessagesCount)
            unseenActivityCount(stats.unseenActivityCount)
        }

        fun build() = User(
            alumniNewsletter = alumniNewsletter,
            artsCultureNewsletter = artsCultureNewsletter,
            avatar = avatar,
            backedProjectsCount = backedProjectsCount,
            backingActionCount = backingActionCount,
            createdProjectsCount = createdProjectsCount,
            draftProjectsCount = draftProjectsCount,
            erroredBackingsCount = erroredBackingsCount,
            facebookConnected = facebookConnected,
            filmNewsletter = filmNewsletter,
            gamesNewsletter = gamesNewsletter,
            happeningNewsletter = happeningNewsletter,
            id = id,
            hasPassword = hasPassword,
            inventNewsletter = inventNewsletter,
            isAdmin = isAdmin,
            isEmailVerified = isEmailVerified,
            isCreator = isCreator,
            isDeliverable = isDeliverable,
            email = email,
            chosenCurrency = chosenCurrency,
            location = location,
            memberProjectsCount = memberProjectsCount,
            musicNewsletter = musicNewsletter,
            name = name,
            notifyMobileOfBackings = notifyMobileOfBackings,
            notifyMobileOfComments = notifyMobileOfComments,
            notifyMobileOfCreatorEdu = notifyMobileOfCreatorEdu,
            notifyMobileOfFollower = notifyMobileOfFollower,
            notifyMobileOfFriendActivity = notifyMobileOfFriendActivity,
            notifyMobileOfMessages = notifyMobileOfMessages,
            notifyMobileOfPostLikes = notifyMobileOfPostLikes,
            notifyMobileOfUpdates = notifyMobileOfUpdates,
            notifyMobileOfMarketingUpdate = notifyMobileOfMarketingUpdate,
            notifyOfBackings = notifyOfBackings,
            notifyOfComments = notifyOfComments,
            notifyOfCommentReplies = notifyOfCommentReplies,
            notifyOfCreatorDigest = notifyOfCreatorDigest,
            notifyOfCreatorEdu = notifyOfCreatorEdu,
            notifyOfFollower = notifyOfFollower,
            notifyOfFriendActivity = notifyOfFriendActivity,
            notifyOfMessages = notifyOfMessages,
            notifyOfUpdates = notifyOfUpdates,
            optedOutOfRecommendations = optedOutOfRecommendations,
            ppoHasAction = ppoHasAction,
            promoNewsletter = promoNewsletter,
            publishingNewsletter = publishingNewsletter,
            showPublicProfile = showPublicProfile,
            social = social,
            starredProjectsCount = starredProjectsCount,
            unreadMessagesCount = unreadMessagesCount,
            unseenActivityCount = unseenActivityCount,
            weeklyNewsletter = weeklyNewsletter,
            needsPassword = needsPassword
        )
    }

    fun param(): String {
        return id().toString()
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        alumniNewsletter = alumniNewsletter,
        artsCultureNewsletter = artsCultureNewsletter,
        avatar = avatar,
        backedProjectsCount = backedProjectsCount,
        backingActionCount = backingActionCount,
        createdProjectsCount = createdProjectsCount,
        draftProjectsCount = draftProjectsCount,
        erroredBackingsCount = erroredBackingsCount,
        facebookConnected = facebookConnected,
        filmNewsletter = filmNewsletter,
        gamesNewsletter = gamesNewsletter,
        happeningNewsletter = happeningNewsletter,
        id = id,
        inventNewsletter = inventNewsletter,
        isAdmin = isAdmin,
        isEmailVerified = isEmailVerified,
        email = email,
        hasPassword = hasPassword,
        isCreator = isCreator,
        isDeliverable = isDeliverable,
        chosenCurrency = chosenCurrency,
        location = location,
        memberProjectsCount = memberProjectsCount,
        musicNewsletter = musicNewsletter,
        name = name,
        notifyMobileOfBackings = notifyMobileOfBackings,
        notifyMobileOfComments = notifyMobileOfComments,
        notifyMobileOfCreatorEdu = notifyMobileOfCreatorEdu,
        notifyMobileOfFollower = notifyMobileOfFollower,
        notifyMobileOfFriendActivity = notifyMobileOfFriendActivity,
        notifyMobileOfMessages = notifyMobileOfMessages,
        notifyMobileOfPostLikes = notifyMobileOfPostLikes,
        notifyMobileOfUpdates = notifyMobileOfUpdates,
        notifyMobileOfMarketingUpdate = notifyMobileOfMarketingUpdate,
        notifyOfBackings = notifyOfBackings,
        notifyOfComments = notifyOfComments,
        notifyOfCommentReplies = notifyOfCommentReplies,
        notifyOfCreatorDigest = notifyOfCreatorDigest,
        notifyOfCreatorEdu = notifyOfCreatorEdu,
        notifyOfFollower = notifyOfFollower,
        notifyOfFriendActivity = notifyOfFriendActivity,
        notifyOfMessages = notifyOfMessages,
        notifyOfUpdates = notifyOfUpdates,
        optedOutOfRecommendations = optedOutOfRecommendations,
        ppoHasAction = ppoHasAction,
        promoNewsletter = promoNewsletter,
        publishingNewsletter = publishingNewsletter,
        showPublicProfile = showPublicProfile,
        social = social,
        starredProjectsCount = starredProjectsCount,
        unreadMessagesCount = unreadMessagesCount,
        unseenActivityCount = unseenActivityCount,
        weeklyNewsletter = weeklyNewsletter,
        needsPassword = needsPassword
    )

    enum class EmailFrequency(private val stringResId: Int) {
        TWICE_A_DAY_SUMMARY(R.string.Twice_a_day_summary), DAILY_SUMMARY(R.string.Daily_summary);

        companion object {
            fun getStrings(resources: Resources): Array<String?> {
                val strings = arrayOfNulls<String>(values().size)
                val values = values()
                var i = 0
                val valuesLength = values.size
                while (i < valuesLength) {
                    val emailFrequency = values[i]
                    strings[i] = resources.getString(emailFrequency.stringResId)
                    i++
                }
                return strings
            }
        }
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is User) {
            equals = id() == obj.id() &&
                alumniNewsletter() == obj.alumniNewsletter() &&
                artsCultureNewsletter() == obj.artsCultureNewsletter() &&
                backedProjectsCount() == obj.backedProjectsCount() &&
                backingActionCount() == obj.backingActionCount() &&
                createdProjectsCount() == obj.createdProjectsCount() &&
                draftProjectsCount() == obj.draftProjectsCount() &&
                name() == obj.name() &&
                avatar() == obj.avatar() &&
                createdProjectsCount() == obj.createdProjectsCount() &&
                facebookConnected() == obj.facebookConnected() &&
                filmNewsletter() == obj.filmNewsletter() &&
                facebookConnected() == obj.facebookConnected() &&
                gamesNewsletter() == obj.gamesNewsletter() &&
                happeningNewsletter() == obj.happeningNewsletter() &&
                inventNewsletter() == obj.inventNewsletter() &&
                isAdmin() == obj.isAdmin() &&
                location() == obj.location() &&
                memberProjectsCount() == obj.memberProjectsCount() &&
                musicNewsletter() == obj.musicNewsletter() &&
                notifyMobileOfBackings() == obj.notifyMobileOfBackings() &&
                notifyMobileOfComments() == obj.notifyMobileOfComments() &&
                notifyMobileOfCreatorEdu() == obj.notifyMobileOfCreatorEdu() &&
                notifyMobileOfFollower() == obj.notifyMobileOfFollower() &&
                notifyMobileOfFriendActivity() == obj.notifyMobileOfFriendActivity() &&
                notifyMobileOfMessages() == obj.notifyMobileOfMessages() &&
                notifyMobileOfPostLikes() == obj.notifyMobileOfPostLikes() &&
                notifyMobileOfUpdates() == obj.notifyMobileOfUpdates() &&
                notifyMobileOfMarketingUpdate() == obj.notifyMobileOfMarketingUpdate() &&
                notifyOfBackings() == obj.notifyOfBackings() &&
                notifyOfComments() == obj.notifyOfComments() &&
                notifyOfCommentReplies() == obj.notifyOfCommentReplies() &&
                notifyOfCreatorDigest() == obj.notifyOfCreatorDigest() &&
                notifyOfCreatorEdu() == obj.notifyOfCreatorEdu() &&
                notifyOfFollower() == obj.notifyOfFollower() &&
                notifyOfFriendActivity() == obj.notifyOfFriendActivity() &&
                notifyOfMessages() == obj.notifyOfMessages() &&
                optedOutOfRecommendations() == obj.optedOutOfRecommendations() &&
                ppoHasAction() == obj.ppoHasAction() &&
                promoNewsletter() == obj.promoNewsletter() &&
                publishingNewsletter() == obj.publishingNewsletter() &&
                showPublicProfile() == obj.showPublicProfile() &&
                social() == obj.social() &&
                starredProjectsCount() == obj.starredProjectsCount() &&
                unreadMessagesCount() == obj.unreadMessagesCount() &&
                unseenActivityCount() == obj.unseenActivityCount() &&
                weeklyNewsletter() == obj.weeklyNewsletter() &&
                needsPassword() == obj.needsPassword() &&
                email() == obj.email() &&
                hasPassword() == obj.hasPassword() &&
                isCreator() == obj.isCreator() &&
                isDeliverable() == obj.isDeliverable() &&
                isDeliverable() == obj.isDeliverable()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
