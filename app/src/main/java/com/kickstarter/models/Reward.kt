package com.kickstarter.models

import android.os.Parcelable
import androidx.annotation.StringDef
import com.kickstarter.libs.utils.extensions.isZero
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

/**
 * Immutable domain model representing a project reward tier or add-on item.
 *
 * Negative constraints:
 * - Do not pass floating-point [minimum] or [pledgeAmount] across modern feature boundaries; convert to [com.kickstarter.ax.types.MinorCurrencyAmount].
 * - Do not use [isAvailable] without verifying [remaining] quantity and [endsAt] expiration.
 */
@Parcelize
@androidx.annotation.Keep

@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.*")
class Reward private constructor(
    private val backersCount: Int?,
    private val convertedMinimum: Double,
    private val description: String?,
    private val endsAt: DateTime?,
    private val id: Long,
    private val image: Photo?,
    private val limit: Int?,
    private val minimum: Double,
    private val pledgeAmount: Double,
    private val latePledgeAmount: Double,
    private val estimatedDeliveryOn: DateTime?,
    private val remaining: Int?,
    private val rewardsItems: List<RewardsItem>?,
    private val shippingPreference: String?,
    private val shippingSingleLocation: SingleLocation?,
    @ShippingType
    private val shippingType: String?,
    private val title: String?,
    private val isAddOn: Boolean,
    private val addOnsItems: List<RewardsItem>?,
    private val quantity: Int?,
    private val hasAddons: Boolean,
    private val startsAt: DateTime?,
    /**
     * This field will be available just for GraphQL, in V1 it would be null
     * A Reward is available when:
     * - Limit has not been reached
     * - ExpireData has not been reached
     *
     * @return true is the Reward is available
     */
    private val isAvailable: Boolean,
    /**
     * this field will be available just for GraphQL, in V1 it would be empty
     */
    private val shippingPreferenceType: ShippingPreference?,
    /**
     * this field will be available just for GraphQL, in V1 it would be empty
     */
    private val shippingRules: List<ShippingRule>?,
    /**
     * field reflecting the local pickup location, available only at GraphQL, in V1 it would be empty
     */
    private val localReceiptLocation: Location?,
    private val isSecretReward: Boolean?,
    private val isFeatured: Boolean
) : Parcelable, Relay {
    /** Total count of backers who have pledged for this reward tier. */
    fun backersCount() = this.backersCount

    /** Converted minimum pledge amount in the viewer's preferred display currency. */
    fun convertedMinimum() = this.convertedMinimum

    /** Detailed text description of the reward tier and deliverables. */
    fun description() = this.description

    /** Expiration timestamp for time-limited reward tiers, or null if available until campaign deadline. */
    fun endsAt() = this.endsAt

    /**
     * Database identifier (PID) of the reward.
     * Negative constraint: Do not confuse with GraphQL Relay global IDs.
     */
    override fun id() = this.id

    /** Preview photo/image metadata for this reward tier. */
    fun image() = this.image

    /** Total inventory limit set by the creator, or null if unlimited. */
    fun limit() = this.limit

    /**
     * Minimum pledge amount required for this reward in project native currency.
     * Negative constraint: Do not pass directly across modern AX boundaries; convert to MinorCurrencyAmount.
     */
    fun minimum() = this.minimum

    /**
     * Current total pledge amount associated with this selected reward.
     * Negative constraint: Do not pass directly across modern AX boundaries; convert to MinorCurrencyAmount.
     */
    fun pledgeAmount() = this.pledgeAmount

    /** Pledge price tier during late / post-campaign pledging. */
    fun latePledgeAmount() = this.latePledgeAmount

    /** Estimated delivery date for backer fulfillment. */
    fun estimatedDeliveryOn() = this.estimatedDeliveryOn

    /**
     * Remaining inventory count, or null if inventory is unlimited.
     * Negative constraint: Do not assume non-null for open reward tiers.
     */
    fun remaining() = this.remaining

    /** List of component items/deliverables bundled within this reward tier. */
    fun rewardsItems() = this.rewardsItems

    /** Shipping preference string ("none", "restricted", "unrestricted"). */
    fun shippingPreference() = this.shippingPreference

    /** Destination details if reward ships to a single specific country. */
    fun shippingSingleLocation() = this.shippingSingleLocation

    /** Shipping classification type string. */
    fun shippingType() = this.shippingType

    /** Display title of this reward tier. */
    fun title() = this.title

    /** Whether this reward represents an add-on item rather than a primary tier. */
    fun isAddOn() = this.isAddOn

    /** Add-on items bundled with this reward selection. */
    fun addOnsItems() = this.addOnsItems

    /** Selected quantity of this reward item. */
    fun quantity() = this.quantity

    /** Whether this reward tier permits add-on attachments. */
    fun hasAddons() = this.hasAddons

    /** Scheduled availability start timestamp, or null if available immediately. */
    fun startsAt() = this.startsAt

    /**
     * Whether this reward tier is currently open and backable (within inventory limits and time window).
     */
    fun isAvailable() = this.isAvailable

    /** GraphQL shipping preference enum value. */
    fun shippingPreferenceType() = this.shippingPreferenceType

    /** Applicable shipping destination rules and pricing rates. */
    fun shippingRules() = this.shippingRules

    /** Returns true if inventory is exhausted ([remaining] == 0). */
    fun isAllGone() = remaining().isZero()

    /** Returns true if this tier has a finite inventory limit and items remain. */
    fun isLimited() = limit() != null && !isAllGone()

    /** Physical pickup location details for local collection rewards. */
    fun localReceiptLocation() = this.localReceiptLocation

    /** Whether this reward tier requires a secret share link or token for access. */
    fun isSecretReward() = this.isSecretReward

    /** Whether this reward tier is highlighted as featured by the creator. */
    fun isFeatured() = this.isFeatured
    @Parcelize
    data class Builder(
        private var backersCount: Int? = null,
        private var convertedMinimum: Double = 0.0,
        private var description: String? = null,
        private var endsAt: DateTime? = null,
        private var id: Long = 0L,
        private var image: Photo? = null,
        private var limit: Int? = null,
        private var minimum: Double = 0.0,
        private var pledgeAmount: Double = 0.0,
        private var latePledgeAmount: Double = 0.0,
        private var estimatedDeliveryOn: DateTime? = null,
        private var remaining: Int? = null,
        private var rewardsItems: List<RewardsItem>? = emptyList(),
        private var shippingPreference: String? = null,
        private var shippingSingleLocation: SingleLocation? = null,
        private var shippingType: String? = null,
        private var title: String? = null,
        private var isAddOn: Boolean = false,
        private var addOnsItems: List<RewardsItem>? = emptyList(),
        private var quantity: Int? = null,
        private var hasAddons: Boolean = false,
        private var startsAt: DateTime? = null,
        private var isAvailable: Boolean = false,
        private var shippingPreferenceType: ShippingPreference? = null,
        private var shippingRules: List<ShippingRule>? = null,
        private var localReceiptLocation: Location? = null,
        private var isSecretReward: Boolean? = null,
        private var isFeatured: Boolean = false
    ) : Parcelable {
        fun backersCount(backersCount: Int?) = apply { this.backersCount = backersCount }
        fun convertedMinimum(convertedMinimum: Double?) = apply { this.convertedMinimum = convertedMinimum ?: 0.0 }
        fun description(description: String?) = apply { this.description = description }
        fun endsAt(endsAt: DateTime?) = apply { this.endsAt = endsAt }
        fun startsAt(startsAt: DateTime?) = apply { this.startsAt = startsAt }
        fun id(id: Long?) = apply { this.id = id ?: -1L }
        fun image(image: Photo?) = apply { this.image = image }
        fun limit(limit: Int?) = apply { this.limit = limit }
        fun minimum(minimum: Double?) = apply { this.minimum = minimum ?: 0.0 }
        fun pledgeAmount(pledgeAmount: Double?) = apply { this.pledgeAmount = pledgeAmount ?: 0.0 }
        fun latePledgeAmount(latePledgeAmount: Double?) = apply { this.latePledgeAmount = latePledgeAmount ?: 0.0 }
        fun estimatedDeliveryOn(estimatedDeliveryOn: DateTime?) = apply { this.estimatedDeliveryOn = estimatedDeliveryOn }
        fun remaining(remaining: Int?) = apply { this.remaining = remaining }
        fun rewardsItems(rewardsItems: List<RewardsItem>?) = apply { this.rewardsItems = rewardsItems ?: emptyList() }
        fun shippingPreference(shippingPreference: String?) = apply { this.shippingPreference = shippingPreference }
        fun shippingSingleLocation(shippingSingleLocation: SingleLocation?) = apply { this.shippingSingleLocation = shippingSingleLocation }
        fun shippingType(shippingType: String?) = apply { this.shippingType = shippingType }
        fun title(title: String?) = apply { this.title = title }
        fun isAddOn(isAddOn: Boolean?) = apply { this.isAddOn = isAddOn ?: false }
        fun addOnsItems(addOnsItems: List<RewardsItem>?) = apply { this.addOnsItems = addOnsItems ?: emptyList() }
        fun quantity(quantity: Int?) = apply { this.quantity = quantity }
        fun hasAddons(hasAddons: Boolean?) = apply { this.hasAddons = hasAddons ?: false }
        fun shippingRules(shippingRules: List<ShippingRule>?) = apply { this.shippingRules = shippingRules }
        fun shippingPreferenceType(shippingPreferenceType: ShippingPreference?) = apply { this.shippingPreferenceType = shippingPreferenceType }
        fun isAvailable(isAvailable: Boolean?) = apply { this.isAvailable = isAvailable ?: false }
        fun localReceiptLocation(localReceiptLocation: Location?) = apply { this.localReceiptLocation = localReceiptLocation }
        fun isSecretReward(isSecretReward: Boolean?) = apply { this.isSecretReward = isSecretReward ?: false }
        fun isFeatured(isFeatured: Boolean?) = apply { this.isFeatured = isFeatured ?: false }
        fun build() = Reward(
            backersCount = backersCount, convertedMinimum = convertedMinimum, description = description,
            endsAt = endsAt, startsAt = startsAt, id = id, image = image, limit = limit, minimum = minimum,
            pledgeAmount = pledgeAmount, latePledgeAmount = latePledgeAmount, estimatedDeliveryOn = estimatedDeliveryOn,
            remaining = remaining, rewardsItems = rewardsItems, shippingPreference = shippingPreference,
            shippingSingleLocation = shippingSingleLocation, shippingType = shippingType, title = title,
            isAddOn = isAddOn, addOnsItems = addOnsItems, quantity = quantity, hasAddons = hasAddons,
            shippingRules = shippingRules, shippingPreferenceType = shippingPreferenceType, isAvailable = isAvailable,
            localReceiptLocation = localReceiptLocation, isSecretReward = isSecretReward, isFeatured = isFeatured
        )
    }

    companion object {
        @JvmStatic fun builder() = Builder()
        const val SHIPPING_TYPE_ANYWHERE = "anywhere"
        const val SHIPPING_TYPE_MULTIPLE_LOCATIONS = "multiple_locations"
        const val SHIPPING_TYPE_NO_SHIPPING = "no_shipping"
        const val SHIPPING_TYPE_SINGLE_LOCATION = "single_location"
        const val SHIPPING_TYPE_LOCAL_PICKUP = "local"
    }

    fun toBuilder() = Builder(
        backersCount = backersCount, convertedMinimum = convertedMinimum, description = description,
        endsAt = endsAt, startsAt = startsAt, id = id, image = image, limit = limit, minimum = minimum,
        pledgeAmount = pledgeAmount, latePledgeAmount = latePledgeAmount, estimatedDeliveryOn = estimatedDeliveryOn,
        remaining = remaining, rewardsItems = rewardsItems, shippingPreference = shippingPreference,
        shippingSingleLocation = shippingSingleLocation, shippingType = shippingType, title = title,
        isAddOn = isAddOn, addOnsItems = addOnsItems, quantity = quantity, hasAddons = hasAddons,
        shippingRules = shippingRules, shippingPreferenceType = shippingPreferenceType, isAvailable = isAvailable,
        localReceiptLocation = localReceiptLocation, isSecretReward = isSecretReward, isFeatured = isFeatured
    )

    override fun equals(other: Any?): Boolean = RewardEqualityHelper.areEqual(this, other)

    override fun hashCode(): Int = RewardEqualityHelper.hash(this)

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(
        SHIPPING_TYPE_ANYWHERE,
        SHIPPING_TYPE_MULTIPLE_LOCATIONS,
        SHIPPING_TYPE_NO_SHIPPING,
        SHIPPING_TYPE_SINGLE_LOCATION
    )
    annotation class ShippingType

    enum class ShippingPreference(private val type: String) {
        NONE("none"),
        RESTRICTED("restricted"),
        UNRESTRICTED("unrestricted"),
        NOSHIPPING(
            SHIPPING_TYPE_NO_SHIPPING
        ),
        LOCAL("local"),
        UNKNOWN("\$UNKNOWN");
    }
}
