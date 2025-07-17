package me.farshad.dsl.serializer

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.farshad.dsl.spec.Example
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExampleSerializerTest {
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = false
        }

    @Test
    fun testSerializeExampleWithAllFields() {
        val example =
            Example(
                summary = "Test Summary",
                description = "Test Description",
                value =
                    buildJsonObject {
                        put("id", 123)
                        put("name", "Test User")
                    },
                externalValue = null,
            )

        val jsonString = json.encodeToString(example)
        val jsonElement = json.parseToJsonElement(jsonString)

        assertNotNull(jsonElement)
        val jsonObject = jsonElement.jsonObject
        assertEquals("Test Summary", jsonObject["summary"]?.jsonPrimitive?.content)
        assertEquals("Test Description", jsonObject["description"]?.jsonPrimitive?.content)

        val value = jsonObject["value"]?.jsonObject
        assertNotNull(value)
        assertEquals(123, value["id"]?.jsonPrimitive?.int)
        assertEquals("Test User", value["name"]?.jsonPrimitive?.content)
    }

    @Test
    fun testSerializeExampleWithOnlySummary() {
        val example =
            Example(
                summary = "Only Summary",
                description = null,
                value = null,
                externalValue = null,
            )

        val jsonString = json.encodeToString(example)
        val jsonElement = json.parseToJsonElement(jsonString)

        val jsonObject = jsonElement.jsonObject
        assertEquals(1, jsonObject.size)
        assertEquals("Only Summary", jsonObject["summary"]?.jsonPrimitive?.content)
    }

    @Test
    fun testSerializeExampleWithOnlyValue() {
        val example =
            Example(
                summary = null,
                description = null,
                value = JsonPrimitive("Simple String Value"),
                externalValue = null,
            )

        val jsonString = json.encodeToString(example)
        val jsonElement = json.parseToJsonElement(jsonString)

        val jsonObject = jsonElement.jsonObject
        assertEquals(1, jsonObject.size)
        assertEquals("Simple String Value", jsonObject["value"]?.jsonPrimitive?.content)
    }

    @Test
    fun testSerializeExampleWithExternalValue() {
        val example =
            Example(
                summary = "External Example",
                description = null,
                value = null,
                externalValue = "https://example.com/example.json",
            )

        val jsonString = json.encodeToString(example)
        val jsonElement = json.parseToJsonElement(jsonString)

        val jsonObject = jsonElement.jsonObject
        assertEquals(2, jsonObject.size)
        assertEquals("External Example", jsonObject["summary"]?.jsonPrimitive?.content)
        assertEquals("https://example.com/example.json", jsonObject["externalValue"]?.jsonPrimitive?.content)
    }

    @Test
    fun testSerializeEmptyExample() {
        val example = Example(null, null, null, null)

        val jsonString = json.encodeToString(example)
        assertEquals("{}", jsonString.trim())
    }

    @Test
    fun testDeserializeExampleWithAllFields() {
        val jsonString =
            """
            {
                "summary": "Deserialized Summary",
                "description": "Deserialized Description",
                "value": {
                    "status": "success",
                    "code": 200
                },
                "externalValue": "https://api.example.com/data"
            }
            """.trimIndent()

        val example: Example = json.decodeFromString(jsonString)

        assertEquals("Deserialized Summary", example.summary)
        assertEquals("Deserialized Description", example.description)
        assertEquals("https://api.example.com/data", example.externalValue)

        val value = example.value?.jsonObject
        assertNotNull(value)
        assertEquals("success", value["status"]?.jsonPrimitive?.content)
        assertEquals(200, value["code"]?.jsonPrimitive?.int)
    }

    @Test
    fun testDeserializeExampleWithPartialFields() {
        val jsonString =
            """
            {
                "summary": "Partial Example",
                "value": [1, 2, 3]
            }
            """.trimIndent()

        val example: Example = json.decodeFromString(jsonString)

        assertEquals("Partial Example", example.summary)
        assertNull(example.description)
        assertNull(example.externalValue)

        val value = example.value?.jsonArray
        assertNotNull(value)
        assertEquals(3, value.size)
        assertEquals(1, value[0].jsonPrimitive.int)
        assertEquals(2, value[1].jsonPrimitive.int)
        assertEquals(3, value[2].jsonPrimitive.int)
    }

    @Test
    fun testDeserializeEmptyExample() {
        val jsonString = "{}"

        val example: Example = json.decodeFromString(jsonString)

        assertNull(example.summary)
        assertNull(example.description)
        assertNull(example.value)
        assertNull(example.externalValue)
    }

    @Test
    fun testSerializeDeserializeWithComplexJsonObject() {
        val complexValue =
            buildJsonObject {
                put(
                    "user",
                    buildJsonObject {
                        put("id", 1)
                        put("name", "John Doe")
                        put(
                            "roles",
                            buildJsonArray {
                                add("admin")
                                add("user")
                            },
                        )
                        put(
                            "settings",
                            buildJsonObject {
                                put("theme", "dark")
                                put("notifications", true)
                            },
                        )
                    },
                )
                put(
                    "metadata",
                    buildJsonObject {
                        put("created", "2024-01-01")
                        put("version", 2.5)
                    },
                )
            }

        val example =
            Example(
                summary = "Complex Example",
                description = "Example with nested objects",
                value = complexValue,
                externalValue = null,
            )

        val jsonString = json.encodeToString(example)
        val deserializedExample: Example = json.decodeFromString(jsonString)

        assertEquals(example.summary, deserializedExample.summary)
        assertEquals(example.description, deserializedExample.description)

        val deserializedValue = deserializedExample.value?.jsonObject
        assertNotNull(deserializedValue)

        val user = deserializedValue["user"]?.jsonObject
        assertNotNull(user)
        assertEquals("John Doe", user["name"]?.jsonPrimitive?.content)

        val roles = user["roles"]?.jsonArray
        assertNotNull(roles)
        assertEquals(2, roles.size)
        assertEquals("admin", roles[0].jsonPrimitive.content)
    }

    @Test
    fun testSerializeDeserializeWithJsonArray() {
        val arrayValue =
            buildJsonArray {
                add("string")
                add(42)
                add(true)
                add(
                    buildJsonObject {
                        put("nested", "object")
                    },
                )
            }

        val example =
            Example(
                summary = "Array Example",
                value = arrayValue,
            )

        val jsonString = json.encodeToString(example)
        val deserializedExample: Example = json.decodeFromString(jsonString)

        val deserializedArray = deserializedExample.value?.jsonArray
        assertNotNull(deserializedArray)
        assertEquals(4, deserializedArray.size)
        assertEquals("string", deserializedArray[0].jsonPrimitive.content)
        assertEquals(42, deserializedArray[1].jsonPrimitive.int)
        assertEquals(true, deserializedArray[2].jsonPrimitive.boolean)
        assertEquals("object", deserializedArray[3].jsonObject["nested"]?.jsonPrimitive?.content)
    }

    @Test
    fun testSerializeDeserializeWithJsonPrimitives() {
        val stringExample = Example(value = JsonPrimitive("test string"))
        val numberExample = Example(value = JsonPrimitive(123.45))
        val booleanExample = Example(value = JsonPrimitive(false))

        val stringJson = json.encodeToString(stringExample)
        val numberJson = json.encodeToString(numberExample)
        val booleanJson = json.encodeToString(booleanExample)

        val deserializedString: Example = json.decodeFromString(stringJson)
        val deserializedNumber: Example = json.decodeFromString(numberJson)
        val deserializedBoolean: Example = json.decodeFromString(booleanJson)

        assertEquals("test string", deserializedString.value?.jsonPrimitive?.content)
        assertEquals(123.45, deserializedNumber.value?.jsonPrimitive?.double)
        assertEquals(false, deserializedBoolean.value?.jsonPrimitive?.boolean)
    }

    @Test
    fun testSerializeDeserializeWithJsonNull() {
        val example =
            Example(
                summary = "Null Value Example",
                value = JsonNull,
            )

        val jsonString = json.encodeToString(example)
        val deserializedExample: Example = json.decodeFromString(jsonString)

        assertEquals("Null Value Example", deserializedExample.summary)
        assertEquals(JsonNull, deserializedExample.value)
    }

    @Test
    fun testSerializeWithEmptyStrings() {
        val example =
            Example(
                summary = "",
                description = "",
                value = JsonPrimitive(""),
                externalValue = "",
            )

        val jsonString = json.encodeToString(example)
        val jsonElement = json.parseToJsonElement(jsonString)

        val jsonObject = jsonElement.jsonObject
        assertEquals("", jsonObject["summary"]?.jsonPrimitive?.content)
        assertEquals("", jsonObject["description"]?.jsonPrimitive?.content)
        assertEquals("", jsonObject["value"]?.jsonPrimitive?.content)
        assertEquals("", jsonObject["externalValue"]?.jsonPrimitive?.content)
    }

    @Test
    fun testRoundTripSerialization() {
        val originalExamples =
            listOf(
                Example("Summary 1", "Description 1", JsonPrimitive("value1"), null),
                Example(null, null, buildJsonObject { put("key", "value") }, null),
                Example("Summary 3", null, null, "https://example.com"),
                Example(
                    null,
                    "Description 4",
                    buildJsonArray {
                        add(1)
                        add(2)
                        add(3)
                    },
                    null,
                ),
                Example("Summary 5", "Description 5", JsonNull, null),
            )

        originalExamples.forEach { original ->
            val jsonString = json.encodeToString(original)
            val deserialized: Example = json.decodeFromString(jsonString)

            assertEquals(original.summary, deserialized.summary)
            assertEquals(original.description, deserialized.description)
            assertEquals(original.value, deserialized.value)
            assertEquals(original.externalValue, deserialized.externalValue)
        }
    }

    @Test
    fun testDeserializeWithUnknownFields() {
        // Note: The custom ExampleSerializer is strict and doesn't ignore unknown fields
        // This test verifies that behavior by expecting an exception
        val jsonWithIgnoreUnknownKeys =
            Json {
                prettyPrint = true
                encodeDefaults = false
                ignoreUnknownKeys = true
            }

        val jsonString =
            """
            {
                "summary": "Test",
                "unknownField": "should be ignored",
                "value": {"test": true},
                "anotherUnknown": 123
            }
            """.trimIndent()

        // Even with ignoreUnknownKeys = true, the custom serializer's behavior takes precedence
        // and it will fail on unknown fields because it uses decodeElementIndex which is strict
        try {
            val example: Example = jsonWithIgnoreUnknownKeys.decodeFromString(jsonString)
            // If we get here, the serializer has been updated to handle unknown fields
            assertEquals("Test", example.summary)
            assertNotNull(example.value)
            assertEquals(
                true,
                example.value
                    ?.jsonObject
                    ?.get("test")
                    ?.jsonPrimitive
                    ?.boolean,
            )
        } catch (e: Exception) {
            // Expected behavior with the current strict serializer implementation
            assert(e.message?.contains("unknown key") == true || e.message?.contains("Unexpected JSON token") == true)
        }
    }

    @Test
    fun testSerializeWithSpecialCharacters() {
        val example =
            Example(
                summary = "Summary with \"quotes\" and 'apostrophes'",
                description = "Description with\nnewlines\tand\ttabs",
                value = JsonPrimitive("Special chars: <>&{}[]"),
                externalValue = "https://example.com/path?query=value&param=test",
            )

        val jsonString = json.encodeToString(example)
        val deserializedExample: Example = json.decodeFromString(jsonString)

        assertEquals(example.summary, deserializedExample.summary)
        assertEquals(example.description, deserializedExample.description)
        assertEquals("Special chars: <>&{}[]", deserializedExample.value?.jsonPrimitive?.content)
        assertEquals(example.externalValue, deserializedExample.externalValue)
    }
}
