package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.Environment
import com.kickstarter.models.User

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
