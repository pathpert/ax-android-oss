package com.kickstarter.models

internal object UserEquality {
    fun areEqual(first: User, other: Any?): Boolean {
        if (first === other) return true
        if (other !is User) return false
        return first.id() == other.id() &&
            first.alumniNewsletter() == other.alumniNewsletter() &&
            first.artsCultureNewsletter() == other.artsCultureNewsletter() &&
            first.backedProjectsCount() == other.backedProjectsCount() &&
            first.backingActionCount() == other.backingActionCount() &&
            first.createdProjectsCount() == other.createdProjectsCount() &&
            first.draftProjectsCount() == other.draftProjectsCount() &&
            first.name() == other.name() &&
            first.avatar() == other.avatar() &&
            first.facebookConnected() == other.facebookConnected() &&
            first.filmNewsletter() == other.filmNewsletter() &&
            first.gamesNewsletter() == other.gamesNewsletter() &&
            first.happeningNewsletter() == other.happeningNewsletter() &&
            first.inventNewsletter() == other.inventNewsletter() &&
            first.isAdmin() == other.isAdmin() &&
            first.location() == other.location() &&
            first.memberProjectsCount() == other.memberProjectsCount() &&
            first.musicNewsletter() == other.musicNewsletter() &&
            first.notifyMobileOfBackings() == other.notifyMobileOfBackings() &&
            first.notifyMobileOfComments() == other.notifyMobileOfComments() &&
            first.notifyMobileOfCreatorEdu() == other.notifyMobileOfCreatorEdu() &&
            first.notifyMobileOfFollower() == other.notifyMobileOfFollower() &&
            first.notifyMobileOfFriendActivity() == other.notifyMobileOfFriendActivity() &&
            first.notifyMobileOfMessages() == other.notifyMobileOfMessages() &&
            first.notifyMobileOfPostLikes() == other.notifyMobileOfPostLikes() &&
            first.notifyMobileOfUpdates() == other.notifyMobileOfUpdates() &&
            first.notifyMobileOfMarketingUpdate() == other.notifyMobileOfMarketingUpdate() &&
            first.notifyOfBackings() == other.notifyOfBackings() &&
            first.notifyOfComments() == other.notifyOfComments() &&
            first.notifyOfCommentReplies() == other.notifyOfCommentReplies() &&
            first.notifyOfCreatorDigest() == other.notifyOfCreatorDigest() &&
            first.notifyOfCreatorEdu() == other.notifyOfCreatorEdu() &&
            first.notifyOfFollower() == other.notifyOfFollower() &&
            first.notifyOfFriendActivity() == other.notifyOfFriendActivity() &&
            first.notifyOfMessages() == other.notifyOfMessages() &&
            first.optedOutOfRecommendations() == other.optedOutOfRecommendations() &&
            first.ppoHasAction() == other.ppoHasAction() &&
            first.promoNewsletter() == other.promoNewsletter() &&
            first.publishingNewsletter() == other.publishingNewsletter() &&
            first.showPublicProfile() == other.showPublicProfile() &&
            first.social() == other.social() &&
            first.starredProjectsCount() == other.starredProjectsCount() &&
            first.unreadMessagesCount() == other.unreadMessagesCount() &&
            first.unseenActivityCount() == other.unseenActivityCount() &&
            first.weeklyNewsletter() == other.weeklyNewsletter() &&
            first.needsPassword() == other.needsPassword() &&
            first.email() == other.email() &&
            first.hasPassword() == other.hasPassword() &&
            first.isCreator() == other.isCreator() &&
            first.isDeliverable() == other.isDeliverable()
    }
}
