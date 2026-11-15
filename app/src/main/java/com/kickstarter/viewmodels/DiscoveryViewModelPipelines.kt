package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Pair
import androidx.core.content.edit
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DiscoveryUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deriveNavigationDrawerData
import com.kickstarter.libs.utils.extensions.getTokenFromQueryParams
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isVerificationEmailUrl
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.models.Category
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.ui.SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE
import com.kickstarter.ui.SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS
import com.kickstarter.ui.SharedPreferenceKey.HAS_SEEN_ONBOARDING
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

internal fun DiscoveryViewModel.DiscoveryViewModel.initPipelines(disposables: CompositeDisposable) {
    showActivityFeed = activityFeedClick
    showHelp = loggedOutHelpClick
    showInternalTools = internalToolsClick
    showLoginTout = loggedOutLoginToutClick
    showMessages = messagesClick
    showProfile = profileClick
    showSettings = settingsClick
    showPledgedProjects = pledgedProjectsClick

    val currentUser = currentUserType.observable()
        .map { it.getValue() ?: User.builder().build() }
        .filter { it.isNotNull() }
        .map { it }

    val changedUser = currentUser
        .filter { it.isNotNull() }
        .map { it }
        .distinctUntilChanged()

    changedUser
        .subscribe {
            apiClient.config()
                .compose(Transformers.neverErrorV2())
                .subscribe { currentConfigType.config(it) }
                .addToDisposable(disposables)
        }
        .addToDisposable(disposables)

    val intentObservable = intent.share()

    val paramsFromInitialIntent = intentObservable
        .take(1)
        .filter { it.action.isNotNull() }
        .map { it.action }
        .filter { Intent.ACTION_MAIN == it }
        .compose(Transformers.combineLatestPair(changedUser))
        .map { DiscoveryParams.getDefaultParams(it.second) }
        .share()

    val uriFromVerification = intentObservable
        .filter { it.data.isNotNull() }
        .map { it.data }
        .ofType(Uri::class.java)
        .filter { it.isVerificationEmailUrl() }

    val paramsFromIntent = intentObservable
        .map { it }
        .flatMap { DiscoveryIntentMapper.params(it, apiClient, apolloClient) }

    val verification = uriFromVerification
        .map { it.getTokenFromQueryParams() }
        .filter { it.isNotNull() }
        .switchMap { apiClient.verifyEmail(it) }
        .materialize()
        .share()
        .distinctUntilChanged()

    verification
        .compose(Transformers.valuesV2())
        .map { it.message() }
        .subscribe { successMessage.onNext(it) }
        .addToDisposable(disposables)

    verification
        .compose(Transformers.errorsV2())
        .map { ErrorEnvelope.fromThrowable(it) }
        .map { it.errorMessage() }
        .filter { it.isNotNull() }
        .map { it }
        .subscribe { messageError.onNext(it) }
        .addToDisposable(disposables)

    setupUserOnboarding(disposables)

    val pagerSelectedPage = pagerSetPrimaryPage.distinctUntilChanged()

    val drawerParamsClicked = childFilterRowClick
        .mergeWith(topFilterRowClick)
        .withLatestFrom(
            pagerSelectedPage.map { DiscoveryUtils.sortFromPosition(it) }
        ) { drawerClickParams, currentParams ->
            if (drawerClickParams.params().sort() == null)
                drawerClickParams.params().toBuilder().sort(currentParams).build()
            else drawerClickParams.params()
        }

    val params = Observable.merge(
        paramsFromInitialIntent,
        paramsFromIntent,
        drawerParamsClicked
    )

    val sortToTabOpen = Observable.merge(
        pagerSelectedPage.map { DiscoveryUtils.sortFromPosition(it) },
        params.filter { it.sort().isNotNull() }.map { it.sort() }
    ).filter { it.isNotNull() }
        .map { it }

    val paramsWithSort = Observable.combineLatest(
        params,
        sortToTabOpen
    ) { p, s -> p.toBuilder().sort(s).build() }

    paramsWithSort
        .subscribe { updateParamsForPage.onNext(it) }
        .addToDisposable(disposables)

    paramsWithSort
        .compose(Transformers.takePairWhenV2(sortClicked.map { DiscoveryUtils.sortFromPosition(it) }))
        .map<Pair<DiscoveryParams.Sort, DiscoveryParams>> {
            Pair.create(
                it.first.sort(),
                it.first.toBuilder().sort(it.second).build()
            )
        }
        .subscribe { analyticEvents.trackDiscoverSortCTA(it.first, it.second) }
        .addToDisposable(disposables)

    paramsWithSort
        .compose(Transformers.takeWhenV2(drawerParamsClicked))
        .subscribe { analyticEvents.trackDiscoverFilterCTA(it) }
        .addToDisposable(disposables)

    val categories = apolloClient.fetchCategories()
        .compose(Transformers.neverErrorV2())
        .flatMapIterable { it }
        .toSortedList()
        .toObservable()
        .share()

    Observable.combineLatest<List<Category>?, Int, Pair<List<Category>, Int>>(
        categories
            .flatMapIterable { it }
            .filter { it.isRoot }
            .toList().toObservable(),
        pagerSelectedPage
    ) { c, psp -> Pair.create(c, psp) }
        .subscribe { rootCategoriesAndPosition.onNext(it) }
        .addToDisposable(disposables)

    val drawerClickedParentCategory = parentFilterRowClick
        .map { it.params().category() ?: Category.builder().build() }
        .startWith(Category.builder().build())

    val expandedCategory = Observable.merge(
        topFilterRowClick.map { Category.builder().build() },
        drawerClickedParentCategory
    ).scan { previous: Category, next: Category ->
        if (previous == next) {
            return@scan Category.builder().build()
        }
        next
    }

    pagerSelectedPage
        .compose(Transformers.takeWhenV2(params))
        .compose(Transformers.combineLatestPair(changedUser))
        .map { it.first }
        .flatMap { pageInt ->
            Observable.just(
                DiscoveryParams.Sort.defaultSorts
                    .map { it.positionFromSort() }
                    .filter { it != pageInt }
            )
        }
        .subscribe { clearPages.onNext(it) }
        .addToDisposable(disposables)

    params
        .distinctUntilChanged()
        .subscribe { updateToolbarWithParams.onNext(it) }
        .addToDisposable(disposables)

    updateParamsForPage
        .map { true }
        .subscribe { expandSortTabLayout.onNext(it) }
        .addToDisposable(disposables)

    Observable.combineLatest(
        categories,
        params,
        expandedCategory,
        currentUser
    ) { c, s, ec, u -> s.deriveNavigationDrawerData(c, ec, u) }
        .distinctUntilChanged()
        .subscribe { navigationDrawerData.onNext(it) }
        .addToDisposable(disposables)

    val drawerOpenObservables = listOf(
        childFilterRowClick.map { },
        topFilterRowClick.map { },
        internalToolsClick.map { },
        loggedOutLoginToutClick.map { },
        loggedOutHelpClick.map { },
        activityFeedClick.map { },
        messagesClick.map { },
        profileClick.map { },
        settingsClick.map { },
        pledgedProjectsClick.map { }
    )

    Observable.merge(drawerOpenObservables)
        .subscribe { closeDrawer.onNext(Unit) }
        .addToDisposable(disposables)

    currentUser
        .compose(Transformers.takeWhenV2(darkThemeEnabled))
        .map { DiscoveryViewModelHelper.currentDrawerMenuIcon(it, isDarkTheme) }
        .distinctUntilChanged()
        .subscribe { drawerMenuIcon.onNext(it) }
        .addToDisposable(disposables)
}

