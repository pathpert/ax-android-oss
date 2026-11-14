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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.models.FlaggingOption
import com.kickstarter.models.childrenOf
import com.kickstarter.models.roots
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * The API-driven report-project screen.
 * Renders the flat [FlaggingOption] list as a recursive, nested accordion:
 * GROUP nodes expand/collapse their children in place;
 * OPTION leaves invoke [onOptionSelected] (→ formulary).
 * Real `<a href>` links in titles/subtitles open via [onOpenUrl].
 */
@Composable
fun ReportProjectFlaggingScreen(
    padding: PaddingValues,
    options: List<FlaggingOption>,
    onOptionSelected: (FlaggingOption) -> Unit = {},
    onOpenUrl: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .systemBarsPadding()
            .animateContentSize()
            .padding(padding)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.kds_white)
        ) {
            items(items = options.roots()) { root ->
                FlaggingNodeRow(
                    node = root,
                    allOptions = options,
                    depth = 0,
                    onOptionSelected = onOptionSelected,
                    onOpenUrl = onOpenUrl
                )
            }
        }
    }
}

/**
 * A single node of the flagging tree.
 * GROUP → an expandable header that renders its children recursively (indentation grows with [depth]);
 * OPTION → a selectable leaf that navigates to the formulary.
 */
@Composable
fun FlaggingNodeRow(
    node: FlaggingOption,
    allOptions: List<FlaggingOption>,
    depth: Int,
    onOptionSelected: (FlaggingOption) -> Unit = {},
    onOpenUrl: (String) -> Unit = {}
) {
    val startIndent = dimensionResource(id = R.dimen.grid_3) + dimensionResource(id = R.dimen.grid_2) * depth

    if (node.isGroup) {
        val expanded = remember { mutableStateOf(false) }
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                verticalAlignment = if (node.subtitle.isNullOrEmpty()) {
                    Alignment.CenterVertically
                } else {
                    Alignment.Top
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded.value = !expanded.value }
                    .padding(
                        start = startIndent,
                        top = dimensionResource(id = R.dimen.grid_2)
                    )
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    HtmlLinkText(
                        html = node.title,
                        style = if (depth == 0) {
                            typographyV2.headLine.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.kds_support_700
                            )
                        } else {
                            typographyV2.subHeadline.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.kds_support_700
                            )
                        },
                        onOpenUrl = onOpenUrl
                    )
                    node.subtitle?.let {
                        HtmlLinkText(
                            html = it,
                            style = typographyV2.body.copy(color = colors.kds_support_700),
                            onOpenUrl = onOpenUrl
                        )
                    }
                }
                IconButton(onClick = { expanded.value = !expanded.value }) {
                    Icon(
                        painter = painterResource(
                            id = if (expanded.value) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
                        ),
                        contentDescription = null,
                        tint = colors.kds_support_700
                    )
                }
            }

            Divider()

            if (expanded.value) {
                allOptions.childrenOf(node.id).forEach { child ->
                    FlaggingNodeRow(
                        node = child,
                        allOptions = allOptions,
                        depth = depth + 1,
                        onOptionSelected = onOptionSelected,
                        onOpenUrl = onOpenUrl
                    )
                }
            }
        }
    } else {
        Column {
            Row(
                verticalAlignment = if (node.subtitle.isNullOrEmpty()) {
                    Alignment.CenterVertically
                } else {
                    Alignment.Top
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(node) }
                    .padding(
                        start = startIndent,
                        top = dimensionResource(id = R.dimen.grid_1),
                        bottom = dimensionResource(id = R.dimen.grid_1)
                    )
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    HtmlLinkText(
                        html = node.title,
                        style = typographyV2.subHeadline.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.kds_support_700
                        ),
                        onOpenUrl = onOpenUrl
                    )
                    node.subtitle?.let {
                        HtmlLinkText(
                            html = it,
                            style = typographyV2.bodyMD.copy(color = colors.kds_support_700),
                            onOpenUrl = onOpenUrl
                        )
                    }
                }
                IconButton(onClick = { onOptionSelected(node) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = colors.kds_support_700
                    )
                }
            }
            Divider()
        }
    }
}

@Composable
private fun Divider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.grid_2),
                vertical = dimensionResource(id = R.dimen.grid_2)
            )
            .background(color = colors.kds_support_300)
            .height(1.dp)
    )
}

private val ANCHOR_REGEX = Regex("<a href=\"([^\"]*)\">(.*?)</a>")
private val HTML_TAG_REGEX = Regex("<[^>]*>")

private fun String.stripHtmlTags(): String = replace(HTML_TAG_REGEX, "")

/**
 * Renders a server-provided string that may contain real `<a href="url">label</a>` anchors.
 */
@Composable
fun HtmlLinkText(
    html: String,
    style: TextStyle,
    onOpenUrl: (String) -> Unit = {}
) {
    val annotatedText = buildAnnotatedString {
        var lastIndex = 0
        for (match in ANCHOR_REGEX.findAll(html)) {
            append(html.substring(lastIndex, match.range.first).stripHtmlTags())
            val url = match.groupValues[1]
            val label = match.groupValues[2].stripHtmlTags()
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(
                style = SpanStyle(
                    color = colors.kds_create_700,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(label)
            }
            pop()
            lastIndex = match.range.last + 1
        }
        append(html.substring(lastIndex).stripHtmlTags())
    }

    ClickableText(
        text = annotatedText,
        style = style,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { onOpenUrl(it.item) }
        }
    )
}
