package me.farshad.dsl.builder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import me.farshad.dsl.builder.core.ServerBuilder

class ServerBuilderTest {
    
    @Test
    fun testServerWithUrlOnly() {
        val serverBuilder = ServerBuilder("https://api.example.com")
        val server = serverBuilder.build()
        
        assertEquals("https://api.example.com", server.url)
        assertNull(server.description)
    }
    
    @Test
    fun testServerWithDescription() {
        val serverBuilder = ServerBuilder("https://api.example.com")
        serverBuilder.description = "Production server"
        
        val server = serverBuilder.build()
        
        assertEquals("https://api.example.com", server.url)
        assertEquals("Production server", server.description)
    }
    
    @Test
    fun testServerWithHttpUrl() {
        val serverBuilder = ServerBuilder("http://localhost:8080")
        val server = serverBuilder.build()
        
        assertEquals("http://localhost:8080", server.url)
        assertNull(server.description)
    }
    
    @Test
    fun testServerWithRelativeUrl() {
        val serverBuilder = ServerBuilder("/api/v1")
        val server = serverBuilder.build()
        
        assertEquals("/api/v1", server.url)
        assertNull(server.description)
    }
    
    @Test
    fun testServerWithEmptyDescription() {
        val serverBuilder = ServerBuilder("https://api.example.com")
        serverBuilder.description = ""
        
        val server = serverBuilder.build()
        
        assertEquals("https://api.example.com", server.url)
        assertEquals("", server.description)
    }
    
    @Test
    fun testServerBuilderChaining() {
        val server = ServerBuilder("https://staging.example.com").apply {
            description = "Staging environment"
        }.build()
        
        assertEquals("https://staging.example.com", server.url)
        assertEquals("Staging environment", server.description)
    }
    
    @Test
    fun testServerWithComplexUrl() {
        val complexUrl = "https://api.example.com:8443/v2/base-path"
        val serverBuilder = ServerBuilder(complexUrl)
        serverBuilder.description = "Custom port and base path"
        
        val server = serverBuilder.build()
        
        assertEquals(complexUrl, server.url)
        assertEquals("Custom port and base path", server.description)
    }
    
    @Test
    fun testServerWithQueryParameters() {
        val urlWithParams = "https://api.example.com/path?version=2&region=us-east"
        val serverBuilder = ServerBuilder(urlWithParams)
        
        val server = serverBuilder.build()
        
        assertEquals(urlWithParams, server.url)
    }
    
    @Test
    fun testServerWithSpecialCharactersInDescription() {
        val serverBuilder = ServerBuilder("https://api.example.com")
        serverBuilder.description = "Production server (US East) - High availability & 99.9% uptime"
        
        val server = serverBuilder.build()
        
        assertEquals("Production server (US East) - High availability & 99.9% uptime", server.description)
    }
    
    @Test
    fun testServerWithUnicodeInDescription() {
        val serverBuilder = ServerBuilder("https://api.example.com")
        serverBuilder.description = "서버 설명 - Server Description - وصف الخادم"
        
        val server = serverBuilder.build()
        
        assertEquals("서버 설명 - Server Description - وصف الخادم", server.description)
    }
    
    @Test
    fun testServerWithLongDescription() {
        val longDescription = "A".repeat(500)
        val serverBuilder = ServerBuilder("https://api.example.com")
        serverBuilder.description = longDescription
        
        val server = serverBuilder.build()
        
        assertEquals(longDescription, server.description)
    }
    
    @Test
    fun testServerDescriptionOverwrite() {
        val serverBuilder = ServerBuilder("https://api.example.com")
        serverBuilder.description = "First description"
        serverBuilder.description = "Second description"
        
        val server = serverBuilder.build()
        
        assertEquals("Second description", server.description)
    }
    
    @Test
    fun testServerWithLocalhostVariations() {
        val variations = listOf(
            "http://localhost",
            "http://localhost:3000",
            "http://127.0.0.1:8080",
            "http://[::1]:8080",
            "http://0.0.0.0:9000"
        )
        
        variations.forEach { url ->
            val server = ServerBuilder(url).build()
            assertEquals(url, server.url)
        }
    }
    
    @Test
    fun testServerWithEmptyUrl() {
        val serverBuilder = ServerBuilder("")
        val server = serverBuilder.build()
        
        assertEquals("", server.url)
        assertNull(server.description)
    }
}