internal fun DiscoveryViewModel.DiscoveryViewModel.setupUserOnboarding(disposables: CompositeDisposable) {
    val newUserHeuristic = !sharedPreferences.contains(CONSENT_MANAGEMENT_PREFERENCE)

    Observable.just(ffClient?.getBoolean(FlagKey.ANDROID_NATIVE_ONBOARDING_FLOW))
        .filter { it }
        .filter { !sharedPreferences.getBoolean(HAS_SEEN_ONBOARDING, false) }
        .filter { newUserHeuristic }
        .subscribe { showOnboardingFlow.onNext(Unit) }
        .addToDisposable(disposables)

    hasSeenOnboarding
        .subscribe { sharedPreferences.edit { putBoolean(HAS_SEEN_ONBOARDING, it) } }
        .addToDisposable(disposables)

    currentUserType.isLoggedIn
        .filter { it }
        .distinctUntilChanged()
        .take(1)
        .filter { !sharedPreferences.getBoolean(HAS_SEEN_NOTIF_PERMISSIONS, false) }
        .subscribe { showNotifPermissionRequest.onNext(Unit) }
        .addToDisposable(disposables)

    hasSeenNotificationsPermission
        .subscribe { sharedPreferences.edit { putBoolean(HAS_SEEN_NOTIF_PERMISSIONS, it) } }
        .addToDisposable(disposables)

    hasExitedOnboarding
        .switchMap { hasSeenConsentManagement }
        .filter { !it }
        .subscribe { showConsentManagementDialog.onNext(Unit) }
        .addToDisposable(disposables)
}
