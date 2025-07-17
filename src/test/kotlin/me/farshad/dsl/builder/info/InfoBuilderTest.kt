package me.farshad.dsl.builder.info

import me.farshad.dsl.builder.info.InfoBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InfoBuilderTest {
    @Test
    fun testMinimalInfo() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "Test API"
        infoBuilder.version = "1.0.0"

        val info = infoBuilder.build()

        assertEquals("Test API", info.title)
        assertEquals("1.0.0", info.version)
        assertNull(info.description)
        assertNull(info.termsOfService)
        assertNull(info.contact)
        assertNull(info.license)
    }

    @Test
    fun testCompleteInfo() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "Complete API"
        infoBuilder.version = "2.0.0"
        infoBuilder.description = "A comprehensive API description"
        infoBuilder.termsOfService = "https://example.com/terms"
        infoBuilder.contact {
            name = "API Support"
            url = "https://support.example.com"
            email = "support@example.com"
        }
        infoBuilder.license("MIT", "https://opensource.org/licenses/MIT")

        val info = infoBuilder.build()

        assertEquals("Complete API", info.title)
        assertEquals("2.0.0", info.version)
        assertEquals("A comprehensive API description", info.description)
        assertEquals("https://example.com/terms", info.termsOfService)

        assertNotNull(info.contact)
        assertEquals("API Support", info.contact?.name)
        assertEquals("https://support.example.com", info.contact?.url)
        assertEquals("support@example.com", info.contact?.email)

        assertNotNull(info.license)
        assertEquals("MIT", info.license?.name)
        assertEquals("https://opensource.org/licenses/MIT", info.license?.url)
    }

    @Test
    fun testInfoWithContactOnly() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "Contact API"
        infoBuilder.version = "1.0.0"
        infoBuilder.contact {
            name = "John Doe"
            email = "john@example.com"
        }

        val info = infoBuilder.build()

        assertNotNull(info.contact)
        assertEquals("John Doe", info.contact?.name)
        assertEquals("john@example.com", info.contact?.email)
        assertNull(info.contact?.url)
        assertNull(info.license)
    }

    @Test
    fun testInfoWithLicenseOnly() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "Licensed API"
        infoBuilder.version = "1.0.0"
        infoBuilder.license("Apache 2.0")

        val info = infoBuilder.build()

        assertNotNull(info.license)
        assertEquals("Apache 2.0", info.license?.name)
        assertNull(info.license?.url)
        assertNull(info.contact)
    }

    @Test
    fun testInfoWithLicenseAndUrl() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "Licensed API"
        infoBuilder.version = "1.0.0"
        infoBuilder.license("GPL-3.0", "https://www.gnu.org/licenses/gpl-3.0.html")

        val info = infoBuilder.build()

        assertNotNull(info.license)
        assertEquals("GPL-3.0", info.license?.name)
        assertEquals("https://www.gnu.org/licenses/gpl-3.0.html", info.license?.url)
    }

    @Test
    fun testInfoWithEmptyDescription() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "API"
        infoBuilder.version = "1.0.0"
        infoBuilder.description = ""

        val info = infoBuilder.build()

        assertEquals("", info.description)
    }

    @Test
    fun testInfoWithEmptyTermsOfService() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "API"
        infoBuilder.version = "1.0.0"
        infoBuilder.termsOfService = ""

        val info = infoBuilder.build()

        assertEquals("", info.termsOfService)
    }

    @Test
    fun testInfoBuilderChaining() {
        val infoBuilder =
            InfoBuilder().apply {
                title = "Chained API"
                version = "1.0.0"
                description = "Built with chaining"
                termsOfService = "https://example.com/terms"
                contact {
                    name = "Support"
                }
                license("MIT")
            }

        val info = infoBuilder.build()

        assertEquals("Chained API", info.title)
        assertEquals("1.0.0", info.version)
        assertEquals("Built with chaining", info.description)
        assertEquals("https://example.com/terms", info.termsOfService)
        assertNotNull(info.contact)
        assertNotNull(info.license)
    }

    @Test
    fun testInfoWithSpecialCharacters() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "API with Special Characters: !@#$%^&*()"
        infoBuilder.version = "1.0.0-beta+build.123"
        infoBuilder.description = "Description with newlines\nand\ttabs"

        val info = infoBuilder.build()

        assertEquals("API with Special Characters: !@#$%^&*()", info.title)
        assertEquals("1.0.0-beta+build.123", info.version)
        assertEquals("Description with newlines\nand\ttabs", info.description)
    }

    @Test
    fun testInfoWithUnicodeCharacters() {
        val infoBuilder = InfoBuilder()
        infoBuilder.title = "API with Unicode: 你好世界 مرحبا بالعالم"
        infoBuilder.version = "1.0.0"
        infoBuilder.description = "Emoji support: 🚀 🎉 ✨"

        val info = infoBuilder.build()

        assertEquals("API with Unicode: 你好世界 مرحبا بالعالم", info.title)
        assertEquals("Emoji support: 🚀 🎉 ✨", info.description)
    }
}
