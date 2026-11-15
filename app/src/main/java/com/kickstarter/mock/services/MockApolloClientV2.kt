package com.kickstarter.mock.services

import android.util.Pair
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.features.projectstory.data.RichTextComponent
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.features.search.data.SearchEnvelope
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ErroredBackingFactory
import com.kickstarter.mock.factories.PageInfoEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.BuildPaymentPlanData
import com.kickstarter.models.Category
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.Comment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.models.CompleteOrderPayload
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Location
import com.kickstarter.models.PaymentPlan
import com.kickstarter.models.PaymentValidationResponse
import com.kickstarter.models.Project
import com.kickstarter.models.ShippingCountryLocationsWrapper
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.services.mutations.UpdateBackerCompletedData
import io.reactivex.Observable
import java.util.Collections

open class MockApolloClientV2 : BaseMockApolloClientV2() {

    override fun getProjectUpdates(
        slug: String,
        cursor: String,
        limit: Int
    ): Observable<UpdatesGraphQlEnvelope> {
        return Observable.just(
            UpdatesGraphQlEnvelope.builder()
                .pageInfoEnvelope(PageInfoEnvelopeFactory.pageInfoEnvelope())
                .updates(listOf(UpdateFactory.update()))
                .totalCount(1)
                .build()
        )
    }

    override fun getComment(commentableId: String): Observable<Comment> {
        return Observable.just(CommentFactory.comment())
    }

    override fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int
    ): Observable<CommentEnvelope> {
        return Observable.just(
            CommentEnvelope.builder()
                .pageInfoEnvelope(PageInfoEnvelopeFactory.pageInfoEnvelope())
                .comments(listOf(CommentFactory.comment()))
                .commentableId(updateId)
                .totalCount(1)
                .build()
        )
    }

    override fun getProjectComments(slug: String, cursor: String, limit: Int): Observable<CommentEnvelope> {
        return Observable.just(
            CommentEnvelope.builder()
                .pageInfoEnvelope(PageInfoEnvelopeFactory.pageInfoEnvelope())
                .comments(listOf(CommentFactory.comment()))
                .totalCount(1)
                .build()
        )
    }

    override fun getRepliesForComment(
        comment: Comment,
        cursor: String?,
        pageSize: Int
    ): Observable<CommentEnvelope> {
        return Observable.empty()
    }

    override fun createComment(comment: PostCommentData): Observable<Comment> {
        return Observable.just(CommentFactory.comment())
    }

    override fun erroredBackings(): Observable<List<ErroredBacking>> {
        return Observable.just(Collections.singletonList(ErroredBackingFactory.erroredBacking()))
    }

    override fun clearUnseenActivity(): Observable<Int> {
        return Observable.just(0)
    }

    override fun getProjectBacking(slug: String): Observable<Backing> {
        return Observable.just(BackingFactory.backing())
    }

    override fun createCheckout(createCheckoutData: CreateCheckoutData): Observable<CheckoutPayment> {
        return Observable.empty()
    }

    override fun createAttributionEvent(eventInput: CreateAttributionEventData): Observable<Boolean> {
        return Observable.empty()
    }

    override fun validateCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String
    ): Observable<PaymentValidationResponse> {
        return Observable.empty()
    }

    override fun completeOnSessionCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String?,
        paymentSourceReusable: Boolean
    ): Observable<Pair<String, Boolean>> {
        return Observable.empty()
    }

    override fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): Observable<String> {
        return Observable.empty()
    }

    override fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): Observable<Boolean> {
        return Observable.empty()
    }

    override fun completeOrder(orderInput: CompleteOrderInput): Observable<CompleteOrderPayload> {
        return Observable.empty()
    }

    override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): Observable<PledgedProjectsOverviewEnvelope> {
        return Observable.empty()
    }

    override fun buildPaymentPlan(input: BuildPaymentPlanData): Observable<PaymentPlan> {
        return Observable.empty()
    }

    override fun updateBackerCompleted(inputData: UpdateBackerCompletedData): Observable<Boolean> {
        return Observable.empty()
    }

    override suspend fun addUserToSecretRewardGroup(
        project: Project,
        secretRewardToken: String,
    ): Result<Project> {
        return Result.success(project)
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return Result.success(emptyList())
    }

    override suspend fun getTags(): Result<List<Tag>> {
        return Result.success(emptyList())
    }

    override suspend fun getLocations(useDefault: Boolean, term: String?, lat: Float?, long: Float?, radius: Float?, filterByCoordinates: Boolean?): Result<List<Location>> {
        return Result.success(emptyList())
    }

    override suspend fun fetchProjectStory(slug: String): Result<StoriedProject> {
        return Result.success(StoriedProject(ProjectFactory.project(), RichTextComponent(emptyList())))
    }

    override suspend fun getSearchProjects(discoveryParams: DiscoveryParams, cursor: String?): Result<SearchEnvelope> {
        return Result.success(SearchEnvelope())
    }

    override suspend fun getVideoFeed(first: Int, cursor: String?, categoryId: String?): Result<VideoFeedEnvelope> {
        return Result.success(VideoFeedEnvelope())
    }

    override suspend fun watchProjectSuspend(project: Project): Result<Project> {
        return Result.success(project.toBuilder().isStarred(true).build())
    }

    override suspend fun unWatchProjectSuspend(project: Project): Result<Project> {
        return Result.success(project.toBuilder().isStarred(false).build())
    }

    override suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>> {
        return Result.success(listOf())
    }

    override suspend fun fetchShippingCountryLocations(forProject: Boolean, slug: String?): Result<ShippingCountryLocationsWrapper> {
        return Result.success(ShippingCountryLocationsWrapper())
    }

    override fun cleanDisposables() {
        TODO("Not yet implemented")
    }
}
