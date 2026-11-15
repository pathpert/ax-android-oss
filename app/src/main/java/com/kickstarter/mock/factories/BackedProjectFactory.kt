package com.kickstarter.mock.factories

import com.kickstarter.models.Backing
import com.kickstarter.models.Order
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.type.CreditCardPaymentType
import org.joda.time.DateTime
import java.util.ArrayList

/**
 * Mock factory producing Kickstarter [Project] instances with various backing states.
 */
object BackedProjectFactory {

    private fun baseBacking(project: Project, reward: Reward): Backing.Builder {
        return Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
    }

    @JvmStatic
    fun backedProjectWithError(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        val backing = baseBacking(project, reward)
            .status(Backing.STATUS_ERRORED)
            .build()
        return project.toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProject(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        val backing = baseBacking(project, reward).build()
        return project.toBuilder()
            .availableCardTypes(listOf(PaymentSourceFactory.visa().type() ?: CreditCardPaymentType.CREDIT_CARD.rawValue))
            .canComment(true)
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithPMCheckoutOrder(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        val order = Order.builder().checkoutState(Order.CheckoutStateEnum.COMPLETE).build()
        val backing = baseBacking(project, reward)
            .backingDetailsPageRoute("https://ksr.com/backing/survey_responses")
            .order(order)
            .build()
        return project.toBuilder()
            .availableCardTypes(listOf(PaymentSourceFactory.visa().type() ?: CreditCardPaymentType.CREDIT_CARD.rawValue))
            .canComment(true)
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithPlotSelected(): Project {
        val project = ProjectFactory.project().toBuilder().isPledgeOverTimeAllowed(true).build()
        val reward = RewardFactory.reward()
        val backing = baseBacking(project, reward)
            .incremental(true)
            .build()
        return project.toBuilder()
            .availableCardTypes(listOf(PaymentSourceFactory.visa().type() ?: CreditCardPaymentType.CREDIT_CARD.rawValue))
            .canComment(true)
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithRewardAndAddOnsLimitReached(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward().toBuilder().hasAddons(true).limit(10).build()
        val add1 = RewardFactory.addOn().toBuilder().remaining(0).limit(0).quantity(1).build()
        val addOns: MutableList<Reward> = ArrayList()
        addOns.add(add1)
        val backing = baseBacking(project, reward)
            .addOns(addOns)
            .build()
        return project.toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithAddOns(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward().toBuilder().hasAddons(true).build()
        val add1 = RewardFactory.addOn()
        val addOns: MutableList<Reward> = ArrayList()
        addOns.add(add1)
        addOns.add(add1)
        val backing = baseBacking(project, reward)
            .addOns(addOns)
            .build()
        return project.toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectRewardAvailableAddOnsNotBackedAddOns(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward().toBuilder().hasAddons(true).isAvailable(true).build()
        val backing = baseBacking(project, reward).build()
        return project.toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedSuccessfulProject(): Project {
        val project = ProjectFactory.successfulProject()
        val reward = RewardFactory.reward()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project.toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithRewardLimited(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.limited()
        val backing = baseBacking(project, reward).build()
        return project.toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithRewardLimitReached(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.limitReached().toBuilder().hasAddons(true).build()
        val backing = baseBacking(project, reward).build()
        return project.toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithNoReward(): Project {
        val project = ProjectFactory.project()
        val reward = RewardFactory.noReward()
        val backing = baseBacking(project, reward)
            .rewardId(null)
            .build()
        return project.toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }
}
