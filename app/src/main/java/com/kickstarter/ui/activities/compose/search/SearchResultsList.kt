package com.kickstarter.ui.activities.compose.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSProjectCardLarge
import com.kickstarter.ui.compose.designsystem.KSProjectCardSmall
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.KSVideoFeedBanner

@Composable
fun SearchResultsList(
    modifier: Modifier = Modifier,
    itemsList: List<Project>,
    isDefaultList: Boolean,
    isLoading: Boolean,
    lazyColumnListState: LazyListState,
    environment: Environment?,
    isVideoFeedBannerVisible: Boolean,
    onVideoFeedBannerClicked: () -> Unit,
    onItemClicked: (Project) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .testTag(SearchScreenTestTag.LIST_VIEW.name)
            .background(colors.backgroundSurfacePrimary)
            .fillMaxWidth(),
        contentPadding = PaddingValues(
            start = dimensions.paddingMediumLarge,
            end = dimensions.paddingMediumLarge
        ),
        state = lazyColumnListState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isVideoFeedBannerVisible) {
            item {
                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                KSVideoFeedBanner(onButtonClick = onVideoFeedBannerClicked)
                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }
        }

        itemsIndexed(itemsList) { index, project ->
            if (index == 0 && isDefaultList) {
                Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                Text(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.DISCOVER_PROJECTS_TITLE.name)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.activity_empty_state_logged_in_button),
                    style = typographyV2.title2,
                    color = colors.kds_support_700,
                    textAlign = TextAlign.Start
                )
            }

            val state = getCardProjectState(project)
            val fundingInfoString = getFundingInfoString(state, environment, project)

            if (index == 0) {
                Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                KSProjectCardLarge(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.FEATURED_PROJECT_VIEW.name),
                    photo = project.photo(),
                    title = project.name(),
                    state = state,
                    fundingInfoString = fundingInfoString,
                    fundedPercentage = project.percentageFunded().toInt(),
                ) {
                    onItemClicked(project)
                }

                if (itemsList.size > 1) {
                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                }
            } else {
                KSProjectCardSmall(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.NORMAL_PROJECT_VIEW.name + index),
                    photo = project.photo(),
                    title = project.name(),
                    state = state,
                    fundingInfoString = fundingInfoString,
                    fundedPercentage = project.percentageFunded().toInt(),
                ) {
                    onItemClicked(project)
                }

                if (index < itemsList.size - 1) {
                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                } else {
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                }
            }
        }

        item(isLoading) {
            if (isLoading && itemsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                KSCircularProgressIndicator(
                    modifier = Modifier
                        .testTag(SearchScreenTestTag.IN_LIST_LOADING_VIEW.name)
                        .size(size = dimensions.imageSizeLarge)
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }
        }
    }
}
