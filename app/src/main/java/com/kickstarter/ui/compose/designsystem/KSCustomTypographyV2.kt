package com.kickstarter.ui.compose.designsystem

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.kickstarter.R

val interFontFamily = FontFamily(
    Font(R.font.inter_thin, FontWeight.Thin),
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

private fun interStyle(
    fontWeight: FontWeight,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = 0.sp,
    textDecoration: TextDecoration? = null
) = TextStyle(
    fontFamily = interFontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    color = kds_support_700,
    textDecoration = textDecoration
)

val KSCustomTypographyV2 = KSTypographyV2(
    heading2XL = interStyle(FontWeight.Medium, 24.sp, 28.sp, (-0.8).sp),
    headingXL = interStyle(FontWeight.Medium, 20.sp, 24.sp, (-0.67).sp),
    headingLG = interStyle(FontWeight.Medium, 16.sp, 20.sp, (-0.37).sp),
    headingMD = interStyle(FontWeight.Medium, 14.sp, 16.sp, (-0.32).sp),
    headingSM = interStyle(FontWeight.Medium, 12.sp, 16.sp, (-0.15).sp),
    headingXS = interStyle(FontWeight.Medium, 11.sp, 16.sp, (-0.15).sp),
    bodyXL = interStyle(FontWeight.Normal, 20.sp, 28.sp),
    bodyBoldXL = interStyle(FontWeight.Medium, 20.sp, 28.sp),
    bodyLG = interStyle(FontWeight.Normal, 16.sp, 24.sp),
    bodyBoldLG = interStyle(FontWeight.Medium, 16.sp, 24.sp),
    bodyMD = interStyle(FontWeight.Normal, 14.sp, 20.sp),
    bodyBoldMD = interStyle(FontWeight.Medium, 14.sp, 20.sp),
    bodySM = interStyle(FontWeight.Normal, 12.sp, 16.sp),
    bodyBoldSM = interStyle(FontWeight.Medium, 12.sp, 16.sp),
    bodyXS = interStyle(FontWeight.Normal, 11.sp, 14.sp),
    bodyBoldXS = interStyle(FontWeight.Medium, 11.sp, 14.sp),
    bodyXXS = interStyle(FontWeight.Normal, 10.sp, 13.sp),
    bodyBoldXXS = interStyle(FontWeight.Medium, 10.sp, 13.sp),
    buttonLabel = interStyle(FontWeight.Medium, 16.sp, 20.sp, (-0.4).sp),
    linkLG = interStyle(FontWeight.Medium, 16.sp, 20.sp, (-0.4).sp, TextDecoration.Underline),
    linkMD = interStyle(FontWeight.Medium, 14.sp, 20.sp, (-0.4).sp, TextDecoration.Underline),
    linkSM = interStyle(FontWeight.Medium, 12.sp, 16.sp, textDecoration = TextDecoration.Underline),
    linkXS = interStyle(FontWeight.Medium, 11.sp, 14.sp, textDecoration = TextDecoration.Underline),
    // OLD DESIGN SYSTEM
    title1 = interStyle(FontWeight.Normal, 28.sp, 36.sp),
    title1Bold = interStyle(FontWeight.Medium, 28.sp, 32.sp, (-0.85).sp),
    title2 = interStyle(FontWeight.Normal, 22.sp, 30.sp),
    title2Bold = interStyle(FontWeight.Medium, 22.sp, 26.sp, (-0.75).sp),
    titleRewardBold = interStyle(FontWeight.SemiBold, 24.sp, 28.sp),
    titleBoldSmall = interStyle(FontWeight.SemiBold, 11.sp, 16.sp, (-0.15).sp),
    headLine = interStyle(FontWeight.Medium, 18.sp, 22.sp, (-0.5).sp),
    body = interStyle(FontWeight.Normal, 18.sp, 21.sp),
    footNote = interStyle(FontWeight.Normal, 13.sp, 17.sp),
    footNoteMedium = interStyle(FontWeight.Medium, 13.sp, 17.sp, (-0.23).sp),
    subHeadline = interStyle(FontWeight.Normal, 15.sp, 21.sp),
    subHeadlineMedium = interStyle(FontWeight.Medium, 15.sp, 17.sp, (-0.34).sp)
)
