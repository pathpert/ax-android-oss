package com.kickstarter.services

import android.util.Pair
import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.features.checkout.data.AddOnsEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.models.Backing
import com.kickstarter.models.BuildPaymentPlanData
import com.kickstarter.models.Checkout
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.models.CompleteOrderPayload
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Location
import com.kickstarter.models.PaymentPlan
import com.kickstarter.models.PaymentValidationResponse
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingCountryLocationsWrapper
import com.kickstarter.models.StoredCard
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackerCompletedData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.type.ProjectRewardsSort
import io.reactivex.Observable

/**
 * Apollo GraphQL operations for Checkout, Pledging, Payment Methods, and Backings.
 */
interface ApolloCheckoutClientType {

    fun createSetupIntent(project: Project? = null): Observable<String>
    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard>
    fun getStoredCards(): Observable<List<StoredCard>>
    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>
    fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope>
    fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>>
    suspend fun getRewardAllowedAddOns(locationId: Location, rewardId: Reward, cursor: String? = null): Result<AddOnsEnvelope>
    fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout>
    fun createBacking(createBackingData: CreateBackingData): Observable<Checkout>
    fun cancelBacking(backing: Backing, note: String): Observable<Any>
    fun getBacking(backingId: String): Observable<Backing>
    fun erroredBackings(): Observable<List<ErroredBacking>>
    fun getProjectBacking(slug: String): Observable<Backing>
    fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment>
    fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String>

    fun validateCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String
    ): Observable<PaymentValidationResponse>

    fun completeOnSessionCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String?,
        paymentSourceReusable: Boolean
    ): Observable<Pair<String, Boolean>>

    fun createAttributionEvent(eventInput: CreateAttributionEventData): Observable<Boolean>
    fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean>
    fun completeOrder(orderInput: CompleteOrderInput): Observable<CompleteOrderPayload>
    fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope>

    suspend fun getRewardsFromProject(
        slug: String,
        sort: ProjectRewardsSort = ProjectRewardsSort.ELIGIBILITY
    ): Result<List<Reward>>

    fun buildPaymentPlan(input: BuildPaymentPlanData): Observable<PaymentPlan>
    fun updateBackerCompleted(inputData: UpdateBackerCompletedData): Observable<Boolean>
    suspend fun addUserToSecretRewardGroup(project: Project, secretRewardToken: String): Result<Project>
    suspend fun fetchShippingCountryLocations(forProject: Boolean, slug: String?): Result<ShippingCountryLocationsWrapper>
}
