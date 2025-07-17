package me.farshad.dsl.builder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.*
import me.farshad.dsl.builder.request.RequestBodyBuilder
import me.farshad.dsl.spec.SchemaType
import kotlinx.serialization.Serializable

class RequestBodyBuilderTest {
    
    @Test
    fun testMinimalRequestBody() {
        val requestBodyBuilder = RequestBodyBuilder()
        val requestBody = requestBodyBuilder.build()
        
        assertNull(requestBody.description)
        assertEquals(false, requestBody.required)
        assertEquals(0, requestBody.content.size)
    }
    
    @Test
    fun testRequestBodyWithDescription() {
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.description = "User data to create a new user"
        requestBodyBuilder.required = true
        
        val requestBody = requestBodyBuilder.build()
        
        assertEquals("User data to create a new user", requestBody.description)
        assertEquals(true, requestBody.required)
        assertEquals(0, requestBody.content.size)
    }
    
    @Test
    fun testRequestBodyWithJsonContentRef() {
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent("User")
        
        val requestBody = requestBodyBuilder.build()
        
        assertEquals(1, requestBody.content.size)
        assertNotNull(requestBody.content["application/json"])
        assertEquals("#/components/schemas/User", requestBody.content["application/json"]?.schema?.ref)
    }
    
    @Test
    fun testRequestBodyWithJsonContentClass() {
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent(TestUser::class)
        
        val requestBody = requestBodyBuilder.build()
        
        assertEquals(1, requestBody.content.size)
        assertNotNull(requestBody.content["application/json"])
        assertEquals("#/components/schemas/TestUser", requestBody.content["application/json"]?.schema?.ref)
    }
    
