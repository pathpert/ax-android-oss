package com.kickstarter.features.pledgedprojectsoverview.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents an actionable or informational flag banner attached to a PPO project card.
 *
 * Provides visual cues (warning, action required, reminder) to backers regarding pending surveys,
 * address confirmation deadlines, or payment failure alerts.
 *
 * ### Negative Constraints:
 * - Do not pass unlocalized raw backend error strings directly into [message]; map to localized UI strings.
 * - Do not use for non-PPO banner messaging; use core alert/banner components instead.
 */
@Parcelize
class Flag private constructor(
    val icon: String?,
    val message: String?,
    val type: String?,
) : Parcelable {

    fun icon() = this.icon
    fun message() = this.message
    fun type() = this.type
    @Parcelize
    data class Builder(
        var icon: String? = null,
        var message: String? = null,
        var type: String? = null
    ) : Parcelable {

        fun icon(icon: String?) = apply { this.icon = icon }
        fun message(message: String?) = apply { this.message = message }
        fun type(type: String?) = apply { this.type = type }
        fun build() = Flag(
            icon = icon,
            message = message,
            type = type,
        )
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        icon = icon,
        message = message,
        type = type,
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Flag) {
            equals = icon() == other.icon() &&
                message() == other.message() &&
                type() == other.type()
        }
        return equals
    }
}
