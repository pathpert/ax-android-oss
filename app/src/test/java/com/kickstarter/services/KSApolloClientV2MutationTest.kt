package com.kickstarter.services

import com.apollographql.apollo3.ApolloClient
import com.google.gson.Gson
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import io.reactivex.observers.TestObserver
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class KSApolloClientV2MutationTest : KSRobolectricTestCase() {

    private fun createClient(): KSApolloClientV2 {
        val apollo = ApolloClient.Builder()
            .serverUrl("http://localhost:8080/graphql")
            .build()
        return KSApolloClientV2(apollo, Gson())
    }

    @Test
    fun `cancelBacking throws ProblemException with TYPE_IN_FLIGHT when mutation already active`() {
        val client = createClient()

        val guardField = KSApolloClientV2::class.java.getDeclaredField("cancelBackingGuard")
        guardField.isAccessible = true
        val guard = guardField.get(client) as AtomicBoolean
        guard.set(true)

        val testObserver = TestObserver<Any>()
        val backing = BackingFactory.backing()

        client.cancelBacking(backing, "cancelled by test").subscribe(testObserver)

        testObserver.assertError { throwable ->
            throwable is ProblemException &&
                throwable.problem.type == ProblemDetails.TYPE_IN_FLIGHT &&
                throwable.problem.status == 409 &&
                !throwable.problem.isRetriable
        }
    }

    @Test
    fun `createBacking throws ProblemException with TYPE_IN_FLIGHT when mutation already active`() {
        val client = createClient()

        val guardField = KSApolloClientV2::class.java.getDeclaredField("createBackingGuard")
        guardField.isAccessible = true
        val guard = guardField.get(client) as AtomicBoolean
        guard.set(true)

        val testObserver = TestObserver<Any>()
        val mockData = CreateBackingData(
            project = ProjectFactory.project(),
            amount = "10.00",
            locationId = null,
            refTag = null
        )

        client.createBacking(mockData).subscribe(testObserver)

        testObserver.assertError { throwable ->
            throwable is ProblemException &&
                throwable.problem.type == ProblemDetails.TYPE_IN_FLIGHT &&
                throwable.problem.status == 409 &&
                !throwable.problem.isRetriable
        }
    }

    @Test
    fun `updateBacking throws ProblemException with TYPE_IN_FLIGHT when mutation already active`() {
        val client = createClient()

        val guardField = KSApolloClientV2::class.java.getDeclaredField("updateBackingGuard")
        guardField.isAccessible = true
        val guard = guardField.get(client) as AtomicBoolean
        guard.set(true)

        val testObserver = TestObserver<Any>()
        val mockData = UpdateBackingData(
            backing = BackingFactory.backing(),
            amount = "25.00"
        )

        client.updateBacking(mockData).subscribe(testObserver)

        testObserver.assertError { throwable ->
            throwable is ProblemException &&
                throwable.problem.type == ProblemDetails.TYPE_IN_FLIGHT &&
                throwable.problem.status == 409 &&
                !throwable.problem.isRetriable
        }
    }
}
