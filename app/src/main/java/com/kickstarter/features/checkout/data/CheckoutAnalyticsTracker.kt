package com.kickstarter.features.checkout.data

import android.content.SharedPreferences
import android.util.Pair
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow

/**
 * Isolated analytics tracker for checkout impressions and CTA events.
 *
 * Negative Constraints:
 * - Do NOT mutate checkout or pledge state within analytics trackers.
 */
object CheckoutAnalyticsTracker {

    fun sendPageViewedEvent(
        analytics: AnalyticEvents,
        checkoutData: CheckoutData?,
        pledgeData: PledgeData?
    ) {
        if (checkoutData != null && pledgeData != null) {
            if (pledgeData.pledgeFlowContext() == PledgeFlowContext.NEW_PLEDGE) {
                analytics.trackCheckoutScreenViewed(checkoutData, pledgeData)
            } else {
                analytics.trackUpdatePledgePageViewed(checkoutData, pledgeData)
            }
        }
    }

    fun trackPledgeSubmitCTA(
        analytics: AnalyticEvents,
        checkoutData: CheckoutData?,
        pledgeData: PledgeData?
    ) {
        if (checkoutData != null && pledgeData != null) {
            analytics.trackPledgeSubmitCTA(checkoutData, pledgeData)
        }
    }

    fun sendThirdPartyAddPaymentInfoEvent(
        sharedPreferences: SharedPreferences,
        ffClient: FeatureFlagClientType,
        project: Project,
        currentUser: CurrentUserTypeV2,
        apolloClient: ApolloClientTypeV2,
        pledgeData: PledgeData?,
        shippingAmount: Double,
        checkoutData: CheckoutData?
    ): Flow<Pair<Boolean, String>> {
        return SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
            .sendThirdPartyEvent(
                project = Observable.just(project),
                currentUser = currentUser,
                apolloClient = apolloClient,
                draftPledge = Pair(pledgeData?.pledgeAmountTotal(), shippingAmount),
                checkoutAndPledgeData = Observable.just(Pair(checkoutData, pledgeData)),
                eventName = ThirdPartyEventValues.EventName.ADD_PAYMENT_INFO
            ).asFlow()
    }
}
