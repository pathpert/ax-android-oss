package com.kickstarter.features.comments.viewmodel

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.comments.data.CommentsRepository
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Test

class CommentsFeatureViewModelTest : KSRobolectricTestCase() {

    private fun createRepository(
        commentsEnvelope: CommentEnvelope = CommentEnvelopeFactory.emptyCommentsEnvelope()
    ): CommentsRepository {
        return object : CommentsRepository {
            override fun getProjectComments(slug: String, cursor: String): Observable<CommentEnvelope> =
                Observable.just(commentsEnvelope)

            override fun getProjectUpdateComments(updateId: String, cursor: String): Observable<CommentEnvelope> =
                Observable.just(commentsEnvelope)

            override fun getComment(commentId: String): Observable<Comment> =
                Observable.empty()

            override fun getProject(projectId: String): Observable<Project?> =
                Observable.just(ProjectFactory.project())

            override fun cleanDisposables() {}
        }
    }

    @Test
    fun `test comments composer status when backer is logged in`() {
        val user = UserFactory.user()
        val project = ProjectFactory.backedProject()
        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(project))
        val env = environment().toBuilder()
            .currentUserV2(MockCurrentUserV2(user))
            .build()

        val vm = CommentsFeatureViewModel(env, intent, createRepository())

        val composerStatusObserver = TestObserver<CommentComposerStatus>()
        val showComposerObserver = TestObserver<Boolean>()

        vm.outputs.commentComposerStatus().subscribe(composerStatusObserver)
        vm.outputs.showCommentComposer().subscribe(showComposerObserver)

        composerStatusObserver.assertValue(CommentComposerStatus.ENABLED)
        showComposerObserver.assertValue(true)
    }

    @Test
    fun `test backPressed triggers closeCommentsPage`() {
        val vm = CommentsFeatureViewModel(environment(), null, createRepository())
        val closeObserver = TestObserver<Unit>()

        vm.outputs.closeCommentsPage().subscribe(closeObserver)
        vm.inputs.backPressed()

        closeObserver.assertValueCount(1)
    }

    @Test
    fun `test onShowGuideLinesLinkClicked triggers showCommentGuideLinesLink`() {
        val vm = CommentsFeatureViewModel(environment(), null, createRepository())
        val guidelinesObserver = TestObserver<Unit>()

        vm.outputs.showCommentGuideLinesLink().subscribe(guidelinesObserver)
        vm.inputs.onShowGuideLinesLinkClicked()

        guidelinesObserver.assertValueCount(1)
    }
}
