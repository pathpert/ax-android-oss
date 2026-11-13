package com.kickstarter.features.projectpage.data

import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectPageRepositoryTest : KSRobolectricTestCase() {

    private lateinit var repository: ProjectPageRepository
    private lateinit var apolloClient: MockApolloClientV2
    private val disposables = CompositeDisposable()
    private val scope = TestScope()

    @Before
    override fun setUp() {
        super.setUp()
        apolloClient = object : MockApolloClientV2() {
            override fun watchProject(project: Project): Observable<Project> {
                return Observable.just(project.toBuilder().isStarred(true).build())
            }

            override fun unWatchProject(project: Project): Observable<Project> {
                return Observable.just(project.toBuilder().isStarred(false).build())
            }
        }
        val env = environment().toBuilder().apolloClientV2(apolloClient).build()
        repository = ProjectPageRepository(env, scope, disposables)
    }

    @After
    override fun tearDown() {
        disposables.clear()
        super.tearDown()
    }

    @Test
    fun testToggleProjectSave_whenUnstarred_starsProject() {
        val unstarredProject = ProjectFactory.project().toBuilder().isStarred(false).build()
        val observer = repository.toggleProjectSave(unstarredProject).test()

        observer.assertValueCount(1)
        assertTrue(observer.values().first().isStarred())
    }

    @Test
    fun testToggleProjectSave_whenStarred_unstarsProject() {
        val starredProject = ProjectFactory.project().toBuilder().isStarred(true).build()
        val observer = repository.toggleProjectSave(starredProject).test()

        observer.assertValueCount(1)
        assertFalse(observer.values().first().isStarred())
    }

    @Test
    fun testBuildProjectData() {
        val project = ProjectFactory.project()
        val refTagFromIntent: KsOptional<RefTag?> = KsOptional.of(RefTag.discovery())
        val refTagFromCookie: KsOptional<RefTag?> = KsOptional.of(RefTag.recommended())
        val deeplink: KsOptional<Uri?> = KsOptional.of(Uri.parse("https://kickstarter.com/projects/123"))

        val observer = repository.buildProjectData(
            Observable.just(refTagFromIntent),
            Observable.just(refTagFromCookie),
            Observable.just(deeplink),
            Observable.just(project)
        ).test()

        observer.assertValueCount(1)
        val projectData = observer.values().first()
        assertEquals(RefTag.discovery(), projectData.refTagFromIntent())
        assertEquals(RefTag.recommended(), projectData.refTagFromCookie())
        assertEquals(project, projectData.project())
    }
}
