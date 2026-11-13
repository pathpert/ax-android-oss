package com.kickstarter.features.rewardtracking.data

/**
 * Value class representing a validated shipment tracking number.
 *
 * Negative constraints:
 * - Do not pass blank or empty strings as tracking numbers.
 * - Do not use for external carrier deep links without carrier-specific schema validation.
 */
@JvmInline
value class TrackingNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "Tracking number must not be blank." }
    }

    companion object {
        fun of(raw: String): TrackingNumber = TrackingNumber(raw.trim())
    }
}

/**
 * Test tags for headless Compose UI verification.
 */
enum class RewardTrackingTestTag(val tag: String) {
    TRACK_SHIPMENT_BUTTON("track_shipment_button"),
    PROJECT_CARD_MODAL("project_card_modal")
}

/**
 * Surface context for reward tracking presentation.
 *
 * Negative constraints:
 * - Do not assume all page types provide a project card header.
 */
enum class RewardTrackingPageType(val type: String) {
    VIEW_YOUR_PLEDGE("view_your_pledge"),
    ACTIVITY_FEED("activity_feed")
}
