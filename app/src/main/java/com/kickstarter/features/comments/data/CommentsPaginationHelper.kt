package com.kickstarter.features.comments.data

import android.util.Pair
import com.kickstarter.libs.loadmore.ApolloPaginateV2
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.data.CommentCardData
import io.reactivex.Observable

/**
 * Encapsulated pagination builder for comments streams.
 *
 * Negative Constraints:
 * - Do NOT instantiate without a valid project reference.
 */
object CommentsPaginationHelper {

    fun buildPaginate(
        nextPage: Observable<Unit>,
        startOverWith: Observable<Pair<Project, Update?>>,
        projectProvider: () -> Project,
        currentUserProvider: () -> User?,
        loadWithComments: (Observable<Pair<Project, Update?>>, String) -> Observable<CommentEnvelope>
    ): ApolloPaginateV2<CommentCardData, CommentEnvelope, Pair<Project, Update?>> {
        return ApolloPaginateV2.builder<CommentCardData, CommentEnvelope, Pair<Project, Update?>>()
            .nextPage(nextPage)
            .distinctUntilChanged(true)
            .startOverWith(startOverWith)
            .envelopeToListOfData {
                CommentsListMapper.mapToCommentCardDataList(it, projectProvider(), currentUserProvider())
            }
            .loadWithParams { loadWithComments(Observable.just(it.first), it.second) }
            .clearWhenStartingOver(false)
            .build()
    }
}
