package com.kickstarter.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.rx.operators.ApiErrorOperatorV2;
import com.kickstarter.libs.rx.operators.OperatorsV2;
import com.kickstarter.libs.utils.extensions.AnyExtKt;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.models.Reward;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.CategoriesEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope;
import com.kickstarter.ui.data.Mailbox;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * Base Retrofit API client providing core discovery, activity, project, and messaging endpoints.
 */
abstract class BaseApiClientV2 implements ApiClientTypeV2 {
  protected final ApiServiceV2 service;
  protected final Gson gson;

  public BaseApiClientV2(final @NonNull ApiServiceV2 service, final @NonNull Gson gson) {
    this.gson = gson;
    this.service = service;
  }

  @Override
  public @NonNull Observable<Config> config() {
    return this.service
      .config()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivities() {
    return fetchActivities(null);
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivities(final @Nullable Integer count) {
    final List<String> categories = Arrays.asList(
      Activity.CATEGORY_BACKING,
      Activity.CATEGORY_CANCELLATION,
      Activity.CATEGORY_FAILURE,
      Activity.CATEGORY_LAUNCH,
      Activity.CATEGORY_SUCCESS,
      Activity.CATEGORY_UPDATE,
      Activity.CATEGORY_FOLLOW,
      Activity.CATEGORY_SHIPPED
    );

    return this.service
      .activities(categories, count)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ActivityEnvelope> fetchActivitiesWithPaginationPath(final @NonNull String paginationPath) {
    return this.service
      .activities(paginationPath)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<List<Category>> fetchCategories() {
    return this.service
      .categories()
      .lift(apiErrorOperator())
      .map(CategoriesEnvelope::categories)
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Category> fetchCategory(final @NonNull String id) {
    return this.service
      .category(id)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Category> fetchCategory(final @NonNull Category category) {
    return fetchCategory(String.valueOf(category.id()));
  }

  @Override
  public @NonNull Observable<User> fetchCurrentUser() {
    return this.service
      .currentUser()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Location> fetchLocation(final @NonNull String param) {
    return this.service.location(param)
      .subscribeOn(Schedulers.io())
      .lift(apiErrorOperator());
  }

  @Override
  public @NonNull Observable<List<ProjectNotification>> fetchProjectNotifications() {
    return this.service
      .projectNotifications()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> fetchProject(final @NonNull String param) {
    return this.service
      .project(param)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Project> fetchProject(final @NonNull Project project) {
    return fetchProject(project.param()).startWith(project);
  }

  @Override
  public @NonNull Observable<ProjectsEnvelope> fetchProjects(final boolean isMember) {
    return this.service
      .projects(isMember ? 1 : 0)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
    return this.service
      .projects(params.queryParams())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull String paginationUrl) {
    return this.service
      .projects(paginationUrl)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ProjectStatsEnvelope> fetchProjectStats(final @NonNull Project project) {
    return this.service
      .projectStats(project.param())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
    return this.service
      .projectBacking(project.param(), user.param())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
    return this.service
      .messagesForBacking(backing.projectId(), backing.backerId())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread messageThread) {
    return this.service
      .messagesForThread(messageThread.id())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull Long messageThreadId) {
    return this.service
      .messagesForThread(messageThreadId)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads(final @Nullable Project project,
    final @NonNull Mailbox mailbox) {

    final Observable<Response<MessageThreadsEnvelope>> apiResponse = project == null
      ? this.service.messageThreads(mailbox.getType())
      : this.service.messageThreads(project.id(), mailbox.getType());

    return apiResponse
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreadsWithPaginationPath(final @NonNull String paginationPath) {
    return this.service
      .paginatedMessageThreads(paginationPath)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<ShippingRulesEnvelope> fetchShippingRules(final @NonNull Project project, final @NonNull Reward reward) {
    return this.service
      .shippingRules(project.id(), reward.id())
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<SurveyResponse> fetchSurveyResponse(final long surveyResponseId) {
    return this.service
      .surveyResponse(surveyResponseId)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<List<SurveyResponse>> fetchUnansweredSurveys() {
    return this.service
      .unansweredSurveys()
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Update> fetchUpdate(final @NonNull String projectParam, final @NonNull String updateParam) {
    return this.service
      .update(projectParam, updateParam)
      .lift(apiErrorOperator())
      .subscribeOn(Schedulers.io());
  }

  @Override
  public @NonNull Observable<Update> fetchUpdate(final @NonNull Update update) {
    final String projectParam = AnyExtKt.numToString(update.projectId());
    final String updateParam = AnyExtKt.numToString(update.id());

    return fetchUpdate(projectParam, updateParam)
      .startWith(update);
  }

  protected @NonNull <T> ApiErrorOperatorV2<T> apiErrorOperator() {
    return OperatorsV2.apiError(this.gson);
  }
}
