import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.mock.factories.PaymentIncrementFactory
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.joda.time.DateTime

@Preview(name = "Light Mode - Pledge In Full", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode - Pledge In Full", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPledgeInFull() {
    KSTheme {
        CollectionPlan(
            isEligible = true,
            initialSelectedOption = CollectionOptions.PLEDGE_IN_FULL
        )
    }
}

@Preview(name = "Light Mode - Pledge Over Time", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode - Pledge Over Time", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPledgeOverTime() {
    KSTheme {
        CollectionPlan(
            isEligible = true,
            initialSelectedOption = CollectionOptions.PLEDGE_OVER_TIME,
            pledgeOverTimeShortPitch = "You will be charged over four payments.",
            pledgeOverTimeCollectionPlanChargeExplanation = "First charge occurs when the project ends successfully.",
            paymentIncrements = listOf(
                PaymentIncrementFactory.incrementUsdCollected(DateTime.now(), "150"),
                PaymentIncrementFactory.incrementUsdCollected(DateTime.now().plusWeeks(2), "150"),
                PaymentIncrementFactory.incrementUsdCollected(DateTime.now().plusWeeks(4), "150"),
                PaymentIncrementFactory.incrementUsdCollected(DateTime.now().plusWeeks(6), "150"),
            )
        )
    }
}

@Preview(name = "Light Mode - Not Eligible", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode - Not Eligible", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewNotEligible() {
    KSTheme {
        CollectionPlan(
            isEligible = false,
            initialSelectedOption = CollectionOptions.PLEDGE_OVER_TIME,
            plotMinimum = "$10 minimum to split"
        )
    }
}

