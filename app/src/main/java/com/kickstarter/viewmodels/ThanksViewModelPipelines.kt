package com.kickstarter.viewmodels

import android.os.Parcelable
import android.util.Pair
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.THANKS
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.combineProjectsAndParams
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.updateStartedProjectAndDiscoveryParamsList
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isLocationGermany
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.data.ThanksData
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

/**
 * ViewModel managing state transitions and user intents for ThanksViewModel.
 *
 * Negative constraints:
 * - For new code, consume corresponding feature contracts in com.kickstarter.features.*; do not mutate state directly.
 */
internal fun ThanksViewModel.ThanksViewModel.initPipelines(disposables: CompositeDisposable) {
    val project = intent()
        .filter { it.getParcelableExtra<Parcelable?>(IntentKey.PROJECT).isNotNull() }
        .map<Project?> { it.getParcelableExtra(IntentKey.PROJECT) }
        .ofType(Project::class.java)
        .take(1)

    val rootCategory = project
        .switchMap {
            findRootCategory(it, apolloClient)
        }
        .compose(Transformers.neverErrorV2())
        .filter { it.isNotNull() }
        .map { it }

    val isGamesCategory = rootCategory.map { "games" == it.slug() }

    val hasSeenGamesNewsletterDialog = Observable.just(
        hasSeenGamesNewsletterPreference?.get() ?: false
    ).filter { it.isNotNull() }.map { it }

    val isSignedUpToGamesNewsletter = currentUser.observable()
        .map { it.getValue() != null && it.getValue()?.gamesNewsletter().isTrue() }

    val showGamesNewsletter = Observable.combineLatest(
        isGamesCategory,
        hasSeenGamesNewsletterDialog,
        isSignedUpToGamesNewsletter
    ) { isGames, hasSeen, isSignedUp ->
        isGames && !hasSeen && !isSignedUp
    }

    categoryCardViewHolderClicked
        .map { DiscoveryParams.builder().category(it).build() }
        .subscribe { startDiscoveryActivity.onNext(it) }
        .addToDisposable(disposables)

    closeButtonClicked
        .subscribe(finish)

    projectCardViewHolderClicked
        .subscribe {
            startProjectActivity.onNext(Pair(it, RefTag.thanks()))
        }
        .addToDisposable(disposables)

    val projectOnUserChangeSave = this.onHeartButtonClicked
        .switchMap { this.toggleProjectSave(it) }
        .share()

    val recommendedProjects = project.switchMap {
        fetchRelatedProjects(it, apiClient)?.toObservable()
    }.filter { it.isNotNull() }
        .map {
            combineProjectsAndParams(it, DiscoveryParams.builder().build())
        }

    projectOnUserChangeSave
        .subscribe { this.analyticEvents?.trackWatchProjectCTA(it, THANKS) }
        .addToDisposable(disposables)

    projectOnUserChangeSave
        .filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
        .compose(Transformers.ignoreValuesV2())
        .subscribe(this.showSavedPrompt)

    Observable.just(hasSeenAppRatingPreference?.get())
        .filter { it.isNotNull() }
        .map { it }
        .take(1)
        .compose(Transformers.combineLatestPair(showGamesNewsletter))
        .filter { !it.first && !it.second }
        .filter { environment.featureFlagClient()?.getBoolean(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG) == false }
        .compose(Transformers.ignoreValuesV2())
        .subscribe { showRatingDialog.onNext(Unit) }
        .addToDisposable(disposables)

    showGamesNewsletter
        .filter { it }
        .subscribe { showGamesNewsletterDialog.onNext(Unit) }
        .addToDisposable(disposables)

    showGamesNewsletterDialog
        .subscribe { hasSeenGamesNewsletterPreference?.set(true) }
        .addToDisposable(disposables)

    currentUser.observable()
        .filter { it.getValue().isNotNull() }
        .map { it.getValue() }
        .compose(Transformers.takeWhenV2(signupToGamesNewsletterClick))
        .flatMap { executeSignupToGamesNewsletter(it, apiClient) }
        .subscribe { signedUpToGamesNewsletter.onNext(it) }
        .addToDisposable(disposables)

    currentUser.observable()
        .filter { it.getValue().isNotNull() }
        .map { it.getValue() }
        .compose(Transformers.takeWhenV2(signedUpToGamesNewsletter))
        .filter { it.isLocationGermany() }
        .subscribe { showConfirmGamesNewsletterDialog.onNext(Unit) }
        .addToDisposable(disposables)

    val checkoutData = intent()
        .filter { it.getParcelableExtra<Parcelable?>(IntentKey.CHECKOUT_DATA).isNotNull() }
        .map<CheckoutData?> { it.getParcelableExtra(IntentKey.CHECKOUT_DATA) }
        .ofType(CheckoutData::class.java)
        .take(1)

    val pledgeData = intent()
        .filter { it.getParcelableExtra<Parcelable?>(IntentKey.PLEDGE_DATA).isNotNull() }
        .map<PledgeData?> { it.getParcelableExtra(IntentKey.PLEDGE_DATA) }
        .ofType(PledgeData::class.java)
        .take(1)

    val checkoutAndPledgeData = Observable.combineLatest<CheckoutData, PledgeData, Pair<CheckoutData, PledgeData>>(
        checkoutData,
        pledgeData
    ) { a, b -> Pair(a, b) }

    checkoutAndPledgeData
        .subscribe { checkoutDataPledgeData: Pair<CheckoutData, PledgeData> ->
            analyticEvents?.trackThanksScreenViewed(
                checkoutDataPledgeData.first,
                checkoutDataPledgeData.second
            )
        }
        .addToDisposable(disposables)

    Observable.combineLatest(
        project,
        rootCategory,
        recommendedProjects.startWith(listOf()),
        checkoutData,
    ) { backedProject, category, projects, cData ->
        ThanksData(backedProject, cData, category, projects)
    }.subscribe { adapterData.onNext(it) }
        .addToDisposable(disposables)

    adapterData
        .compose(Transformers.takePairWhenV2(projectOnUserChangeSave))
        .map { Pair(it.first, it.second.updateStartedProjectAndDiscoveryParamsList(it.first.recommendedProjects)) }
        .map {
            ThanksData(it.first.backedProject, it.first.checkoutData, it.first.category, it.second)
        }.distinctUntilChanged()
        .subscribe { adapterData.onNext(it) }
        .addToDisposable(disposables)

    SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
        .sendThirdPartyEvent(
            project = project,
            apolloClient = apolloClient,
            currentUser = currentUser,
            eventName = ThirdPartyEventValues.EventName.PURCHASE,
            checkoutAndPledgeData = checkoutAndPledgeData,
        )
        .compose(Transformers.neverErrorV2())
        .subscribe { onThirdPartyEventSent.onNext(it.first) }
        .addToDisposable(disposables)

    checkoutAndPledgeData
        .compose(Transformers.takePairWhenV2(projectCardViewHolderClicked))
        .subscribe { dataCheckoutProjectPair: Pair<Pair<CheckoutData, PledgeData>, Project> ->
            val cookieRefTag = RefTagUtils.storedCookieRefTagForProject(
                dataCheckoutProjectPair.second,
                cookieManager,
                sharedPreferences
            )
            val projectData = ProjectData.builder()
                .refTagFromIntent(RefTag.thanks())
                .refTagFromCookie(cookieRefTag)
                .project(dataCheckoutProjectPair.second)
                .build()

            analyticEvents?.trackThanksActivityProjectCardClicked(
                projectData,
                dataCheckoutProjectPair.first.first,
                dataCheckoutProjectPair.first.second
            )
        }.addToDisposable(disposables)
}

