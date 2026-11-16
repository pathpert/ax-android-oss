package com.kickstarter.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Visual matrix of all design system typography scales (V1 and V2).
 *
 * Negative constraints:
 * - Internal showcase component only.
 */
@Composable
fun TypographyVisuals() {
    Column {
        Text(text = "Heading2XL", style = typographyV2.heading2XL, color = colors.kds_support_700)
        Text(text = "HeadingXL", style = typographyV2.headingXL, color = colors.kds_support_700)
        Text(text = "HeadingLG", style = typographyV2.headingLG, color = colors.kds_support_700)
        Text(text = "HeadingMD", style = typographyV2.headingMD, color = colors.kds_support_700)
        Text(text = "HeadingSM", style = typographyV2.headingSM, color = colors.kds_support_700)
        Text(text = "HeadingXS", style = typographyV2.headingXS, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Text(text = "BodyXL", style = typographyV2.bodyXL, color = colors.kds_support_700)
        Text(text = "BodyBoldXL", style = typographyV2.bodyBoldXL, color = colors.kds_support_700)
        Text(text = "BodyLG", style = typographyV2.bodyLG, color = colors.kds_support_700)
        Text(text = "BodyBoldLG", style = typographyV2.bodyBoldLG, color = colors.kds_support_700)
        Text(text = "BodyMD", style = typographyV2.bodyMD, color = colors.kds_support_700)
        Text(text = "BodyBoldMD", style = typographyV2.bodyBoldMD, color = colors.kds_support_700)
        Text(text = "BodySM", style = typographyV2.bodySM, color = colors.kds_support_700)
        Text(text = "BodyBoldSM", style = typographyV2.bodyBoldSM, color = colors.kds_support_700)
        Text(text = "BodyXS", style = typographyV2.bodyXS, color = colors.kds_support_700)
        Text(text = "BodyBoldXS", style = typographyV2.bodyBoldXS, color = colors.kds_support_700)
        Text(text = "BodyXXS", style = typographyV2.bodyXXS, color = colors.kds_support_700)
        Text(text = "BodyBoldXXS", style = typographyV2.bodyBoldXXS, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Text(text = "ButtonLabel", style = typographyV2.buttonLabel, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        Text(text = "LinkLG", style = typographyV2.linkLG, color = colors.kds_support_700)
        Text(text = "LinkMD", style = typographyV2.linkMD, color = colors.kds_support_700)
        Text(text = "LinkSM", style = typographyV2.linkSM, color = colors.kds_support_700)
        Text(text = "LinkXS", style = typographyV2.linkXS, color = colors.kds_support_700)

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

        // Legacy V1 Typography
        Text(text = "Title1", style = typographyV2.title1, color = colors.kds_support_700)
        Text(text = "Title1Bold", style = typographyV2.title1Bold, color = colors.kds_support_700)
        Text(text = "Title2", style = typographyV2.title2, color = colors.kds_support_700)
        Text(text = "Title2Bold", style = typographyV2.title2Bold, color = colors.kds_support_700)
        Text(text = "TitleRewardBold", style = typographyV2.titleRewardBold, color = colors.kds_support_700)
        Text(text = "Headline", style = typographyV2.headLine, color = colors.kds_support_700)
        Text(text = "Body", style = typographyV2.body, color = colors.kds_support_700)
        Text(text = "Footnote", style = typographyV2.footNote, color = colors.kds_support_700)
        Text(text = "FootnoteMedium", style = typographyV2.footNoteMedium, color = colors.kds_support_700)
        Text(text = "SubHeadline", style = typographyV2.subHeadline, color = colors.kds_support_700)
        Text(text = "SubHeadlineMedium", style = typographyV2.subHeadlineMedium, color = colors.kds_support_700)
    }
}
