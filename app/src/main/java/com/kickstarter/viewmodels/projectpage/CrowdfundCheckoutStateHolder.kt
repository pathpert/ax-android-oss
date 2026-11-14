package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * State container for reactive UI states in CrowdfundCheckoutViewModel.
 */
@Deprecated(
    message = "Legacy monolithic path replaced by CrowdfundCheckoutContracts under com.kickstarter.features.checkout.data.",
    replaceWith = ReplaceWith("CrowdfundCheckoutContracts", "com.kickstarter.features.checkout.data.CrowdfundCheckoutContracts")
)
abstract class CrowdfundCheckoutStateHolder : ViewModel() {

    protected var storedCards = emptyList<StoredCard>()
    protected var user: User? = null
    protected var selectedRewards = emptyList<Reward>()
    protected var selectedPaymentMethod: StoredCard = StoredCard.builder().build()
    protected var shippingRule: ShippingRule? = null
    protected var shippingAmount = 0.0
    protected var totalAmount = 0.0
    protected var bonusAmount = 0.0
    protected var incrementalPledge: Boolean? = null
    protected var showPlotWidget: Boolean = false
    protected var plotEligible: Boolean = false
    protected var paymentIncrements: List<PaymentIncrement>? = null

    protected var _crowdfundCheckoutUIState = MutableStateFlow(CheckoutUIState())
    val crowdfundCheckoutUIState: StateFlow<CheckoutUIState>
        get() = _crowdfundCheckoutUIState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = CheckoutUIState(isLoading = false)
        )

    protected var _checkoutResultState = MutableStateFlow<Pair<CheckoutData, PledgeData>>(Pair(null, null))
    val checkoutResultState: StateFlow<Pair<CheckoutData, PledgeData>>
        get() = _checkoutResultState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Pair<CheckoutData, PledgeData>(null, null)
        )

    protected var _presentPaymentSheet = MutableStateFlow(PaymentSheetPresenterState())
    val presentPaymentSheetStates
        get() = _presentPaymentSheet.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PaymentSheetPresenterState()
        )

    protected suspend fun emitCurrentState(isLoading: Boolean = false) {
        _crowdfundCheckoutUIState.emit(
            CheckoutUIState(
                storeCards = storedCards.toList(),
                userEmail = user?.email() ?: "",
                isLoading = isLoading,
                selectedRewards = selectedRewards,
                shippingAmount = shippingAmount,
                checkoutTotal = totalAmount,
                isPledgeButtonEnabled = !isLoading,
                selectedPaymentMethod = selectedPaymentMethod,
                bonusAmount = bonusAmount,
                shippingRule = shippingRule,
                plotEligible = plotEligible,
                showPlotWidget = showPlotWidget,
                paymentIncrements = paymentIncrements,
                isIncrementalPledge = incrementalPledge
            )
        )
    }
}
