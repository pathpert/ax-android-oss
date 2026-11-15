package com.kickstarter.ui.activities.compose.projectpage.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.hrefUrlFromTranslation
import com.kickstarter.libs.utils.extensions.stringsFromHtmlTranslation
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.compose.login.LoginToutTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Activity displaying and coordinating user interactions for TermsOfUseClickableText.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun TermsOfUseClickableText(
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onCookiePolicyClicked: () -> Unit
) {
    val formattedText = HtmlCompat.fromHtml(
        stringResource(id = R.string.By_pledging_you_agree_to_Kickstarters_Terms_of_Use_Privacy_Policy_and_Cookie_Policy),
        0
    ).toString()

    val annotatedLinkString = buildAnnotatedString {
        val termsOfUseString = stringResource(id = R.string.login_tout_help_sheet_terms).lowercase()
        val termsOfUseStartIndex = formattedText.indexOf(string = termsOfUseString, ignoreCase = true)
        val termsOfUserEndIndex = termsOfUseStartIndex + termsOfUseString.length

        val privacyPolicyString = stringResource(id = R.string.login_tout_help_sheet_privacy).lowercase()
        val privacyPolicyStartIndex = formattedText.indexOf(string = privacyPolicyString, ignoreCase = true)
        val privacyPolicyEndIndex = privacyPolicyStartIndex + privacyPolicyString.length

        val cookiePolicyString = stringResource(id = R.string.login_tout_help_sheet_cookie).lowercase()
        val cookiePolicyStartIndex = formattedText.indexOf(string = cookiePolicyString, ignoreCase = true)
        val cookiePolicyEndIndex = cookiePolicyStartIndex + cookiePolicyString.length

        append(formattedText)

        if (termsOfUseStartIndex != -1) {
            addStyle(style = SpanStyle(color = colors.textAccentGreen), start = termsOfUseStartIndex, end = termsOfUserEndIndex)
            addStringAnnotation(tag = DisclaimerItems.TERMS.name, annotation = "", start = termsOfUseStartIndex, end = termsOfUserEndIndex)
        }

        if (privacyPolicyStartIndex != -1) {
            addStyle(style = SpanStyle(color = colors.textAccentGreen), start = privacyPolicyStartIndex, end = privacyPolicyEndIndex)
            addStringAnnotation(tag = DisclaimerItems.PRIVACY.name, annotation = "", start = privacyPolicyStartIndex, end = privacyPolicyEndIndex)
        }

        if (cookiePolicyStartIndex != -1) {
            addStyle(style = SpanStyle(color = colors.textAccentGreen), start = cookiePolicyStartIndex, end = cookiePolicyEndIndex)
            addStringAnnotation(tag = DisclaimerItems.COOKIES.name, annotation = "", start = cookiePolicyStartIndex, end = cookiePolicyEndIndex)
        }
    }

    ClickableText(
        modifier = Modifier.testTag(LoginToutTestTag.TOU_PP_COOKIE_DISCLAIMER.name),
        text = annotatedLinkString,
        style = typographyV2.bodyXS.copy(
            color = colors.kds_support_400,
            textAlign = TextAlign.Center
        ),
        onClick = { index ->
            annotatedLinkString.getStringAnnotations(index, index).firstOrNull()?.let { annotation ->
                when (annotation.tag) {
                    DisclaimerItems.TERMS.name -> onTermsOfUseClicked.invoke()
                    DisclaimerItems.PRIVACY.name -> onPrivacyPolicyClicked.invoke()
                    DisclaimerItems.COOKIES.name -> onCookiePolicyClicked.invoke()
                }
            }
        }
    )
}

@Composable
fun TextWithClickableAccountabilityLink(
    padding: Dp,
    html: String,
    onClickCallback: (String?) -> Unit = {}
) {
    val stringList = html.stringsFromHtmlTranslation()
    val annotation = html.hrefUrlFromTranslation()
    if (stringList.size == 3) {
        val annotatedText = buildAnnotatedString {
            append(stringList.first())
            pushStringAnnotation(tag = annotation, annotation = stringList[1])
            withStyle(style = SpanStyle(color = colors.kds_create_700)) {
                append(stringList[1])
            }
            append(stringList.last())
            pop()
        }

        ClickableText(
            modifier = Modifier.padding(bottom = padding),
            text = annotatedText,
            style = TextStyle(
                fontWeight = FontWeight(400),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
                color = colors.kds_support_400
            ),
            onClick = {
                annotatedText.getStringAnnotations(tag = annotation, start = it, end = it)
                    .firstOrNull()?.let { a -> onClickCallback(a.tag) } ?: onClickCallback(null)
            }
        )
    }
}

@Composable
fun CheckoutNotAStoreCard(
    modifier: Modifier = Modifier,
    onAccountabilityLinkClicked: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            bottomStart = dimensions.radiusMediumLarge,
            bottomEnd = dimensions.radiusMediumLarge,
            topStart = dimensions.radiusMediumLarge,
            topEnd = dimensions.radiusMediumLarge
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.kds_support_200,
        )
    ) {
        Row(modifier = Modifier.padding(dimensions.paddingSmall)) {
            Icon(
                modifier = Modifier
                    .padding(start = dimensions.paddingMediumSmall, end = dimensions.paddingLarge)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.ic_not_a_store),
                contentDescription = null,
                tint = colors.textAccentGreen
            )

            Column {
                Text(
                    modifier = Modifier.padding(bottom = dimensions.paddingXSmall, top = dimensions.paddingXSmall),
                    text = stringResource(id = R.string.Kickstarter_is_not_a_store),
                    style = typographyV2.bodyBoldMD,
                    color = colors.kds_support_400
                )
                TextWithClickableAccountabilityLink(
                    padding = dimensions.paddingXSmall,
                    html = stringResource(id = R.string.Its_a_way_to_bring_creative_projects_to_life_Learn_more_about_accountability),
                    onClickCallback = { onAccountabilityLinkClicked.invoke() }
                )
            }
        }
    }
}
