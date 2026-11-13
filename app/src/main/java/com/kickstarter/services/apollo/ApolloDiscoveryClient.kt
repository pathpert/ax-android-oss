package com.kickstarter.services.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxFlowable
import com.apollographql.apollo3.rx2.rxSingle
import com.kickstarter.FetchCategoryQuery
import com.kickstarter.FetchProjectsQuery
import com.kickstarter.GetCreativePromptTagsQuery
import com.kickstarter.GetLocationsQuery
import com.kickstarter.GetRootCategoriesQuery
import com.kickstarter.VideoFeedQuery
import com.kickstarter.features.search.data.SearchEnvelope
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.toBoolean
import com.kickstarter.libs.utils.extensions.toGoalBucket
import com.kickstarter.libs.utils.extensions.toPledgedBucket
import com.kickstarter.libs.utils.extensions.toProjectSort
import com.kickstarter.libs.utils.extensions.toProjectState
import com.kickstarter.libs.utils.extensions.toRaisedBucket
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Tag
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.transformers.categoryTransformer
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.extensions.toVideoFeedEnvelope
import com.kickstarter.services.transformers.projectTransformer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Apollo GraphQL domain client for project discovery, search, tags, categories, and video feeds.
 */
class ApolloDiscoveryClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable
) {
    fun getProjects(
        discoveryParams: DiscoveryParams,
        cursor: String?
    ): Observable<DiscoverEnvelope> {
        val ps = PublishSubject.create<DiscoverEnvelope>()
        service.query(query = buildFetchProjectsQuery(discoveryParams, cursor))
            .rxFlowable()
            .subscribeOn(Schedulers.io())
            .doOnError {
                ps.onError(it)
            }
            .subscribe { response ->
                if (response.hasErrors()) {
                    ps.onError(java.lang.Exception(response.errors?.first()?.message))
                } else {
                    response.data?.let { responseData ->
                        val projects = responseData.projects?.edges?.map {
                            projectTransformer(it?.node?.projectCard)
                        }
                        val pageInfoEnvelope = responseData.projects?.pageInfo?.pageInfo?.let {
                            createPageInfoObject(it)
                        }
                        val discoverEnvelope = DiscoverEnvelope.builder()
                            .projects(projects)
                            .pageInfoEnvelope(pageInfoEnvelope)
                            .build()
                        ps.onNext(discoverEnvelope)
                    }
                }
                ps.onComplete()
            }.addToDisposable(disposables)
        return ps
    }

    suspend fun getSearchProjects(
        discoveryParams: DiscoveryParams,
        cursor: String? = null
    ): Result<SearchEnvelope> = executeForResult {
        val query = buildFetchProjectsQuery(discoveryParams, cursor)
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.let { responseData ->
            val projects = responseData.projects?.edges?.map {
                projectTransformer(it?.node?.projectCard)
            } ?: emptyList()
            val pageInfoEnvelope = responseData.projects?.pageInfo?.pageInfo?.let {
                createPageInfoObject(it)
            }
            val totalCount = responseData.projects?.totalCount ?: 0
            SearchEnvelope(projects, pageInfoEnvelope, totalCount)
        } ?: SearchEnvelope()
    }

    fun fetchCategory(categoryParam: String): Observable<Category?> {
        return Observable.defer {
            val ps = PublishSubject.create<Category>()
            val query = FetchCategoryQuery(categoryParam = categoryParam)
            service.query(query)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        val category = categoryTransformer(response.data?.category?.category)
                        ps.onNext(category)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    fun fetchCategories(): Observable<List<Category>> {
        return Observable.defer {
            val query = GetRootCategoriesQuery()
            val ps = PublishSubject.create<List<Category>>()
            service.query(query)
                .rxFlowable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response: ApolloResponse<GetRootCategoriesQuery.Data> ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    } else {
                        response.data?.let { responseData ->
                            val subCategories = responseData.rootCategories
                                .flatMap { it.subcategories?.nodes.orEmpty() }
                                .map { categoryTransformer(it?.category) }
                            val rootCategories = responseData.rootCategories
                                .map { categoryTransformer(it.category) }
                                .toMutableList()
                                .apply { addAll(subCategories) }
                            ps.onNext(rootCategories)
                        }
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    suspend fun getCategories(): Result<List<Category>> = executeForResult {
        val query = GetRootCategoriesQuery()
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.let { responseData ->
            val categories = mutableListOf<Category>()
            responseData.rootCategories.forEach { rootCategory ->
                categories.add(categoryTransformer(rootCategory.category))
                rootCategory.subcategories?.nodes?.map { subcategory ->
                    categories.add(categoryTransformer(subcategory?.category))
                }
            }
            categories
        } ?: emptyList()
    }

    suspend fun getTags(): Result<List<Tag>> = executeForResult {
        val response = service.query(GetCreativePromptTagsQuery()).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.tags?.nodes?.mapNotNull { node ->
            node?.let {
                Tag(
                    id = decodeRelayId(it.id) ?: return@mapNotNull null,
                    name = it.name,
                    url = it.url,
                    slug = it.slug
                )
            }
        } ?: emptyList()
    }

    suspend fun getLocations(
        useDefault: Boolean,
        term: String?,
        lat: Float? = null,
        long: Float? = null,
        radius: Float? = null,
        filterByCoordinates: Boolean? = null
    ): Result<List<Location>> = executeForResult {
        val query = GetLocationsQuery(
            useSessionLocation = if (useDefault.isTrue()) Optional.present(useDefault) else Optional.absent(),
            term = if (term.isNullOrEmpty()) Optional.absent() else Optional.present(term),
            filterByCoordinates = if (filterByCoordinates == null) Optional.absent() else Optional.present(filterByCoordinates),
            first = if (useDefault) Optional.Present(5) else Optional.present(10),
            radius = if (radius == null) Optional.absent() else Optional.present(radius.toDouble()),
            long = if (long == null) Optional.absent() else Optional.present(long.toDouble()),
            lat = if (lat == null) Optional.absent() else Optional.present(lat.toDouble()),
            discoverable = if (useDefault.isTrue()) Optional.present(true) else Optional.absent()
        )
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.let { responseData ->
            responseData.locations?.nodes?.map {
                Location.builder()
                    .name(it?.location?.name)
                    .displayableName(it?.location?.displayableName)
                    .id(decodeRelayId(it?.location?.id))
                    .country(it?.location?.country)
                    .build()
            }
        } ?: emptyList()
    }

    suspend fun getVideoFeed(
        first: Int,
        cursor: String? = null,
        categoryId: String? = null
    ): Result<VideoFeedEnvelope> = executeForResult {
        val query = VideoFeedQuery(
            first = first,
            after = Optional.presentIfNotNull(cursor),
            categoryId = Optional.presentIfNotNull(categoryId)
        )
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.videoFeed.toVideoFeedEnvelope()
    }

    private fun buildFetchProjectsQuery(
        discoveryParams: DiscoveryParams,
        cursor: String?
    ): FetchProjectsQuery {
        return FetchProjectsQuery(
            sort = Optional.present(discoveryParams.sort()?.toProjectSort()),
            cursor = cursor?.let { if (it.isNotEmpty()) Optional.present(it) else Optional.absent() } ?: Optional.absent(),
            categoryId = if (discoveryParams.category()?.id() == null) Optional.absent() else Optional.present(discoveryParams.category()?.id().toString()),
            recommended = if (discoveryParams.recommended() == null) Optional.absent() else Optional.present(discoveryParams.recommended()),
            starred = if (discoveryParams.starred() == null) Optional.absent() else Optional.present(discoveryParams.starred().toBoolean()),
            backed = if (discoveryParams.staffPicks() == null) Optional.absent() else Optional.present(discoveryParams.backed().toBoolean()),
            searchTerm = if (discoveryParams.term() == null || discoveryParams.term().isNullOrBlank()) Optional.absent() else Optional.present(discoveryParams.term()),
            state = if (discoveryParams.state() == null) Optional.absent() else Optional.present(discoveryParams.state()?.toProjectState()),
            raised = if (discoveryParams.raisedBucket() == null) Optional.absent() else Optional.present(discoveryParams.raisedBucket()?.toRaisedBucket()),
            location = if (discoveryParams.location() == null) Optional.absent() else Optional.present(discoveryParams.location()?.let { encodeRelayId(it) }),
            amountRaisedBucket = if (discoveryParams.amountBucket() == null) Optional.absent() else Optional.present(discoveryParams.amountBucket()?.toPledgedBucket()),
            goalBucket = if (discoveryParams.goalBucket() == null) Optional.absent() else Optional.present(discoveryParams.goalBucket()?.toGoalBucket()),
            following = if (discoveryParams.social() == null) Optional.absent() else Optional.present(discoveryParams.social().toBoolean()),
            staffPicks = if (discoveryParams.staffPicks() == null) Optional.absent() else Optional.present(discoveryParams.staffPicks()),
            tagId = discoveryParams.tagId().toOptional()
        )
    }

    private fun <T : Any> T?.toOptional(): Optional<T> =
        if (this == null) Optional.absent() else Optional.present(this)
}
