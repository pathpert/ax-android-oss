package com.kickstarter.models

internal object ProjectEquality {
    fun areEqual(first: Project, other: Any?): Boolean {
        if (first === other) return true
        if (other !is Project) return false
        return first.backersCount() == other.backersCount() &&
            first.availableCardTypes() == other.availableCardTypes() &&
            first.blurb() == other.blurb() &&
            first.backing() == other.backing() &&
            first.category() == other.category() &&
            first.commentsCount() == other.commentsCount() &&
            first.country() == other.country() &&
            first.countryCode() == other.countryCode() &&
            first.createdAt() == other.createdAt() &&
            first.creator() == other.creator() &&
            first.currency() == other.currency() &&
            first.currencySymbol() == other.currencySymbol() &&
            first.currentCurrency() == other.currentCurrency() &&
            first.currencyTrailingCode() == other.currencyTrailingCode() &&
            first.displayPrelaunch() == other.displayPrelaunch() &&
            first.featuredAt() == other.featuredAt() &&
            first.friends() == other.friends() &&
            first.deadline() == other.deadline() &&
            first.fxRate() == other.fxRate() &&
            first.goal() == other.goal() &&
            first.id() == other.id() &&
            first.isBacking() == other.isBacking() &&
            first.isPledgeOverTimeAllowed() == other.isPledgeOverTimeAllowed() &&
            first.isStarred() == other.isStarred() &&
            first.lastUpdatePublishedAt() == other.lastUpdatePublishedAt() &&
            first.lastWave() == other.lastWave() &&
            first.launchedAt() == other.launchedAt() &&
            first.location() == other.location() &&
            first.name() == other.name() &&
            first.percentFunded() == other.percentFunded() &&
            first.permissions() == other.permissions() &&
            first.pledged() == other.pledged() &&
            first.pledgeManager() == other.pledgeManager() &&
            first.photo() == other.photo() &&
            first.prelaunchActivated() == other.prelaunchActivated() &&
            first.projectNotice() == other.projectNotice() &&
            first.sendMetaCapiEvents() == other.sendMetaCapiEvents() &&
            first.sendThirdPartyEvents() == other.sendThirdPartyEvents() &&
            first.redemptionPageUrl() == other.redemptionPageUrl() &&
            first.rewards() == other.rewards() &&
            first.slug() == other.slug() &&
            first.staffPick() == other.staffPick() &&
            first.canComment() == other.canComment() &&
            first.pledgeOverTimeMinimumExplanation() == other.pledgeOverTimeMinimumExplanation() &&
            first.state() == other.state() &&
            first.stateChangedAt() == other.stateChangedAt() &&
            first.staticUsdRate() == other.staticUsdRate() &&
            first.usdExchangeRate() == other.usdExchangeRate() &&
            first.unreadMessagesCount() == other.unreadMessagesCount() &&
            first.unseenActivityCount() == other.unseenActivityCount() &&
            first.updatesCount() == other.updatesCount() &&
            first.updatedAt() == other.updatedAt() &&
            first.urls() == other.urls() &&
            first.video() == other.video() &&
            first.projectFaqs() == other.projectFaqs() &&
            first.envCommitments() == other.envCommitments() &&
            first.aiDisclosure() == other.aiDisclosure() &&
            first.risks() == other.risks() &&
            first.story() == other.story() &&
            first.isFlagged() == other.isFlagged() &&
            first.sharesCount() == other.sharesCount() &&
            first.pledgeOverTimeCollectionPlanShortPitch() == other.pledgeOverTimeCollectionPlanShortPitch() &&
            first.pledgeOverTimeCollectionPlanChargedAsNPayments() == other.pledgeOverTimeCollectionPlanChargedAsNPayments() &&
            first.pledgeOverTimeCollectionPlanChargeExplanation() == other.pledgeOverTimeCollectionPlanChargeExplanation()
    }
}
