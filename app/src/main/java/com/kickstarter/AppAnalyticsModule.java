package com.kickstarter;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kickstarter.libs.AnalyticEvents;
import com.kickstarter.libs.AttributionEvents;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentConfigTypeV2;
import com.kickstarter.libs.CurrentUserTypeV2;
import com.kickstarter.libs.FirebaseAnalyticsClient;
import com.kickstarter.libs.FirebaseAnalyticsClientType;
import com.kickstarter.libs.SegmentTrackingClient;
import com.kickstarter.libs.TrackingClientType;
import com.kickstarter.libs.braze.BrazeClient;
import com.kickstarter.libs.braze.RemotePushClientType;
import com.kickstarter.libs.featureflag.FeatureFlagClient;
import com.kickstarter.libs.featureflag.FeatureFlagClientType;
import com.kickstarter.libs.featureflag.StatsigClient;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.services.ApolloClientTypeV2;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;

/**
 * Application component for AppAnalyticsModule.
 *
 * Negative constraints:
 * - Do not mutate internal state directly from external callers.
 */
@Module
public final class AppAnalyticsModule {

  @Provides
  @Nonnull
  @Singleton
  static FirebaseAnalyticsClientType provideFirebaseAnalyticsClientType(final @NonNull FeatureFlagClientType ffClient, final @NonNull SharedPreferences sharedPreferences, final @ApplicationContext @NonNull Context context) {
    return new FirebaseAnalyticsClient(sharedPreferences, FirebaseAnalytics.getInstance(context));
  }

  @Provides
  @Nonnull
  @Singleton
  static RemotePushClientType provideBrazeClient(final @NonNull Build build, final @ApplicationContext @NonNull Context context) {
    return new BrazeClient(context, build);
  }

  @Provides
  @Nonnull
  @Singleton
  static FeatureFlagClientType provideFeatureFlagClientType(final @NonNull Build build) {
    return new FeatureFlagClient(build);
  }

  @Provides
  @Singleton
  static SegmentTrackingClient provideSegmentTrackingClient(
          final @ApplicationContext @NonNull Context context,
          final @NonNull CurrentUserTypeV2 currentUser,
          final @NonNull Build build,
          final @NonNull CurrentConfigTypeV2 currentConfig) {
    return new SegmentTrackingClient(build, context, currentConfig, currentUser, PreferenceManager.getDefaultSharedPreferences(context));
  }

  @Provides
  @Singleton
  static StatsigClient provideStatsigClient(
          final @ApplicationContext @NonNull Context context,
          final @NonNull CurrentUserTypeV2 currentUser,
          final @NonNull SegmentTrackingClient segmentTrackingClient,
          final @NonNull Build build
  ) {
    return new StatsigClient(build, context, currentUser, segmentTrackingClient);
  }

  @Provides
  @Singleton
  static AnalyticEvents provideAnalytics(
          final @ApplicationContext @NonNull Context context,
          final @NonNull CurrentUserTypeV2 currentUser,
          final @NonNull Build build,
          final @NonNull FeatureFlagClientType ffClient,
          final @NonNull SegmentTrackingClient segmentClient) {
    final List<TrackingClientType> clients = Arrays.asList(segmentClient);
    return new AnalyticEvents(clients);
  }

  @Provides
  @Singleton
  static AttributionEvents provideAttributionEvents(
    final @NonNull ApolloClientTypeV2 apolloClient) {
    return new AttributionEvents(apolloClient);
  }
}
