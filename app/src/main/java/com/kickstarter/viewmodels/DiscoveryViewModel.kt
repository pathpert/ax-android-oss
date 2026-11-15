package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Category
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

@Deprecated(
    message = "Legacy monolithic ViewModel replaced by DiscoveryFeatureViewModel",
    replaceWith = ReplaceWith(
        "DiscoveryFeatureViewModel",
        "com.kickstarter.features.discovery.viewmodel.DiscoveryFeatureViewModel"
    )
)
interface DiscoveryViewModel {
    interface Inputs : DiscoveryDrawerAdapter.Delegate, DiscoveryPagerAdapter.Delegate {
        /** Call when the user selects a sort tab.  */
        fun sortClicked(sortPosition: Int)

        /** Call when the user has seen the consent management dialog.  */
        fun hasSeenConsentManagement(hasShown: Boolean)

        /** Call when the user has seen the notifications permission request.  */
        fun hasSeenNotificationsPermission(hasShown: Boolean)

        /** Call when the user has seen the onboarding flow.  */
        fun hasSeenOnboarding(hasSeen: Boolean)

        /** Call when the user has exited the onboarding flow.  */
        fun hasExitedOnboarding()
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    interface Outputs {
        fun drawerMenuIcon(): Observable<Int>
        fun expandSortTabLayout(): Observable<Boolean>
        fun updateToolbarWithParams(): Observable<DiscoveryParams>
        fun updateParamsForPage(): Observable<DiscoveryParams>
        fun navigationDrawerData(): Observable<NavigationDrawerData>
        fun rootCategoriesAndPosition(): Observable<Pair<List<Category>, Int>>
        fun clearPages(): Observable<List<Int>>
        fun showActivityFeed(): Observable<Unit>
        fun showHelp(): Observable<Unit>
        fun showInternalTools(): Observable<Unit>
        fun showLoginTout(): Observable<Unit>
        fun showMessages(): Observable<Unit>
        fun showProfile(): Observable<Unit>
        fun showPledgedProjects(): Observable<Unit>
        fun showSettings(): Observable<Unit>
        fun showSuccessMessage(): Observable<String>
        fun showNotifPermissionsRequest(): Observable<Unit>
        fun showConsentManagementDialog(): Observable<Unit>
        fun showOnboardingFlow(): Observable<Unit>
        fun showErrorMessage(): Observable<String>
        fun darkThemeEnabled(): Observable<Boolean>
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class DiscoveryViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs = this
        val outputs = this

        internal val apiClient = requireNotNull(environment.apiClientV2())
        internal val apolloClient = requireNotNull(environment.apolloClientV2())
        internal val currentUserType = requireNotNull(environment.currentUserV2())
        internal val currentConfigType = requireNotNull(environment.currentConfigV2())
        internal val sharedPreferences = requireNotNull(environment.sharedPreferences())
        internal val analyticEvents = requireNotNull(environment.analytics())
        internal val ffClient = environment.featureFlagClient()

        internal val activityFeedClick = PublishSubject.create<Unit>()
        internal val childFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row>()
        internal val internalToolsClick = PublishSubject.create<Unit>()
        internal val loggedOutLoginToutClick = PublishSubject.create<Unit>()
        internal val loggedOutHelpClick = PublishSubject.create<Unit>()
        internal val messagesClick = PublishSubject.create<Unit>()
        internal val pagerSetPrimaryPage = PublishSubject.create<Int>()
        internal val parentFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row>()
        internal val profileClick = PublishSubject.create<Unit>()
        internal val showNotifPermissionRequest = BehaviorSubject.create<Unit>()
        internal val showConsentManagementDialog = BehaviorSubject.create<Unit>()
        internal val showOnboardingFlow = BehaviorSubject.create<Unit>()
        internal val settingsClick = PublishSubject.create<Unit>()
        internal val pledgedProjectsClick = PublishSubject.create<Unit>()
        internal val sortClicked = PublishSubject.create<Int>()
        internal val hasSeenConsentManagement = BehaviorSubject.createDefault<Boolean>(false)
        internal val hasSeenNotificationsPermission = PublishSubject.create<Boolean>()
        internal val hasSeenOnboarding = PublishSubject.create<Boolean>()
        internal val hasExitedOnboarding = PublishSubject.create<Unit>()
        internal val topFilterRowClick = PublishSubject.create<NavigationDrawerData.Section.Row>()
        internal val clearPages = BehaviorSubject.create<List<Int>>()
        internal val drawerMenuIcon = BehaviorSubject.create<Int>()
        internal val expandSortTabLayout = BehaviorSubject.create<Boolean>()
        internal val navigationDrawerData = BehaviorSubject.create<NavigationDrawerData>()
        internal val rootCategoriesAndPosition = BehaviorSubject.create<Pair<List<Category>, Int>>()
        internal lateinit var showActivityFeed: Observable<Unit>
        internal lateinit var showHelp: Observable<Unit>
        internal lateinit var showInternalTools: Observable<Unit>
        internal lateinit var showLoginTout: Observable<Unit>
        internal lateinit var showMessages: Observable<Unit>
        internal lateinit var showProfile: Observable<Unit>
        internal lateinit var showSettings: Observable<Unit>
        internal lateinit var showPledgedProjects: Observable<Unit>
        internal val updateParamsForPage = BehaviorSubject.create<DiscoveryParams>()
        internal val updateToolbarWithParams = BehaviorSubject.create<DiscoveryParams>()
        internal val successMessage = PublishSubject.create<String>()
        internal val messageError = PublishSubject.create<String>()
        internal val darkThemeEnabled = BehaviorSubject.create<Boolean>()
        internal val intent = PublishSubject.create<Intent>()
        internal val closeDrawer = PublishSubject.create<Unit>()
        internal var isDarkTheme = false
        internal val disposables = CompositeDisposable()

        init {
            initPipelines(disposables)
        }

        override fun childFilterViewHolderRowClick(viewHolder: ChildFilterViewHolder, row: NavigationDrawerData.Section.Row) = childFilterRowClick.onNext(row)
        override fun discoveryPagerAdapterSetPrimaryPage(adapter: DiscoveryPagerAdapter, position: Int) = pagerSetPrimaryPage.onNext(position)
        override fun loggedInViewHolderActivityClick(viewHolder: LoggedInViewHolder) = activityFeedClick.onNext(Unit)
        override fun loggedInViewHolderInternalToolsClick(viewHolder: LoggedInViewHolder) = internalToolsClick.onNext(Unit)
        override fun loggedInViewHolderMessagesClick(viewHolder: LoggedInViewHolder) = messagesClick.onNext(Unit)
        override fun loggedInViewHolderProfileClick(viewHolder: LoggedInViewHolder, user: User) = profileClick.onNext(Unit)
        override fun loggedInViewHolderSettingsClick(viewHolder: LoggedInViewHolder, user: User) = settingsClick.onNext(Unit)
        override fun loggedInViewHolderPledgedProjectsClick(viewHolder: LoggedInViewHolder) = pledgedProjectsClick.onNext(Unit)
        override fun loggedOutViewHolderActivityClick(viewHolder: LoggedOutViewHolder) = activityFeedClick.onNext(Unit)
        override fun loggedOutViewHolderInternalToolsClick(viewHolder: LoggedOutViewHolder) = internalToolsClick.onNext(Unit)
        override fun loggedOutViewHolderLoginToutClick(viewHolder: LoggedOutViewHolder) = loggedOutLoginToutClick.onNext(Unit)
        override fun loggedOutViewHolderHelpClick(viewHolder: LoggedOutViewHolder) = loggedOutHelpClick.onNext(Unit)
        override fun loggedOutViewHolderSettingsClick(viewHolder: LoggedOutViewHolder) = settingsClick.onNext(Unit)
        override fun topFilterViewHolderRowClick(viewHolder: TopFilterViewHolder, row: NavigationDrawerData.Section.Row) = topFilterRowClick.onNext(row)
        override fun parentFilterViewHolderRowClick(viewHolder: ParentFilterViewHolder, row: NavigationDrawerData.Section.Row) = parentFilterRowClick.onNext(row)
        override fun sortClicked(sortPosition: Int) = sortClicked.onNext(sortPosition)
        override fun hasSeenConsentManagement(hasShown: Boolean) = hasSeenConsentManagement.onNext(hasShown)
        override fun hasSeenNotificationsPermission(hasShown: Boolean) = hasSeenNotificationsPermission.onNext(hasShown)
        override fun hasSeenOnboarding(hasSeen: Boolean) = hasSeenOnboarding.onNext(hasSeen)
        override fun hasExitedOnboarding() = hasExitedOnboarding.onNext(Unit)

        override fun clearPages(): Observable<List<Int>> = clearPages
        override fun drawerMenuIcon(): Observable<Int> = drawerMenuIcon
        override fun expandSortTabLayout(): Observable<Boolean> = expandSortTabLayout
        override fun navigationDrawerData(): Observable<NavigationDrawerData> = navigationDrawerData
        override fun rootCategoriesAndPosition(): Observable<Pair<List<Category>, Int>> = rootCategoriesAndPosition
        override fun showActivityFeed(): Observable<Unit> = showActivityFeed
        override fun showHelp(): Observable<Unit> = showHelp
        override fun showInternalTools(): Observable<Unit> = showInternalTools
        override fun showLoginTout(): Observable<Unit> = showLoginTout
        override fun showMessages(): Observable<Unit> = showMessages
        override fun showProfile(): Observable<Unit> = showProfile
        override fun showPledgedProjects(): Observable<Unit> = showPledgedProjects
        override fun showSettings(): Observable<Unit> = showSettings
        override fun updateParamsForPage(): Observable<DiscoveryParams> = updateParamsForPage
        override fun updateToolbarWithParams(): Observable<DiscoveryParams> = updateToolbarWithParams
        override fun showSuccessMessage(): Observable<String> = successMessage
        override fun showErrorMessage(): Observable<String> = messageError
        override fun showNotifPermissionsRequest(): Observable<Unit> = showNotifPermissionRequest
        override fun showConsentManagementDialog(): Observable<Unit> = showConsentManagementDialog
        override fun showOnboardingFlow(): Observable<Unit> = showOnboardingFlow
        override fun darkThemeEnabled(): Observable<Boolean> = darkThemeEnabled
        fun closeDrawer(): Observable<Unit> = closeDrawer

        fun setDarkTheme(isDarkTheme: Boolean) {
            this.isDarkTheme = isDarkTheme
            darkThemeEnabled.onNext(isDarkTheme)
        }

        fun provideIntent(intent: Intent) {
            if (intent.isNotNull()) {
                this.intent.onNext(intent)
            }
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }
    }

    @Deprecated("Legacy monolithic path replaced by features.*.viewmodel.*")
    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiscoveryViewModel(environment) as T
        }
    }
}
