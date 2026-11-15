package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.libs.Either
import com.kickstarter.libs.Either.Left
import com.kickstarter.libs.Either.Right
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Backing
import com.kickstarter.models.Message
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.MessageSubject
import com.kickstarter.ui.data.MessagesData
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

internal fun MessagesViewModel.MessagesViewModel.initPipelines() {
    val configData = intent()
        .map { i: Intent ->
            val messageThread =
                i.getParcelableExtra<MessageThread>(IntentKey.MESSAGE_THREAD)
            messageThread?.let { Left(it) }
                ?: Right<MessageThread, Pair<Project?, Backing?>>(
                    Pair.create(
                        i.getParcelableExtra(IntentKey.PROJECT),
                        i.getParcelableExtra(IntentKey.BACKING)
                    )
                )
        }

    val messageAccountTypeObservable = intent()
        .map { it.getSerializableExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT) }
        .ofType(MessagePreviousScreenType::class.java)

    val configBacking = configData
        .filter { it.isNotNull() && it.right().isNotNull() }
        .map { requireNotNull(it.right()) }
        .filter { it.second.isNotNull() }
        .map { requireNotNull(it.second) }

    val configThread = configData
        .filter { it.isNotNull() && it.left().isNotNull() }
        .map { requireNotNull(it.left()) }

    val backingOrThread: Observable<Either<Backing, MessageThread>> =
        Observable.merge<Either<Backing, MessageThread>?>(
            configBacking.filter { it.isNotNull() }.map { Left(it) },
            configThread.filter { it.isNotNull() }.map { Right(it) }
        )

    val messageIsSending = PublishSubject.create<Boolean>()
    val messagesAreLoading = PublishSubject.create<Boolean>()

    val project = configData
        .map { data: Either<MessageThread, Pair<Project?, Backing?>> ->
            data.either({ obj: MessageThread -> obj.project() }) { projectAndBacking: Pair<Project?, Backing?> ->
                projectAndBacking.first
            }
        }.filter { it.isNotNull() }
        .map { requireNotNull(it) }

    val initialMessageThreadEnvelope = backingOrThread
        .switchMap {
            it.either(
                ifLeft = { backing ->
                    apiClient.fetchMessagesForBacking(backing)
                },
                ifRight = { messageThread ->
                    apiClient.fetchMessagesForThread(messageThread)
                }
            )
                .doOnSubscribe { messagesAreLoading.onNext(true) }
                .doAfterTerminate { messagesAreLoading.onNext(false) }
                .compose(Transformers.neverErrorV2())
                .share()
        }

    loadingIndicatorViewIsGone = messagesAreLoading
        .map { it.negate() }
        .distinctUntilChanged()

    val thread = initialMessageThreadEnvelope
        .map { it.messageThread() ?: MessageThread.builder().build() }

    val participant = Observable.combineLatest(
        thread,
        project
    ) { a: MessageThread, b: Project -> Pair.create(a, b) }
        .map {
            if (it.first != null && it.first.participant() != null)
                it.first?.participant()
            else
                it.second?.creator()
        }
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }
        .take(1)

    participant
        .map { it.name() }
        .subscribe { messageEditTextHint.onNext(it) }
        .addToDisposable(disposables)

    val messagesData: Observable<MessagesData> = Observable.combineLatest(
        backingOrThread,
        project,
        participant,
        currentUser.observable()
    ) { backOrThread, proj, part, currentUserOptional ->
        MessagesData(
            backOrThread,
            proj,
            part,
            requireNotNull(currentUserOptional.getValue())
        )
    }

    val messageSubject = messagesData
        .map { (backingOrThread1, project1, _, currentUser1): MessagesData ->
            backingOrThread1.either(
                { backing: Backing ->
                    if (backing.backerId() == currentUser1.id()) MessageSubject.Project(
                        project1
                    ) else MessageSubject.Backing(backing)
                }) { messageThread: MessageThread ->
                MessageSubject.MessageThread(
                    messageThread
                )
            }
        }

    val messageNotification = messageSubject
        .compose(Transformers.combineLatestPair(messageEditTextChanged))
        .compose(Transformers.takeWhenV2(sendMessageButtonClicked))
        .switchMap {
            apiClient.sendMessage(it.first, it.second)
                .doOnSubscribe { messageIsSending.onNext(true) }
        }
        .materialize()
        .share()

    val messageSent = messageNotification.compose(Transformers.valuesV2()).ofType(
        Message::class.java
    )

    val sentMessageThreadEnvelope = backingOrThread
        .compose(Transformers.takeWhenV2(messageSent))
        .switchMap {
            it.either({ backing: Backing ->
                apiClient.fetchMessagesForBacking(backing)
            }) { messageThread: MessageThread ->
                apiClient.fetchMessagesForThread(messageThread)
            }
        }
        .compose(Transformers.neverErrorV2())
        .share()

    val messageThreadEnvelope = Observable.merge(
        initialMessageThreadEnvelope,
        sentMessageThreadEnvelope
    ).distinctUntilChanged()

    val messageHasBody = messageEditTextChanged
        .map { it.isNotNull() && it.isPresent() }

    messageThreadEnvelope
        .filter { it.isNotNull() && it.messageThread().isNotNull() }
        .map { requireNotNull(it.messageThread()) }
        .switchMap { apiClient.markAsRead(it) }
        .materialize()
        .compose(Transformers.ignoreValuesV2())
        .subscribe { successfullyMarkedAsRead.onNext(it) }
        .addToDisposable(disposables)

    val initialMessages = initialMessageThreadEnvelope
        .filter { it.isNotNull() && it.messages().isNotNull() }
        .map { it.messages() }

    val newMessages = sentMessageThreadEnvelope
        .filter { it.isNotNull() && it.messages().isNotNull() }
        .map { requireNotNull(it.messages()) }

    val updatedMessages = initialMessages
        .compose(Transformers.takePairWhenV2(newMessages))
        .map {
            val messagesList = it.first
            val message = it.second?.toList()

            messagesList?.let { initialMessages ->
                message?.let { m ->
                    ListUtils.concatDistinct(initialMessages, m)
                }
            } ?: it.second
        }

    initialMessages
        .filter { it.isNotNull() }
        .take(1)
        .subscribe { messageList.onNext(it) }
        .addToDisposable(disposables)

    updatedMessages
        .subscribe { v: List<Message> -> messageList.onNext(v) }
        .addToDisposable(disposables)

    project
        .filter { it.creator().name().isNotNull() }
        .map { it.creator().name() }
        .subscribe { v: String -> creatorNameTextViewText.onNext(v) }
        .addToDisposable(disposables)

    initialMessageThreadEnvelope
        .filter { it.messages().isNotNull() }
        .map { it.messages() }
        .filter { it.isEmpty() }
        .distinctUntilChanged()
        .subscribe { messageEditTextShouldRequestFocus.onNext(Unit) }
        .addToDisposable(disposables)

    messagesData
        .switchMap { MessagesViewModelHelper.backingAndProjectFromData(it, apiClient, apolloClient) }
        .filter { it.isNotNull() }
        .subscribe { backingAndProject ->
            if (backingAndProject.first.isNotNull() && backingAndProject.second.isNotNull()) {
                this.backingAndProject.onNext(Pair<Backing, Project>(backingAndProject.first, backingAndProject.second))
                backingInfoViewIsGone.onNext(false)
            }
            if (backingAndProject.first == null) {
                backingInfoViewIsGone.onNext(true)
            }
        }
        .addToDisposable(disposables)

    messageNotification
        .compose(Transformers.errorsV2())
        .map { ErrorEnvelope.fromThrowable(it) }
        .map { it?.errorMessage() }
        .filter { it.isNotNull() }
        .map { requireNotNull(it) }
        .subscribe { showMessageErrorToast.onNext(it) }
        .addToDisposable(disposables)

    initNavigationAndUiPipelines(
        messageAccountTypeObservable = messageAccountTypeObservable,
        updatedMessages = updatedMessages,
        messageHasBody = messageHasBody,
        messageIsSending = messageIsSending,
        messageSent = messageSent,
        project = project,
        messageThreadEnvelope = messageThreadEnvelope,
        messagesData = messagesData
    )
}
