import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.libs.KSCurrency
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.PaymentIncrementFactory
import com.kickstarter.ui.compose.designsystem.KSTheme

@Preview(
    name = "Dark Collapsed State", uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Light Collapsed State", uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
fun PreviewCollapsedPaymentScheduleWhite() {
    KSTheme {
        PaymentSchedule(
            isExpanded = false,
            onExpandChange = {}
        )
    }
}

private fun getMockKSCurrencyForUS(): KSCurrency {
    val config = ConfigFactory.configForUSUser()

    val currentConfig = MockCurrentConfigV2()
    currentConfig.config(config)
    val mockCurrency = KSCurrency(currentConfig)

    return mockCurrency
}

// Expanded State Preview
@Preview(showBackground = true, name = "Expanded State", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewExpandedPaymentScheduleDark() {
    KSTheme {
        PaymentSchedule(
            isExpanded = true,
            onExpandChange = {},
            paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements(),
            ksCurrency = getMockKSCurrencyForUS()
        )
    }
}

// Expanded State Preview
@Preview(showBackground = true, name = "Expanded State")
@Composable
fun PreviewExpandedPaymentSchedule() {
    KSTheme {
        PaymentSchedule(
            isExpanded = true,
            onExpandChange = {},
            paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements(),
            ksCurrency = getMockKSCurrencyForUS()
        )
    }
}

@Preview
@Composable
fun InteractivePaymentSchedulePreview() {
    var isExpanded by remember { mutableStateOf(false) }
    KSTheme {
        PaymentSchedule(
            isExpanded = isExpanded,
            onExpandChange = { isExpanded = it },
            paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements(),
            ksCurrency = getMockKSCurrencyForUS()
        )
    }
}

