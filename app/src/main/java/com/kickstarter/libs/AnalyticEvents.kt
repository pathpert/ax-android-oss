package com.kickstarter.libs

import OnboardingPage
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_SECTION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_TYPE
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ACTIVITY_FEED
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN_SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ONBOARDING
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.TWO_FACTOR_AUTH
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ACTIVITY_TRACKING_PROMPT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ENABLE_NOTIFICATIONS_PROMPT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.ALLOW_TRACKING
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CLOSE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.GET_NOTIFIED
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_OR_SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.NEXT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGNUP_LOGIN
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGN_UP_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGN_UP_SUBMIT
import com.kickstarter.libs.utils.EventName.CTA_CLICKED
import com.kickstarter.libs.utils.EventName.PAGE_VIEWED
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ProjectData

/**
 * Library utility component for AnalyticEvents.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing this utility.
 */
@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.*.data.*Tracker")
class AnalyticEvents(trackingClients: List<TrackingClientType?>) {

    internal val client = ProxyClient(trackingClients)
    private val discovery = AnalyticEventsDiscovery(client)
    private val checkout = AnalyticEventsCheckout(client)
    private val projectEvents = AnalyticEventsProject(client)

    // APPLICATION LIFECYCLE
    fun trackAppOpen() = client.track("App Open")

