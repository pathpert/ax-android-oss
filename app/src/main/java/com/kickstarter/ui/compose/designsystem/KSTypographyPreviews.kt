package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun KSTextPreviewV2() {
    KSTheme {
        Column(modifier = Modifier.background(KSTheme.colors.kds_white)) {
            Text(text = "heading2XL", style = KSTheme.typographyV2.heading2XL)
            Text(text = "headingXL", style = KSTheme.typographyV2.headingXL)
            Text(text = "headingLG", style = KSTheme.typographyV2.headingLG)
            Text(text = "headingMD", style = KSTheme.typographyV2.headingMD)
            Text(text = "headingSM", style = KSTheme.typographyV2.headingSM)
            Text(text = "headingXS", style = KSTheme.typographyV2.headingXS)
            Text(text = "bodyXL", style = KSTheme.typographyV2.bodyXL)
            Text(text = "bodyBoldXL", style = KSTheme.typographyV2.bodyBoldXL)
            Text(text = "bodyLG", style = KSTheme.typographyV2.bodyLG)
            Text(text = "bodyBoldLG", style = KSTheme.typographyV2.bodyBoldLG)
            Text(text = "bodyMD", style = KSTheme.typographyV2.bodyMD)
            Text(text = "bodyBoldMD", style = KSTheme.typographyV2.bodyBoldMD)
            Text(text = "bodySM", style = KSTheme.typographyV2.bodySM)
            Text(text = "bodyBoldSM", style = KSTheme.typographyV2.bodyBoldSM)
            Text(text = "bodyXS", style = KSTheme.typographyV2.bodyXS)
            Text(text = "bodyBoldXS", style = KSTheme.typographyV2.bodyBoldXS)
            Text(text = "bodyXXS", style = KSTheme.typographyV2.bodyXXS)
            Text(text = "bodyBoldXXS", style = KSTheme.typographyV2.bodyBoldXXS)
            Text(text = "buttonLabel", style = KSTheme.typographyV2.buttonLabel)
            Text(text = "linkLG", style = KSTheme.typographyV2.linkLG)
            Text(text = "linkMD", style = KSTheme.typographyV2.linkMD)
            Text(text = "linkSM", style = KSTheme.typographyV2.linkSM)
            Text(text = "linkXS", style = KSTheme.typographyV2.linkXS)
            // OLD DESIGN SYSTEM
            Text(text = "title1", style = KSTheme.typographyV2.title1)
            Text(text = "title1Bold", style = KSTheme.typographyV2.title1Bold)
            Text(text = "title2", style = KSTheme.typographyV2.title2)
            Text(text = "titleRewardBold", style = KSTheme.typographyV2.titleRewardBold)
            Text(text = "title2Bold", style = KSTheme.typographyV2.title2Bold)
            Text(text = "headLine", style = KSTheme.typographyV2.headLine)
            Text(text = "body", style = KSTheme.typographyV2.body)
            Text(text = "footNote", style = KSTheme.typographyV2.footNote)
            Text(text = "footNoteMedium", style = KSTheme.typographyV2.footNoteMedium)
            Text(text = "subHeadline", style = KSTheme.typographyV2.subHeadline)
            Text(text = "subHeadlineMedium", style = KSTheme.typographyV2.subHeadlineMedium)
        }
    }
}
