package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
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
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import okhttp3.Request

@Deprecated(
    message = "Legacy monolithic path replaced by ProjectFeatureViewModel under com.kickstarter.features.projectpage.viewmodel.",
    replaceWith = ReplaceWith("ProjectFeatureViewModel", "com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel")
)
interface UpdateViewModel {
    interface Inputs {

        /** Call when a project update comments uri request has been made.  */
        fun goToCommentsRequest(request: Request)

        /** Call when a project uri request has been made.  */
        fun goToProjectRequest(request: Request)

        /** Call when a project update uri request has been made.  */
        fun goToUpdateRequest(request: Request)

        /** Call when the share button is clicked.  */
        fun shareIconButtonClicked()

        /** Call when a project update comments deep link*/
        fun goToCommentsActivity()

        /** Call when a project update comments deep link*/
        fun goToCommentsActivityToDeepLinkThreadActivity(commentableID: String)
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        /** Emits a project url to open externally.  */
        fun openProjectExternally(): Observable<String>

        /** Emits when we should start the share intent to show the share sheet.  */
        fun startShareIntent(): Observable<Pair<Update, String>>

        /** Emits an update to start the comments activity with.  */
        fun startRootCommentsActivity(): Observable<Update>

        /** Emits an update to start the comments activity with.  */
        fun deepLinkToThreadActivity(): Observable<Pair<String, Boolean>>

        /** Emits a Uri and a ref tag to start the project activity with.  */
        fun startProjectActivity(): Observable<Pair<Uri, RefTag>>

        fun startRootCommentsActivityToDeepLinkThreadActivity(): Observable<Pair<String, Update>>

        /** Emits a string to display in the toolbar title.  */
        fun updateSequence(): Observable<String>

        /** Emits a url to load in the web view.  */
        fun webViewUrl(): Observable<String>

        fun hasCommentsDeepLinks(): Observable<Boolean>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class UpdateViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {

        internal val client = requireNotNull(environment.apiClientV2())
        internal val goToCommentsRequest = PublishSubject.create<Request>()
        internal val goToProjectRequest = PublishSubject.create<Request>()
        internal val goToUpdateRequest = PublishSubject.create<Request>()
        internal val shareButtonClicked = PublishSubject.create<Unit>()
        internal val goToCommentsActivity = PublishSubject.create<Unit>()
        internal val goToCommentsActivityToDeepLinkThreadActivity = PublishSubject.create<String>()

        internal val openProjectExternally = PublishSubject.create<String>()
        internal val startShareIntent = PublishSubject.create<Pair<Update, String>>()
        internal val startRootCommentsActivity = PublishSubject.create<Update>()
        internal val startRootCommentsActivityToDeepLinkThreadActivity =
            PublishSubject.create<Pair<String, Update>>()
        internal val startProjectActivity = PublishSubject.create<Pair<Uri, RefTag>>()
        internal val updateSequence = BehaviorSubject.create<String>()
        internal val webViewUrl = BehaviorSubject.create<String>()
        internal val deepLinkToRootComment = BehaviorSubject.create<Boolean>()
        internal val deepLinkToThreadActivity = BehaviorSubject.create<Pair<String, Boolean>>()

        internal val intent = PublishSubject.create<Intent>()

        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        init {
            initUpdatePipelines(disposables)
        }

        fun provideIntent(intent: Intent?) {
            intent?.let {
                this.intent.onNext(it)
            }
        }

        override fun goToCommentsRequest(request: Request) = goToCommentsRequest.onNext(request)

        override fun goToProjectRequest(request: Request) = goToProjectRequest.onNext(request)

        override fun goToUpdateRequest(request: Request) = goToUpdateRequest.onNext(request)

        override fun shareIconButtonClicked() = shareButtonClicked.onNext(Unit)

        override fun goToCommentsActivity() = goToCommentsActivity.onNext(Unit)

        override fun goToCommentsActivityToDeepLinkThreadActivity(commentableID: String) =
            goToCommentsActivityToDeepLinkThreadActivity.onNext(commentableID)

        override fun openProjectExternally(): Observable<String> = openProjectExternally

        override fun startShareIntent(): Observable<Pair<Update, String>> = startShareIntent

        override fun startRootCommentsActivity(): Observable<Update> = startRootCommentsActivity

        override fun deepLinkToThreadActivity(): Observable<Pair<String, Boolean>> =
            deepLinkToThreadActivity

        override fun startProjectActivity(): Observable<Pair<Uri, RefTag>> = startProjectActivity

        override fun startRootCommentsActivityToDeepLinkThreadActivity(): Observable<Pair<String, Update>> =
            startRootCommentsActivityToDeepLinkThreadActivity

        override fun updateSequence(): Observable<String> = updateSequence

        override fun webViewUrl(): Observable<String> = webViewUrl

        override fun hasCommentsDeepLinks(): Observable<Boolean> = deepLinkToRootComment

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdateViewModel(environment) as T
        }
    }
}
