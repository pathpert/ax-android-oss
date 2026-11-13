package com.kickstarter.features.discovery.data

import android.util.Pair
import androidx.lifecycle.Lifecycle
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.loadmore.ApolloPaginateV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.combineProjectsAndParams
import com.kickstarter.libs.utils.extensions.fillRootCategoryForFeaturedProjects
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.updateStartedProjectAndDiscoveryParamsList
import com.kickstarter.models.Activity
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.data.Editorial
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable

/**
 * Reactive pipeline managing project pagination, activity sampling, and interactions for Discovery.
 */
class DiscoveryFragmentPipeline(environment: Environment) {
    private val apiClient = requireNotNull(environment.apiClientV2())
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val activitySamplePreference = environment.activitySamplePreference()
    private val sharedPreferences = requireNotNull(environment.sharedPreferences())
    private val cookieManager = requireNotNull(environment.cookieManager())
    private val currentUser = requireNotNull(environment.currentUserV2())
    private val featureFlagClient = environment.featureFlagClient()
    private val analyticEvents = requireNotNull(environment.analytics())
    private val statsigClient = requireNotNull(environment.statsigClient())

    val lifecycleObservable = BehaviorSubject.create<Lifecycle.State>()
    val activityClick = PublishSubject.create<Boolean>()
    val activitySampleProjectClick = PublishSubject.create<Project>()
    val activityUpdateClick = PublishSubject.create<Activity>()
    val clearPage = PublishSubject.create<Unit>()
    val discoveryOnboardingLoginToutClick = PublishSubject.create<Boolean>()
    val editorialClicked = PublishSubject.create<Editorial>()
    val videoFeedBannerClicked = PublishSubject.create<Unit>()
    val nextPage = PublishSubject.create<Unit>()
    val paramsFromActivity = PublishSubject.create<DiscoveryParams>()
    val projectCardClicked = PublishSubject.create<Project>()
    val onHeartButtonClicked = PublishSubject.create<Project>()
    val refresh = PublishSubject.create<Unit>()
    val rootCategories = PublishSubject.create<List<Category>>()

    val activity = BehaviorSubject.create<Activity>()
    val clearActivities = BehaviorSubject.create<Unit>()
    val heartContainerClicked = BehaviorSubject.create<Unit>()
    val isFetchingProjects = BehaviorSubject.create<Boolean>()
    val projectList = BehaviorSubject.create<List<Pair<Project, DiscoveryParams>>>()
    val showActivityFeed: Observable<Boolean> = activityClick
    val showLoginTout: Observable<Boolean> = discoveryOnboardingLoginToutClick
    val shouldShowEditorial = BehaviorSubject.create<Editorial?>()
    val shouldShowEmptySavedView = BehaviorSubject.create<Boolean>()
    val shouldShowOnboardingView = BehaviorSubject.create<Boolean>()
    val shouldShowVideoFeedBanner = BehaviorSubject.create<Boolean>()
    val startSetPasswordActivity = BehaviorSubject.create<String>()
    val startEditorialActivity = PublishSubject.create<Editorial>()
    val startVideoFeedActivity = PublishSubject.create<Unit>()
    val startPreLaunchProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
    val startProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
    val startUpdateActivity: Observable<Activity> = activityUpdateClick
    val startHeartAnimation = BehaviorSubject.create<Unit>()
    val startLoginToutActivityToSaveProject = PublishSubject.create<Project>()
    val scrollToSavedProjectPosition = PublishSubject.create<Int>()
    val showSavedPrompt = PublishSubject.create<Unit>()
    val disposables = CompositeDisposable()

