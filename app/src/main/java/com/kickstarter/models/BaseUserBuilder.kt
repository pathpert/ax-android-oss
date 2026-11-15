package com.kickstarter.models

@Suppress("UNCHECKED_CAST")
open class BaseUserBuilder<T : BaseUserBuilder<T>>(
    var alumniNewsletter: Boolean = false,
    var artsCultureNewsletter: Boolean = false,
    var avatar: Avatar = Avatar.builder().build(),
    var backedProjectsCount: Int = 0,
    var backingActionCount: Int = 0,
    var createdProjectsCount: Int = 0,
    var draftProjectsCount: Int = 0,
    var erroredBackingsCount: Int = 0,
    var facebookConnected: Boolean = false,
    var filmNewsletter: Boolean = false,
    var gamesNewsletter: Boolean = false,
    var happeningNewsletter: Boolean = false,
    var id: Long = 0L,
    var inventNewsletter: Boolean = false,
    var isAdmin: Boolean = false,
    var isEmailVerified: Boolean = false,
    var isCreator: Boolean? = false,
    var isDeliverable: Boolean? = false,
    var email: String? = null,
    var chosenCurrency: String? = null,
    var location: Location? = null,
    var memberProjectsCount: Int = 0,
    var musicNewsletter: Boolean = false,
    var name: String = "",
    var notifyMobileOfBackings: Boolean = false,
    var hasPassword: Boolean? = false,
    var notifyMobileOfComments: Boolean = false,
    var notifyMobileOfCreatorEdu: Boolean = false,
    var notifyMobileOfFollower: Boolean = false,
    var notifyMobileOfFriendActivity: Boolean = false,
    var notifyMobileOfMessages: Boolean = false,
    var notifyMobileOfPostLikes: Boolean = false,
    var notifyMobileOfUpdates: Boolean = false,
    var notifyMobileOfMarketingUpdate: Boolean = false,
    var notifyOfBackings: Boolean = false,
    var notifyOfComments: Boolean = false,
    var notifyOfCommentReplies: Boolean = false,
    var notifyOfCreatorDigest: Boolean = false,
    var notifyOfCreatorEdu: Boolean = false,
    var notifyOfFollower: Boolean = false,
    var notifyOfFriendActivity: Boolean = false,
    var notifyOfMessages: Boolean = false,
    var notifyOfUpdates: Boolean = false,
    var optedOutOfRecommendations: Boolean = false,
    var promoNewsletter: Boolean = false,
    var ppoHasAction: Boolean = false,
    var publishingNewsletter: Boolean = false,
    var showPublicProfile: Boolean = false,
    var needsPassword: Boolean? = false,
    var social: Boolean = false,
    var starredProjectsCount: Int = 0,
    var unreadMessagesCount: Int = 0,
    var unseenActivityCount: Int = 0,
    var weeklyNewsletter: Boolean = false
) {
    fun alumniNewsletter(alN: Boolean?): T = apply { this.alumniNewsletter = alN ?: false } as T
    fun artsCultureNewsletter(arN: Boolean?): T = apply { this.artsCultureNewsletter = arN ?: false } as T
    fun avatar(avatar: Avatar?): T = apply { avatar?.let { this.avatar = it } } as T
    fun backedProjectsCount(bPC: Int?): T = apply { this.backedProjectsCount = bPC ?: 0 } as T
    fun backingActionCount(bPC: Int?): T = apply { this.backingActionCount = bPC ?: 0 } as T
    fun draftProjectsCount(dPC: Int?): T = apply { this.draftProjectsCount = dPC ?: 0 } as T
    fun createdProjectsCount(cPC: Int?): T = apply { this.createdProjectsCount = cPC ?: 0 } as T
    fun erroredBackingsCount(eBC: Int?): T = apply { this.erroredBackingsCount = eBC ?: 0 } as T
    fun facebookConnected(facebookConnected: Boolean?): T = apply { this.facebookConnected = facebookConnected ?: false } as T
    fun filmNewsletter(filmNewsletter: Boolean?): T = apply { this.filmNewsletter = filmNewsletter ?: false } as T
    fun gamesNewsletter(gamesNewsletter: Boolean?): T = apply { this.gamesNewsletter = gamesNewsletter ?: false } as T
    fun happeningNewsletter(happeningNewsletter: Boolean?): T = apply { this.happeningNewsletter = happeningNewsletter ?: false } as T
    fun id(id: Long?): T = apply { this.id = id ?: 0 } as T
    fun isAdmin(isAdmin: Boolean?): T = apply { this.isAdmin = isAdmin ?: false } as T
    fun isEmailVerified(isEmailVerified: Boolean?): T = apply { this.isEmailVerified = isEmailVerified ?: false } as T
    fun isCreator(isCreator: Boolean?): T = apply { this.isCreator = isCreator ?: false } as T
    fun isDeliverable(isDeliverable: Boolean?): T = apply { this.isDeliverable = isDeliverable ?: false } as T
    fun email(email: String?): T = apply { this.email = email } as T
    fun chosenCurrency(chosenCurrency: String?): T = apply { this.chosenCurrency = chosenCurrency } as T
    fun inventNewsletter(inventNewsletter: Boolean?): T = apply { this.inventNewsletter = inventNewsletter ?: false } as T
    fun hasPassword(hasPassword: Boolean?): T = apply { this.hasPassword = hasPassword ?: false } as T
    fun location(location: Location?): T = apply { this.location = location } as T
    fun memberProjectsCount(memberProjectsCount: Int?): T = apply { this.memberProjectsCount = memberProjectsCount ?: 0 } as T
    fun musicNewsletter(musicNewsletter: Boolean?): T = apply { this.musicNewsletter = musicNewsletter ?: false } as T
    fun name(name: String?): T = apply { name?.let { this.name = it } } as T
    fun notifyMobileOfBackings(notifyOfFriendActivity: Boolean?): T = apply { this.notifyMobileOfBackings = notifyOfFriendActivity ?: false } as T
    fun notifyMobileOfComments(notifyMobileOfComments: Boolean?): T = apply { this.notifyMobileOfComments = notifyMobileOfComments ?: false } as T
    fun notifyMobileOfCreatorEdu(notifyMobileOfCreatorEdu: Boolean?): T = apply { this.notifyMobileOfCreatorEdu = notifyMobileOfCreatorEdu ?: false } as T
    fun notifyMobileOfFollower(notifyMobileOfFollower: Boolean?): T = apply { this.notifyMobileOfFollower = notifyMobileOfFollower ?: false } as T
    fun notifyMobileOfFriendActivity(notifyMobileOfFriendActivity: Boolean?): T = apply { this.notifyMobileOfFriendActivity = notifyMobileOfFriendActivity ?: false } as T
    fun notifyMobileOfMessages(notifyMobileOfMessages: Boolean?): T = apply { this.notifyMobileOfMessages = notifyMobileOfMessages ?: false } as T
    fun notifyMobileOfPostLikes(notifyMobileOfPostLikes: Boolean?): T = apply { this.notifyMobileOfPostLikes = notifyMobileOfPostLikes ?: false } as T
    fun notifyMobileOfUpdates(notifyMobileOfUpdates: Boolean?): T = apply { this.notifyMobileOfUpdates = notifyMobileOfUpdates ?: false } as T
    fun notifyMobileOfMarketingUpdate(notifyMobileOfMarketingUpdate: Boolean?): T = apply { this.notifyMobileOfMarketingUpdate = notifyMobileOfMarketingUpdate ?: false } as T
    fun notifyOfBackings(notifyOfBackings: Boolean?): T = apply { this.notifyOfBackings = notifyOfBackings ?: false } as T
    fun notifyOfComments(notifyOfComments: Boolean?): T = apply { this.notifyOfComments = notifyOfComments ?: false } as T
    fun notifyOfCommentReplies(notifyOfCommentReplies: Boolean?): T = apply { this.notifyOfCommentReplies = notifyOfCommentReplies ?: false } as T
    fun notifyOfCreatorDigest(notifyOfCreatorDigest: Boolean?): T = apply { this.notifyOfCreatorDigest = notifyOfCreatorDigest ?: false } as T
    fun notifyOfCreatorEdu(notifyOfCreatorEdu: Boolean?): T = apply { this.notifyOfCreatorEdu = notifyOfCreatorEdu ?: false } as T
    fun notifyOfFollower(notifyOfFollower: Boolean?): T = apply { this.notifyOfFollower = notifyOfFollower ?: false } as T
    fun notifyOfFriendActivity(notifyOfFriendActivity: Boolean?): T = apply { this.notifyOfFriendActivity = notifyOfFriendActivity ?: false } as T
    fun notifyOfMessages(notifyOfMessages: Boolean?): T = apply { this.notifyOfMessages = notifyOfMessages ?: false } as T
    fun notifyOfUpdates(notifyOfUpdates: Boolean?): T = apply { this.notifyOfUpdates = notifyOfUpdates ?: false } as T
    fun optedOutOfRecommendations(optedOutOfRecommendations: Boolean?): T = apply { this.optedOutOfRecommendations = optedOutOfRecommendations ?: false } as T
    fun ppoHasAction(ppoHasAction: Boolean?): T = apply { this.ppoHasAction = ppoHasAction ?: false } as T
    fun promoNewsletter(promoNewsletter: Boolean?): T = apply { this.promoNewsletter = promoNewsletter ?: false } as T
    fun publishingNewsletter(publishingNewsletter: Boolean?): T = apply { this.publishingNewsletter = publishingNewsletter ?: false } as T
    fun showPublicProfile(showPublicProfile: Boolean?): T = apply { this.showPublicProfile = showPublicProfile ?: false } as T
    fun social(social: Boolean?): T = apply { this.social = social ?: false } as T
    fun starredProjectsCount(starredProjectsCount: Int?): T = apply { starredProjectsCount?.let { this.starredProjectsCount = it } } as T
    fun unreadMessagesCount(unreadMessagesCount: Int?): T = apply { this.unreadMessagesCount = unreadMessagesCount ?: 0 } as T
    fun unseenActivityCount(unseenActivityCount: Int?): T = apply { this.unseenActivityCount = unseenActivityCount ?: 0 } as T
    fun weeklyNewsletter(weeklyNewsletter: Boolean?): T = apply { this.weeklyNewsletter = weeklyNewsletter ?: false } as T
    fun needsPassword(needsPassword: Boolean?): T = apply { this.needsPassword = needsPassword ?: false } as T

    fun newsletterPreferences(prefs: UserNewsletterPreferences): T = apply {
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
    } as T

    fun notificationPreferences(prefs: UserNotificationPreferences): T = apply {
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
    } as T

    fun stats(stats: UserStats): T = apply {
        backedProjectsCount(stats.backedProjectsCount)
        backingActionCount(stats.backingActionCount)
        createdProjectsCount(stats.createdProjectsCount)
        draftProjectsCount(stats.draftProjectsCount)
        erroredBackingsCount(stats.erroredBackingsCount)
        memberProjectsCount(stats.memberProjectsCount)
        starredProjectsCount(stats.starredProjectsCount)
        unreadMessagesCount(stats.unreadMessagesCount)
        unseenActivityCount(stats.unseenActivityCount)
    } as T

    fun build(): User = UserBuilderHelper.build(this)
}
