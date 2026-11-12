package com.kickstarter.features.pledgedprojectsoverview.data

import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.type.CurrencyCode
import org.junit.Assert.assertEquals
import org.junit.Test

class PPOCardDataTest {

    @Test
    fun `flag builder correctly populates all fields`() {
        val flag = Flag.builder()
            .icon("alert_triangle")
            .message("Payment Failed")
            .type("warning")
            .build()

        assertEquals("alert_triangle", flag.icon())
        assertEquals("Payment Failed", flag.message())
        assertEquals("warning", flag.type())

        val copy = flag.toBuilder().message("Updated").build()
        assertEquals("Updated", copy.message())
        assertEquals("warning", copy.type())
    }

    @Test
    fun `PPOCard builder correctly sets and exposes all properties`() {
        val flag = Flag.builder().icon("flag").message("Survey needed").build()
        val address = DeliveryAddress.builder().recipientName("Backer").city("NYC").build()

        val card = PPOCard.builder()
            .backingId("backing-456")
            .projectId("project-789")
            .projectName("Next Gen Keyboard")
            .projectSlug("next-gen-keyboard")
            .creatorID("creator-01")
            .creatorName("Acme Studios")
            .amount("$150")
            .currencyCode(CurrencyCode.USD)
            .currencySymbol("$")
            .clientSecret("secret_123")
            .backerCompleted(true)
            .surveyID("survey-99")
            .timeNumberForAction(3)
            .viewType(PPOCardViewType.CONFIRM_ADDRESS)
            .deliveryAddress(address)
            .flags(listOf(flag))
            .backingDetailsUrl("https://kickstarter.com/backing/456")
            .webviewUrl("https://kickstarter.com/survey/99")
            .imageContentDescription("Keyboard image")
            .imageUrl("https://kickstarter.com/img.jpg")
            .build()

        assertEquals("backing-456", card.backingId())
        assertEquals("project-789", card.projectId())
        assertEquals("Next Gen Keyboard", card.projectName())
        assertEquals("next-gen-keyboard", card.projectSlug())
        assertEquals("creator-01", card.creatorID())
        assertEquals("Acme Studios", card.creatorName())
        assertEquals("$150", card.amount())
        assertEquals(CurrencyCode.USD, card.currencyCode())
        assertEquals("$", card.currencySymbol())
        assertEquals("secret_123", card.clientSecret())
        assertEquals(true, card.backerCompleted())
        assertEquals("survey-99", card.surveyID())
        assertEquals(3, card.timeNumberForAction())
        assertEquals(PPOCardViewType.CONFIRM_ADDRESS, card.viewType())
        assertEquals(address, card.deliveryAddress())
        assertEquals(1, card.flags()?.size)
        assertEquals("https://kickstarter.com/backing/456", card.backingDetailsUrl())
        assertEquals("https://kickstarter.com/survey/99", card.webviewUrl())
        assertEquals("Keyboard image", card.imageContentDescription())
        assertEquals("https://kickstarter.com/img.jpg", card.imageUrl())

        val cardCopy = card.toBuilder().timeNumberForAction(5).build()
        assertEquals(5, cardCopy.timeNumberForAction())
        assertEquals("backing-456", cardCopy.backingId())
    }

    @Test
    fun `envelope builder correctly populates page info and cards list`() {
        val card = PPOCard.builder().backingId("b-1").build()
        val pageInfo = PageInfoEnvelope.builder()
            .hasNextPage(true)
            .endCursor("cursor-xyz")
            .build()

        val envelope = PledgedProjectsOverviewEnvelope.builder()
            .totalCount(42)
            .pageInfoEnvelope(pageInfo)
            .pledges(listOf(card))
            .build()

        assertEquals(42, envelope.totalCount())
        assertEquals(true, envelope.pageInfoEnvelope().hasNextPage)
        assertEquals("cursor-xyz", envelope.pageInfoEnvelope().endCursor)
        assertEquals(1, envelope.pledges()?.size)
        assertEquals("b-1", envelope.pledges()?.first()?.backingId())
    }
}
