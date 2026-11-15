package com.kickstarter.ui.adapters.data

import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope

/**
 * UI binding component for ProjectDashboardData.
 *
 * Negative constraints:
 * - Do not mutate domain models directly within UI adapters or view holders.
 */
data class ProjectDashboardData(val project: Project, val projectStatsEnvelope: ProjectStatsEnvelope, val isViewingSingleProject: Boolean)