    @Test
    fun testRequestBodyWithJsonContentClassAndExample() {
        val example = TestUser("123", "John Doe", "john@example.com")
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent(TestUser::class, example)
        
        val requestBody = requestBodyBuilder.build()
        
        assertEquals(1, requestBody.content.size)
        assertNotNull(requestBody.content["application/json"])
        assertEquals("#/components/schemas/TestUser", requestBody.content["application/json"]?.schema?.ref)
        assertNotNull(requestBody.content["application/json"]?.example)
        
        val exampleJson = requestBody.content["application/json"]?.example as JsonObject
        assertEquals("123", exampleJson["id"]?.jsonPrimitive?.content)
        assertEquals("John Doe", exampleJson["name"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testRequestBodyWithInlineSchema() {
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent {
            type = SchemaType.OBJECT
            property("id", me.farshad.dsl.spec.PropertyType.STRING, true)
            property("name", me.farshad.dsl.spec.PropertyType.STRING, true)
        }
        
        val requestBody = requestBodyBuilder.build()
        
        assertEquals(1, requestBody.content.size)
        assertNotNull(requestBody.content["application/json"])
        assertNull(requestBody.content["application/json"]?.schema?.ref)
        assertEquals(SchemaType.OBJECT, requestBody.content["application/json"]?.schema?.type)
        assertEquals(2, requestBody.content["application/json"]?.schema?.properties?.size)
    }
    
    @Test
    fun testRequestBodyWithJsonContentAndExample() {
        val example = mapOf("id" to "123", "name" to "John")
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent("User", example)
        
        val requestBody = requestBodyBuilder.build()
        
        assertEquals(1, requestBody.content.size)
        assertNotNull(requestBody.content["application/json"]?.example)
        
        val exampleJson = requestBody.content["application/json"]?.example as JsonObject
        assertEquals("123", exampleJson["id"]?.jsonPrimitive?.content)
        assertEquals("John", exampleJson["name"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testRequestBodyAddExampleAfterContent() {
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent("User")
        requestBodyBuilder.example(mapOf("id" to "456", "name" to "Jane"))
        
        val requestBody = requestBodyBuilder.build()
        
        assertNotNull(requestBody.content["application/json"]?.example)
        val exampleJson = requestBody.content["application/json"]?.example as JsonObject
        assertEquals("456", exampleJson["id"]?.jsonPrimitive?.content)
        assertEquals("Jane", exampleJson["name"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testRequestBodyWithExamples() {
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent("User")
        requestBodyBuilder.examples {
            example("valid", mapOf("id" to "123", "name" to "John"), "Valid user", "A valid user example")
            example("minimal") {
                summary = "Minimal user"
                value(mapOf("id" to "456"))
            }
        }
        
        val requestBody = requestBodyBuilder.build()
        
        assertNotNull(requestBody.content["application/json"]?.examples)
        assertEquals(2, requestBody.content["application/json"]?.examples?.size)
        assertNull(requestBody.content["application/json"]?.example) // example should be null when examples is set
        
        val validExample = requestBody.content["application/json"]?.examples?.get("valid")
        assertNotNull(validExample)
        assertEquals("Valid user", validExample.summary)
        assertEquals("A valid user example", validExample.description)
        
        val minimalExample = requestBody.content["application/json"]?.examples?.get("minimal")
        assertNotNull(minimalExample)
        assertEquals("Minimal user", minimalExample.summary)
    }
    
    @Test
    fun testRequestBodyWithoutJsonContent() {
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.description = "Some request body"
        requestBodyBuilder.required = true
        // Try to add example without content - should not crash
        requestBodyBuilder.example(mapOf("test" to "value"))
        
        val requestBody = requestBodyBuilder.build()
        
        assertEquals("Some request body", requestBody.description)
        assertEquals(true, requestBody.required)
        assertEquals(0, requestBody.content.size) // No content added
    }
    
    @Test
    fun testRequestBodyBuilderChaining() {
        val requestBody = RequestBodyBuilder().apply {
            description = "Create user request"
            required = true
            jsonContent("User")
            example(mapOf("id" to "123", "name" to "Test User"))
        }.build()
        
        assertEquals("Create user request", requestBody.description)
        assertEquals(true, requestBody.required)
        assertNotNull(requestBody.content["application/json"])
        assertNotNull(requestBody.content["application/json"]?.example)
    }
    
    @Test
    fun testRequestBodyComplexExample() {
        val complexExample = mapOf(
            "user" to mapOf(
                "id" to 123,
                "name" to "John Doe",
                "tags" to listOf("admin", "user"),
                "active" to true,
                "metadata" to mapOf(
                    "created" to "2024-01-01",
                    "updated" to "2024-01-02"
                )
            )
        )
        
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent("ComplexUser", complexExample)
        
        val requestBody = requestBodyBuilder.build()
        
        assertNotNull(requestBody.content["application/json"]?.example)
        val exampleJson = requestBody.content["application/json"]?.example as JsonObject
        val userJson = exampleJson["user"] as JsonObject
        assertEquals(123, userJson["id"]?.jsonPrimitive?.int)
        assertEquals("John Doe", userJson["name"]?.jsonPrimitive?.content)
        
        val tagsJson = userJson["tags"] as JsonArray
        assertEquals(2, tagsJson.size)
        assertEquals("admin", tagsJson[0].jsonPrimitive.content)
        
        val metadataJson = userJson["metadata"] as JsonObject
        assertEquals("2024-01-01", metadataJson["created"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testRequestBodyWithNullExample() {
        val requestBodyBuilder = RequestBodyBuilder()
        requestBodyBuilder.jsonContent {
            type = SchemaType.OBJECT
        }
        // Set the example directly on the media type
        val mediaType = requestBodyBuilder.build().content["application/json"]
        
        // Create a new request body builder and manually set the null example
        val requestBodyBuilder2 = RequestBodyBuilder()
        requestBodyBuilder2.jsonContent {
            type = SchemaType.OBJECT
        }
        requestBodyBuilder2.example(mapOf<String, Any?>())
        
        val requestBody = requestBodyBuilder2.build()
        
        assertNotNull(requestBody.content["application/json"]?.example)
    }
}

@Serializable
data class TestUser(
    val id: String,
    val name: String,
    val email: String? = null
)