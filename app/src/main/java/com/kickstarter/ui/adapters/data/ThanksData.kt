package com.kickstarter.ui.adapters.data

import android.util.Pair
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.CheckoutData

/**
 * UI binding component for ThanksData.
 *
 * Negative constraints:
 * - Do not mutate domain models directly within UI adapters or view holders.
 */
class ThanksData(
    val backedProject: Project,
    val checkoutData: CheckoutData,
    val category: Category,
    val recommendedProjects: List<Pair<Project, DiscoveryParams>>
)
