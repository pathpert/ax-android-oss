package com.kickstarter.services;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kickstarter.libs.utils.extensions.AnyExtKt;
import com.kickstarter.libs.utils.extensions.BoolenExtKt;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.apirequests.BackingBody;
import com.kickstarter.services.apirequests.LoginWithFacebookBody;
import com.kickstarter.services.apirequests.MessageBody;
import com.kickstarter.services.apirequests.PKCEBody;
import com.kickstarter.services.apirequests.ProjectNotificationBody;
import com.kickstarter.services.apirequests.PushTokenBody;
import com.kickstarter.services.apirequests.RegisterWithFacebookBody;
import com.kickstarter.services.apirequests.ResetPasswordBody;
import com.kickstarter.services.apirequests.SettingsBody;
import com.kickstarter.services.apirequests.SignupBody;
import com.kickstarter.services.apirequests.XauthBody;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope;
import com.kickstarter.services.apiresponses.OAuthTokenEnvelope;
import com.kickstarter.services.apiresponses.StarEnvelope;
import com.kickstarter.services.apiresponses.UpdatesEnvelope;
import com.kickstarter.ui.data.MessageSubject;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * @deprecated Legacy monolithic Retrofit API client. Use domain-specific clients or feature repositories.
 */
@Deprecated
public final class ApiClientV2 extends BaseApiClientV2 {

  public ApiClientV2(final @NonNull ApiServiceV2 service, final @NonNull Gson gson) {
    super(service, gson);
  }

