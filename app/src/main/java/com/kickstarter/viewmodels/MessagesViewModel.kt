package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Message
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.data.MessagesData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

@Deprecated(
    message = "Legacy monolithic path replaced by CommentsFeatureViewModel under com.kickstarter.features.comments.viewmodel.",
    replaceWith = ReplaceWith("CommentsFeatureViewModel", "com.kickstarter.features.comments.viewmodel.CommentsFeatureViewModel")
)
interface MessagesViewModel {
    interface Inputs {
        /** Call with the app bar's vertical offset value. */
        fun appBarOffset(verticalOffset: Int)

        /** Call with the app bar's total scroll range. */
        fun appBarTotalScrollRange(totalScrollRange: Int)

        /** Call when the message edit text changes. */
        fun messageEditTextChanged(messageBody: String)

        /** Call when the message edit text is in focus. */
        fun messageEditTextIsFocused(isFocused: Boolean)

        /** Call when the send message button has been clicked. */
        fun sendMessageButtonClicked()

        /** Call when the view pledge button is clicked. */
        fun viewPledgeButtonClicked()

        /** Call when the project container view is clicked. */
        fun projectContainerViewClicked()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        /** Emits a boolean that determines if the back button should be gone. */
        fun backButtonIsGone(): Observable<Boolean>

        /** Emits the backing and project to populate the backing info header. */
        fun backingAndProject(): Observable<Pair<Backing, Project>>

        /** Emits a boolean that determines if the backing info view should be gone. */
        fun backingInfoViewIsGone(): Observable<Boolean>

        /** Emits a boolean that determines if the close button should be gone. */
        fun closeButtonIsGone(): Observable<Boolean>

        /** Emits the creator name to be displayed. */
        fun creatorNameTextViewText(): Observable<String>

        /** Emits a boolean to determine if the loading indicator should be gone. */
        fun loadingIndicatorViewIsGone(): Observable<Boolean>

        /** Emits a string to display as the message edit text hint. */
        fun messageEditTextHint(): Observable<String>

        /** Emits when the edit text should request focus. */
        fun messageEditTextShouldRequestFocus(): Observable<Unit>

        /** Emits a list of messages to be displayed. */
        fun messageList(): Observable<List<Message>>

        /** Emits the project name to be displayed. */
        fun projectNameTextViewText(): Observable<String>

        /** Emits the project name to be displayed in the toolbar. */
        fun projectNameToolbarTextViewText(): Observable<String>

        /** Emits the bottom padding for the recycler view. */
        fun recyclerViewDefaultBottomPadding(): Observable<Unit>

        /** Emits the initial bottom padding for the recycler view to account for the app bar scroll range. */
        fun recyclerViewInitialBottomPadding(): Observable<Int>

        /** Emits when the RecyclerView should be scrolled to the bottom. */
        fun scrollRecyclerViewToBottom(): Observable<Unit>

        /** Emits a boolean that determines if the Send button should be enabled. */
        fun sendMessageButtonIsEnabled(): Observable<Boolean>

        /** Emits a string to set the message edit text to. */
        fun setMessageEditText(): Observable<String>

        /** Emits a string to display in the message error toast. */
        fun showMessageErrorToast(): Observable<String>

        /** Emits when we should start the [BackingActivity]. */
        fun startBackingActivity(): Observable<BackingWrapper>

        /** Emits when we should start the [ProjectPageActivity]. */
        fun startProjectPageActivity(): Observable<Project>

        /** Emits when we should start the [BackingDetailsActivity]. */
        fun startBackingDetailsWebActivity(): Observable<String>

        /** Emits when the thread has been marked as read. */
        fun successfullyMarkedAsRead(): Observable<Unit>

        /** Emits a boolean to determine when the toolbar should be expanded. */
        fun toolbarIsExpanded(): Observable<Boolean>

