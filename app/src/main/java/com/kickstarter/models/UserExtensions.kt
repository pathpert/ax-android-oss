package com.kickstarter.models

/** Sub-model grouping user newsletter subscription settings. */
fun User.newsletterPreferences() = UserNewsletterPreferences(
    alumni = this.alumniNewsletter(),
    artsCulture = this.artsCultureNewsletter(),
    film = this.filmNewsletter(),
    games = this.gamesNewsletter(),
    happening = this.happeningNewsletter(),
    invent = this.inventNewsletter(),
    music = this.musicNewsletter(),
    promo = this.promoNewsletter(),
    publishing = this.publishingNewsletter(),
    weekly = this.weeklyNewsletter()
)

/** Sub-model grouping user push and email notification channels. */
fun User.notificationPreferences() = UserNotificationPreferences(
    mobileBackings = this.notifyMobileOfBackings(),
    mobileComments = this.notifyMobileOfComments(),
    mobileCreatorEdu = this.notifyMobileOfCreatorEdu(),
    mobileFollower = this.notifyMobileOfFollower(),
    mobileFriendActivity = this.notifyMobileOfFriendActivity(),
    mobileMessages = this.notifyMobileOfMessages(),
    mobilePostLikes = this.notifyMobileOfPostLikes(),
    mobileUpdates = this.notifyMobileOfUpdates(),
    mobileMarketing = this.notifyMobileOfMarketingUpdate(),
    emailBackings = this.notifyOfBackings(),
    emailComments = this.notifyOfComments(),
    emailCommentReplies = this.notifyOfCommentReplies(),
    emailCreatorDigest = this.notifyOfCreatorDigest(),
    emailCreatorEdu = this.notifyOfCreatorEdu(),
    emailFollower = this.notifyOfFollower(),
    emailFriendActivity = this.notifyOfFriendActivity(),
    emailMessages = this.notifyOfMessages(),
    emailUpdates = this.notifyOfUpdates()
)

/** Sub-model grouping user activity, backings, and project counts. */
fun User.stats() = UserStats(
    backedProjectsCount = this.backedProjectsCount(),
    backingActionCount = this.backingActionCount(),
    createdProjectsCount = this.createdProjectsCount(),
    draftProjectsCount = this.draftProjectsCount(),
    erroredBackingsCount = this.erroredBackingsCount(),
    memberProjectsCount = this.memberProjectsCount(),
    starredProjectsCount = this.starredProjectsCount(),
    unreadMessagesCount = this.unreadMessagesCount(),
    unseenActivityCount = this.unseenActivityCount()
)
