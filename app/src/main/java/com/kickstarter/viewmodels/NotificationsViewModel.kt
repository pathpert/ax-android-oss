package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.models.User
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

@Deprecated(
    message = "Legacy monolithic path replaced by HomeScreenViewModel under com.kickstarter.features.home.viewmodel.",
    replaceWith = ReplaceWith("HomeScreenViewModel", "com.kickstarter.features.home.viewmodel.HomeScreenViewModel")
)
interface NotificationsViewModel {
    interface Inputs {

        /** Call when the notify mobile of pledge activity toggle changes.  */
        fun notifyMobileOfBackings(checked: Boolean)

        /** Call when the notify mobile of new comments toggle changes.  */
        fun notifyMobileOfComments(checked: Boolean)

        /** Call when the notify mobile of new comments toggle changes.  */
        fun notifyMobileOfCreatorEdu(checked: Boolean)

        /** Call when the notify mobile of new followers toggle changes.  */
        fun notifyMobileOfFollower(checked: Boolean)

        /** Call when the notify mobile of friend backs a project toggle changes.  */
        fun notifyMobileOfFriendActivity(checked: Boolean)

        /** Call when the notify mobile of messages toggle changes.  */
        fun notifyMobileOfMessages(checked: Boolean)

        /** Call when the notify mobile of project updates toggle changes.  */
        fun notifyMobileOfPostLikes(checked: Boolean)

        /** Call when the notify mobile of project updates toggle changes.  */
        fun notifyMobileOfUpdates(checked: Boolean)

        /** Call when the notify mobile of marketing updates toggle changes.  */
        fun notifyMobileOfMarketingUpdate(checked: Boolean)

        /** Call when the notify of pledge activity toggle changes.  */
        fun notifyOfBackings(checked: Boolean)

        /** Call when the notify of new comments toggle changes.  */
        fun notifyOfComments(checked: Boolean)

        /** Call when the notify of new comment reply toggle changes.  */
        fun notifyOfCommentReplies(checked: Boolean)

        /** Call when the email frequency spinner selection changes.  */
        fun notifyOfCreatorDigest(checked: Boolean)

        /** Call when the notify of creator tips toggle changes.  */
        fun notifyOfCreatorEdu(checked: Boolean)

        /** Call when the notify of new followers toggle changes.  */
        fun notifyOfFollower(checked: Boolean)

        /** Call when the notify of friend backs a project toggle changes.  */
        fun notifyOfFriendActivity(checked: Boolean)

        /** Call when the notify of messages toggle changes.  */
        fun notifyOfMessages(checked: Boolean)

