package com.kickstarter.services.transformers

import com.apollographql.apollo3.api.Optional
import com.kickstarter.BuildPaymentPlanQuery
import com.kickstarter.PledgedProjectsOverviewQuery
import com.kickstarter.features.pledgedprojectsoverview.data.Flag
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.data.PledgeTierType
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import com.kickstarter.fragment.PpoCard.DeliveryAddress
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.CheckoutWave
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.PaymentIncrementAmount
import com.kickstarter.models.PaymentPlan
import com.kickstarter.models.PledgeManager
import com.kickstarter.models.Reward
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.type.PledgeManagerStateEnum
import com.kickstarter.type.PledgeProjectsOverviewSort
import com.kickstarter.type.ShippingPreference

fun getPledgedProjectsOverviewQuery(queryInput: PledgedProjectsOverviewQueryData): PledgedProjectsOverviewQuery {
    val tierTypes =
        queryInput.tierTypes?.map { getPledgeTierType(it) }?.filter { it != PledgeProjectsOverviewSort.UNKNOWN__ }

    return PledgedProjectsOverviewQuery(
        after = Optional.present(queryInput.after),
        before = Optional.present(queryInput.before),
        first = Optional.present(queryInput.first),
        last = Optional.present(queryInput.last),
        tierTypes = Optional.present(tierTypes)
    )
}

fun pledgedProjectsOverviewEnvelopeTransformer(ppoResponse: PledgedProjectsOverviewQuery.PledgeProjectsOverview): PledgedProjectsOverviewEnvelope {
    val ppoCards =
        ppoResponse.pledges?.edges?.mapNotNull {
            val ppoBackingData = it?.node?.backing?.ppoCard
            val flags = it?.node?.flags?.map { flag ->
                Flag.builder().message(flag.message).icon(flag.icon).type(flag.type).build()
            }
            val reward = Reward.builder()
                .id(decodeRelayId(ppoBackingData?.reward?.id) ?: 0)
                .shippingPreference(ppoBackingData?.reward?.shippingPreference?.name?.lowercase())
                .shippingPreferenceType(getShippingPreference(ppoBackingData?.reward?.shippingPreference))
                .shippingType(ppoBackingData?.reward?.shippingPreference?.name?.lowercase())
                .build()
            PPOCard.builder()
                .backingId(ppoBackingData?.id)
                .backingDetailsUrl(ppoBackingData?.backingDetailsPageRoute)
                .clientSecret(ppoBackingData?.clientSecret)
                .amount(ppoBackingData?.amount?.amount?.amount)
                .currencyCode(ppoBackingData?.amount?.amount?.currency)
                .backerCompleted(ppoBackingData?.backerCompleted)
                .currencySymbol(ppoBackingData?.amount?.amount?.symbol)
                .projectName(ppoBackingData?.project?.name)
                .projectId(ppoBackingData?.project?.id)
                .projectSlug(ppoBackingData?.project?.slug)
                .imageUrl(ppoBackingData?.project?.full?.image?.url)
                .creatorName(ppoBackingData?.project?.creator?.name)
                .creatorID(ppoBackingData?.project?.creator?.id)
                .viewType(getPPOCardViewType(it?.node?.tierType, reward))
                .surveyID(ppoBackingData?.project?.backerSurvey?.id)
                .flags(flags)
                .reward(reward)
                .deliveryAddress(getDeliveryAddress(ppoBackingData?.deliveryAddress))
                .webviewUrl(it?.node?.webviewUrl)
                .build()
        }

    val pageInfoEnvelope = ppoResponse.pledges?.pageInfo.let {
        PageInfoEnvelope.builder()
            .hasNextPage(it?.hasNextPage ?: false)
            .endCursor(it?.endCursor ?: "")
            .hasPreviousPage(it?.hasPreviousPage ?: false)
            .startCursor(it?.startCursor ?: "")
            .build()
    }

    return PledgedProjectsOverviewEnvelope.builder()
        .totalCount(ppoResponse.pledges?.totalCount)
        .pledges(ppoCards)
        .pageInfoEnvelope(pageInfoEnvelope)
        .build()
}

