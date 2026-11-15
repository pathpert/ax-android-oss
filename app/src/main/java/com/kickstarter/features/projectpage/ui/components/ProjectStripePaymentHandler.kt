package com.kickstarter.features.projectpage.ui.components

import android.content.Intent
import androidx.activity.ComponentActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.utils.extensions.getPaymentSheetConfiguration
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.StripeIntentResult
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.model.PaymentOption
import timber.log.Timber

/**
 * Handles Stripe payment sheet flow controller operations, 3DS verification, and payment options callbacks.
 *
 * Negative constraints:
 * - Do not invoke payment confirmation without a valid Stripe flow controller initialization.
 */
class ProjectStripePaymentHandler(
    private val activity: ComponentActivity,
    private val stripe: Stripe,
    private val flowController: PaymentSheet.FlowController,
    private val latePledgeCheckoutViewModel: LatePledgeCheckoutViewModel,
    private val showToastError: (message: String?) -> Unit
) {
    fun onPaymentOption(paymentOption: PaymentOption?) {
        paymentOption?.let {
            flowController.confirm()
            latePledgeCheckoutViewModel.loading()
        }
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                showToastError(activity.getString(R.string.general_error_oops))
                latePledgeCheckoutViewModel.onNewCardFailed()
            }

            is PaymentSheetResult.Failed -> {
                val errorMessage = paymentSheetResult.error.localizedMessage
                    ?: activity.getString(R.string.general_error_something_wrong)
                showToastError(errorMessage)
                latePledgeCheckoutViewModel.onNewCardFailed()
            }

            is PaymentSheetResult.Completed -> {
                latePledgeCheckoutViewModel.onNewCardSuccessfullyAdded()
            }
        }
    }

    fun stripeNextAction(actionUrlOrSecret: String): DomainResult<Unit> {
        return try {
            if (actionUrlOrSecret.contains("pi_")) {
                stripe.handleNextActionForPayment(activity, actionUrlOrSecret)
            } else {
                stripe.handleNextActionForSetupIntent(activity, actionUrlOrSecret)
            }
            DomainResult.Success(Unit)
        } catch (exception: Exception) {
            val problem = ProblemDetailsMapper.fromThrowable(exception, "execute Stripe next action")
            FirebaseCrashlytics.getInstance().recordException(ProblemException(problem, exception))
            showToastError(problem.suggestedAction.ifBlank { problem.detail })
            DomainResult.Failure(problem)
        }
    }

    fun flowControllerPresentPaymentOption(
        clientSecret: String,
        userEmail: String,
        googlePayCountryCode: String? = null,
        googlePayCurrencyCode: String? = null
    ) {
        val googlePayEnabled = latePledgeCheckoutViewModel.isPaymentSheetGooglePayEnabled()
        Timber.d("googlePayEnabled: $googlePayEnabled, googlePayCountryCode: $googlePayCountryCode, googlePayCurrencyCode: $googlePayCurrencyCode")
        flowController.configureWithSetupIntent(
            setupIntentClientSecret = clientSecret,
            configuration = activity.getPaymentSheetConfiguration(userEmail, googlePayEnabled, googlePayCountryCode, googlePayCurrencyCode),
            callback = ::onConfigured
        )
    }

    private fun onConfigured(success: Boolean, error: Throwable?) {
        if (success) {
            flowController.presentPaymentOptions()
        } else {
            showToastError(null)
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        stripe.onPaymentResult(
            requestCode,
            intent,
            object : ApiResultCallback<PaymentIntentResult> {
                override fun onSuccess(result: PaymentIntentResult) {
                    if (result.outcome == StripeIntentResult.Outcome.SUCCEEDED) {
                        latePledgeCheckoutViewModel.completeOnSessionCheckoutFor3DS()
                    } else {
                        latePledgeCheckoutViewModel.clear3DSValues()
                        showToastError(null)
                    }
                }

                override fun onError(e: Exception) {
                    latePledgeCheckoutViewModel.clear3DSValues()
                    showToastError(null)
                }
            }
        )
    }
}
