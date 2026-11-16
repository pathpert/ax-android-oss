package com.kickstarter.services.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxFlowable
import com.apollographql.apollo3.rx2.rxSingle
import com.kickstarter.FetchProjectRewardsQuery
import com.kickstarter.FetchShippingCountryLocationsQuery
import com.kickstarter.GetProjectAddOnsQuery
import com.kickstarter.GetRewardAllowedAddOnsQuery
import com.kickstarter.GetShippingRulesForRewardIdQuery
import com.kickstarter.features.checkout.data.AddOnsEnvelope
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Location
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingCountryLocationsWrapper
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.interceptors.RateLimitBackoffInterceptor
import com.kickstarter.services.transformers.complexRewardItemsTransformer
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.services.transformers.rewardTransformer
import com.kickstarter.services.transformers.shippingRulesListTransformer
import com.kickstarter.type.ProjectRewardsSort
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Apollo GraphQL domain client for rewards, add-ons, and shipping rules.
 */
class ApolloRewardClient(
    private val service: ApolloClient,
    private val disposables: CompositeDisposable,
    val rateLimitInterceptor: RateLimitBackoffInterceptor = RateLimitBackoffInterceptor()
) {
    fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
        return Observable.defer {
            val ps = PublishSubject.create<ShippingRulesEnvelope>()
            val query = GetShippingRulesForRewardIdQuery(rewardId = encodeRelayId(reward))
            service.query(query)
                .rxFlowable()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let { data ->
                        val shippingEnvelope = data.node?.onReward?.shippingRulesExpanded?.nodes?.mapNotNull { node ->
                            node?.shippingRule
                        } ?: emptyList()
                        ps.onNext(shippingRulesListTransformer(shippingEnvelope))
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }.subscribeOn(Schedulers.io())
    }

    suspend fun getRewardsFromProject(
        slug: String,
        sort: ProjectRewardsSort = ProjectRewardsSort.ELIGIBILITY
    ): Result<List<Reward>> = executeForResult {
        val query = FetchProjectRewardsQuery(
            slug = slug,
            sort = Optional.present(sort)
        )
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.let { data ->
            val rwList: List<Reward?> = data.project?.rewards?.nodes?.map {
                it?.reward?.let { rwGr ->
                    rewardTransformer(
                        rewardGr = rwGr,
                        allowedAddons = it.allowedAddons.pageInfo.startCursor?.isNotEmpty() ?: false,
                        rewardItems = complexRewardItemsTransformer(it.items?.rewardItems),
                        simpleShippingRules = it.simpleShippingRulesExpanded.filterNotNull(),
                        rewardImage = it.rewardImage
                    )
                }
            } ?: emptyList()
            val minPledge = data.project?.minPledge?.toDouble() ?: 1.0
            val modifiedRewards = rwList.filterNotNull().toMutableList()
            modifiedRewards.add(0, RewardFactory.noReward().toBuilder().minimum(minPledge).build())
            modifiedRewards
        } ?: emptyList()
    }

    suspend fun getRewardAllowedAddOns(
        locationId: Location,
        rewardId: Reward,
        cursor: String? = null
    ): Result<AddOnsEnvelope> = executeForResult {
        val query = GetRewardAllowedAddOnsQuery(
            locationId = encodeRelayId(locationId),
            rewardId = encodeRelayId(rewardId),
            cursor = cursor?.let { Optional.present(it) } ?: Optional.absent()
        )
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.let {
            val allowedAddOns = it.node?.onReward?.allowedAddons
            val addOns = if (allowedAddOns != null) {
                getAddOnsFromAllowedAddOns(allowedAddOns)
            } else {
                emptyList()
            }
            val pageInfoEnvelope = allowedAddOns?.pageInfo?.pageInfo?.let { pageInfo ->
                createPageInfoObject(pageInfo)
            }
            AddOnsEnvelope(
                addOnsList = addOns,
                pageInfo = pageInfoEnvelope
            )
        } ?: AddOnsEnvelope()
    }

    fun getProjectAddOns(slug: String, locationId: Location): Observable<List<Reward>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<Reward>>()
            val query = GetProjectAddOnsQuery(
                slug = slug,
                locationId = encodeRelayId(locationId)
            )
            service.query(query)
                .rxSingle()
                .doOnError { throwable ->
                    ps.onError(throwable)
                }
                .subscribe { response ->
                    if (response.hasErrors()) {
                        ps.onError(Exception(response.errors?.first()?.message))
                    }
                    response.data?.let { data ->
                        val addOns = getAddOnsFromProject(requireNotNull(data.project?.addOns))
                        ps.onNext(addOns)
                    }
                    ps.onComplete()
                }.addToDisposable(disposables)
            return@defer ps
        }
    }

    suspend fun fetchShippingCountryLocations(
        forProject: Boolean,
        slug: String?
    ): Result<ShippingCountryLocationsWrapper> = executeForResult {
        val query = FetchShippingCountryLocationsQuery(
            forProject = forProject,
            slug = Optional.present(slug)
        )
        val response = service.query(query).execute()
        if (response.hasErrors()) {
            throw buildClientException(response.errors)
        }
        response.data?.let { responseData ->
            ShippingCountryLocationsWrapper(
                shippingCountryLocations = responseData.shippingCountryLocations.map {
                    Location.builder()
                        .id(decodeRelayId(it.id) ?: -1)
                        .displayableName(it.displayableName)
                        .name(it.name)
                        .country(it.country)
                        .build()
                },
                shippableCountriesForProject = responseData.project?.shippableCountriesExpanded?.map {
                    Location.builder()
                        .id(decodeRelayId(it.id) ?: -1)
                        .displayableName(it.displayableName)
                        .name(it.name)
                        .country(it.country)
                        .build()
                }
            )
        } ?: ShippingCountryLocationsWrapper()
    }

    private fun getAddOnsFromProject(addOnsGr: GetProjectAddOnsQuery.AddOns): List<Reward> {
        return addOnsGr.nodes?.map { node ->
            val shippingRulesGr = node?.shippingRulesExpanded?.nodes?.map { requireNotNull(it?.shippingRule) }
                ?: emptyList()
            rewardTransformer(
                requireNotNull(node?.reward),
                shippingRulesExpanded = shippingRulesGr,
                addOnItems = complexRewardItemsTransformer(node?.items?.rewardItems),
                rewardImage = node?.rewardImage
            )
        }?.toList() ?: emptyList()
    }

    private fun getAddOnsFromAllowedAddOns(addOnsGr: GetRewardAllowedAddOnsQuery.AllowedAddons): List<Reward> {
        return addOnsGr.edges?.mapNotNull { edge ->
            val node = edge?.node ?: return@mapNotNull null
            val shippingRulesGr = node.shippingRulesExpanded?.nodes
                ?.mapNotNull { it?.shippingRule } ?: emptyList()
            rewardTransformer(
                requireNotNull(node.reward),
                shippingRulesExpanded = shippingRulesGr,
                addOnItems = complexRewardItemsTransformer(node.items?.rewardItems),
                rewardImage = node.rewardImage
            )
        }?.toList() ?: emptyList()
    }
}
