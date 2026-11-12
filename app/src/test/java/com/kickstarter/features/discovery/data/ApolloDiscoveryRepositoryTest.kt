package com.kickstarter.features.discovery.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Category
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApolloDiscoveryRepositoryTest {

    private val sampleCategory = CategoryFactory.artCategory()
    private val sampleProject = ProjectFactory.project().toBuilder()
        .id(1234L)
        .name("Great Project")
        .pledged(500.0)
        .goal(1000.0)
        .build()

    @Test
    fun `getProjects returns mapped projects and pagination metadata`() = runBlocking {
        val repo = ApolloDiscoveryRepository(
            object : MockApolloClientV2() {
                override fun getProjects(
                    discoveryParams: DiscoveryParams,
                    cursor: String?
                ): Observable<DiscoverEnvelope> {
                    val pageInfo = PageInfoEnvelope.builder()
                        .endCursor("next-cursor-101")
                        .hasNextPage(true)
                        .build()
                    val envelope = DiscoverEnvelope.builder()
                        .projects(listOf(sampleProject))
                        .pageInfoEnvelope(pageInfo)
                        .build()
                    return Observable.just(envelope)
                }
            }
        )

        val query = DiscoveryQuery(category = sampleCategory)
        val result = repo.getProjects(query)

        assertTrue(result is DomainResult.Success)
        val contract = (result as DomainResult.Success).value
        assertEquals(1, contract.projects.size)
        assertEquals("Great Project", contract.projects.first().name)
        assertEquals(50000L, contract.projects.first().pledgedAmount.minorUnits)
        assertEquals(100000L, contract.projects.first().goalAmount.minorUnits)
        assertEquals("next-cursor-101", contract.endCursor)
        assertTrue(contract.hasMorePages)
    }

    @Test
    fun `getCategories returns list of categories`() = runBlocking {
        val repo = ApolloDiscoveryRepository(
            object : MockApolloClientV2() {
                override fun fetchCategories(): Observable<List<Category>> {
                    return Observable.just(listOf(sampleCategory))
                }
            }
        )

        val result = repo.getCategories()

        assertTrue(result is DomainResult.Success)
        val categories = (result as DomainResult.Success).value
        assertEquals(1, categories.size)
        assertEquals(sampleCategory.id(), categories.first().id())
    }

    @Test
    fun `DiscoveryQuery converts to DiscoveryParams accurately`() {
        val query = DiscoveryQuery(
            category = sampleCategory,
            isStaffPicks = true
        )

        val params = query.toDiscoveryParams()
        assertEquals(sampleCategory, params.category())
        assertTrue(params.staffPicks() == true)
    }
}
