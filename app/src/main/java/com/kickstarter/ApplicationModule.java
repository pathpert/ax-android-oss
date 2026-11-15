package com.kickstarter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.kickstarter.ax.errors.ProblemDetails;
import com.kickstarter.ax.errors.ProblemDetailsMapper;
import com.kickstarter.ax.errors.ProblemException;
import com.kickstarter.libs.AnalyticEvents;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.AttributionEvents;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentConfigTypeV2;
import com.kickstarter.libs.CurrentConfigV2;
import com.kickstarter.libs.CurrentUserTypeV2;
import com.kickstarter.libs.CurrentUserV2;
import com.kickstarter.libs.DeviceRegistrar;
import com.kickstarter.libs.DeviceRegistrarType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.FirebaseAnalyticsClientType;
import com.kickstarter.libs.Font;
import com.kickstarter.libs.InternalToolsType;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.braze.RemotePushClientType;
import com.kickstarter.libs.featureflag.FeatureFlagClientType;
import com.kickstarter.libs.featureflag.StatsigClient;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.qualifiers.AccessTokenPreference;
import com.kickstarter.libs.qualifiers.ActivitySamplePreference;
import com.kickstarter.libs.qualifiers.AppRatingPreference;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.qualifiers.ConfigPreference;
import com.kickstarter.libs.qualifiers.FirstSessionPreference;
import com.kickstarter.libs.qualifiers.GamesNewsletterPreference;
import com.kickstarter.libs.qualifiers.PackageNameString;
import com.kickstarter.libs.qualifiers.UserPreference;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.libs.utils.PlayServicesCapability;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.services.ApiClientTypeV2;
import com.kickstarter.services.ApolloClientTypeV2;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;

import java.net.CookieManager;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

/**
 * @deprecated Legacy monolithic Dagger module. Feature dependencies should be provided by feature-scoped modules.
 */
@Deprecated
@Module(includes = {AppNetworkModule.class, AppPreferencesModule.class, AppAnalyticsModule.class})
public class ApplicationModule {
  private final Application application;

  public ApplicationModule(final @NonNull Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  static Environment provideEnvironment(final @NonNull @ActivitySamplePreference IntPreferenceType activitySamplePreference,
    final @NonNull ApiClientTypeV2 apiClientV2,
    final @NonNull ApolloClientTypeV2 apolloClientV2,
    final @NonNull Build build,
    final @NonNull CookieManager cookieManager,
    final @NonNull CurrentConfigTypeV2 currentConfig2,
    final @NonNull CurrentUserTypeV2 currentUser2,
    final @NonNull @FirstSessionPreference BooleanPreferenceType firstSessionPreference,
    final @NonNull Gson gson,
    final @NonNull @AppRatingPreference BooleanPreferenceType hasSeenAppRatingPreference,
    final @NonNull @GamesNewsletterPreference BooleanPreferenceType hasSeenGamesNewsletterPreference,
    final @NonNull InternalToolsType internalToolsType,
    final @NonNull KSCurrency ksCurrency,
    final @NonNull KSString ksString,
    final @NonNull AnalyticEvents analytics,
    final @NonNull AttributionEvents attributionEvents,
    final @NonNull Logout logout,
    final @NonNull PlayServicesCapability playServicesCapability,
    final @NonNull Scheduler schedulerV2,
    final @NonNull SharedPreferences sharedPreferences,
    final @NonNull Stripe stripe,
    final @NonNull @WebEndpoint String webEndpoint,
    final @NonNull FirebaseAnalyticsClientType firebaseAnalyticsClientType,
    final @NonNull StatsigClient statsigClient,
    final @NonNull FeatureFlagClientType featureFlagClient) {

    return Environment.builder()
      .activitySamplePreference(activitySamplePreference)
      .apiClientV2(apiClientV2)
      .apolloClientV2(apolloClientV2)
      .build(build)
      .cookieManager(cookieManager)
      .currentConfig2(currentConfig2)
      .currentUserV2(currentUser2)
      .firstSessionPreference(firstSessionPreference)
      .gson(gson)
      .hasSeenAppRatingPreference(hasSeenAppRatingPreference)
      .hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference)
      .internalTools(internalToolsType)
      .ksCurrency(ksCurrency)
      .ksString(ksString)
      .analytics(analytics)
      .attributionEvents(attributionEvents)
      .logout(logout)
      .playServicesCapability(playServicesCapability)
      .schedulerV2(schedulerV2)
      .sharedPreferences(sharedPreferences)
      .stripe(stripe)
      .webEndpoint(webEndpoint)
      .firebaseAnalyticsClient(firebaseAnalyticsClientType)
      .featureFlagClient(featureFlagClient)
      .statsigClient(statsigClient)
      .build();
  }

