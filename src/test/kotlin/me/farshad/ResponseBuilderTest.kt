package me.farshad

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.*
import me.farshad.dsl.builder.ResponseBuilder
import me.farshad.dsl.spec.SchemaType
import kotlinx.serialization.Serializable

class ResponseBuilderTest {
    
    @Test
    fun testMinimalResponse() {
        val responseBuilder = ResponseBuilder("Success")
        val response = responseBuilder.build()
        
        assertEquals("Success", response.description)
        assertNull(response.content)
    }
    
    @Test
    fun testResponseWithJsonContentRef() {
        val responseBuilder = ResponseBuilder("User retrieved successfully")
        responseBuilder.jsonContent("User")
        
        val response = responseBuilder.build()
        
        assertEquals("User retrieved successfully", response.description)
        assertNotNull(response.content)
        assertEquals(1, response.content?.size)
        assertNotNull(response.content?.get("application/json"))
        assertEquals("#/components/schemas/User", response.content?.get("application/json")?.schema?.ref)
    }
    
    @Test
    fun testResponseWithJsonContentClass() {
        val responseBuilder = ResponseBuilder("User list")
        responseBuilder.jsonContent(TestUserResponse::class)
        
        val response = responseBuilder.build()
        
        assertEquals("User list", response.description)
        assertNotNull(response.content)
        assertEquals("#/components/schemas/TestUserResponse", response.content?.get("application/json")?.schema?.ref)
    }
    
    @Test
    fun testResponseWithJsonContentClassAndExample() {
        val example = TestUserResponse("123", "John Doe", true)
        val responseBuilder = ResponseBuilder("User details")
        responseBuilder.jsonContent(TestUserResponse::class, example)
        
        val response = responseBuilder.build()
        
        assertNotNull(response.content?.get("application/json")?.example)
        val exampleJson = response.content?.get("application/json")?.example as JsonObject
        assertEquals("123", exampleJson["id"]?.jsonPrimitive?.content)
        assertEquals("John Doe", exampleJson["name"]?.jsonPrimitive?.content)
        assertEquals(true, exampleJson["active"]?.jsonPrimitive?.boolean)
    }
    
    @Test
    fun testResponseWithInlineSchema() {
        val responseBuilder = ResponseBuilder("Custom response")
        responseBuilder.jsonContent {
            type = SchemaType.OBJECT
            property("status", me.farshad.dsl.spec.PropertyType.STRING, true)
            property("message", me.farshad.dsl.spec.PropertyType.STRING, true)
            property("data", me.farshad.dsl.spec.PropertyType.OBJECT, false)
        }
        
        val response = responseBuilder.build()
        
        assertNotNull(response.content)
        val schema = response.content?.get("application/json")?.schema
        assertNull(schema?.ref)
        assertEquals(SchemaType.OBJECT, schema?.type)
        assertEquals(3, schema?.properties?.size)
    }
    
