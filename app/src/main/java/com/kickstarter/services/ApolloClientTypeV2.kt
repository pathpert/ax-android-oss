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
 * Decoupled from [KSApolloClientV2] to satisfy AX Component-Local Modularization (Pillar 1)
 * and Poka-Yoke / Contract-First Typing (Pillar 2 & 3).
 *
 * ### Negative Constraints:
 * - Do not pass unvalidated or blank IDs, slugs, or tokens across this boundary.
 * - Do not swallow mutation or query errors; all responses are typed or wrapped in [Result].
 */
interface ApolloClientTypeV2 {

    /**
     * Fetches detailed project metadata using an existing [Project] domain reference.
     *
     * ### Negative Constraints:
     * - Do not call with a project whose slug or ID is empty.
     */
    fun getProject(project: Project): Observable<Project>

    /**
     * Fetches project metadata by URL slug (e.g., `"creator-name/project-title"`).
     *
     * ### Negative Constraints:
     * - Do not pass a blank or malformed slug.
     */
    fun getProject(slug: String): Observable<Project>

    /**
     * Queries projects matching the specified discovery filters and pagination cursor.
     *
     * ### Negative Constraints:
     * - Do not pass negative page offsets or invalid cursor tokens.
     */
    fun getProjects(discoveryParams: DiscoveryParams, cursor: String?): Observable<DiscoverEnvelope>

    /**
     * Initiates a Stripe SetupIntent for adding payment methods.
     *
     * @param project Optional associated project for pledge authorization scoping.
     * @return Observable emitting the Stripe client secret string.
     */
    fun createSetupIntent(project: Project? = null): Observable<String>

    /**
     * Persists a validated payment method card token on the user's account.
     *
     * ### Negative Constraints:
     * - Do not invoke with empty or expired payment method tokens.
     */
    fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard>

    /**
     * Retrieves all stored credit cards and payment methods for the authenticated user.
     *
     * ### Negative Constraints:
     * - Do not call for unauthenticated sessions.
     */
    fun getStoredCards(): Observable<List<StoredCard>>

    /**
     * Deletes a stored payment source from the user's account.
     *
     * @param paymentSourceId Unique identifier of the payment source to remove.
     */
    fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data>

    /**
     * Submits a user report/flagging for inappropriate or policy-violating content.
     */
    fun createFlagging(
        project: Project? = null,
        details: String,
        flaggingKind: String
    ): Observable<String>

    /**
     * Fetches the flagging options (groups + kinds) for the given [contentType] as a flat list.
     */
    fun flaggingOptions(contentType: FlaggingContent = FlaggingContent.PROJECT): Observable<List<FlaggingOption>>

    /**
     * Retrieves the current authenticated user's privacy and newsletter preference settings.
     */
    fun userPrivacy(): Observable<UserPrivacy>

    /**
     * Bookmarks / stars a project for the authenticated user.
     *
     * ### Negative Constraints:
     * - Do not call if user is not logged in.
     */
    fun watchProject(project: Project): Observable<Project>

    /**
     * Removes bookmark / unstars a project for the authenticated user.
     */
    fun unWatchProject(project: Project): Observable<Project>

    /**
     * Updates account password with verification of old and new passwords.
     *
     * ### Negative Constraints:
     * - Do not pass mismatched [newPassword] and [confirmPassword].
     */
    fun updateUserPassword(
        currentPassword: String = "",
        newPassword: String,
        confirmPassword: String
    ): Observable<UpdateUserPasswordMutation.Data>

    /**
     * Updates account email address with password verification.
     */
    fun updateUserEmail(
        email: String,
        currentPassword: String
    ): Observable<UpdateUserEmailMutation.Data>

    /**
     * Triggers a verification email dispatch to the unverified user email address.
     */
    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>

    /**
     * Updates the user's preferred display currency preference across all surfaces.
     */
    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    /**
     * Fetches destination shipping rules and pricing tiers for the specified [reward].
     */
    fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope>

    /**
     * Fetches eligible project add-on rewards for the given project slug and shipping location.
     */
    fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>>

    /**
     * Suspended query fetching allowed add-on items for a specific reward and location.
     */
    suspend fun getRewardAllowedAddOns(locationId: Location, rewardId: Reward, cursor: String? = null): Result<AddOnsEnvelope>

    /**
     * Mutates an existing backing pledge (e.g. changing amount, reward, or payment method).
     */
    fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout>

    /**
     * Creates a new backing pledge for a project.
     *
     * ### Negative Constraints:
     * - Do not call without an idempotency key guard.
     */
    fun createBacking(createBackingData: CreateBackingData): Observable<Checkout>

    /**
     * Dispatches attribution or conversion events to external third-party SDKs.
     */
    fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>>

    /**
     * Sets an initial account password for users who registered via OAuth/SSO.
     */
    fun createPassword(
        password: String,
        confirmPassword: String
    ): Observable<CreatePasswordMutation.Data>

    /**
     * Retrieves creator biography, badges, and verified identity details for a project.
     */
    fun creatorDetails(slug: String): Observable<CreatorDetails>

    /**
     * Sends a direct private message from the user to the project creator.
     */
    fun sendMessage(project: Project, recipient: User, body: String): Observable<Long>

    /**
     * Cancels an active backing pledge with a creator feedback note.
     */
    fun cancelBacking(backing: Backing, note: String): Observable<Any>

