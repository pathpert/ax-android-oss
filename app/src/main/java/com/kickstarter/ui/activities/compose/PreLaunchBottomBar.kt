package com.kickstarter.ui.activities.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_FOLLOWERS
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_SAVE_BUTTON
import com.kickstarter.ui.compose.TextCaptionStyle
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KsButton

/**
 * Activity displaying and coordinating user interactions for PreLaunchBottomBar.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun PreLaunchBottomBar(
    modifier: Modifier = Modifier,
    project: Project?,
    screenPadding: Dp,
    numberOfFollowers: String?,
    onButtonClicked: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = dimensionResource(id = R.dimen.grid_5),
            topEnd = dimensionResource(id = R.dimen.grid_5)
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.kds_white
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.grid_2)
        )
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(
                    vertical = dimensionResource(id = R.dimen.grid_3)
                )
                .fillMaxWidth()
        ) {
            val (button, text) = createRefs()
            KsButton(
                defaultText = stringResource(id = R.string.Notify_me_on_launch),
                pressedText = stringResource(id = R.string.Saved),
                defaultImageVector = ImageVector.vectorResource(id = R.drawable.ic_discovery_heart),
                pressedImageVector = ImageVector.vectorResource(id = R.drawable.icon__heart),
                pressedButtonColor = colors.kds_white,
                pressedTextColor = colors.kds_support_500,
                onClickAction = onButtonClicked,
                isChecked = project?.isStarred() ?: false,
                modifier = Modifier
                    .testTag(PROJECT_SAVE_BUTTON.name)
                    .constrainAs(button) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start, screenPadding)
                        end.linkTo(parent.end, screenPadding)
                    }
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.grid_4)
                    )
                    .padding(top = dimensionResource(id = R.dimen.grid_5_half))
                    .fillMaxWidth()
            )

            numberOfFollowers?.let {
                TextCaptionStyle(
                    text = it,
                    modifier = Modifier
                        .testTag(PROJECT_FOLLOWERS.name)
                        .constrainAs(text) {
                            top.linkTo(button.bottom, margin = 8.dp)
                            start.linkTo(parent.start, screenPadding)
                            end.linkTo(parent.end, screenPadding)
                        }
                        .padding(bottom = dimensionResource(id = R.dimen.grid_3))
                )
            }
        }
    }
}
