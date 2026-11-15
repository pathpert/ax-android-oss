package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES_TAG
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.OUR_RULES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.OUR_RULES_TAG
import org.joda.time.DateTime

internal fun ProjectOverviewViewModel.ProjectOverviewViewModel.initPipelines() {
    val project = projectData
        .distinctUntilChanged()
        .filter { it.project().isNotNull() }
        .map { requireNotNull(it.project()) }

    avatarPhotoUrl = project
        .filter { it.creator().avatar().medium().isNotNull() }
        .map { requireNotNull(it.creator().avatar().medium()) }

    backersCountTextViewText = project
        .map { NumberUtils.format(it.backersCount()) }

    blurbTextViewText = project
        .map { it.blurb() }

    categoryTextViewText = project
        .filter { it.category().isNotNull() }
        .map { it.category() }
        .map { it?.name() ?: "" }

    commentsCountTextViewText = project
        .filter { it.commentsCount().isNotNull() }
        .map { requireNotNull(it.commentsCount()) }
        .map { NumberUtils.format(it) }

    conversionTextViewIsGone = project
        .map { it.currency() != it.currentCurrency() }
        .map { it.negate() }

    conversionPledgedAndGoalText = project
        .map { proj ->
            val pledged = ksCurrency.format(proj.pledged(), proj)
            val goal = ksCurrency.format(proj.goal(), proj)
            Pair.create(pledged, goal)
        }

    creatorNameTextViewText = project
        .map { it.creator().name() }

    val creatorDetailsNotification = project
        .take(1)
        .distinctUntilChanged()
        .map { it.slug() }
        .switchMap { slug ->
            apolloClient.creatorDetails(slug ?: "")
                .doOnSubscribe { creatorDetailsLoadingContainerIsVisible.onNext(true) }
                .doAfterTerminate { creatorDetailsLoadingContainerIsVisible.onNext(false) }
                .materialize()
        }
        .share()

    creatorDetailsNotification
        .compose(Transformers.errorsV2())
        .map { true }
        .subscribe { creatorDetailsIsGone.onNext(it) }
        .addToDisposable(disposables)

    deadlineCountdownTextViewText = project
        .map { proj -> proj.deadlineCountdownValue() }
        .map { NumberUtils.format(it) }

    goalStringForTextView = project
        .map { p: Project -> ksCurrency.formatWithUserPreference(p.goal(), p) }

    locationTextViewText = project
        .filter { it.location().isNotNull() }
        .map { it.location() }
        .map { it?.displayableName() ?: "" }

    percentageFundedProgress = project
        .map { it.percentageFunded() }
        .map { ProgressBarUtils.progress(it) }

    percentageFundedProgressBarIsGone = project
        .map { p: Project -> p.isSuccessful || p.isCanceled || p.isFailed || p.isSuspended }

    pledgedTextViewText = project
        .map { p: Project -> ksCurrency.formatWithUserPreference(p.pledged(), p) }

    val userIsCreatorOfProject = project
        .map { it.creator() }
        .compose(Transformers.combineLatestPair(currentUser.observable()))
        .map { creatorAndCurrentUser: Pair<User, KsOptional<User>> ->
            creatorAndCurrentUser.second.isNotNull() &&
                creatorAndCurrentUser.first.id() == creatorAndCurrentUser.second?.getValue()?.id()
        }

    projectDisclaimerGoalReachedDateTime = project
        .filter { obj: Project -> obj.isFunded }
        .map { obj: Project -> obj.deadline() }

    projectDisclaimerGoalNotReachedString = project
        .filter { p: Project -> p.deadline() != null && p.isLive && !p.isFunded }
        .map { p: Project -> Pair.create(ksCurrency.format(p.goal(), p), p.deadline()) }

    projectDisclaimerTextViewIsGone =
        project.map { p: Project -> p.deadline() == null || !p.isLive }

    projectLaunchDate = project
        .filter { it.launchedAt().isNotNull() }
        .map { requireNotNull(it.launchedAt()) }
        .map { DateTimeUtils.longDate(it) }

    projectLaunchDateIsGone = project
        .compose(Transformers.combineLatestPair(userIsCreatorOfProject))
        .map { launchDateAndIsCreator: Pair<Project, Boolean> ->
            launchDateAndIsCreator.first.launchedAt().isNotNull() && launchDateAndIsCreator.second.isTrue()
        }
        .map { it.negate() }

    projectNameTextViewText = project
        .map { it.name() }

    projectOutput = project

    projectProjectNotice = project
        .filter { it.projectNotice().isNotNull() }
        .map { it.projectNotice() }

    projectSocialTextViewFriends = project
        .filter { it.isFriendBacking }
        .filter { it.friends().isNotNull() }
        .map { requireNotNull(it.friends()) }

    projectSocialImageViewUrl = projectSocialTextViewFriends
        .filter { it.first().isNotNull() }
        .map { requireNotNull(it.first()) }
        .map { it.avatar().medium() }

    projectSocialViewGroupIsGone = project
        .map { it.isFriendBacking }
        .map { it.negate() }

    projectStateViewGroupBackgroundColorInt = project
        .filter { p: Project -> !p.isLive }
        .map { p: Project ->
            if (p.state() == Project.STATE_SUCCESSFUL) R.color.green_alpha_50
            else R.color.kds_support_300
        }

    projectStateViewGroupIsGone = project
        .map { it.isLive }

    projectSocialImageViewIsGone = projectSocialViewGroupIsGone
    shouldSetDefaultStatsMargins = projectSocialViewGroupIsGone

    setCanceledProjectStateView = project
        .filter { it.isCanceled }
        .compose(Transformers.ignoreValuesV2())

    setProjectSocialClickListener = project
        .filter { it.isFriendBacking }
        .map { it.friends() }
        .filter { it.size >= 2 }
        .compose(Transformers.ignoreValuesV2())

    setSuccessfulProjectStateView = project
        .filter { it.isSuccessful }
        .filter { !(it.postCampaignPledgingEnabled() ?: false) || !(it.isInPostCampaignPledgingPhase() ?: false) }
        .map { it.stateChangedAt() ?: DateTime() }

    setSuccessfulProjectStillCollectingView = project
        .filter { it.isSuccessful }
        .filter { it.postCampaignPledgingEnabled() ?: false && it.isInPostCampaignPledgingPhase() ?: false }
        .map { it.stateChangedAt() ?: DateTime() }

    setSuspendedProjectStateView = project
        .filter { it.isSuspended }
        .compose(Transformers.ignoreValuesV2())

    setUnsuccessfulProjectStateView = project
        .filter { it.isFailed }
        .map { it.stateChangedAt() ?: DateTime() }

    startProjectSocialActivity = project.compose(
        Transformers.takeWhenV2(projectSocialViewGroupClicked)
    )

    updatesCountTextViewText = project
        .filter { it.updatesCount().isNotNull() }
        .map { requireNotNull(it.updatesCount()) }
        .map { NumberUtils.format(it) }

    startCreatorView = projectData
        .compose(Transformers.takePairWhenV2(creatorInfoClicked))
        .map { it.first }

    startCommentsView = projectData
        .compose(Transformers.takePairWhenV2(commentsClicked))
        .map { it.first }

    startUpdatesView = projectData
        .compose(Transformers.takePairWhenV2(updatesClicked))
        .map { it.first }

    startReportProjectView = projectData
        .compose(Transformers.takePairWhenV2(reportProjectButtonClicked))
        .map { it.first }
        .withLatestFrom(this.currentUser.isLoggedIn) { pData, isLoggedIn ->
            Pair(pData, isLoggedIn)
        }
        .filter { it.second }
        .map { it.first }

    reportProjectButtonClicked
        .withLatestFrom(this.currentUser.isLoggedIn) { _, isUser -> isUser }
        .filter { !it }
        .subscribe { this.startLogin.onNext(Unit) }
        .addToDisposable(disposables)

    shouldShowProjectFlagged = project
        .map { it.isFlagged() ?: false }
        .compose(Transformers.combineLatestPair(refreshFlagged.startWith("")))
        .map { pair ->
            val isFlagged = pair.first
            val shouldRefresh = pair.second
            if (shouldRefresh.isNotEmpty()) true else isFlagged
        }

    shouldShowReportProject = shouldShowProjectFlagged
        .map { !it }

    linkTagClicked
        .map {
            if (it.contains(OUR_RULES_TAG)) "${environment.webEndpoint()}$OUR_RULES"
            else if (it.contains(COMMUNITY_GUIDELINES_TAG)) "${environment.webEndpoint()}$COMMUNITY_GUIDELINES"
            else ""
        }
        .filter { it.isNotEmpty() }
        .subscribe { openExternally.onNext(it) }
        .addToDisposable(disposables)

    projectData
        .compose(Transformers.takePairWhenV2(campaignClicked))
        .map { it.first }
        .filter { it.project().isLive && !it.project().isBacking() }
        .subscribe { this.analyticEvents.trackCampaignDetailsCTAClicked(it) }
        .addToDisposable(disposables)
}
