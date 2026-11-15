package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.libs.ReferrerType
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime
import java.util.Locale

@Parcelize
@androidx.annotation.Keep
data class ProjectStatsEnvelope(
    private val cumulativeStats: CumulativeStats? = null,
    private val fundingDistribution: List<FundingDateStats> = emptyList(),
    private val referralAggregates: ReferralAggregateStats = ReferralAggregateStats.builder().build(),
    private val referralDistribution: List<ReferrerStats> = emptyList(),
    private val rewardDistribution: List<RewardStats> = emptyList(),
    private val videoStats: VideoStats? = null
) : Parcelable {
    fun cumulative() = this.cumulativeStats
    fun fundingDistribution() = this.fundingDistribution
    fun referralAggregates() = this.referralAggregates
    fun referralDistribution() = this.referralDistribution
    fun rewardDistribution() = this.rewardDistribution
    fun videoStats() = this.videoStats

    @Parcelize
    data class Builder(
        private var cumulativeStats: CumulativeStats? = null,
        private var fundingDistribution: List<FundingDateStats> = emptyList(),
        private var referralAggregates: ReferralAggregateStats = ReferralAggregateStats.builder().build(),
        private var referralDistribution: List<ReferrerStats> = emptyList(),
        private var rewardDistribution: List<RewardStats> = emptyList(),
        private var videoStats: VideoStats? = null
    ) : Parcelable {
        fun cumulative(v: CumulativeStats?) = apply { this.cumulativeStats = v }
        fun fundingDistribution(v: List<FundingDateStats>?) = apply { v?.let { this.fundingDistribution = it } }
        fun referralAggregates(v: ReferralAggregateStats?) = apply { v?.let { this.referralAggregates = it } }
        fun referralDistribution(v: List<ReferrerStats>?) = apply { v?.let { this.referralDistribution = it } }
        fun rewardDistribution(v: List<RewardStats>?) = apply { v?.let { this.rewardDistribution = it } }
        fun videoStats(v: VideoStats?) = apply { this.videoStats = v }
        fun build() = ProjectStatsEnvelope(cumulativeStats, fundingDistribution, referralAggregates, referralDistribution, rewardDistribution, videoStats)
    }

    fun toBuilder() = Builder(cumulativeStats, fundingDistribution, referralAggregates, referralDistribution, rewardDistribution, videoStats)

    companion object {
        @JvmStatic fun builder() = Builder()
    }

    @Parcelize
    data class CumulativeStats(
        private val averagePledge: Float = 0f,
        private val backersCount: Int = 0,
        private val goal: Int = 0,
        private val percentRaised: Float = 0f,
        private val pledged: Float = 0f
    ) : Parcelable {
        fun averagePledge() = this.averagePledge
        fun backersCount() = this.backersCount
        fun goal() = this.goal
        fun percentRaised() = this.percentRaised
        fun pledged() = this.pledged

        @Parcelize
        data class Builder(
            private var averagePledge: Float = 0f,
            private var backersCount: Int = 0,
            private var goal: Int = 0,
            private var percentRaised: Float = 0f,
            private var pledged: Float = 0f,
        ) : Parcelable {
            fun averagePledge(v: Float?) = apply { this.averagePledge = v ?: 0f }
            fun backersCount(v: Int?) = apply { this.backersCount = v ?: 0 }
            fun goal(v: Int?) = apply { this.goal = v ?: 0 }
            fun percentRaised(v: Float?) = apply { this.percentRaised = v ?: 0f }
            fun pledged(v: Float?) = apply { this.pledged = v ?: 0f }
            fun build() = CumulativeStats(averagePledge, backersCount, goal, percentRaised, pledged)
        }

        fun toBuilder() = Builder(averagePledge, backersCount, goal, percentRaised, pledged)
        companion object {
            @JvmStatic fun builder() = Builder()
        }
    }

    @Parcelize
    data class FundingDateStats(
        private val backersCount: Int = 0,
        private val cumulativePledged: Float = 0f,
        private val cumulativeBackersCount: Int = 0,
        private val date: DateTime = DateTime.now(),
        private val pledged: Float = 0f
    ) : Parcelable {
        fun backersCount() = this.backersCount
        fun cumulativePledged() = this.cumulativePledged
        fun cumulativeBackersCount() = this.cumulativeBackersCount
        fun date() = this.date
        fun pledged() = this.pledged

        @Parcelize
        data class Builder(
            private var backersCount: Int = 0,
            private var cumulativePledged: Float = 0f,
            private var cumulativeBackersCount: Int = 0,
            private var date: DateTime = DateTime.now(),
            private var pledged: Float = 0f
        ) : Parcelable {
            fun backersCount(v: Int?) = apply { this.backersCount = v ?: 0 }
            fun cumulativePledged(v: Float?) = apply { this.cumulativePledged = v ?: 0f }
            fun cumulativeBackersCount(v: Int?) = apply { this.cumulativeBackersCount = v ?: 0 }
            fun date(v: DateTime?) = apply { v?.let { this.date = it } }
            fun pledged(v: Float?) = apply { this.pledged = v ?: 0f }
            fun build() = FundingDateStats(backersCount, cumulativePledged, cumulativeBackersCount, date, pledged)
        }

        fun toBuilder() = Builder(backersCount, cumulativePledged, cumulativeBackersCount, date, pledged)
        companion object {
            @JvmStatic fun builder() = Builder()
        }
    }

    @Parcelize
    data class ReferralAggregateStats(
        private val custom: Float = 0f,
        private val external: Float = 0f,
        private val internal: Float = 0f
    ) : Parcelable {
        fun custom() = this.custom
        fun external() = this.external
        fun internal() = this.internal

        @Parcelize
        data class Builder(
            private var custom: Float = 0f,
            private var external: Float = 0f,
            private var internal: Float = 0f
        ) : Parcelable {
            fun custom(v: Float?) = apply { this.custom = v ?: 0f }
            fun external(v: Float?) = apply { this.external = v ?: 0f }
            fun internal(v: Float?) = apply { this.internal = v ?: 0f }
            fun build() = ReferralAggregateStats(custom, external, internal)
        }

        fun toBuilder() = Builder(custom, external, internal)
        companion object {
            @JvmStatic fun builder() = Builder()
        }
    }

    @Parcelize
    data class ReferrerStats(
        private val backersCount: Int = 0,
        private val code: String = "",
        private val percentageOfDollars: Float = 0f,
        private val pledged: Float = 0f,
        private val referrerName: String = "",
        private val referrerType: String = ""
    ) : Parcelable {
        fun backersCount() = this.backersCount
        fun code() = this.code
        fun percentageOfDollars() = this.percentageOfDollars
        fun pledged() = this.pledged
        fun referrerName() = this.referrerName
        fun referrerType() = this.referrerType

        @Parcelize
        data class Builder(
            private var backersCount: Int = 0,
            private var code: String = "",
            private var percentageOfDollars: Float = 0f,
            private var pledged: Float = 0f,
            private var referrerName: String = "",
            private var referrerType: String = ""
        ) : Parcelable {
            fun backersCount(v: Int?) = apply { this.backersCount = v ?: 0 }
            fun code(v: String?) = apply { this.code = v ?: "" }
            fun percentageOfDollars(v: Float?) = apply { this.percentageOfDollars = v ?: 0f }
            fun pledged(v: Float?) = apply { this.pledged = v ?: 0f }
            fun referrerName(v: String?) = apply { this.referrerName = v ?: "" }
            fun referrerType(v: String?) = apply { this.referrerType = v ?: "" }
            fun build() = ReferrerStats(backersCount, code, percentageOfDollars, pledged, referrerName, referrerType)
        }

        fun toBuilder() = Builder(backersCount, code, percentageOfDollars, pledged, referrerName, referrerType)

        companion object {
            @JvmStatic
            fun referrerTypeEnum(referrerType: String): ReferrerType {
                return when (referrerType.lowercase(Locale.getDefault())) {
                    "custom" -> ReferrerType.CUSTOM
                    "external" -> ReferrerType.EXTERNAL
                    "kickstarter" -> ReferrerType.KICKSTARTER
                    else -> ReferrerType.KICKSTARTER
                }
            }

            @JvmStatic fun builder(): Builder = Builder()
        }
    }

    @Parcelize
    data class RewardStats(
        private val backersCount: Int = 0,
        private val rewardId: Int = 0,
        private val minimum: Int = 0,
        private val pledged: Float = 0f
    ) : Parcelable {
        fun backersCount() = this.backersCount
        fun rewardId() = this.rewardId
        fun minimum() = this.minimum
        fun pledged() = this.pledged

        @Parcelize
        data class Builder(
            private var backersCount: Int = 0,
            private var rewardId: Int = 0,
            private var minimum: Int = 0,
            private var pledged: Float = 0f
        ) : Parcelable {
            fun backersCount(v: Int?) = apply { this.backersCount = v ?: 0 }
            fun rewardId(v: Int?) = apply { this.rewardId = v ?: 0 }
            fun minimum(v: Int?) = apply { this.minimum = v ?: 0 }
            fun pledged(v: Float?) = apply { this.pledged = v ?: 0f }
            fun build() = RewardStats(backersCount, rewardId, minimum, pledged)
        }

        fun toBuilder() = Builder(backersCount, rewardId, minimum, pledged)
        companion object {
            @JvmStatic fun builder(): Builder = Builder()
        }
    }

    @Parcelize
    data class VideoStats(
        private val externalCompletions: Int = 0,
        private val externalStarts: Int = 0,
        private val internalCompletions: Int = 0,
        private val internalStarts: Int = 0,
    ) : Parcelable {
        fun externalCompletions() = this.externalCompletions
        fun externalStarts() = this.externalStarts
        fun internalCompletions() = this.internalCompletions
        fun internalStarts() = this.internalStarts

        @Parcelize
        data class Builder(
            private var externalCompletions: Int = 0,
            private var externalStarts: Int = 0,
            private var internalCompletions: Int = 0,
            private var internalStarts: Int = 0,
        ) : Parcelable {
            fun externalCompletions(v: Int?) = apply { this.externalCompletions = v ?: 0 }
            fun externalStarts(v: Int?) = apply { this.externalStarts = v ?: 0 }
            fun internalCompletions(v: Int?) = apply { this.internalCompletions = v ?: 0 }
            fun internalStarts(v: Int?) = apply { this.internalStarts = v ?: 0 }
            fun build() = VideoStats(externalCompletions, externalStarts, internalCompletions, internalStarts)
        }

        fun toBuilder() = Builder(externalCompletions, externalStarts, internalCompletions, internalStarts)
        companion object {
            @JvmStatic fun builder(): Builder = Builder()
        }
    }
}
