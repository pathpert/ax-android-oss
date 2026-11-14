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
@Parcelize
@androidx.annotation.Keep
class Project private constructor(
    private val availableCardTypes: List<String>?,
    private val backersCount: Int,
    private val blurb: String,
    private val backing: Backing?,
    private val category: Category?,
    private val commentsCount: Int?,
    private val country: String, // e.g.: United States
    private val countryCode: String, // e.g.: US
    private val createdAt: DateTime,
    private val creator: User,
    private val currency: String, // e.g.: USD
    private val currencySymbol: String, // e.g.: $
    private val currentCurrency: String?, // e.g.: User's Preferred currency USD
    private val currencyTrailingCode: Boolean,
    private val displayPrelaunch: Boolean?,
    private val featuredAt: DateTime?,
    private val friends: List<User>?,
    private val fxRate: Float,
    private val deadline: DateTime?,
    private val sendMetaCapiEvents: Boolean?,
    private val sendThirdPartyEvents: Boolean?,
    private val goal: Double,
    private val id: Long, // in the Kickstarter app, this is project.pid not project.id
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
    /** Financial metrics and currency configuration sub-model. */
    fun financials() = ProjectFinancials(
        currency = this.currency,
        currencySymbol = this.currencySymbol,
        currentCurrency = this.currentCurrency,
        currencyTrailingCode = this.currencyTrailingCode,
        goal = this.goal,
        pledged = this.pledged,
        staticUsdRate = this.staticUsdRate,
        usdExchangeRate = this.usdExchangeRate,
        fxRate = this.fxRate
    )

    /** Pledge-Over-Time configuration and copy sub-model. */
    fun pledgeOverTime() = ProjectPledgeOverTime(
        isAllowed = this.isPledgeOverTimeAllowed ?: false,
        minimumExplanation = this.pledgeOverTimeMinimumExplanation ?: "",
        chargeExplanation = this.pledgeOverTimeCollectionPlanChargeExplanation ?: "",
        chargedAsNPayments = this.pledgeOverTimeCollectionPlanChargedAsNPayments ?: "",
        shortPitch = this.pledgeOverTimeCollectionPlanShortPitch ?: ""
    )

    /** Backer, community, and engagement counts sub-model. */
    fun engagementStats() = ProjectEngagementStats(
        backersCount = this.backersCount,
        commentsCount = this.commentsCount ?: 0,
        updatesCount = this.updatesCount ?: 0,
        watchesCount = this.watchesCount,
        sharesCount = this.sharesCount,
        unreadMessagesCount = this.unreadMessagesCount ?: 0,
        unseenActivityCount = this.unseenActivityCount ?: 0
    )

    /**
     * List of accepted credit card brands (e.g., "AMEX", "DISCOVER", "MASTERCARD", "VISA").
     * Negative constraint: Do not pass directly to payment processor without validating against supported payment methods.
     */
    fun availableCardTypes() = this.availableCardTypes

    /**
     * Total number of unique backers supporting this campaign.
     * Negative constraint: Do not use to determine funding goal completion; use [pledged] or [percentageFunded].
     */
    fun backersCount() = this.backersCount

    /**
     * Short promotional blurb describing the campaign (typically <= 140 characters).
     * Negative constraint: Do not confuse with the full campaign [story].
     */
    fun blurb() = this.blurb

    /**
     * The current viewer's backing domain model if authenticated and pledged, or null otherwise.
     * Negative constraint: Do not assume non-null for unauthenticated sessions.
     */
    fun backing() = this.backing

    /**
     * Primary category and optional parent category taxonomy.
     */
    fun category() = this.category

    /**
     * Total count of user and creator comments on this project.
     * Negative constraint: May be null on discovery or search summary cards.
     */
    fun commentsCount() = this.commentsCount

    /**
     * Full display name of the project's origin country (e.g., "United States").
     */
    fun country() = this.country

    /**
     * ISO 3166-1 alpha-2 two-letter country code (e.g., "US", "CA", "GB").
     */
    fun countryCode() = this.countryCode

    /**
     * Timestamp when the project draft was originally created.
     */
    fun createdAt() = this.createdAt

    /**
     * Domain model representing the project's creator user.
     */
    fun creator() = this.creator

    /**
     * Three-letter ISO 4217 project currency code (e.g., "USD", "EUR", "CAD").
     */
    fun currency() = this.currency

    /**
     * Display currency symbol for the project's native currency (e.g., "$", "£", "€").
     */
    fun currencySymbol() = this.currencySymbol

    /**
     * Three-letter ISO 4217 code of the viewer's preferred display currency, or null if matching project native currency.
     */
    fun currentCurrency() = this.currentCurrency

    /**
     * Whether the currency code should trail the formatted symbol (e.g. "10 $ CAD" vs "$10").
     */
    fun currencyTrailingCode() = this.currencyTrailingCode

    /**
     * Whether the prelaunch landing UI should be shown before campaign launch.
     */
    fun displayPrelaunch() = this.displayPrelaunch

    /**
     * Timestamp when the project was selected as a featured project or staff pick, or null.
     */
    fun featuredAt() = this.featuredAt

    /**
     * Backer friends following or backing this project. Returns empty list if null.
     */
    fun friends() = this.friends ?: emptyList()

    /**
     * FX exchange rate from project currency to USD.
     * Negative constraints:
     * - Do not use for user-facing currency display; use `KSCurrency.format`.
     * - Do not confuse with [staticUsdRate] or [usdExchangeRate].
     */
    fun fxRate() = this.fxRate

    /**
     * Campaign conclusion deadline timestamp, or null for draft/prelaunch states.
     * Negative constraint: Do not assume non-null for unlaunched projects.
     */
    fun deadline() = this.deadline

    /**
     * Funding goal in major units of the project's native currency.
     * Negative constraint: Do not pass directly across modern AX feature boundaries; convert to [com.kickstarter.ax.types.MinorCurrencyAmount].
     */
    fun goal() = this.goal

    /**
     * Persistent database identifier (PID) of the project.
     * Negative constraint: Do not confuse with GraphQL Relay global IDs (`gid://Kickstarter/Project/...`).
     */
    override fun id() = this.id

    /**
     * Whether the current authenticated user is actively backing this project.
     */
    fun isBacking() = this.isBacking

    /**
     * Whether installment pledges (Pledge Over Time) are permitted for this project.
     */
    fun isPledgeOverTimeAllowed() = this.isPledgeOverTimeAllowed

    /**
     * Whether the current authenticated user has saved/starred this project.
     */
    fun isStarred() = this.isStarred

    /**
     * Timestamp of the most recently published creator update.
     */
    fun lastUpdatePublishedAt() = this.lastUpdatePublishedAt

    /**
     * Checkout wave configuration for late pledge processing.
     */
    fun lastWave() = this.lastWave

    /**
     * Timestamp when the campaign went live.
     */
    fun launchedAt() = this.launchedAt

    /**
     * Geographic location details for the creator / project.
     */
    fun location() = this.location

    /**
     * Full project title.
     */
    fun name() = this.name

    /**
     * Integer percentage of funding goal reached (e.g. 100 for 100%).
     * Negative constraint: May exceed 100 for overfunded campaigns.
     */
    fun percentFunded() = this.percentFunded

    /**
     * Current user's administrative or creator permissions on this project.
     */
    fun permissions() = this.permissions

    /**
     * Total pledged amount in major units of the project's native currency.
     * Negative constraint: Do not pass directly across modern AX feature boundaries; convert to [com.kickstarter.ax.types.MinorCurrencyAmount].
     */
    fun pledged() = this.pledged

    /**
     * External or integrated pledge manager metadata, or null if native Kickstarter checkout.
     */
    fun pledgeManager() = this.pledgeManager

    /**
     * Primary display photo/image metadata for the campaign.
     */
    fun photo() = this.photo

    /**
     * Whether the prelaunch landing page has been published by the creator.
     */
    fun prelaunchActivated() = this.prelaunchActivated

    /**
     * System banner or administrative notice string.
     */
    fun projectNotice() = this.projectNotice

    /**
     * URL for external pledge redemption, if applicable.
     */
    fun redemptionPageUrl() = this.redemptionPageUrl

    /**
     * Whether Meta Conversions API (CAPI) tracking events are enabled.
     */
    fun sendMetaCapiEvents() = this.sendMetaCapiEvents

    /**
     * Whether third-party analytics attribution event dispatch is enabled.
     */
    fun sendThirdPartyEvents() = this.sendThirdPartyEvents

    /**
     * Discovery taxonomy tags.
     */
    fun tags() = this.tags

    /**
     * List of reward tiers and add-ons associated with this project.
     * Negative constraint: May be empty on discovery cards.
     */
    fun rewards() = this.rewards

    /**
     * URL-safe project slug (e.g., "creator-name/project-title").
     */
    fun slug() = this.slug

    /**
     * Whether the project is marked as a Kickstarter "Project We Love".
     */
    fun staffPick() = this.staffPick

    /**
     * Whether the current user has permission to post comments on this project.
     */
    fun canComment() = this.canComment

    /**
     * Explanatory text describing minimum threshold for installment pledges.
     */
    fun pledgeOverTimeMinimumExplanation() = this.pledgeOverTimeMinimumExplanation

    /**
     * Current lifecycle state string ([STATE_STARTED], [STATE_SUBMITTED], [STATE_LIVE], [STATE_SUCCESSFUL], [STATE_FAILED], [STATE_CANCELED], [STATE_SUSPENDED], [STATE_PURGED]).
     */
    fun state() = this.state

    /**
     * Timestamp when the project transitioned to its current [state].
     */
    fun stateChangedAt() = this.stateChangedAt

    /**
     * Cached static USD exchange rate refreshed on a daily schedule.
     * Negative constraint: Do not use for real-time checkout conversion; use [usdExchangeRate].
     */
    fun staticUsdRate() = this.staticUsdRate

    /**
     * Live spot USD exchange rate from payment processor.
     * Negative constraint: Do not use for static budget calculations; use [staticUsdRate].
     */
    fun usdExchangeRate() = this.usdExchangeRate

    /**
     * Number of unread direct messages from the creator for the current user.
     */
    fun unreadMessagesCount() = this.unreadMessagesCount ?: 0

    /**
     * Number of unseen activity notifications on this project.
     */
    fun unseenActivityCount() = this.unseenActivityCount ?: 0

    /**
     * Number of creator updates published to date.
     */
    fun updatesCount() = this.updatesCount

    /**
     * Timestamp of the most recent project update/modification.
     */
    fun updatedAt() = this.updatedAt

    /**
     * Navigation URLs (web, API, mobile) associated with this project.
     */
    fun urls() = this.urls

    /**
     * Video pitch metadata, or null if no campaign video is present.
     */
    fun video() = this.video

    /**
     * List of creator-submitted FAQ question-and-answer pairs.
     */
    fun projectFaqs() = this.projectFaqs

    /**
     * List of environmental commitment declarations by the creator.
     */
    fun envCommitments() = this.envCommitments

    /**
     * Generative AI disclosure statement and usage declaration.
     */
    fun aiDisclosure() = this.aiDisclosure

    /**
     * Creator-submitted statement of challenges and risks.
     */
    fun risks() = this.risks

    /**
     * Full HTML/rich-text campaign story content.
     */
    fun story() = this.story

    /**
     * Whether this project has been flagged by the viewer or moderation system.
     */
    fun isFlagged() = this.isFlagged

    /**
     * Total number of users tracking or watching this project.
     */
    fun watchesCount() = this.watchesCount

    /**
     * Total number of social shares recorded for this project.
     */
    fun sharesCount() = this.sharesCount

    /**
     * Whether the project is currently in the post-campaign late pledging phase.
     */
    fun isInPostCampaignPledgingPhase() = this.isInPostCampaignPledgingPhase

    /**
     * Whether late pledging has been enabled by the creator.
     */
    fun postCampaignPledgingEnabled() = this.postCampaignPledgingEnabled

    /**
     * Explanatory text for installment pledge collection schedule.
     */
    fun pledgeOverTimeCollectionPlanChargeExplanation() =
        this.pledgeOverTimeCollectionPlanChargeExplanation

    /**
     * Formatted string specifying payment installments (e.g., "Charged as 4 payments").
     */
    fun pledgeOverTimeCollectionPlanChargedAsNPayments() =
        this.pledgeOverTimeCollectionPlanChargedAsNPayments

    /**
     * Short promotional summary of installment payment plan.
     */
    fun pledgeOverTimeCollectionPlanShortPitch() = this.pledgeOverTimeCollectionPlanShortPitch

    @Parcelize
    data class Builder(
        private var availableCardTypes: List<String>? = null,
        private var backersCount: Int = 0,
        private var blurb: String = "",
        private var backing: Backing? = null,
        private var category: Category? = null,
        private var commentsCount: Int? = null,
        private var country: String = "",
        private var countryCode: String = "",
        private var createdAt: DateTime = DateTime.now(),
        private var creator: User = User.builder().build(),
        private var currency: String = "",
        private var currencySymbol: String = "",
        private var currentCurrency: String? = null,
        private var currencyTrailingCode: Boolean = false,
        private var displayPrelaunch: Boolean? = null,
        private var featuredAt: DateTime? = null,
        private var friends: List<User>? = emptyList(),
        private var fxRate: Float = 0f,
        private var deadline: DateTime? = null,
        private var goal: Double = 0.0,
        private var id: Long = 0L,
        private var isBacking: Boolean = false,
        private var isPledgeOverTimeAllowed: Boolean? = null,
        private var isStarred: Boolean = false,
        private var lastUpdatePublishedAt: DateTime? = null,
        private var lastWave: CheckoutWave? = null,
        private var launchedAt: DateTime? = null,
        private var location: Location? = null,
        private var name: String = "",
        private var percentFunded: Int? = null,
        private var permissions: List<Permission?>? = null,
        private var pledged: Double = 0.0,
        private var pledgeManager: PledgeManager? = null,
        private var photo: Photo? = null,
        private var prelaunchActivated: Boolean? = null,
        private var projectNotice: String? = null,
        private var redemptionPageUrl: String? = null,
        private var sendMetaCapiEvents: Boolean? = null,
        private var sendThirdPartyEvents: Boolean? = null,
        private var tags: List<String>? = emptyList(),
        private var rewards: List<Reward>? = emptyList(),
        private var slug: String? = null,
        private var staffPick: Boolean? = null,
        private var canComment: Boolean? = null,
        private var pledgeOverTimeMinimumExplanation: String? = "",
        private var pledgeOverTimeCollectionPlanChargeExplanation: String? = null,
        private var pledgeOverTimeCollectionPlanChargedAsNPayments: String? = null,
        private var pledgeOverTimeCollectionPlanShortPitch: String? = null,
        @State
        private var state: String = STATE_STARTED,
        private var stateChangedAt: DateTime? = null,
        private var staticUsdRate: Float = 0f,
        private var usdExchangeRate: Float = 0f,
        private var unreadMessagesCount: Int? = null,
        private var unseenActivityCount: Int? = null,
        private var updatesCount: Int? = null,
        private var updatedAt: DateTime? = null,
        private var urls: Urls = Urls.builder().build(),
        private var video: Video? = null,
        private var projectFaqs: List<ProjectFaq>? = emptyList(),
        private var envCommitments: List<EnvironmentalCommitment>? = emptyList(),
        private var aiDisclosure: AiDisclosure? = null,
        private var risks: String? = "",
        private var story: String? = "",
        private var isFlagged: Boolean? = null,
        private var watchesCount: Int = 0,
        private var sharesCount: Int = 0,
        private var isInPostCampaignPledgingPhase: Boolean? = null,
        private var postCampaignPledgingEnabled: Boolean? = null,
    ) : Parcelable {
        fun availableCardTypes(availableCardTypes: List<String>?) =
            apply { this.availableCardTypes = availableCardTypes }

        fun backersCount(backersCount: Int?) = apply { this.backersCount = backersCount ?: 0 }
        fun watchesCount(watchesCount: Int?) = apply { this.watchesCount = watchesCount ?: 0 }
        fun sharesCount(sharesCount: Int?) = apply { this.sharesCount = sharesCount ?: 0 }
        fun blurb(blurb: String?) = apply { this.blurb = blurb ?: "" }
        fun backing(backing: Backing?) = apply { this.backing = backing }
        fun category(category: Category?) = apply { this.category = category }
        fun commentsCount(commentsCount: Int?) = apply { this.commentsCount = commentsCount }
        fun country(country: String?) = apply { this.country = country ?: "" }
        fun countryCode(countryCode: String?) = apply { this.countryCode = countryCode ?: "" }
        fun createdAt(createdAt: DateTime?) = apply { createdAt?.let { this.createdAt = it } }
        fun creator(creator: User?) = apply { creator?.let { this.creator = it } }
        fun currency(currency: String?) = apply { currency?.let { this.currency = it } }
        fun currencySymbol(currencySymbol: String?) =
            apply { currencySymbol?.let { this.currencySymbol = it } }

        fun currentCurrency(currency: String?) = apply { this.currentCurrency = currency }
        fun currencyTrailingCode(currencyTrailingCode: Boolean?) =
            apply { this.currencyTrailingCode = currencyTrailingCode ?: false }

        fun displayPrelaunch(displayPrelaunch: Boolean?) =
            apply { this.displayPrelaunch = displayPrelaunch }

        fun canComment(canComment: Boolean?) = apply { this.canComment = canComment ?: false }
        fun pledgeOverTimeMinimumExplanation(pledgeOverTimeMinimumExplanation: String?) =
            apply { this.pledgeOverTimeMinimumExplanation = pledgeOverTimeMinimumExplanation }

        fun deadline(deadline: DateTime?) = apply { this.deadline = deadline }
        fun featuredAt(featuredAt: DateTime?) = apply { this.featuredAt = featuredAt }
        fun friends(friends: List<User>?) = apply { this.friends = friends ?: emptyList() }
        fun fxRate(fxRate: Float?) = apply { this.fxRate = fxRate ?: 0f }
        fun goal(goal: Double?) = apply { this.goal = goal ?: 0.0 }
        fun id(id: Long?) = apply { this.id = id ?: 0L }
        fun isBacking(isBacking: Boolean?) = apply { this.isBacking = isBacking ?: false }
        fun isPledgeOverTimeAllowed(isPledgeOverTimeAllowed: Boolean?) =
            apply { this.isPledgeOverTimeAllowed = isPledgeOverTimeAllowed }

        fun isStarred(isStarred: Boolean?) = apply { this.isStarred = isStarred ?: false }
        fun lastUpdatePublishedAt(lastUpdatePublishedAt: DateTime?) =
            apply { this.lastUpdatePublishedAt = lastUpdatePublishedAt }
        fun lastWave(lastWave: CheckoutWave?) = apply { this.lastWave = lastWave }
        fun launchedAt(launchedAt: DateTime?) = apply { this.launchedAt = launchedAt }
        fun location(location: Location?) = apply { this.location = location }
        fun name(name: String?) = apply { this.name = name ?: "" }
        fun percentFunded(percentFunded: Int?) = apply { this.percentFunded = percentFunded }
        fun permissions(permissions: List<Permission?>?) =
            apply { this.permissions = permissions?.filterNotNull() ?: emptyList() }

        fun pledged(pledged: Double?) = apply { this.pledged = pledged ?: 0.0 }
        fun pledgeManager(pledgeManager: PledgeManager?) = apply { this.pledgeManager = pledgeManager }
        fun photo(photo: Photo?) = apply { this.photo = photo }
        fun prelaunchActivated(prelaunchActivated: Boolean?) =
            apply { this.prelaunchActivated = prelaunchActivated }

        fun projectNotice(projectNotice: String?) = apply { this.projectNotice = projectNotice }
        fun redemptionPageUrl(redemptionPageUrl: String?) = apply { this.redemptionPageUrl = redemptionPageUrl }
        fun sendMetaCapiEvents(sendMetaCapiEvents: Boolean?) =
            apply { this.sendMetaCapiEvents = sendMetaCapiEvents }

        fun sendThirdPartyEvents(sendThirdPartyEvents: Boolean?) =
            apply { this.sendThirdPartyEvents = sendThirdPartyEvents }

        fun tags(tags: List<String>?) = apply { this.tags = tags ?: emptyList() }
        fun rewards(rewards: List<Reward>?) = apply { this.rewards = rewards ?: emptyList() }
        fun slug(slug: String?) = apply { this.slug = slug }
        fun staffPick(staffPick: Boolean?) = apply { this.staffPick = staffPick }
        fun staticUsdRate(staticUsdRate: Float?) =
            apply { this.staticUsdRate = staticUsdRate ?: 0f }

        fun usdExchangeRate(usdExchangeRate: Float?) =
            apply { this.usdExchangeRate = usdExchangeRate ?: 0f }

        fun state(state: String?) = apply { this.state = state ?: "" }
        fun stateChangedAt(stateChangedAt: DateTime?) =
            apply { this.stateChangedAt = stateChangedAt }

        fun unreadMessagesCount(unreadMessagesCount: Int?) =
            apply { this.unreadMessagesCount = unreadMessagesCount ?: 0 }

        fun unseenActivityCount(unseenActivityCount: Int?) =
            apply { this.unseenActivityCount = unseenActivityCount ?: 0 }

        fun updatedAt(updatedAt: DateTime?) = apply { this.updatedAt = updatedAt }
        fun updatesCount(updatesCount: Int?) = apply { this.updatesCount = updatesCount ?: 0 }
        fun urls(urls: Urls?) = apply { urls?.let { this.urls = it } }
        fun video(video: Video?) = apply { this.video = video }
        fun projectFaqs(projectFaqs: List<ProjectFaq>?) =
            apply { this.projectFaqs = projectFaqs ?: emptyList() }

        fun envCommitments(envCommitments: List<EnvironmentalCommitment>?) =
            apply { this.envCommitments = envCommitments ?: emptyList() }

        fun aiDisclosure(aiDisclosure: AiDisclosure?) = apply { this.aiDisclosure = aiDisclosure }
        fun risks(risks: String?) = apply { this.risks = risks ?: "" }
        fun story(story: String?) = apply { this.story = story ?: "" }
        fun isFlagged(isFlagged: Boolean?) = apply { this.isFlagged = isFlagged }
        fun isInPostCampaignPledgingPhase(isInPostCampaignPledgingPhase: Boolean?) =
            apply { this.isInPostCampaignPledgingPhase = isInPostCampaignPledgingPhase }

        fun postCampaignPledgingEnabled(postCampaignPledgingEnabled: Boolean?) =
            apply { this.postCampaignPledgingEnabled = postCampaignPledgingEnabled }

        fun pledgeOverTimeCollectionPlanChargeExplanation(
            pledgeOverTimeCollectionPlanChargeExplanation: String?,
        ) = apply {
            this.pledgeOverTimeCollectionPlanChargeExplanation =
                pledgeOverTimeCollectionPlanChargeExplanation
        }

        fun pledgeOverTimeCollectionPlanChargedAsNPayments(
            pledgeOverTimeCollectionPlanChargedAsNPayments: String?,
        ) = apply {
            this.pledgeOverTimeCollectionPlanChargedAsNPayments =
                pledgeOverTimeCollectionPlanChargedAsNPayments
        }

        fun pledgeOverTimeCollectionPlanShortPitch(pledgeOverTimeCollectionPlanShortPitch: String?) =
            apply {
                this.pledgeOverTimeCollectionPlanShortPitch = pledgeOverTimeCollectionPlanShortPitch
            }

        fun financials(fin: ProjectFinancials) = apply {
            currency(fin.currency)
            currencySymbol(fin.currencySymbol)
            currentCurrency(fin.currentCurrency)
            currencyTrailingCode(fin.currencyTrailingCode)
            goal(fin.goal)
            pledged(fin.pledged)
            staticUsdRate(fin.staticUsdRate)
            usdExchangeRate(fin.usdExchangeRate)
            fxRate(fin.fxRate)
        }

        fun pledgeOverTime(pot: ProjectPledgeOverTime) = apply {
            isPledgeOverTimeAllowed(pot.isAllowed)
            pledgeOverTimeMinimumExplanation(pot.minimumExplanation)
            pledgeOverTimeCollectionPlanChargeExplanation(pot.chargeExplanation)
            pledgeOverTimeCollectionPlanChargedAsNPayments(pot.chargedAsNPayments)
            pledgeOverTimeCollectionPlanShortPitch(pot.shortPitch)
        }

        fun engagementStats(stats: ProjectEngagementStats) = apply {
            backersCount(stats.backersCount)
            commentsCount(stats.commentsCount)
            updatesCount(stats.updatesCount)
            watchesCount(stats.watchesCount)
            sharesCount(stats.sharesCount)
            unreadMessagesCount(stats.unreadMessagesCount)
            unseenActivityCount(stats.unseenActivityCount)
        }

        fun build() = Project(
            availableCardTypes = availableCardTypes,
            backersCount = backersCount,
            blurb = blurb,
            backing = backing,
            category = category,
            commentsCount = commentsCount,
            country = country,
            countryCode = countryCode,
            createdAt = createdAt,
            creator = creator,
            currency = currency,
            currencySymbol = currencySymbol,
            currentCurrency = currentCurrency,
            currencyTrailingCode = currencyTrailingCode,
            displayPrelaunch = displayPrelaunch,
            featuredAt = featuredAt,
            friends = friends,
            fxRate = fxRate,
            deadline = deadline,
            goal = goal,
            id = id,
            isBacking = isBacking,
            isPledgeOverTimeAllowed = isPledgeOverTimeAllowed,
            isStarred = isStarred,
            lastUpdatePublishedAt = lastUpdatePublishedAt,
            lastWave = lastWave,
            launchedAt = launchedAt,
            location = location,
            name = name,
            percentFunded = percentFunded,
            permissions = permissions,
            pledged = pledged,
            pledgeManager = pledgeManager,
            photo = photo,
            prelaunchActivated = prelaunchActivated,
            projectNotice = projectNotice,
            redemptionPageUrl = redemptionPageUrl,
            sendMetaCapiEvents = sendMetaCapiEvents,
            sendThirdPartyEvents = sendThirdPartyEvents,
            tags = tags,
            rewards = rewards,
            slug = slug,
            staffPick = staffPick,
            canComment = canComment,
            pledgeOverTimeMinimumExplanation = pledgeOverTimeMinimumExplanation,
            state = state,
            stateChangedAt = stateChangedAt,
            staticUsdRate = staticUsdRate,
            usdExchangeRate = usdExchangeRate,
            unreadMessagesCount = unreadMessagesCount,
            unseenActivityCount = unseenActivityCount,
            updatesCount = updatesCount,
            updatedAt = updatedAt,
            urls = urls,
            video = video,
            projectFaqs = projectFaqs,
            envCommitments = envCommitments,
            aiDisclosure = aiDisclosure,
            risks = risks,
            story = story,
            isFlagged = isFlagged,
            watchesCount = watchesCount,
            sharesCount = sharesCount,
            isInPostCampaignPledgingPhase = isInPostCampaignPledgingPhase,
            postCampaignPledgingEnabled = postCampaignPledgingEnabled,
            pledgeOverTimeCollectionPlanShortPitch = pledgeOverTimeCollectionPlanShortPitch,
            pledgeOverTimeCollectionPlanChargedAsNPayments = pledgeOverTimeCollectionPlanChargedAsNPayments,
            pledgeOverTimeCollectionPlanChargeExplanation = pledgeOverTimeCollectionPlanChargeExplanation
        )
    }

    fun toBuilder() = Builder(
        availableCardTypes = availableCardTypes,
        backersCount = backersCount,
        blurb = blurb,
        backing = backing,
        category = category,
        commentsCount = commentsCount,
        country = country,
        countryCode = countryCode,
        createdAt = createdAt,
        creator = creator,
        currency = currency,
        currencySymbol = currencySymbol,
        currentCurrency = currentCurrency,
        currencyTrailingCode = currencyTrailingCode,
        displayPrelaunch = displayPrelaunch,
        featuredAt = featuredAt,
        friends = friends,
        fxRate = fxRate,
        deadline = deadline,
        goal = goal,
        id = id,
        isBacking = isBacking,
        isPledgeOverTimeAllowed = isPledgeOverTimeAllowed,
        isStarred = isStarred,
        lastUpdatePublishedAt = lastUpdatePublishedAt,
        lastWave = lastWave,
        launchedAt = launchedAt,
        location = location,
        name = name,
        percentFunded = percentFunded,
        permissions = permissions,
        pledged = pledged,
        pledgeManager = pledgeManager,
        photo = photo,
        prelaunchActivated = prelaunchActivated,
        projectNotice = projectNotice,
        redemptionPageUrl = redemptionPageUrl,
        sendMetaCapiEvents = sendMetaCapiEvents,
        sendThirdPartyEvents = sendThirdPartyEvents,
        tags = tags,
        rewards = rewards,
        slug = slug,
        staffPick = staffPick,
        canComment = canComment,
        pledgeOverTimeMinimumExplanation = pledgeOverTimeMinimumExplanation,
        state = state,
        stateChangedAt = stateChangedAt,
        staticUsdRate = staticUsdRate,
        usdExchangeRate = usdExchangeRate,
        unreadMessagesCount = unreadMessagesCount,
        unseenActivityCount = unseenActivityCount,
        updatesCount = updatesCount,
        updatedAt = updatedAt,
        urls = urls,
        video = video,
        projectFaqs = projectFaqs,
        envCommitments = envCommitments,
        aiDisclosure = aiDisclosure,
        risks = risks,
        story = story,
        isFlagged = isFlagged,
        watchesCount = watchesCount,
        sharesCount = sharesCount,
        isInPostCampaignPledgingPhase = isInPostCampaignPledgingPhase,
        postCampaignPledgingEnabled = postCampaignPledgingEnabled,
        pledgeOverTimeCollectionPlanShortPitch = pledgeOverTimeCollectionPlanShortPitch,
        pledgeOverTimeCollectionPlanChargedAsNPayments = pledgeOverTimeCollectionPlanChargedAsNPayments,
        pledgeOverTimeCollectionPlanChargeExplanation = pledgeOverTimeCollectionPlanChargeExplanation
    )

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

    /** Web URL for the creator biography page. */
    fun creatorBioUrl() = urls().web().creatorBio()

    /** Web URL for the campaign description. */
    fun descriptionUrl() = urls().web().description()

    /** Web URL for the campaign updates feed. Returns empty string if unavailable. */
    fun updatesUrl() = urls().web().updates() ?: ""

    /** Canonical public web URL for this project. */
    fun webProjectUrl() = urls().web().project()

    /** Returns whether this project has at least one published comment. */
    fun hasComments() = commentsCount().isNonZero()

    /** Returns whether reward tiers are available for this project. */
    fun hasRewards() = rewards()?.isNotEmpty() ?: false

    /** Returns whether a campaign pitch video is present. */
    fun hasVideo() = video() != null

    /** Returns whether the project is in a canceled state. */
    val isCanceled: Boolean
        get() = STATE_CANCELED == state()

    /** Returns whether the project is in a failed state. */
    val isFailed: Boolean
        get() = STATE_FAILED == state()

    /** Returns whether this project was selected as a featured project today. */
    val isFeaturedToday: Boolean
        get() = featuredAt()?.let {
            DateTimeUtils.isDateToday(it)
        } ?: false

    /** Returns whether the project is currently live and accepting pledges. */
    val isLive: Boolean
        get() = STATE_LIVE == state()

    /** Returns whether any connected friends are backing this project. */
    val isFriendBacking: Boolean
        get() = friends().isNotEmpty()

    /** Returns whether this project is live and has achieved 100% or more of its funding goal. */
    val isFunded: Boolean
        get() = isLive && percentageFunded() >= 100

    /** Returns whether the project is in a purged state. */
    val isPurged: Boolean
        get() = STATE_PURGED == state()

    /** Returns whether the project draft has been started but not yet submitted. */
    val isStarted: Boolean
        get() = STATE_STARTED == state()

    /** Returns whether the project has been submitted for review. */
    val isSubmitted: Boolean
        get() = STATE_SUBMITTED == state()

    /** Returns whether the project is in a suspended state. */
    val isSuspended: Boolean
        get() = STATE_SUSPENDED == state()

    /** Returns whether the campaign ended successfully with full funding. */
    val isSuccessful: Boolean
        get() = STATE_SUCCESSFUL == state()

    /** Returns whether the campaign deadline is within the next 48 hours and not expired. */
    val isApproachingDeadline: Boolean
        get() = deadline()?.let {
            if (it.isBeforeNow) false
            else it.isBefore(DateTime().plusDays(2))
        } ?: false

    /**
     * Percentage of funding goal achieved (0.0 to 100.0+).
     * Negative constraint: Returns 0.0f when [goal] is zero.
     */
    fun percentageFunded(): Float {
        return if (goal() > 0.0f) {
            pledged().toFloat() / goal().toFloat() * 100.0f
        } else 0.0f
    }

    /**
     * Routing parameter for project endpoints: prefers [slug] if present, falling back to string [id].
     */
    fun param(): String {
        val slug = slug()
        return slug ?: id().toString()
    }

    /** Canonical HTTPS web project URL. */
    fun secureWebProjectUrl(): String {
        // TODO: Just use http with local env
        return Uri.parse(webProjectUrl()).buildUpon().scheme("https").build().toString()
    }

    /** Web URL for creating a new pledge on this project. */
    fun newPledgeUrl(): String {
        return Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/new")
            .toString()
    }

    /** Web URL for modifying an existing pledge on this project. */
    fun editPledgeUrl(): String {
        return Uri.parse(secureWebProjectUrl()).buildUpon().appendEncodedPath("pledge/edit")
            .toString()
    }

    override fun toString(): String {
        return (
            "Project{" +
                "id=" + id() + ", " +
                "name=" + name() + ", " +
                "}"
            )
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Project) {
            equals = backersCount() == other.backersCount() &&
                availableCardTypes() == other.availableCardTypes() &&
                blurb() == other.blurb() &&
                backing() == other.backing() &&
                category() == other.category() &&
                commentsCount() == other.commentsCount() &&
                country() == other.country() &&
                countryCode() == other.countryCode() &&
                createdAt() == other.createdAt() &&
                creator() == other.creator() &&
                createdAt() == other.createdAt() &&
                currency() == other.currency() &&
                currencySymbol() == other.currencySymbol() &&
                currentCurrency() == other.currentCurrency() &&
                currencyTrailingCode() == other.currencyTrailingCode() &&
                displayPrelaunch() == other.displayPrelaunch() &&
                featuredAt() == other.featuredAt() &&
                friends() == other.friends() &&
                deadline() == other.deadline() &&
                fxRate() == other.fxRate() &&
                goal() == other.goal() &&
                id() == other.id() &&
                isBacking() == other.isBacking() &&
                isPledgeOverTimeAllowed() == other.isPledgeOverTimeAllowed() &&
                isStarred() == other.isStarred() &&
                lastUpdatePublishedAt() == other.lastUpdatePublishedAt() &&
                lastWave() == other.lastWave() &&
                launchedAt() == other.launchedAt() &&
                location() == other.location() &&
                name() == other.name() &&
                percentFunded() == other.percentFunded() &&
                permissions() == other.permissions() &&
                pledged() == other.pledged() &&
                pledgeManager() == other.pledgeManager() &&
                photo() == other.photo() &&
                prelaunchActivated() == other.prelaunchActivated() &&
                projectNotice() == other.projectNotice() &&
                sendMetaCapiEvents() == other.sendMetaCapiEvents() &&
                sendThirdPartyEvents() == other.sendThirdPartyEvents() &&
                redemptionPageUrl() == other.redemptionPageUrl() &&
                rewards() == other.rewards() &&
                slug() == other.slug() &&
                staffPick() == other.staffPick() &&
                slug() == other.slug() &&
                canComment() == other.canComment() &&
                pledgeOverTimeMinimumExplanation() == other.pledgeOverTimeMinimumExplanation() &&
                state() == other.state() &&
                stateChangedAt() == other.stateChangedAt() &&
                staticUsdRate() == other.staticUsdRate() &&
                usdExchangeRate() == other.usdExchangeRate() &&
                unreadMessagesCount() == other.unreadMessagesCount() &&
                unseenActivityCount() == other.unseenActivityCount() &&
                updatesCount() == other.updatesCount() &&
                updatedAt() == other.updatedAt() &&
                urls() == other.urls() &&
                video() == other.video() &&
                projectFaqs() == other.projectFaqs() &&
                envCommitments() == other.envCommitments() &&
                aiDisclosure() == other.aiDisclosure() &&
                risks() == other.risks() &&
                story() == other.story() &&
                isFlagged() == other.isFlagged() &&
                sharesCount() == other.sharesCount() &&
                pledgeOverTimeCollectionPlanShortPitch() == other.pledgeOverTimeCollectionPlanShortPitch() &&
                pledgeOverTimeCollectionPlanChargedAsNPayments() == other.pledgeOverTimeCollectionPlanChargedAsNPayments() &&
                pledgeOverTimeCollectionPlanChargeExplanation() == other.pledgeOverTimeCollectionPlanChargeExplanation()
        }
        return equals
    }

    override fun hashCode(): Int {
        return id().toInt()
    }

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