        /** Emits a boolean that determines if the View pledge button should be gone. */
        fun viewPledgeButtonIsGone(): Observable<Boolean>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class MessagesViewModel(environment: Environment, private val intent: Intent? = null) :
        ViewModel(),
        Inputs,
        Outputs {
        internal val apiClient: ApiClientTypeV2
        internal val apolloClient: ApolloClientTypeV2
        internal val currentUser: CurrentUserTypeV2
        internal val featureFlagClient: FeatureFlagClientType
        internal val disposables = CompositeDisposable()

        internal fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()

        internal val appBarOffset = PublishSubject.create<Int>()
        internal val appBarTotalScrollRange = PublishSubject.create<Int>()
        internal val messageEditTextChanged = PublishSubject.create<String>()
        internal val messageEditTextIsFocused = PublishSubject.create<Boolean>()
        internal val sendMessageButtonClicked = PublishSubject.create<Unit>()
        internal val projectContainerViewClicked = PublishSubject.create<Unit>()
        internal val viewPledgeButtonClicked = PublishSubject.create<Unit>()
        internal var backButtonIsGone: Observable<Boolean> = Observable.empty()
        internal val backingAndProject = BehaviorSubject.create<Pair<Backing, Project>>()
        internal val backingInfoViewIsGone = BehaviorSubject.create<Boolean>()
        internal var closeButtonIsGone: Observable<Boolean> = Observable.empty()
        internal val creatorNameTextViewText = BehaviorSubject.create<String>()
        internal var loadingIndicatorViewIsGone: Observable<Boolean> = Observable.empty()
        internal val messageEditTextHint = BehaviorSubject.create<String>()
        internal val messageEditTextShouldRequestFocus = BehaviorSubject.create<Unit>()
        internal val messageList = BehaviorSubject.create<List<Message>>()
        internal val projectNameTextViewText = BehaviorSubject.create<String>()
        internal var projectNameToolbarTextViewText: Observable<String> = Observable.empty()
        internal var recyclerViewDefaultBottomPadding: Observable<Unit> = Observable.empty()
        internal var recyclerViewInitialBottomPadding: Observable<Int> = Observable.empty()
        internal var scrollRecyclerViewToBottom: Observable<Unit> = Observable.empty()
        internal val showMessageErrorToast = PublishSubject.create<String>()
        internal var sendMessageButtonIsEnabled: Observable<Boolean> = Observable.empty()
        internal var setMessageEditText: Observable<String> = Observable.empty()
        internal val startBackingActivity = PublishSubject.create<BackingWrapper>()
        internal val startProjectPageActivity = PublishSubject.create<Project>()
        internal val startBackingDetailsWebActivity = PublishSubject.create<String>()
        internal val successfullyMarkedAsRead = BehaviorSubject.create<Unit>()
        internal var toolbarIsExpanded: Observable<Boolean> = Observable.empty()
        internal val viewPledgeButtonIsGone = BehaviorSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        override fun appBarOffset(verticalOffset: Int) {
            appBarOffset.onNext(verticalOffset)
        }

        override fun appBarTotalScrollRange(totalScrollRange: Int) {
            appBarTotalScrollRange.onNext(totalScrollRange)
        }

        override fun messageEditTextChanged(messageBody: String) {
            messageEditTextChanged.onNext(messageBody)
        }

        override fun messageEditTextIsFocused(isFocused: Boolean) {
            messageEditTextIsFocused.onNext(isFocused)
        }

        override fun sendMessageButtonClicked() {
            sendMessageButtonClicked.onNext(Unit)
        }

        override fun viewPledgeButtonClicked() {
            viewPledgeButtonClicked.onNext(Unit)
        }

        override fun projectContainerViewClicked() {
            projectContainerViewClicked.onNext(Unit)
        }

        override fun backButtonIsGone(): Observable<Boolean> = backButtonIsGone
        override fun backingAndProject(): Observable<Pair<Backing, Project>> = backingAndProject
        override fun backingInfoViewIsGone(): Observable<Boolean> = backingInfoViewIsGone
        override fun closeButtonIsGone(): Observable<Boolean> = closeButtonIsGone
        override fun loadingIndicatorViewIsGone(): Observable<Boolean> = loadingIndicatorViewIsGone
        override fun messageEditTextHint(): Observable<String> = messageEditTextHint
        override fun messageEditTextShouldRequestFocus(): Observable<Unit> = messageEditTextShouldRequestFocus
        override fun messageList(): Observable<List<Message>> = messageList
        override fun creatorNameTextViewText(): Observable<String> = creatorNameTextViewText
        override fun projectNameTextViewText(): Observable<String> = projectNameTextViewText
        override fun projectNameToolbarTextViewText(): Observable<String> = projectNameToolbarTextViewText
        override fun recyclerViewDefaultBottomPadding(): Observable<Unit> = recyclerViewDefaultBottomPadding
        override fun recyclerViewInitialBottomPadding(): Observable<Int> = recyclerViewInitialBottomPadding
        override fun scrollRecyclerViewToBottom(): Observable<Unit> = scrollRecyclerViewToBottom
        override fun showMessageErrorToast(): Observable<String> = showMessageErrorToast
        override fun sendMessageButtonIsEnabled(): Observable<Boolean> = sendMessageButtonIsEnabled
        override fun setMessageEditText(): Observable<String> = setMessageEditText
        override fun startBackingActivity(): Observable<BackingWrapper> = startBackingActivity
        override fun startProjectPageActivity(): Observable<Project> = startProjectPageActivity
        override fun startBackingDetailsWebActivity(): Observable<String> = startBackingDetailsWebActivity
        override fun successfullyMarkedAsRead(): Observable<Unit> = successfullyMarkedAsRead
        override fun toolbarIsExpanded(): Observable<Boolean> = toolbarIsExpanded
        override fun viewPledgeButtonIsGone(): Observable<Boolean> = viewPledgeButtonIsGone

        init {
            apolloClient = requireNotNull(environment.apolloClientV2())
            apiClient = requireNotNull(environment.apiClientV2())
            currentUser = requireNotNull(environment.currentUserV2())
            featureFlagClient = requireNotNull(environment.featureFlagClient())

            initPipelines()
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MessagesViewModel(environment, intent) as T
        }
    }
}
