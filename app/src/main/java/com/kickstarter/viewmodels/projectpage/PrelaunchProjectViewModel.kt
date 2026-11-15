package com.kickstarter.viewmodels.projectpage

import android.content.Intent
import android.net.Uri
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.libs.rx.transformers.TakeWhenTransformerV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.updateProjectWith
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

@Deprecated(
    message = "Legacy monolithic path replaced by ProjectFeatureViewModel under com.kickstarter.features.projectpage.viewmodel.",
    replaceWith = ReplaceWith("ProjectFeatureViewModel", "com.kickstarter.features.projectpage.viewmodel.ProjectFeatureViewModel")
)
interface PrelaunchProjectViewModel {

    interface Inputs {
        /** Configure with current [projectSlug]. */
        fun configureWith(intent: Intent)

        /** Call when the creator button is clicked  */
        fun creatorInfoButtonClicked()

        /** Call when the heart button is clicked.  */
        fun bookmarkButtonClicked()

        /** Call when the share button is clicked.  */
        fun shareButtonClicked()

        /** Call when the creator Info is clicked  */
        fun creatorInfoClicked()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        fun project(): Observable<Project>

        /** Emits when we should show the share sheet with the name of the project and share URL.  */
        fun showShareSheet(): Observable<Pair<String, String>>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivity(): Observable<Unit>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Unit>

        fun startCreatorView(): Observable<Project>

        /** Returns true when the new social sharing experience should be shown. */
        fun isNewSocialShareEnabled(): Boolean
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class PrelaunchProjectViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this
        private val disposables = CompositeDisposable()

        internal val cookieManager = requireNotNull(environment.cookieManager())
        internal val currentUser = requireNotNull(environment.currentUserV2())
        internal val apolloClient = requireNotNull(environment.apolloClientV2())
        internal val apolloClientLegacy = requireNotNull(environment.apolloClientV2())
        internal val currentConfig = requireNotNull(environment.currentConfigV2())
        internal val sharedPreferences = requireNotNull(environment.sharedPreferences())
        internal val ffClient = requireNotNull(environment.featureFlagClient())
        internal val attributionEvents = requireNotNull(environment.attributionEvents())
        internal val statsigClient = requireNotNull(environment.statsigClient())

        internal val intent = BehaviorSubject.create<Intent>()
        internal val creatorInfoClicked = PublishSubject.create<Unit>()
        internal val shareButtonClicked = PublishSubject.create<Unit>()
        internal val bookmarkButtonClicked = PublishSubject.create<Unit>()
        internal val projectData = BehaviorSubject.create<ProjectData>()

        internal val project = PublishSubject.create<Project>()
        internal val startCreatorView = PublishSubject.create<Project>()
        internal val showShareSheet = PublishSubject.create<Pair<String, String>>()
        internal val startLoginToutActivity = PublishSubject.create<Unit>()
        internal val showSavedPrompt = PublishSubject.create<Unit>()
        internal val currentProject2 = PublishSubject.create<Project>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onThirdPartyEventSent = BehaviorSubject.create<Boolean>()

        init {
            initPipelines(disposables)
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        // - Inputs
        override fun configureWith(intent: Intent) = this.intent.onNext(intent)
        override fun creatorInfoButtonClicked() = this.creatorInfoClicked.onNext(Unit)
        override fun bookmarkButtonClicked() = this.bookmarkButtonClicked.onNext(Unit)
        override fun shareButtonClicked() = shareButtonClicked.onNext(Unit)
        override fun creatorInfoClicked() = creatorInfoClicked.onNext(Unit)

        // - Outputs
        override fun project(): Observable<Project> = this.project
        override fun showShareSheet(): Observable<Pair<String, String>> = this.showShareSheet
        override fun startLoginToutActivity(): Observable<Unit> = this.startLoginToutActivity
        override fun showSavedPrompt(): Observable<Unit> = this.showSavedPrompt
        override fun startCreatorView(): Observable<Project> = this.startCreatorView
        override fun isNewSocialShareEnabled(): Boolean =
            statsigClient.isReady.value &&
                statsigClient.checkGate(StatsigGateKey.ANDROID_PRELAUNCH_SOCIAL_SHARE.key)
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PrelaunchProjectViewModel(environment) as T
        }
    }
}
