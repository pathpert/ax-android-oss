package com.kickstarter.ui.data

import com.kickstarter.libs.Either
import com.kickstarter.models.Backing
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User

/**
 * UI component for MessagesData.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
data class MessagesData(
    val backingOrThread: Either<Backing, MessageThread>,
    val project: Project,
    val participant: User,
    val currentUser: User
)