        /** Call when the notify of project updates toggle changes.  */
        fun notifyOfUpdates(checked: Boolean)
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {

        /** Emission determines whether we should be hiding backings frequency emails settings.  */
        fun creatorDigestFrequencyIsGone(): Observable<Boolean>

        /** Emission determines whether we should be hiding creator notification settings.   */
        fun creatorNotificationsAreGone(): Observable<Boolean>

        /** Emits user containing settings state.  */
        fun user(): Observable<User>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Errors {
        fun unableToSavePreferenceError(): Observable<String>
    }

    class NotificationsViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs, Errors {
        private val userInput = PublishSubject.create<User>()

        private val creatorDigestFrequencyIsGone: Observable<Boolean>
        private val creatorNotificationsAreGone: Observable<Boolean>
        private val userOutput = BehaviorSubject.create<User>()
        private val updateSuccess = PublishSubject.create<Unit>()

        private val unableToSavePreferenceError = PublishSubject.create<Throwable>()

        val inputs: Inputs = this
        val outputs: Outputs = this
        val errors: Errors = this

        private val client = requireNotNull(environment.apiClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())

        private val disposables = CompositeDisposable()

        init {

            this.client.fetchCurrentUser()
                .retry(2)
                .compose(neverErrorV2())
                .subscribe { this.currentUser.refresh(it) }
                .addToDisposable(disposables)

            val currentUser = this.currentUser.observable()
                .filter { it.isPresent() }
                .map { checkNotNull(it.getValue()) }

            currentUser
                .take(1)
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            this.creatorDigestFrequencyIsGone = Observable.merge(currentUser, this.userInput)
                .map { !it.notifyOfBackings() }
                .distinctUntilChanged()

            this.creatorNotificationsAreGone = currentUser
                .map { (it.createdProjectsCount()).isZero() }
                .distinctUntilChanged()

            val updateSettingsNotification = this.userInput
                .concatMap { this.updateSettings(it) }

            updateSettingsNotification
                .compose(valuesV2())
                .subscribe { this.success(it) }
                .addToDisposable(disposables)

            updateSettingsNotification
                .compose(errorsV2())
                .subscribe {
                    this.unableToSavePreferenceError.onNext(
                        it ?: Throwable("update notifications settings error")
                    )
                }
                .addToDisposable(disposables)

            this.userInput
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)

            this.userOutput
                .window(2, 1)
                .flatMap<List<User>> { it.toList().toObservable() }
                .map { it.first() }
                .compose(takeWhenV2(this.unableToSavePreferenceError))
                .subscribe { this.userOutput.onNext(it) }
                .addToDisposable(disposables)
        }

        private fun updateUser(update: User.Builder.() -> User.Builder) {
            this.userOutput.value?.let { this.userInput.onNext(it.toBuilder().update().build()) }
        }

        override fun notifyMobileOfBackings(checked: Boolean) =
            updateUser { notifyMobileOfBackings(checked) }

        override fun notifyMobileOfComments(checked: Boolean) =
            updateUser { notifyMobileOfComments(checked) }

        override fun notifyMobileOfCreatorEdu(checked: Boolean) =
            updateUser { notifyMobileOfCreatorEdu(checked) }

        override fun notifyMobileOfFollower(checked: Boolean) =
            updateUser { notifyMobileOfFollower(checked) }

        override fun notifyMobileOfFriendActivity(checked: Boolean) =
            updateUser { notifyMobileOfFriendActivity(checked) }

        override fun notifyMobileOfMessages(checked: Boolean) =
            updateUser { notifyMobileOfMessages(checked) }

        override fun notifyMobileOfPostLikes(checked: Boolean) =
            updateUser { notifyMobileOfPostLikes(checked) }

        override fun notifyMobileOfUpdates(checked: Boolean) =
            updateUser { notifyMobileOfUpdates(checked) }

        override fun notifyMobileOfMarketingUpdate(checked: Boolean) =
            updateUser { notifyMobileOfMarketingUpdate(checked) }

        override fun notifyOfBackings(checked: Boolean) {
            this.userOutput.value?.let {
                val userBuilder = it.toBuilder().notifyOfBackings(checked)
                if (!checked) {
                    userBuilder.notifyOfCreatorDigest(false)
                }
                this.userInput.onNext(userBuilder.build())
            }
        }

        override fun notifyOfComments(checked: Boolean) =
            updateUser { notifyOfComments(checked) }

        override fun notifyOfCommentReplies(checked: Boolean) =
            updateUser { notifyOfCommentReplies(checked) }

        override fun notifyOfCreatorDigest(checked: Boolean) =
            updateUser { notifyOfCreatorDigest(checked) }

        override fun notifyOfCreatorEdu(checked: Boolean) =
            updateUser { notifyOfCreatorEdu(checked) }

        override fun notifyOfFollower(checked: Boolean) =
            updateUser { notifyOfFollower(checked) }

        override fun notifyOfFriendActivity(checked: Boolean) =
            updateUser { notifyOfFriendActivity(checked) }

        override fun notifyOfMessages(checked: Boolean) =
            updateUser { notifyOfMessages(checked) }

        override fun notifyOfUpdates(checked: Boolean) =
            updateUser { notifyOfUpdates(checked) }

        override fun creatorDigestFrequencyIsGone() = this.creatorDigestFrequencyIsGone

        override fun creatorNotificationsAreGone() = this.creatorNotificationsAreGone

        override fun user(): Observable<User> = this.userOutput

        override fun unableToSavePreferenceError(): Observable<String> =
            this.unableToSavePreferenceError
                .takeUntil(this.updateSuccess)
                .map { it.message ?: "Unable to save preference" }

        private fun success(user: User) {
            this.currentUser.refresh(user)
            this.updateSuccess.onNext(Unit)
        }

        private fun updateSettings(user: User): Observable<Notification<User>> {
            return this.client.updateUserSettings(user)
                .materialize()
                .share()
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationsViewModel(environment) as T
        }
    }
}
