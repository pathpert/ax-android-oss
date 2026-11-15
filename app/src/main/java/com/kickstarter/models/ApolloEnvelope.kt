package com.kickstarter.models

import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

/**
 * Domain data model representing ApolloEnvelope.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
interface ApolloEnvelope {
    fun pageInfoEnvelope(): PageInfoEnvelope?
}
