package com.kickstarter.features.paymentmethods.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.mock.factories.StoredCardFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentMethodBoundaryMapperTest {

    @Test
    fun `fromLegacy maps valid stored card into PaymentMethodCard`() {
        val legacy = StoredCardFactory.visa()

        val result = PaymentMethodBoundaryMapper.fromLegacy(legacy)

        assertTrue(result is DomainResult.Success)
        val card = (result as DomainResult.Success).value
        assertEquals(legacy.id(), card.id.value)
        assertEquals("4321", card.lastFour)
        assertEquals("visa", card.issuer)
        assertTrue(card.issuerDrawableRes > 0)
        assertTrue(card.expirationFormatted.isNotBlank())
    }

    @Test
    fun `fromLegacy rejects stored card with blank id`() {
        val invalid = StoredCardFactory.visa().toBuilder().id("   ").build()

        val result = PaymentMethodBoundaryMapper.fromLegacy(invalid)

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertFalse(problem.isRetriable)
        assertEquals("storedCard.id", problem.invalidParams.single().name)
    }
}