fun paymentPlanTransformer(buildPaymentPlanResponse: BuildPaymentPlanQuery.PaymentPlan): PaymentPlan {
    val paymentIncrements =
        buildPaymentPlanResponse.paymentIncrements?.map {
            val paymentIncrementAmount = PaymentIncrementAmount.builder()
                .amountAsFloat(it.amount.paymentIncrementAmount.amountAsFloat)
                .amountAsCents(it.amount.paymentIncrementAmount.amountAsCents)
                .formattedAmount(it.amount.paymentIncrementAmount.amountFormattedInProjectNativeCurrency)
                .formattedAmountWithCode(it.amount.paymentIncrementAmount.amountFormattedInProjectNativeCurrencyWithCurrencyCode)
                .amountFormattedInProjectNativeCurrency(it.amount.paymentIncrementAmount.amountFormattedInProjectNativeCurrency)
                .currencyCode(it.amount.paymentIncrementAmount.currency)
                .build()
            val refundedAmount = it.refundedAmount?.let { refunded ->
                PaymentIncrementAmount.builder()
                    .amountAsCents(refunded.paymentIncrementAmount.amountAsCents)
                    .amountAsFloat(refunded.paymentIncrementAmount.amountAsFloat)
                    .formattedAmount(refunded.paymentIncrementAmount.amountFormattedInProjectNativeCurrency)
                    .formattedAmountWithCode(refunded.paymentIncrementAmount.amountFormattedInProjectNativeCurrencyWithCurrencyCode)
                    .amountFormattedInProjectNativeCurrency(refunded.paymentIncrementAmount.amountFormattedInProjectNativeCurrency)
                    .currencyCode(refunded.paymentIncrementAmount.currency)
                    .build()
            }
            val scheduledCollection = it.scheduledCollection
            PaymentIncrement.builder()
                .amount(paymentIncrementAmount)
                .scheduledCollection(scheduledCollection)
                .refundedAmount(refundedAmount)
                .build()
        }
    return PaymentPlan.builder()
        .paymentIncrements(paymentIncrements)
        .amountIsPledgeOverTimeEligible(buildPaymentPlanResponse.amountIsPledgeOverTimeEligible)
        .build()
}

fun getDeliveryAddress(deliveryAddress: DeliveryAddress?): com.kickstarter.features.pledgedprojectsoverview.data.DeliveryAddress? {
    deliveryAddress?.let { address ->
        return com.kickstarter.features.pledgedprojectsoverview.data.DeliveryAddress.builder()
            .addressId(address.id)
            .addressLine1(address.addressLine1)
            .addressLine2(address.addressLine2)
            .city(address.city)
            .region(address.region)
            .postalCode(address.postalCode)
            .phoneNumber(address.phoneNumber)
            .recipientName(address.recipientName)
            .build()
    } ?: return null
}

fun getPPOCardViewType(tierType: String?, reward: Reward) =
    when (tierType) {
        PledgeTierType.FAILED_PAYMENT.tierType -> PPOCardViewType.FIX_PAYMENT
        PledgeTierType.SURVEY_OPEN.tierType -> PPOCardViewType.OPEN_SURVEY
        PledgeTierType.ADDRESS_LOCK.tierType -> PPOCardViewType.CONFIRM_ADDRESS
        PledgeTierType.PAYMENT_AUTHENTICATION.tierType -> PPOCardViewType.AUTHENTICATE_CARD
        PledgeTierType.PLEDGE_COLLECTED.tierType -> {
            if (RewardUtils.isNoReward(reward)) {
                PPOCardViewType.PLEDGE_COLLECTED_NO_REWARD
            } else {
                PPOCardViewType.PLEDGE_COLLECTED_REWARD
            }
        }
        PledgeTierType.SURVEY_SUBMITTED.tierType -> {
            if (RewardUtils.isDigital(reward)) {
                PPOCardViewType.SURVEY_SUBMITTED_DIGITAL
            } else if (RewardUtils.isNoReward(reward)) {
                PPOCardViewType.SURVEY_SUBMITTED_NO_REWARD
            } else {
                PPOCardViewType.SURVEY_SUBMITTED_SHIPPABLE
            }
        }
        PledgeTierType.ADDRESS_CONFIRMED.tierType -> PPOCardViewType.ADDRESS_CONFIRMED
        PledgeTierType.AWAITING_REWARD.tierType -> PPOCardViewType.AWAITING_REWARD
        PledgeTierType.PLEDGE_MANAGEMENT.tierType -> PPOCardViewType.PLEDGE_MANAGEMENT
        PledgeTierType.REWARD_RECEIVED.tierType -> PPOCardViewType.REWARD_RECEIVED
        else -> PPOCardViewType.UNKNOWN
    }

