package me.farshad.dsl.builder.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import me.farshad.dsl.builder.response.ResponseBuilder
import me.farshad.dsl.spec.SchemaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        assertEquals(
            "#/components/schemas/User",
            response.content
                ?.get("application/json")
                ?.schema
                ?.ref,
        )
    }

    @Test
    fun testResponseWithJsonContentClass() {
        val responseBuilder = ResponseBuilder("User list")
        responseBuilder.jsonContent(TestUserResponse::class)

        val response = responseBuilder.build()

        assertEquals("User list", response.description)
        assertNotNull(response.content)
        assertEquals(
            "#/components/schemas/TestUserResponse",
            response.content
                ?.get("application/json")
                ?.schema
                ?.ref,
        )
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
        val example =
            mapOf(
                "status" to "success",
                "data" to mapOf("id" to "123", "name" to "Test"),
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
        assertEquals(
            2,
            response.content
                ?.get("application/json")
                ?.examples
                ?.size,
        )
        assertNull(response.content?.get("application/json")?.example)

        val successExample =
            response.content
                ?.get("application/json")
                ?.examples
                ?.get("success")
        assertNotNull(successExample)
        assertEquals("Successful response", successExample.summary)

        val errorExample =
            response.content
                ?.get("application/json")
                ?.examples
                ?.get("error")
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
        val response =
            ResponseBuilder("Chained response")
                .apply {
                    jsonContent("ChainedData")
                    example(mapOf("chained" to true, "value" to 42))
                }.build()

        assertEquals("Chained response", response.description)
        assertNotNull(response.content)
        assertNotNull(response.content?.get("application/json")?.example)
    }

    @Test
    fun testResponseWithArrayExample() {
        val arrayExample =
            listOf(
                mapOf("id" to 1, "name" to "First"),
                mapOf("id" to 2, "name" to "Second"),
                mapOf("id" to 3, "name" to "Third"),
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
        val nestedExample =
            mapOf(
                "user" to
                    mapOf(
                        "profile" to
                            mapOf(
                                "firstName" to "John",
                                "lastName" to "Doe",
                                "settings" to
                                    mapOf(
                                        "notifications" to true,
                                        "theme" to "dark",
                                    ),
                            ),
                        "stats" to
                            mapOf(
                                "posts" to 42,
                                "followers" to 1000,
                            ),
                    ),
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

    @Test
    fun testResponseWithBasicHeader() {
        val responseBuilder = ResponseBuilder("Success with headers")
        responseBuilder.jsonContent("User")
        responseBuilder.header("X-Rate-Limit", "The rate limit", SchemaType.INTEGER)

        val response = responseBuilder.build()

        assertNotNull(response.headers)
        assertEquals(1, response.headers?.size)
        val header = response.headers?.get("X-Rate-Limit")
        assertNotNull(header)
        assertEquals("The rate limit", header.description)
        assertEquals(SchemaType.INTEGER, header.schema?.type)
    }

    @Test
    fun testResponseWithMultipleHeaders() {
        val responseBuilder = ResponseBuilder("Success")
        responseBuilder.header("X-Rate-Limit", "Rate limit", SchemaType.INTEGER, true)
        responseBuilder.header("X-Rate-Remaining", "Remaining calls", SchemaType.INTEGER)
        responseBuilder.header("X-Request-ID", "Request identifier", SchemaType.STRING)

        val response = responseBuilder.build()

        assertNotNull(response.headers)
        assertEquals(3, response.headers?.size)
        assertTrue(response.headers?.get("X-Rate-Limit")?.required ?: false)
        assertEquals(SchemaType.STRING, response.headers?.get("X-Request-ID")?.schema?.type)
    }

    @Test
    fun testResponseWithHeaderUsingBuilder() {
        val responseBuilder = ResponseBuilder("Success")
        responseBuilder.header("X-Custom-Header", "Custom header") {
            required = true
            deprecated = true
            schema {
                type = SchemaType.STRING
                format = me.farshad.dsl.spec.SchemaFormat.EMAIL
            }
            example("user@example.com")
        }

        val response = responseBuilder.build()

        val header = response.headers?.get("X-Custom-Header")
        assertNotNull(header)
        assertTrue(header.required)
        assertTrue(header.deprecated)
        assertEquals(SchemaType.STRING, header.schema?.type)
        assertEquals(me.farshad.dsl.spec.SchemaFormat.EMAIL, header.schema?.format)
        assertEquals("user@example.com", header.example?.jsonPrimitive?.content)
    }

    @Test
    fun testResponseWithHeaderReference() {
        val responseBuilder = ResponseBuilder("Success")
        responseBuilder.header("X-Api-Version", "ApiVersion", "API version header", true)

        val response = responseBuilder.build()

        val header = response.headers?.get("X-Api-Version")
        assertNotNull(header)
        assertEquals("API version header", header.description)
        assertTrue(header.required)
        assertEquals("#/components/schemas/ApiVersion", header.schema?.ref)
    }

    @Test
    fun testResponseWithHeaderExamples() {
        val responseBuilder = ResponseBuilder("Success")
        responseBuilder.header("X-Custom-Header") {
            description = "Custom header with examples"
            schema {
                type = SchemaType.STRING
            }
            examples {
                example("example1", "value1", "First example")
                example("example2") {
                    summary = "Second example"
                    value("value2")
                }
            }
        }

        val response = responseBuilder.build()

        val header = response.headers?.get("X-Custom-Header")
        assertNotNull(header)
        assertNotNull(header.examples)
        assertEquals(2, header.examples?.size)
        assertNull(header.example)
    }

    @Test
    fun testResponseWithContentAndHeaders() {
        val responseBuilder = ResponseBuilder("Complete response")
        responseBuilder.jsonContent("User")
        responseBuilder.example(mapOf("id" to "123", "name" to "Test User"))
        responseBuilder.header("X-Total-Count", "Total number of items", SchemaType.INTEGER)
        responseBuilder.header("Link", "Pagination links", SchemaType.STRING)

        val response = responseBuilder.build()

        assertNotNull(response.content)
        assertNotNull(response.headers)
        assertEquals(2, response.headers?.size)
        assertNotNull(response.content?.get("application/json")?.example)
    }

    @Test
    fun testResponseWithNoContentButHeaders() {
        val responseBuilder = ResponseBuilder("No content response")
        responseBuilder.header("Location", "Resource location", SchemaType.STRING, true)

        val response = responseBuilder.build()

        assertNull(response.content)
        assertNotNull(response.headers)
        assertEquals(1, response.headers?.size)
    }
}

@Serializable
data class TestUserResponse(
    val id: String,
    val name: String,
    val active: Boolean,
)
