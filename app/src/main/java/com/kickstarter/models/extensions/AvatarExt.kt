package com.kickstarter.models.extensions

import com.kickstarter.models.Avatar

/**
 * Domain data model representing Avatar.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
fun Avatar.replaceSmallImageWithMediumIfEmpty(): String = this.small().ifEmpty { this.medium() }
