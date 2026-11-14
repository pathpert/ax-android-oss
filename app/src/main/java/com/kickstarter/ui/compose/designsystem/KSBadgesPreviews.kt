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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSBadgesPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .fillMaxWidth()
                .padding(dimensions.paddingSmall)
        ) {
            KSGreenBadge(text = "Add-ons available")
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSCoralBadge(text = "3 days left")
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSAlertBadge(
                icon = ImageVector.vectorResource(id = R.drawable.ic_alert),
                message = "Payment Failed"
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSWarningBadge(
                icon = ImageVector.vectorResource(id = R.drawable.ic_clock),
                message = "Address locks in 7 days"
            )
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSBetaBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSCountBadge(4)
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSSecretRewardBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSFeaturedRewardBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSVideoBadge(text = "Just launched")
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSSubtleWarningBadge(text = "19 left of 1,000")
        }
    }
}

@Composable
@Preview(name = "Video Badges", backgroundColor = 0xFF000000, showBackground = true)
fun KSVideoBadgesPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(dimensions.paddingSmall)
        ) {
            KSProjectWeLoveVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSDaysLeftVideoBadge(text = "3 days left")
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSJustLaunchedVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSFirstTimeCreatorVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSSuperbackerVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSNearYouVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSNSFWVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSAlmostThereVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSTrendingVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSPopularVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSHotVideoBadge()
        }
    }
}