    fun trackActivityFeedPageViewed() {
        val props = hashMapOf<String, Any>(CONTEXT_PAGE.contextName to ACTIVITY_FEED.contextName)
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackDiscoverProjectCTAClicked() {
        val props = hashMapOf<String, Any>(
            CONTEXT_CTA.contextName to DISCOVER.contextName,
            CONTEXT_PAGE.contextName to ACTIVITY_FEED.contextName
        )
        client.track(CTA_CLICKED.eventName, props)
    }

    // AUTH & LOGIN
    fun trackLoginOrSignUpCtaClicked(type: String?, page: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to LOGIN_OR_SIGN_UP.contextName)
        props[CONTEXT_PAGE.contextName] = page
        type?.let { props[CONTEXT_TYPE.contextName] = it }
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackSignUpPageViewed() {
        client.track(PAGE_VIEWED.eventName, hashMapOf(CONTEXT_PAGE.contextName to SIGN_UP.contextName))
    }

    fun trackLoginOrSignUpPagedViewed() {
        client.track(PAGE_VIEWED.eventName, hashMapOf(CONTEXT_PAGE.contextName to LOGIN_SIGN_UP.contextName))
    }

    fun trackLoginPagedViewed() {
        client.track(PAGE_VIEWED.eventName, hashMapOf(CONTEXT_PAGE.contextName to LOGIN.contextName))
    }

    fun trackTwoFactorAuthPageViewed() {
        client.track(PAGE_VIEWED.eventName, hashMapOf(CONTEXT_PAGE.contextName to TWO_FACTOR_AUTH.contextName))
    }

    fun trackLogInButtonCtaClicked() {
        client.track(CTA_CLICKED.eventName, hashMapOf(CONTEXT_PAGE.contextName to LOGIN.contextName, CONTEXT_CTA.contextName to LOGIN_SUBMIT.contextName))
    }

    fun trackLogInInitiateCtaClicked() {
        client.track(CTA_CLICKED.eventName, hashMapOf(CONTEXT_CTA.contextName to LOGIN_INITIATE.contextName, CONTEXT_PAGE.contextName to LOGIN_SIGN_UP.contextName))
    }

    fun trackSignUpInitiateCtaClicked() {
        client.track(CTA_CLICKED.eventName, hashMapOf(CONTEXT_CTA.contextName to SIGN_UP_INITIATE.contextName, CONTEXT_PAGE.contextName to LOGIN_SIGN_UP.contextName))
    }

    fun trackSignUpSubmitCtaClicked() {
        client.track(CTA_CLICKED.eventName, hashMapOf(CONTEXT_CTA.contextName to SIGN_UP_SUBMIT.contextName, CONTEXT_PAGE.contextName to SIGN_UP.contextName))
    }

    // ONBOARDING
    fun trackOnboardingPageViewed(onboardingPage: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ONBOARDING.contextName, CONTEXT_SECTION.contextName to onboardingPage)
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackOnboardingCloseCTAClicked(onboardingPage: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ONBOARDING.contextName, CONTEXT_SECTION.contextName to onboardingPage, CONTEXT_CTA.contextName to CLOSE.contextName)
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackOnboardingNextCTAClicked(onboardingPage: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ONBOARDING.contextName, CONTEXT_SECTION.contextName to onboardingPage, CONTEXT_CTA.contextName to NEXT.contextName)
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackOnboardingGetNotifiedCTAClicked() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ONBOARDING.contextName, CONTEXT_SECTION.contextName to ENABLE_NOTIFICATIONS_PROMPT.contextName, CONTEXT_CTA.contextName to GET_NOTIFIED.contextName)
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackOnboardingAllowTrackingCTAClicked() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ONBOARDING.contextName, CONTEXT_SECTION.contextName to ACTIVITY_TRACKING_PROMPT.contextName, CONTEXT_CTA.contextName to ALLOW_TRACKING.contextName)
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackOnboardingAllowDenyPromptCTAClicked(prompt: String, allowOrDeny: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ONBOARDING.contextName, CONTEXT_SECTION.contextName to prompt, CONTEXT_CTA.contextName to allowOrDeny)
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackOnboardingSignupLoginCTAClicked() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ONBOARDING.contextName, CONTEXT_SECTION.contextName to OnboardingPage.SIGNUP_LOGIN, CONTEXT_CTA.contextName to SIGNUP_LOGIN.contextName)
        client.track(CTA_CLICKED.eventName, props)
    }

    // DISCOVERY & SEARCH DELEGATES
    fun trackDiscoverSortCTA(currentSort: DiscoveryParams.Sort, discoverParams: DiscoveryParams) =
        discovery.trackDiscoverSortCTA(currentSort, discoverParams)

    fun trackDiscoverSearchResultProjectCATClicked(discoveryParams: DiscoveryParams, projectData: ProjectData, count: Int, sort: DiscoveryParams.Sort) =
        discovery.trackDiscoverSearchResultProjectCATClicked(discoveryParams, projectData, count, sort)

    fun trackDiscoverProjectCtaClicked(discoveryParams: DiscoveryParams, projectData: ProjectData) =
        discovery.trackDiscoverProjectCtaClicked(discoveryParams, projectData)

    fun trackDiscoveryPageViewed(discoveryParams: DiscoveryParams) =
        discovery.trackDiscoveryPageViewed(discoveryParams)

    fun trackDiscoverFilterCTA(discoveryParams: DiscoveryParams) =
        discovery.trackDiscoverFilterCTA(discoveryParams)

    fun trackSearchCTAButtonClicked(discoveryParams: DiscoveryParams) =
        discovery.trackSearchCTAButtonClicked(discoveryParams)

    fun trackSearchResultPageViewed(discoveryParams: DiscoveryParams, count: Int, sort: DiscoveryParams.Sort) =
        discovery.trackSearchResultPageViewed(discoveryParams, count, sort)

    // CHECKOUT & PLEDGE & PPO DELEGATES
    fun trackPledgedProjectsOverviewPageViewed(ppoCards: List<PPOCard>, totalCount: Int) =
        checkout.trackPledgedProjectsOverviewPageViewed(ppoCards, totalCount)

    fun trackPPOMessageCreatorCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int, creatorID: String) =
        checkout.trackPPOMessageCreatorCTAClicked(projectID, ppoCards, totalCount, creatorID)

    fun trackPPOFixPaymentCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) =
        checkout.trackPPOFixPaymentCTAClicked(projectID, ppoCards, totalCount)

    fun trackPPOOpenSurveyCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int, surveyID: String) =
        checkout.trackPPOOpenSurveyCTAClicked(projectID, ppoCards, totalCount, surveyID)

    fun trackPPOFinalizePledgeCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) =
        checkout.trackPPOFinalizePledgeCTAClicked(projectID, ppoCards, totalCount)

    fun trackPPOConfirmAddressInitiateCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) =
        checkout.trackPPOConfirmAddressInitiateCTAClicked(projectID, ppoCards, totalCount)

    fun trackPPOConfirmAddressSubmitCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) =
        checkout.trackPPOConfirmAddressSubmitCTAClicked(projectID, ppoCards, totalCount)

    fun trackPPOConfirmAddressEditCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) =
        checkout.trackPPOConfirmAddressEditCTAClicked(projectID, ppoCards, totalCount)

    fun trackCheckoutScreenViewed(checkoutData: CheckoutData, pledgeData: PledgeData) =
        checkout.trackCheckoutScreenViewed(checkoutData, pledgeData)

    fun trackUpdatePledgePageViewed(checkoutData: CheckoutData, pledgeData: PledgeData) =
        checkout.trackUpdatePledgePageViewed(checkoutData, pledgeData)

    fun trackPledgeConfirmCTA(checkoutData: CheckoutData, pledgeData: PledgeData) =
        checkout.trackPledgeConfirmCTA(checkoutData, pledgeData)

    fun trackPledgeSubmitCTA(checkoutData: CheckoutData, pledgeData: PledgeData) =
        checkout.trackPledgeSubmitCTA(checkoutData, pledgeData)

    fun trackLatePledgeSubmitCTA(checkoutData: CheckoutData, pledgeData: PledgeData) =
        checkout.trackLatePledgeSubmitCTA(checkoutData, pledgeData)

    fun trackManagePledgePageViewed(backing: Backing, projectData: ProjectData) =
        checkout.trackManagePledgePageViewed(backing, projectData)

    fun trackPledgeInitiateCTA(projectData: ProjectData) =
        checkout.trackPledgeInitiateCTA(projectData)

    fun trackSelectRewardCTA(pledgeData: PledgeData) =
        checkout.trackSelectRewardCTA(pledgeData)

    fun trackThanksScreenViewed(checkoutData: CheckoutData, pledgeData: PledgeData) =
        checkout.trackThanksScreenViewed(checkoutData, pledgeData)

    fun trackAddOnsScreenViewed(pledgeData: PledgeData) =
        checkout.trackAddOnsScreenViewed(pledgeData)

    fun trackAddOnsContinueCTA(pledgeData: PledgeData) =
        checkout.trackAddOnsContinueCTA(pledgeData)

    fun trackRewardsCarouselViewed(projectData: ProjectData) =
        checkout.trackRewardsCarouselViewed(projectData)

    fun trackChangePaymentMethod(pledgeData: PledgeData) =
        checkout.trackChangePaymentMethod(pledgeData)

    fun trackThanksActivityProjectCardClicked(projectData: ProjectData, checkoutData: CheckoutData, pledgeData: PledgeData) =
        checkout.trackThanksActivityProjectCardClicked(projectData, checkoutData, pledgeData)

    // PROJECT & VIDEO & COMMENTS DELEGATES
    fun trackVideoStarted(project: Project, videoLength: Long, videoPosition: Long) =
        projectEvents.trackVideoStarted(project, videoLength, videoPosition)

    fun trackVideoCompleted(project: Project, videoLength: Long, videoPosition: Long) =
        projectEvents.trackVideoCompleted(project, videoLength, videoPosition)

    fun trackProjectScreenViewed(projectData: ProjectData, pageSectionContext: String) =
        projectEvents.trackProjectScreenViewed(projectData, pageSectionContext)

    fun trackProjectCardClicked(project: Project, pageContext: String) =
        projectEvents.trackProjectCardClicked(project, pageContext)

    fun trackCampaignDetailsCTAClicked(projectData: ProjectData) =
        projectEvents.trackCampaignDetailsCTAClicked(projectData)

    fun trackProjectPageTabChanged(projectData: ProjectData, pageSectionContext: String) =
        projectEvents.trackProjectPageTabChanged(projectData, pageSectionContext)

    fun trackWatchProjectCTA(project: Project, contextPageName: EventContextValues.ContextPageName) =
        projectEvents.trackWatchProjectCTA(project, contextPageName)

    fun trackRootCommentPageViewed(project: Project, projectUpdateId: String? = null) =
        projectEvents.trackRootCommentPageViewed(project, projectUpdateId)

    fun trackThreadCommentPageViewed(project: Project, rootCommentId: String, projectUpdateId: String? = null) =
        projectEvents.trackThreadCommentPageViewed(project, rootCommentId, projectUpdateId)

    fun trackCommentCTA(project: Project, commentId: String, comment: String, projectUpdateId: String? = null) =
        projectEvents.trackCommentCTA(project, commentId, comment, projectUpdateId)

    fun trackRootCommentReplyCTA(project: Project, commentId: String, commentReply: String, rootCommentId: String, projectUpdateId: String? = null) =
        projectEvents.trackRootCommentReplyCTA(project, commentId, commentReply, rootCommentId, projectUpdateId)

    fun trackVideoFeedImpression(item: VideoFeedItem, position: Int, entrySurface: String) =
        projectEvents.trackVideoFeedImpression(item, position, entrySurface)

    fun trackVideoFeedPageViewed(videoFeedItem: VideoFeedItem, toPosition: Int, fromVideoFeedItem: VideoFeedItem, watchTimeMs: Long?, videoDurationMs: Long?, entrySurface: String) =
        projectEvents.trackVideoFeedPageViewed(videoFeedItem, toPosition, fromVideoFeedItem, watchTimeMs, videoDurationMs, entrySurface)

    fun trackVideoFeedProgressBarTap(item: VideoFeedItem, percentageWatched: Float, watchTimeAtClick: Long? = null) =
        projectEvents.trackVideoFeedProgressBarTap(item, percentageWatched, watchTimeAtClick)

    fun trackSharePlatformCTAClicked(platform: SocialSharePlatform, contextPage: EventContextValues.ContextPageName) =
        projectEvents.trackSharePlatformCTAClicked(platform, contextPage)
    fun trackVideoFeedCTAClicked(project: Project, videoId: Long, ctaType: EventContextValues.CtaContextName, watchTimeAtClick: Long? = null) =
        projectEvents.trackVideoFeedCTAClicked(project, videoId, ctaType, watchTimeAtClick)
    fun reset() = client.reset()
    internal class ProxyClient(private val clients: List<TrackingClientType?>) {
        fun track(eventName: String) {
            clients.forEach { it?.track(eventName) }
        }
        fun track(eventName: String, additionalProperties: Map<String, Any>) {
            clients.forEach { it?.track(eventName, additionalProperties) }
        }
        fun reset() = clients.forEach { it?.reset() }
        fun loggedInUser(): User? = clients.firstOrNull()?.loggedInUser()
    }
}
