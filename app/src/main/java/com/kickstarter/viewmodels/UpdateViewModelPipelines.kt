package com.kickstarter.viewmodels

import android.net.Uri
import android.os.Build
import android.util.Pair
import androidx.core.net.toUri
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.UrlUtils.appendRefTag
import com.kickstarter.libs.utils.UrlUtils.refTag
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isProjectPreviewUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.models.Update
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import okhttp3.Request

/**
 * Rx stream bindings and pipeline configuration for [UpdateViewModel.UpdateViewModel].
 */
internal fun UpdateViewModel.UpdateViewModel.initUpdatePipelines(disposables: CompositeDisposable) {
    val initialUpdate = intent
        .filter {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra(IntentKey.UPDATE, Update::class.java) != null
            } else {
                it.getParcelableExtra(IntentKey.UPDATE) as? Update? != null
            }
        }
        .map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra(IntentKey.UPDATE, Update::class.java)
            } else {
                it.getParcelableExtra(IntentKey.UPDATE) as? Update?
            }
        }

    intent
        .filter { it.hasExtra(IntentKey.IS_UPDATE_COMMENT) && !it.hasExtra(IntentKey.COMMENT) }
        .map {
            it.getBooleanExtra(IntentKey.IS_UPDATE_COMMENT, false)
        }
        .subscribe { this.deepLinkToRootComment.onNext(it) }
        .addToDisposable(disposables)

    intent
        .filter {
            it.hasExtra(IntentKey.COMMENT) && !it.getStringExtra(IntentKey.COMMENT).isNullOrEmpty()
        }
        .map {
            Pair(
                requireNotNull(it.getStringExtra(IntentKey.COMMENT)),
                it.getBooleanExtra(IntentKey.IS_UPDATE_COMMENT, false)
            )
        }
        .subscribe {
            this.deepLinkToThreadActivity.onNext(it)
        }
        .addToDisposable(disposables)

    val project = intent
        .filter { it != null }
        .flatMap { intent ->
            ProjectIntentMapper
                .project(intent, client)
                .compose(Transformers.neverErrorV2())
        }
        .share()

    val initialUpdateUrl = initialUpdate
        ?.map {
            it.urls()?.web()?.update() ?: ""
        }

    val deepLinkUpdate = intent
        .filter {
            it.hasExtra(IntentKey.UPDATE_POST_ID) && !it.getStringExtra(IntentKey.UPDATE_POST_ID).isNullOrEmpty()
        }
        .map { requireNotNull(it.getStringExtra(IntentKey.UPDATE_POST_ID)) }
        .compose(Transformers.combineLatestPair(project))
        .map {
            Pair(requireNotNull(it.second.slug()), requireNotNull(it.first))
        }
        .switchMap {
            client.fetchUpdate(it.first, it.second).compose(Transformers.neverErrorV2())
        }
        .share()

    val anotherUpdateUrl = goToUpdateRequest
        .map { it.url.toString() }

    val deepLinkUrl = deepLinkUpdate
        .map { it.urls()?.web()?.update() ?: "" }

    Observable.merge(initialUpdateUrl, anotherUpdateUrl, deepLinkUrl)
        .distinctUntilChanged()
        .filter { it.isNotEmpty() }
        .subscribe {
            it?.let { url ->
                webViewUrl.onNext(url)
            }
        }
        .addToDisposable(disposables)

    val anotherUpdate = goToUpdateRequest
        .map { parseProjectUpdateParams(it) }
        .switchMap {
            client.fetchUpdate(it.first, it.second).compose(Transformers.neverErrorV2())
        }
        .share()

    val currentUpdate = Observable.merge(initialUpdate, anotherUpdate, deepLinkUpdate)
        .filter {
            it.isNotNull()
        }

    currentUpdate
        .compose(Transformers.takeWhenV2(shareButtonClicked))
        .map {
            it.let { update ->
                Pair.create(
                    update,
                    update.urls()?.web()?.let { web ->
                        appendRefTag(web.update(), RefTag.updateShare().tag())
                    }
                )
            }
        }
        .subscribe {
            it?.let { pair ->
                startShareIntent.onNext(pair)
            }
        }
        .addToDisposable(disposables)

    goToCommentsActivity
        .withLatestFrom(currentUpdate) { _, update -> update }
        .distinctUntilChanged()
        .subscribe {
            startRootCommentsActivity.onNext(it)
            deepLinkToRootComment.onNext(false)
        }
        .addToDisposable(disposables)

    goToCommentsActivityToDeepLinkThreadActivity
        .withLatestFrom(currentUpdate) { commentableId, update ->
            Pair(commentableId, requireNotNull(update))
        }
        .distinctUntilChanged()
        .subscribe {
            startRootCommentsActivityToDeepLinkThreadActivity.onNext(it)
            deepLinkToThreadActivity.onNext(Pair(it.first, false))
        }
        .addToDisposable(disposables)

    currentUpdate
        .compose(Transformers.takeWhenV2(goToCommentsRequest))
        .subscribe {
            it?.let { update ->
                startRootCommentsActivity.onNext(update)
            }
        }
        .addToDisposable(disposables)

    currentUpdate
        .map { NumberUtils.format(it.sequence()) }
        .subscribe { updateSequence.onNext(it) }
        .addToDisposable(disposables)

    goToProjectRequest
        .map { request -> Uri.parse(request.url.toUri().toString()) }
        .filter { it.isProjectUri(Secrets.WebEndpoint.PRODUCTION) }
        .filter { !it.isProjectPreviewUri(Secrets.WebEndpoint.PRODUCTION) }
        .subscribe {
            startProjectActivity.onNext(
                Pair(
                    it,
                    RefTag.update(),
                )
            )
        }
        .addToDisposable(disposables)

    goToProjectRequest
        .map { Uri.parse(it.url.toUri().toString()) }
        .filter { it.isProjectUri(Secrets.WebEndpoint.PRODUCTION) }
        .filter { it.isProjectPreviewUri(Secrets.WebEndpoint.PRODUCTION) }
        .map { it.toString() }
        .map {
            if (refTag(it) == null)
                appendRefTag(it, RefTag.update().tag())
            else
                it
        }
        .subscribe { openProjectExternally.onNext(it) }
        .addToDisposable(disposables)
}

internal fun parseProjectUpdateParams(request: Request): Pair<String, String> {
    val projectParam = request.url.encodedPathSegments[2]
    val updateParam = request.url.encodedPathSegments[4]
    return Pair.create(projectParam, updateParam)
}
