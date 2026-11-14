package com.kickstarter.ui.activities.compose

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.toHtml
import com.kickstarter.models.Project
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.COMING_SOON_BADGE
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.CREATOR_LAYOUT
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_CATEGORY_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_DESCRIPTION
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_IMAGE
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_LOCATION_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_NAME
import com.kickstarter.ui.compose.ProjectImageFromURl
import com.kickstarter.ui.compose.TextBody2Style
import com.kickstarter.ui.compose.TextCaptionStyleWithStartIcon
import com.kickstarter.ui.compose.TextH6ExtraBoldTitle
import com.kickstarter.ui.compose.TextWithKdsSupport700Bg
import com.kickstarter.ui.views.compose.KsCreatorLayout

@Composable
fun PreLaunchProjectHeader(
    project: Project?,
    screenPadding: Dp,
    onCreatorLayoutClicked: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
    ) {
        val (
            projectImage, comingSoonBadge, projectName, creatorLayout,
            projectDescription, category, location
        ) = createRefs()

        ProjectImageFromURl(
            imageUrl = (project?.photo()?.full()),
            modifier = Modifier
                .testTag(PROJECT_IMAGE.name)
                .constrainAs(projectImage) {
                    top.linkTo(parent.top)
                }
                .aspectRatio(1.77f)
        )

        TextWithKdsSupport700Bg(
            stringResource(id = R.string.Coming_soon),
            Modifier
                .constrainAs(comingSoonBadge) {
                    top.linkTo(projectImage.bottom)
                    bottom.linkTo(projectImage.bottom)
                    start.linkTo(parent.start, screenPadding)
                }
                .testTag(COMING_SOON_BADGE.name)
        )

        val projectNameAlpha = if (project?.name().isNullOrBlank()) 0f else 1f
        TextH6ExtraBoldTitle(
            text = project?.name().orEmpty(),
            modifier = Modifier
                .alpha(projectNameAlpha)
                .constrainAs(projectName) {
                    top.linkTo(comingSoonBadge.bottom)
                    start.linkTo(parent.start, screenPadding)
                }
                .padding(end = screenPadding)
                .testTag(PROJECT_NAME.name)
        )

        KsCreatorLayout(
            creatorName = project?.creator()?.name() ?: "",
            imageUrl = project?.creator()?.avatar()?.medium() ?: "",
            modifier = Modifier
                .testTag(CREATOR_LAYOUT.name)
                .fillMaxWidth()
                .constrainAs(creatorLayout) {
                    top.linkTo(projectName.bottom)
                    start.linkTo(parent.start, screenPadding)
                }
                .padding(end = screenPadding)
                .padding(
                    vertical = dimensionResource(id = R.dimen.grid_1)
                ),
            onClickAction = onCreatorLayoutClicked
        )

        project?.blurb()?.let {
            TextBody2Style(
                text = it.toHtml().toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PROJECT_DESCRIPTION.name)
                    .constrainAs(projectDescription) {
                        top.linkTo(creatorLayout.bottom)
                        start.linkTo(parent.start, screenPadding)
                    }
                    .padding(end = screenPadding)
                    .padding(
                        vertical = dimensionResource(id = R.dimen.grid_1)
                    )
            )
        }

        project?.category()?.name()?.let {
            TextCaptionStyleWithStartIcon(
                it,
                painterResource(R.drawable.icon__compass),
                modifier = Modifier
                    .testTag(PROJECT_CATEGORY_NAME.name)
                    .constrainAs(category) {
                        top.linkTo(projectDescription.bottom)
                        start.linkTo(parent.start, screenPadding)
                    }
                    .padding(end = screenPadding)
                    .padding(vertical = dimensionResource(id = R.dimen.grid_2))
            )
        }

        project?.location()?.name()?.let {
            val locationPadding = dimensionResource(id = R.dimen.grid_5)
            TextCaptionStyleWithStartIcon(
                it,
                Icons.Filled.LocationOn,
                modifier = Modifier
                    .testTag(PROJECT_LOCATION_NAME.name)
                    .constrainAs(location) {
                        start.linkTo(category.end, locationPadding)
                        top.linkTo(category.top)
                        bottom.linkTo(category.bottom)
                    }
                    .padding(end = screenPadding)
            )
        }
    }
}
