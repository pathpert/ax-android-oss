package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Immutable financial metrics and currency configuration for a Kickstarter project.
 *
 * Negative Constraints:
 * - Do not pass floating point numbers for currency calculations; use integer minor units where possible.
 *
 * @param currency ISO 4217 currency code (e.g. "USD", "EUR", "GBP").
 * @param currencySymbol Formatted currency symbol (e.g. "$", "€", "£").
 * @param currentCurrency User's preferred localized display currency code.
 * @param currencyTrailingCode Whether the currency code is displayed following the amount.
 * @param goal Target funding goal in project currency units.
 * @param pledged Total amount pledged to date in project currency units.
 * @param staticUsdRate Static conversion rate to USD recorded at project creation.
 * @param usdExchangeRate Live conversion rate to USD.
 * @param fxRate Foreign exchange rate applied for localized display.
 */
@Parcelize
data class ProjectFinancials(
    val currency: String = "USD",
    val currencySymbol: String = "$",
    val currentCurrency: String? = null,
    val currencyTrailingCode: Boolean = false,
    val goal: Double = 0.0,
    val pledged: Double = 0.0,
    val staticUsdRate: Float = 1.0f,
    val usdExchangeRate: Float = 1.0f,
    val fxRate: Float = 1.0f
) : Parcelable

