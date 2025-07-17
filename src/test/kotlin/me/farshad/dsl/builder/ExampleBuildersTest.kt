package me.farshad.dsl.builder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.*
import me.farshad.dsl.builder.core.ExampleBuilder
import me.farshad.dsl.builder.core.ExamplesBuilder
import kotlinx.serialization.Serializable

class ExampleBuildersTest {
    
    // ExampleBuilder Tests
    @Test
    fun testMinimalExample() {
        val exampleBuilder = ExampleBuilder()
        val example = exampleBuilder.build()
        
        assertNull(example.summary)
        assertNull(example.description)
        assertNull(example.value)
        assertNull(example.externalValue)
    }
    
    @Test
    fun testExampleWithSummaryAndDescription() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.summary = "Example summary"
        exampleBuilder.description = "This is a detailed description of the example"
        
        val example = exampleBuilder.build()
        
        assertEquals("Example summary", example.summary)
        assertEquals("This is a detailed description of the example", example.description)
        assertNull(example.value)
        assertNull(example.externalValue)
    }
    
    @Test
    fun testExampleWithStringValue() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.value("Simple string value")
        
        val example = exampleBuilder.build()
        
        assertNotNull(example.value)
        assertEquals("Simple string value", example.value?.jsonPrimitive?.content)
    }
    
    @Test
    fun testExampleWithNumberValue() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.value(42)
        
        val example = exampleBuilder.build()
        
        assertNotNull(example.value)
        assertEquals(42, example.value?.jsonPrimitive?.int)
    }
    
    @Test
    fun testExampleWithBooleanValue() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.value(true)
        
        val example = exampleBuilder.build()
        
        assertNotNull(example.value)
        assertEquals(true, example.value?.jsonPrimitive?.boolean)
    }
    
    @Test
    fun testExampleWithMapValue() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.value(mapOf(
            "id" to 123,
            "name" to "Test User",
            "active" to true
        ))
        
        val example = exampleBuilder.build()
        
        assertNotNull(example.value)
        val jsonObject = example.value as JsonObject
        assertEquals(123, jsonObject["id"]?.jsonPrimitive?.int)
        assertEquals("Test User", jsonObject["name"]?.jsonPrimitive?.content)
        assertEquals(true, jsonObject["active"]?.jsonPrimitive?.boolean)
    }
    
    @Test
    fun testExampleWithListValue() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.value(listOf("apple", "banana", "orange"))
        
        val example = exampleBuilder.build()
        
        assertNotNull(example.value)
        val jsonArray = example.value as JsonArray
        assertEquals(3, jsonArray.size)
        assertEquals("apple", jsonArray[0].jsonPrimitive.content)
        assertEquals("banana", jsonArray[1].jsonPrimitive.content)
        assertEquals("orange", jsonArray[2].jsonPrimitive.content)
    }
    
    @Test
    fun testExampleWithComplexNestedValue() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.value(mapOf(
            "user" to mapOf(
                "id" to 1,
                "profile" to mapOf(
                    "name" to "John",
                    "tags" to listOf("admin", "user"),
                    "settings" to mapOf(
                        "notifications" to true,
                        "theme" to "dark"
                    )
                )
            ),
            "timestamp" to "2024-01-01T00:00:00Z"
        ))
        
        val example = exampleBuilder.build()
        
        assertNotNull(example.value)
        val root = example.value as JsonObject
        val user = root["user"] as JsonObject
        val profile = user["profile"] as JsonObject
        assertEquals("John", profile["name"]?.jsonPrimitive?.content)
        
        val tags = profile["tags"] as JsonArray
        assertEquals(2, tags.size)
        
        val settings = profile["settings"] as JsonObject
        assertEquals(true, settings["notifications"]?.jsonPrimitive?.boolean)
    }
    
    @Test
    fun testExampleWithExternalValue() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.summary = "External example"
        exampleBuilder.externalValue = "https://example.com/examples/user-example.json"
        
        val example = exampleBuilder.build()
        
        assertEquals("External example", example.summary)
        assertEquals("https://example.com/examples/user-example.json", example.externalValue)
        assertNull(example.value) // value should be null when externalValue is set
    }
    
    @Test
    fun testExampleWithSerializableObject() {
        val exampleBuilder = ExampleBuilder()
        val testObject = TestExampleObject("123", "Test Object", listOf("tag1", "tag2"))
        exampleBuilder.value(testObject)
        
        val example = exampleBuilder.build()
        
        assertNotNull(example.value)
        val jsonObject = example.value as JsonObject
        assertEquals("123", jsonObject["id"]?.jsonPrimitive?.content)
        assertEquals("Test Object", jsonObject["name"]?.jsonPrimitive?.content)
        
        val tags = jsonObject["tags"] as JsonArray
        assertEquals(2, tags.size)
    }
    
    @Test
    fun testExampleWithNullValue() {
        val exampleBuilder = ExampleBuilder()
        exampleBuilder.value = JsonNull
        
        val example = exampleBuilder.build()
        
        assertNotNull(example.value)
        assertEquals(JsonNull, example.value)
    }
    
    @Test
    fun testExampleBuilderChaining() {
        val example = ExampleBuilder().apply {
            summary = "Chained example"
            description = "Built with chaining"
            value(mapOf("chained" to true))
        }.build()
        
        assertEquals("Chained example", example.summary)
        assertEquals("Built with chaining", example.description)
        assertNotNull(example.value)
    }
    
    // ExamplesBuilder Tests
    @Test
    fun testEmptyExamples() {
        val examplesBuilder = ExamplesBuilder()
        val examples = examplesBuilder.build()
        
        assertEquals(0, examples.size)
    }
    
    @Test
    fun testExamplesWithSingleExample() {
        val examplesBuilder = ExamplesBuilder()
        examplesBuilder.example("success") {
            summary = "Successful response"
            value(mapOf("status" to "ok", "data" to "result"))
        }
        
        val examples = examplesBuilder.build()
        
        assertEquals(1, examples.size)
        assertNotNull(examples["success"])
        assertEquals("Successful response", examples["success"]?.summary)
    }
    
    @Test
    fun testExamplesWithMultipleExamples() {
        val examplesBuilder = ExamplesBuilder()
        
        examplesBuilder.example("success") {
            summary = "Success"
            value(mapOf("status" to 200))
        }
        
        examplesBuilder.example("error") {
            summary = "Error"
            value(mapOf("status" to 500, "error" to "Internal Server Error"))
        }
        
        examplesBuilder.example("notFound") {
            summary = "Not Found"
            description = "Resource not found"
            value(mapOf("status" to 404, "error" to "Not Found"))
        }
        
        val examples = examplesBuilder.build()
        
        assertEquals(3, examples.size)
        assertNotNull(examples["success"])
        assertNotNull(examples["error"])
        assertNotNull(examples["notFound"])
        
        assertEquals("Not Found", examples["notFound"]?.summary)
        assertEquals("Resource not found", examples["notFound"]?.description)
    }
    
    @Test
    fun testExamplesWithShorthandMethod() {
        val examplesBuilder = ExamplesBuilder()
        
        examplesBuilder.example(
            "user",
            mapOf("id" to 1, "name" to "John"),
            "User example",
            "A complete user object"
        )
        
        examplesBuilder.example(
            "minimal",
            mapOf("id" to 2),
            "Minimal user"
        )
        
        examplesBuilder.example(
            "simple",
            "Just a string"
        )
        
        val examples = examplesBuilder.build()
        
        assertEquals(3, examples.size)
        
        val userExample = examples["user"]
        assertNotNull(userExample)
        assertEquals("User example", userExample.summary)
        assertEquals("A complete user object", userExample.description)
        
        val minimalExample = examples["minimal"]
        assertNotNull(minimalExample)
        assertEquals("Minimal user", minimalExample.summary)
        assertNull(minimalExample.description)
        
        val simpleExample = examples["simple"]
        assertNotNull(simpleExample)
        assertEquals("Just a string", simpleExample.value?.jsonPrimitive?.content)
    }
    
    @Test
    fun testExamplesOverwrite() {
        val examplesBuilder = ExamplesBuilder()
        
        examplesBuilder.example("test", "First value")
        examplesBuilder.example("test", "Second value", "Overwritten")
        
        val examples = examplesBuilder.build()
        
        assertEquals(1, examples.size)
        assertEquals("Second value", examples["test"]?.value?.jsonPrimitive?.content)
        assertEquals("Overwritten", examples["test"]?.summary)
    }
    
    @Test
    fun testExamplesWithVariousValueTypes() {
        val examplesBuilder = ExamplesBuilder()
        
        examplesBuilder.example("string", "text value")
        examplesBuilder.example("number", 42)
        examplesBuilder.example("boolean", false)
        examplesBuilder.example("array", listOf(1, 2, 3))
        examplesBuilder.example("object", mapOf("key" to "value"))
        examplesBuilder.example("null") {
            value = JsonNull
        }
        
        val examples = examplesBuilder.build()
        
        assertEquals(6, examples.size)
        assertEquals("text value", examples["string"]?.value?.jsonPrimitive?.content)
        assertEquals(42, examples["number"]?.value?.jsonPrimitive?.int)
        assertEquals(false, examples["boolean"]?.value?.jsonPrimitive?.boolean)
        assertEquals(3, (examples["array"]?.value as JsonArray).size)
        assertNotNull((examples["object"]?.value as JsonObject)["key"])
        assertEquals(JsonNull, examples["null"]?.value)
    }
    
    @Test
    fun testExamplesBuilderChaining() {
        val examples = ExamplesBuilder().apply {
            example("first", "value1")
            example("second") {
                summary = "Second example"
                value(mapOf("data" to "value2"))
            }
            example("third", 123, "Third example")
        }.build()
        
        assertEquals(3, examples.size)
        assertNotNull(examples["first"])
        assertNotNull(examples["second"])
        assertNotNull(examples["third"])
    }
    
    @Test
    fun testExamplesWithSpecialCharacterKeys() {
        val examplesBuilder = ExamplesBuilder()
        
        examplesBuilder.example("simple-key", "value1")
        examplesBuilder.example("key.with.dots", "value2")
        examplesBuilder.example("key-with-dashes", "value3")
        examplesBuilder.example("key_with_underscores", "value4")
        examplesBuilder.example("key with spaces", "value5")
        
        val examples = examplesBuilder.build()
        
        assertEquals(5, examples.size)
        assertNotNull(examples["simple-key"])
        assertNotNull(examples["key.with.dots"])
        assertNotNull(examples["key-with-dashes"])
        assertNotNull(examples["key_with_underscores"])
        assertNotNull(examples["key with spaces"])
    }
}

@Serializable
data class TestExampleObject(
    val id: String,
    val name: String,
    val tags: List<String>
)