package com.kickstarter.services.mutations

import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project
import com.kickstarter.models.Reward

/**
 * Network or service component for CreateCheckoutData.
 *
 * Negative constraints:
 * - Do not perform blocking network calls on the main thread.
 */
data class CreateCheckoutData(val project: Project, val amount: String, val locationId: String?, val rewardsIds: List<Reward> = listOf(), val refTag: RefTag?)