    init {
        val changedUser = currentUser.observable().distinctUntilChanged()
        val userIsLoggedIn = currentUser.isLoggedIn.distinctUntilChanged()
        val selectedParams = Observable.combineLatest(
            changedUser,
            paramsFromActivity.distinctUntilChanged()
        ) { _, params -> params }

        val startOverWith = Observable.merge(
            selectedParams,
            selectedParams.compose(Transformers.takeWhenV2(refresh))
        )

        val paginator = ApolloPaginateV2.builder<Project, DiscoverEnvelope, DiscoveryParams?>()
            .nextPage(nextPage)
            .distinctUntilChanged(true)
            .startOverWith(startOverWith)
            .envelopeToListOfData { it.projects() }
            .loadWithParams { p ->
                p.first?.let {
                    apolloClient.getProjects(it, p.second).compose(Transformers.neverErrorV2())
                }
            }
            .clearWhenStartingOver(false)
            .concater { xs, ys -> ListUtils.concatDistinct(xs, ys) }
            .build()

        paginator.isFetching.subscribe { isFetchingProjects.onNext(it) }.addToDisposable(disposables)
        projectList.compose(Transformers.ignoreValuesV2()).subscribe { isFetchingProjects.onNext(false) }.addToDisposable(disposables)

        val activitySampleProjectClick = activitySampleProjectClick
            .map<Pair<Project, RefTag>> { Pair.create(it, RefTag.activitySample()) }

        projectCardClicked.subscribe { DiscoveryAnalyticsTracker.trackCardClick(analyticEvents, it) }.addToDisposable(disposables)

        paramsFromActivity.compose(Transformers.takePairWhenV2(projectCardClicked))
            .subscribe {
                DiscoveryAnalyticsTracker.trackCtaClick(analyticEvents, it.first, it.second, cookieManager, sharedPreferences)
            }.addToDisposable(disposables)

        val projectCardClick = paramsFromActivity.compose(Transformers.takePairWhenV2(projectCardClicked))
            .map { RefTagUtils.projectAndRefTagFromParamsAndProject(it.first, it.second) }

        val projects = Observable.combineLatest(paginator.paginatedData(), rootCategories) { projs, roots ->
            projs.fillRootCategoryForFeaturedProjects(roots)
        }

        Observable.combineLatest(projects, selectedParams.distinctUntilChanged()) { projs, params ->
            combineProjectsAndParams(projs, params)
        }.filter { it.isNotNull() }
            .map { it }
            .subscribe { projectList.onNext(it.toList()) }
            .addToDisposable(disposables)

        Observable.merge(activitySampleProjectClick, projectCardClick).subscribe {
            if (it.first.displayPrelaunch() == true) {
                startPreLaunchProjectActivity.onNext(it)
            } else {
                startProjectActivity.onNext(it)
            }
        }.addToDisposable(disposables)

        clearPage.subscribe {
            shouldShowOnboardingView.onNext(false)
            clearActivities.onNext(Unit)
            projectList.onNext(emptyList())
        }.addToDisposable(disposables)

        currentUser.observable().compose(Transformers.combineLatestPair(paramsFromActivity))
            .filter { it.first.isPresent() }
            .map { Pair(requireNotNull(it.first.getValue()), it.second) }
            .map { DiscoveryFiltersMapper.isDefaultParams(it) && it.second.tagId() == Editorial.LIGHTS_ON.tagId }
            .filter { shouldShow -> shouldShow }
            .map { Editorial.LIGHTS_ON }
            .subscribe { shouldShowEditorial.onNext(it) }
            .addToDisposable(disposables)

        editorialClicked.subscribe { startEditorialActivity.onNext(it) }.addToDisposable(disposables)
        videoFeedBannerClicked.subscribe { startVideoFeedActivity.onNext(Unit) }.addToDisposable(disposables)

        paramsFromActivity.compose(Transformers.combineLatestPair(userIsLoggedIn))
            .map { DiscoveryFiltersMapper.isOnboardingVisible(it.first, it.second) }
            .subscribe { shouldShowOnboardingView.onNext(it) }
            .addToDisposable(disposables)

        Observable.combineLatest(paramsFromActivity, statsigClient.configReady.asObservable()) { params, configReady -> params to configReady }
            .concatMap { (params, configReady) ->
                val visible = DiscoveryFiltersMapper.videoFeedBannerVisibility(params, configReady, statsigClient)
                if (visible == null) Observable.empty() else Observable.just(visible)
            }
            .distinctUntilChanged()
            .subscribe { shouldShowVideoFeedBanner.onNext(it) }
            .addToDisposable(disposables)

        paramsFromActivity.compose(Transformers.combineLatestPair(userIsLoggedIn))
            .filter { it.second && featureFlagClient?.getBoolean(FlagKey.ANDROID_FACEBOOK_LOGIN_REMOVE) == true }
            .compose(Transformers.combineLatestPair(currentUser.loggedInUser()))
            .filter { it.second.needsPassword() == true }
            .switchMap { DiscoveryDataSourceHelper.fetchUserEmail(apolloClient) }
            .distinctUntilChanged()
            .subscribe { startSetPasswordActivity.onNext(it) }
            .addToDisposable(disposables)

        paramsFromActivity.map { DiscoveryFiltersMapper.isSavedVisible(it) }
            .compose(Transformers.combineLatestPair(projectList))
            .map { it.first && it.second.isEmpty() }
            .distinctUntilChanged()
            .subscribe { shouldShowEmptySavedView.onNext(it) }
            .addToDisposable(disposables)

        shouldShowEmptySavedView.filter { it.isTrue() }
            .map<Any?> { }
            .mergeWith(heartContainerClicked)
            .subscribe { startHeartAnimation.onNext(Unit) }
            .addToDisposable(disposables)

        val loggedInUserAndParams = currentUser.loggedInUser().distinctUntilChanged()
            .compose(Transformers.combineLatestPair(paramsFromActivity.distinctUntilChanged()))

        loggedInUserAndParams.filter { DiscoveryFiltersMapper.isDefaultParams(it) }
            .switchMap { DiscoveryDataSourceHelper.fetchActivity(apiClient) }
            .filter { DiscoveryFiltersMapper.activityHasNotBeenSeen(it, activitySamplePreference?.get()) }
            .doOnNext { act -> if (act != null) activitySamplePreference?.set(act.id().toInt()) }
            .subscribe { activity.onNext(it) }
            .addToDisposable(disposables)

        loggedInUserAndParams.filter { !DiscoveryFiltersMapper.isDefaultParams(it) }
            .subscribe { clearActivities.onNext(Unit) }
            .addToDisposable(disposables)

        paramsFromActivity.compose(Transformers.combineLatestPair(lifecycleObservable))
            .filter { it.second == Lifecycle.State.RESUMED }
            .distinctUntilChanged()
            .subscribe { DiscoveryAnalyticsTracker.trackPageView(analyticEvents, it.first) }
            .addToDisposable(disposables)

        discoveryOnboardingLoginToutClick.subscribe { DiscoveryAnalyticsTracker.trackLoginTout(analyticEvents) }.addToDisposable(disposables)

        val loggedInUserOnHeartClick = userIsLoggedIn.compose(Transformers.takePairWhenV2(onHeartButtonClicked)).filter { it.first == true }
        val loggedOutUserOnHeartClick = userIsLoggedIn.compose(Transformers.takePairWhenV2(onHeartButtonClicked)).filter { it.first == false }

        val projectOnUserChangeSave = loggedInUserOnHeartClick
            .switchMap { DiscoveryDataSourceHelper.toggleProjectSave(apolloClient, it.second) }
            .share()

        loggedOutUserOnHeartClick.map { it }.subscribe { startLoginToutActivityToSaveProject.onNext(it.second) }.addToDisposable(disposables)

        val savedProjectOnLoginSuccess = startLoginToutActivityToSaveProject
            .compose(Transformers.combineLatestPair(currentUser.observable()))
            .filter { it.second.isPresent() }
            .take(1)
            .switchMap { DiscoveryDataSourceHelper.saveProject(apolloClient, it.first) }
            .share()

        projectList.compose(Transformers.takePairWhenV2(projectOnUserChangeSave))
            .map { it.second.updateStartedProjectAndDiscoveryParamsList(it.first) }
            .distinctUntilChanged()
            .subscribe { projectList.onNext(it) }
            .addToDisposable(disposables)

        projectList.compose(Transformers.takePairWhenV2(savedProjectOnLoginSuccess))
            .map { it.first.indexOfFirst { item -> item.first.id() == it.second.id() && item.first.slug() == it.second.slug() } }
            .distinctUntilChanged()
            .subscribe { scrollToSavedProjectPosition.onNext(it) }
            .addToDisposable(disposables)

        val projectSavedStatus = projectOnUserChangeSave.mergeWith(savedProjectOnLoginSuccess)
        projectSavedStatus.subscribe { DiscoveryAnalyticsTracker.trackWatch(analyticEvents, it) }.addToDisposable(disposables)
        projectSavedStatus.filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
            .compose(Transformers.ignoreValuesV2())
            .subscribe { showSavedPrompt.onNext(it) }
            .addToDisposable(disposables)
    }

    fun clear() {
        apolloClient.cleanDisposables()
        disposables.clear()
    }
}

