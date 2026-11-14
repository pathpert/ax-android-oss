package com.kickstarter.viewmodels.usecases

import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.ApolloClientTypeV2

@Deprecated(
    message = "Legacy monolithic path replaced by HomeScreenViewModel under com.kickstarter.features.home.viewmodel.",
    replaceWith = ReplaceWith("HomeScreenViewModel", "com.kickstarter.features.home.viewmodel.HomeScreenViewModel")
)
class GetUserPrivacyUseCaseV2(
    private val apolloClient: ApolloClientTypeV2
) {
    fun getUserPrivacy(): io.reactivex.Observable<UserPrivacy> {
        return apolloClient.userPrivacy()
    }
}
