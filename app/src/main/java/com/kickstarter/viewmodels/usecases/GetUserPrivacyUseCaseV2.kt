package com.kickstarter.viewmodels.usecases

import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.ApolloClientTypeV2

/**
 * ViewModel managing state transitions and user intents for GetUserPrivacyUseCaseV2.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
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
