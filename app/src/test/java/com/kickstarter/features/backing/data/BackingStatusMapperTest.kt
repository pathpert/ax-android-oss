package com.kickstarter.features.backing.data

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.PaymentSourceFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Backing
import com.stripe.android.model.CardBrand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackingStatusMapperTest : KSRobolectricTestCase() {

    @Test
    fun `test shouldHideShipping when location is null or pickup`() {
        val backingNoLocation = BackingFactory.backing().toBuilder().locationId(null).build()
        assertTrue(BackingStatusMapper.shouldHideShipping(backingNoLocation))

        val localPickupReward = RewardFactory.localReceiptLocation()
        val backingWithPickup = BackingFactory.backing().toBuilder()
            .locationId(123L)
            .reward(localPickupReward)
            .build()
        assertTrue(BackingStatusMapper.shouldHideShipping(backingWithPickup))

        val backingWithShipping = BackingFactory.backing().toBuilder()
            .locationId(123L)
            .reward(RewardFactory.reward())
            .build()
        assertFalse(BackingStatusMapper.shouldHideShipping(backingWithShipping))
    }

    @Test
    fun `test cardIssuer returns expected brands and descriptions`() {
        val googlePaySource = PaymentSourceFactory.googlePay()
        val googlePayResult = BackingStatusMapper.cardIssuer(googlePaySource)
        assertTrue(googlePayResult.isRight())
        assertEquals(R.string.googlepay_button_content_description, googlePayResult.right())

        val applePaySource = PaymentSourceFactory.applePay()
        val applePayResult = BackingStatusMapper.cardIssuer(applePaySource)
        assertTrue(applePayResult.isRight())
        assertEquals(R.string.apple_pay_content_description, applePayResult.right())

        val visaSource = PaymentSourceFactory.visa()
        val visaResult = BackingStatusMapper.cardIssuer(visaSource)
        assertTrue(visaResult.isLeft())
        assertEquals(CardBrand.Visa.code, visaResult.left())
    }

    @Test
    fun `test cardLogo returns correct drawable resources`() {
        val googlePaySource = PaymentSourceFactory.googlePay()
        assertEquals(R.drawable.google_pay_mark, BackingStatusMapper.cardLogo(googlePaySource))

        val applePaySource = PaymentSourceFactory.applePay()
        assertEquals(R.drawable.apple_pay_mark, BackingStatusMapper.cardLogo(applePaySource))

        val bankAccountSource = PaymentSourceFactory.bankAccount()
        assertEquals(R.drawable.credit_card, BackingStatusMapper.cardLogo(bankAccountSource))
    }

    @Test
    fun `test joinProjectDataAndReward fallback hierarchy`() {
        val reward = RewardFactory.reward()
        val backing = BackingFactory.backing().toBuilder().reward(reward).build()
        val project = ProjectFactory.project().toBuilder().backing(backing).build()
        val projectData = ProjectDataFactory.project(project)

        val pair = BackingStatusMapper.joinProjectDataAndReward(projectData)
        assertEquals(reward, pair.second)
    }

    @Test
    fun `test pledgeStatusData for backer canceled and collected states`() {
        val user = UserFactory.user()
        val creator = UserFactory.creator()
        val project = ProjectFactory.project().toBuilder().creator(creator).build()
        val ksCurrency = environment().ksCurrency()!!
        val ffClient = MockFeatureFlagClient()

        val canceledBacking = BackingFactory.backing().toBuilder().status(Backing.STATUS_CANCELED).build()
        val canceledStatus = BackingStatusMapper.pledgeStatusData(
            project, canceledBacking, user, ksCurrency, ffClient, "https://kickstarter.com"
        )
        assertEquals(R.string.You_canceled_your_pledge_for_this_project, canceledStatus.statusStringRes)

        val collectedBacking = BackingFactory.backing().toBuilder().status(Backing.STATUS_COLLECTED).build()
        val collectedStatus = BackingStatusMapper.pledgeStatusData(
            project, collectedBacking, user, ksCurrency, ffClient, "https://kickstarter.com"
        )
        assertEquals(R.string.We_collected_your_pledge_for_this_project, collectedStatus.statusStringRes)
    }
}
