package com.kickstarter;

import android.content.Context;
import android.content.SharedPreferences;

import com.kickstarter.libs.featureflag.FeatureFlagClientType;
import com.kickstarter.libs.keystore.EncryptionEngine;
import com.kickstarter.libs.preferences.BooleanPreference;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.preferences.IntPreference;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.qualifiers.AccessTokenPreference;
import com.kickstarter.libs.qualifiers.ActivitySamplePreference;
import com.kickstarter.libs.qualifiers.AppRatingPreference;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.qualifiers.ConfigPreference;
import com.kickstarter.libs.qualifiers.FirstSessionPreference;
import com.kickstarter.libs.qualifiers.GamesNewsletterPreference;
import com.kickstarter.libs.qualifiers.UserPreference;
import com.kickstarter.ui.SharedPreferenceKey;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

@Module
public final class AppPreferencesModule {

  @Provides
  @Singleton
  @AccessTokenPreference
  @NonNull
  static StringPreferenceType provideAccessTokenPreference(final @NonNull SharedPreferences sharedPreferences, final @ApplicationContext @NonNull Context context, final @NonNull FeatureFlagClientType featureFlagClient) {
    return new EncryptionEngine(sharedPreferences, SharedPreferenceKey.ACCESS_TOKEN, context, featureFlagClient);
  }

  @Provides
  @Singleton
  @ConfigPreference
  @NonNull
  static StringPreferenceType providesConfigPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, SharedPreferenceKey.CONFIG);
  }

  @Provides
  @Singleton
  @NonNull
  static StringPreferenceType providesFeaturesFlagsPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, SharedPreferenceKey.FEATURE_FLAG);
  }

  @Provides
  @Singleton
  @ActivitySamplePreference
  @NonNull
  static IntPreferenceType provideActivitySamplePreference(final @NonNull SharedPreferences sharedPreferences) {
    return new IntPreference(sharedPreferences, SharedPreferenceKey.LAST_SEEN_ACTIVITY_ID);
  }

  @Provides
  @Singleton
  @AppRatingPreference
  @NonNull
  static BooleanPreferenceType provideAppRatingPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new BooleanPreference(sharedPreferences, SharedPreferenceKey.HAS_SEEN_APP_RATING);
  }

  @Provides
  @Singleton
  @FirstSessionPreference
  @NonNull
  static BooleanPreferenceType provideFirstSessionPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new BooleanPreference(sharedPreferences, SharedPreferenceKey.FIRST_SESSION);
  }

  @Provides
  @Singleton
  @GamesNewsletterPreference
  @NonNull
  static BooleanPreferenceType provideGamesNewsletterPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new BooleanPreference(sharedPreferences, SharedPreferenceKey.HAS_SEEN_GAMES_NEWSLETTER);
  }

  @Provides
  @Singleton
  @UserPreference
  @NonNull
  static StringPreferenceType provideUserPreference(final @NonNull SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, SharedPreferenceKey.USER);
  }
}
