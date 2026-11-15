package com.kickstarter.viewmodels.projectpage

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Pair
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.CurrentConfigTypeV2
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.extensions.updateProjectWith
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.Observable
import java.net.CookieManager

/**
 * Mapping and data helper methods for [PrelaunchProjectViewModel].
 */
internal object PrelaunchProjectHelper {

    fun mapProject(
        latestProjectData: ProjectData,
        initProject: Pair<Project, KsOptional<User>>,
    ): Project {
        return if (latestProjectData.project().isStarred() != initProject.first.isStarred()) {
            latestProjectData.project()
        } else {
            initProject.first
        }
    }

    fun saveProject(apolloClient: ApolloClientTypeV2, project: Project): Observable<Project> {
        return apolloClient.watchProject(project)
            .compose(Transformers.neverErrorV2())
    }

    fun unSaveProject(apolloClient: ApolloClientTypeV2, project: Project): Observable<Project> {
        return apolloClient.unWatchProject(project).compose(Transformers.neverErrorV2())
    }

    fun toggleProjectSave(apolloClient: ApolloClientTypeV2, project: Project): Observable<Project> {
        return if (project.isStarred()) {
            unSaveProject(apolloClient, project)
        } else {
            saveProject(apolloClient, project)
        }
    }

    fun buildProjectData(
        refTagFromIntent: KsOptional<RefTag?>,
        refTagFromCookie: KsOptional<RefTag?>,
        fullDeeplink: KsOptional<Uri?>,
        project: Project,
    ): ProjectData {
        return ProjectData
            .builder()
            .refTagFromIntent(refTagFromIntent.getValue())
            .refTagFromCookie(refTagFromCookie.getValue())
            .fullDeeplink(fullDeeplink.getValue())
            .project(project)
            .build()
    }

    fun storeCurrentCookieRefTag(
        data: ProjectData,
        cookieManager: CookieManager,
        sharedPreferences: SharedPreferences
    ): ProjectData {
        return data
            .toBuilder()
            .refTagFromCookie(RefTagUtils.storedCookieRefTagForProject(data.project(), cookieManager, sharedPreferences))
            .build()
    }

    fun loadProject(
        intent: Intent,
        apolloClient: ApolloClientTypeV2,
        currentConfig: CurrentConfigTypeV2,
        currentUser: CurrentUserTypeV2
    ): Observable<Project> = ProjectIntentMapper
        .project(intent, apolloClient).doOnError {
            val problem = ProblemDetailsMapper.fromThrowable(it, "load prelaunch project")
            timber.log.Timber.w(ProblemException(problem, it), "Failed to load prelaunch project: %s", problem.detail)
        }
        .onErrorResumeNext(Observable.empty())
        .withLatestFrom(
            currentConfig.observable(),
            currentUser.observable(),
        ) { project, config, user ->
            if (user.isPresent()) {
                project.updateProjectWith(config, user.getValue())
            } else {
                project.updateProjectWith(config, null)
            }
        }
}
