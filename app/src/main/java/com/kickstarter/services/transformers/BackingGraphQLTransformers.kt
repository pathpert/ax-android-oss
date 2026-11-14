package com.kickstarter.services.transformers

import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Avatar
import com.kickstarter.models.Backing
import com.kickstarter.models.Order
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.PaymentIncrementAmount
import com.kickstarter.models.PaymentIncrementBadge
import com.kickstarter.models.PaymentIncrementBadgeVariant
import com.kickstarter.models.PaymentSource
import com.kickstarter.models.User
import com.kickstarter.type.CheckoutStateEnum
import com.kickstarter.type.CreditCardPaymentType

/**
 * Transform the Backing GraphQL data structure into our own Backing data model
 * @param fragment.Backing backingGr
 * @return Backing
 */
fun backingTransformer(backingGr: com.kickstarter.fragment.Backing?): Backing {
    val payment = backingGr?.paymentSource?.paymentSourceFragment?.let { paymentSource ->
        if (paymentSource.onBankAccount != null) {
            PaymentSource.builder()
                .paymentType(CreditCardPaymentType.BANK_ACCOUNT.rawValue)
                .id(paymentSource.onBankAccount.id)
                .lastFour(paymentSource.onBankAccount.lastFour)
                .bankName(paymentSource.onBankAccount.bankName)
                .build()
        } else if (paymentSource.onCreditCard != null) {
            PaymentSource.builder()
                .state(paymentSource.onCreditCard.state.toString())
                .type(paymentSource.onCreditCard.type.rawValue)
                .paymentType(CreditCardPaymentType.CREDIT_CARD.rawValue)
                .id(paymentSource.onCreditCard.id)
                .expirationDate(paymentSource.onCreditCard.expirationDate)
                .lastFour(paymentSource.onCreditCard.lastFour)
                .build()
        } else {
            null
        }
    }

    val addOns = backingGr?.addOns?.let {
        return@let getAddOnsList(it)
    }

    val id = decodeRelayId(backingGr?.id)?.let { it } ?: 0

    val location = backingGr?.location?.location
    val locationId = decodeRelayId(location?.id)
    val projectId = decodeRelayId(backingGr?.project?.project?.id) ?: -1
    val shippingAmount = backingGr?.shippingAmount
    val items = backingGr?.reward?.items
    val rewardImage = backingGr?.reward?.rewardImage
    val reward = backingGr?.reward?.reward?.let { reward ->
        return@let rewardTransformer(
            reward,
            allowedAddons = reward.allowedAddons.isNotNull(),
            rewardItems = complexRewardItemsTransformer(items?.rewardItems),
            rewardImage = rewardImage
        )
    }

    val backerData = backingGr?.backer?.user
    val nameBacker = backerData?.let { it.name } ?: ""
    val backerId = decodeRelayId(backerData?.id) ?: -1
    val avatar = Avatar.builder()
        .medium(backerData?.imageUrl)
        .build()
    val completedByBacker = backingGr?.backerCompleted ?: false

    val backer = User.builder()
        .id(backerId)
        .name(nameBacker)
        .avatar(avatar)
        .build()
    val status = backingGr?.status?.rawValue ?: ""

    val isPostCampaign = backingGr?.isPostCampaign ?: false
    val incremental = backingGr?.incremental ?: false

    fun getPaymentIncrementBadgeVariantType(badgeVariant: com.kickstarter.type.PaymentIncrementBadgeVariant) =
        when (badgeVariant) {
            com.kickstarter.type.PaymentIncrementBadgeVariant.RED -> PaymentIncrementBadgeVariant.RED
            com.kickstarter.type.PaymentIncrementBadgeVariant.GRAY -> PaymentIncrementBadgeVariant.GRAY
            com.kickstarter.type.PaymentIncrementBadgeVariant.GREEN -> PaymentIncrementBadgeVariant.GREEN
            com.kickstarter.type.PaymentIncrementBadgeVariant.DANGER -> PaymentIncrementBadgeVariant.DANGER
            com.kickstarter.type.PaymentIncrementBadgeVariant.PURPLE -> PaymentIncrementBadgeVariant.PURPLE
            else -> PaymentIncrementBadgeVariant.GRAY
        }
    val paymentIncrements = backingGr?.paymentIncrements?.map {
        val paymentIncrementAmount = PaymentIncrementAmount.builder()
            .amountAsCents(it.paymentIncrement.amount.paymentIncrementAmount.amountAsCents)
            .amountAsFloat(it.paymentIncrement.amount.paymentIncrementAmount.amountAsFloat)
            .formattedAmount(it.paymentIncrement.amount.paymentIncrementAmount.amountFormattedInProjectNativeCurrency)
            .formattedAmountWithCode(it.paymentIncrement.amount.paymentIncrementAmount.amountFormattedInProjectNativeCurrencyWithCurrencyCode)
            .amountFormattedInProjectNativeCurrency(it.paymentIncrement.amount.paymentIncrementAmount.amountFormattedInProjectNativeCurrency)
            .currencyCode(it.paymentIncrement.amount.paymentIncrementAmount.currency)
            .build()
        val paymentIncrementBadge = it.paymentIncrement.badge?.let { badgeGr ->
            PaymentIncrementBadge.builder()
                .copy(badgeGr.paymentIncrementBadge.copy)
                .variant(getPaymentIncrementBadgeVariantType(badgeVariant = badgeGr.paymentIncrementBadge.variant))
                .build()
        }
        val refundedAmount = it.paymentIncrement.refundedAmount?.let { refunded ->
            PaymentIncrementAmount.builder()
                .amountAsCents(refunded.paymentIncrementAmount.amountAsCents)
                .amountAsFloat(refunded.paymentIncrementAmount.amountAsFloat)
                .formattedAmount(refunded.paymentIncrementAmount.amountFormattedInProjectNativeCurrency)
                .formattedAmountWithCode(refunded.paymentIncrementAmount.amountFormattedInProjectNativeCurrencyWithCurrencyCode)
                .amountFormattedInProjectNativeCurrency(refunded.paymentIncrementAmount.amountFormattedInProjectNativeCurrency)
                .currencyCode(refunded.paymentIncrementAmount.currency)
                .build()
        }
        val scheduleCollection = it.paymentIncrement.scheduledCollection
        PaymentIncrement.builder()
            .amount(paymentIncrementAmount)
            .paymentIncrementBadge(paymentIncrementBadge)
            .scheduledCollection(scheduleCollection)
            .state(it.paymentIncrement.state)
            .stateReason(it.paymentIncrement.stateReason)
            .refundedAmount(refundedAmount)
            .refundUpdatedAmountInProjectNativeCurrency(it.paymentIncrement.refundUpdatedAmountInProjectNativeCurrency)
            .build()
    }

    fun getCheckoutStateType(checkoutStateType: CheckoutStateEnum) =
        when (checkoutStateType) {
            CheckoutStateEnum.in_progress -> Order.CheckoutStateEnum.IN_PROGRESS
            CheckoutStateEnum.complete -> Order.CheckoutStateEnum.COMPLETE
            else -> Order.CheckoutStateEnum.NOT_STARTED
        }

    val order = backingGr?.order?.order?.let { order ->
        Order.builder()
            .id(order.id)
            .checkoutState(getCheckoutStateType(order.checkoutState))
            .currency(order.currency)
            .total(order.total)
            .build()
    }

    return Backing.builder()
        .amount(backingGr?.amount?.amount?.amount?.toDouble() ?: 0.0)
        .bonusAmount(backingGr?.bonusAmount?.amount?.amount?.toDouble() ?: 0.0)
        .paymentSource(payment)
        .paymentIncrements(paymentIncrements)
        .backerId(backerId)
        .backerUrl(backerData?.imageUrl)
        .backerName(nameBacker)
        .backer(backer)
        .backingDetailsPageRoute(backingGr?.backingDetailsPageRoute)
        .id(id)
        .incremental(incremental)
        .reward(reward)
        .addOns(addOns)
        .rewardId(reward?.id())
        .locationId(locationId)
        .locationName(location?.displayableName)
        .order(order)
        .pledgedAt(backingGr?.pledgedOn)
        .projectId(projectId)
        .sequence(backingGr?.sequence?.toLong() ?: 0)
        .shippingAmount(shippingAmount?.amount?.amount?.toFloat() ?: 0f)
        .status(status)
        .cancelable(backingGr?.cancelable ?: false)
        .completedByBacker(completedByBacker)
        .isPostCampaign(isPostCampaign)
        .build()
}