    @Test
    fun testResponseWithJsonContentAndExample() {
        val example = mapOf(
            "status" to "success",
            "data" to mapOf("id" to "123", "name" to "Test")
        )
        val responseBuilder = ResponseBuilder("API response")
        responseBuilder.jsonContent("ApiResponse", example)
        
        val response = responseBuilder.build()
        
        assertNotNull(response.content?.get("application/json")?.example)
        val exampleJson = response.content?.get("application/json")?.example as JsonObject
        assertEquals("success", exampleJson["status"]?.jsonPrimitive?.content)
        
        val dataJson = exampleJson["data"] as JsonObject
        assertEquals("123", dataJson["id"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testResponseAddExampleAfterContent() {
        val responseBuilder = ResponseBuilder("Success response")
        responseBuilder.jsonContent("User")
        responseBuilder.example(mapOf("id" to "789", "name" to "Updated User"))
        
        val response = responseBuilder.build()
        
        assertNotNull(response.content?.get("application/json")?.example)
        val exampleJson = response.content?.get("application/json")?.example as JsonObject
        assertEquals("789", exampleJson["id"]?.jsonPrimitive?.content)
        assertEquals("Updated User", exampleJson["name"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testResponseWithExamples() {
        val responseBuilder = ResponseBuilder("User response with examples")
        responseBuilder.jsonContent("User")
        responseBuilder.examples {
            example("success", mapOf("id" to "1", "name" to "Success User"), "Successful response")
            example("error") {
                summary = "Error response"
                description = "Example of an error"
                value(mapOf("error" to "User not found"))
            }
        }
        
        val response = responseBuilder.build()
        
        assertNotNull(response.content?.get("application/json")?.examples)
        assertEquals(2, response.content?.get("application/json")?.examples?.size)
        assertNull(response.content?.get("application/json")?.example)
        
        val successExample = response.content?.get("application/json")?.examples?.get("success")
        assertNotNull(successExample)
        assertEquals("Successful response", successExample.summary)
        
        val errorExample = response.content?.get("application/json")?.examples?.get("error")
        assertNotNull(errorExample)
        assertEquals("Error response", errorExample.summary)
        assertEquals("Example of an error", errorExample.description)
    }
    
    @Test
    fun testResponseWithoutContent() {
        val responseBuilder = ResponseBuilder("No content")
        // Try to add example without content
        responseBuilder.example(mapOf("ignored" to "value"))
        
        val response = responseBuilder.build()
        
        assertEquals("No content", response.description)
        assertNull(response.content)
    }
    
    @Test
    fun testResponseBuilderChaining() {
        val response = ResponseBuilder("Chained response").apply {
            jsonContent("ChainedData")
            example(mapOf("chained" to true, "value" to 42))
        }.build()
        
        assertEquals("Chained response", response.description)
        assertNotNull(response.content)
        assertNotNull(response.content?.get("application/json")?.example)
    }
    
    @Test
    fun testResponseWithArrayExample() {
        val arrayExample = listOf(
            mapOf("id" to 1, "name" to "First"),
            mapOf("id" to 2, "name" to "Second"),
            mapOf("id" to 3, "name" to "Third")
        )
        
        val responseBuilder = ResponseBuilder("Array response")
        responseBuilder.jsonContent("UserList", arrayExample)
        
        val response = responseBuilder.build()
        
        assertNotNull(response.content?.get("application/json")?.example)
        val exampleJson = response.content?.get("application/json")?.example as JsonArray
        assertEquals(3, exampleJson.size)
        assertEquals(1, (exampleJson[0] as JsonObject)["id"]?.jsonPrimitive?.int)
        assertEquals("Second", (exampleJson[1] as JsonObject)["name"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testResponseWithNestedExample() {
        val nestedExample = mapOf(
            "user" to mapOf(
                "profile" to mapOf(
                    "firstName" to "John",
                    "lastName" to "Doe",
                    "settings" to mapOf(
                        "notifications" to true,
                        "theme" to "dark"
                    )
                ),
                "stats" to mapOf(
                    "posts" to 42,
                    "followers" to 1000
                )
            )
        )
        
        val responseBuilder = ResponseBuilder("Nested response")
        responseBuilder.jsonContent("UserProfile", nestedExample)
        
        val response = responseBuilder.build()
        
        assertNotNull(response.content?.get("application/json")?.example)
        val exampleJson = response.content?.get("application/json")?.example as JsonObject
        val userJson = exampleJson["user"] as JsonObject
        val profileJson = userJson["profile"] as JsonObject
        assertEquals("John", profileJson["firstName"]?.jsonPrimitive?.content)
        
        val settingsJson = profileJson["settings"] as JsonObject
        assertEquals(true, settingsJson["notifications"]?.jsonPrimitive?.boolean)
        assertEquals("dark", settingsJson["theme"]?.jsonPrimitive?.content)
        
        val statsJson = userJson["stats"] as JsonObject
        assertEquals(42, statsJson["posts"]?.jsonPrimitive?.int)
    }
    
    @Test
    fun testResponseWithEmptyDescription() {
        val responseBuilder = ResponseBuilder("")
        responseBuilder.jsonContent("Empty")
        
        val response = responseBuilder.build()
        
        assertEquals("", response.description)
        assertNotNull(response.content)
    }
    
    @Test
    fun testResponseWithSpecialCharactersInDescription() {
        val specialDesc = "Response with special chars: !@#$%^&*() and unicode: 한글 العربية"
        val responseBuilder = ResponseBuilder(specialDesc)
        
        val response = responseBuilder.build()
        
        assertEquals(specialDesc, response.description)
    }
}

@Serializable
data class TestUserResponse(
    val id: String,
    val name: String,
    val active: Boolean
)