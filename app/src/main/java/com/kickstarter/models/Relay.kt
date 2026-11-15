package com.kickstarter.models

/**
 * Domain data model representing Relay.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
interface Relay {
    fun id(): Long
}
