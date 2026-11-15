package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Environment
import com.kickstarter.models.User

/**
 * ViewModel managing state transitions and user intents for RefreshUserUseCase.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
@Deprecated(
    message = "Legacy monolithic path replaced by HomeScreenViewModel under com.kickstarter.features.home.viewmodel.",
    replaceWith = ReplaceWith("HomeScreenViewModel", "com.kickstarter.features.home.viewmodel.HomeScreenViewModel")
)
class RefreshUserUseCase(environment: Environment) {
    private val currentUserV2 = requireNotNull(environment.currentUserV2())

    fun refresh(newUser: User) {
        currentUserV2.refresh(newUser)
    }
}
