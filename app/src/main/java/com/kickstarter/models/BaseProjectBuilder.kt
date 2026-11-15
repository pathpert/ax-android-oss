package com.kickstarter.models

import com.kickstarter.libs.Permission
import org.joda.time.DateTime

/**
 * Domain data model representing BaseProjectBuilder.
 *
 * Negative constraints:
 * - Do not pass unvalidated or null values across feature boundaries; consume feature contracts.
 */
@Suppress("UNCHECKED_CAST")
open class BaseProjectBuilder<T : BaseProjectBuilder<T>>(
    var availableCardTypes: List<String>? = null,
    var backersCount: Int = 0,
    var blurb: String = "",
    var backing: Backing? = null,
    var category: Category? = null,
    var commentsCount: Int? = null,
    var country: String = "",
    var countryCode: String = "",
    var createdAt: DateTime = DateTime.now(),
    var creator: User = User.builder().build(),
    var currency: String = "",
    var currencySymbol: String = "",
    var currentCurrency: String? = null,
    var currencyTrailingCode: Boolean = false,
    var displayPrelaunch: Boolean? = null,
    var featuredAt: DateTime? = null,
    var friends: List<User>? = emptyList(),
    var fxRate: Float = 0f,
    var deadline: DateTime? = null,
    var goal: Double = 0.0,
    var id: Long = 0L,
    var isBacking: Boolean = false,
    var isPledgeOverTimeAllowed: Boolean? = null,
    var isStarred: Boolean = false,
    var lastUpdatePublishedAt: DateTime? = null,
    var lastWave: CheckoutWave? = null,
    var launchedAt: DateTime? = null,
    var location: Location? = null,
    var name: String = "",
    var percentFunded: Int? = null,
    var permissions: List<Permission?>? = null,
    var pledged: Double = 0.0,
    var pledgeManager: PledgeManager? = null,
    var photo: Photo? = null,
    var prelaunchActivated: Boolean? = null,
    var projectNotice: String? = null,
    var redemptionPageUrl: String? = null,
    var sendMetaCapiEvents: Boolean? = null,
    var sendThirdPartyEvents: Boolean? = null,
    var tags: List<String>? = emptyList(),
    var rewards: List<Reward>? = emptyList(),
    var slug: String? = null,
    var staffPick: Boolean? = null,
    var canComment: Boolean? = null,
    var pledgeOverTimeMinimumExplanation: String? = "",
    var pledgeOverTimeCollectionPlanChargeExplanation: String? = null,
    var pledgeOverTimeCollectionPlanChargedAsNPayments: String? = null,
    var pledgeOverTimeCollectionPlanShortPitch: String? = null,
    var state: String = Project.STATE_STARTED,
    var stateChangedAt: DateTime? = null,
    var staticUsdRate: Float = 0f,
    var usdExchangeRate: Float = 0f,
    var unreadMessagesCount: Int? = null,
    var unseenActivityCount: Int? = null,
    var updatesCount: Int? = null,
    var updatedAt: DateTime? = null,
    var urls: Urls = Urls.builder().build(),
    var video: Video? = null,
    var projectFaqs: List<ProjectFaq>? = emptyList(),
    var envCommitments: List<EnvironmentalCommitment>? = emptyList(),
    var aiDisclosure: AiDisclosure? = null,
    var risks: String? = "",
    var story: String? = "",
    var isFlagged: Boolean? = null,
    var watchesCount: Int = 0,
    var sharesCount: Int = 0,
    var isInPostCampaignPledgingPhase: Boolean? = null,
    var postCampaignPledgingEnabled: Boolean? = null,
) {
    fun availableCardTypes(types: List<String>?): T = apply { this.availableCardTypes = types } as T
    fun backersCount(count: Int?): T = apply { this.backersCount = count ?: 0 } as T
    fun watchesCount(count: Int?): T = apply { this.watchesCount = count ?: 0 } as T
    fun sharesCount(count: Int?): T = apply { this.sharesCount = count ?: 0 } as T
    fun blurb(blurb: String?): T = apply { this.blurb = blurb ?: "" } as T
    fun backing(backing: Backing?): T = apply { this.backing = backing } as T
    fun category(category: Category?): T = apply { this.category = category } as T
    fun commentsCount(count: Int?): T = apply { this.commentsCount = count } as T
    fun country(country: String?): T = apply { this.country = country ?: "" } as T
    fun countryCode(code: String?): T = apply { this.countryCode = code ?: "" } as T
    fun createdAt(at: DateTime?): T = apply { at?.let { this.createdAt = it } } as T
    fun creator(creator: User?): T = apply { creator?.let { this.creator = it } } as T
    fun currency(currency: String?): T = apply { currency?.let { this.currency = it } } as T
    fun currencySymbol(symbol: String?): T = apply { symbol?.let { this.currencySymbol = it } } as T
    fun currentCurrency(currency: String?): T = apply { this.currentCurrency = currency } as T
    fun currencyTrailingCode(trailing: Boolean?): T = apply { this.currencyTrailingCode = trailing ?: false } as T
    fun displayPrelaunch(display: Boolean?): T = apply { this.displayPrelaunch = display } as T
    fun canComment(can: Boolean?): T = apply { this.canComment = can ?: false } as T
    fun pledgeOverTimeMinimumExplanation(exp: String?): T = apply { this.pledgeOverTimeMinimumExplanation = exp } as T
    fun deadline(deadline: DateTime?): T = apply { this.deadline = deadline } as T
    fun featuredAt(at: DateTime?): T = apply { this.featuredAt = at } as T
    fun friends(friends: List<User>?): T = apply { this.friends = friends ?: emptyList() } as T
    fun fxRate(rate: Float?): T = apply { this.fxRate = rate ?: 0f } as T
    fun goal(goal: Double?): T = apply { this.goal = goal ?: 0.0 } as T
    fun id(id: Long?): T = apply { this.id = id ?: 0L } as T
    fun isBacking(isBacking: Boolean?): T = apply { this.isBacking = isBacking ?: false } as T
    fun isPledgeOverTimeAllowed(allowed: Boolean?): T = apply { this.isPledgeOverTimeAllowed = allowed } as T
    fun isStarred(starred: Boolean?): T = apply { this.isStarred = starred ?: false } as T
    fun lastUpdatePublishedAt(at: DateTime?): T = apply { this.lastUpdatePublishedAt = at } as T
    fun lastWave(wave: CheckoutWave?): T = apply { this.lastWave = wave } as T
    fun launchedAt(at: DateTime?): T = apply { this.launchedAt = at } as T
    fun location(loc: Location?): T = apply { this.location = loc } as T
    fun name(name: String?): T = apply { this.name = name ?: "" } as T
    fun percentFunded(percent: Int?): T = apply { this.percentFunded = percent } as T
    fun permissions(perms: List<Permission?>?): T = apply { this.permissions = perms?.filterNotNull() ?: emptyList() } as T
    fun pledged(pledged: Double?): T = apply { this.pledged = pledged ?: 0.0 } as T
    fun pledgeManager(pm: PledgeManager?): T = apply { this.pledgeManager = pm } as T
    fun photo(photo: Photo?): T = apply { this.photo = photo } as T
    fun prelaunchActivated(activated: Boolean?): T = apply { this.prelaunchActivated = activated } as T
    fun projectNotice(notice: String?): T = apply { this.projectNotice = notice } as T
    fun redemptionPageUrl(url: String?): T = apply { this.redemptionPageUrl = url } as T
    fun sendMetaCapiEvents(send: Boolean?): T = apply { this.sendMetaCapiEvents = send } as T
    fun sendThirdPartyEvents(send: Boolean?): T = apply { this.sendThirdPartyEvents = send } as T
    fun tags(tags: List<String>?): T = apply { this.tags = tags ?: emptyList() } as T
    fun rewards(rewards: List<Reward>?): T = apply { this.rewards = rewards ?: emptyList() } as T
    fun slug(slug: String?): T = apply { this.slug = slug } as T
    fun staffPick(pick: Boolean?): T = apply { this.staffPick = pick } as T
    fun staticUsdRate(rate: Float?): T = apply { this.staticUsdRate = rate ?: 0f } as T
    fun usdExchangeRate(rate: Float?): T = apply { this.usdExchangeRate = rate ?: 0f } as T
    fun state(state: String?): T = apply { this.state = state ?: "" } as T
    fun stateChangedAt(at: DateTime?): T = apply { this.stateChangedAt = at } as T
    fun unreadMessagesCount(count: Int?): T = apply { this.unreadMessagesCount = count ?: 0 } as T
    fun unseenActivityCount(count: Int?): T = apply { this.unseenActivityCount = count ?: 0 } as T
    fun updatedAt(at: DateTime?): T = apply { this.updatedAt = at } as T
    fun updatesCount(count: Int?): T = apply { this.updatesCount = count ?: 0 } as T
    fun urls(urls: Urls?): T = apply { urls?.let { this.urls = it } } as T
    fun video(video: Video?): T = apply { this.video = video } as T
    fun projectFaqs(faqs: List<ProjectFaq>?): T = apply { this.projectFaqs = faqs ?: emptyList() } as T
    fun envCommitments(envs: List<EnvironmentalCommitment>?): T = apply { this.envCommitments = envs ?: emptyList() } as T
    fun aiDisclosure(ai: AiDisclosure?): T = apply { this.aiDisclosure = ai } as T
    fun risks(risks: String?): T = apply { this.risks = risks ?: "" } as T
    fun story(story: String?): T = apply { this.story = story ?: "" } as T
    fun isFlagged(flagged: Boolean?): T = apply { this.isFlagged = flagged } as T
    fun isInPostCampaignPledgingPhase(phase: Boolean?): T = apply { this.isInPostCampaignPledgingPhase = phase } as T
    fun postCampaignPledgingEnabled(enabled: Boolean?): T = apply { this.postCampaignPledgingEnabled = enabled } as T
    fun pledgeOverTimeCollectionPlanChargeExplanation(exp: String?): T = apply { this.pledgeOverTimeCollectionPlanChargeExplanation = exp } as T
    fun pledgeOverTimeCollectionPlanChargedAsNPayments(charged: String?): T = apply { this.pledgeOverTimeCollectionPlanChargedAsNPayments = charged } as T
    fun pledgeOverTimeCollectionPlanShortPitch(pitch: String?): T = apply { this.pledgeOverTimeCollectionPlanShortPitch = pitch } as T

    fun financials(fin: ProjectFinancials): T = apply {
        currency(fin.currency)
        currencySymbol(fin.currencySymbol)
        currentCurrency(fin.currentCurrency)
        currencyTrailingCode(fin.currencyTrailingCode)
        goal(fin.goal)
        pledged(fin.pledged)
        staticUsdRate(fin.staticUsdRate)
        usdExchangeRate(fin.usdExchangeRate)
        fxRate(fin.fxRate)
    } as T

    fun pledgeOverTime(pot: ProjectPledgeOverTime): T = apply {
        isPledgeOverTimeAllowed(pot.isAllowed)
        pledgeOverTimeMinimumExplanation(pot.minimumExplanation)
        pledgeOverTimeCollectionPlanChargeExplanation(pot.chargeExplanation)
        pledgeOverTimeCollectionPlanChargedAsNPayments(pot.chargedAsNPayments)
        pledgeOverTimeCollectionPlanShortPitch(pot.shortPitch)
    } as T

    fun engagementStats(stats: ProjectEngagementStats): T = apply {
        backersCount(stats.backersCount)
        commentsCount(stats.commentsCount)
        updatesCount(stats.updatesCount)
        watchesCount(stats.watchesCount)
        sharesCount(stats.sharesCount)
        unreadMessagesCount(stats.unreadMessagesCount)
        unseenActivityCount(stats.unseenActivityCount)
    } as T

    fun build(): Project = ProjectBuilderHelper.build(this)
}
