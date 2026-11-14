package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Generic badge designed for use within the Video Player.
 * It features a semi-transparent background with optional glassmorphism effect via [HazeState].
 */
@Composable
fun KSVideoBadge(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    hazeState: HazeState? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState) {
                        blurRadius = 28.dp
                        noiseFactor = 0.05f
                        val baseColor = Color(0xFF2B2B2D).copy(alpha = 0.25f)
                        backgroundColor = baseColor
                        tints = listOf(HazeTint(baseColor))
                    }
                } else {
                    Modifier.background(Color(0xFF2B2B2D).copy(alpha = 0.25f))
                }
            )
            .border(
                width = 1.38.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(6.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = text
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Image(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = dimensions.paddingXSmall)
                        .size(16.dp)
                )
            }
            Text(
                text = text,
                color = Color.White,
                style = typographyV2.headingSM.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun KSProjectWeLoveVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Project_We_Love),
        icon = ProjectWeLove,
        hazeState = hazeState
    )
}

@Composable
fun KSDaysLeftVideoBadge(
    modifier: Modifier = Modifier,
    text: String,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = text,
        icon = Clock,
        hazeState = hazeState
    )
}

@Composable
fun KSJustLaunchedVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Just_launched),
        hazeState = hazeState
    )
}

@Composable
fun KSFirstTimeCreatorVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.First_time_creator),
        hazeState = hazeState
    )
}

@Composable
fun KSSuperbackerVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.By_a_superbacker),
        hazeState = hazeState
    )
}

@Composable
fun KSNearYouVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Near_you),
        hazeState = hazeState
    )
}

@Composable
fun KSNSFWVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = "NSFW",
        hazeState = hazeState
    )
}

@Composable
fun KSAlmostThereVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Almost_there),
        hazeState = hazeState
    )
}

@Composable
fun KSTrendingVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Trending),
        icon = Whatshot,
        hazeState = hazeState
    )
}

@Composable
fun KSPopularVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Popular),
        icon = Whatshot,
        hazeState = hazeState
    )
}

@Composable
fun KSHotVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Hot_right_now),
        icon = Whatshot,
        hazeState = hazeState
    )
}
