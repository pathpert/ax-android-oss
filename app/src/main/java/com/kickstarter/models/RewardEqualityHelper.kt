package com.kickstarter.models

import java.util.Objects

/**
 * Equality and hashing helper for [Reward].
 */
object RewardEqualityHelper {

    fun areEqual(first: Reward, other: Any?): Boolean {
        if (first === other) return true
        if (other !is Reward) return false

        return first.backersCount() == other.backersCount() &&
            first.convertedMinimum() == other.convertedMinimum() &&
            first.description() == other.description() &&
            first.endsAt() == other.endsAt() &&
            first.startsAt() == other.startsAt() &&
            first.id() == other.id() &&
            first.image() == other.image() &&
            first.limit() == other.limit() &&
            first.minimum() == other.minimum() &&
            first.pledgeAmount() == other.pledgeAmount() &&
            first.latePledgeAmount() == other.latePledgeAmount() &&
            first.estimatedDeliveryOn() == other.estimatedDeliveryOn() &&
            first.remaining() == other.remaining() &&
            first.rewardsItems() == other.rewardsItems() &&
            first.shippingPreference() == other.shippingPreference() &&
            first.shippingSingleLocation() == other.shippingSingleLocation() &&
            first.shippingType() == other.shippingType() &&
            first.title() == other.title() &&
            first.isAddOn() == other.isAddOn() &&
            first.addOnsItems() == other.addOnsItems() &&
            first.quantity() == other.quantity() &&
            first.hasAddons() == other.hasAddons() &&
            first.shippingRules() == other.shippingRules() &&
            first.shippingPreferenceType() == other.shippingPreferenceType() &&
            first.isAvailable() == other.isAvailable() &&
            first.localReceiptLocation() == other.localReceiptLocation() &&
            first.isSecretReward() == other.isSecretReward() &&
            first.isFeatured() == other.isFeatured()
    }

    fun hash(reward: Reward): Int {
        return Objects.hash(
            reward.id(),
            reward.minimum(),
            reward.title(),
            reward.description(),
            reward.limit(),
            reward.remaining(),
            reward.shippingPreference(),
            reward.shippingType(),
            reward.isAddOn(),
            reward.hasAddons(),
            reward.isAvailable()
        )
    }
}
