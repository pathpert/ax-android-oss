package com.kickstarter.services.transformers

import com.kickstarter.FetchProjectRewardsQuery
import com.kickstarter.fragment.RewardImage
import com.kickstarter.models.Item
import com.kickstarter.models.Location
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.models.ShippingRule
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.type.RewardType
import com.kickstarter.type.ShippingPreference
import org.joda.time.DateTime

/**
 * Transform the Reward GraphQL data structure into our own Reward data model
 * @param fragment.reward rewardGr
 * @return Reward
 */
fun rewardTransformer(
    rewardGr: com.kickstarter.fragment.Reward,
    shippingRulesExpanded: List<com.kickstarter.fragment.ShippingRule> = emptyList(),
    simpleShippingRules: List<FetchProjectRewardsQuery.SimpleShippingRulesExpanded> = emptyList(),
    allowedAddons: Boolean = false,
    rewardItems: List<RewardsItem> = emptyList(),
    addOnItems: List<RewardsItem> = emptyList(),
    rewardImage: RewardImage? = null
): Reward {
    val amount = rewardGr.amount.amount.amount?.toDouble() ?: 0.0
    val latePledgeAmount = rewardGr.latePledgeAmount.amount.amount?.toDouble() ?: 0.0
    val pledgeAmount = rewardGr.pledgeAmount.amount.amount?.toDouble() ?: 0.0
    val convertedAmount =
        rewardGr.convertedAmount.amount.amount?.toDouble() ?: 0.0
    val desc = rewardGr.description
    val title = rewardGr.name
    val estimatedDelivery = rewardGr.estimatedDeliveryOn?.let { DateTime(it) }
    val remaining = rewardGr.remainingQuantity
    val endsAt = rewardGr.endsAt?.let { DateTime(it) }
    val startsAt = rewardGr.startsAt?.let { DateTime(it) }
    val rewardId = decodeRelayId(rewardGr.id) ?: -1
    val available = rewardGr.available
    val isAddOn = rewardGr.rewardType == RewardType.addon
    val isReward = rewardGr.rewardType == RewardType.base
    val backersCount = rewardGr.backersCount
    val shippingPreference = when (rewardGr.shippingPreference) {
        ShippingPreference.none -> Reward.ShippingPreference.NONE
        ShippingPreference.restricted -> Reward.ShippingPreference.RESTRICTED
        ShippingPreference.unrestricted -> Reward.ShippingPreference.UNRESTRICTED
        ShippingPreference.local -> Reward.ShippingPreference.LOCAL
        else -> Reward.ShippingPreference.UNKNOWN
    }

    val limit = if (isAddOn) chooseLimit(rewardGr.limit, rewardGr.limitPerBacker)
    else rewardGr.limit

    val shippingRules =
        shippingRulesExpanded.takeIf { it.isNotEmpty() }?.map { shippingRuleTransformer(it) }
            ?: simpleShippingRules.takeIf { it.isNotEmpty() }?.map {
                return@map simpleShippingRuleTransformer(it)
            } ?: emptyList()

    val localReceiptLocation = locationTransformer(rewardGr.localReceiptLocation?.location)

    val photo = getPhoto(rewardImage?.image?.url, rewardImage?.image?.altText)
    val isSecretReward = rewardGr.audienceData.secret
    val isFeatured = rewardGr.featured ?: false
    return Reward.builder()
        .title(title)
        .convertedMinimum(convertedAmount)
        .minimum(amount)
        .pledgeAmount(pledgeAmount)
        .latePledgeAmount(latePledgeAmount)
        .limit(limit)
        .remaining(remaining)
        .endsAt(endsAt)
        .startsAt(startsAt)
        .description(desc)
        .estimatedDeliveryOn(estimatedDelivery)
        .isAddOn(isAddOn)
        .addOnsItems(addOnItems)
        .hasAddons(allowedAddons)
        .rewardsItems(rewardItems)
        .id(rewardId)
        .shippingPreference(shippingPreference.name.lowercase())
        .shippingPreferenceType(shippingPreference)
        .shippingType(shippingPreference.name.lowercase())
        .shippingRules(shippingRules)
        .isAvailable(available)
        .backersCount(backersCount)
        .localReceiptLocation(localReceiptLocation)
        .image(photo)
        .isSecretReward(isSecretReward)
        .isFeatured(isFeatured)
        .build()
}

