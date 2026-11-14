package com.kickstarter.ui.activities.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.models.FlaggingOption
import com.kickstarter.ui.compose.designsystem.KSTheme

/**
 * Mock data provided for the compose preview
 */
internal fun sampleFlaggingOptions(): List<FlaggingOption> = listOf(
    FlaggingOption(
        id = "project/our_rules",
        parentId = "project",
        kind = null,
        isGroup = true,
        title = "This project breaks one of <a href=\"https://www.kickstarter.com/rules\">Our Rules</a>",
        subtitle = "All projects on Kickstarter must create something to share with others. They can't offer " +
            "<a href=\"https://www.kickstarter.com/rules/prohibited\">prohibited items</a> or offer funds to charity.",
        placeholder = null
    ),
    FlaggingOption(
        id = "project/our_rules/prohibited_items",
        parentId = "project/our_rules",
        kind = null,
        isGroup = true,
        title = "Prohibited items",
        subtitle = "Projects may not offer items mentioned in our " +
            "<a href=\"https://www.kickstarter.com/rules/prohibited\">prohibited items</a> list.",
        placeholder = null
    ),
    FlaggingOption(
        id = "project/our_rules/prohibited_items/vices",
        parentId = "project/our_rules/prohibited_items",
        kind = null,
        isGroup = true,
        title = "Drugs, alcohol or weapons",
        subtitle = null,
        placeholder = null
    ),
    FlaggingOption(
        id = "project/our_rules/prohibited_items/vices/vices_drugs",
        parentId = "project/our_rules/prohibited_items/vices",
        kind = "VICES_DRUGS",
        isGroup = false,
        title = "Drugs, tobacco, vaporizers and paraphernalia",
        subtitle = "Ex: CBD oil, hookah equipment, vape juice, glass pipes",
        placeholder = "Please provide any additional context about your report that could be helpful."
    ),
    FlaggingOption(
        id = "project/our_rules/prohibited_items/vices/vices_alcohol",
        parentId = "project/our_rules/prohibited_items/vices",
        kind = "VICES_ALCOHOL",
        isGroup = false,
        title = "Alcohol as a reward",
        subtitle = "Ex: Bottles of beer, wine or liquor, growler fill-ups",
        placeholder = "Please provide any additional context about your report that could be helpful."
    ),
    FlaggingOption(
        id = "project/our_rules/prohibited_items/health_claims",
        parentId = "project/our_rules/prohibited_items",
        kind = null,
        isGroup = true,
        title = "Health claims or regulated items",
        subtitle = null,
        placeholder = null
    ),
    FlaggingOption(
        id = "project/our_rules/prohibited_items/health_claims/health_claims",
        parentId = "project/our_rules/prohibited_items/health_claims",
        kind = "HEALTH_CLAIMS",
        isGroup = false,
        title = "Claims to cure, treat, or prevent an illness or condition",
        subtitle = "Ex: Claims to treat symptoms of endometriosis, diagnose cancer, cure autism",
        placeholder = "Please provide any additional context about your report that could be helpful."
    ),
    FlaggingOption(
        id = "project/our_rules/resale",
        parentId = "project/our_rules",
        kind = null,
        isGroup = true,
        title = "Copying, reselling or plagiarism",
        subtitle = "Projects cannot plagiarize or offer items that aren't produced by the creator.",
        placeholder = null
    ),
    FlaggingOption(
        id = "project/our_rules/resale/reselling",
        parentId = "project/our_rules/resale",
        kind = "RESELLING",
        isGroup = false,
        title = "This project is reselling or repackaging an existing product.",
        subtitle = null,
        placeholder = "Please provide a URL(s) showing the reward currently available for purchase elsewhere."
    ),
    FlaggingOption(
        id = "project/community_guidelines",
        parentId = "project",
        kind = null,
        isGroup = true,
        title = "Report spam or abusive behavior",
        subtitle = "Our <a href=\"https://www.kickstarter.com/help/community\">Community Guidelines</a> prohibit spam.",
        placeholder = null
    ),
    FlaggingOption(
        id = "project/community_guidelines/guidelines_spam",
        parentId = "project/community_guidelines",
        kind = "GUIDELINES_SPAM",
        isGroup = false,
        title = "Spam",
        subtitle = "Ex: Using email lists from outside sources, link bombing.",
        placeholder = "Please provide additional context about this content or behavior."
    ),
    FlaggingOption(
        id = "project/intellectual_property_violation",
        parentId = "project",
        kind = null,
        isGroup = true,
        title = "Intellectual property violation",
        subtitle = "A project is infringing on your copyright or trademark.",
        placeholder = null
    ),
    FlaggingOption(
        id = "project/intellectual_property_violation/copyright",
        parentId = "project/intellectual_property_violation",
        kind = "COPYRIGHT",
        isGroup = false,
        title = "Kickstarter takes claims of intellectual property infringement seriously. For more information, " +
            "please visit our <a href=\"https://www.kickstarter.com/help/copyright\">Copyright Policy</a> and our " +
            "<a href=\"https://www.kickstarter.com/help/trademark\">Trademark Policy</a>.",
        subtitle = null,
        placeholder = null
    )
)

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
fun ReportProjectFlaggingScreenPreview() {
    KSTheme {
        ReportProjectFlaggingScreen(
            padding = PaddingValues(),
            options = sampleFlaggingOptions()
        )
    }
}
