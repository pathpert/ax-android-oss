package com.kickstarter.ui.activities.compose.projectpage.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.acceptedCardType
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.getCardTypeDrawable
import com.kickstarter.models.extensions.isFromPaymentSheet
import com.kickstarter.ui.compose.designsystem.KSRadioButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Activity displaying and coordinating user interactions for UpdateSelectedCardIfNewCardAdded.
 *
 * Negative constraints:
 * - Do not execute network calls or business logic directly within Activity; delegate to ViewModel.
 */
@Composable
fun UpdateSelectedCardIfNewCardAdded(
    index: MutableState<Int>,
    storedCards: List<StoredCard>,
    onOptionSelected: (StoredCard?) -> Unit
) {
    if (index.value != storedCards.size && storedCards.isNotEmpty()) {
        onOptionSelected(storedCards.first())
        index.value = storedCards.size
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSCardElementPreview() {
    KSTheme {
        KSCardElement(
            StoredCardFactory.visa(), Environment.builder().build().ksString(), true
        )
    }
}

@Composable
fun KSCardElement(card: StoredCard, ksString: KSString?, isAvailable: Boolean) {
    val sdf = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

    val expirationString = ksString?.let {
        card.expiration()?.let { expiration ->
            ksString.format(
                stringResource(id = R.string.Credit_card_expiration),
                "expiration_date", sdf.format(expiration).toString()
            )
        }
    }

    val lastFourString = ksString?.let {
        card.lastFourDigits()?.let { lastFour ->
            ksString.format(
                stringResource(id = R.string.payment_method_last_four),
                "last_four", lastFour
            )
        }
    }

    Row {
        Image(
            modifier = Modifier
                .padding(end = dimensions.paddingMediumSmall)
                .align(Alignment.CenterVertically)
                .width(dimensions.storedCardImageWidth)
                .height(dimensions.storedCardImageHeight),
            contentDescription = card.type()?.let { StoredCard.issuer(it) },
            alpha = if (isAvailable) 1.0f else .5f,
            painter = painterResource(id = card.getCardTypeDrawable()),
        )

        Column(
            modifier = Modifier.align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.Center
        ) {
            if (!lastFourString.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.padding(end = dimensions.paddingMediumLarge),
                    color = if (isAvailable) colors.kds_support_700 else colors.kds_support_400,
                    style = typographyV2.bodyBoldMD,
                    text = lastFourString
                )
            }

            if (!expirationString.isNullOrEmpty()) {
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        top = dimensions.paddingXSmall,
                        end = dimensions.paddingMediumLarge
                    ),
                    style = typographyV2.headingXS,
                    color = if (isAvailable) colors.kds_support_700 else colors.kds_support_400,
                    text = expirationString
                )
            }
        }
    }
}

@Composable
fun CheckoutPaymentMethodSelector(
    storedCards: List<StoredCard>,
    selectedCard: StoredCard?,
    project: Project,
    ksString: KSString?,
    onOptionSelected: (StoredCard?) -> Unit,
    newPaymentMethodClicked: () -> Unit
) {
    Text(
        modifier = Modifier.padding(start = dimensions.paddingMediumLarge, end = dimensions.paddingMediumLarge),
        text = stringResource(id = R.string.Payment),
        style = typographyV2.headLine,
        color = colors.kds_black,
    )

    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

    storedCards.forEachIndexed { index, card ->
        val isAvailable = project.acceptedCardType(card.type()) || card.isFromPaymentSheet()
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colors.backgroundSurfacePrimary,
                contentColor = colors.backgroundSurfaceSecondary
            ),
            modifier = Modifier
                .padding(start = dimensions.paddingMedium, end = dimensions.paddingMedium)
                .fillMaxWidth()
                .selectableGroup()
                .selectable(
                    enabled = isAvailable,
                    selected = if (index == 0) true else false,
                    onClick = { onOptionSelected(card) }
                )
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = dimensions.paddingSmall, bottom = dimensions.paddingSmall)
                ) {
                    KSRadioButton(
                        selected = card == selectedCard,
                        onClick = { onOptionSelected(card) },
                        enabled = isAvailable
                    )
                    KSCardElement(card = card, ksString = ksString, isAvailable = isAvailable)
                }

                if (!isAvailable) {
                    Text(
                        modifier = Modifier.padding(
                            start = dimensions.paddingDoubleLarge,
                            end = dimensions.paddingMediumLarge,
                            bottom = dimensions.paddingSmall
                        ),
                        style = typographyV2.headingSM,
                        color = colors.kds_alert,
                        text = stringResource(id = R.string.This_project_has_a_set_currency_that_cant_process_this_option)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colors.backgroundSurfacePrimary,
            contentColor = colors.backgroundSurfaceSecondary
        ),
        modifier = Modifier
            .padding(start = dimensions.paddingMedium, end = dimensions.paddingMedium)
            .clickable { newPaymentMethodClicked.invoke() }
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensions.paddingMedium, bottom = dimensions.paddingMedium)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_rounded),
                contentDescription = "",
                tint = colors.textAccentGreen,
                modifier = Modifier.background(
                    color = colors.kds_create_700.copy(alpha = 0.2f),
                    CircleShape
                )
            )

            Text(
                modifier = Modifier.padding(start = dimensions.paddingSmall),
                color = colors.textAccentGreen,
                style = typographyV2.subHeadlineMedium,
                text = stringResource(id = R.string.New_payment_method)
            )
        }
    }
}
