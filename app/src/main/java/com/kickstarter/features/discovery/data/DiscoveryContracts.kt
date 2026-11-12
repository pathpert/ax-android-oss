package com.kickstarter.features.discovery.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.type.ProjectSort
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.awaitFirstOrNull

/**
 * Immutable query contract for discovery pagination and filtering.
 *
 * Negative constraints:
 * - Do not pass untyped Map or loose String query dictionaries across the feature boundary.
 *
 * @property category Optional filtered category.
 * @property sort Sort order for discovery projects.
 * @property cursor Optional pagination cursor for subsequent pages.
 * @property isStaffPicks Whether to filter exclusively for staff picks.
 */
data class DiscoveryQuery(
    val category: Category? = null,
    val sort: ProjectSort = ProjectSort.MAGIC,
    val cursor: String? = null,
    val isStaffPicks: Boolean = false
) {
    /**
     * Converts to legacy [DiscoveryParams] for Apollo execution.
     */
    fun toDiscoveryParams(): DiscoveryParams {
        var builder = DiscoveryParams.builder()
            .sort(DiscoveryParams.Sort.MAGIC)
        category?.let { builder = builder.category(it) }
        if (isStaffPicks) {
            builder = builder.staffPicks(true)
        }
        return builder.build()
    }
}

/**
 * Immutable domain representation of a project card in Discovery.
 *
 * Negative constraints:
 * - Do not pass raw floating-point currency; currency is represented via [MinorCurrencyAmount].
 */
data class DiscoveryProjectCard(
    val id: NonBlankId,
    val name: String,
    val blurb: String,
    val photoUrl: String?,
    val pledgedAmount: MinorCurrencyAmount,
    val goalAmount: MinorCurrencyAmount,
    val percentFunded: Int,
    val creatorName: String
)

/**
 * Discovery pagination envelope.
 */
data class DiscoveryEnvelopeContract(
    val projects: List<DiscoveryProjectCard>,
    val endCursor: String? = null,
    val hasMorePages: Boolean = false
)

/**
 * Strictly-typed repository boundary for project discovery.
 */
interface DiscoveryRepository {
    suspend fun getProjects(query: DiscoveryQuery): DomainResult<DiscoveryEnvelopeContract>
    suspend fun getCategories(): DomainResult<List<Category>>
}

/**
 * Apollo-backed implementation of [DiscoveryRepository] mapping queries into [DomainResult].
 */
class ApolloDiscoveryRepository(
    private val apolloClient: ApolloClientTypeV2
) : DiscoveryRepository {

    override suspend fun getProjects(query: DiscoveryQuery): DomainResult<DiscoveryEnvelopeContract> {
        return try {
            val envelope = apolloClient.getProjects(
                discoveryParams = query.toDiscoveryParams(),
                cursor = query.cursor
            ).awaitFirstOrNull()

            if (envelope != null) {
                val mapped = (envelope.projects() ?: emptyList()).mapNotNull { proj ->
                    mapProject(proj)
                }
                val pageInfo = envelope.pageInfoEnvelope()
                DomainResult.Success(
                    DiscoveryEnvelopeContract(
                        projects = mapped,
                        endCursor = pageInfo?.endCursor,
                        hasMorePages = pageInfo?.hasNextPage == true
                    )
                )
            } else {
                DomainResult.Failure(
                    ProblemDetails(
                        type = ProblemDetails.TYPE_UNEXPECTED,
                        title = "Discovery Feed Unavailable",
                        status = 502,
                        detail = "Received empty response from discovery feed.",
                        suggestedAction = "Check your network connection and pull to refresh.",
                        isRetriable = true
                    )
                )
            }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            DomainResult.Failure(ProblemDetailsMapper.fromThrowable(e, "load discovery projects"))
        }
    }

    override suspend fun getCategories(): DomainResult<List<Category>> {
        return try {
            val categories = apolloClient.fetchCategories().awaitFirstOrNull() ?: emptyList()
            DomainResult.Success(categories)
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            DomainResult.Failure(ProblemDetailsMapper.fromThrowable(e, "fetch discovery categories"))
        }
    }

    private fun mapProject(project: Project): DiscoveryProjectCard? {
        val id = when (val res = NonBlankId.create(project.id().toString(), "project.id")) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> return null
        }
        val pledged = when (val res = MinorCurrencyAmount.fromMajorDouble(project.pledged())) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> (MinorCurrencyAmount.fromMinorUnits(0L) as DomainResult.Success).value
        }
        val goal = when (val res = MinorCurrencyAmount.fromMajorDouble(project.goal())) {
            is DomainResult.Success -> res.value
            is DomainResult.Failure -> (MinorCurrencyAmount.fromMinorUnits(0L) as DomainResult.Success).value
        }

        return DiscoveryProjectCard(
            id = id,
            name = project.name().orEmpty(),
            blurb = project.blurb().orEmpty(),
            photoUrl = project.photo()?.full(),
            pledgedAmount = pledged,
            goalAmount = goal,
            percentFunded = project.percentageFunded().toInt(),
            creatorName = project.creator()?.name().orEmpty()
        )
    }
}