    /**
     * Queries taxonomy metadata for a specific category parameter identifier.
     */
    fun fetchCategory(categoryParam: String): Observable<Category?>

    /**
     * Retrieves detailed backing data by persistent database backing ID.
     */
    fun getBacking(backingId: String): Observable<Backing>

    /**
     * Fetches all primary and secondary Kickstarter project taxonomy categories.
     */
    fun fetchCategories(): Observable<List<Category>>

    /**
     * Queries paginated creator updates for a project.
     */
    fun getProjectUpdates(
        slug: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<UpdatesGraphQlEnvelope>

    /**
     * Retrieves a single comment by commentable entity ID.
     */
    fun getComment(commentableId: String): Observable<Comment>

    /**
     * Queries comments on a specific project update post.
     */
    fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<CommentEnvelope>

    /**
     * Queries root project comments by project slug.
     */
    fun getProjectComments(
        slug: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<CommentEnvelope>

    /**
     * Queries threaded replies to a parent [comment].
     */
    fun getRepliesForComment(
        comment: Comment,
        cursor: String? = null,
        pageSize: Int = APOLLO_REPLIES_PAGE_SIZE
    ): Observable<CommentEnvelope>

    /**
     * Posts a new root comment or reply to an update or project.
     */
    fun createComment(comment: PostCommentData): Observable<Comment>

    /**
     * Retrieves a list of user backings with uncollected or errored payment states.
     */
    fun erroredBackings(): Observable<List<ErroredBacking>>

    /**
     * Clears unseen notification and activity counts on the user's profile badge.
     */
    fun clearUnseenActivity(): Observable<Int>

    /**
     * Queries the authenticated user's backing status on a specific project by slug.
     */
    fun getProjectBacking(slug: String): Observable<Backing>

    /**
     * Initiates checkout session for pledge processing.
     */
    fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment>

    /**
     * Generates a Stripe PaymentIntent client secret for checkout validation.
     */
    fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String>

    /**
     * Validates payment authorization with payment provider.
     */
    fun validateCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String
    ): Observable<PaymentValidationResponse>

    /**
     * Completes an on-session payment checkout transaction.
     */
    fun completeOnSessionCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String?,
        paymentSourceReusable: Boolean
    ): Observable<Pair<String, Boolean>>

    /**
     * Dispatches attribution events for ad network tracking.
     */
    fun createAttributionEvent(eventInput: CreateAttributionEventData): Observable<Boolean>

    /**
     * Submits or updates delivery shipping address for a physical reward backing.
     */
    fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean>

    /**
     * Completes post-campaign pledge redemption order.
     */
    fun completeOrder(orderInput: CompleteOrderInput): Observable<CompleteOrderPayload>

    /**
     * Queries user pledged projects overview list and activity summaries.
     */
    fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope>

    /**
     * Suspended query fetching sorted reward tiers for a project.
     */
    suspend fun getRewardsFromProject(
        slug: String,
        sort: ProjectRewardsSort = ProjectRewardsSort.ELIGIBILITY
    ): Result<List<Reward>>

    /**
     * Calculates installment payment schedule (Pledge Over Time) for checkout.
     */
    fun buildPaymentPlan(input: BuildPaymentPlanData): Observable<PaymentPlan>

    /**
     * Marks backer survey or fulfillment form as completed.
     */
    fun updateBackerCompleted(inputData: UpdateBackerCompletedData): Observable<Boolean>

    /**
     * Grants access to secret or private reward tiers via token invitation.
     */
    suspend fun addUserToSecretRewardGroup(project: Project, secretRewardToken: String): Result<Project>

    /**
     * Executes full-text search and discovery query returning projects.
     */
    suspend fun getSearchProjects(discoveryParams: DiscoveryParams, cursor: String? = null): Result<SearchEnvelope>

    /**
     * Queries recommended projects similar to the specified project ID.
     */
    suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>>

    /**
     * Suspended query retrieving all project taxonomy categories.
     */
    suspend fun getCategories(): Result<List<Category>>

    /**
     * Suspended query retrieving popular project tags.
     */
    suspend fun getTags(): Result<List<Tag>>

    /**
     * Queries geographic locations by search query or geographic coordinates.
     */
    suspend fun getLocations(useDefault: Boolean, term: String?, lat: Float? = null, long: Float? = null, radius: Float? = null, filterByCoordinates: Boolean? = null): Result<List<Location>>

    /**
     * Suspended query fetching rich-text project story components.
     */
    suspend fun fetchProjectStory(slug: String): Result<StoriedProject>

    /**
     * Queries vertical full-screen video feed items.
     */
    suspend fun getVideoFeed(first: Int, cursor: String? = null, categoryId: String? = null): Result<VideoFeedEnvelope>

    /**
     * Suspended coroutines equivalent of [watchProject].
     */
    suspend fun watchProjectSuspend(project: Project): Result<Project>

    /**
     * Suspended coroutines equivalent of [unWatchProject].
     */
    suspend fun unWatchProjectSuspend(project: Project): Result<Project>

    /**
     * Queries supported shipping country locations for campaign delivery.
     */
    suspend fun fetchShippingCountryLocations(forProject: Boolean, slug: String?): Result<ShippingCountryLocationsWrapper>

    /**
     * Cleans up all pending RxJava composite disposables.
     */
    fun cleanDisposables()
}
