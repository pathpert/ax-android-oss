package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.ProjectMetadata
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.libs.utils.extensions.isCompleted
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.metadataForProject
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Project
import com.kickstarter.models.extensions.replaceSmallImageWithMediumIfEmpty
import io.reactivex.Observable
import org.joda.time.DateTime

internal fun ProjectCardHolderViewModel.ViewModel.initPipelines() {
    projectForDeadlineCountdownDetail = project
    backersCountTextViewText = project
        .map { it.backersCount() }
        .map { value -> NumberUtils.format(value) }

    backingViewGroupIsGone = project
        .map { it.metadataForProject() !== ProjectMetadata.BACKING }

    comingSoonViewGroupIsGone = project
        .map { it.metadataForProject() !== ProjectMetadata.COMING_SOON }

    deadlineCountdownText = project
        .map { it.deadlineCountdownValue() }
        .map { NumberUtils.format(it) }

    project
        .map { p -> if (p.isStarred()) R.drawable.icon__heart else R.drawable.icon__heart_outline }
        .subscribe { this.heartDrawableId.onNext(it) }
        .addToDisposable(disposables)

    project
        .compose(Transformers.takeWhenV2(heartButtonClicked))
        .filter { it.isNotNull() }
        .subscribe { notifyDelegateOfHeartButtonClicked.onNext(it) }
        .addToDisposable(disposables)

    featuredViewGroupIsGone = project
        .map { it.metadataForProject() !== ProjectMetadata.CATEGORY_FEATURED }

    friendAvatarUrl1 = project
        .filter(Project::isFriendBacking)
        .filter { it.isNotNull() }
        .map { it.friends() }
        .filter { it.isNotEmpty() }
        .map { it[0].avatar().replaceSmallImageWithMediumIfEmpty() }
        .filter { it.isNotEmpty() }

    friendAvatarUrl2 = project
        .filter(Project::isFriendBacking)
        .filter { it.isNotNull() }
        .map { it.friends() }
        .filter { it.size > 1 }
        .map { it[1].avatar().replaceSmallImageWithMediumIfEmpty() }
        .filter { it.isNotEmpty() }

    friendAvatarUrl3 = project
        .filter(Project::isFriendBacking)
        .filter { it.isNotNull() }
        .map { it.friends() }
        .filter { it.size > 2 }
        .map { it[2].avatar().replaceSmallImageWithMediumIfEmpty() }
        .filter { it.isNotEmpty() }

    friendAvatar2IsGone = project
        .filter { it.isNotNull() }
        .map { it.friends() }
        .map { it.size > 1 }
        .map { it.negate() }

    friendAvatar3IsGone = project
        .filter { it.isNotNull() }
        .map { it.friends() }
        .map { it.size > 2 }
        .map { it.negate() }

    friendBackingViewIsHidden = project
        .map(Project::isFriendBacking)
        .map { it.negate() }

    friendsForNamepile = project
        .filter(Project::isFriendBacking)
        .filter { it.isNotNull() }
        .filter { it.friends().isNotNull() }
        .map { it.friends() }

    fundingUnsuccessfulViewGroupIsGone = project
        .filter { it.isNotNull() }
        .map {
            it.state() != Project.STATE_CANCELED &&
                it.state() != Project.STATE_FAILED &&
                it.state() != Project.STATE_SUSPENDED
        }

    fundingSuccessfulViewGroupIsGone = project
        .filter { it.isNotNull() }
        .map { it.state() != Project.STATE_SUCCESSFUL }

    imageIsInvisible = project
        .filter { it.isNotNull() }
        .map { it.photo().isNull() }

    project
        .filter { it.isNotNull() && it.location().isNotNull() }
        .map { requireNotNull(it.location()) }
        .map { it.displayableName() }
        .distinctUntilChanged()
        .subscribe { locationName.onNext(it) }
        .addToDisposable(disposables)

    discoveryParams
        .map { ProjectCardHolderViewModelHelper.shouldShowLocationTag(it) }
        .compose(Transformers.combineLatestPair(project))
        .map { distanceSortAndProject: Pair<Boolean, Project> ->
            distanceSortAndProject.first == true && distanceSortAndProject.second.location().isNotNull()
        }
        .map { it.negate() }
        .distinctUntilChanged()
        .subscribe { locationContainerIsGone.onNext(it) }
        .addToDisposable(disposables)

    metadataViewGroupIsGone = project
        .map { it.metadataForProject() == ProjectMetadata.NONE }

    metadataViewGroupBackground = backingViewGroupIsGone
        .compose(Transformers.combineLatestPair(comingSoonViewGroupIsGone))
        .map {
            val backingGone = it.first
            val comingSoonGone = it.second
            if (backingGone && comingSoonGone) {
                R.drawable.rect_white_grey_stroke
            } else {
                R.drawable.rect_green_grey_stroke
            }
        }

    nameAndBlurbText = project
        .filter { it.isNotNull() }
        .map { Pair.create(it.name(), it.blurb()) }

    notifyDelegateOfProjectClick = project
        .compose(Transformers.takeWhenV2(projectCardClicked))

    percentageFundedForProgressBar = project
        .filter { it.isNotNull() }
        .map {
            if (it.state() == Project.STATE_LIVE || it.state() == Project.STATE_SUCCESSFUL) {
                it.percentageFunded()
            } else {
                0.0f
            }
        }.map { ProgressBarUtils.progress(it) }

    percentageFundedProgressBarIsGone = project
        .filter { it.isNotNull() }
        .map { it.state() == Project.STATE_CANCELED }

    percentageFundedTextViewText = project
        .map { it.percentageFunded() }
        .map { NumberUtils.flooredPercentage(it) }

    photoUrl = project
        .filter { it.isNotNull() }
        .map {
            if (it.photo() == null) ""
            else it.photo()?.full() ?: ""
        }

    projectCanceledAt = project
        .filter { it.isNotNull() }
        .filter { it.state() == Project.STATE_CANCELED }
        .map { it.stateChangedAt() ?: DateTime() }

    projectCardStatsViewGroupIsGone = project
        .filter { it.isNotNull() }
        .map { it.state() != Project.STATE_LIVE }

    projectFailedAt = project
        .filter { it.isNotNull() }
        .filter { it.state() == Project.STATE_FAILED }
        .map { it.stateChangedAt() ?: DateTime() }

    projectStateViewGroupIsGone = project
        .filter { it.isNotNull() }
        .map { it.isCompleted() }
        .map { it.negate() }

    val projectCategory = project
        .filter { it.isNotNull() && it.category() != null }
        .map { requireNotNull(it.category()) }

    projectSubcategoryIsGone = discoveryParams
        .filter { it.category().isNotNull() }
        .map { requireNotNull(it.category()) }
        .compose(Transformers.combineLatestPair(projectCategory))
        .map { ProjectCardHolderViewModelHelper.areParamsAllOrSameCategoryAsProject(it) }
        .distinctUntilChanged()

    projectSubcategoryName = projectCategory
        .map { it.name() }

    projectSuccessfulAt = project
        .filter { it.isNotNull() }
        .filter { it.state() == Project.STATE_SUCCESSFUL }
        .map { it.stateChangedAt() ?: DateTime() }

    projectSuspendedAt = project
        .filter { it.isNotNull() }
        .filter { it.state() == Project.STATE_SUSPENDED }
        .map { it.stateChangedAt() ?: DateTime() }

    projectWeLoveIsGone = project
        .filter { it.isNotNull() }
        .map { it.staffPick() ?: false }
        .compose(
            Transformers.combineLatestPair(
                discoveryParams.map { it.staffPicks() ?: false }
            )
        )
        .map { it.first == true && it.second == false }
        .map { it.negate() }
        .distinctUntilChanged()

    projectTagContainerIsGone = Observable.combineLatest<Boolean, Boolean, Pair<Boolean, Boolean>>(
        projectSubcategoryIsGone,
        projectWeLoveIsGone,
    ) { a: Boolean?, b: Boolean? -> Pair.create(a, b) }
        .map { it.first && it.second }
        .distinctUntilChanged()

    rootCategoryNameForFeatured = projectCategory
        .filter { it.isNotNull() && it.root().isNotNull() }
        .map { requireNotNull(it.root()) }
        .map { it.name() }

    savedViewGroupIsGone = project
        .filter { it.isNotNull() }
        .map { it.metadataForProject() !== ProjectMetadata.SAVING }

    setDefaultTopPadding = metadataViewGroupIsGone
}