  @Provides
  @Singleton
  @NonNull
  static PlayServicesCapability providePlayServicesCapability(final @ApplicationContext @NonNull Context context) {
    return new PlayServicesCapability(context);
  }

  @Provides
  @Singleton
  Application provideApplication() {
    return this.application;
  }

  @Provides
  @Singleton
  static io.reactivex.Scheduler provideSchedulerV2() {
    return io.reactivex.schedulers.Schedulers.computation();
  }

  @Provides
  @Singleton
  @ApplicationContext
  Context provideApplicationContext() {
    return this.application;
  }

  @Provides
  @Singleton
  AssetManager provideAssetManager() {
    return this.application.getAssets();
  }

  @Provides
  @Singleton
  @NonNull
  static Build provideBuild(final @NonNull PackageInfo packageInfo) {
    return new Build(packageInfo);
  }

  @Provides
  @Singleton
  static CurrentConfigTypeV2 provideCurrentConfig2(final @NonNull AssetManager assetManager,
                                                   final @NonNull Gson gson,
                                                   final @ConfigPreference @NonNull StringPreferenceType configPreference) {
    return new CurrentConfigV2(assetManager, gson, configPreference);
  }

  @Provides
  @Singleton
  static CurrentUserTypeV2 provideCurrentUser2(final @AccessTokenPreference @NonNull StringPreferenceType accessTokenPreference,
                                               final @NonNull DeviceRegistrarType deviceRegistrar, final @NonNull Gson gson,
                                               final @NonNull @UserPreference StringPreferenceType userPreference) {
    return new CurrentUserV2(accessTokenPreference, deviceRegistrar, gson, userPreference);
  }

  @Provides
  @Singleton
  @NonNull
  static DeviceRegistrarType provideDeviceRegistrar(final @NonNull PlayServicesCapability playServicesCapability,
                                                    final @ApplicationContext @NonNull Context context,
                                                    final @NonNull RemotePushClientType brazeClient) {
    return new DeviceRegistrar(playServicesCapability, context, brazeClient);
  }

  @Provides
  @Singleton
  static Font provideFont(final @NonNull AssetManager assetManager) {
    return new Font(assetManager);
  }

  @Provides
  @Singleton
  static KSCurrency provideKSCurrency(final @NonNull CurrentConfigTypeV2 currentConfig) {
    return new KSCurrency(currentConfig);
  }

  @Provides
  @Singleton
  @NonNull
  static KSString provideKSString(final @PackageNameString @NonNull String packageName, final @NonNull Resources resources) {
    return new KSString(packageName, resources);
  }

  @Provides
  @Singleton
  static Logout provideLogout(final @NonNull CookieManager cookieManager, final @NonNull CurrentUserTypeV2 currentUser, final @NonNull CurrentUserTypeV2 currentUserV2) {
    return new Logout(cookieManager, currentUserV2);
  }

  @Provides
  @Singleton
  @NonNull
  static PushNotifications providePushNotifications(final @ApplicationContext @NonNull Context context,
    final @NonNull ApiClientTypeV2 client) {
    return new PushNotifications(context, client);
  }

  @Provides
  @Singleton
  static PackageInfo providePackageInfo(final @NonNull Application application) {
    try {
      return application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      final ProblemDetails problem = ProblemDetailsMapper.INSTANCE.fromThrowable(e, "retrieve package info");
      FirebaseCrashlytics.getInstance().setCustomKey("ApplicationModule_packageInfo", problem.getTitle() + ": " + problem.getDetail());
      FirebaseCrashlytics.getInstance().recordException(new ProblemException(problem, e));
      throw new RuntimeException(problem.getTitle() + ": " + problem.getDetail(), e);
    }
  }

  @Provides
  @Singleton
  @PackageNameString
  static String providePackageName(final @NonNull Application application) {
    return application.getPackageName();
  }

  @Provides
  @Singleton
  static Resources provideResources(final @ApplicationContext @NonNull Context context) {
    return context.getResources();
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(this.application);
  }

  @Provides
  @Singleton
  Stripe provideStripe(final @ApplicationContext @NonNull Context context, final @NonNull ApiEndpoint apiEndpoint) {
    final String stripePublishableKey = apiEndpoint == ApiEndpoint.PRODUCTION
      ? Secrets.StripePublishableKey.PRODUCTION
      : Secrets.StripePublishableKey.STAGING;
    PaymentConfiguration.init(
            context,
            stripePublishableKey);
    return new Stripe(context, stripePublishableKey);
  }
}
