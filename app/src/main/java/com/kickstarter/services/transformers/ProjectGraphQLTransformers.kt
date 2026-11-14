package com.kickstarter.services.transformers

import com.kickstarter.fragment.FullProject
import com.kickstarter.libs.Permission
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.models.User
import com.kickstarter.models.Web
import com.kickstarter.type.CollaboratorPermission

/**
 * Transform the Project GraphQL data structure into our own Project data model
 * @param fragment.FullProject projectFragment
 * @return Project
 */
fun projectTransformer(projectFragment: FullProject?): Project {
    val availableCards = projectFragment?.availableCardTypes ?: emptyList()
    val backersCount = projectFragment?.backersCount ?: 0
    val blurb = projectFragment?.description ?: ""
    val backing = if (projectFragment?.backing?.backing != null) {
        backingTransformer(projectFragment.backing?.backing)
    } else null
    val category = if (projectFragment?.category?.category != null) {
        categoryTransformer(projectFragment.category?.category)
    } else null
    val commentsCount = projectFragment?.commentsCount ?: 0
    val country = projectFragment?.country?.country?.name ?: ""
    val countryCode = projectFragment?.country?.country?.code?.name ?: ""
    val createdAt = projectFragment?.createdAt
    val creator = userTransformer(projectFragment?.creator?.user)
    val currency = projectFragment?.currency?.name ?: ""
    val currencySymbol = projectFragment?.goal?.amount?.symbol
    val prelaunchActivated = projectFragment?.prelaunchActivated
    val sendMetaCapiEvents = projectFragment?.sendMetaCapiEvents
    val sendThirdPartyEvents = projectFragment?.sendThirdPartyEvents
    val featuredAt = projectFragment?.projectOfTheDayAt
    val friends =
        projectFragment?.friends?.nodes?.map { userTransformer(it?.user) }
            ?: emptyList<User>()
    val fxRate = projectFragment?.fxRate?.toFloat()
    val deadline = projectFragment?.deadlineAt
    val goal = projectFragment?.goal?.amount?.amount?.toDouble() ?: 0.0
    val id = decodeRelayId(projectFragment?.id) ?: -1
    val isBacking = projectFragment?.backing?.backing?.let { true } ?: false
    val isPledgeOverTimeAllowed = projectFragment?.isPledgeOverTimeAllowed ?: false
    val isStarred = projectFragment?.isWatched ?: false
    val lastWave = projectFragment?.lastWave?.lastWave?.let { checkoutWave ->
        checkoutWaveTransformer(checkoutWave)
    }
    val launchedAt = projectFragment?.launchedAt
    val location = locationTransformer(projectFragment?.location?.location)
    val name = projectFragment?.name
    val permission = projectFragment?.collaboratorPermissions?.map {
        when (it) {
            CollaboratorPermission.comment -> Permission.COMMENT
            CollaboratorPermission.edit_faq -> Permission.EDIT_FAQ
            CollaboratorPermission.edit_project -> Permission.EDIT_PROJECT
            CollaboratorPermission.fulfillment -> Permission.FULFILLMENT
            CollaboratorPermission.post -> Permission.POST
            CollaboratorPermission.view_pledges -> Permission.VIEW_PLEDGES
            else -> Permission.UNKNOWN
        }
    }
    val pledged = projectFragment?.pledged?.amount?.amount?.toDouble() ?: 0.0
    val pledgeManager = projectFragment?.pledgeManager?.pledgeManager?.let { pledgeManager ->
        pledgeManagerTransformer(projectFragment.pledgeManager.pledgeManager)
    }
    val photo = getPhoto(projectFragment?.full?.image?.url, projectFragment?.full?.image?.altText)
    val projectNotice = projectFragment?.projectNotice
    val redemptionPageUrl = projectFragment?.redemptionPageUrl
    val tags = mutableListOf<String>()
    projectFragment?.tagsCreative?.tags?.map { tags.add(it?.id ?: "") }
    projectFragment?.tagsDiscovery?.tags?.map { tags.add(it?.id ?: "") }
    val pledgeOverTimeMinimumExplanation = projectFragment?.pledgeOverTimeMinimumExplanation
    val minPledge = projectFragment?.minPledge?.toDouble() ?: 1.0
    val rewards =
        projectFragment?.rewards?.nodes?.map {
            it?.reward?.let { rw ->
                rewardTransformer(
                    rewardGr = rw
                )
            }
        }

    // - GraphQL does not provide the Reward no reward, we need to add it first
    val modifiedRewards = rewards?.toMutableList()
    modifiedRewards?.add(0, RewardFactory.noReward().toBuilder().minimum(minPledge).build())
    modifiedRewards?.toList()

    val slug = projectFragment?.slug
    val staffPicked = projectFragment?.isProjectWeLove ?: false
    val state = projectFragment?.state?.name?.lowercase()
    val stateChangedAt = projectFragment?.stateChangedAt
    val staticUSDRate = projectFragment?.usdExchangeRate?.toFloat() ?: 1f
    val usdExchangeRate = projectFragment?.usdExchangeRate?.toFloat() ?: 1f
    val updatedAt = projectFragment?.posts?.updates?.nodes?.let {
        if (it.isNotEmpty()) return@let it.first()?.updatedAt
        else null
    }
    val updatesCount = projectFragment?.posts?.updates?.totalCount
    val url = projectFragment?.url
    val urlsWeb = Web.builder()
        .project(url)
        .rewards("$url/rewards")
        .build()
    val urls = Urls.builder().web(urlsWeb).build()
    val video = if (projectFragment?.video?.video != null) {
        videoTransformer(projectFragment.video?.video)
    } else null
    val displayPrelaunch = (projectFragment?.isLaunched ?: false).negate()
    val faqs = projectFragment?.faqs?.nodes?.map { node ->
        node?.let { projectFaqTransformer(it.faq) }
    } ?: emptyList()
    val eCommitment = projectFragment?.environmentalCommitments?.map {
        it?.let { it1 -> environmentalCommitmentTransformer(it1.environmentalCommitment) }
    } ?: emptyList()
    val aiDisclosure = projectFragment?.aiDisclosure?.aiDisclosure?.let {
        aiDisclosureTransformer(it)
    }
    val risks = projectFragment?.risks
    val story = projectFragment?.story?.toString() ?: ""
    val userCanComment = projectFragment?.canComment ?: false
    val isFlagged = projectFragment?.flagging?.kind?.let { true } ?: false
    val watchesCount = projectFragment?.watchesCount ?: 0
    val isInPostCampaignPledgingPhase = projectFragment?.isInPostCampaignPledgingPhase ?: false
    val postCampaignPledgingEnabled = projectFragment?.postCampaignPledgingEnabled ?: false
    val pledgeOverTimeCollectionPlanChargeExplanation = projectFragment?.pledgeOverTimeCollectionPlanChargeExplanation ?: ""
    val pledgeOverTimeCollectionPlanShortPitch = projectFragment?.pledgeOverTimeCollectionPlanShortPitch ?: ""
    val pledgeOverTimeCollectionPlanChargedAsNPayments = projectFragment?.pledgeOverTimeCollectionPlanChargedAsNPayments ?: ""

    return Project.builder()
        .availableCardTypes(availableCards.map { it.name })
        .backersCount(backersCount)
        .blurb(blurb)
        .canComment(userCanComment)
        .backing(backing)
        .category(category)
        .commentsCount(commentsCount)
        .country(country)
        .countryCode(countryCode)
        .createdAt(createdAt)
        .creator(creator)
        .currency(currency)
        .currencySymbol(currencySymbol)
        .currentCurrency(currency) // - selected currency can be fetched form the User/Configuration Object
        .currencyTrailingCode(false) // - This field is available on V1 Configuration Object
        .displayPrelaunch(displayPrelaunch)
        .featuredAt(featuredAt)
        .friends(friends)
        .fxRate(fxRate)
        .deadline(deadline)
        .goal(goal)
        .id(id)
        .isBacking(isBacking)
        .isPledgeOverTimeAllowed(isPledgeOverTimeAllowed)
        .isStarred(isStarred)
        .lastUpdatePublishedAt(updatedAt)
        .lastWave(lastWave)
        .launchedAt(launchedAt)
        .location(location)
        .name(name)
        .permissions(permission)
        .pledged(pledged)
        .pledgeManager(pledgeManager)
        .photo(photo) // - now we get the full size for field from GraphQL, but V1 provided several image sizes
        .prelaunchActivated(prelaunchActivated)
        .projectNotice(projectNotice)
        .redemptionPageUrl(redemptionPageUrl)
        .sendMetaCapiEvents(sendMetaCapiEvents)
        .sendThirdPartyEvents(sendThirdPartyEvents)
        .tags(tags)
        .rewards(modifiedRewards?.filterNotNull()?.toList())
        .slug(slug)
        .staffPick(staffPicked)
        .state(state)
        .stateChangedAt(stateChangedAt)
        .staticUsdRate(staticUSDRate)
        .usdExchangeRate(usdExchangeRate)
        .updatedAt(updatedAt)
        .updatesCount(updatesCount)
        .urls(urls)
        .video(video)
        .projectFaqs(faqs.filterNotNull())
        .envCommitments(eCommitment.filterNotNull())
        .aiDisclosure(aiDisclosure)
        .risks(risks)
        .story(story)
        .isFlagged(isFlagged)
        .watchesCount(watchesCount)
        .isInPostCampaignPledgingPhase(isInPostCampaignPledgingPhase)
        .postCampaignPledgingEnabled(postCampaignPledgingEnabled)
        .pledgeOverTimeMinimumExplanation(pledgeOverTimeMinimumExplanation)
        .pledgeOverTimeCollectionPlanChargeExplanation(pledgeOverTimeCollectionPlanChargeExplanation)
        .pledgeOverTimeCollectionPlanShortPitch(pledgeOverTimeCollectionPlanShortPitch)
        .pledgeOverTimeCollectionPlanChargedAsNPayments(pledgeOverTimeCollectionPlanChargedAsNPayments)
        .build()
}
