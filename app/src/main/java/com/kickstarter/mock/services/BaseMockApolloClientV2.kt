package com.kickstarter.mock.services

import android.util.Pair
import com.kickstarter.CreatePasswordMutation
import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.SendEmailVerificationMutation
import com.kickstarter.UpdateUserCurrencyMutation
import com.kickstarter.UpdateUserEmailMutation
import com.kickstarter.UpdateUserPasswordMutation
import com.kickstarter.features.checkout.data.AddOnsEnvelope
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Category
import com.kickstarter.models.Checkout
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.FlaggingOption
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.type.CurrencyCode
import com.kickstarter.type.FlaggingContent
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.reactivex.Observable
import java.util.Collections

/**
 * Network or service component for BaseMockApolloClientV2.
 *
 * Negative constraints:
 * - Do not perform blocking network calls on the main thread.
 */
abstract class BaseMockApolloClientV2 : ApolloClientTypeV2 {

    override suspend fun getRewardsFromProject(
        slug: String,
        sort: com.kickstarter.type.ProjectRewardsSort
    ): Result<List<Reward>> {
        return Result.success(emptyList())
    }

    override fun watchProject(project: Project): Observable<Project> {
        return Observable.just(project.toBuilder().isStarred(true).build())
    }

    override fun unWatchProject(project: Project): Observable<Project> {
        return Observable.just(project.toBuilder().isStarred(false).build())
    }

    override fun updateUserPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Observable<UpdateUserPasswordMutation.Data> {
        return Observable.just(
            UpdateUserPasswordMutation.Data(
                UpdateUserPasswordMutation.UpdateUserAccount(
                    UpdateUserPasswordMutation.User("some@email.com", true, true)
                )
            )
        )
    }

    override fun updateUserEmail(
        email: String,
        currentPassword: String
    ): Observable<UpdateUserEmailMutation.Data> {
        return Observable.just(
            UpdateUserEmailMutation.Data(
                UpdateUserEmailMutation.UpdateUserAccount(
                    UpdateUserEmailMutation.User("Some Name", "some@email.com")
                )
            )
        )
    }

    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.just(
            SendEmailVerificationMutation.Data(
                SendEmailVerificationMutation.UserSendEmailVerification("12345")
            )
        )
    }

    override fun getProject(project: Project): Observable<Project> {
        return Observable.just(ProjectFactory.backedProject())
    }

    override fun getProject(slug: String): Observable<Project> {
        return Observable.just(ProjectFactory.backedProject())
    }

    override fun getProjects(discoveryParams: DiscoveryParams, cursor: String?): Observable<DiscoverEnvelope> {
        return Observable.just(
            DiscoverEnvelope
                .builder()
                .projects(
                    listOf(
                        ProjectFactory.project(),
                        ProjectFactory.allTheWayProject(),
                        ProjectFactory.successfulProject()
                    )
                )
                .urls(
                    DiscoverEnvelope.UrlsEnvelope
                        .builder()
                        .api(
                            DiscoverEnvelope.UrlsEnvelope.ApiEnvelope
                                .builder()
                                .moreProjects("http://more.projects.please")
                                .build()
                        )
                        .build()
                )
                .stats(
                    DiscoverEnvelope.StatsEnvelope
                        .builder()
                        .count(10)
                        .build()
                )
                .build()
        )
    }

    override fun createSetupIntent(project: Project?): Observable<String> {
        return Observable.just("")
    }

    override fun getStoredCards(): Observable<List<StoredCard>> {
        return Observable.just(Collections.singletonList(StoredCardFactory.discoverCard()))
    }

    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.just(DeletePaymentSourceMutation.Data(DeletePaymentSourceMutation.PaymentSourceDelete("")))
    }

    override fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard> {
        return Observable.just(StoredCardFactory.discoverCard())
    }

    override fun createFlagging(project: Project?, details: String, flaggingKind: String): Observable<String> {
        return Observable.empty<String>()
    }

    override fun flaggingOptions(contentType: FlaggingContent): Observable<List<FlaggingOption>> {
        return Observable.just(
            listOf(
                FlaggingOption(
                    id = "project/our_rules",
                    parentId = "project",
                    kind = null,
                    isGroup = true,
                    title = "This project breaks one of Our Rules",
                    subtitle = "All projects on Kickstarter must create something to share with others.",
                    placeholder = null
                ),
                FlaggingOption(
                    id = "project/our_rules/resale",
                    parentId = "project/our_rules",
                    kind = null,
                    isGroup = true,
                    title = "Copying, reselling or plagiarism",
                    subtitle = "Projects cannot plagiarize, offer items that aren't produced by the creator.",
                    placeholder = null
                ),
                FlaggingOption(
                    id = "project/our_rules/resale/reselling",
                    parentId = "project/our_rules/resale",
                    kind = "RESELLING",
                    isGroup = false,
                    title = "This project is reselling or repackaging an existing product.",
                    subtitle = null,
                    placeholder = "Please provide a URL(s) showing the reward currently available for purchase elsewhere."
                ),
                FlaggingOption(
                    id = "project/community_guidelines",
                    parentId = "project",
                    kind = null,
                    isGroup = true,
                    title = "Report spam or abusive behavior",
                    subtitle = "Our Community Guidelines prohibit spam and abusive behavior.",
                    placeholder = null
                ),
                FlaggingOption(
                    id = "project/community_guidelines/guidelines_spam",
                    parentId = "project/community_guidelines",
                    kind = "GUIDELINES_SPAM",
                    isGroup = false,
                    title = "Spam",
                    subtitle = "Ex: Using email lists from outside sources, link bombing.",
                    placeholder = "Please provide additional context about this content or behavior."
                )
            )
        )
    }

    override fun userPrivacy(): Observable<UserPrivacy> {
        return Observable.just(
            UserPrivacy(
                "Some Name",
                "some@email.com",
                true,
                true,
                true,
                true,
                "USD"
            )
        )
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.empty()
    }

    override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
        return Observable.empty()
    }

    override fun getProjectAddOns(
        slug: String,
        locationId: Location
    ): Observable<List<Reward>> {
        return Observable.empty()
    }

    override suspend fun getRewardAllowedAddOns(locationId: Location, rewardId: Reward, cursor: String?): Result<AddOnsEnvelope> {
        return Result.success(AddOnsEnvelope())
    }

    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
        return Observable.empty()
    }

    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> {
        return Observable.empty()
    }

    override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
        return Observable.empty()
    }

    override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
        return Observable.empty()
    }

    override fun creatorDetails(slug: String): Observable<CreatorDetails> {
        return Observable.empty()
    }

    override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
        return Observable.just(1L)
    }

    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
        return Observable.just(true)
    }

    override fun fetchCategory(param: String): Observable<Category?> {
        return Observable.just(CategoryFactory.musicCategory())
    }

    override fun getBacking(backingId: String): Observable<Backing> {
        return Observable.just(BackingFactory.backing())
    }

    override fun fetchCategories(): Observable<List<Category>> {
        return Observable.just(CategoryFactory.rootCategories())
    }
}
