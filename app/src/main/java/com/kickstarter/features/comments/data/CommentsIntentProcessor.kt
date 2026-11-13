package com.kickstarter.features.comments.data

import android.content.Intent
import com.kickstarter.libs.Either
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable

/**
 * Pure parsing and extraction helpers for Comments Intent payloads.
 *
 * Negative Constraints:
 * - Do NOT pass untyped or mutated intents.
 */
object CommentsIntentProcessor {

    fun toIntentObservable(intent: Intent?): Observable<Intent> {
        return intent?.let { Observable.just(it) } ?: Observable.empty()
    }

    fun parseProjectOrUpdate(intent: Intent?): Observable<Either<Project?, Update?>> {
        return toIntentObservable(intent)
            .map {
                val projectData = it.getParcelableExtra(IntentKey.PROJECT_DATA) as? ProjectData
                val update = it.getParcelableExtra(IntentKey.UPDATE) as? Update
                projectData?.project()?.let { p ->
                    Either.Left<Project?, Update?>(p)
                } ?: Either.Right<Project?, Update?>(update)
            }
            .take(1)
    }

    fun extractDeepLinkId(intent: Intent?): Observable<String> {
        return toIntentObservable(intent)
            .filter { it.getStringExtra(IntentKey.COMMENT)?.isNotEmpty() ?: false }
            .map { requireNotNull(it.getStringExtra(IntentKey.COMMENT)) }
    }

    fun hasDeepLinkComment(intent: Intent?): Observable<Boolean> {
        return toIntentObservable(intent).map { it.hasExtra(IntentKey.COMMENT) }
    }
}
