package com.kickstarter.ui.compose.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/**
 * UI component rendering KSTypographyV2.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Immutable
data class KSTypographyV2(
    val heading2XL: TextStyle = TextStyle.Default,
    val headingXL: TextStyle = TextStyle.Default,
    val headingLG: TextStyle = TextStyle.Default,
    val headingMD: TextStyle = TextStyle.Default,
    val headingSM: TextStyle = TextStyle.Default,
    val headingXS: TextStyle = TextStyle.Default,
    val bodyXL: TextStyle = TextStyle.Default,
    val bodyBoldXL: TextStyle = TextStyle.Default,
    val bodyLG: TextStyle = TextStyle.Default,
    val bodyBoldLG: TextStyle = TextStyle.Default,
    val bodyMD: TextStyle = TextStyle.Default,
    val bodyBoldMD: TextStyle = TextStyle.Default,
    val bodySM: TextStyle = TextStyle.Default,
    val bodyBoldSM: TextStyle = TextStyle.Default,
    val bodyXS: TextStyle = TextStyle.Default,
    val bodyBoldXS: TextStyle = TextStyle.Default,
    val bodyXXS: TextStyle = TextStyle.Default,
    val bodyBoldXXS: TextStyle = TextStyle.Default,
    val buttonLabel: TextStyle = TextStyle.Default,
    val linkLG: TextStyle = TextStyle.Default,
    val linkMD: TextStyle = TextStyle.Default,
    val linkSM: TextStyle = TextStyle.Default,
    val linkXS: TextStyle = TextStyle.Default,
    // OLD DESIGN SYSTEM
    val title1: TextStyle = TextStyle.Default,
    val title1Bold: TextStyle = TextStyle.Default,
    val title2: TextStyle = TextStyle.Default,
    val title2Bold: TextStyle = TextStyle.Default,
    val titleRewardBold: TextStyle = TextStyle.Default,
    val titleBoldSmall: TextStyle = TextStyle.Default,
    val headLine: TextStyle = TextStyle.Default,
    val body: TextStyle = TextStyle.Default,
    val footNote: TextStyle = TextStyle.Default,
    val footNoteMedium: TextStyle = TextStyle.Default,
    val subHeadline: TextStyle = TextStyle.Default,
    val subHeadlineMedium: TextStyle = TextStyle.Default,
)

val LocalKSCustomTypographyV2 = staticCompositionLocalOf {
    KSTypographyV2()
}
