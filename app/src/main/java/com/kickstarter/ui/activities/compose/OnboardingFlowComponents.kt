import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Activity displaying and coordinating user interactions for OnboardingProgressBar.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun OnboardingProgressBar(
    currentPage: Int,
    totalPages: Float,
    filteredPages: List<OnboardingPageData>,
    onboardingCancelled: (OnboardingPage) -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (currentPage + 1) / totalPages,
        animationSpec = tween(durationMillis = 1000)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = dimensions.paddingLarge)
            .padding(top = dimensions.paddingDoubleLarge, bottom = dimensions.paddingLarge)
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            color = colors.textAccentGreen,
            trackColor = colors.kds_white,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            modifier = Modifier
                .weight(1f)
                .height(dimensions.paddingSmall)
                .testTag(OnboardingScreenTestTags.PROGRESS_BAR)
        )
        KSIconButton(
            modifier = Modifier
                .testTag(OnboardingScreenTestTags.CLOSE_BUTTON),
            onClick = { onboardingCancelled(filteredPages[currentPage].page) },
            contentDescription = stringResource(R.string.Close),
            imageVector = Icons.Filled.Close
        )
    }
}

@Composable
fun OnboardingPageAnimation(modifier: Modifier, pageData: OnboardingPageData) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AnimatedContent(
            targetState = pageData,
            transitionSpec = {
                (fadeIn() + slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 4 }))
                    .togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "TextTransition"
        ) { targetPage ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = targetPage.title,
                    style = typographyV2.heading2XL,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .testTag(OnboardingScreenTestTags.PAGE_TITLE)
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                Text(
                    text = targetPage.description,
                    style = typographyV2.bodyLG,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    minLines = 4,
                    modifier = Modifier
                        .testTag(OnboardingScreenTestTags.PAGE_DESCRIPTION)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        AnimatedContent(
            targetState = pageData,
            transitionSpec = {
                (fadeIn(tween(2000)) + slideInHorizontally(initialOffsetX = { it })).togetherWith(
                    fadeOut(tween(2000)) + slideOutHorizontally(targetOffsetX = { -it })
                )
            },
            label = "ImageTransition"
        ) { targetPage ->
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(targetPage.animationRes))
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever,
            )
            LottieAnimation(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(OnboardingScreenTestTags.PAGE_ANIMATION),
                composition = composition,
                progress = { progress },
            )
        }
    }
}
