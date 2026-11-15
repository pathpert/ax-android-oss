package com.kickstarter.viewmodels.projectpage

import com.kickstarter.features.checkout.data.LatePledgePaymentProcessor
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.pledgeAmountTotal
import com.kickstarter.libs.utils.extensions.shippingCostIfShipping
import com.kickstarter.models.Backing
import com.kickstarter.models.Checkout
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import kotlinx.coroutines.launch

/**
 * Coordination and data building helper for [LatePledgeCheckoutViewModel].
 */
internal object LatePledgeCheckoutCoordinator {

    fun createCheckoutData(
        shippingAmount: Double,
        total: Double,
        bonusAmount: Double?,
        checkout: Checkout? = null
    ): CheckoutData {
        return CheckoutData.builder()
            .amount(total)
            .id(checkout?.id())
            .paymentType(CreditCardPaymentType.CREDIT_CARD)
            .bonusAmount(bonusAmount)
            .shippingAmount(shippingAmount)
            .build()
    }

    suspend fun createPaymentIntentForCheckout(
        paymentProcessor: LatePledgePaymentProcessor,
        selectedCard: StoredCard?,
        project: Project,
        totalAmount: Double,
        checkoutId: String?,
        backing: Backing?,
        onSuccess: (clientSecret: String) -> Unit,
        onFailure: () -> Unit,
        onErrorAction: () -> Unit
    ) {
        if (checkoutId != null && backing != null) {
            paymentProcessor.createPaymentIntent(
                project = project,
                amount = totalAmount,
                checkoutId = checkoutId,
                backing = backing,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        } else {
            onErrorAction()
        }
    }

    suspend fun validateCheckout(
        paymentProcessor: LatePledgePaymentProcessor,
        checkoutId: String,
        clientSecret: String,
        selectedCard: StoredCard,
        onRequiresAction: suspend (String) -> Unit,
        onPaymentError: (String?) -> Unit,
        onSuccess: () -> Unit,
        onValidationFailure: (String?) -> Unit,
        onComplete: suspend () -> Unit
    ) {
        paymentProcessor.validateAndConfirmPayment(
            checkoutId = checkoutId,
            clientSecret = clientSecret,
            selectedCard = selectedCard,
            onRequiresAction = onRequiresAction,
            onPaymentError = onPaymentError,
            onSuccess = onSuccess,
            onValidationFailure = onValidationFailure,
            onComplete = onComplete
        )
    }

    suspend fun completeOnSessionCheckout(
        paymentProcessor: LatePledgePaymentProcessor,
        checkoutId: String,
        clientSecret: String,
        selectedCard: StoredCard?,
        newStoredCard: StoredCard?,
        onRequiresAction: suspend (String) -> Unit,
        onPledgeSuccess: suspend () -> Unit,
        onFailure: () -> Unit,
        onComplete: suspend () -> Unit
    ) {
        paymentProcessor.completeOnSessionCheckout(
            checkoutId = checkoutId,
            clientSecret = clientSecret,
            selectedCard = selectedCard,
            newStoredCard = newStoredCard,
            onRequiresAction = onRequiresAction,
            onPledgeSuccess = onPledgeSuccess,
            onFailure = onFailure,
            onComplete = onComplete
        )
    }
}

internal fun LatePledgeCheckoutViewModel.handleAddNewCard(project: Project) {
    scope.launch(dispatcher) {
        paymentProcessor.createSetupIntent(
            project = project,
            onStart = { emitCurrentState(isLoading = true) },
            onSuccess = { clientSecret ->
                buttonEnabled = true
                clientSecretForNewCard = clientSecret
                mutableClientSecretForNewPaymentMethod.emit(clientSecretForNewCard)
            },
            onFailure = { resetPledgeButtonUIOnFailure() },
            onComplete = { emitCurrentState() }
        )
    }
}

internal fun LatePledgeCheckoutViewModel.handleNewCardSuccessfullyAdded() {
    scope.launch(dispatcher) {
        paymentProcessor.savePaymentMethod(
            clientSecret = clientSecretForNewCard,
            onStart = { emitCurrentState(isLoading = true) },
            onSuccess = { cards -> storedCards = cards },
            onFailure = { resetPledgeButtonUIOnFailure() },
            onComplete = { emitCurrentState() }
        )
    }
}

internal fun LatePledgeCheckoutViewModel.handlePledgeDataProvided(data: PledgeData) {
    this.pledgeData = data
    this.checkoutData = LatePledgeCheckoutCoordinator.createCheckoutData(
        data.shippingCostIfShipping(),
        data.pledgeAmountTotal(),
        data.bonusAmount()
    )
    selectedRewards.clear()
    data.addOns()?.let { addOns ->
        selectedRewards.add(data.reward())
        selectedRewards.addAll(addOns)
    }
    scope.launch(dispatcher) {
        paymentProcessor.loadUserInfo(
            currentUserStream = environment.currentUserV2(),
            onStart = { emitCurrentState(isLoading = true) },
            onSuccess = { email, cards ->
                userEmail = email
                storedCards = cards
                emitCurrentState()
            },
            onError = {
                errorAction.invoke(null)
                emitCurrentState()
            }
        )

        scope.launch(dispatcher) {
            paymentProcessor.createCheckout(
                pledgeData = data,
                onStart = { emitCurrentState(isLoading = false) },
                onSuccess = { cp -> mutableCheckoutPayment.emit(cp) },
                onFailure = { msg -> resetPledgeButtonUIOnFailure(msg) },
                onComplete = { emitCurrentState() },
                onErrorAction = { errorAction.invoke(null) }
            )
        }
    }
}

internal suspend fun LatePledgeCheckoutViewModel.executeCreatePaymentIntent(
    selectedCard: StoredCard?,
    project: Project,
    totalAmount: Double
) {
    if (paymentIntent.isNotNull()) return
    LatePledgeCheckoutCoordinator.createPaymentIntentForCheckout(
        paymentProcessor = paymentProcessor,
        selectedCard = selectedCard,
        project = project,
        totalAmount = totalAmount,
        checkoutId = checkoutId,
        backing = backing,
        onSuccess = { clientSecret ->
            paymentIntent = clientSecret
            if (selectedCard != null) {
                scope.launch(dispatcher) {
                    executeValidateCheckout(clientSecret, selectedCard)
                }
            } else {
                scope.launch {
                    emitCurrentState()
                    resetPledgeButtonUIOnFailure()
                }
            }
        },
        onFailure = {
            paymentIntent = null
            resetPledgeButtonUIOnFailure()
        },
        onErrorAction = {
            buttonEnabled = true
            paymentIntent = null
            scope.launch { emitCurrentState() }
            errorAction.invoke(null)
        }
    )
}

internal suspend fun LatePledgeCheckoutViewModel.executeValidateCheckout(
    clientSecret: String,
    selectedCard: StoredCard
) {
    LatePledgeCheckoutCoordinator.validateCheckout(
        paymentProcessor = paymentProcessor,
        checkoutId = checkoutId ?: "",
        clientSecret = clientSecret,
        selectedCard = selectedCard,
        onRequiresAction = { secret ->
            clientSecretFor3DSVerification = secret
            selectedCardFor3DSVerification = selectedCard
            mutablePaymentRequiresAction.emit(secret)
            emitCurrentState()
        },
        onPaymentError = { errorMsg ->
            scope.launch { emitCurrentState() }
            resetPledgeButtonUIOnFailure(errorMsg)
        },
        onSuccess = {
            scope.launch(dispatcher) {
                executeCompleteOnSessionCheckout(clientSecret, selectedCard)
            }
        },
        onValidationFailure = { errorMsg -> resetPledgeButtonUIOnFailure(errorMsg) },
        onComplete = { emitCurrentState() }
    )
}

internal suspend fun LatePledgeCheckoutViewModel.executeCompleteOnSessionCheckout(
    clientSecret: String,
    selectedCard: StoredCard
) {
    LatePledgeCheckoutCoordinator.completeOnSessionCheckout(
        paymentProcessor = paymentProcessor,
        checkoutId = checkoutId ?: "",
        clientSecret = clientSecret,
        selectedCard = selectedCard,
        newStoredCard = newStoredCard,
        onRequiresAction = { secret -> mutablePaymentRequiresAction.emit(secret) },
        onPledgeSuccess = { mutableOnPledgeSuccessAction.emit(true) },
        onFailure = { resetPledgeButtonUIOnFailure() },
        onComplete = { emitCurrentState() }
    )
}

internal fun LatePledgeCheckoutViewModel.handleCompleteOnSessionCheckoutFor3DS() {
    scope.launch(dispatcher) {
        if (clientSecretFor3DSVerification.isNotEmpty() && selectedCardFor3DSVerification.isNotNull()) {
            LatePledgeCheckoutCoordinator.completeOnSessionCheckout(
                paymentProcessor = paymentProcessor,
                checkoutId = checkoutId ?: "",
                clientSecret = clientSecretFor3DSVerification,
                selectedCard = selectedCardFor3DSVerification,
                newStoredCard = newStoredCard,
                onRequiresAction = { secret -> mutablePaymentRequiresAction.emit(secret) },
                onPledgeSuccess = { mutableOnPledgeSuccessAction.emit(true) },
                onFailure = {
                    resetPledgeButtonUIOnFailure()
                    clear3DSValues()
                },
                onComplete = { emitCurrentState() }
            )
        } else {
            resetPledgeButtonUIOnFailure()
            clear3DSValues()
        }
    }
}
