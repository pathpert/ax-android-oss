package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Pledge-Over-Time (POT) configuration and user-facing copy for installment backing.
 *
 * Negative Constraints:
 * - Do not pass unformatted strings into copy fields; ensure localized copy is provided.
 *
 * @param isAllowed Whether the project creator enabled Pledge Over Time for this campaign.
 * @param minimumExplanation Explanatory copy regarding the minimum pledge threshold for POT.
 * @param chargeExplanation Explanatory copy regarding when subsequent installment charges occur.
 * @param chargedAsNPayments Explanatory copy indicating the number of installment payments (e.g. "Charged as 4 payments").
 * @param shortPitch Promotional summary pitch for POT shown in reward selection.
 */
@Parcelize
data class ProjectPledgeOverTime(
    val isAllowed: Boolean = false,
    val minimumExplanation: String = "",
    val chargeExplanation: String = "",
    val chargedAsNPayments: String = "",
    val shortPitch: String = ""
) : Parcelable

