package com.kickstarter.features.checkout.data

import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.checkoutTotalAmount
import com.kickstarter.libs.utils.extensions.locationId
import com.kickstarter.libs.utils.extensions.rewardsAndAddOnsList
import com.kickstarter.models.Backing
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.ui.data.PledgeData
import com.stripe.android.Stripe
import com.stripe.android.confirmPaymentIntent
import com.stripe.android.model.ConfirmPaymentIntentParams
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.rx2.asFlow

/**
 * Handles late pledge checkout network calls, Apollo mutations, and Stripe payment confirmation.
 *
 * Extracted from [com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel]
 * to adhere to AX Component-Local Modularization (Rule 2) and file-size constraints (< 300 LOC).
 */
class LatePledgePaymentProcessor(
    private val apolloClient: ApolloClientTypeV2,
    private val stripe: Stripe
) {

    suspend fun loadUserInfo(
        currentUserStream: CurrentUserTypeV2?,
        onStart: suspend () -> Unit,
        onSuccess: suspend (email: String, cards: List<StoredCard>) -> Unit,
        onError: suspend () -> Unit
    ) {
        try {
            coroutineScope {
                onStart()

                val user = currentUserStream?.observable()?.asFlow()?.first()?.getValue()
                user ?: return@coroutineScope

                val privacyAsync = async { apolloClient.userPrivacy().asFlow().first() }
                val cardsAsync = async { apolloClient.getStoredCards().asFlow().first() }

                val privacy = privacyAsync.await()
                val cards = cardsAsync.await()

                onSuccess(privacy.email, cards)
            }
        } catch (_: Throwable) {
            onError()
        }
    }

    suspend fun createSetupIntent(
        project: Project,
        onStart: suspend () -> Unit,
        onSuccess: suspend (String) -> Unit,
        onFailure: suspend () -> Unit,
        onComplete: suspend () -> Unit
    ) {
        apolloClient.createSetupIntent(project = project)
            .asFlow()
            .onStart { onStart() }
            .map { clientSecret -> onSuccess(clientSecret) }
            .catch { onFailure() }
            .onCompletion { onComplete() }
            .collect()
    }

    suspend fun savePaymentMethod(
        clientSecret: String,
        onStart: suspend () -> Unit,
        onSuccess: suspend (List<StoredCard>) -> Unit,
        onFailure: suspend () -> Unit,
        onComplete: suspend () -> Unit
    ) {
        apolloClient.savePaymentMethod(
            SavePaymentMethodData(reusable = true, intentClientSecret = clientSecret)
        ).asFlow()
            .onStart { onStart() }
            .map {
                val cards = apolloClient.getStoredCards().asFlow().first()
                onSuccess(cards)
            }
            .catch { onFailure() }
            .onCompletion { onComplete() }
            .collect()
    }

    suspend fun createCheckout(
        pledgeData: PledgeData,
        onStart: suspend () -> Unit,
        onSuccess: suspend (CheckoutPayment) -> Unit,
        onFailure: suspend (String?) -> Unit,
        onComplete: suspend () -> Unit,
        onErrorAction: () -> Unit
    ) {
        val locationId = if (!RewardUtils.isNoReward(pledgeData.reward())) pledgeData.locationId() else null
        val rewards = if (!RewardUtils.isNoReward(pledgeData.reward())) pledgeData.rewardsAndAddOnsList() else emptyList()
        val totalPledge = pledgeData.checkoutTotalAmount()

        val projectData = pledgeData.projectData() ?: return onErrorAction()
        val project = projectData.project()

        if (project.postCampaignPledgingEnabled() == true && project.isInPostCampaignPledgingPhase() == true) {
            apolloClient.createCheckout(
                CreateCheckoutData(
                    project = project,
                    amount = totalPledge.toString(),
                    locationId = locationId?.toString(),
                    rewardsIds = rewards,
                    refTag = projectData.refTagFromIntent()
                )
            ).asFlow()
                .onStart { onStart() }
                .map { checkoutPayment -> onSuccess(checkoutPayment) }
                .catch { onFailure(it.message) }
                .onCompletion { onComplete() }
                .collect()
        } else {
            onErrorAction()
        }
    }

    suspend fun createPaymentIntent(
        project: Project,
        amount: Double,
        checkoutId: String,
        backing: Backing,
        onSuccess: suspend (String) -> Unit,
        onFailure: suspend () -> Unit
    ) {
        apolloClient.createPaymentIntent(
            CreatePaymentIntentInput(
                project = project,
                amount = amount.toString(),
                checkoutId = checkoutId,
                backing = backing
            )
        ).asFlow()
            .map { clientSecret -> onSuccess(clientSecret) }
            .catch { onFailure() }
            .collect()
    }

    suspend fun validateAndConfirmPayment(
        checkoutId: String,
        clientSecret: String,
        selectedCard: StoredCard,
        onRequiresAction: suspend (String) -> Unit,
        onPaymentError: suspend (String?) -> Unit,
        onSuccess: suspend () -> Unit,
        onValidationFailure: suspend (String?) -> Unit,
        onComplete: suspend () -> Unit
    ) {
        apolloClient.validateCheckout(
            checkoutId = checkoutId,
            paymentIntentClientSecret = clientSecret,
            paymentSourceId = selectedCard.stripeCardId() ?: ""
        ).asFlow()
            .map { validation ->
                if (validation.isValid) {
                    confirmStripePayment(clientSecret, selectedCard, onRequiresAction, onPaymentError, onSuccess)
                } else {
                    val error = validation.messages.firstOrNull()
                    onValidationFailure(error)
                }
            }
            .catch { onValidationFailure(null) }
            .onCompletion { onComplete() }
            .collect()
    }

    private suspend fun confirmStripePayment(
        clientSecret: String,
        selectedCard: StoredCard,
        onRequiresAction: suspend (String) -> Unit,
        onPaymentError: suspend (String?) -> Unit,
        onSuccess: suspend () -> Unit
    ) {
        val intentParams = ConfirmPaymentIntentParams.createWithPaymentMethodId(
            clientSecret = clientSecret,
            paymentMethodId = selectedCard.stripeCardId() ?: ""
        )
        val withSDK = intentParams.withShouldUseStripeSdk(shouldUseStripeSdk = true)
        val paymentIntent = stripe.confirmPaymentIntent(withSDK)

        when {
            paymentIntent.requiresAction() -> onRequiresAction(clientSecret)
            paymentIntent.lastPaymentError != null -> onPaymentError(paymentIntent.lastPaymentError?.message)
            else -> onSuccess()
        }
    }

    suspend fun completeOnSessionCheckout(
        checkoutId: String,
        clientSecret: String,
        selectedCard: StoredCard?,
        newStoredCard: StoredCard?,
        onRequiresAction: suspend (String) -> Unit,
        onPledgeSuccess: suspend () -> Unit,
        onFailure: suspend () -> Unit,
        onComplete: suspend () -> Unit
    ) {
        apolloClient.completeOnSessionCheckout(
            checkoutId = checkoutId,
            paymentIntentClientSecret = clientSecret,
            paymentSourceId = if (selectedCard == newStoredCard) null else selectedCard?.id() ?: "",
            paymentSourceReusable = true
        ).asFlow()
            .map { pair ->
                if (pair.second) {
                    onRequiresAction(clientSecret)
                } else {
                    onPledgeSuccess()
                }
            }
            .catch { onFailure() }
            .onCompletion { onComplete() }
            .collect()
    }
}
