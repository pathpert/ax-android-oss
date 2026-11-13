package com.kickstarter.services

import android.util.Pair
import com.apollographql.apollo3.ApolloClient
import com.google.gson.Gson
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
import com.kickstarter.services.apollo.ApolloBackingMutationClient
import com.kickstarter.services.apollo.ApolloBackingQueryClient
import com.kickstarter.services.apollo.ApolloCheckoutClient
import com.kickstarter.services.apollo.ApolloCommentClient
import com.kickstarter.services.apollo.ApolloCreatorSocialClient
import com.kickstarter.services.apollo.ApolloDiscoveryClient
import com.kickstarter.services.apollo.ApolloPaymentClient
import com.kickstarter.services.apollo.ApolloProjectClient
import com.kickstarter.services.apollo.ApolloRewardClient
import com.kickstarter.services.apollo.ApolloUserClient
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
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Apollo GraphQL Client facade delegating queries and mutations to domain-specific clients.
 *
 * Modernized for AX Component-Local Modularization (Pillar 1) and Strangler Fig (Rule 1).
 */
class KSApolloClientV2(val service: ApolloClient, val gson: Gson) : ApolloClientTypeV2 {
    private val disposables = CompositeDisposable()

    // Machine-tempo idempotency single-flight guards preserved for reflection compatibility
    private val createBackingGuard = AtomicBoolean(false)
    private val updateBackingGuard = AtomicBoolean(false)
    private val cancelBackingGuard = AtomicBoolean(false)
    private val createPaymentIntentGuard = AtomicBoolean(false)
    private val completeCheckoutGuard = AtomicBoolean(false)

    // Domain facades
    private val projectClient = ApolloProjectClient(service, disposables)
    private val rewardClient = ApolloRewardClient(service, disposables)
    private val paymentClient = ApolloPaymentClient(service, disposables, createPaymentIntentGuard)
    private val checkoutClient = ApolloCheckoutClient(service, disposables, completeCheckoutGuard)
    private val backingMutationClient = ApolloBackingMutationClient(
        service,
        disposables,
        createBackingGuard,
        updateBackingGuard,
        cancelBackingGuard
    )
    private val backingQueryClient = ApolloBackingQueryClient(service, disposables)
    private val userClient = ApolloUserClient(service, disposables)
    private val commentClient = ApolloCommentClient(service, disposables)
    private val discoveryClient = ApolloDiscoveryClient(service, disposables)
    private val creatorSocialClient = ApolloCreatorSocialClient(service, gson, disposables)

    override fun cleanDisposables() {
        disposables.clear()
    }

    override fun getProject(project: Project): Observable<Project> = projectClient.getProject(project)

    override fun getProject(slug: String): Observable<Project> = projectClient.getProject(slug)

    override fun getProjects(discoveryParams: DiscoveryParams, cursor: String?): Observable<DiscoverEnvelope> =
        discoveryClient.getProjects(discoveryParams, cursor)

    override fun createSetupIntent(project: Project?): Observable<String> = paymentClient.createSetupIntent(project)

    override fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard> =
        paymentClient.savePaymentMethod(savePaymentMethodData)

    override fun getStoredCards(): Observable<List<StoredCard>> = paymentClient.getStoredCards()

    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> =
        paymentClient.deletePaymentSource(paymentSourceId)

    override fun createFlagging(project: Project?, details: String, flaggingKind: String): Observable<String> =
        creatorSocialClient.createFlagging(project, details, flaggingKind)

    override fun flaggingOptions(contentType: FlaggingContent): Observable<List<FlaggingOption>> =
        creatorSocialClient.flaggingOptions(contentType)

    override fun userPrivacy(): Observable<UserPrivacy> = userClient.userPrivacy()

    override fun watchProject(project: Project): Observable<Project> = projectClient.watchProject(project)

    override fun unWatchProject(project: Project): Observable<Project> = projectClient.unWatchProject(project)

    override fun updateUserPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Observable<UpdateUserPasswordMutation.Data> =
        userClient.updateUserPassword(currentPassword, newPassword, confirmPassword)

    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> =
        userClient.updateUserEmail(email, currentPassword)

    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> =
        userClient.sendVerificationEmail()

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> =
        userClient.updateUserCurrencyPreference(currency)

    override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> =
        rewardClient.getShippingRules(reward)

    override fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>> =
        rewardClient.getProjectAddOns(slug, locationId)

    override suspend fun getRewardAllowedAddOns(
        locationId: Location,
        rewardId: Reward,
        cursor: String?
    ): Result<AddOnsEnvelope> = rewardClient.getRewardAllowedAddOns(locationId, rewardId, cursor)

    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> =
        backingMutationClient.updateBacking(updateBackingData)

    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> =
        backingMutationClient.createBacking(createBackingData)

    override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> =
        creatorSocialClient.triggerThirdPartyEvent(eventInput)

    override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> =
        userClient.createPassword(password, confirmPassword)

    override fun creatorDetails(slug: String): Observable<CreatorDetails> = creatorSocialClient.creatorDetails(slug)

    override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> =
        creatorSocialClient.sendMessage(project, recipient, body)

    override fun cancelBacking(backing: Backing, note: String): Observable<Any> =
        backingMutationClient.cancelBacking(backing, note)

    override fun fetchCategory(categoryParam: String): Observable<Category?> =
        discoveryClient.fetchCategory(categoryParam)

    override fun getBacking(backingId: String): Observable<Backing> = backingQueryClient.getBacking(backingId)

