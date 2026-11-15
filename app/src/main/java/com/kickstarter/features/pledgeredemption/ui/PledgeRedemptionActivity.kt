package com.kickstarter.features.pledgeredemption.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.features.pledgeredemption.data.PledgeRedemptionUiState
import com.kickstarter.features.pledgeredemption.viewmodels.PledgeRedemptionViewModel
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.activities.compose.projectpage.CheckoutScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.PledgeReason

/**
 * Activity hosting Jetpack Compose pledge redemption screen.
 *
 * Negative constraints:
 * - Do not place business logic directly in this Activity; delegate to [PledgeRedemptionViewModel].
 */
class PledgeRedemptionActivity : ComponentActivity() {
    private lateinit var viewModelFactory: PledgeRedemptionViewModel.Factory
    private val viewModel: PledgeRedemptionViewModel by viewModels { viewModelFactory }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val env = this.getEnvironment()
        viewModelFactory = PledgeRedemptionViewModel.Factory(env, bundle = intent.extras)
        viewModel.start()

        setContent {
            val darkModeEnabled = env?.let { this.isDarkModeEnabled(env = it) } ?: false
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            KickstarterApp(useDarkTheme = darkModeEnabled) {
                when (val current = state) {
                    is PledgeRedemptionUiState.Loading -> {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is PledgeRedemptionUiState.Error -> {
                        PledgeRedemptionErrorScreen(
                            problem = current.problem,
                            onCloseClicked = { finish() }
                        )
                    }
                    is PledgeRedemptionUiState.Ready -> {
                        if (env == null) {
                            PledgeRedemptionErrorScreen(
                                problem = ProblemDetails(
                                    type = ProblemDetails.TYPE_UNEXPECTED,
                                    title = "Application Environment Unavailable",
                                    detail = "Kickstarter Environment could not be resolved.",
                                    suggestedAction = "Reopen the screen from an initialized Kickstarter session.",
                                    isRetriable = false
                                ),
                                onCloseClicked = { finish() }
                            )
                        } else {
                            val args = current.args
                            val snapshot = current.legacySnapshot
                            CheckoutScreen(
                                rewardsList = args.rewardLines.map { item ->
                                    Pair(item.title, item.formattedAmount)
                                },
                                environment = env,
                                shippingAmount = args.shippingAmount.toLegacyMajorDouble(),
                                selectedReward = snapshot.selectedReward,
                                currentShippingRule = snapshot.shippingRule,
                                totalAmount = args.totalAmount.toLegacyMajorDouble(),
                                totalBonusSupport = args.bonusAmount.toLegacyMajorDouble(),
                                storedCards = emptyList(),
                                project = snapshot.project,
                                email = args.userEmail.orEmpty(),
                                pledgeReason = PledgeReason.PLEDGE,
                                rewardsHaveShippables = true,
                                onPledgeCtaClicked = { _, _ -> },
                                newPaymentMethodClicked = { },
                                onDisclaimerItemClicked = {},
                                onAccountabilityLinkClicked = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PledgeRedemptionErrorScreen(
    problem: ProblemDetails,
    onCloseClicked: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = problem.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = problem.detail,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = problem.suggestedAction,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onCloseClicked) {
                Text(text = "Close")
            }
        }
    }
}
