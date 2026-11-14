package com.kickstarter.services.transformers

import com.kickstarter.AddUserToSecretRewardGroupMutation
import com.kickstarter.fragment.ProjectCard
import com.kickstarter.fragment.SimilarProject
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.Urls
import com.kickstarter.models.Web
import org.jetbrains.annotations.Nullable

internal fun getPhoto(photoUrl: @Nullable String?, altText: String?): Photo? {
    val photo = photoUrl?.let {
        Photo.builder()
            .ed(photoUrl)
            .full(photoUrl)
            .little(photoUrl)
            .med(photoUrl)
            .small(photoUrl)
            .thumb(photoUrl)
            .altText(altText)
            .build()
    }

    return photo
}

/**
 * Transform the Project GraphQL data structure into our own Project data model
 * @param fragment.ProjectCard projectFragment
 * @return Project
 */
fun projectTransformer(projectFragment: ProjectCard?): Project {
    val backersCount = projectFragment?.backersCount ?: 0
    val blurb = projectFragment?.description ?: ""
    val category = if (projectFragment?.category?.category != null) {
        categoryTransformer(projectFragment.category.category)
    } else null
    val country = projectFragment?.country?.country?.name ?: ""
    val createdAt = projectFragment?.createdAt
    val creator = userTransformer(projectFragment?.creator?.user)
    val currencySymbol = projectFragment?.goal?.amount?.symbol
    val prelaunchActivated = projectFragment?.prelaunchActivated
    val featuredAt = projectFragment?.projectOfTheDayAt
    val friends =
        projectFragment?.friends?.nodes?.map { userTransformer(it?.user) }
            ?: emptyList()
    val pledged = projectFragment?.pledged?.amount?.amount?.toDouble() ?: 0.0
    val fxRate = projectFragment?.fxRate?.toFloat()
    val deadline = projectFragment?.deadlineAt
    val goal = projectFragment?.goal?.amount?.amount?.toDouble() ?: 0.0
    val id = decodeRelayId(projectFragment?.id) ?: -1
    val isBacking = projectFragment?.backing?.id?.let { true } ?: false
    val isPledgeOverTimeAllowed = projectFragment?.isPledgeOverTimeAllowed ?: false
    val isStarred = projectFragment?.isWatched ?: false
    val launchedAt = projectFragment?.launchedAt
    val location = locationTransformer(projectFragment?.location?.location)
    val name = projectFragment?.name
    val photo = getPhoto(projectFragment?.full?.image?.url, projectFragment?.full?.image?.altText)
    val projectNotice = projectFragment?.projectNotice
    val slug = projectFragment?.slug
    val staffPicked = projectFragment?.isProjectWeLove ?: false
    val state = projectFragment?.state?.name?.lowercase()
    val stateChangedAt = projectFragment?.stateChangedAt
    val url = projectFragment?.url
    val urlsWeb = Web.builder()
        .project(url)
        .rewards("$url/rewards")
        .build()
    val urls = Urls.builder().web(urlsWeb).build()
    val video = videoTransformer(projectFragment?.video?.video)
    val watchesCount = projectFragment?.watchesCount ?: 0
    val displayPrelaunch = (projectFragment?.isLaunched ?: false).negate()
    val isInPostCampaignPledgingPhase = projectFragment?.isInPostCampaignPledgingPhase ?: false
    val postCampaignPledgingEnabled = projectFragment?.postCampaignPledgingEnabled ?: false

    return Project.builder()
        .backersCount(backersCount)
        .blurb(blurb)
        .category(category)
        .country(country)
        .createdAt(createdAt)
        .creator(creator)
        .currencySymbol(currencySymbol)
        .currencyTrailingCode(false) // - This field is available on V1 Configuration Object
        .displayPrelaunch(displayPrelaunch)
        .featuredAt(featuredAt)
        .friends(friends)
        .fxRate(fxRate)
        .deadline(deadline)
        .pledged(pledged)
        .goal(goal)
        .id(id)
        .isBacking(isBacking)
        .isPledgeOverTimeAllowed(isPledgeOverTimeAllowed)
        .isStarred(isStarred)
        .launchedAt(launchedAt)
        .location(location)
        .name(name)
        .photo(photo) // - now we get the full size for field from GraphQL, but V1 provided several image sizes
        .prelaunchActivated(prelaunchActivated)
        .projectNotice(projectNotice)
        .slug(slug)
        .staffPick(staffPicked)
        .state(state)
        .urls(urls)
        .stateChangedAt(stateChangedAt)
        .isInPostCampaignPledgingPhase(isInPostCampaignPledgingPhase)
        .video(video)
        .postCampaignPledgingEnabled(postCampaignPledgingEnabled)
        .watchesCount(watchesCount)
        .build()
}

/**
 * Transform the Project GraphQL data structure into our own Project data model
 * @param fragment.SimilarProject similarProjectFragment
 * @return Project
 */
fun projectTransformer(similarProjectFragment: SimilarProject?): Project {
    val id = decodeRelayId(similarProjectFragment?.id) ?: -1
    val name = similarProjectFragment?.name
    val slug = similarProjectFragment?.slug
    val displayPrelaunch = (similarProjectFragment?.isLaunched ?: false).negate()
    val deadline = similarProjectFragment?.deadlineAt
    val percentFunded = similarProjectFragment?.percentFunded
    val photo = getPhoto(similarProjectFragment?.imageUrl, null)

    return Project.builder()
        .currencyTrailingCode(false) // - This field is available on V1 Configuration Object
        .displayPrelaunch(displayPrelaunch)
        .deadline(deadline)
        .id(id)
        .name(name)
        .percentFunded(percentFunded)
        .photo(photo) // - now we get the full size for field from GraphQL, but V1 provided several image sizes
        .slug(slug)
        .build()
}

/**
 * Transform the AddUserToSecretRewardGroupMutation.Project GraphQL data structure into our own Project data model
 * @param AddUserToSecretRewardGroupMutation.Project project
 * @return Project
 */
fun projectTransformer(project: AddUserToSecretRewardGroupMutation.Project?): Project {
    val id = decodeRelayId(project?.id) ?: -1
    val rewards = project?.rewards?.nodes?.mapNotNull { node ->
        node?.let {
            Reward.builder()
                .id(decodeRelayId(it.id) ?: -1)
                .title(it.name)
                .build()
        }
    } ?: emptyList()

    return Project.builder()
        .id(id)
        .rewards(rewards)
        .build()
}
