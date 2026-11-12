package com.kickstarter.features.discovery.viewmodel

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.types.MinorCurrencyAmount
import com.kickstarter.ax.types.NonBlankId
import com.kickstarter.features.discovery.data.DiscoveryEnvelopeContract
import com.kickstarter.features.discovery.data.DiscoveryProjectCard
import com.kickstarter.features.discovery.data.DiscoveryQuery
import com.kickstarter.features.discovery.data.DiscoveryRepository
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.models.Category
import com.kickstarter.type.ProjectSort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryFeatureViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val sampleCategory = CategoryFactory.artCategory()

    private val sampleProject1 = DiscoveryProjectCard(
        id = (NonBlankId.create("1") as DomainResult.Success).value,
        name = "Project 1",
        blurb = "Blurb 1",
        creatorName = "Creator 1",
        photoUrl = null,
        pledgedAmount = (MinorCurrencyAmount.fromMinorUnits(1000L) as DomainResult.Success).value,
        goalAmount = (MinorCurrencyAmount.fromMinorUnits(2000L) as DomainResult.Success).value,
        percentFunded = 50
    )

    private val sampleProject2 = DiscoveryProjectCard(
        id = (NonBlankId.create("2") as DomainResult.Success).value,
        name = "Project 2",
        blurb = "Blurb 2",
        creatorName = "Creator 2",
        photoUrl = null,
        pledgedAmount = (MinorCurrencyAmount.fromMinorUnits(3000L) as DomainResult.Success).value,
        goalAmount = (MinorCurrencyAmount.fromMinorUnits(3000L) as DomainResult.Success).value,
        percentFunded = 100
    )

    private class FakeDiscoveryRepository(
        var categoriesResult: DomainResult<List<Category>> = DomainResult.Success(emptyList()),
        var projectsResult: DomainResult<DiscoveryEnvelopeContract> = DomainResult.Success(
            DiscoveryEnvelopeContract(emptyList(), null, false)
        )
    ) : DiscoveryRepository {
        var lastQuery: DiscoveryQuery? = null

        override suspend fun getCategories(): DomainResult<List<Category>> = categoriesResult

        override suspend fun getProjects(query: DiscoveryQuery): DomainResult<DiscoveryEnvelopeContract> {
            lastQuery = query
            return projectsResult
        }
    }

    @Test
    fun `initial load fetches categories and initial projects successfully`() = runTest(testDispatcher) {
        val fakeRepo = FakeDiscoveryRepository(
            categoriesResult = DomainResult.Success(listOf(sampleCategory)),
            projectsResult = DomainResult.Success(
                DiscoveryEnvelopeContract(
                    projects = listOf(sampleProject1),
                    endCursor = "cursor-1",
                    hasMorePages = true
                )
            )
        )

        val vm = DiscoveryFeatureViewModel(fakeRepo, testDispatcher)

        val state = vm.uiState.value
        assertEquals(listOf(sampleCategory), state.categories)
        assertEquals(listOf(sampleProject1), state.projects)
        assertTrue(state.hasMorePages)
        assertFalse(state.isLoading)
        assertNull(state.problem)
    }

    @Test
    fun `loadNextPage appends projects to existing list`() = runTest(testDispatcher) {
        val fakeRepo = FakeDiscoveryRepository(
            projectsResult = DomainResult.Success(
                DiscoveryEnvelopeContract(
                    projects = listOf(sampleProject1),
                    endCursor = "cursor-1",
                    hasMorePages = true
                )
            )
        )

        val vm = DiscoveryFeatureViewModel(fakeRepo, testDispatcher)
        assertEquals(1, vm.uiState.value.projects.size)

        fakeRepo.projectsResult = DomainResult.Success(
            DiscoveryEnvelopeContract(
                projects = listOf(sampleProject2),
                endCursor = "cursor-2",
                hasMorePages = false
            )
        )

        vm.loadNextPage()

        val state = vm.uiState.value
        assertEquals(2, state.projects.size)
        assertEquals("Project 1", state.projects[0].name)
        assertEquals("Project 2", state.projects[1].name)
        assertFalse(state.hasMorePages)
        assertEquals("cursor-1", fakeRepo.lastQuery?.cursor)
    }

    @Test
    fun `categorySelected triggers query refresh with selected category`() = runTest(testDispatcher) {
        val fakeRepo = FakeDiscoveryRepository()
        val vm = DiscoveryFeatureViewModel(fakeRepo, testDispatcher)

        vm.categorySelected(sampleCategory)

        assertEquals(sampleCategory, vm.uiState.value.selectedCategory)
        assertEquals(sampleCategory, fakeRepo.lastQuery?.category)
        assertNull(fakeRepo.lastQuery?.cursor)
    }

    @Test
    fun `sortSelected triggers query refresh with new sort`() = runTest(testDispatcher) {
        val fakeRepo = FakeDiscoveryRepository()
        val vm = DiscoveryFeatureViewModel(fakeRepo, testDispatcher)

        vm.sortSelected(ProjectSort.NEWEST)

        assertEquals(ProjectSort.NEWEST, vm.uiState.value.selectedSort)
        assertEquals(ProjectSort.NEWEST, fakeRepo.lastQuery?.sort)
    }

    @Test
    fun `repository failure updates problem state and preserves previous projects`() = runTest(testDispatcher) {
        val fakeRepo = FakeDiscoveryRepository(
            projectsResult = DomainResult.Success(
                DiscoveryEnvelopeContract(
                    projects = listOf(sampleProject1),
                    endCursor = "cursor-1",
                    hasMorePages = true
                )
            )
        )
        val vm = DiscoveryFeatureViewModel(fakeRepo, testDispatcher)

        fakeRepo.projectsResult = DomainResult.Failure(
            ProblemDetails(
                type = ProblemDetails.TYPE_UNEXPECTED,
                title = "Network Error",
                detail = "Could not fetch next page",
                suggestedAction = "Check your connection and retry",
                isRetriable = true
            )
        )

        vm.loadNextPage()

        val state = vm.uiState.value
        assertEquals(listOf(sampleProject1), state.projects) // projects preserved
        assertEquals("Network Error", state.problem?.title)
        assertFalse(state.isLoading)

        vm.clearProblem()
        assertNull(vm.uiState.value.problem)
    }
}