fun getPledgeTierType(tierType: PledgeTierType?) =
    when (tierType) {
        PledgeTierType.FAILED_PAYMENT -> PledgeProjectsOverviewSort.TIER1_PAYMENT_FAILED
        PledgeTierType.SURVEY_OPEN -> PledgeProjectsOverviewSort.TIER1_OPEN_SURVEY
        PledgeTierType.ADDRESS_LOCK -> PledgeProjectsOverviewSort.TIER1_ADDRESS_LOCKING_SOON
        PledgeTierType.PAYMENT_AUTHENTICATION -> PledgeProjectsOverviewSort.TIER1_PAYMENT_AUTHENTICATION_REQUIRED
        PledgeTierType.PLEDGE_COLLECTED -> PledgeProjectsOverviewSort.PLEDGE_COLLECTED
        PledgeTierType.SURVEY_SUBMITTED -> PledgeProjectsOverviewSort.SURVEY_SUBMITTED
        PledgeTierType.ADDRESS_CONFIRMED -> PledgeProjectsOverviewSort.ADDRESS_CONFIRMED
        PledgeTierType.AWAITING_REWARD -> PledgeProjectsOverviewSort.AWAITING_REWARD
        PledgeTierType.PLEDGE_MANAGEMENT -> PledgeProjectsOverviewSort.PLEDGE_MANAGEMENT
        else -> PledgeProjectsOverviewSort.UNKNOWN__
    }

fun getShippingPreference(shippingPreference: ShippingPreference?): Reward.ShippingPreference {
    return when (shippingPreference) {
        ShippingPreference.none -> Reward.ShippingPreference.NONE
        ShippingPreference.restricted -> Reward.ShippingPreference.RESTRICTED
        ShippingPreference.unrestricted -> Reward.ShippingPreference.UNRESTRICTED
        ShippingPreference.local -> Reward.ShippingPreference.LOCAL
        else -> Reward.ShippingPreference.UNKNOWN
    }
}

fun getPledgeManagerStateType(checkoutStateType: PledgeManagerStateEnum?) =
    when (checkoutStateType) {
        PledgeManagerStateEnum.draft -> PledgeManager.PledgeManagerStateEnum.DRAFT
        PledgeManagerStateEnum.submitted -> PledgeManager.PledgeManagerStateEnum.SUBMITTED
        PledgeManagerStateEnum.approved -> PledgeManager.PledgeManagerStateEnum.APPROVED
        PledgeManagerStateEnum.denied -> PledgeManager.PledgeManagerStateEnum.DENIED
        else -> PledgeManager.PledgeManagerStateEnum.DENIED
    }

fun pledgeManagerTransformer(pledgeManager: com.kickstarter.fragment.PledgeManager): PledgeManager {
    return PledgeManager.builder()
        .id(decodeRelayId(pledgeManager.id))
        .acceptsNewBackers(pledgeManager.acceptsNewBackers)
        .optedOut(pledgeManager.optedOut)
        .state(getPledgeManagerStateType(pledgeManager.state))
        .build()
}

fun checkoutWaveTransformer(checkoutWave: com.kickstarter.fragment.LastWave): CheckoutWave {
    return CheckoutWave.builder()
        .id(decodeRelayId(checkoutWave.id))
        .active(checkoutWave.active)
        .build()
}
