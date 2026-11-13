package com.kickstarter.services.apollo

import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.exception.ApolloHttpException
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.fragment.PageInfo
import com.kickstarter.models.Project
import com.kickstarter.services.KSApolloClientV2.KSApolloClientV2Exception
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import io.reactivex.Observable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.asObservable
import timber.log.Timber
import java.net.SocketTimeoutException

/**
 * Shared exceptions and execution utilities for Apollo GraphQL domain clients.
 */
fun buildClientException(errors: List<Error>?): KSApolloClientV2Exception {
    return KSApolloClientV2Exception.ApiError(errors?.first()?.message)
}

fun invalidProjectException(
    project: Project,
    context: String = "Attempted watch/unwatch mutation"
): KSApolloClientV2Exception {
    val exception = KSApolloClientV2Exception.InvalidProject(
        "$context: Invalid project id: ${project.id()}"
    )
    runCatching {
        FirebaseCrashlytics.getInstance().recordException(exception)
    }.onFailure { e ->
        Timber.e(e, "Error recording exception in Firebase")
    }
    return exception
}

fun ApolloException.toClientException(): Exception =
    when (this) {
        is ApolloNetworkException -> {
            when (cause) {
                is SocketTimeoutException -> KSApolloClientV2Exception.Timeout()
                else -> this
            }
        }
        is ApolloHttpException -> {
            when (statusCode) {
                429 -> KSApolloClientV2Exception.TooManyRequests()
                else -> this
            }
        }
        else -> this
    }

suspend fun <T> executeForResult(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (apolloException: ApolloException) {
        val exception = apolloException.toClientException()
        FirebaseCrashlytics.getInstance().recordException(exception)
        Result.failure(exception)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        Result.failure(e)
    }

fun <T : Any> emitAsObservable(block: suspend () -> T): Observable<T> =
    flow { emit(block()) }.asObservable()

fun createPageInfoObject(pageFr: PageInfo?): PageInfoEnvelope {
    return PageInfoEnvelope.builder()
        .endCursor(pageFr?.endCursor ?: "")
        .hasNextPage(pageFr?.hasNextPage ?: false)
        .hasPreviousPage(pageFr?.hasPreviousPage ?: false)
        .startCursor(pageFr?.startCursor ?: "")
        .build()
}
