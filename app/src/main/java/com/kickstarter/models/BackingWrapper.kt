package com.kickstarter.models

/**
 * Domain data model representing BackingWrapper.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
data class BackingWrapper(val backing: Backing, val user: User, val project: Project)
