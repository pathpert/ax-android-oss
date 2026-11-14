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
        fun amount(amount: Double?) = apply { this.amount = amount ?: 0.0 }
        fun backer(backer: User?) = apply { this.backer = backer }
        fun backerNote(backerNote: String?) = apply { this.backerNote = backerNote }
        fun backerName(backerName: String?) = apply { this.backerName = backerName }
        fun backerUrl(backerUrl: String?) = apply { this.backerUrl = backerUrl }
        fun backerId(backerId: Long?) = apply { this.backerId = backerId ?: 0L }
        fun backerCompletedAt(backerCompletedAt: DateTime?) = apply { this.backerCompletedAt = backerCompletedAt }
        fun backingDetailsPageRoute(backingDetailsPageRoute: String?) = apply { this.backingDetailsPageRoute = backingDetailsPageRoute }
        fun cancelable(cancelable: Boolean?) = apply { this.cancelable = cancelable ?: false }
        fun completedAt(completedAt: DateTime?) = apply { this.completedAt = completedAt }
        fun completedByBacker(completedByBacker: Boolean?) = apply { this.completedByBacker = completedByBacker ?: false }
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun incremental(incremental: Boolean?) = apply { this.incremental = incremental }
        fun location(location: Location?) = apply { this.location = location }
        fun locationId(locationId: Long?) = apply { this.locationId = locationId }
        fun locationName(locationName: String?) = apply { this.locationName = locationName }
        fun paymentSource(paymentSource: PaymentSource?) = apply { this.paymentSource = paymentSource }
        fun paymentIncrements(paymentIncrements: List<PaymentIncrement>?) = apply { this.paymentIncrements = paymentIncrements }
        fun pledgedAt(pledgedAt: DateTime?) = apply { this.pledgedAt = pledgedAt }
        fun project(project: Project?) = apply { this.project = project }
        fun projectId(projectId: Long?) = apply { this.projectId = projectId ?: 0L }
        fun reward(reward: Reward?) = apply { this.reward = reward }
        fun rewardId(rewardId: Long?) = apply { this.rewardId = rewardId }
        fun sequence(sequence: Long) = apply { this.sequence = sequence }
        fun shippingAmount(shippingAmount: Float?) = apply { this.shippingAmount = shippingAmount ?: 0.0f }
        fun status(status: String?) = apply { this.status = status ?: "" }
        fun addOns(addOns: List<Reward>?) = apply { this.addOns = addOns ?: emptyList() }
        fun bonusAmount(bonusAmount: Double?) = apply { this.bonusAmount = bonusAmount ?: 0.0 }
        fun isPostCampaign(isPostCampaign: Boolean) = apply { this.isPostCampaign = isPostCampaign }
        fun order(order: Order?) = apply { this.order = order }
        fun build() = Backing(
            amount = amount,
            backer = backer,
            backerNote = backerNote,
            backerName = backerName,
            backerUrl = backerUrl,
            backerId = backerId,
            backerCompletedAt = backerCompletedAt,
            backingDetailsPageRoute = backingDetailsPageRoute,
            cancelable = cancelable,
            completedAt = completedAt,
            completedByBacker = completedByBacker,
            id = id,
            incremental = incremental,
            location = location,
            locationId = locationId,
            locationName = locationName,
            paymentSource = paymentSource,
            pledgedAt = pledgedAt,
            project = project,
            projectId = projectId,
            reward = reward,
            rewardId = rewardId,
            sequence = sequence,
            shippingAmount = shippingAmount,
            status = status,
            addOns = addOns,
            bonusAmount = bonusAmount,
            isPostCampaign = isPostCampaign,
            paymentIncrements = paymentIncrements,
            order = order,
        )
    }

    fun toBuilder() = Builder(
        amount = amount,
        backer = backer,
        backerNote = backerNote,
        backerName = backerName,
        backerUrl = backerUrl,
        backerId = backerId,
        backerCompletedAt = backerCompletedAt,
        backingDetailsPageRoute = backingDetailsPageRoute,
        cancelable = cancelable,
        completedAt = completedAt,
        completedByBacker = completedByBacker,
        id = id,
        incremental = incremental,
        location = location,
        locationId = locationId,
        locationName = locationName,
        paymentSource = paymentSource,
        paymentIncrements = paymentIncrements,
        pledgedAt = pledgedAt,
        project = project,
        projectId = projectId,
        reward = reward,
        rewardId = rewardId,
        sequence = sequence,
        shippingAmount = shippingAmount,
        status = status,
        addOns = addOns,
        bonusAmount = bonusAmount,
        isPostCampaign = isPostCampaign,
        order = order,
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Backing) {
            equals = id() == other.id() &&
                incremental() == other.incremental() &&
                amount() == other.amount() &&
                backer() == other.backer() &&
                backerNote() == other.backerNote() &&
                backerName() == other.backerName() &&
                backerUrl() == other.backerUrl() &&
                backerId() == other.backerId() &&
                backerCompletedAt() == other.backerCompletedAt() &&
                backingDetailsPageRoute() == other.backingDetailsPageRoute() &&
                cancelable() == other.cancelable() &&
                completedAt() == other.completedAt() &&
                completedByBacker() == other.completedByBacker() &&
                location() == other.location() &&
                locationId() == other.locationId() &&
                locationName() == other.locationName() &&
                paymentSource() == other.paymentSource() &&
                paymentIncrements() == other.paymentIncrements() &&
                pledgedAt() == other.pledgedAt() &&
                project() == other.project() &&
                projectId() == other.projectId() &&
                reward() == other.reward() &&
                rewardId() == other.rewardId() &&
                sequence() == other.sequence() &&
                shippingAmount() == other.shippingAmount() &&
                status() == other.status() &&
                addOns() == other.addOns() &&
                bonusAmount() == other.bonusAmount()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

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
