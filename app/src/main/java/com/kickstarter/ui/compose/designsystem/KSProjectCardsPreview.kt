package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.models.Photo
import com.kickstarter.ui.activities.compose.search.CardProjectState
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSProjectCardsPreview() {
    KSTheme {
        KSProjectCardsColumn()
    }
}

@Composable
fun KSProjectCardsColumn() {
    Column(
        Modifier
            .background(color = colors.kds_white)
            .fillMaxWidth()
            .padding(dimensions.paddingSmall)
    ) {
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck",
            state = CardProjectState.LIVE,
            fundingInfoString = "4 days to go • 50% funded",
            fundedPercentage = 50,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.LATE_PLEDGES_ACTIVE,
            fundingInfoString = "Late pledges active",
            fundedPercentage = 100,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.LAUNCHING_SOON,
            fundingInfoString = "Launching soon",
            fundedPercentage = 0,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.ENDED_SUCCESSFUL,
            fundingInfoString = "Ended",
            fundedPercentage = 100,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardLarge(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.ENDED_UNSUCCESSFUL,
            fundingInfoString = "Ended",
            fundedPercentage = 50,
            onClick = {}
        )

        Spacer(modifier = Modifier.height(dimensions.listItemSpacingMedium))

        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck Cat Themed Pawker Deck",
            state = CardProjectState.LIVE,
            fundingInfoString = "4 days to go • 50% funded",
            fundedPercentage = 50,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.LATE_PLEDGES_ACTIVE,
            fundingInfoString = "Late pledges active",
            fundedPercentage = 100,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.LAUNCHING_SOON,
            fundingInfoString = "Launching soon",
            fundedPercentage = 0,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.ENDED_SUCCESSFUL,
            fundingInfoString = "Ended",
            fundedPercentage = 100,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
        KSProjectCardSmall(
            photo = Photo.builder().altText("").full("").build(),
            title = "Cat Themed Pawker Deck",
            state = CardProjectState.ENDED_UNSUCCESSFUL,
            fundingInfoString = "Ended",
            fundedPercentage = 50,
            onClick = {}
        )
    }
}

