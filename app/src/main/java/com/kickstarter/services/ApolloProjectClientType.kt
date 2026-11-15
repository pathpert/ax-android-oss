package com.kickstarter.services

import android.util.Pair
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.features.search.data.SearchEnvelope
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.models.Category
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.FlaggingOption
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Tag
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.type.FlaggingContent
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.reactivex.Observable

/**
 * Apollo GraphQL operations for Project exploration, discovery, media, and creator interactions.
 */
interface ApolloProjectClientType {

    fun getProject(project: Project): Observable<Project>
    fun getProject(slug: String): Observable<Project>
    fun getProjects(discoveryParams: DiscoveryParams, cursor: String?): Observable<DiscoverEnvelope>
    fun watchProject(project: Project): Observable<Project>
    fun unWatchProject(project: Project): Observable<Project>
    suspend fun watchProjectSuspend(project: Project): Result<Project>
    suspend fun unWatchProjectSuspend(project: Project): Result<Project>

    fun createFlagging(
        project: Project? = null,
        details: String,
        flaggingKind: String
    ): Observable<String>

    fun flaggingOptions(contentType: FlaggingContent = FlaggingContent.PROJECT): Observable<List<FlaggingOption>>
    fun creatorDetails(slug: String): Observable<CreatorDetails>
    fun sendMessage(project: Project, recipient: User, body: String): Observable<Long>
    fun fetchCategory(categoryParam: String): Observable<Category?>
    fun fetchCategories(): Observable<List<Category>>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getTags(): Result<List<Tag>>

    fun getProjectUpdates(
        slug: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<UpdatesGraphQlEnvelope>

    fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>>
    suspend fun getSearchProjects(discoveryParams: DiscoveryParams, cursor: String? = null): Result<SearchEnvelope>
    suspend fun fetchSimilarProjects(pid: Long): Result<List<Project>>
    suspend fun getLocations(useDefault: Boolean, term: String?, lat: Float? = null, long: Float? = null, radius: Float? = null, filterByCoordinates: Boolean? = null): Result<List<Location>>
    suspend fun fetchProjectStory(slug: String): Result<StoriedProject>
    suspend fun getVideoFeed(first: Int, cursor: String? = null, categoryId: String? = null): Result<VideoFeedEnvelope>
    fun cleanDisposables()
}
