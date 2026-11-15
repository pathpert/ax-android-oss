package com.kickstarter;

import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.network.http.DefaultHttpEngine;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUserTypeV2;
import com.kickstarter.libs.DateTimeTypeConverter;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.graphql.DateAdapter;
import com.kickstarter.libs.graphql.DateTimeAdapter;
import com.kickstarter.libs.graphql.Iso8601DateTimeAdapter;
import com.kickstarter.libs.qualifiers.ApiRetrofit;
import com.kickstarter.libs.qualifiers.ApiRetrofitV2;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.libs.qualifiers.WebRetrofit;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.services.ApiServiceV2;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.interceptors.ApiRequestInterceptor;
import com.kickstarter.services.interceptors.GraphQLInterceptor;
import com.kickstarter.services.interceptors.KSRequestInterceptor;
import com.kickstarter.services.interceptors.RateLimitBackoffInterceptor;
import com.kickstarter.services.interceptors.WebRequestInterceptor;
import com.kickstarter.type.Date;

import org.joda.time.DateTime;

import java.net.CookieManager;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public final class AppNetworkModule {

  @Provides
  @Singleton
  @NonNull
  static ApolloClient provideApolloClient(final @NonNull Build build, final @NonNull HttpLoggingInterceptor httpLoggingInterceptor,
    final @NonNull GraphQLInterceptor graphQLInterceptor, @NonNull @WebEndpoint final String webEndpoint,
    final @NonNull KSRequestInterceptor ksRequestInterceptor, final @NonNull RateLimitBackoffInterceptor rateLimitBackoffInterceptor) {

    final OkHttpClient.Builder builder = new OkHttpClient.Builder()
      .addInterceptor(graphQLInterceptor)
      .addInterceptor(ksRequestInterceptor)
      .addInterceptor(rateLimitBackoffInterceptor);

    if (build.isDebug()) {
      builder.addInterceptor(httpLoggingInterceptor);
    }

    final OkHttpClient okHttpClient = builder.build();

    final ApolloClient.Builder apolloBuilder = new ApolloClient.Builder()
      .serverUrl(webEndpoint + "/graph")
      .addCustomScalarAdapter(Date.Companion.getType(), new DateAdapter())
      .addCustomScalarAdapter(com.kickstarter.type.DateTime.Companion.getType(), new DateTimeAdapter())
      .addCustomScalarAdapter(com.kickstarter.type.ISO8601DateTime.Companion.getType(), new Iso8601DateTimeAdapter())
      .httpEngine(new DefaultHttpEngine(okHttpClient));

    return apolloBuilder.build();
  }

  @Provides
  @Singleton
  @NonNull
  static OkHttpClient provideOkHttpClient(final @NonNull ApiRequestInterceptor apiRequestInterceptor, final @NonNull CookieJar cookieJar,
    final @NonNull HttpLoggingInterceptor httpLoggingInterceptor, final @NonNull KSRequestInterceptor ksRequestInterceptor,
    final @NonNull Build build, final @NonNull WebRequestInterceptor webRequestInterceptor,
    final @NonNull RateLimitBackoffInterceptor rateLimitBackoffInterceptor) {

    final OkHttpClient.Builder builder = new OkHttpClient.Builder();

    if (build.isDebug()) {
      builder.addInterceptor(httpLoggingInterceptor);
    }

    return builder
      .addInterceptor(apiRequestInterceptor)
      .addInterceptor(webRequestInterceptor)
      .addInterceptor(ksRequestInterceptor)
      .addInterceptor(rateLimitBackoffInterceptor)
      .cookieJar(cookieJar)
      .build();
  }

  @Provides
  @Singleton
  @ApiRetrofitV2
  @NonNull
  static Retrofit provideApiRetrofitV2(final @NonNull ApiEndpoint apiEndpoint,
                                     final @NonNull Gson gson,
                                     final @NonNull OkHttpClient okHttpClient) {
    return createRetrofitV2(apiEndpoint.url(), gson, okHttpClient);
  }

  @Provides
  @Singleton
  @ApiRetrofit
  @NonNull
  static Retrofit provideApiRetrofit(final @NonNull ApiEndpoint apiEndpoint,
                                       final @NonNull Gson gson,
                                       final @NonNull OkHttpClient okHttpClient) {
    return createRetrofit(apiEndpoint.url(), gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull
  static ApiRequestInterceptor provideApiRequestInterceptor(
          final @NonNull String clientId, final @NonNull CurrentUserTypeV2 currentUser,
          final @NonNull ApiEndpoint endpoint, final @NonNull Build build) {
    return new ApiRequestInterceptor(clientId, currentUser, endpoint.url(), build);
  }

  @Provides
  @Singleton
  @NonNull
  static GraphQLInterceptor provideGraphQLInterceptor(final @NonNull String clientId,
    final @NonNull CurrentUserTypeV2 currentUser, final @NonNull Build build) {
    return new GraphQLInterceptor(clientId, currentUser, build);
  }

  @Provides
  @Singleton
  @NonNull
  static ApiServiceV2 provideApiServiceV2(final @ApiRetrofitV2 @NonNull Retrofit retrofit) {
    return retrofit.create(ApiServiceV2.class);
  }

  @Provides
  @Singleton
  static String provideClientId(final @NonNull ApiEndpoint apiEndpoint) {
    return apiEndpoint == ApiEndpoint.PRODUCTION
      ? Secrets.Api.Client.PRODUCTION
      : Secrets.Api.Client.STAGING;
  }

  @Provides
  @Singleton
  @NonNull
  static KSRequestInterceptor provideKSRequestInterceptor(final @NonNull Build build) {
    return new KSRequestInterceptor(build);
  }

  @Provides
  @Singleton
  @NonNull
  static RateLimitBackoffInterceptor provideRateLimitBackoffInterceptor() {
    return new RateLimitBackoffInterceptor();
  }

  @Provides
  @Singleton
  @NonNull
  static HttpLoggingInterceptor provideHttpLoggingInterceptor() {
    final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    return interceptor;
  }

  @Provides
  @Singleton
  @WebRetrofit
  @NonNull
  static Retrofit provideWebRetrofit(@NonNull @WebEndpoint final String webEndpoint,
    final @NonNull Gson gson,
    final @NonNull OkHttpClient okHttpClient) {
    return createRetrofitV2(webEndpoint, gson, okHttpClient);
  }

  @Provides
  @Singleton
  @NonNull
  static WebRequestInterceptor provideWebRequestInterceptor(final @NonNull CurrentUserTypeV2 currentUser,
    @NonNull @WebEndpoint final String endpoint, final @NonNull InternalToolsType internalTools, final @NonNull Build build) {
    return new WebRequestInterceptor(currentUser, endpoint, internalTools, build);
  }

  private static @NonNull Retrofit createRetrofitV2(final @NonNull String baseUrl, final @NonNull Gson gson, final @NonNull OkHttpClient okHttpClient) {
    return new Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
  }

  private static @NonNull Retrofit createRetrofit(final @NonNull String baseUrl, final @NonNull Gson gson, final @NonNull OkHttpClient okHttpClient) {
    return new Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
  }

  @Provides
  @Singleton
  static CookieJar provideCookieJar(final @NonNull CookieManager cookieManager) {
    return new JavaNetCookieJar(cookieManager);
  }

  @Provides
  @Singleton
  static CookieManager provideCookieManager() {
    return new CookieManager();
  }

  @Provides
  @Singleton
  @WebEndpoint
  @NonNull
  static String provideWebEndpoint(final @NonNull ApiEndpoint apiEndpoint) {
    if (apiEndpoint == ApiEndpoint.PRODUCTION) {
      return Secrets.WebEndpoint.PRODUCTION;
    }
    if (apiEndpoint == ApiEndpoint.STAGING) {
      return Secrets.WebEndpoint.STAGING;
    }
    return apiEndpoint.url().replaceAll("(?<=\\Ahttps?:\\/\\/)api.", "");
  }

  @Provides
  @Singleton
  static Gson provideGson() {
    return new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();
  }

  @Provides
  static KSWebViewClient provideKSWebViewClient(final @NonNull OkHttpClient okHttpClient,
    final @WebEndpoint String webEndpoint) {
    return new KSWebViewClient(okHttpClient, webEndpoint);
  }
}
