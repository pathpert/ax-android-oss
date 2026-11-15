package com.kickstarter.ui.activities.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.kickstarter.R
import com.kickstarter.features.projectstory.ProjectStoryUiState
import com.kickstarter.features.projectstory.data.RichTextItem
import com.kickstarter.features.projectstory.data.aspectRatio
import com.kickstarter.features.projectstory.data.childPhoto
import com.kickstarter.features.projectstory.data.hasChildPhoto
import com.kickstarter.features.projectstory.ui.RichTextItemPhotoComponent
import com.kickstarter.features.projectstory.ui.RichTextItemTextComponent
import com.kickstarter.features.projectstory.ui.WebViewComponent
import com.kickstarter.libs.utils.extensions.toSha256
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.SIMILAR_PROJECTS_CONTAINER
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.fragments.projectpage.ui.SimilarProjectsComponent
import com.kickstarter.ui.toolbars.compose.ToolbarIconButton
import com.kickstarter.ui.toolbars.compose.ToolbarIconToggleButton
import com.kickstarter.ui.toolbars.compose.TopToolBar
import com.kickstarter.viewmodels.projectpage.SimilarProjectsUiState
import timber.log.Timber

/**
 * Activity displaying and coordinating user interactions for PreLaunchProjectPageScreenPreview.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PreLaunchProjectPageScreenPreview() {
    KSTheme {
        val projectState = remember { mutableStateOf(ProjectFactory.backedProject()) }
        val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
        val projectStoryState = remember { mutableStateOf(ProjectStoryUiState()) }
        PreLaunchProjectPageScreen(projectState, similarProjectsState, projectStoryState)
    }
}

enum class PreLaunchProjectPageScreenTestTag {
    PROJECT_IMAGE,
    COMING_SOON_BADGE,
    PROJECT_NAME,
    CREATOR_LAYOUT,
    PROJECT_DESCRIPTION,
    PROJECT_CATEGORY_NAME,
    PROJECT_LOCATION_NAME,
    PROJECT_SAVE_BUTTON,
    PROJECT_FOLLOWERS,
    SIMILAR_PROJECTS_CONTAINER,
    CONTENT_LIST,
    RICH_TEXT_COMPONENT
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreLaunchProjectPageScreen(
    projectState: State<Project?>,
    similarProjectsState: State<SimilarProjectsUiState>,
    projectStoryState: State<ProjectStoryUiState>,
    leftOnClickAction: () -> Unit = {},
    rightOnClickAction: () -> Unit = {},
    middleRightClickAction: () -> Unit = {},
    onCreatorLayoutClicked: () -> Unit = {},
    onButtonClicked: () -> Unit = {},
    onSimilarProjectClick: (Project) -> Unit = {},
    numberOfFollowers: String? = null
) {
    val project = projectState.value
    val story = projectStoryState.value.storiedProject?.story
    val photoAspectRatiosByUrl = remember { mutableStateMapOf<String, Float>() }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopToolBar(
                right = {
                    ToolbarIconToggleButton(
                        icon = ImageVector.vectorResource(id = R.drawable.icon__heart_outline),
                        checkedImageVector = ImageVector.vectorResource(id = R.drawable.icon__heart),
                        clickAction = { rightOnClickAction.invoke() },
                        initialState = project?.isStarred() ?: false
                    )
                },
                middle = {
                    ToolbarIconButton(
                        icon = Icons.Filled.Share,
                        contentDescription = stringResource(R.string.project_accessibility_button_share_label),
                        clickAction = { middleRightClickAction.invoke() }
                    )
                },
                leftOnClickAction = { leftOnClickAction() }
            )
        }
    ) { contentPadding ->
        ConstraintLayout(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(colors.preLaunchProjectPage.background)
        ) {
            val (lazyColumn, buttonCardLayout) = createRefs()
            val screenPadding = dimensionResource(id = R.dimen.activity_horizontal_margin)

            LazyColumn(
                modifier = Modifier
                    .constrainAs(lazyColumn) {
                        linkTo(parent.top, buttonCardLayout.bottom)
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        absoluteRight.linkTo(parent.absoluteRight)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    }
                    .testTag(PreLaunchProjectPageScreenTestTag.CONTENT_LIST.name)
            ) {
                item {
                    PreLaunchProjectHeader(
                        project = project,
                        screenPadding = screenPadding,
                        onCreatorLayoutClicked = onCreatorLayoutClicked
                    )
                }

                if (!story?.items.isNullOrEmpty()) {
                    item {
                        Spacer(Modifier.height(dimensionResource(id = R.dimen.grid_2)))
                    }
                }

                itemsIndexed(
                    story?.items ?: emptyList(),
                    key = { index, item -> item.lazyListKey(index) },
                    contentType = { _, item -> item.lazyListContentType() }
                ) { _, item ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = screenPadding, vertical = dimensionResource(id = R.dimen.grid_1))
                            .testTag(PreLaunchProjectPageScreenTestTag.RICH_TEXT_COMPONENT.name)
                    ) {
                        when (item) {
                            is RichTextItem.Text -> {
                                when (item) {
                                    is RichTextItem.Text.Paragraph -> {
                                        val childPhoto = item.childPhoto()
                                        when {
                                            childPhoto != null -> {
                                                val photoUrl = childPhoto.asset?.url ?: childPhoto.url
                                                RichTextItemPhotoComponent(
                                                    childPhoto,
                                                    item.link,
                                                    placeholderAspectRatio = photoAspectRatiosByUrl[photoUrl],
                                                    onPhotoAspectRatioResolved = { aspectRatio ->
                                                        photoAspectRatiosByUrl[photoUrl] = aspectRatio
                                                    }
                                                )
                                            }
                                            else -> RichTextItemTextComponent(item)
                                        }
                                    }
                                    else -> RichTextItemTextComponent(item)
                                }
                            }
                            is RichTextItem.Photo -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(id = R.dimen.grid_1))
                                ) {
                                    val photoUrl = item.asset?.url ?: item.url
                                    RichTextItemPhotoComponent(
                                        item,
                                        placeholderAspectRatio = photoAspectRatiosByUrl[photoUrl],
                                        onPhotoAspectRatioResolved = { aspectRatio ->
                                            photoAspectRatiosByUrl[photoUrl] = aspectRatio
                                        }
                                    )
                                }
                            }
                            is RichTextItem.Oembed -> {
                                Timber.d("RichTextItem.Oembed item: $item")
                                if (item.iframeUrl.isNotBlank()) {
                                    val aspectRatio = item.aspectRatio?.takeIf { it > 0 } ?: (16f / 9f)
                                    Box(
                                        modifier = Modifier.fillMaxWidth().aspectRatio(aspectRatio)
                                    ) {
                                        WebViewComponent(item.iframeUrl)
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }

                if (!projectStoryState.value.isLoading &&
                    (projectStoryState.value.storiedProject != null || projectStoryState.value.error != null)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .padding(
                                    top = dimensionResource(id = R.dimen.grid_3),
                                    bottom = dimensionResource(id = R.dimen.grid_27)
                                )
                                .testTag(SIMILAR_PROJECTS_CONTAINER.name)
                        ) {
                            SimilarProjectsComponent(
                                uiState = similarProjectsState,
                                onClick = onSimilarProjectClick
                            )
                        }
                    }
                }
            }

            PreLaunchBottomBar(
                modifier = Modifier.constrainAs(buttonCardLayout) {
                    bottom.linkTo(parent.bottom)
                },
                project = project,
                screenPadding = screenPadding,
                numberOfFollowers = numberOfFollowers,
                onButtonClicked = onButtonClicked
            )
        }
    }
}

private fun RichTextItem.lazyListContentType(): String =
    if (this is RichTextItem.Text.Paragraph && hasChildPhoto()) {
        RichTextItem.Photo::class.simpleName.orEmpty()
    } else {
        this::class.simpleName.orEmpty()
    }

private fun RichTextItem.lazyListKey(index: Int): String =
    "${this.toString().toSha256()}_$index"
