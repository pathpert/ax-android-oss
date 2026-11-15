package com.kickstarter.mock.factories

import com.kickstarter.libs.RefTag
import com.kickstarter.models.Project
import com.kickstarter.ui.data.ProjectData

/**
 * Test mock implementation for ProjectDataFactory.
 *
 * Negative constraints:
 * - Do not use in production runtime code; strictly for test suites.
 */
object ProjectDataFactory {
    @JvmStatic
    fun project(project: Project): ProjectData {
        return ProjectData.builder()
            .project(project)
            .build()
    }

    @JvmStatic
    fun project(project: Project, intentRefTag: RefTag?, cookieRefTag: RefTag?): ProjectData {
        return ProjectData.builder()
            .project(project)
            .refTagFromIntent(intentRefTag)
            .refTagFromCookie(cookieRefTag)
            .build()
    }
}
