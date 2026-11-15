package com.kickstarter.mock.factories

import com.kickstarter.models.Project
import com.kickstarter.type.CreditCardTypes

/**
 * Mock factory producing Kickstarter [Project] instances localized for different international currencies and countries.
 */
object InternationalProjectFactory {

    @JvmStatic
    fun britishProject(): Project {
        return ProjectFactory.project()
            .toBuilder()
            .country("GB")
            .countryCode("GB")
            .currency("GBP")
            .currencySymbol("£")
            .build()
    }

    @JvmStatic
    fun caProject(): Project {
        return ProjectFactory.project()
            .toBuilder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .name("caProject")
            .country("CA")
            .countryCode("CA")
            .currentCurrency("CAD")
            .currencySymbol("$")
            .currency("CAD")
            .staticUsdRate(0.75f)
            .usdExchangeRate(0.75f)
            .fxRate(0.75f)
            .build()
    }

    @JvmStatic
    fun mxCurrencyCAProject(): Project {
        return ProjectFactory.project()
            .toBuilder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .name("mxCurrencyCAProject")
            .country("CA")
            .countryCode("CA")
            .currentCurrency("MXN")
            .currencySymbol("$")
            .currency("CAD")
            .staticUsdRate(0.75f)
            .usdExchangeRate(0.75f)
            .fxRate(0.75f)
            .build()
    }

    @JvmStatic
    fun mxProject(): Project {
        return ProjectFactory.project()
            .toBuilder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .name("mxProject")
            .country("MX")
            .countryCode("MX")
            .currentCurrency("MXN")
            .currencySymbol("$")
            .currency("MXN")
            .location(LocationFactory.mexico())
            .staticUsdRate(0.75f)
            .usdExchangeRate(0.75f)
            .fxRate(0.75f)
            .build()
    }

    @JvmStatic
    fun ukProject(): Project {
        return ProjectFactory.project()
            .toBuilder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .name("ukProject")
            .country("UK")
            .countryCode("GB")
            .currentCurrency("GBP")
            .currencySymbol("£")
            .currency("GBP")
            .staticUsdRate(1.5f)
            .usdExchangeRate(0.75f)
            .fxRate(1.5f)
            .build()
    }
}
