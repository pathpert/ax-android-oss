package com.kickstarter.ui.activities.compose.projectpage

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.Coil
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AddOnsScreenPreview() {
    KSTheme {
        Scaffold(
            containerColor = colors.backgroundAccentGraySubtle
        ) { padding ->
            AddOnsScreen(
                modifier = Modifier
                    .padding(padding)
                    .systemBarsPadding(),
                environment = Environment.Builder().build(),
                lazyColumnListState = rememberLazyListState(),
                selectedReward = RewardFactory.reward().toBuilder().minimum(5.0).build(),
                addOns = (0..10).map {
                    Reward.builder()
                        .title("Item Number $it")
                        .description("This is a description for item $it")
                        .id(it.toLong())
                        .quantity(3)
                        .convertedMinimum((100 * (it + 1)).toDouble())
                        .isAvailable(it != 0)
                        .limit(if (it == 0) 1 else 10)
                        .build()
                },
                project =
                Project.builder()
                    .currency("USD")
                    .currentCurrency("USD")
                    .build(),
                onItemAddedOrRemoved = { q, l -> },
                bonusAmountChanged = {},
                onContinueClicked = {},
                addOnCount = 2,
                totalPledgeAmount = 30.0,
                totalBonusSupport = 5.0
            )
        }
    }
}

@Composable
fun AddOnsBottomBar(
    totalAmountString: String,
    addOnCount: Int,
    environment: Environment,
    onContinueClicked: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = dimensions.radiusLarge,
            topEnd = dimensions.radiusLarge
        ),
        color = colors.backgroundSurfacePrimary,
        tonalElevation = dimensions.elevationLarge,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.paddingMediumLarge)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.Total_amount),
                        style = typographyV2.subHeadlineMedium,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = totalAmountString,
                        style = typographyV2.subHeadlineMedium,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                KSPrimaryGreenButton(
                    onClickAction = onContinueClicked,
                    text =
                    if (addOnCount == 0)
                        stringResource(id = R.string.Continue)
                    else {
                        when {
                            addOnCount == 1 -> environment.ksString()?.format(
                                stringResource(R.string.Continue_with_quantity_count_add_ons_one),
                                "quantity_count",
                                addOnCount.toString()
                            ) ?: ""

                            addOnCount > 1 -> environment.ksString()?.format(
                                stringResource(R.string.Continue_with_quantity_count_add_ons_many),
                                "quantity_count",
                                addOnCount.toString()
                            ) ?: ""

                            else -> stringResource(id = R.string.Continue)
                        }
                    },
                    isEnabled = true
                )
            }
        }
    }
}

fun preloadImages(context: Context, rewards: List<Reward>) {
    val rewardsIterator = rewards.iterator()
    while (rewardsIterator.hasNext()) {
        val reward = rewardsIterator.next()
        reward.image()?.let {
            val request = ImageRequest.Builder(context)
                .data(reward.image()?.full())
                .build()
            Coil.imageLoader(context).enqueue(request)
        }
    }
}

