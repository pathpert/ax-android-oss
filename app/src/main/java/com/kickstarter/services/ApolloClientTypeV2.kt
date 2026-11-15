package com.kickstarter.services

const val APOLLO_PAGE_SIZE = 25
const val APOLLO_REPLIES_PAGE_SIZE = 7

/**
 * Apollo GraphQL Client contract interface.
 *
 * Decoupled into [ApolloProjectClientType], [ApolloCheckoutClientType], and [ApolloUserClientType]
 * to satisfy AX Component-Local Modularization (Pillar 1) and Poka-Yoke / Contract-First Typing (Pillar 2 & 3).
 *
 * ### Negative Constraints:
 * - Do not pass unvalidated or blank IDs, slugs, or tokens across this boundary.
 * - Do not swallow mutation or query errors; all responses are typed or wrapped in [Result].
 */
@Deprecated("Legacy monolithic path replaced by com.kickstarter.services.apollo.*")
interface ApolloClientTypeV2 :
    ApolloProjectClientType,
    ApolloCheckoutClientType,
    ApolloUserClientType
