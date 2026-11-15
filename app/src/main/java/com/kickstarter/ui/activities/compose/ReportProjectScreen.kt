package com.kickstarter.ui.activities.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.hrefUrlFromTranslation
import com.kickstarter.libs.utils.extensions.parseHtmlTag
import com.kickstarter.libs.utils.extensions.stringsFromHtmlTranslation
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.viewmodels.ReportProjectViewModel

/**
 * Activity displaying and coordinating user interactions for Rules.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun Rules(rule: Triple<String, String, String>, navigationAction: (String) -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.grid_1))
            .clickable {
                navigationAction(rule.third)
            }
    ) {
        Column(
            modifier = Modifier
                .padding(PaddingValues(start = dimensionResource(id = R.dimen.grid_4)))
                .weight(1F)
        ) {
            Text(
                text = rule.first.parseHtmlTag(),
                style = typographyV2.subHeadline.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.kds_support_700
                )
            )
            Text(
                text = rule.second.parseHtmlTag(),
                style = typographyV2.bodyMD,
                color = colors.kds_support_700
            )
        }
        IconButton(
            onClick = {
                navigationAction(rule.third)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = colors.kds_support_700
            )
        }
    }
}

@Composable
fun RulesList(
    rulesList: List<Triple<String, String, String>>,
    navigationAction: (String) -> Unit = {}
) {
    Column {
        rulesList.map { rule ->
            Rules(rule = rule, navigationAction)
        }
    }
}

@Composable
fun CategoryRow(
    category: Triple<String, String, Boolean>,
    rulesList: List<Triple<String, String, String>>,
    navigationAction: (String) -> Unit = {},
    inputs: ReportProjectViewModel.Inputs? = null,
) {
    val expanded = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        start = dimensionResource(id = R.dimen.grid_3),
                        top = dimensionResource(id = R.dimen.grid_2)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = category.first,
                    style = typographyV2.headLine.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.kds_support_700
                    )
                )
                if (!category.third) {
                    val text = category.second.parseHtmlTag()
                    Text(
                        text = text,
                        style = typographyV2.body,
                        color = colors.kds_support_700
                    )
                } else {
                    TextWithClickableLink(
                        html = category.second,
                        onClickCallback = { tag ->
                            tag?.let { inputs?.openExternalBrowser(tag) }
                        }
                    )
                }
            }
            IconButton(
                onClick = {
                    expanded.value = !expanded.value
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (expanded.value) R.drawable.ic_arrow_up
                        else R.drawable.ic_arrow_down,
                    ),
                    contentDescription = null,
                    tint = colors.kds_support_700
                )
            }
        }
        if (expanded.value) {
            RulesList(rulesList = rulesList, navigationAction)
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.grid_2),
                    vertical = dimensionResource(id = R.dimen.grid_2)
                )
                .background(
                    color = colors.kds_support_300
                )
                .height(1.dp)
        )
    }
}

@Composable
fun ReportProjectCategoryScreen(
    padding: PaddingValues,
    navigationAction: (String) -> Unit = {},
    inputs: ReportProjectViewModel.Inputs? = null
) {
    Surface(
        modifier = Modifier
            .systemBarsPadding()
            .animateContentSize()
            .padding(padding)
            .fillMaxSize()
    ) {
        val categories = rulesMap().keys.toList()
        Spacer(modifier = Modifier.padding(padding))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.kds_white)
        ) {
            items(items = categories) { key ->
                rulesMap()[key]?.let { value ->
                    CategoryRow(category = key, rulesList = value, navigationAction, inputs)
                }
            }
        }
    }
}

@Composable
fun TextWithClickableLink(
    html: String,
    onClickCallback: (String?) -> Unit = {}
) {
    val stringList = html.stringsFromHtmlTranslation()
    val annotation = html.hrefUrlFromTranslation()
    if (stringList.size == 3) {
        val annotatedText = buildAnnotatedString {
            append(stringList.first())
            pushStringAnnotation(
                tag = annotation,
                annotation = stringList[1]
            )
            withStyle(
                style = SpanStyle(
                    color = colors.kds_create_700,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(stringList[1])
            }
            append(stringList.last())

            pop()
        }

        ClickableText(
            text = annotatedText,
            style = typographyV2.body.copy(color = colors.kds_support_700),
            onClick = {
                annotatedText.getStringAnnotations(
                    tag = annotation, start = it,
                    end = it
                )
                    .firstOrNull()?.let { annotation ->
                        onClickCallback(annotation.tag)
                    } ?: onClickCallback(null)
            }
        )
    }
}
