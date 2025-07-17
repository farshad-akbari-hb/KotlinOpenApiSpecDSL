package me.farshad.dsl.builder.info

import me.farshad.dsl.builder.info.ContactBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContactBuilderTest {
    @Test
    fun testEmptyContact() {
        val contactBuilder = ContactBuilder()
        val contact = contactBuilder.build()

        assertNull(contact.name)
        assertNull(contact.url)
        assertNull(contact.email)
    }

    @Test
    fun testContactWithNameOnly() {
        val contactBuilder = ContactBuilder()
        contactBuilder.name = "API Support Team"

        val contact = contactBuilder.build()

        assertEquals("API Support Team", contact.name)
        assertNull(contact.url)
        assertNull(contact.email)
    }

    @Test
    fun testContactWithEmailOnly() {
        val contactBuilder = ContactBuilder()
        contactBuilder.email = "support@example.com"

        val contact = contactBuilder.build()

        assertNull(contact.name)
        assertNull(contact.url)
        assertEquals("support@example.com", contact.email)
    }

    @Test
    fun testContactWithUrlOnly() {
        val contactBuilder = ContactBuilder()
        contactBuilder.url = "https://support.example.com"

        val contact = contactBuilder.build()

        assertNull(contact.name)
        assertEquals("https://support.example.com", contact.url)
        assertNull(contact.email)
    }

    @Test
    fun testCompleteContact() {
        val contactBuilder = ContactBuilder()
        contactBuilder.name = "John Doe"
        contactBuilder.url = "https://johndoe.com"
        contactBuilder.email = "john@example.com"

        val contact = contactBuilder.build()

        assertEquals("John Doe", contact.name)
        assertEquals("https://johndoe.com", contact.url)
        assertEquals("john@example.com", contact.email)
    }

    @Test
    fun testContactBuilderChaining() {
        val contact =
            ContactBuilder()
                .apply {
                    name = "Support"
                    url = "https://support.com"
                    email = "help@support.com"
                }.build()

        assertEquals("Support", contact.name)
        assertEquals("https://support.com", contact.url)
        assertEquals("help@support.com", contact.email)
    }

    @Test
    fun testContactWithEmptyStrings() {
        val contactBuilder = ContactBuilder()
        contactBuilder.name = ""
        contactBuilder.url = ""
        contactBuilder.email = ""

        val contact = contactBuilder.build()

        assertEquals("", contact.name)
        assertEquals("", contact.url)
        assertEquals("", contact.email)
    }

    @Test
    fun testContactWithSpecialCharacters() {
        val contactBuilder = ContactBuilder()
        contactBuilder.name = "Support & Sales Team"
        contactBuilder.url = "https://example.com/support?team=sales&region=us"
        contactBuilder.email = "support+sales@example.com"

        val contact = contactBuilder.build()

        assertEquals("Support & Sales Team", contact.name)
        assertEquals("https://example.com/support?team=sales&region=us", contact.url)
        assertEquals("support+sales@example.com", contact.email)
    }

    @Test
    fun testContactWithInternationalCharacters() {
        val contactBuilder = ContactBuilder()
        contactBuilder.name = "José García"
        contactBuilder.email = "josé@ejemplo.es"

        val contact = contactBuilder.build()

        assertEquals("José García", contact.name)
        assertEquals("josé@ejemplo.es", contact.email)
    }

    @Test
    fun testContactWithLongValues() {
        val longName = "A".repeat(100)
        val longUrl = "https://example.com/" + "path/".repeat(50)
        val longEmail = "a".repeat(50) + "@example.com"

        val contactBuilder = ContactBuilder()
        contactBuilder.name = longName
        contactBuilder.url = longUrl
        contactBuilder.email = longEmail

        val contact = contactBuilder.build()

        assertEquals(longName, contact.name)
        assertEquals(longUrl, contact.url)
        assertEquals(longEmail, contact.email)
    }

    @Test
    fun testContactPropertyOverwrite() {
        val contactBuilder = ContactBuilder()
        contactBuilder.name = "First Name"
        contactBuilder.name = "Second Name"
        contactBuilder.email = "first@example.com"
        contactBuilder.email = "second@example.com"

        val contact = contactBuilder.build()

        assertEquals("Second Name", contact.name)
        assertEquals("second@example.com", contact.email)
    }
}
