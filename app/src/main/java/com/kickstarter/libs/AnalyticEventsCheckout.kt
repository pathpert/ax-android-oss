package com.kickstarter.libs

import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_LOCATION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_TYPE
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ADD_ONS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.CHANGE_PAYMENT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.CHECKOUT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.MANAGE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT_ALERTS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.REWARDS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.THANKS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.UPDATE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.ADDRESS
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.CREDIT_CARD
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.ADD_ONS_CONTINUE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CONFIRM_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CONFIRM_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.EDIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.FINALIZE_PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.FIX_PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LATE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.MESSAGE_CREATOR_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.PLEDGE_CONFIRM
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.PLEDGE_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.REWARD_CONTINUE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SURVEY_RESPONSE_INITIATE
import com.kickstarter.libs.utils.EventName.CTA_CLICKED
import com.kickstarter.libs.utils.EventName.PAGE_VIEWED
import com.kickstarter.libs.utils.checkoutProperties
import com.kickstarter.models.Backing
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ProjectData

internal class AnalyticEventsCheckout(private val client: AnalyticEvents.ProxyClient) {

    fun trackPledgedProjectsOverviewPageViewed(ppoCards: List<PPOCard>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackPPOMessageCreatorCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int, creatorID: String) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["interaction_target_id"] = creatorID
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = MESSAGE_CREATOR_INITIATE.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackPPOFixPaymentCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = FIX_PLEDGE_INITIATE.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackPPOOpenSurveyCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int, surveyID: String) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props["survey_id"] = surveyID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = SURVEY_RESPONSE_INITIATE.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackPPOFinalizePledgeCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = FINALIZE_PLEDGE_INITIATE.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackPPOConfirmAddressInitiateCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = CONFIRM_INITIATE.contextName
        props[CONTEXT_TYPE.contextName] = ADDRESS.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackPPOConfirmAddressSubmitCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = CONFIRM_SUBMIT.contextName
        props[CONTEXT_TYPE.contextName] = ADDRESS.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackPPOConfirmAddressEditCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = EDIT.contextName
        props[CONTEXT_TYPE.contextName] = ADDRESS.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackCheckoutScreenViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to CHECKOUT.contextName)
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackUpdatePledgePageViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to UPDATE_PLEDGE.contextName)
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackPledgeConfirmCTA(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PLEDGE_CONFIRM.contextName)
        props[CONTEXT_TYPE.contextName] = CREDIT_CARD.contextName
        props[CONTEXT_PAGE.contextName] = CHECKOUT.contextName
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackPledgeSubmitCTA(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PLEDGE_SUBMIT.contextName)
        props[CONTEXT_TYPE.contextName] = CREDIT_CARD.contextName
        props[CONTEXT_PAGE.contextName] = CHECKOUT.contextName
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackLatePledgeSubmitCTA(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to LATE_PLEDGE.contextName)
        props[CONTEXT_TYPE.contextName] = CREDIT_CARD.contextName
        props[CONTEXT_PAGE.contextName] = CHECKOUT.contextName
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackManagePledgePageViewed(backing: Backing, projectData: ProjectData) {
        val checkoutData = checkoutProperties(
            amount = backing.amount(),
            checkoutId = null,
            bonus = backing.bonusAmount(),
            shippingAmount = backing.shippingAmount().toDouble()
        )

        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to MANAGE_PLEDGE.contextName)
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.checkoutProperties(checkoutData, projectData.project(), backing.addOns()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))

        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackPledgeInitiateCTA(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PLEDGE_INITIATE.contextName)
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackSelectRewardCTA(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to REWARD_CONTINUE.contextName)
        props[CONTEXT_PAGE.contextName] = REWARDS.contextName
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackThanksScreenViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to THANKS.contextName)
        props[CONTEXT_TYPE.contextName] = pledgeData.pledgeFlowContext().trackingString
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackAddOnsScreenViewed(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ADD_ONS.contextName)
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackAddOnsContinueCTA(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to ADD_ONS_CONTINUE.contextName)
        props[CONTEXT_PAGE.contextName] = ADD_ONS.contextName
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackRewardsCarouselViewed(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to REWARDS.contextName)
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackChangePaymentMethod(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to CHANGE_PAYMENT.contextName)
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    fun trackThanksActivityProjectCardClicked(projectData: ProjectData, checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PROJECT.contextName)
        props[CONTEXT_PAGE.contextName] = THANKS.contextName
        props[CONTEXT_LOCATION.contextName] = EventContextValues.LocationContextName.CURATED.contextName
        props[CONTEXT_TYPE.contextName] = EventContextValues.DiscoveryContextType.RECOMMENDED.contextName
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }
}
