@file:JvmMultifileClass
@file:JvmName("ProjectExt")

package com.kickstarter.libs.utils.extensions

import android.util.Pair
import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.I18nUtils
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.type.CreditCardTypes
import com.kickstarter.type.ProjectSort
import io.reactivex.Observable

/**
 * When fetching a project from GraphQL, we need to populate the next fields:
 *     Project.currentCurrency()
 *     Project.currencyTrailingCode()
 *     Project.currencySymbol()
 *
 * - with the country code on configuration in case no user logged in
 * - with the selected currency by the user in case there is a logged in user.
 * - if configuration empty default to project currency
 */
fun Project.updateProjectWith(config: Config, user: User?): Project {
    val currentCountry = config.launchedCountries().find {
        it.name().equals(config.countryCode())
    }

    val currentCurrency = user?.chosenCurrency()
        ?: currentCountry?.currencyCode()
        ?: currency()

    val countryOfCurrency = config.launchedCountries().find { it.currencyCode() == currentCurrency }

    val currencySymbol = countryOfCurrency?.currencySymbol() ?: currencySymbol()
    val trailingCode = countryOfCurrency?.trailingCode() ?: currencyTrailingCode()

    return this.toBuilder()
        .currentCurrency(currentCurrency)
        .currencyTrailingCode(trailingCode ?: false)
        .currencySymbol(currencySymbol)
        .build()
}

/**
 * The watch/unwatch mutations build their Relay id from [Project.id] via `encodeRelayId`. An
 * invalid id (e.g. `-1`) yields a malformed id like Base64("Project--1") and a backend 500,
 * so callers should skip the request for such a project.
 */
fun Project.hasValidRelayId(): Boolean = this.id() > 0L

fun Project.showLatePledgeFlow() = this.isInPostCampaignPledgingPhase() ?: false && this.postCampaignPledgingEnabled() ?: false && !this.isBacking()

fun Project.isLatePledgesActive() = this.isInPostCampaignPledgingPhase() ?: false && this.postCampaignPledgingEnabled() ?: false

fun Project.pledgeManagementAvailable() = this.lastWave()?.active() == true &&
    (this.pledgeManager()?.acceptsNewBackers() == true || this.isBacking())

/**
 * Checks if the given card type is listed in the available card types.
 */
fun Project.acceptedCardType(cardType: CreditCardTypes?) = this.availableCardTypes()?.contains(cardType?.rawValue) ?: false

/**
 * Combines each project in the list with the discovery param.
 */
fun combineProjectsAndParams(projects: List<Project>, params: DiscoveryParams): ArrayList<Pair<Project, DiscoveryParams>> {
    val projectAndParams = arrayListOf<Pair<Project, DiscoveryParams>>()
    projects.forEach {
        projectAndParams.add(Pair.create(it, params))
    }
    return projectAndParams
}

/**
 * The project is allowed to pledge during crowdfund active campaign or late pledges phase.
 */
fun Project.isAllowedToPledge(): Boolean {
    return (!this.isCompleted() || this.showLatePledgeFlow())
}

/**
 * Returns `true` if the project is no longer live, `false` otherwise.
 */
fun Project.isCompleted(): Boolean =
    when (this.state()) {
        Project.STATE_CANCELED,
        Project.STATE_FAILED,
        Project.STATE_SUCCESSFUL,
        Project.STATE_PURGED,
        Project.STATE_SUSPENDED,
        -> true
        else -> false
    }

/**
 * Returns `true` if the project name ends with a punctuation character.
 */
fun isProjectNamePunctuated(name: String): Boolean = name.substring(name.length - 1).matches(".*\\p{Punct}".toRegex())

/**
 * Returns the metadata for the project based on backing, starred, or featured.
 */
