package com.kickstarter.ui.activities.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme

/**
 * Activity displaying and coordinating user interactions for PreviewTextLinks.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
@Preview(
    showBackground = true,
    backgroundColor = 0xFFF0EAE2,
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    showBackground = true,
    backgroundColor = 0X00000000,
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun PreviewTextLinks() {
    KSTheme {
        TextWithClickableLink(html = stringResource(id = R.string.Projects_may_not_offer))
    }
}

@Composable
@Preview(
    showBackground = true,
    backgroundColor = 0xFFF0EAE2,
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    showBackground = true,
    backgroundColor = 0X00000000,
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun ReportProjectScreenPreview() {
    KSTheme {
        ReportProjectCategoryScreen(PaddingValues())
    }
}

@Composable
@Preview(
    showBackground = true,
    backgroundColor = 0xFFF0EAE2,
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    showBackground = true,
    backgroundColor = 0X00000000,
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun CategoryRowPreview() {
    KSTheme {
        val list = rulesMap().values.first()
        val list2 = rulesMap().keys.first()
        CategoryRow(category = list2, rulesList = list)
    }
}

@Composable
@Preview(
    showBackground = true,
    backgroundColor = 0xFFF0EAE2,
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    showBackground = true,
    backgroundColor = 0X00000000,
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
fun RulesListPreview() {
    KSTheme {
        val list = rulesMap().values.first()
        RulesList(rulesList = list)
    }
}