  @Override
  public @NonNull Observable<UpdatesEnvelope> fetchUpdates(final @NonNull Project project) {
    return this.service
            .updates(project.param())
            .lift(apiErrorOperator())
            .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<UpdatesEnvelope> fetchUpdates(final @NonNull String paginationPath) {
    return this.service
            .paginatedUpdates(paginationPath)
            .lift(apiErrorOperator())
            .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken) {
    return this.service
      .login(LoginWithFacebookBody.builder().accessToken(accessToken).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken, final @NonNull String code) {
    return this.service
      .login(LoginWithFacebookBody.builder().accessToken(fbAccessToken).code(code).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<OAuthTokenEnvelope> loginWithCodes(final @NonNull String codeVerifier, final @NonNull String code, final @NonNull String clientId) {
    return this.service
            .login(PKCEBody.builder()
                    .code(code)
                    .codeVerifier(codeVerifier)
                    .clientId(clientId)
                    .build())
            .lift(apiErrorOperator())
            .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password) {
    return this.service
      .login(XauthBody.builder()
        .email(email)
        .password(password)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password,
    final @NonNull String code) {
    return this.service
      .login(XauthBody.builder()
        .email(email)
        .password(password)
        .code(code)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThread> markAsRead(final @NonNull MessageThread messageThread) {
    return this.service
      .markAsRead(messageThread.id())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<JsonObject> registerPushToken(final @NonNull String token) {
    return this.service
      .registerPushToken(PushTokenBody.builder().token(token).pushServer("development").build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken, final boolean sendNewsletters) {
    return this.service
      .login(RegisterWithFacebookBody.builder()
        .accessToken(fbAccessToken)
        .sendNewsletters(sendNewsletters)
        .newsletterOptIn(sendNewsletters)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<User> resetPassword(final @NonNull String email) {
    return this.service
      .resetPassword(ResetPasswordBody.builder().email(email).build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Message> sendMessage(final @NonNull MessageSubject messageSubject, final @NonNull String body) {
    final MessageBody messageBody = MessageBody.builder().body(body).build();

    return messageSubject
      .value(
        backing -> this.service
          .sendMessageToBacking(backing.projectId(), backing.backerId(), messageBody),
        messageThread -> this.service
          .sendMessageToThread(messageThread.id(), messageBody),
        project -> this.service
          .sendMessageToProject(project.id(), messageBody)
      )
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<AccessTokenEnvelope> signup(final @NonNull String name, final @NonNull String email,
    final @NonNull String password, final @NonNull String passwordConfirmation,
    final boolean sendNewsletters) {
    return this.service
      .signup(
        SignupBody.builder()
          .name(name)
          .email(email)
          .password(password)
          .passwordConfirmation(passwordConfirmation)
          .sendNewsletters(sendNewsletters)
          .newsletterOptIn(sendNewsletters)
          .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> saveProject(final @NonNull Project project) {
    return this.service
      .starProject(project.param())
      .lift(apiErrorOperator())
      .map(StarEnvelope::project)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> toggleProjectSave(final @NonNull Project project) {
    return this.service
      .toggleProjectStar(project.param())
      .lift(apiErrorOperator())
      .map(StarEnvelope::project)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Backing> postBacking(final @NonNull Project project, final @NonNull Backing backing, final boolean checked) {
    return this.service
      .putProjectBacking(project.id(), backing.backerId(), BackingBody.builder()
        .backer(backing.backerId())
        .id(backing.id())
        .backerCompletedAt(checked ? 1 : 0)
        .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ProjectNotification> updateProjectNotifications(final @NonNull ProjectNotification projectNotification, final boolean checked) {
    return this.service
      .updateProjectNotifications(projectNotification.id(),
        ProjectNotificationBody.builder()
          .email(checked)
          .mobile(checked)
          .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<User> updateUserSettings(final @NonNull User user) {
    return this.service
      .updateUserSettings(
        SettingsBody.builder()
          .optedOutOfRecommendations(BoolenExtKt.isTrue(user.optedOutOfRecommendations()) ? 1 : 0)
          .notifyMobileOfBackings(BoolenExtKt.isTrue(user.notifyMobileOfBackings()))
          .notifyMobileOfComments(BoolenExtKt.isTrue(user.notifyMobileOfComments()))
          .notifyMobileOfCreatorEdu(BoolenExtKt.isTrue(user.notifyMobileOfCreatorEdu()))
          .notifyMobileOfFollower(BoolenExtKt.isTrue(user.notifyMobileOfFollower()))
          .notifyMobileOfFriendActivity(BoolenExtKt.isTrue(user.notifyMobileOfFriendActivity()))
          .notifyMobileOfMessages(BoolenExtKt.isTrue(user.notifyMobileOfMessages()))
          .notifyMobileOfPostLikes(BoolenExtKt.isTrue(user.notifyMobileOfPostLikes()))
          .notifyMobileOfUpdates(BoolenExtKt.isTrue(user.notifyMobileOfUpdates()))
          .notifyMobileOfMarketingUpdate(BoolenExtKt.isTrue(user.notifyMobileOfMarketingUpdate()))
          .notifyOfBackings(BoolenExtKt.isTrue(user.notifyOfBackings()))
          .notifyOfComments(BoolenExtKt.isTrue(user.notifyOfComments()))
          .notifyOfCommentReplies(BoolenExtKt.isTrue(user.notifyOfCommentReplies()))
          .notifyOfCreatorDigest(BoolenExtKt.isTrue(user.notifyOfCreatorDigest()))
          .notifyOfCreatorEdu(BoolenExtKt.isTrue(user.notifyOfCreatorEdu()))
          .notifyOfFollower(BoolenExtKt.isTrue(user.notifyOfFollower()))
          .notifyOfFriendActivity(BoolenExtKt.isTrue(user.notifyOfFriendActivity()))
          .notifyOfMessages(BoolenExtKt.isTrue(user.notifyOfMessages()))
          .notifyOfUpdates(BoolenExtKt.isTrue(user.notifyOfUpdates()))
          .alumniNewsletter(BoolenExtKt.isTrue(user.alumniNewsletter()) ? 1 : 0)
          .artsCultureNewsletter(BoolenExtKt.isTrue(user.artsCultureNewsletter()) ? 1 : 0)
          .filmNewsletter(BoolenExtKt.isTrue(user.filmNewsletter()) ? 1 : 0)
          .gamesNewsletter(BoolenExtKt.isTrue(user.gamesNewsletter()) ? 1 : 0)
          .happeningNewsletter(BoolenExtKt.isTrue(user.happeningNewsletter()) ? 1 : 0)
          .inventNewsletter(BoolenExtKt.isTrue(user.inventNewsletter()) ? 1 : 0)
          .musicNewsletter(BoolenExtKt.isTrue(user.musicNewsletter()) ? 1 : 0)
          .promoNewsletter(BoolenExtKt.isTrue(user.promoNewsletter()) ? 1 : 0)
          .publishingNewsletter(BoolenExtKt.isTrue(user.publishingNewsletter()) ? 1 : 0)
          .showPublicProfile(BoolenExtKt.isTrue(user.showPublicProfile()) ? 1 : 0)
          .social(BoolenExtKt.isTrue(user.social()) ? 1 : 0)
          .weeklyNewsletter(BoolenExtKt.isTrue(user.weeklyNewsletter()) ? 1 : 0)
          .build())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @NonNull
  @Override
  public Observable<EmailVerificationEnvelope> verifyEmail(final @NonNull String token) {
    return this.service.verifyEmail(token)
            .lift(apiErrorOperator())
            .subscribeOn(Schedulers.io());
  }
}
