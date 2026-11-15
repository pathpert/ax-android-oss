package com.kickstarter.features.pledgedprojectsoverview.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.data.Flag
import com.kickstarter.libs.utils.extensions.format
import com.kickstarter.ui.compose.designsystem.KSAlertBadge
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSGreenBadge
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.KSWarningBadge
import com.kickstarter.ui.compose.designsystem.shapes

/**
 * Feature component for ProjectPledgeSummaryView in pledgedprojectsoverview module.
 *
 * Negative constraints:
 * - Do not bypass feature-scoped boundary contracts or leak internal UI state.
 */
@Composable
fun ProjectPledgeSummaryView(
    projectName: String? = null,
    pledgeAmount: String? = null,
    imageUrl: String? = null,
    imageContentDescription: String? = null,
    onProjectPledgeSummaryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onProjectPledgeSummaryClick.invoke() }
            .fillMaxWidth()
            .padding(all = dimensions.paddingMediumSmall)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = imageContentDescription,
            modifier = Modifier
                .weight(0.25f)
                .height(dimensions.clickableButtonHeight)
                .clip(shapes.small),
            placeholder = ColorPainter(color = colors.backgroundDisabled),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.width(dimensions.paddingSmall))

        Column(
            modifier = Modifier
                .weight(0.75f)
        ) {
            Text(
                text = projectName ?: "",
                color = colors.textPrimary,
                style = typographyV2.footNoteMedium,
                overflow = TextOverflow.Ellipsis,
                minLines = 1,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

            Text(
                text = stringResource(id = R.string.Pledge_amount_pledged).format("pledge_amount", pledgeAmount),
                color = colors.textSecondary,
                style = typographyV2.bodyXS
            )
        }

        Image(
            modifier = Modifier.padding(start = dimensions.paddingSmall),
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_rounded_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = colors.textSecondary)
        )
    }
}

@Composable
fun CreatorNameSendMessageView(
    creatorName: String? = null,
    sendAMessageClickAction: () -> Unit
) {
    KSDividerLineGrey()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimensions.paddingMediumSmall,
                end = dimensions.paddingXSmall
            )
    ) {
        Text(
            text = stringResource(id = R.string.project_menu_created_by),
            color = colors.textSecondary,
            style = typographyV2.bodyXS
        )

        Text(
            modifier = Modifier.weight(1f),
            text = " ${creatorName.orEmpty()}",
            overflow = TextOverflow.Ellipsis,
            color = colors.textSecondary,
            style = typographyV2.headingXS,
            maxLines = 1
        )

        Box(
            modifier = Modifier.width(dimensions.minButtonHeight).height(dimensions.minButtonHeight).clickable { sendAMessageClickAction.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_envelope),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = colors.textSecondary)
            )
        }
    }
    KSDividerLineGrey()
}

@Composable
fun ShippingAddressView(
    shippingAddress: String? = null,
    onEditAddressClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.paddingSmall, start = dimensions.paddingMediumSmall, end = dimensions.paddingXSmall)
            .testTag(PPOCardViewTestTag.SHIPPING_ADDRESS_VIEW.name),
    ) {
        Text(
            text = stringResource(id = R.string.Shipping_address),
            modifier = Modifier
                .weight(0.25f)
                .height(dimensions.clickableButtonHeight)
                .clip(shapes.small),
            color = colors.textPrimary,
            style = typographyV2.headingSM,
        )

        Spacer(modifier = Modifier.width(dimensions.paddingSmall))

        Row(
            modifier = Modifier.weight(0.75f)
        ) {
            if (!shippingAddress.isNullOrEmpty()) {
                Text(
                    text = shippingAddress,
                    color = colors.textPrimary,
                    style = typographyV2.bodySM,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 4,
                    maxLines = 6
                )
            } else {
                Text(
                    modifier = Modifier.weight(10f),
                    text = stringResource(R.string.fpo_the_creator_has_not_collected_your_address_please_contact_them_to_resolve_the_issue),
                    color = colors.textSecondary,
                    style = typographyV2.headingSM,
                )
            }

            Spacer(modifier = Modifier.width(dimensions.paddingSmall).weight(1f))
            Box(
                modifier = Modifier.width(dimensions.minButtonHeight).height(dimensions.minButtonHeight).clickable { onEditAddressClicked.invoke() }
            ) {
                Image(
                    modifier = Modifier.padding(start = dimensions.paddingSmall),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_pencil),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = colors.textSecondary)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertFlagsView(flags: List<Flag?>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(PPOCardViewTestTag.FlAG_LIST_VIEW.name)
            .padding(
                top = dimensions.paddingMediumSmall,
                start = dimensions.paddingMediumSmall,
                end = dimensions.paddingMediumSmall
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        flags.forEach {
            val icon =
                when (it?.icon) {
                    "alert" -> ImageVector.vectorResource(id = R.drawable.ic_alert)
                    "time" -> ImageVector.vectorResource(id = R.drawable.ic_clock)
                    else -> null
                }

            when (it?.type) {
                "alert" -> KSAlertBadge(icon = icon, message = it.message)
                "warning" -> KSWarningBadge(icon = icon, message = it.message)
                "info" -> it.message?.let { message -> KSGreenBadge(text = message) }
                else -> {}
            }
        }
    }
}

