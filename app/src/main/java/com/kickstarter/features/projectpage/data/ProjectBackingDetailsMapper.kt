package com.kickstarter.features.projectpage.data

import com.kickstarter.R
import com.kickstarter.libs.Either
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.AI
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.CAMPAIGN
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ENVIRONMENT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.FAQS
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.OVERVIEW
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.RISKS
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.data.PledgeFlowContext
import java.math.RoundingMode

/**
 * Boundary mapper for project backing details, contextual flow, and UI state resolution.
 *
 * Negative constraints:
 * - Do not pass unvalidated projects or floating-point currency doubles without localized formatting.
 */
object ProjectBackingDetailsMapper {

    fun backingDetailsIsVisible(project: Project): Boolean {
        return project.isBacking() && project.isLive || project.backing()?.isErrored() == true
    }

    fun backingDetailsTitle(project: Project): Int {
        return if (project.backing()?.isErrored() == true) {
            R.string.Payment_failure
        } else {
            R.string.Youre_a_backer
        }
    }

    fun backingDetailsSubtitle(project: Project, ksCurrency: KSCurrency): Either<String, Int>? {
        return project.backing()?.let { backing ->
            if (backing.status() == Backing.STATUS_ERRORED) {
                Either.Right(R.string.We_cant_process_your_pledge)
            } else {
                val reward = project.rewards()?.firstOrNull { it.id() == backing.rewardId() }
                val title = reward?.let { "• ${it.title()}" } ?: ""
                val backingAmount = backing.amount()
                val formattedAmount = ksCurrency.format(backingAmount, project, RoundingMode.HALF_UP)
                Either.Left("$formattedAmount $title".trim())
            }
        }
    }

    fun getSelectedTabContextName(selectedTabIndex: Int): String =
        when (selectedTabIndex) {
            ProjectPagerTabs.OVERVIEW.ordinal -> OVERVIEW.contextName
            ProjectPagerTabs.CAMPAIGN.ordinal -> CAMPAIGN.contextName
            ProjectPagerTabs.FAQS.ordinal -> FAQS.contextName
            ProjectPagerTabs.RISKS.ordinal -> RISKS.contextName
            ProjectPagerTabs.USE_OF_AI.ordinal -> AI.contextName
            ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT.ordinal -> ENVIRONMENT.contextName
            else -> OVERVIEW.contextName
        }

    fun pledgeFlowContext(project: Project, currentUser: User?): PledgeFlowContext? {
        return when {
            project.userIsCreator(currentUser) -> null
            project.isLive && !project.isBacking() -> PledgeFlowContext.NEW_PLEDGE
            !project.isLive && project.backing()?.isErrored() ?: false -> PledgeFlowContext.FIX_ERRORED_PLEDGE
            else -> null
        }
    }
}
