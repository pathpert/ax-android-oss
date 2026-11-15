package com.kickstarter.models

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.StringDef
import com.kickstarter.libs.Permission
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.isNonZero
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

/**
 * Immutable domain model representing a Kickstarter crowdfunding project.
 *
 * Negative constraints:
 * - Do not pass raw floating-point [goal] or [pledged] currency values directly across modern feature boundaries;
 *   convert to [com.kickstarter.ax.types.MinorCurrencyAmount] at the boundary.
 * - Do not confuse the integer [id] (Kickstarter database PID) with GraphQL Relay global IDs.
 * - Do not confuse FX rate variants: [fxRate] is the project-to-USD conversion rate, [staticUsdRate] is the cached
 *   daily rate, and [usdExchangeRate] is the spot market rate.
 */
@Deprecated("Legacy monolithic domain model. For new code, consume feature-scoped contracts in com.kickstarter.features.*")
@Parcelize
@androidx.annotation.Keep
class Project internal constructor(
    private val availableCardTypes: List<String>?,
    private val backersCount: Int,
    private val blurb: String,
    private val backing: Backing?,
    private val category: Category?,
    private val commentsCount: Int?,
    private val country: String,
    private val countryCode: String,
    private val createdAt: DateTime,
    private val creator: User,
    private val currency: String,
    private val currencySymbol: String,
    private val currentCurrency: String?,
    private val currencyTrailingCode: Boolean,
    private val displayPrelaunch: Boolean?,
    private val featuredAt: DateTime?,
    private val friends: List<User>?,
    private val fxRate: Float,
    private val deadline: DateTime?,
    private val sendMetaCapiEvents: Boolean?,
    private val sendThirdPartyEvents: Boolean?,
    private val goal: Double,
    private val id: Long,
    private val isBacking: Boolean,
    private val isPledgeOverTimeAllowed: Boolean?,
    private val isStarred: Boolean,
    private val lastUpdatePublishedAt: DateTime?,
    private val lastWave: CheckoutWave?,
    private val launchedAt: DateTime?,
    private val location: Location?,
    private val name: String,
    private val percentFunded: Int?,
    private val permissions: List<Permission?>?,
    private val pledged: Double,
    private val pledgeManager: PledgeManager?,
    private val photo: Photo?,
    private val prelaunchActivated: Boolean?,
    private val projectNotice: String?,
    private val redemptionPageUrl: String?,
    private val tags: List<String>?,
    private val rewards: List<Reward>?,
    private val slug: String?,
    private val staffPick: Boolean?,
    private val canComment: Boolean?,
    private val pledgeOverTimeMinimumExplanation: String?,
    private val pledgeOverTimeCollectionPlanChargeExplanation: String?,
    private val pledgeOverTimeCollectionPlanChargedAsNPayments: String?,
    private val pledgeOverTimeCollectionPlanShortPitch: String?,
    @State
    private val state: String,
    private val stateChangedAt: DateTime?,
    private val staticUsdRate: Float,
    private val usdExchangeRate: Float,
    private val unreadMessagesCount: Int?,
    private val unseenActivityCount: Int?,
    private val updatesCount: Int?,
    private val updatedAt: DateTime?,
    private val urls: Urls,
    private val video: Video?,
    private val projectFaqs: List<ProjectFaq>?,
    private val envCommitments: List<EnvironmentalCommitment>?,
    private val aiDisclosure: AiDisclosure?,
    private val risks: String?,
    private val story: String?,
    private val isFlagged: Boolean?,
    private val watchesCount: Int,
    private val sharesCount: Int,
    private val isInPostCampaignPledgingPhase: Boolean? = null,
    private val postCampaignPledgingEnabled: Boolean? = null
) : Parcelable, Relay {

    fun availableCardTypes() = this.availableCardTypes
    fun backersCount() = this.backersCount
    fun blurb() = this.blurb
    fun backing() = this.backing
    fun category() = this.category
    fun commentsCount() = this.commentsCount
    fun country() = this.country
    fun countryCode() = this.countryCode
    fun createdAt() = this.createdAt
    fun creator() = this.creator
    fun currency() = this.currency
    fun currencySymbol() = this.currencySymbol
    fun currentCurrency() = this.currentCurrency
    fun currencyTrailingCode() = this.currencyTrailingCode
    fun displayPrelaunch() = this.displayPrelaunch
    fun featuredAt() = this.featuredAt
    fun friends() = this.friends ?: emptyList()
    fun fxRate() = this.fxRate
    fun deadline() = this.deadline
    fun goal() = this.goal
    override fun id() = this.id
    fun isBacking() = this.isBacking
    fun isPledgeOverTimeAllowed() = this.isPledgeOverTimeAllowed
    fun isStarred() = this.isStarred
    fun lastUpdatePublishedAt() = this.lastUpdatePublishedAt
    fun lastWave() = this.lastWave
    fun launchedAt() = this.launchedAt
    fun location() = this.location
    fun name() = this.name
    fun percentFunded() = this.percentFunded
    fun permissions() = this.permissions
    fun pledged() = this.pledged
    fun pledgeManager() = this.pledgeManager
    fun photo() = this.photo
    fun prelaunchActivated() = this.prelaunchActivated
    fun projectNotice() = this.projectNotice
    fun redemptionPageUrl() = this.redemptionPageUrl
    fun sendMetaCapiEvents() = this.sendMetaCapiEvents
    fun sendThirdPartyEvents() = this.sendThirdPartyEvents
    fun tags() = this.tags
    fun rewards() = this.rewards
    fun slug() = this.slug
    fun staffPick() = this.staffPick
    fun canComment() = this.canComment
    fun pledgeOverTimeMinimumExplanation() = this.pledgeOverTimeMinimumExplanation
    fun state() = this.state
    fun stateChangedAt() = this.stateChangedAt
    fun staticUsdRate() = this.staticUsdRate
    fun usdExchangeRate() = this.usdExchangeRate
    fun unreadMessagesCount() = this.unreadMessagesCount ?: 0
    fun unseenActivityCount() = this.unseenActivityCount ?: 0
    fun updatesCount() = this.updatesCount
    fun updatedAt() = this.updatedAt
    fun urls() = this.urls
    fun video() = this.video
    fun projectFaqs() = this.projectFaqs
    fun envCommitments() = this.envCommitments
    fun aiDisclosure() = this.aiDisclosure
    fun risks() = this.risks
    fun story() = this.story
    fun isFlagged() = this.isFlagged
    fun watchesCount() = this.watchesCount
    fun sharesCount() = this.sharesCount
    fun isInPostCampaignPledgingPhase() = this.isInPostCampaignPledgingPhase
    fun postCampaignPledgingEnabled() = this.postCampaignPledgingEnabled
    fun pledgeOverTimeCollectionPlanChargeExplanation() = this.pledgeOverTimeCollectionPlanChargeExplanation
    fun pledgeOverTimeCollectionPlanChargedAsNPayments() = this.pledgeOverTimeCollectionPlanChargedAsNPayments
    fun pledgeOverTimeCollectionPlanShortPitch() = this.pledgeOverTimeCollectionPlanShortPitch

    fun financials() = ProjectFinancials(
        currency = currency(),
        currencySymbol = currencySymbol(),
        currentCurrency = currentCurrency(),
        currencyTrailingCode = currencyTrailingCode(),
        goal = goal(),
        pledged = pledged(),
        staticUsdRate = staticUsdRate(),
        usdExchangeRate = usdExchangeRate(),
        fxRate = fxRate()
    )

    fun pledgeOverTime() = ProjectPledgeOverTime(
        isAllowed = isPledgeOverTimeAllowed() ?: false,
        minimumExplanation = pledgeOverTimeMinimumExplanation() ?: "",
        chargeExplanation = pledgeOverTimeCollectionPlanChargeExplanation() ?: "",
        chargedAsNPayments = pledgeOverTimeCollectionPlanChargedAsNPayments() ?: "",
        shortPitch = pledgeOverTimeCollectionPlanShortPitch() ?: ""
    )

    fun engagementStats() = ProjectEngagementStats(
        backersCount = backersCount(),
        commentsCount = commentsCount() ?: 0,
        updatesCount = updatesCount() ?: 0,
        watchesCount = watchesCount(),
        sharesCount = sharesCount(),
        unreadMessagesCount = unreadMessagesCount(),
        unseenActivityCount = unseenActivityCount()
    )

    fun creatorBioUrl(): String = urls().web().creatorBio()
    fun descriptionUrl(): String = urls().web().description()
    fun updatesUrl(): String = urls().web().updates() ?: ""
    fun webProjectUrl(): String = urls().web().project()
    fun secureWebProjectUrl(): String = Uri.parse(webProjectUrl()).buildUpon().scheme("https").build().toString()
    fun newPledgeUrl(): String = Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/new").toString()
    fun editPledgeUrl(): String = Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/edit").toString()

    fun hasComments(): Boolean = commentsCount().isNonZero()
    fun hasRewards(): Boolean = rewards()?.isNotEmpty() ?: false
    fun hasVideo(): Boolean = video() != null

    val isCanceled: Boolean get() = STATE_CANCELED == state()
    val isFailed: Boolean get() = STATE_FAILED == state()
    val isFeaturedToday: Boolean get() = featuredAt()?.let { DateTimeUtils.isDateToday(it) } ?: false
    val isLive: Boolean get() = STATE_LIVE == state()
    val isFriendBacking: Boolean get() = friends().isNotEmpty()
    val isFunded: Boolean get() = isLive && percentageFunded() >= 100
    val isPurged: Boolean get() = STATE_PURGED == state()
    val isStarted: Boolean get() = STATE_STARTED == state()
    val isSubmitted: Boolean get() = STATE_SUBMITTED == state()
    val isSuspended: Boolean get() = STATE_SUSPENDED == state()
    val isSuccessful: Boolean get() = STATE_SUCCESSFUL == state()
    val isApproachingDeadline: Boolean get() = deadline()?.let {
        if (it.isBeforeNow) false else it.isBefore(DateTime().plusDays(2))
    } ?: false

    fun percentageFunded(): Float = if (goal() > 0.0) (pledged() / goal() * 100.0).toFloat() else 0.0f

    class Builder : BaseProjectBuilder<Builder>()

    fun toBuilder(): Builder = ProjectBuilderHelper.toBuilder(this)

    fun param(): String = slug() ?: id().toString()

    override fun toString(): String = "Project{id=${id()}, name=${name()}}"

    override fun equals(other: Any?): Boolean = ProjectEquality.areEqual(this, other)

    override fun hashCode(): Int = id().toInt()

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(
        STATE_STARTED,
        STATE_SUBMITTED,
        STATE_LIVE,
        STATE_SUCCESSFUL,
        STATE_FAILED,
        STATE_CANCELED,
        STATE_SUSPENDED,
        STATE_PURGED
    )
    annotation class State

    companion object {
        @JvmStatic
        fun builder() = Builder()

        const val STATE_STARTED = "started"
        const val STATE_SUBMITTED = "submitted"
        const val STATE_LIVE = "live"
        const val STATE_SUCCESSFUL = "successful"
        const val STATE_FAILED = "failed"
        const val STATE_CANCELED = "canceled"
        const val STATE_SUSPENDED = "suspended"
        const val STATE_PURGED = "purged"
    }
}
