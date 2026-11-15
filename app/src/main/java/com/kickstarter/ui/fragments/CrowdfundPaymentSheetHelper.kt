package com.kickstarter.ui.fragments

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.getPaymentSheetConfiguration
import com.kickstarter.libs.utils.extensions.isGooglePay
import com.kickstarter.libs.utils.extensions.isLink
import com.kickstarter.models.StoredCard
import com.kickstarter.models.toStripePaymentSheetResult
import com.kickstarter.ui.extensions.showErrorToast
import com.kickstarter.viewmodels.projectpage.CrowdfundCheckoutViewModel
import com.stripe.android.paymentsheet.PaymentOptionCallback
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.PaymentSheetResultCallback
import timber.log.Timber

/**
 * Stripe PaymentSheet orchestration helper for [CrowdfundCheckoutFragment].
 */
internal class CrowdfundPaymentSheetHelper(
    private val fragment: Fragment,
    private val viewModel: CrowdfundCheckoutViewModel,
    private val getErrorTargetView: () -> ViewGroup?
) {
    lateinit var flowController: PaymentSheet.FlowController
        private set

    fun initialize(setupClientIdProvider: () -> String) {
        val paymentOptionCallback = PaymentOptionCallback { paymentOption ->
            paymentOption?.let {
                val storedCard = StoredCard.Builder(
                    lastFourDigits = if (paymentOption.isLink() || paymentOption.isGooglePay()) {
                        paymentOption.label
                    } else {
                        paymentOption.label.takeLast(4)
                    },
                    resourceId = if (paymentOption.isGooglePay()) {
                        R.drawable.google_pay_mark_stored_card
                    } else {
                        paymentOption.drawableResourceId
                    },
                    clientSetupId = setupClientIdProvider()
                ).build()
                viewModel.newlyAddedPaymentMethod(storedCard)
                Timber.d("${fragment.javaClass.canonicalName} onPaymentOption with ${storedCard.lastFourDigits()} and ${storedCard.clientSetupId()}")
                flowController.confirm()
            }
        }

        val onPaymentSheetResult = PaymentSheetResultCallback { paymentSheetResult ->
            viewModel.paymentSheetResult(paymentSheetResult.toStripePaymentSheetResult())
            when (paymentSheetResult) {
                is PaymentSheetResult.Canceled -> {
                    showError(fragment.getString(R.string.general_error_oops))
                }
                is PaymentSheetResult.Failed -> {
                    val errorMessage = paymentSheetResult.error.localizedMessage
                        ?: fragment.getString(R.string.general_error_something_wrong)
                    showError(errorMessage)
                }
                is PaymentSheetResult.Completed -> {
                }
            }
        }

        flowController = PaymentSheet.FlowController.create(
            fragment = fragment,
            paymentOptionCallback = paymentOptionCallback,
            paymentResultCallback = onPaymentSheetResult
        )
    }

    fun presentPaymentOption(
        context: Context,
        clientSecret: String,
        userEmail: String,
        googlePayCountryCode: String? = null,
        googlePayCurrencyCode: String? = null
    ) {
        val googlePayEnabled = viewModel.isPaymentSheetGooglePayEnabled()
        Timber.d("googlePayEnabled: $googlePayEnabled, googlePayCountryCode: $googlePayCountryCode, googlePayCurrencyCode: $googlePayCurrencyCode")
        flowController.configureWithSetupIntent(
            setupIntentClientSecret = clientSecret,
            configuration = context.getPaymentSheetConfiguration(userEmail, googlePayEnabled, googlePayCountryCode, googlePayCurrencyCode),
            callback = ::onConfigured
        )
    }

    private fun onConfigured(success: Boolean, error: Throwable?) {
        if (success) {
            flowController.presentPaymentOptions()
        } else {
            val msg = error?.message ?: fragment.getString(R.string.general_error_something_wrong)
            showError(msg)
        }
        viewModel.paymentSheetPresented(success)
    }

    private fun showError(message: String) {
        getErrorTargetView()?.let { view ->
            fragment.context?.let { ctx ->
                fragment.activity?.runOnUiThread {
                    showErrorToast(ctx, view, message)
                }
            }
        }
    }
}
