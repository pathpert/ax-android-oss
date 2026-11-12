package com.kickstarter.features.checkout.data

import com.kickstarter.ax.errors.DomainResult
import com.kickstarter.ax.errors.ProblemDetails
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("DEPRECATION")
class AddOnBoundaryMapperTest {

    @Test
    fun `fromLegacy maps valid AddOnsEnvelope into AddOnsPageContract with minor units`() {
        val addOn = RewardFactory.addOn().toBuilder()
            .id(555L)
            .title("Extra Sticker Pack")
            .pledgeAmount(12.50)
            .isAvailable(true)
            .build()
        val envelope = AddOnsEnvelope(
            addOnsList = listOf(addOn),
            pageInfo = PageInfoEnvelope.builder().endCursor("cursor-2").hasNextPage(true).build()
        )

        val result = AddOnBoundaryMapper.fromLegacy(envelope)

        assertTrue(result is DomainResult.Success)
        val contract = (result as DomainResult.Success).value
        assertEquals(1, contract.items.size)
        assertEquals("555", contract.items.single().id.value)
        assertEquals("Extra Sticker Pack", contract.items.single().title)
        assertEquals(1250L, contract.items.single().pledgeAmount.minorUnits)
        assertTrue(contract.pageInfo.hasNextPage)
        assertEquals("cursor-2", contract.pageInfo.endCursor)
    }

    @Test
    fun `fromLegacy rejects invalid non-positive add-on id`() {
        val invalidAddOn = RewardFactory.addOn().toBuilder().id(0L).build()
        val result = AddOnBoundaryMapper.fromLegacy(AddOnsEnvelope(addOnsList = listOf(invalidAddOn)))

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals(ProblemDetails.TYPE_VALIDATION, problem.type)
        assertFalse(problem.isRetriable)
        assertEquals("addOnsList[0].id", problem.invalidParams.single().name)
    }

    @Test
    fun `fromLegacy rejects negative pledgeAmount`() {
        val invalidAddOn = RewardFactory.addOn().toBuilder()
            .id(123L)
            .pledgeAmount(-5.0)
            .build()
        val result = AddOnBoundaryMapper.fromLegacy(AddOnsEnvelope(addOnsList = listOf(invalidAddOn)))

        assertTrue(result is DomainResult.Failure)
        val problem = (result as DomainResult.Failure).problem
        assertEquals("addOnsList[0].pledgeAmount", problem.invalidParams.single().name)
    }
}
