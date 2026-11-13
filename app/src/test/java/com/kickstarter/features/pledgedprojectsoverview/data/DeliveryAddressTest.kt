package com.kickstarter.features.pledgedprojectsoverview.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryAddressTest {

    @Test
    fun `builder creates DeliveryAddress with full fields and formats string correctly`() {
        val address = DeliveryAddress.builder()
            .addressId("addr-123")
            .recipientName("Ada Lovelace")
            .addressLine1("123 Analytical Way")
            .addressLine2("Apt 4B")
            .city("London")
            .region("Greater London")
            .postalCode("SW1A 1AA")
            .countryCode("GB")
            .phoneNumber("+442079460912")
            .build()

        assertEquals("addr-123", address.addressId())
        assertEquals("Ada Lovelace", address.recipientName())
        assertEquals("123 Analytical Way", address.addressLine1())
        assertEquals("Apt 4B", address.addressLine2())
        assertEquals("London", address.city())
        assertEquals("Greater London", address.region())
        assertEquals("SW1A 1AA", address.postalCode())
        assertEquals("GB", address.countryCode())
        assertEquals("+442079460912", address.phoneNumber())

        val formatted = address.getFormattedAddress()
        assertTrue(formatted.contains("Ada Lovelace"))
        assertTrue(formatted.contains("123 Analytical Way"))
        assertTrue(formatted.contains("Apt 4B"))
        assertTrue(formatted.contains("London, Greater London SW1A 1AA"))
        assertTrue(formatted.contains("GB"))
        assertTrue(formatted.contains("+442079460912"))
    }

    @Test
    fun `builder creates DeliveryAddress with optional fields omitted`() {
        val address = DeliveryAddress.builder()
            .recipientName("Grace Hopper")
            .addressLine1("456 Navy Blvd")
            .city("Arlington")
            .region("VA")
            .postalCode("22202")
            .build()

        val formatted = address.getFormattedAddress()
        assertTrue(formatted.contains("Grace Hopper"))
        assertTrue(formatted.contains("456 Navy Blvd"))
        assertTrue(formatted.contains("Arlington, VA 22202"))
    }

    @Test
    fun `toBuilder preserves all fields and supports immutable copying`() {
        val original = DeliveryAddress.builder()
            .addressId("1")
            .recipientName("Original")
            .city("Tokyo")
            .countryCode("JP")
            .build()

        val copy = original.toBuilder()
            .recipientName("Modified")
            .build()

        assertEquals("1", copy.addressId())
        assertEquals("Modified", copy.recipientName())
        assertEquals("Tokyo", copy.city())
        assertEquals("JP", copy.countryCode())
        assertNotEquals(original, copy)
    }

    @Test
    fun `equality holds for matching instances`() {
        val addr1 = DeliveryAddress.builder().addressId("10").city("Berlin").build()
        val addr2 = DeliveryAddress.builder().addressId("10").city("Berlin").build()
        val addr3 = DeliveryAddress.builder().addressId("11").city("Berlin").build()

        assertEquals(addr1, addr2)
        assertNotEquals(addr1, addr3)
    }
}
