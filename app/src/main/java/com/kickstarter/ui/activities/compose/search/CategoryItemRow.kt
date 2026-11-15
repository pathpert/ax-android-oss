package com.kickstarter.ui.activities.compose.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.models.Category
import com.kickstarter.ui.compose.designsystem.KSDimensions
import com.kickstarter.ui.compose.designsystem.KSPillButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * Activity displaying and coordinating user interactions for CategoryItemRowTestTags.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
object CategoryItemRowTestTags {
    const val ROOTCATEGORY_TITLE = "rowcategory_title"
    const val RADIO_BUTTON = "radio_button"
    const val SUBCATEGORY_ROW = "subcategory_row"
    const val ROOTCATEGORY_ROW = "rootcategory_row"
    fun pillTag(category: Category) = "pill_${category.id()}"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryItemRow(
    modifier: Modifier = Modifier,
    category: Category,
    selectedCategory: Category? = null,
    onSelectionChange: (Category) -> Unit,
    subcategories: List<Category> = emptyList()
) {
    val backgroundDisabledColor = colors.backgroundDisabled
    val dimensions: KSDimensions = KSTheme.dimensions

    val isSelected = category.id() == selectedCategory?.id() || category.id() == selectedCategory?.parentId()

    Column(
        modifier = modifier
            .testTag(CategoryItemRowTestTags.RADIO_BUTTON)
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = backgroundDisabledColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = dimensions.dividerThickness.toPx()
                )
            }
            .clickable {
                onSelectionChange(category)
            }
            .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMedium),
    ) {
        Row(
            modifier = Modifier.testTag(CategoryItemRowTestTags.ROOTCATEGORY_ROW),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = modifier
                    .testTag(CategoryItemRowTestTags.ROOTCATEGORY_TITLE)
                    .weight(1f),
                color = colors.textPrimary,
                text = category.name(),
                style = typographyV2.headingLG
            )

            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(unselectedColor = colors.backgroundSelected, selectedColor = colors.backgroundSelected)
            )
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FlowRow(
                modifier = Modifier
                    .testTag(CategoryItemRowTestTags.SUBCATEGORY_ROW)
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KSPillButton(
                    text = stringResource(R.string.Project_status_all),
                    isSelected = selectedCategory?.isRoot == true,
                    modifier = Modifier.testTag(CategoryItemRowTestTags.pillTag(category)),
                    onClick = {
                        onSelectionChange(category)
                    }
                )
                subcategories.map { subcategory ->
                    KSPillButton(
                        text = subcategory.name(),
                        isSelected = selectedCategory?.id() == subcategory.id(),
                        modifier = Modifier.testTag(CategoryItemRowTestTags.pillTag(subcategory)),
                        onClick = {
                            onSelectionChange(subcategory)
                        }
                    )
                }
            }
        }
    }
}

