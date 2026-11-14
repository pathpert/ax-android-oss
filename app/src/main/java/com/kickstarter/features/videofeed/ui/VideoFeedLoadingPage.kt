package com.kickstarter.features.videofeed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlinx.coroutines.launch

/**
 * Full-screen loading page rendered as the trailing page of the feed while the next page is
 * being fetched. A centered spinner over the video background keeps the look consistent with the
 * player while the user waits for more videos to load.
 */
@Composable
fun VideoFeedLoadingPage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KSTheme.colors.videoPlayer.background)
            .testTag(VideoFeedScreenTestTag.VIDEO_FEED_LOADING_PAGE.name),
        contentAlignment = Alignment.Center
    ) {
        KSCircularProgressIndicator(color = KSTheme.colors.videoPlayer.content)
    }
}

@Preview
@Composable
fun VideoFeedLoadingPagePreview() {
    KSTheme {
        VideoFeedLoadingPage()
    }
}

@Composable
fun setUpVideoFeedErrorActions(snackbarHostState: SnackbarHostState): (String?) -> Unit {
    val scope = rememberCoroutineScope()
    val defaultErrorMessage = stringResource(R.string.Something_went_wrong_please_try_again)
    return { message: String? ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message ?: defaultErrorMessage,
                actionLabel = KSSnackbarTypes.KS_ERROR.name,
                duration = SnackbarDuration.Long
            )
        }
    }
}

