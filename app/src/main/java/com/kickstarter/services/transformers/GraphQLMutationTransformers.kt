package com.kickstarter.services.transformers

import com.apollographql.apollo3.api.Optional
import com.google.gson.Gson
import com.kickstarter.CreateAttributionEventMutation
import com.kickstarter.CreateOrUpdateBackingAddressMutation
import com.kickstarter.TriggerThirdPartyEventMutation
import com.kickstarter.UpdateBackerCompletedMutation
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.UpdateBackerCompletedData
import com.kickstarter.type.AppDataInput
import com.kickstarter.type.CreateAttributionEventInput
import com.kickstarter.type.CreateOrUpdateBackingAddressInput
import com.kickstarter.type.ThirdPartyEventItemInput
import com.kickstarter.type.UpdateBackerCompletedInput
import com.kickstarter.viewmodels.usecases.TPEventInputData

/**
 * From KS dataModel TPEventInputData, transform it into
 * GraphQL defined mutation TriggerThirdPartyEventMutation
 */
fun getTriggerThirdPartyEventMutation(eventInput: TPEventInputData): TriggerThirdPartyEventMutation {
    val graphAppData = AppDataInput(
        advertiserTrackingEnabled = eventInput.appData.iOSConsent,
        applicationTrackingEnabled = eventInput.appData.androidConsent,
        extinfo = eventInput.appData.extInfo
    )

    val items: List<ThirdPartyEventItemInput> = eventInput.items
        .map {
            ThirdPartyEventItemInput(
                itemId = it.itemId,
                itemName = it.itemName,
                price = if (it.price.isNotNull()) Optional.present(it.price) else Optional.absent()
            )
        }

    return TriggerThirdPartyEventMutation(
        userId = if (eventInput.isNull()) Optional.absent() else Optional.present(eventInput.userId),
        eventName = eventInput.eventName,
        deviceId = eventInput.deviceId,
        firebaseScreen = if (eventInput.firebaseScreen.isNull()) Optional.absent() else Optional.present(
            eventInput.firebaseScreen
        ),
        firebasePreviousScreen = if (eventInput.firebasePreviousScreen.isNull()) Optional.absent() else Optional.present(
            eventInput.firebasePreviousScreen
        ),
        projectId = eventInput.projectId,
        pledgeAmount = if (eventInput.pledgeAmount.isNull()) Optional.absent() else Optional.present(
            eventInput.pledgeAmount
        ),
        shipping = if (eventInput.shipping.isNull()) Optional.absent() else Optional.present(
            eventInput.shipping
        ),
        appData = if (graphAppData.isNull()) Optional.absent() else Optional.present(graphAppData),
        items = if (items.isNull()) Optional.absent() else Optional.present(items),
        transactionId = if (eventInput.isNull()) Optional.absent() else Optional.present(eventInput.transactionId)
    )
}

/**
 * From KS dataModel CreateAttributionEventData, transform it into
 * GraphQL defined mutation CreateAttributionEventMutation
 */
fun getCreateAttributionEventMutation(
    eventInput: CreateAttributionEventData,
    gson: Gson
): CreateAttributionEventMutation {

    // Use gson to convert map -> JSON type to match mutation
    val eventPropertiesJson = gson.toJson(eventInput.eventProperties)

    val input = CreateAttributionEventInput(
        eventName = eventInput.eventName,
        eventProperties = if (eventPropertiesJson.isPresent()) Optional.present(eventPropertiesJson) else Optional.absent(),
        projectId = if (eventInput.projectId.isNullOrBlank()) Optional.absent() else Optional.present(
            eventInput.projectId
        )
    )
    return CreateAttributionEventMutation(input = input)
}

fun getCreateOrUpdateBackingAddressMutation(eventInput: CreateOrUpdateBackingAddressData): CreateOrUpdateBackingAddressMutation {

    val graphInput = CreateOrUpdateBackingAddressInput(
        addressId = eventInput.addressID,
        backingId = eventInput.backingID
    )
    return CreateOrUpdateBackingAddressMutation(input = graphInput)
}

fun getUpdateBackerCompletedMutation(inputData: UpdateBackerCompletedData): UpdateBackerCompletedMutation {

    val graphInput = UpdateBackerCompletedInput(
        id = inputData.backingID,
        backerCompleted = inputData.backerCompleted
    )
    return UpdateBackerCompletedMutation(input = graphInput)
}
