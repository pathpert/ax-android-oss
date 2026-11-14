package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * User newsletter subscription preferences for Kickstarter curated newsletters.
 *
 * Negative Constraints:
 * - Do not mutate preferences directly; dispatch mutation through UserRepository.
 */
@Parcelize
data class UserNewsletterPreferences(
    val alumni: Boolean = false,
    val artsCulture: Boolean = false,
    val film: Boolean = false,
    val games: Boolean = false,
    val happening: Boolean = false,
    val invent: Boolean = false,
    val music: Boolean = false,
    val promo: Boolean = false,
    val publishing: Boolean = false,
    val weekly: Boolean = false
) : Parcelable