internal fun fetchRelatedProjects(
    project: Project,
    client: ApiClientTypeV2
): Single<MutableList<Project>>? {
    val recommendedParams = DiscoveryParams.builder()
        .backed(-1)
        .recommended(true)
        .perPage(6)
        .build()

    val similarToParams = DiscoveryParams.builder()
        .backed(-1)
        .similarTo(project)
        .perPage(3)
        .build()

    val category = project.category()

    val staffPickParams = DiscoveryParams.builder()
        .category(category?.root())
        .backed(-1)
        .staffPicks(true)
        .perPage(3)
        .build()

    val recommendedProjects = client.fetchProjects(recommendedParams)
        .retry(2)
        .map { it.projects() }
        .map { ListUtils.shuffle(it) }
        .flatMap { Observable.fromIterable(it) }
        .take(3)
        .filter { it.isNotNull() }

    val similarToProjects = client.fetchProjects(similarToParams)
        .retry(2)
        .map { it.projects() }
        .flatMap { Observable.fromIterable(it) }

    val staffPickProjects = client.fetchProjects(staffPickParams)
        .retry(2)
        .map { it.projects() }
        .flatMap { Observable.fromIterable(it) }

    return Observable.concat(recommendedProjects, similarToProjects, staffPickProjects)
        .compose(Transformers.neverErrorV2())
        .distinct()
        .take(3)
        .toList()
}

internal fun executeSignupToGamesNewsletter(user: User, client: ApiClientTypeV2): Observable<User> {
    return client
        .updateUserSettings(user.toBuilder().gamesNewsletter(true).build())
        .compose(Transformers.neverErrorV2())
}

internal fun findRootCategory(
    project: Project,
    client: ApolloClientTypeV2
): Observable<Category?> {
    val category = project.category() ?: return Observable.empty()
    return when {
        category.parent() != null -> Observable.just(category.parent())
        category.isRoot -> Observable.just(category)
        else -> client.fetchCategory(category.rootId().toString())
    }
}
