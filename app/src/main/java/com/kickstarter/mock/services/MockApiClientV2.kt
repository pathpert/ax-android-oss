package com.kickstarter.mock.services

import com.google.gson.JsonObject
import com.kickstarter.mock.factories.BackingFactory.backing
import com.kickstarter.mock.factories.CategoryFactory.musicCategory
import com.kickstarter.mock.factories.LocationFactory.sydney
import com.kickstarter.mock.factories.SurveyResponseFactory
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.models.Backing
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Message
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.ProjectNotification
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope.Companion.builder
import com.kickstarter.services.apiresponses.OAuthTokenEnvelope
import com.kickstarter.ui.data.MessageSubject
import io.reactivex.Observable

open class MockApiClientV2 : BaseMockApiClientV2() {

    override fun loginWithFacebook(
        fbAccessToken: String,
        code: String
    ): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(user().toBuilder().build())
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun registerWithFacebook(
        fbAccessToken: String,
        sendNewsletters: Boolean
    ): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(user().toBuilder().build())
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun fetchProjectBacking(project: Project, user: User): Observable<Backing> {
        return Observable.just(backing(project, user))
    }

    override fun fetchCategory(param: String): Observable<Category> {
        return Observable.just(musicCategory())
    }

    override fun fetchCategory(category: Category): Observable<Category> {
        return Observable.empty()
    }

    override fun fetchCurrentUser(): Observable<User> {
        return Observable.empty()
    }

    override fun fetchLocation(param: String): Observable<Location> {
        return Observable.just(sydney())
    }

    override fun loginWithCodes(
        codeVerifier: String,
        code: String,
        clientId: String
    ): Observable<OAuthTokenEnvelope> {
        return Observable.just(
            OAuthTokenEnvelope.builder()
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun login(email: String, password: String): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(user().toBuilder().build())
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun login(
        email: String,
        password: String,
        code: String
    ): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(user().toBuilder().build())
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun markAsRead(messageThread: MessageThread): Observable<MessageThread> {
        return Observable.empty()
    }

    override fun postBacking(
        project: Project,
        backing: Backing,
        checked: Boolean
    ): Observable<Backing> {
        return Observable.just(backing())
    }

    override fun registerPushToken(token: String): Observable<JsonObject> {
        return Observable.empty()
    }

    override fun resetPassword(email: String): Observable<User> {
        return Observable.just(user())
    }

    override fun sendMessage(messageSubject: MessageSubject, body: String): Observable<Message> {
        return Observable.empty()
    }

    override fun signup(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        sendNewsletters: Boolean
    ): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(user().toBuilder().name(name).build())
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun saveProject(project: Project): Observable<Project> {
        return Observable.just(project.toBuilder().isStarred(true).build())
    }

    override fun fetchSurveyResponse(surveyResponseId: Long): Observable<SurveyResponse> {
        return Observable.just(
            SurveyResponseFactory.surveyResponse().toBuilder().id(surveyResponseId).build()
        )
    }

    override fun toggleProjectSave(project: Project): Observable<Project> {
        return Observable.just(project.toBuilder().isStarred(!project.isStarred()).build())
    }

    override fun fetchUnansweredSurveys(): Observable<List<SurveyResponse>> {
        return Observable.just(
            listOf(
                SurveyResponseFactory.surveyResponse(),
                SurveyResponseFactory.surveyResponse()
            )
        )
    }

    override fun updateProjectNotifications(
        projectNotification: ProjectNotification,
        checked: Boolean
    ): Observable<ProjectNotification> {
        return Observable.just(
            projectNotification.toBuilder().email(checked).mobile(checked).build()
        )
    }

    override fun updateUserSettings(user: User): Observable<User> {
        val map: Map<String, Any> = mapOf(Pair("user", user))
        observable.onNext(
            Pair("update_user_settings", map)
        )
        return Observable.just(user)
    }

    override fun verifyEmail(token: String): Observable<EmailVerificationEnvelope> {
        return Observable.just(
            builder()
                .code(200)
                .message("")
                .build()
        )
    }
}
