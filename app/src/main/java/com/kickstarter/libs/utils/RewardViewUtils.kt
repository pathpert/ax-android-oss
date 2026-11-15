package com.kickstarter.libs.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Pair
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.NumberOptions
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.trimAllWhitespace
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import java.math.RoundingMode
import kotlin.math.floor

private const val EMPTY_STRING = ""

object RewardViewUtils {

    /**
     * Returns the string resource ID of the rewards button based on project and reward status.
     */
    @StringRes
    fun pledgeButtonText(project: Project, reward: Reward): Int {
        val backing = project.backing()
        val hasAddOnsSelected = backing?.addOns()?.isNotEmpty() ?: false

        if ((backing == null || !backing.isBacked(reward)) && RewardUtils.isAvailableForProject(project, reward)) {
            return R.string.Select
        }

        return if (backing != null && backing.isBacked(reward)) {
            when {
                !reward.hasAddons() -> R.string.Selected
                reward.hasAddons() || hasAddOnsSelected -> R.string.Continue
                else -> R.string.No_longer_available
            }
        } else {
            R.string.No_longer_available
        }
    }

    /**
     * Returns the shipping summary for a reward.
     */
    fun shippingSummary(context: Context, ksString: KSString, stringResAndLocationName: Pair<Int, String?>): String {
        val stringRes = stringResAndLocationName.first
        val locationName = stringResAndLocationName.second
        val shippingSummary = context.getString(stringRes)

        return when (stringRes) {
            R.string.location_name_only -> when (locationName) {
                null -> context.getString(R.string.Limited_shipping)
                else -> ksString.format(shippingSummary, "location_name", locationName)
            }
            else -> context.getString(stringRes)
        }
    }

