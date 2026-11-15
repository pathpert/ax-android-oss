package com.kickstarter.features.backing.data

import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.libs.Either
import com.kickstarter.models.Backing
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeStatusData
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable

/**
 * Sealed UI and loading state representation for backing management.
 *
 * Negative constraints:
 * - Do not pass raw unhandled exceptions into the UI; errors MUST be normalized into [ProblemDetails].
 * - Do not instantiate [Success] with an empty or unvalidated [Backing] instance.
 */
sealed interface BackingState {
    /**
     * Initial or ongoing network fetch state.
     *
     * Negative constraints:
     * - Do not render pledge details while in loading state.
     */
    data object Loading : BackingState

    /**
     * Successfully loaded backing details.
     *
     * Negative constraints:
     * - Do not pass an unvalidated backing instance without a valid ID.
     *
     * @property backing Non-null validated domain backing entity.
     */
    data class Success(
        val backing: Backing
    ) : BackingState

    /**
     * Actionable error state during backing retrieval or update.
     *
     * Negative constraints:
     * - Do not pass generic uncontextualized errors without suggested action.
     *
     * @property problem Structured RFC 9457 actionable error details carrying suggestedAction and isRetriable flag.
     * @property throwable Underlying exception cause.
     */
    data class Error(
        val problem: ProblemDetails,
        val throwable: Throwable = Exception(problem.detail)
    ) : BackingState
}

/**
 * Modern user action inputs for the Backing feature.
 *
 * Negative Constraints:
 * - Do NOT pass null or malformed [ProjectData].
 */
interface BackingFeatureInputs {
    fun configureWith(projectData: ProjectData)
    fun fixPaymentMethodButtonClicked()
    fun pledgeSuccessfullyUpdated()
    fun receivedCheckboxToggled(checked: Boolean)
    fun refreshProject()
    fun isExpanded(state: Boolean?)
}

/**
 * Modern reactive outputs emitted by the Backing feature.
 *
 * Negative Constraints:
 * - Do NOT emit raw unformatted currency doubles; format with user currency and locale.
 */
interface BackingFeatureOutputs {
    fun backerAvatar(): Observable<String>
    fun backerName(): Observable<String>
    fun backerNumber(): Observable<String>
    fun cardExpiration(): Observable<String>
    fun cardIssuer(): Observable<Either<String, Int>>
    fun cardLastFour(): Observable<String>
    fun cardLogo(): Observable<Int>
    fun betaBadgeIsGone(): Observable<Boolean>
    fun fixPaymentMethodButtonIsGone(): Observable<Boolean>
    fun fixPaymentMethodMessageIsGone(): Observable<Boolean>
    fun notifyDelegateToRefreshProject(): Observable<Unit>
    fun notifyDelegateToShowFixPledge(): Observable<Unit>
    fun paymentMethodIsGone(): Observable<Boolean>
    fun pledgeAmount(): Observable<CharSequence>
    fun pledgeDate(): Observable<String>
    fun pledgeStatusData(): Observable<PledgeStatusData>
    fun pledgeSummaryIsGone(): Observable<Boolean>
    fun projectDataAndReward(): Observable<android.util.Pair<ProjectData, Reward>>
    fun projectDataAndAddOns(): Observable<android.util.Pair<ProjectData, List<Reward>>>
    fun receivedCheckboxChecked(): Observable<Boolean>
    fun receivedSectionIsGone(): Observable<Boolean>
    fun receivedSectionCreatorIsGone(): Observable<Boolean>
    fun shippingAmount(): Observable<CharSequence>
    fun shippingLocation(): Observable<String>
    fun shippingSummaryIsGone(): Observable<Boolean>
    fun showUpdatePledgeSuccess(): Observable<Unit>
    fun swipeRefresherProgressIsVisible(): Observable<Boolean>
    fun totalAmount(): Observable<CharSequence>
    fun bonusSupport(): Observable<CharSequence>
    fun estimatedDelivery(): Observable<String>
    fun deliveryDisclaimerSectionIsGone(): Observable<Boolean>
    fun paymentIncrements(): Observable<List<PaymentIncrement>>
    fun pledgeIsPlot(): Observable<Boolean>
    fun backingError(): Observable<Throwable>
    fun backingState(): Observable<BackingState>
}
