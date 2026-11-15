package com.kickstarter.models

/**
 * Domain data model representing ShippingCountryLocationsWrapper.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
data class ShippingCountryLocationsWrapper(
    val shippingCountryLocations: List<Location> = emptyList(),
    val shippableCountriesForProject: List<Location>? = null
)
