package com.kickstarter.mock.factories

import com.kickstarter.models.RewardsItem
import com.kickstarter.models.RewardsItem.Companion.builder

/**
 * Test mock implementation for RewardsItemFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object RewardsItemFactory {
    @JvmStatic
    fun rewardsItem(): RewardsItem {
        val itemId = IdFactory.id().toLong()
        return builder()
            .id(IdFactory.id().toLong())
            .item(ItemFactory.item().toBuilder().id(itemId).build())
            .itemId(itemId)
            .quantity(1)
            .rewardId(IdFactory.id().toLong())
            .build()
    }

    @JvmStatic
    fun rewardsItem2(): RewardsItem {
        val itemId = IdFactory.id().toLong()
        return builder()
            .id(IdFactory.id().toLong())
            .item(ItemFactory.item().toBuilder().id(itemId).name("Comic Book").build())
            .itemId(itemId)
            .quantity(20)
            .rewardId(IdFactory.id().toLong())
            .build()
    }

    @JvmStatic
    fun rewardsItem3(): RewardsItem {
        val itemId = IdFactory.id().toLong()
        return builder()
            .id(IdFactory.id().toLong())
            .item(ItemFactory.item().toBuilder().id(itemId).name("Enamel Pin").build())
            .itemId(itemId)
            .quantity(104)
            .rewardId(IdFactory.id().toLong())
            .build()
    }
}
