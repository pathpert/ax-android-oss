package com.kickstarter.models

import java.util.Objects

/**
 * Equality and hashing helper for [Backing].
 */
object BackingEqualityHelper {

    fun areEqual(first: Backing, other: Any?): Boolean {
        if (first === other) return true
        if (other !is Backing) return false

        return first.id() == other.id() &&
            first.incremental() == other.incremental() &&
            first.amount() == other.amount() &&
            first.backer() == other.backer() &&
            first.backerNote() == other.backerNote() &&
            first.backerName() == other.backerName() &&
            first.backerUrl() == other.backerUrl() &&
            first.backerId() == other.backerId() &&
            first.backerCompletedAt() == other.backerCompletedAt() &&
            first.backingDetailsPageRoute() == other.backingDetailsPageRoute() &&
            first.cancelable() == other.cancelable() &&
            first.completedAt() == other.completedAt() &&
            first.completedByBacker() == other.completedByBacker() &&
            first.location() == other.location() &&
            first.locationId() == other.locationId() &&
            first.locationName() == other.locationName() &&
            first.paymentSource() == other.paymentSource() &&
            first.paymentIncrements() == other.paymentIncrements() &&
            first.pledgedAt() == other.pledgedAt() &&
            first.project() == other.project() &&
            first.projectId() == other.projectId() &&
            first.reward() == other.reward() &&
            first.rewardId() == other.rewardId() &&
            first.sequence() == other.sequence() &&
            first.shippingAmount() == other.shippingAmount() &&
            first.status() == other.status() &&
            first.addOns() == other.addOns() &&
            first.bonusAmount() == other.bonusAmount()
    }

    fun hash(backing: Backing): Int {
        return Objects.hash(
            backing.id(),
            backing.amount(),
            backing.backerId(),
            backing.projectId(),
            backing.rewardId(),
            backing.sequence(),
            backing.status(),
            backing.cancelable()
        )
    }
}
