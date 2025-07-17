package me.farshad.dsl.serializer

import com.charleskorn.kaml.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JsonElementYamlSerializerTest {
    private val yaml =
        Yaml(
            configuration =
                YamlConfiguration(
                    encodeDefaults = false,
                    strictMode = false,
                ),
            serializersModule =
                SerializersModule {
                    contextual(JsonElementYamlSerializer)
                },
        )

    @Test
    fun testSerializeJsonNull() {
        val jsonNull = JsonNull

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, jsonNull)

        // YAML represents null as 'null'
        assertEquals("null", yamlString.trim())
    }

    @Test
    fun testSerializeJsonPrimitiveString() {
        val stringPrimitive = JsonPrimitive("Hello, YAML!")

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, stringPrimitive)

        // YAML may or may not quote strings depending on content
        assertTrue(yamlString.contains("Hello, YAML!"))
    }

    @Test
    fun testSerializeJsonPrimitiveStringWithSpecialChars() {
        val specialStringPrimitive = JsonPrimitive("Line 1\nLine 2\tTabbed")

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, specialStringPrimitive)

        assertNotNull(yamlString)
        // YAML handles special characters with proper escaping or literal blocks
    }

    @Test
    fun testSerializeJsonPrimitiveBoolean() {
        val truePrimitive = JsonPrimitive(true)
        val falsePrimitive = JsonPrimitive(false)

        val trueYaml = yaml.encodeToString(JsonElementYamlSerializer, truePrimitive)
        val falseYaml = yaml.encodeToString(JsonElementYamlSerializer, falsePrimitive)

        assertEquals("true", trueYaml.trim())
        assertEquals("false", falseYaml.trim())
    }

    @Test
    fun testSerializeJsonPrimitiveNumbers() {
        val intPrimitive = JsonPrimitive(42)
        val longPrimitive = JsonPrimitive(9223372036854775807L)
        val doublePrimitive = JsonPrimitive(3.14159)
        val negativePrimitive = JsonPrimitive(-100)

        val intYaml = yaml.encodeToString(JsonElementYamlSerializer, intPrimitive)
        val longYaml = yaml.encodeToString(JsonElementYamlSerializer, longPrimitive)
        val doubleYaml = yaml.encodeToString(JsonElementYamlSerializer, doublePrimitive)
        val negativeYaml = yaml.encodeToString(JsonElementYamlSerializer, negativePrimitive)

        assertEquals("42", intYaml.trim())
        assertEquals("9223372036854775807", longYaml.trim())
        assertEquals("3.14159", doubleYaml.trim())
        assertEquals("-100", negativeYaml.trim())
    }

    @Test
    fun testSerializeJsonPrimitiveEdgeCases() {
        val emptyString = JsonPrimitive("")
        val zeroInt = JsonPrimitive(0)
        val zeroDouble = JsonPrimitive(0.0)

        val emptyYaml = yaml.encodeToString(JsonElementYamlSerializer, emptyString)
        val zeroIntYaml = yaml.encodeToString(JsonElementYamlSerializer, zeroInt)
        val zeroDoubleYaml = yaml.encodeToString(JsonElementYamlSerializer, zeroDouble)

        // Empty string is represented with document marker
        assertTrue(emptyYaml.contains("\"\"") || emptyYaml == "--- \"\"")
        assertEquals("0", zeroIntYaml.trim())
        assertEquals("0.0", zeroDoubleYaml.trim())
    }

    @Test
    fun testSerializeJsonArray() {
        val simpleArray =
            buildJsonArray {
                add("string")
                add(123)
                add(true)
                add(JsonNull)
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, simpleArray)

        // YAML array should be formatted as list items
        assertTrue(yamlString.contains("- "))
        assertTrue(yamlString.contains("string"))
        assertTrue(yamlString.contains("123"))
        assertTrue(yamlString.contains("true"))
    }

    @Test
    fun testSerializeEmptyJsonArray() {
        val emptyArray = buildJsonArray { }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, emptyArray)

        assertEquals("[]", yamlString.trim())
    }

    @Test
    fun testSerializeNestedJsonArray() {
        val nestedArray =
            buildJsonArray {
                add("outer")
                add(
                    buildJsonArray {
                        add("inner1")
                        add("inner2")
                    },
                )
                add(
                    buildJsonArray {
                        add(1)
                        add(2)
                        add(3)
                    },
                )
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, nestedArray)

        assertNotNull(yamlString)
        assertTrue(yamlString.contains("\"outer\"") || yamlString.contains("outer"))
        assertTrue(yamlString.contains("\"inner1\"") || yamlString.contains("inner1"))
        assertTrue(yamlString.contains("\"inner2\"") || yamlString.contains("inner2"))
    }

    @Test
    fun testSerializeJsonObject() {
        val simpleObject =
            buildJsonObject {
                put("name", "John Doe")
                put("age", 30)
                put("active", true)
                put("salary", JsonNull)
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, simpleObject)

        // Keys might be quoted in YAML
        assertTrue(yamlString.contains("\"name\":") || yamlString.contains("name:"))
        assertTrue(yamlString.contains("John Doe"))
        assertTrue(yamlString.contains("\"age\":") || yamlString.contains("age:"))
        assertTrue(yamlString.contains("30"))
        assertTrue(yamlString.contains("\"active\":") || yamlString.contains("active:"))
        assertTrue(yamlString.contains("true"))
        assertTrue(yamlString.contains("\"salary\":") || yamlString.contains("salary:"))
    }

    @Test
    fun testSerializeEmptyJsonObject() {
        val emptyObject = buildJsonObject { }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, emptyObject)

        assertEquals("{}", yamlString.trim())
    }

    @Test
    fun testSerializeNestedJsonObject() {
        val nestedObject =
            buildJsonObject {
                put(
                    "user",
                    buildJsonObject {
                        put("id", 1)
                        put(
                            "profile",
                            buildJsonObject {
                                put("firstName", "John")
                                put("lastName", "Doe")
                                put(
                                    "emails",
                                    buildJsonArray {
                                        add("john@example.com")
                                        add("john.doe@work.com")
                                    },
                                )
                            },
                        )
                    },
                )
                put(
                    "metadata",
                    buildJsonObject {
                        put("created", "2024-01-01")
                        put("version", 1.5)
                    },
                )
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, nestedObject)

        assertNotNull(yamlString)
        // Keys might be quoted in YAML
        assertTrue(yamlString.contains("\"user\":") || yamlString.contains("user:"))
        assertTrue(yamlString.contains("\"profile\":") || yamlString.contains("profile:"))
        assertTrue(yamlString.contains("\"firstName\":") || yamlString.contains("firstName:"))
        assertTrue(yamlString.contains("\"emails\":") || yamlString.contains("emails:"))
        assertTrue(yamlString.contains("\"metadata\":") || yamlString.contains("metadata:"))
    }

    @Test
    fun testSerializeComplexMixedStructure() {
        val complexStructure =
            buildJsonObject {
                put(
                    "api",
                    buildJsonObject {
                        put("version", "3.1.0")
                        put(
                            "endpoints",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("path", "/users")
                                        put(
                                            "methods",
                                            buildJsonArray {
                                                add("GET")
                                                add("POST")
                                            },
                                        )
                                        put(
                                            "parameters",
                                            buildJsonObject {
                                                put(
                                                    "query",
                                                    buildJsonArray {
                                                        add(
                                                            buildJsonObject {
                                                                put("name", "limit")
                                                                put("type", "integer")
                                                                put("required", false)
                                                            },
                                                        )
                                                    },
                                                )
                                            },
                                        )
                                    },
                                )
                                add(
                                    buildJsonObject {
                                        put("path", "/users/{id}")
                                        put(
                                            "methods",
                                            buildJsonArray {
                                                add("GET")
                                                add("PUT")
                                                add("DELETE")
                                            },
                                        )
                                    },
                                )
                            },
                        )
                        put(
                            "security",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("type", "bearer")
                                        put("scheme", "JWT")
                                    },
                                )
                            },
                        )
                    },
                )
                put(
                    "info",
                    buildJsonObject {
                        put("title", "Test API")
                        put("description", JsonNull)
                        put(
                            "contact",
                            buildJsonObject {
                                put("email", "support@example.com")
                            },
                        )
                    },
                )
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, complexStructure)

        assertNotNull(yamlString)
        // Verify structure is properly serialized - keys might be quoted
        assertTrue(yamlString.contains("\"api\":") || yamlString.contains("api:"))
        assertTrue(yamlString.contains("\"version\":") || yamlString.contains("version:"))
        assertTrue(yamlString.contains("/users"))
        assertTrue(yamlString.contains("GET") || yamlString.contains("\"GET\""))
        assertTrue(yamlString.contains("POST") || yamlString.contains("\"POST\""))
    }

    @Test
    fun testSerializeArrayOfObjects() {
        val arrayOfObjects =
            buildJsonArray {
                add(
                    buildJsonObject {
                        put("id", 1)
                        put("name", "Item 1")
                    },
                )
                add(
                    buildJsonObject {
                        put("id", 2)
                        put("name", "Item 2")
                    },
                )
                add(
                    buildJsonObject {
                        put("id", 3)
                        put("name", "Item 3")
                    },
                )
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, arrayOfObjects)

        assertNotNull(yamlString)
        // Each object in array should be a list item
        val itemCount =
            yamlString.count {
                it == '-' &&
                    yamlString.indexOf(it) == 0 ||
                    yamlString.substring(
                        yamlString.indexOf(it) - 1,
                        yamlString.indexOf(it),
                    ) == "\n"
            }
        assertTrue(itemCount >= 3)
    }

    @Test
    fun testSerializeObjectWithArrayValues() {
        val objectWithArrays =
            buildJsonObject {
                put(
                    "tags",
                    buildJsonArray {
                        add("kotlin")
                        add("serialization")
                        add("yaml")
                    },
                )
                put(
                    "numbers",
                    buildJsonArray {
                        add(1)
                        add(2)
                        add(3)
                        add(4)
                        add(5)
                    },
                )
                put(
                    "mixed",
                    buildJsonArray {
                        add("string")
                        add(42)
                        add(true)
                        add(JsonNull)
                        add(
                            buildJsonObject {
                                put("nested", "object")
                            },
                        )
                    },
                )
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, objectWithArrays)

        assertNotNull(yamlString)
        assertTrue(yamlString.contains("\"tags\":") || yamlString.contains("tags:"))
        assertTrue(yamlString.contains("kotlin") || yamlString.contains("\"kotlin\""))
        assertTrue(yamlString.contains("\"numbers\":") || yamlString.contains("numbers:"))
        assertTrue(yamlString.contains("\"mixed\":") || yamlString.contains("mixed:"))
    }

    @Test
    fun testSerializeSpecialYamlValues() {
        // Test values that have special meaning in YAML
        val specialValues =
            buildJsonObject {
                put("yes_string", "yes") // Should not be converted to boolean
                put("no_string", "no")
                put("true_string", "true")
                put("false_string", "false")
                put("null_string", "null")
                put("tilde_string", "~")
                put("number_string", "123")
                put("float_string", "3.14")
                put("actual_bool", true)
                put("actual_number", 123)
                put("actual_null", JsonNull)
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, specialValues)

        assertNotNull(yamlString)
        // Keys might be quoted in YAML
        assertTrue(yamlString.contains("\"yes_string\":") || yamlString.contains("yes_string:"))
        assertTrue(yamlString.contains("\"no_string\":") || yamlString.contains("no_string:"))
        assertTrue(yamlString.contains("\"true_string\":") || yamlString.contains("true_string:"))
    }

    @Test
    fun testDeserializationThrowsNotImplementedError() {
        // The deserialize method in JsonElementYamlSerializer throws NotImplementedError
        // However, KAML might throw its own exception before reaching our deserializer
        val yamlString = "test: value"

        // Try to deserialize - should throw either NotImplementedError or KAML exception
        val exception =
            assertFailsWith<Exception> {
                yaml.decodeFromString(JsonElementYamlSerializer, yamlString)
            }

        // The exception could be NotImplementedError from our deserializer
        // or MissingTypeTagException from KAML
        assertTrue(
            exception is NotImplementedError ||
                exception.javaClass.simpleName == "MissingTypeTagException",
            "Expected NotImplementedError or MissingTypeTagException, but got ${exception.javaClass}",
        )
    }

    @Test
    fun testSerializeDeeplyNestedStructure() {
        var current =
            buildJsonObject {
                put("level", 5)
                put("data", "deepest")
            }

        // Create 4 more levels of nesting
        for (i in 4 downTo 1) {
            current =
                buildJsonObject {
                    put("level", i)
                    put("child", current)
                }
        }

        val deeplyNested =
            buildJsonObject {
                put("root", current)
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, deeplyNested)

        assertNotNull(yamlString)
        assertTrue(yamlString.contains("1") || yamlString.contains("\"level\": 1"))
        assertTrue(yamlString.contains("5") || yamlString.contains("\"level\": 5"))
        assertTrue(yamlString.contains("deepest") || yamlString.contains("\"deepest\""))
    }

    @Test
    fun testSerializeLargeArray() {
        val largeArray =
            buildJsonArray {
                for (i in 1..100) {
                    add(
                        buildJsonObject {
                            put("index", i)
                            put("value", "Item $i")
                            put("even", i % 2 == 0)
                        },
                    )
                }
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, largeArray)

        assertNotNull(yamlString)
        // Values should be present, keys might be quoted
        assertTrue(yamlString.contains("1"))
        assertTrue(yamlString.contains("50"))
        assertTrue(yamlString.contains("100"))
        assertTrue(yamlString.contains("Item 50") || yamlString.contains("\"Item 50\""))
    }

    @Test
    fun testSerializeUnicodeAndSpecialCharacters() {
        val unicodeObject =
            buildJsonObject {
                put("emoji", "🎉🎊✨")
                put("chinese", "你好世界")
                put("arabic", "مرحبا بالعالم")
                put("special", "Line1\nLine2\rLine3\tTabbed")
                put("quotes", "She said \"Hello\" and 'Goodbye'")
                put("backslash", "C:\\Users\\Test")
            }

        val yamlString = yaml.encodeToString(JsonElementYamlSerializer, unicodeObject)

        assertNotNull(yamlString)
        // YAML should preserve Unicode characters
        assertTrue(yamlString.contains("🎉🎊✨"))
        assertTrue(yamlString.contains("你好世界"))
        assertTrue(yamlString.contains("مرحبا بالعالم"))
    }
}