    override fun fetchCategories(): Observable<List<Category>> = discoveryClient.fetchCategories()

    override fun getProjectUpdates(slug: String, cursor: String, limit: Int): Observable<UpdatesGraphQlEnvelope> =
        creatorSocialClient.getProjectUpdates(slug, cursor, limit)

    override fun getComment(commentableId: String): Observable<Comment> = commentClient.getComment(commentableId)

    override fun getProjectUpdateComments(updateId: String, cursor: String, limit: Int): Observable<CommentEnvelope> =
        commentClient.getProjectUpdateComments(updateId, cursor, limit)

    override fun getProjectComments(slug: String, cursor: String, limit: Int): Observable<CommentEnvelope> =
        commentClient.getProjectComments(slug, cursor, limit)

    override fun getRepliesForComment(
        comment: Comment,
        cursor: String?,
        pageSize: Int
    ): Observable<CommentEnvelope> = commentClient.getRepliesForComment(comment, cursor, pageSize)

    override fun createComment(comment: PostCommentData): Observable<Comment> = commentClient.createComment(comment)

    override fun erroredBackings(): Observable<List<ErroredBacking>> = backingQueryClient.erroredBackings()

    override fun clearUnseenActivity(): Observable<Int> = userClient.clearUnseenActivity()

    override fun getProjectBacking(slug: String): Observable<Backing> = backingQueryClient.getProjectBacking(slug)

    override fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> =
        checkoutClient.createCheckout(createCheckoutData)

    override fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String> =
        paymentClient.createPaymentIntent(createPaymentIntentInput)

    override fun validateCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String
    ): Observable<PaymentValidationResponse> =
        checkoutClient.validateCheckout(checkoutId, paymentIntentClientSecret, paymentSourceId)

    override fun completeOnSessionCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String?,
        paymentSourceReusable: Boolean
    ): Observable<Pair<String, Boolean>> =
        checkoutClient.completeOnSessionCheckout(checkoutId, paymentIntentClientSecret, paymentSourceId, paymentSourceReusable)

    override fun createAttributionEvent(eventInput: CreateAttributionEventData): Observable<Boolean> =
        creatorSocialClient.createAttributionEvent(eventInput)

    override fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean> =
        backingQueryClient.createOrUpdateBackingAddress(eventInput)

    override fun completeOrder(orderInput: CompleteOrderInput): Observable<CompleteOrderPayload> =
        checkoutClient.completeOrder(orderInput)

    override fun getPledgedProjectsOverviewPledges(
        inputData: PledgedProjectsOverviewQueryData
    ): Observable<PledgedProjectsOverviewEnvelope> =
        backingQueryClient.getPledgedProjectsOverviewPledges(inputData)

    override suspend fun getRewardsFromProject(slug: String, sort: ProjectRewardsSort): Result<List<Reward>> =
        rewardClient.getRewardsFromProject(slug, sort)

    override fun buildPaymentPlan(input: BuildPaymentPlanData): Observable<PaymentPlan> =
        checkoutClient.buildPaymentPlan(input)

    override fun updateBackerCompleted(inputData: UpdateBackerCompletedData): Observable<Boolean> =
        backingQueryClient.updateBackerCompleted(inputData)

    override suspend fun addUserToSecretRewardGroup(project: Project, secretRewardToken: String): Result<Project> =
        projectClient.addUserToSecretRewardGroup(project, secretRewardToken)

    override suspend fun getSearchProjects(discoveryParams: DiscoveryParams, cursor: String?): Result<SearchEnvelope> =
        discoveryClient.getSearchProjects(discoveryParams, cursor)

    override suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>> =
        projectClient.fetchSimilarProjects(pid)

    override suspend fun getCategories(): Result<List<Category>> = discoveryClient.getCategories()

    override suspend fun getTags(): Result<List<Tag>> = discoveryClient.getTags()

    override suspend fun getLocations(
        useDefault: Boolean,
        term: String?,
        lat: Float?,
        long: Float?,
        radius: Float?,
        filterByCoordinates: Boolean?
    ): Result<List<Location>> =
        discoveryClient.getLocations(useDefault, term, lat, long, radius, filterByCoordinates)

    override suspend fun fetchProjectStory(slug: String): Result<StoriedProject> =
        projectClient.fetchProjectStory(slug)

    override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> =
        discoveryClient.getVideoFeed(first, cursor, categoryId)

    override suspend fun watchProjectSuspend(project: Project): Result<Project> =
        projectClient.watchProjectSuspend(project)

    override suspend fun unWatchProjectSuspend(project: Project): Result<Project> =
        projectClient.unWatchProjectSuspend(project)

    override suspend fun fetchShippingCountryLocations(
        forProject: Boolean,
        slug: String?
    ): Result<ShippingCountryLocationsWrapper> =
        rewardClient.fetchShippingCountryLocations(forProject, slug)

    sealed class KSApolloClientV2Exception(
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message, cause) {
        class Timeout(message: String? = null, cause: Throwable? = null) : KSApolloClientV2Exception(message, cause)
        class TooManyRequests(message: String? = null, cause: Throwable? = null) : KSApolloClientV2Exception(message, cause)
        class ApiError(message: String? = null, cause: Throwable? = null) : KSApolloClientV2Exception(message, cause)
        class InvalidProject(message: String? = null, cause: Throwable? = null) : KSApolloClientV2Exception(message, cause)
    }
}
