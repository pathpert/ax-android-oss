package com.kickstarter.services

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.User
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Network or service component for DiscoveryParams.
 *
 * Negative constraints:
 * - Do not perform blocking network calls on the main thread.
 */
@Parcelize
@Deprecated("Legacy monolithic path replaced by com.kickstarter.features.discovery.data.DiscoveryQuery")
class DiscoveryParams private constructor(
    private val backed: Int?,
    private val category: Category?,
    private val categoryParam: String?,
    private val location: Location?,
    private val locationParam: String?,
    private val page: Int?,
    private val perPage: Int?,
    private val pledged: Int?,
    private val staffPicks: Boolean?,
    private val starred: Int?,
    private val social: Int?,
    private val sort: Sort?,
    private val recommended: Boolean?,
    private val similarTo: Project?,
    private val state: State?,
    private val tagId: Int?,
    private val term: String?,
    private val raisedbucket: RaisedBuckets?,
    private val amountRaisedBucket: AmountBuckets?,
    private val goalBucket: GoalBuckets?
) : Parcelable {

    fun backed() = this.backed
    fun category() = this.category
    fun categoryParam() = this.categoryParam
    fun location() = this.location
    fun locationParam() = this.locationParam
    fun page() = this.page
    fun perPage() = this.perPage
    fun pledged() = this.pledged
    fun staffPicks() = this.staffPicks
    fun starred() = this.starred
    fun social() = this.social
    fun sort() = this.sort
    fun recommended() = this.recommended
    fun similarTo() = this.similarTo
    fun state() = this.state
    fun tagId() = this.tagId
    fun term() = this.term
    fun raisedBucket() = this.raisedbucket
    fun amountRaisedBucket() = this.amountRaisedBucket
    fun amountBucket() = this.amountRaisedBucket
    fun goalBucket() = this.goalBucket

    @Parcelize
    data class Builder(
        private var backed: Int? = null,
        private var category: Category? = null,
        private var categoryParam: String? = null,
        private var location: Location? = null,
        private var locationParam: String? = null,
        private var page: Int? = null,
        private var perPage: Int? = null,
        private var pledged: Int? = null,
        private var staffPicks: Boolean? = null,
        private var starred: Int? = null,
        private var social: Int? = null,
        private var sort: Sort? = null,
        private var recommended: Boolean? = null,
        private var similarTo: Project? = null,
        private var state: State? = null,
        private var tagId: Int? = null,
        private var term: String? = null,
        private var raisedBucket: RaisedBuckets? = null,
        private var amountBucket: AmountBuckets? = null,
        private var goalBucket: GoalBuckets? = null
    ) : Parcelable {
        fun backed(backed: Int?) = apply { this.backed = backed }
        fun category(category: Category?) = apply { this.category = category }
        fun categoryParam(categoryParam: String?) = apply { this.categoryParam = categoryParam }
        fun location(location: Location?) = apply { this.location = location }
        fun locationParam(locationParam: String?) = apply { this.locationParam = locationParam }
        fun page(page: Int?) = apply { this.page = page }
        fun perPage(perPage: Int?) = apply { this.perPage = perPage }
        fun pledged(pledged: Int?) = apply { this.pledged = pledged }
        fun sort(sort: Sort?) = apply { this.sort = sort }
        fun staffPicks(staffPicks: Boolean?) = apply { this.staffPicks = staffPicks }
        fun starred(starred: Int?) = apply { this.starred = starred }
        fun social(social: Int?) = apply { this.social = social }
        fun recommended(recommended: Boolean?) = apply { this.recommended = recommended }
        fun similarTo(similarTo: Project?) = apply { this.similarTo = similarTo }
        fun state(state: State?) = apply { this.state = state }
        fun raisedBucket(raisedbucket: RaisedBuckets?) = apply { this.raisedBucket = raisedbucket }
        fun amountBucket(amountRaisedBucket: AmountBuckets?) = apply { this.amountBucket = amountRaisedBucket }
        fun goalBucket(goalBucket: GoalBuckets?) = apply { this.goalBucket = goalBucket }
        fun tagId(tagId: Int?) = apply { this.tagId = tagId }
        fun term(term: String?) = apply { this.term = term }
        fun build() = DiscoveryParams(
            backed = backed, category = category, categoryParam = categoryParam,
            location = location, locationParam = locationParam, page = page, perPage = perPage,
            pledged = pledged, staffPicks = staffPicks, starred = starred, social = social,
            sort = sort, recommended = recommended, similarTo = similarTo, state = state,
            tagId = tagId, term = term, raisedbucket = raisedBucket,
            amountRaisedBucket = amountBucket, goalBucket = goalBucket
        )

        fun mergeWith(otherBuilder: Builder): Builder = DiscoveryParamsMerger.merge(this, otherBuilder)
    }

    fun toBuilder() = Builder(
        backed = backed, category = category, categoryParam = categoryParam,
        location = location, locationParam = locationParam, page = page, perPage = perPage,
        pledged = pledged, staffPicks = staffPicks, starred = starred, social = social,
        sort = sort, recommended = recommended, similarTo = similarTo, state = state,
        tagId = tagId, term = term, raisedBucket = raisedbucket,
        amountBucket = amountRaisedBucket, goalBucket = goalBucket
    )

    fun queryParams(): Map<String, String> = DiscoveryParamsQueryMapper.toQueryParams(this)

    fun shouldIncludeFeatured(): Boolean =
        category() != null && category()?.parent() == null && page() != null && page() == 1 && (sort() == null || sort() == Sort.MAGIC)

    override fun toString(): String = queryParams().toString()

    override fun equals(other: Any?): Boolean = DiscoveryParamsEqualityHelper.areEqual(this, other)

    override fun hashCode(): Int = DiscoveryParamsEqualityHelper.hash(this)

    @JvmOverloads
    fun filterString(
        context: Context,
        ksString: KSString,
        isToolbar: Boolean = false,
        isParentFilter: Boolean = false
    ): String = DiscoveryParamsFilterFormatter.filterString(this, context, ksString, isToolbar, isParentFilter)

    val isAllProjects: Boolean
        get() = ((staffPicks().isFalse() || staffPicks().isNull()) &&
            (starred() == null || starred() != 1) && (backed() == null || backed() != 1) &&
            (social() == null || social() != 1) && category() == null && location() == null && tagId() == null)

    val isSavedProjects: Boolean
        get() = ((starred() != null && starred() == 1).isTrue() && staffPicks().isFalse() &&
            (backed() == null || backed() != 1) && (social() == null || social() != 1) &&
            category() == null && location() == null && recommended().isFalse() && tagId() == null)

    val isCategorySet: Boolean get() = category() != null

    companion object {
        @JvmStatic
        fun fromUri(uri: Uri): DiscoveryParams = DiscoveryParamsUriParser.fromUri(uri)

        fun getDefaultParams(user: User?): DiscoveryParams {
            val builder = builder()
            if (user != null && user.optedOutOfRecommendations().isFalse()) {
                builder.recommended(true).backed(-1)
            }
            return builder
                .sort(Sort.MAGIC)
                .build()
        }

        @JvmStatic
        fun builder(): Builder = Builder().page(1).perPage(15)
    }

    enum class Sort {
        MAGIC, POPULAR, NEWEST, ENDING_SOON, DISTANCE,
        MOST_FUNDED, MOST_BACKED;

        override fun toString(): String = when (this) {
            MAGIC -> "magic"
            POPULAR -> "popularity"
            NEWEST -> "newest"
            ENDING_SOON -> "end_date"
            DISTANCE -> "distance"
            MOST_FUNDED -> "most_funded"
            MOST_BACKED -> "most_backed"
        }

        fun refTagSuffix(): String = when (this) {
            MAGIC -> ""
            POPULAR -> "_popular"
            NEWEST -> "_newest"
            ENDING_SOON -> "_ending_soon"
            DISTANCE -> "_distance"
            MOST_FUNDED -> "_most_funded"
            MOST_BACKED -> "_most_backed"
        }

        companion object {
            val defaultSorts = arrayOf(MAGIC, POPULAR, NEWEST, ENDING_SOON)

            fun fromString(string: String): Sort = when (string.lowercase(Locale.getDefault())) {
                "magic" -> MAGIC
                "popularity" -> POPULAR
                "newest" -> NEWEST
                "end_date" -> ENDING_SOON
                "distance" -> DISTANCE
                "most_funded" -> MOST_FUNDED
                "most_backed" -> MOST_BACKED
                else -> MAGIC
            }
        }
    }

    enum class State {
        STARTED, SUBMITTED, LIVE, SUCCESSFUL, CANCELED, FAILED, UPCOMING, LATE_PLEDGES, UNKNOWN;

        override fun toString(): String = name.lowercase(Locale.getDefault())

        companion object {
            fun fromString(string: String): State = when (string.lowercase(Locale.getDefault())) {
                "started" -> STARTED
                "submitted" -> SUBMITTED
                "live" -> LIVE
                "successful" -> SUCCESSFUL
                "canceled" -> CANCELED
                "upcoming" -> UPCOMING
                "late_pledges" -> LATE_PLEDGES
                "failed" -> FAILED
                else -> UNKNOWN
            }
        }
    }

    enum class RaisedBuckets {
        BUCKET_0, BUCKET_1, BUCKET_2;

        override fun toString(): String = name.lowercase(Locale.getDefault())

        companion object {
            fun fromString(string: String?): RaisedBuckets? = when (string) {
                "0", "BUCKET_0" -> BUCKET_0
                "1", "BUCKET_1" -> BUCKET_1
                "2", "BUCKET_2" -> BUCKET_2
                else -> null
            }
        }
    }

    enum class AmountBuckets {
        BUCKET_0, BUCKET_1, BUCKET_2, BUCKET_3, BUCKET_4;

        override fun toString(): String = name.lowercase(Locale.getDefault())

        companion object {
            fun fromString(string: String?): AmountBuckets? = when (string) {
                "0", "BUCKET_0" -> BUCKET_0
                "1", "BUCKET_1" -> BUCKET_1
                "2", "BUCKET_2" -> BUCKET_2
                "3", "BUCKET_3" -> BUCKET_3
                "4", "BUCKET_4" -> BUCKET_4
                else -> null
            }
        }
    }

    enum class GoalBuckets {
        BUCKET_0, BUCKET_1, BUCKET_2, BUCKET_3, BUCKET_4;

        override fun toString(): String = name.lowercase(Locale.getDefault())

        companion object {
            fun fromString(string: String?): GoalBuckets? = when (string) {
                "0", "BUCKET_0" -> BUCKET_0
                "1", "BUCKET_1" -> BUCKET_1
                "2", "BUCKET_2" -> BUCKET_2
                "3", "BUCKET_3" -> BUCKET_3
                "4", "BUCKET_4" -> BUCKET_4
                else -> null
            }
        }
    }
}
