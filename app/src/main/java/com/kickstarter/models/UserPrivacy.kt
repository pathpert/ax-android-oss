package com.kickstarter.models

/**
 * Domain data model representing UserPrivacy.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
data class UserPrivacy(
    val name: String,
    val email: String,
    val hasPassword: Boolean,
    val isCreator: Boolean,
    val isDeliverable: Boolean,
    val isEmailVerified: Boolean,
    val chosenCurrency: String,
    val enabledFeatures: List<String> = emptyList()
)
