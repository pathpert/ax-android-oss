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
 *
 * Negative constraints:
 * - Do not hardcode tag strings in UI tests; reference constants from this enum.
 *
 * @property tag Compose test tag string.
 */
enum class RewardTrackingTestTag(val tag: String) {
    /** Test tag for the Track Shipment button. */
    TRACK_SHIPMENT_BUTTON("track_shipment_button"),
    /** Test tag for the project card modal sheet. */
    PROJECT_CARD_MODAL("project_card_modal")
}

/**
 * Surface context for reward tracking presentation.
 *
 * Negative constraints:
 * - Do not assume all page types provide a project card header.
 *
 * @property type Surface context identifier string.
 */
enum class RewardTrackingPageType(val type: String) {
    /** Reward tracking displayed within View Your Pledge screen. */
    VIEW_YOUR_PLEDGE("view_your_pledge"),
    /** Reward tracking displayed within the backer Activity Feed. */
    ACTIVITY_FEED("activity_feed")
}
