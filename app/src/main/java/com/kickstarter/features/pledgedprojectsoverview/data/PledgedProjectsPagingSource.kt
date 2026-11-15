package com.kickstarter.features.pledgedprojectsoverview.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx2.awaitFirstOrNull

internal const val PPO_PAGE_LIMIT = 10

/**
 * PagingSource for Pledged Projects Overview cards.
 *
 * Extracted from `PledgedProjectsOverviewViewModel` to keep file size concise and map
 * paging failures into structured [ProblemException] instances.
 *
 * Negative constraints:
 * - Do not swallow exceptions; map through [ProblemDetailsMapper].
 * - Do not instantiate directly in UI layers; provide via [PledgedProjectsRepository].
 */
class PledgedProjectsPagingSource(
    private val apolloClient: ApolloClientTypeV2,
    private val analyticEvents: AnalyticEvents,
    private val limit: Int = PPO_PAGE_LIMIT,
    private val tierTypes: List<PledgeTierType>
) : PagingSource<String, PPOCard>() {

    override fun getRefreshKey(state: PagingState<String, PPOCard>): String {
        return "" // Default first page is empty string when paginating with GraphQL
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, PPOCard> {
        return try {
            val inputData = PledgedProjectsOverviewQueryData(
                first = limit,
                after = params.key ?: "",
                tierTypes = tierTypes
            )
            val envelope = apolloClient.getPledgedProjectsOverviewPledges(inputData = inputData)
                .awaitFirstOrNull()
                ?: return LoadResult.Error(
                    ProblemException(
                        ProblemDetailsMapper.fromDomainMessage(
                            rawMessage = "Server returned an empty pledged projects response.",
                            operation = "load pledged projects overview",
                            status = 502
                        )
                    )
                )

            val cards = envelope.pledges() ?: emptyList()
            analyticEvents.trackPledgedProjectsOverviewPageViewed(cards, envelope.totalCount() ?: 0)
            val nextPageEnvelope: PageInfoEnvelope? =
                if (envelope.pageInfoEnvelope?.hasNextPage == true) envelope.pageInfoEnvelope else null

            LoadResult.Page(
                data = cards,
                prevKey = null,
                nextKey = nextPageEnvelope?.endCursor
            )
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            LoadResult.Error(
                ProblemException(
                    problem = ProblemDetailsMapper.fromThrowable(
                        throwable = throwable,
                        operation = "load pledged projects overview"
                    ),
                    cause = throwable
                )
            )
        }
    }
}