fun simpleShippingRuleTransformer(simpleShippingRules: FetchProjectRewardsQuery.SimpleShippingRulesExpanded): ShippingRule {
    val id = decodeRelayId(simpleShippingRules.locationId) ?: -1
    val country = simpleShippingRules.country ?: ""
    val displayName = simpleShippingRules.locationName

    val location = Location.builder()
        .id(id)
        .country(country)
        .displayableName(displayName)
        .name(displayName)
        .build()
    val cost = simpleShippingRules.cost?.toDoubleOrNull() ?: 0.0
    val estimatedMin = simpleShippingRules.estimatedMin?.toDoubleOrNull() ?: 0.0
    val estimatedMax = simpleShippingRules.estimatedMax?.toDoubleOrNull() ?: 0.0

    return ShippingRule.builder()
        .id(id)
        .location(location)
        .cost(cost)
        .estimatedMax(estimatedMax)
        .estimatedMin(estimatedMin)
        .build()
}

/**
 * Choose the available limit being the smallest one, we can have limit by backer available just in add-ons
 * or limit by reward, available in V1 and Graphql and for both add-ons and Rewards
 * @return limit
 */
private fun chooseLimit(limitReward: Int?, limitPerBacker: Int?): Int {
    var limit = limitReward?.let { it } ?: -1
    var limitBacker = limitPerBacker?.let { it } ?: -1

    if (limit < 0) limit = limitBacker
    if (limitBacker < 0) limitBacker = limit

    return when (limit <= limitBacker) {
        true -> limit
        else -> limitBacker
    }
}

/**
 * Transform the Reward.Items GraphQL data structure into our own RewardsItems data model
 * @param fragment.Reward.items
 * @return List<RewardItem>
 */
fun complexRewardItemsTransformer(items: com.kickstarter.fragment.RewardItems?): List<RewardsItem> {
    val rewardItems = items?.edges?.map { edge ->
        val quantity = edge?.quantity
        val description = edge?.node?.name
        val id = decodeRelayId(edge?.node?.id) ?: -1
        val name = edge?.node?.name ?: ""

        val item = Item.builder()
            .name(name)
            .description(description)
            .id(id)
            .build()

        return@map RewardsItem.builder()
            .id(id)
            .itemId(item.id())
            .item(item)
            .rewardId(0) // - Discrepancy between V1 and Graph, the Graph object do not have the rewardID
            .quantity(quantity)
            .build()
    } ?: emptyList<RewardsItem>()
    return rewardItems.toList()
}

/**
 * For addOns we receive this kind of data structure :[D, D, D, D, D, C, E, E]
 * and we need to transform it in : D(5),C(1),E(2)
 */
fun getAddOnsList(addOns: com.kickstarter.fragment.Backing.AddOns): List<Reward> {
    val rewardsList = addOns.nodes?.mapNotNull { node ->
        node?.let { rewardTransformer(it.reward, rewardImage = it.rewardImage) }
    }

    val mapHolder = mutableMapOf<Long, Reward>()

    rewardsList?.forEach {
        val rwId = it.id()
        val q = mapHolder[rwId]?.quantity() ?: 0
        mapHolder[rwId] = it.toBuilder().quantity(q + 1).build()
    }

    return mapHolder.values.toList()
}

/**
 * Transform the fragment.ShippingRule GraphQL data structure into our own ShippingRules data model
 * @param fragment.ShippingRule
 * @return ShippingRule
 */
fun shippingRuleTransformer(rule: com.kickstarter.fragment.ShippingRule): ShippingRule {
    val cost = rule.cost?.amount?.amount?.toDoubleOrNull() ?: 0.0
    val location = rule.location?.let {
        locationTransformer(it.location)
    }
    val estimatedMin = rule.estimatedMin?.amount?.toDoubleOrNull() ?: 0.0
    val estimatedMax = rule.estimatedMax?.amount?.toDoubleOrNull() ?: 0.0

    return ShippingRule.builder()
        .cost(cost)
        .location(location)
        .estimatedMin(estimatedMin)
        .estimatedMax(estimatedMax)
        .build()
}

fun shippingRulesListTransformer(shippingRulesExpanded: List<com.kickstarter.fragment.ShippingRule>): ShippingRulesEnvelope {
    val shippingRulesList: List<ShippingRule> = shippingRulesExpanded.map { shippingRule ->
        shippingRuleTransformer(shippingRule)
    }

    return ShippingRulesEnvelope
        .builder()
        .shippingRules(shippingRulesList)
        .build()
}