fun Project.metadataForProject(): ProjectMetadata =
    when {
        this.isBacking() -> ProjectMetadata.BACKING
        this.displayPrelaunch() ?: false -> ProjectMetadata.COMING_SOON
        this.isStarred() -> ProjectMetadata.SAVING
        this.isFeaturedToday -> ProjectMetadata.CATEGORY_FEATURED
        else -> ProjectMetadata.NONE
    }

/**
 * Returns 16:9 height relative to input width.
 */
fun photoHeightFromWidthRatio(width: Int): Int = width * 9 / 16

/**
 * Returns if the current user is the creator of the project.
 */
fun Project.userIsCreator(user: User?): Boolean = user?.let { this.creator().id() == it.id() } ?: false

fun isUSUserViewingNonUSProject(userCountry: String, projectCountry: String): Boolean = I18nUtils.isCountryUS(userCountry) && !I18nUtils.isCountryUS(projectCountry)

/**
 * In order to update the fulfillment state for a project, the user needs to be a backer and the project successful.
 */
fun Project.canUpdateFulfillment() = isBacking() && isSuccessful

enum class ProjectMetadata {
    BACKING, SAVING, CATEGORY_FEATURED, COMING_SOON, NONE
}

/**
 * Given a list of projects and root categories this will determine if the first project is featured
 * and is in need of its root category.
 */
fun List<Project>.fillRootCategoryForFeaturedProjects(rootCategories: List<Category>): List<Project> {
    if (this.isEmpty()) {
        return ListUtils.empty()
    }
    val firstProject = this[0]

    val category = firstProject.category() ?: return this
    val categoryParentId = category.parentId() ?: return this

    if (!firstProject.projectNeedsRootCategory(category)) {
        return this
    }

    val projectRootCategory = Observable.fromIterable(rootCategories)
        .filter { rootCategory: Category -> rootCategory.id() == categoryParentId }
        .take(1)
        .blockingFirst()

    val newCategory = category.toBuilder().parent(projectRootCategory).build()
    val newProject = firstProject.toBuilder().category(newCategory).build()
    return ListUtils.replaced(this, 0, newProject)
}

/**
 * Determines if the project requires finding the root category.
 */
fun Project.projectNeedsRootCategory(category: Category): Boolean {
    return !category.isRoot && category.parent() == null && this.isFeaturedToday
}

fun Project.updateStartedProjectAndDiscoveryParamsList(
    listOfProjects: List<Pair<Project, DiscoveryParams>>,
): List<Pair<Project, DiscoveryParams>> {
    val position = listOfProjects.indexOfFirst { item ->
        item.first.id() == this.id()
    }

    if (position >= 0 && position < listOfProjects.size) {
        return listOfProjects.toMutableList().apply {
            val project = listOfProjects[position].first.toBuilder().isStarred(this@updateStartedProjectAndDiscoveryParamsList.isStarred()).build()
            this[position] = Pair(project, listOfProjects[position].second)
        }
    }

    return listOfProjects
}

fun ProjectSort.toDiscoveryParam(): DiscoveryParams.Sort {
    return when (this) {
        ProjectSort.MAGIC -> DiscoveryParams.Sort.MAGIC
        ProjectSort.DISTANCE -> DiscoveryParams.Sort.DISTANCE
        ProjectSort.POPULARITY -> DiscoveryParams.Sort.POPULAR
        ProjectSort.END_DATE -> DiscoveryParams.Sort.ENDING_SOON
        ProjectSort.NEWEST -> DiscoveryParams.Sort.NEWEST
        ProjectSort.MOST_FUNDED -> DiscoveryParams.Sort.MOST_FUNDED
        ProjectSort.MOST_BACKED -> DiscoveryParams.Sort.MOST_BACKED
        ProjectSort.RELEVANCE -> DiscoveryParams.Sort.MAGIC
        ProjectSort.UNKNOWN__ -> DiscoveryParams.Sort.MAGIC
    }
}

fun Array<ProjectSort>.toDiscoveryParamsList(): List<DiscoveryParams.Sort> =
    this.map { it.toDiscoveryParam() }.distinct()