    /**
     * Returns a SpannableString representing currency that shrinks currency symbol if it's necessary.
     * Special case: US people looking at US currency just get the currency symbol.
     *
     */
    fun styleCurrency(value: Double, project: Project, ksCurrency: KSCurrency): SpannableString {
        val formattedCurrency = ksCurrency.format(value, project, RoundingMode.HALF_UP)
        val spannableString = SpannableString(formattedCurrency)

        val country = Country.findByCurrencyCode(project.currency()) ?: return spannableString

        val currencyNeedsCode = ksCurrency.currencyNeedsCode(country, true)
        val currencySymbolToDisplay = ksCurrency.getCurrencySymbol(country, true).trimAllWhitespace()

        if (currencyNeedsCode) {
            val startOfSymbol = formattedCurrency.indexOf(currencySymbolToDisplay)
            val endOfSymbol = startOfSymbol + currencySymbolToDisplay.length
            spannableString.setSpan(RelativeSizeSpan(.7f), startOfSymbol, endOfSymbol, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        return spannableString
    }

    /**
     * Returns a SpannableString representing currency that shrinks currency symbol if it's necessary.
     * Special case: US people looking at US currency just get the currency symbol.
     *
     */
    fun styleCurrency(value: Double, projectCurrency: String?, projectCurrentCurrency: String?, ksCurrency: KSCurrency): SpannableString {
        val formattedCurrency = ksCurrency.format(initialValue = value, projectCurrency = projectCurrency, projectCurrentCurrency = projectCurrentCurrency, roundingMode = RoundingMode.HALF_UP)
        val spannableString = SpannableString(formattedCurrency)

        val country = projectCurrency?.let {
            Country.findByCurrencyCode(it) ?: return spannableString
        } ?: return spannableString

        val currencyNeedsCode = ksCurrency.currencyNeedsCode(country, true)
        val currencySymbolToDisplay = ksCurrency.getCurrencySymbol(country, true).trimAllWhitespace()

        if (currencyNeedsCode) {
            val startOfSymbol = formattedCurrency.indexOf(currencySymbolToDisplay)
            val endOfSymbol = startOfSymbol + currencySymbolToDisplay.length
            spannableString.setSpan(RelativeSizeSpan(.7f), startOfSymbol, endOfSymbol, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        return spannableString
    }

    /**
     * Returns a String representing currency based on given currency code and symbol ex. $12 USD
     */
    fun formatCurrency(
        initialValue: Double,
        currencyCode: String? = null,
        currencySymbol: String? = null,
        roundingMode: RoundingMode = RoundingMode.DOWN,
    ): String {
        val roundedValue = getRoundedValue(initialValue, roundingMode)
        val numberOptions = NumberOptions.builder()
            .currencyCode(currencyCode)
            .currencySymbol(currencySymbol)
            .roundingMode(roundingMode)
            .precision(NumberUtils.precision(initialValue, roundingMode))
            .build()
        return NumberUtils.format(roundedValue, numberOptions).trimAllWhitespace()
    }

    /**
     * Returns a number rounded to the specification.
     *
     * @param initialValue Value to convert, local to the project's currency.
     * @param roundingMode When this is DOWN, we get the floor of the initialValue.
     */
    private fun getRoundedValue(initialValue: Double, roundingMode: RoundingMode): Float {
        return if (roundingMode == RoundingMode.DOWN) {
            floor(initialValue).toFloat()
        } else {
            initialValue.toFloat()
        }
    }

    /**
     * Returns the title for an Add On ie: 1 x TITLE
     *  [1 x] in green
     *  TITLE regular string
     */
    fun styleTitleForAddOns(context: Context, title: String?, quantity: Int?): SpannableString {
        val symbol = " x "
        val numberGreenCharacters = quantity.toString().length + symbol.length
        val spannable = SpannableString(quantity.toString() + symbol + title)
        spannable.setSpan(
            ForegroundColorSpan(context.getColor(R.color.kds_create_700)),
            0, numberGreenCharacters,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    /**
     * Returns the string for the error message a user receives when their inputted bonus amount causes
     * the total pledge amount to exceed the max pledge amount:
     *
     *  Enter an amount less than $X.
     *
     *  where X is calculated as maxPledgeAmount - rewardAmount
     */
    fun getMaxInputString(
        context: Context,
        selectedReward: Reward?,
        maxPledgeAmount: Double,
        totalAmount: Double,
        totalBonusSupport: Double,
        currencySymbolStartAndEnd: kotlin.Pair<String?, String?>,
        environment: Environment?
    ): String {

        // rewardAmount + totalBonusSupport = totalAmount
        // totalAmount must be <= maxPledgeAmount

        val maxInputAmount = if (selectedReward != null && RewardUtils.isNoReward(selectedReward)) {
            maxPledgeAmount
        } else {
            val rewardAmount = totalAmount - totalBonusSupport
            maxPledgeAmount - rewardAmount
        }
        val maxInputAmountWithCurrency =
            (currencySymbolStartAndEnd.first ?: "") +
                if (maxInputAmount % 1.0 == 0.0) maxInputAmount.toInt().toString()
                else maxInputAmount.toString() + (currencySymbolStartAndEnd.second ?: "")

        return environment?.ksString()?.format(
            context.getString(R.string.Enter_an_amount_less_than_max_pledge), // TODO: MBL-1416 Copy should say less than or equal to
            "max_pledge",
            maxInputAmountWithCurrency
        ) ?: ""
    }

    /**
     * Return a string for the estimated shipping cost of a single Reward for a given Location:
     * either a range or a fixed cost, depending on the shipping rule for that location.
     *
     * Ex. "About $10-$15" or "$10"
     *
     * Used specifically for each Reward Card in the Rewards Carousel.
     */
    fun getEstimatedShippingCost(
        context: Context,
        ksCurrency: KSCurrency,
        ksString: KSString,
        project: Project,
        reward: Reward,
        locationId: Long,
    ): String? = RewardShippingViewUtils.getEstimatedShippingCost(context, ksCurrency, ksString, project, reward, locationId)

    fun getEstimatedShippingRange(
        context: Context,
        ksCurrency: KSCurrency,
        ksString: KSString,
        project: Project,
        rewards: List<Reward> = listOf(),
        selectedShippingRule: ShippingRule,
        multipleQuantitiesAllowed: Boolean,
        useUserPreference: Boolean,
        useAbout: Boolean
    ): String = RewardShippingViewUtils.getEstimatedShippingRange(
        context, ksCurrency, ksString, project, rewards, selectedShippingRule, multipleQuantitiesAllowed, useUserPreference, useAbout
    )

    fun getAddOnShippingAmountString(
        context: Context,
        project: Project,
        reward: Reward,
        rewardShippingRules: List<ShippingRule>?,
        ksCurrency: KSCurrency?,
        ksString: KSString?,
        selectedShippingRule: ShippingRule
    ): String = RewardShippingViewUtils.getAddOnShippingAmountString(
        context, project, reward, rewardShippingRules, ksCurrency, ksString, selectedShippingRule
    )

    fun getQuantityRemainingString(context: Context, ksString: KSString, reward: Reward): String {
        val limit = reward.limit() ?: return EMPTY_STRING
        val remaining = reward.remaining() ?: return EMPTY_STRING
        if (limit <= 0 || remaining <= 0) return EMPTY_STRING

        val remainingCountLeftOfLimitCountString = context.getString(R.string.remaining_count_left_of_limit_count)
        return ksString.format(
            remainingCountLeftOfLimitCountString,
            "remaining_count",
            NumberUtils.format(remaining),
            "limit_count",
            NumberUtils.format(limit),
        )
    }

    /* Copied directly from RewardCarouselScreen.kt. Tested by RewardCarouselScreenTest.kt */
    fun isRewardSelectable(reward: Reward, project: Project, selectedLocationId: Long?, backing: Backing?): Boolean {
        return when {
            RewardUtils.isNoReward(reward) -> true
            reward.hasAddons() &&
                backing?.rewardId() == reward.id() &&
                (
                    project.isLive ||
                        (
                            project.postCampaignPledgingEnabled() == true &&
                                project.isInPostCampaignPledgingPhase() == true
                            )
                    ) -> true

            !reward.isAvailable() -> false

            !RewardUtils.isShippableToLocation(reward, selectedLocationId) -> false

            !reward.hasAddons() && backing?.isBacked(reward) != true -> true

            backing?.rewardId() != reward.id() &&
                RewardUtils.isAvailableForProject(project, reward) -> true

            else -> false
        }
    }
}
