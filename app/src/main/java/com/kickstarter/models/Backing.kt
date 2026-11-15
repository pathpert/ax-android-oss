package com.kickstarter.models

import android.os.Parcelable
import androidx.annotation.StringDef
import com.google.firebase.remoteconfig.internal.ConfigFetchHandler
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

/**
 * Immutable domain model representing a backer's pledge/backing of a project.
 *
 * Negative constraints:
 * - Do not pass floating-point [amount] or [bonusAmount] across modern feature boundaries; convert to [com.kickstarter.ax.types.MinorCurrencyAmount].
 * - Do not assume [cancelable] is true without verifying campaign state and deadline.
 * - Guard mutating actions on backings with [com.kickstarter.ax.safety.IdempotencyGuard].
 */
@Parcelize
@androidx.annotation.Keep

@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.backing.data.BackingState")
class Backing private constructor(
    private val amount: Double,
    private val backer: User?,
    private val backerNote: String?,
    private val backerId: Long,
    private val backerName: String?,
    private val backerUrl: String?,
    private val backerCompletedAt: DateTime?,
    private val backingDetailsPageRoute: String?,
    private val cancelable: Boolean,
    private val completedAt: DateTime?,
    private val completedByBacker: Boolean,
    private val id: Long,
    private val incremental: Boolean?,
    private val location: Location?,
    private val locationId: Long?,
    private val locationName: String?,
    private val paymentSource: PaymentSource?,
    val paymentIncrements: List<PaymentIncrement>?,
    private val pledgedAt: DateTime?,
    private val project: Project?,
    private val projectId: Long,
    private val reward: Reward?,
    private val rewardId: Long?,
    private val sequence: Long,
    private val shippingAmount: Float,
    @ConfigFetchHandler.FetchResponse.Status
    private val status: String,
    private val addOns: List<Reward>?,
    private val bonusAmount: Double,
    private val isPostCampaign: Boolean,
    private val order: Order?,
) : Parcelable, Relay {
    /**
     * Total pledged backing amount in project native currency major units.
     * Negative constraint: Do not pass directly across modern AX boundaries; convert to MinorCurrencyAmount.
     */
    fun amount() = this.amount

    /** Domain model of the backer user who committed this pledge. */
    fun backer() = this.backer

    /** Note or instruction text submitted by the backer. */
    fun backerNote() = this.backerNote

    /** Database identifier (PID) of the backer user. */
    fun backerId() = this.backerId

    /** Display name of the backer. */
    fun backerName() = this.backerName

    /** Web profile URL for the backer. */
    fun backerUrl() = this.backerUrl

    /** Timestamp when the backer submitted reward fulfillment survey responses. */
    fun backerCompletedAt() = this.backerCompletedAt

    /** Deep-link navigation route for the backing details screen. */
    fun backingDetailsPageRoute() = this.backingDetailsPageRoute

    /**
     * Whether the backing can be cancelled by the backer at the current time.
     * Negative constraint: Guard all cancellation mutations with [com.kickstarter.ax.safety.IdempotencyGuard].
     */
    fun cancelable() = this.cancelable

    /** Timestamp when the backing transaction was finalized or collected. */
    fun completedAt() = this.completedAt

    /** Whether the backer has completed all required post-campaign fulfillment forms. */
    fun completedByBacker() = this.completedByBacker

    /**
     * Database identifier (PID) of the backing.
     * Negative constraint: Do not confuse with GraphQL Relay global IDs (`gid://Kickstarter/Backing/...`).
     */
    override fun id() = this.id

    /** Whether this backing is split across installment payments (Pledge Over Time). */
    fun incremental() = this.incremental

    /** Delivery destination location metadata for physical shipping. */
    fun location() = this.location

    /** Location identifier for the selected shipping destination rule. */
    fun locationId() = this.locationId

    /** Display name of the shipping destination. */
    fun locationName() = this.locationName

    /** Payment source / card details used to process this backing transaction. */
    fun paymentSource() = this.paymentSource

    /** List of installment payment schedules if [incremental] is true. */
    fun paymentIncrements() = this.paymentIncrements

    /** Timestamp when the backing was committed. */
    fun pledgedAt() = this.pledgedAt

    /** Associated campaign project domain model. */
    fun project() = this.project

    /** Database identifier (PID) of the campaign project. */
    fun projectId() = this.projectId

    /** Primary reward tier selected by the backer. */
    fun reward() = this.reward

    /** Database identifier (PID) of the selected reward tier. */
    fun rewardId() = this.rewardId

    /** Backer sequence number for this campaign (e.g. Backer #100). */
    fun sequence() = this.sequence

    /** Shipping fee charged for delivery in project native currency. */
    fun shippingAmount() = this.shippingAmount

    /**
     * Current lifecycle status string (e.g. [STATUS_PLEDGED], [STATUS_COLLECTED], [STATUS_DROPPED], [STATUS_ERRORED], [STATUS_CANCELED]).
     */
    @Status
    fun status() = this.status

    /** List of add-on reward items selected alongside the primary reward. */
    fun addOns() = this.addOns

    /** Additional bonus support amount contributed beyond the reward minimum. */
    fun bonusAmount() = this.bonusAmount

    /** Whether this backing was pledged during post-campaign late pledging. */
    fun isPostCampaign() = this.isPostCampaign

    /** Fulfillment order metadata for physical merchandise delivery. */
    fun order() = this.order

    @Parcelize
    data class Builder(
        private var amount: Double = 0.0,
        private var backer: User? = null,
        private var backerNote: String? = null,
        private var backerId: Long = 0L,
        private var backerName: String? = null,
        private var backerUrl: String? = null,
        private var backerCompletedAt: DateTime? = null,
        private var backingDetailsPageRoute: String? = null,
        private var cancelable: Boolean = false,
        private var completedAt: DateTime? = null,
        private var completedByBacker: Boolean = false,
        private var id: Long = 0L,
        private var incremental: Boolean? = null,
        private var location: Location? = null,
        private var locationId: Long? = null,
        private var locationName: String? = null,
        private var paymentSource: PaymentSource? = null,
        private var paymentIncrements: List<PaymentIncrement>? = null,
        private var pledgedAt: DateTime? = null,
        private var project: Project? = null,
        private var projectId: Long = 0L,
        private var reward: Reward? = null,
        private var rewardId: Long? = null,
        private var sequence: Long = 0L,
        private var shippingAmount: Float = 0.0f,
        @ConfigFetchHandler.FetchResponse.Status
        private var status: String = "",
        private var addOns: List<Reward>? = null,
        private var bonusAmount: Double = 0.0,
        private var isPostCampaign: Boolean = false,
        private var order: Order? = null,
    ) : Parcelable {
        fun amount(v: Double?) = apply { this.amount = v ?: 0.0 }
        fun backer(v: User?) = apply { this.backer = v }
        fun backerNote(v: String?) = apply { this.backerNote = v }
        fun backerName(v: String?) = apply { this.backerName = v }
        fun backerUrl(v: String?) = apply { this.backerUrl = v }
        fun backerId(v: Long?) = apply { this.backerId = v ?: 0L }
        fun backerCompletedAt(v: DateTime?) = apply { this.backerCompletedAt = v }
        fun backingDetailsPageRoute(v: String?) = apply { this.backingDetailsPageRoute = v }
        fun cancelable(v: Boolean?) = apply { this.cancelable = v ?: false }
        fun completedAt(v: DateTime?) = apply { this.completedAt = v }
        fun completedByBacker(v: Boolean?) = apply { this.completedByBacker = v ?: false }
        fun id(v: Long?) = apply { this.id = v ?: 0L }
        fun incremental(v: Boolean?) = apply { this.incremental = v }
        fun location(v: Location?) = apply { this.location = v }
        fun locationId(v: Long?) = apply { this.locationId = v }
        fun locationName(v: String?) = apply { this.locationName = v }
        fun paymentSource(v: PaymentSource?) = apply { this.paymentSource = v }
        fun paymentIncrements(v: List<PaymentIncrement>?) = apply { this.paymentIncrements = v }
        fun pledgedAt(v: DateTime?) = apply { this.pledgedAt = v }
        fun project(v: Project?) = apply { this.project = v }
        fun projectId(v: Long?) = apply { this.projectId = v ?: 0L }
        fun reward(v: Reward?) = apply { this.reward = v }
        fun rewardId(v: Long?) = apply { this.rewardId = v }
        fun sequence(v: Long) = apply { this.sequence = v }
        fun shippingAmount(v: Float?) = apply { this.shippingAmount = v ?: 0.0f }
        fun status(v: String?) = apply { this.status = v ?: "" }
        fun addOns(v: List<Reward>?) = apply { this.addOns = v ?: emptyList() }
        fun bonusAmount(v: Double?) = apply { this.bonusAmount = v ?: 0.0 }
        fun isPostCampaign(v: Boolean) = apply { this.isPostCampaign = v }
        fun order(v: Order?) = apply { this.order = v }
        fun build() = Backing(
            amount = amount, backer = backer, backerNote = backerNote, backerName = backerName,
            backerUrl = backerUrl, backerId = backerId, backerCompletedAt = backerCompletedAt,
            backingDetailsPageRoute = backingDetailsPageRoute, cancelable = cancelable, completedAt = completedAt,
            completedByBacker = completedByBacker, id = id, incremental = incremental, location = location,
            locationId = locationId, locationName = locationName, paymentSource = paymentSource, pledgedAt = pledgedAt,
            project = project, projectId = projectId, reward = reward, rewardId = rewardId, sequence = sequence,
            shippingAmount = shippingAmount, status = status, addOns = addOns, bonusAmount = bonusAmount,
            isPostCampaign = isPostCampaign, paymentIncrements = paymentIncrements, order = order
        )
    }

    fun toBuilder() = Builder(
        amount = amount, backer = backer, backerNote = backerNote, backerName = backerName,
        backerUrl = backerUrl, backerId = backerId, backerCompletedAt = backerCompletedAt,
        backingDetailsPageRoute = backingDetailsPageRoute, cancelable = cancelable, completedAt = completedAt,
        completedByBacker = completedByBacker, id = id, incremental = incremental, location = location,
        locationId = locationId, locationName = locationName, paymentSource = paymentSource,
        paymentIncrements = paymentIncrements, pledgedAt = pledgedAt, project = project, projectId = projectId,
        reward = reward, rewardId = rewardId, sequence = sequence, shippingAmount = shippingAmount,
        status = status, addOns = addOns, bonusAmount = bonusAmount, isPostCampaign = isPostCampaign, order = order
    )

    override fun equals(other: Any?): Boolean = BackingEqualityHelper.areEqual(this, other)

    override fun hashCode(): Int = BackingEqualityHelper.hash(this)

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(
        STATUS_CANCELED,
        STATUS_COLLECTED,
        STATUS_DROPPED,
        STATUS_ERRORED,
        STATUS_PLEDGED,
        STATUS_PREAUTH,
        STATUS_AUTHENTICATION_REQUIRED
    )
    annotation class Status

    companion object {
        @JvmStatic
        fun builder() = Builder()

        const val STATUS_CANCELED = "canceled"
        const val STATUS_COLLECTED = "collected"
        const val STATUS_DROPPED = "dropped"
        const val STATUS_ERRORED = "errored"
        const val STATUS_PLEDGED = "pledged"
        const val STATUS_PREAUTH = "preauth"
        const val STATUS_AUTHENTICATION_REQUIRED = "authentication_required"
    }
}
