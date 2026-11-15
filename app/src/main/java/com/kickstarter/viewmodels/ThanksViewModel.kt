package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.adapters.ThanksAdapter
import com.kickstarter.ui.adapters.data.ThanksData
import com.kickstarter.ui.viewholders.ProjectCardViewHolder
import com.kickstarter.ui.viewholders.ThanksCategoryViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

@Deprecated(
    message = "Legacy monolithic path replaced by SocialShareViewModel under com.kickstarter.features.socialshare.viewmodel.",
    replaceWith = ReplaceWith("SocialShareViewModel", "com.kickstarter.features.socialshare.viewmodel.SocialShareViewModel")
)
interface ThanksViewModel {
    interface Inputs :
        ProjectCardViewHolder.Delegate,
        ThanksCategoryViewHolder.Delegate,
        ThanksAdapter.Delegate {
        /** Call when the user clicks the close button.  */
        fun closeButtonClicked()

        /** Call when the user accepts the prompt to signup to the Games newsletter.  */
        fun signupToGamesNewsletterClick()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        /** Emits the data to configure the adapter with.  */
        fun adapterData(): Observable<ThanksData>

        /** Emits when we should finish the [com.kickstarter.ui.activities.ThanksActivity].  */
        fun finish(): Observable<Unit>

        /** Show a dialog confirming the user will be signed up to the games newsletter. Required for German users.  */
        fun showConfirmGamesNewsletterDialog(): Observable<Unit>

        /** Show a dialog prompting the user to sign-up to the games newsletter.  */
        fun showGamesNewsletterDialog(): Observable<Unit>

        /** Show a dialog prompting the user to rate the app.  */
        fun showRatingDialog(): Observable<Unit>

        /** Emits when we should start the [com.kickstarter.ui.activities.DiscoveryActivity].  */
        fun startDiscoveryActivity(): Observable<DiscoveryParams>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivity(): Observable<Pair<Project, RefTag>>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Unit>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class ThanksViewModel(val environment: Environment, private val intent: Intent) :
        ViewModel(),
        Inputs,
        Outputs {
        internal val apiClient = requireNotNull(environment.apiClientV2())
        internal val apolloClient = requireNotNull(environment.apolloClientV2())
        internal val hasSeenAppRatingPreference = environment.hasSeenAppRatingPreference()
        internal val hasSeenGamesNewsletterPreference = environment.hasSeenGamesNewsletterPreference()
        internal val currentUser = requireNotNull(environment.currentUserV2())
        internal val sharedPreferences = requireNotNull(environment.sharedPreferences())
        internal val cookieManager = requireNotNull(environment.cookieManager())
        internal val ffClient = requireNotNull(environment.featureFlagClient())

        internal val categoryCardViewHolderClicked = PublishSubject.create<Category>()
        internal val closeButtonClicked = PublishSubject.create<Unit>()
        internal val projectCardViewHolderClicked = PublishSubject.create<Project>()
        internal val signupToGamesNewsletterClick = PublishSubject.create<Unit>()
        internal val adapterData = BehaviorSubject.create<ThanksData>()
        internal val finish = PublishSubject.create<Unit>()
        internal val showConfirmGamesNewsletterDialog = PublishSubject.create<Unit>()
        internal val showGamesNewsletterDialog = BehaviorSubject.create<Unit>()
        internal val showRatingDialog = BehaviorSubject.create<Unit>()
        internal val signedUpToGamesNewsletter = PublishSubject.create<User>()
        internal val startDiscoveryActivity = PublishSubject.create<DiscoveryParams>()
        internal val startProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        internal val onHeartButtonClicked = PublishSubject.create<Project>()
        internal val showSavedPrompt = PublishSubject.create<Unit>()
        internal val analyticEvents = environment.analytics()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onThirdPartyEventSent = BehaviorSubject.create<Boolean?>()
        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        internal fun intent() = intent.let { Observable.just(it) }

        private val disposables = CompositeDisposable()

        init {
            initPipelines(disposables)
        }

        override fun categoryViewHolderClicked(category: Category) = categoryCardViewHolderClicked.onNext(category)
        override fun closeButtonClicked() = closeButtonClicked.onNext(Unit)
        override fun signupToGamesNewsletterClick() = signupToGamesNewsletterClick.onNext(Unit)
        override fun onHeartButtonClicked(project: Project) = onHeartButtonClicked.onNext(project)
        override fun projectCardViewHolderClicked(project: Project) = projectCardViewHolderClicked.onNext(project)

        override fun adapterData(): Observable<ThanksData> = this.adapterData
        override fun finish(): Observable<Unit> = this.finish
        override fun showConfirmGamesNewsletterDialog(): Observable<Unit> = this.showConfirmGamesNewsletterDialog
        override fun showGamesNewsletterDialog(): Observable<Unit> = this.showGamesNewsletterDialog
        override fun showRatingDialog(): Observable<Unit> = this.showRatingDialog
        override fun startDiscoveryActivity(): Observable<DiscoveryParams> = this.startDiscoveryActivity
        override fun startProjectActivity(): Observable<Pair<Project, RefTag>> = this.startProjectActivity
        override fun showSavedPrompt(): Observable<Unit> = this.showSavedPrompt

        private fun saveProject(project: Project): Observable<Project> {
            return this.apolloClient.watchProject(project)
                .compose(Transformers.neverErrorV2())
        }

        private fun unSaveProject(project: Project): Observable<Project> {
            return this.apolloClient.unWatchProject(project).compose(Transformers.neverErrorV2())
        }

        internal fun toggleProjectSave(project: Project): Observable<Project> {
            return if (project.isStarred()) {
                unSaveProject(project)
            } else {
                saveProject(project)
            }
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThanksViewModel(environment, intent) as T
        }
    }
}
