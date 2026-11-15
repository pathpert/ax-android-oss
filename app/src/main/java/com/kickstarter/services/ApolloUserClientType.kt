package com.kickstarter.services

import com.kickstarter.CreatePasswordMutation
import com.kickstarter.SendEmailVerificationMutation
import com.kickstarter.UpdateUserCurrencyMutation
import com.kickstarter.UpdateUserEmailMutation
import com.kickstarter.UpdateUserPasswordMutation
import com.kickstarter.models.Comment
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.type.CurrencyCode
import io.reactivex.Observable

/**
 * Apollo GraphQL operations for User Profile, Security, Currency Preferences, and Comments.
 */
interface ApolloUserClientType {

    fun userPrivacy(): Observable<UserPrivacy>

    fun updateUserPassword(
        currentPassword: String = "",
        newPassword: String,
        confirmPassword: String
    ): Observable<UpdateUserPasswordMutation.Data>

    fun updateUserEmail(
        email: String,
        currentPassword: String
    ): Observable<UpdateUserEmailMutation.Data>

    fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data>
    fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data>

    fun createPassword(
        password: String,
        confirmPassword: String
    ): Observable<CreatePasswordMutation.Data>

    fun clearUnseenActivity(): Observable<Int>
    fun getComment(commentableId: String): Observable<Comment>

    fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun getProjectComments(
        slug: String,
        cursor: String,
        limit: Int = APOLLO_PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun getRepliesForComment(
        comment: Comment,
        cursor: String? = null,
        pageSize: Int = APOLLO_REPLIES_PAGE_SIZE
    ): Observable<CommentEnvelope>

    fun createComment(comment: PostCommentData): Observable<Comment>
}
