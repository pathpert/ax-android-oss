package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isOrderPresentAndComplete
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Message
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.ui.data.MessagesData
import io.reactivex.Observable

internal fun MessagesViewModel.MessagesViewModel.initNavigationAndUiPipelines(
    messageAccountTypeObservable: Observable<MessagePreviousScreenType>,
    updatedMessages: Observable<List<Message>>,
    messageHasBody: Observable<Boolean>,
    messageIsSending: Observable<Boolean>,
    messageSent: Observable<Message>,
    project: Observable<Project>,
    messageThreadEnvelope: Observable<MessageThreadEnvelope>,
    messagesData: Observable<MessagesData>
) {
    messageAccountTypeObservable
        .map { c: MessagePreviousScreenType -> c == MessagePreviousScreenType.BACKER_MODAL }
        .subscribe { v: Boolean -> viewPledgeButtonIsGone.onNext(v) }
        .addToDisposable(disposables)

    backButtonIsGone = viewPledgeButtonIsGone.map { it.negate() }
    closeButtonIsGone = backButtonIsGone.map { it.negate() }
    projectNameToolbarTextViewText = projectNameTextViewText
    scrollRecyclerViewToBottom = updatedMessages.compose(Transformers.ignoreValuesV2())
    sendMessageButtonIsEnabled = Observable.merge(
        messageHasBody, messageIsSending.map { it.negate() }
    )
    setMessageEditText = messageSent.map { "" }

    toolbarIsExpanded = messageList
        .compose(Transformers.takePairWhenV2(messageEditTextIsFocused))
        .map { it.second }
        .map { it.negate() }

    project
        .map { it.name() }
        .subscribe { projectNameTextViewText.onNext(it) }
        .addToDisposable(disposables)

    project
        .compose(Transformers.takeWhenV2(projectContainerViewClicked))
        .subscribe { startProjectPageActivity.onNext(it) }
        .addToDisposable(disposables)

    messageThreadEnvelope
        .compose(Transformers.combineLatestPair(messagesData))
        .compose(Transformers.takeWhenV2(viewPledgeButtonClicked))
        .map { MessagesViewModelHelper.projectAndBacker(it) }
        .compose(
            Transformers.combineLatestPair(
                this.backingAndProject.filter { it.isNotNull() }
                    .map { requireNotNull(it) }
            )
        )
        .map {
            BackingWrapper(
                requireNotNull(it.second.first), it.first.second, it.first.first
            )
        }.subscribe {
            if (featureFlagClient.getBoolean(FlagKey.ANDROID_COMPLETED_PM_CHECKOUT_WEBVIEW) && it.backing.isOrderPresentAndComplete()) {
                it.backing.backingDetailsPageRoute()?.let { route ->
                    startBackingDetailsWebActivity.onNext(route)
                }
            } else {
                startBackingActivity.onNext(it)
            }
        }
        .addToDisposable(disposables)

    recyclerViewInitialBottomPadding = appBarTotalScrollRange.take(1)
    recyclerViewDefaultBottomPadding = appBarOffset
        .filter { it.isNonZero() }
        .compose(Transformers.ignoreValuesV2())
        .take(1)
}
