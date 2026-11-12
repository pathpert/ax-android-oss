package com.kickstarter.services

import android.util.Pair
import com.kickstarter.CreatePasswordMutation
import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.SendEmailVerificationMutation
import com.kickstarter.UpdateUserCurrencyMutation
import com.kickstarter.UpdateUserEmailMutation
import com.kickstarter.UpdateUserPasswordMutation
import com.kickstarter.features.checkout.data.AddOnsEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.features.search.data.SearchEnvelope
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.models.Backing
import com.kickstarter.models.BuildPaymentPlanData
import com.kickstarter.models.Category
import com.kickstarter.models.Checkout
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.Comment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.models.CompleteOrderPayload
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.FlaggingOption
import com.kickstarter.models.Location
import com.kickstarter.models.PaymentPlan
import com.kickstarter.models.PaymentValidationResponse
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingCountryLocationsWrapper
import com.kickstarter.models.StoredCard
import com.kickstarter.models.Tag
import com.kickstarter.models.User
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackerCompletedData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.type.CurrencyCode
import com.kickstarter.type.FlaggingContent
import com.kickstarter.type.ProjectRewardsSort
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.reactivex.Observable

const val APOLLO_PAGE_SIZE = 25
const val APOLLO_REPLIES_PAGE_SIZE = 7

/**
 * Apollo GraphQL Client contract interface.
 *
 * Decoupled from [KSApolloClientV2] to satisfy AX Component-Local Modularization (Pillar 1).
 */
interface ApolloClientTypeV2 {
    fun getProject(project: Project): Observable<Project>
    fun getProject(slug: String): Observable<Project>
    fun getProjects(discoveryParams: DiscoveryParams, cursor: String?): Observable<DiscoverEnvelope>
    fun createSetupIntent(project: Project? = null): Observable<String>
    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard>
    fun getStoredCards(): Observable<List<StoredCard>>
    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>
    fun createFlagging(
        project: Project? = null,
        details: String,
        flaggingKind: String
    ): Observable<String>

    /** Fetches the flagging options (groups + kinds) for the given [contentType] as a flat list. */
    fun flaggingOptions(contentType: FlaggingContent = FlaggingContent.PROJECT): Observable<List<FlaggingOption>>

    fun userPrivacy(): Observable<UserPrivacy>
    fun watchProject(project: Project): Observable<Project>
    fun unWatchProject(project: Project): Observable<Project>
    fun updateUserPassword(
        currentPassword: String = "",
        newPassword: String,
        confirmPassword: String
    ): Observable<UpdateUserPasswordMutation.Data>

    fun updateUserEmail(
        email: String,
        currentPassword: String
    ): Observable<UpdateUserEmailMutation.Data>

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>
    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>
    fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope>
    fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>>
    suspend fun getRewardAllowedAddOns(locationId: Location, rewardId: Reward, cursor: String? = null): Result<AddOnsEnvelope>
    fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout>
    fun createBacking(createBackingData: CreateBackingData): Observable<Checkout>
    fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>>
    fun createPassword(
        password: String,
        confirmPassword: String
    ): Observable<CreatePasswordMutation.Data>

    fun creatorDetails(slug: String): Observable<CreatorDetails>
    fun sendMessage(project: Project, recipient: User, body: String): Observable<Long>
    fun cancelBacking(backing: Backing, note: String): Observable<Any>
    fun fetchCategory(categoryParam: String): Observable<Category?>
    fun getBacking(backingId: String): Observable<Backing>
    fun fetchCategories(): Observable<List<Category>>

    fun getProjectUpdates(
        slug: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<UpdatesGraphQlEnvelope>

    fun getComment(commentableId: String): Observable<Comment>
    fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun getProjectComments(
        slug: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun getRepliesForComment(
        comment: Comment,
        cursor: String? = null,
        pageSize: Int = APOLLO_REPLIES_PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun createComment(comment: PostCommentData): Observable<Comment>
    fun erroredBackings(): Observable<List<ErroredBacking>>

    fun clearUnseenActivity(): Observable<Int>

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
    suspend fun getSearchProjects(discoveryParams: DiscoveryParams, cursor: String? = null): Result<SearchEnvelope>
    suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getTags(): Result<List<Tag>>
    suspend fun getLocations(useDefault: Boolean, term: String?, lat: Float? = null, long: Float? = null, radius: Float? = null, filterByCoordinates: Boolean? = null): Result<List<Location>>

    suspend fun fetchProjectStory(slug: String): Result<StoriedProject>

    suspend fun getVideoFeed(first: Int, cursor: String? = null, categoryId: String? = null): Result<VideoFeedEnvelope>

    suspend fun watchProjectSuspend(project: Project): Result<Project>
    suspend fun unWatchProjectSuspend(project: Project): Result<Project>

    suspend fun fetchShippingCountryLocations(forProject: Boolean, slug: String?): Result<ShippingCountryLocationsWrapper>

    fun cleanDisposables()
}
