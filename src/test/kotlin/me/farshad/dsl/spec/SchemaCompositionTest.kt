package me.farshad.dsl.spec

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import me.farshad.dsl.builder.core.openApi
import me.farshad.dsl.builder.core.toJson
import me.farshad.dsl.builder.schema.discriminatedUnion
import me.farshad.dsl.builder.schema.extending
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SchemaCompositionTest {
    @Test
    fun testOneOfWithStringReferences() {
        val spec =
            openApi {
                openapi = "3.1.0"
                info {
                    title = "Test API"
                    version = "1.0.0"
                }

                components {
                    schema("PetOrCategory") {
                        oneOf =
                            listOf(
                                "#/components/schemas/Pet",
                                "#/components/schemas/Category",
                            )
                    }
                }
            }

        val json = Json.parseToJsonElement(spec.toJson()).jsonObject
        val schema =
            json["components"]
                ?.jsonObject
                ?.get("schemas")
                ?.jsonObject
                ?.get("PetOrCategory")
                ?.jsonObject
        val oneOfArray = schema?.get("oneOf")?.jsonArray

        assertNotNull(oneOfArray)
        assertEquals(2, oneOfArray.size)
        assertEquals("#/components/schemas/Pet", oneOfArray[0].jsonObject["\$ref"]?.jsonPrimitive?.content)
        assertEquals("#/components/schemas/Category", oneOfArray[1].jsonObject["\$ref"]?.jsonPrimitive?.content)
    }

    @Test
    fun testOneOfWithKClasses() {
        val spec =
            openApi {
                openapi = "3.1.0"
                info {
                    title = "Test API"
                    version = "1.0.0"
                }

                components {
                    schema("AccountType") {
                        oneOf(PrivateAccountDto::class, BusinessAccountDto::class)
                    }
                }
            }

        val json = Json.parseToJsonElement(spec.toJson()).jsonObject
        val schema =
            json["components"]
                ?.jsonObject
                ?.get("schemas")
                ?.jsonObject
                ?.get("AccountType")
                ?.jsonObject
        val oneOfArray = schema?.get("oneOf")?.jsonArray

        assertNotNull(oneOfArray)
        assertEquals(2, oneOfArray.size)
        assertEquals(
            "#/components/schemas/PrivateAccountDto",
            oneOfArray[0].jsonObject["\$ref"]?.jsonPrimitive?.content,
        )
        assertEquals(
            "#/components/schemas/BusinessAccountDto",
            oneOfArray[1].jsonObject["\$ref"]?.jsonPrimitive?.content,
        )
    }

    @Test
    fun testAllOf() {
        val spec =
            openApi {
                openapi = "3.1.0"
                info {
                    title = "Test API"
                    version = "1.0.0"
                }

                components {
                    schema("ComposedSchema") {
                        allOf("BaseSchema1", "BaseSchema2")
                        type = SchemaType.OBJECT
                        property("additionalProp", PropertyType.STRING)
                    }
                }
            }

        val json = Json.parseToJsonElement(spec.toJson()).jsonObject
        val schema =
            json["components"]
                ?.jsonObject
                ?.get("schemas")
                ?.jsonObject
                ?.get("ComposedSchema")
                ?.jsonObject
        val allOfArray = schema?.get("allOf")?.jsonArray

        assertNotNull(allOfArray)
        assertEquals(2, allOfArray.size)
        assertEquals("#/components/schemas/BaseSchema1", allOfArray[0].jsonObject["\$ref"]?.jsonPrimitive?.content)
        assertEquals("#/components/schemas/BaseSchema2", allOfArray[1].jsonObject["\$ref"]?.jsonPrimitive?.content)

        // Check that the schema also has its own properties
        assertEquals("object", schema["type"]?.jsonPrimitive?.content)
        assertNotNull(schema["properties"]?.jsonObject?.get("additionalProp"))
    }

    @Test
    fun testDiscriminator() {
        val spec =
            openApi {
                openapi = "3.1.0"
                info {
                    title = "Test API"
                    version = "1.0.0"
                }

                components {
                    schema("Animal") {
                        oneOf("Dog", "Cat", "Bird")
                        discriminator("type") {
                            mapping("dog", "#/components/schemas/Dog")
                            mapping("cat", "#/components/schemas/Cat")
                            mapping("bird", "#/components/schemas/Bird")
                        }
                    }
                }
            }

        val json = Json.parseToJsonElement(spec.toJson()).jsonObject
        val schema =
            json["components"]
                ?.jsonObject
                ?.get("schemas")
                ?.jsonObject
                ?.get("Animal")
                ?.jsonObject
        val discriminator = schema?.get("discriminator")?.jsonObject

        assertNotNull(discriminator)
        assertEquals("type", discriminator["propertyName"]?.jsonPrimitive?.content)

        val mapping = discriminator["mapping"]?.jsonObject
        assertNotNull(mapping)
        assertEquals("#/components/schemas/Dog", mapping["dog"]?.jsonPrimitive?.content)
        assertEquals("#/components/schemas/Cat", mapping["cat"]?.jsonPrimitive?.content)
        assertEquals("#/components/schemas/Bird", mapping["bird"]?.jsonPrimitive?.content)
    }

    @Test
    fun testInlineSchema() {
        val spec =
            openApi {
                openapi = "3.1.0"
                info {
                    title = "Test API"
                    version = "1.0.0"
                }

                components {
                    schema("MixedOneOf") {
                        oneOf {
                            schema("#/components/schemas/ExistingSchema")
                            schema {
                                type = SchemaType.STRING
                                description = "A simple string option"
                            }
                            schema {
                                type = SchemaType.OBJECT
                                property("inlineProperty", PropertyType.STRING)
                            }
                        }
                    }
                }
            }

        val json = Json.parseToJsonElement(spec.toJson()).jsonObject
        val schema =
            json["components"]
                ?.jsonObject
                ?.get("schemas")
                ?.jsonObject
                ?.get("MixedOneOf")
                ?.jsonObject
        val oneOfArray = schema?.get("oneOf")?.jsonArray

        assertNotNull(oneOfArray)
        assertEquals(3, oneOfArray.size)

        // First is a reference
        assertEquals("#/components/schemas/ExistingSchema", oneOfArray[0].jsonObject["\$ref"]?.jsonPrimitive?.content)

        // Second is an inline string schema
        assertEquals("string", oneOfArray[1].jsonObject["type"]?.jsonPrimitive?.content)
        assertEquals("A simple string option", oneOfArray[1].jsonObject["description"]?.jsonPrimitive?.content)

        // Third is an inline object schema
        assertEquals("object", oneOfArray[2].jsonObject["type"]?.jsonPrimitive?.content)
        assertNotNull(oneOfArray[2].jsonObject["properties"]?.jsonObject?.get("inlineProperty"))
    }

    @Test
    fun testAnyOfAndNot() {
        val spec =
            openApi {
                openapi = "3.1.0"
                info {
                    title = "Test API"
                    version = "1.0.0"
                }

                components {
                    schema("ComplexSchema") {
                        anyOf("Schema1", "Schema2", "Schema3")
                        not("ForbiddenSchema")
                    }
                }
            }

        val json = Json.parseToJsonElement(spec.toJson()).jsonObject
        val schema =
            json["components"]
                ?.jsonObject
                ?.get("schemas")
                ?.jsonObject
                ?.get("ComplexSchema")
                ?.jsonObject

        val anyOfArray = schema?.get("anyOf")?.jsonArray
        assertNotNull(anyOfArray)
        assertEquals(3, anyOfArray.size)

        val notSchema = schema["not"]?.jsonObject
        assertNotNull(notSchema)
        assertEquals("#/components/schemas/ForbiddenSchema", notSchema["\$ref"]?.jsonPrimitive?.content)
    }

    @Test
    fun testHelperFunctions() {
        val spec =
            openApi {
                openapi = "3.1.0"
                info {
                    title = "Test API"
                    version = "1.0.0"
                }

                components {
                    schema("ExtendedSchema") {
                        extending(BaseDto::class) {
                            property("extraField", PropertyType.STRING)
                        }
                    }

                    schema("UnionWithDiscriminator") {
                        discriminatedUnion(
                            "type",
                            "private" to PrivateAccountDto::class,
                            "business" to BusinessAccountDto::class,
                        )
                    }
                }
            }

        val json = Json.parseToJsonElement(spec.toJson()).jsonObject
        val schemas = json["components"]?.jsonObject?.get("schemas")?.jsonObject

        // Test extending
        val extendedSchema = schemas?.get("ExtendedSchema")?.jsonObject
        val allOfArray = extendedSchema?.get("allOf")?.jsonArray
        assertNotNull(allOfArray)
        assertTrue(
            allOfArray.any {
                it.jsonObject["\$ref"]?.jsonPrimitive?.content == "#/components/schemas/BaseDto"
            },
        )

        // Test discriminated union
        val unionSchema = schemas?.get("UnionWithDiscriminator")?.jsonObject
        val oneOfArray = unionSchema?.get("oneOf")?.jsonArray
        assertNotNull(oneOfArray)
        assertEquals(2, oneOfArray.size)

        val discriminator = unionSchema["discriminator"]?.jsonObject
        assertNotNull(discriminator)
        assertEquals("type", discriminator["propertyName"]?.jsonPrimitive?.content)

        val mapping = discriminator["mapping"]?.jsonObject
        assertNotNull(mapping)
        assertEquals("#/components/schemas/PrivateAccountDto", mapping["private"]?.jsonPrimitive?.content)
        assertEquals("#/components/schemas/BusinessAccountDto", mapping["business"]?.jsonPrimitive?.content)
    }
}

// Test DTO for extending test
@Serializable
data class BaseDto(
    val id: String,
    val name: String,
)

@Serializable
data class PrivateAccountDto(
    val id: String? = null,
    val market: String,
    val creationContext: String? = null,
    val type: String,
    val members: List<MemberIdentityDto>? = null,
    val paymentInfoId: String? = null,
    val debtorId: String? = null,
)

@Serializable
data class BusinessAccountDto(
    val id: String? = null,
    val market: String,
    val creationContext: String? = null,
    val type: String,
    val members: List<MemberIdentityDto>? = null,
    val companyName: String,
    val vatId: String? = null,
    val paymentInfoId: String? = null,
    val debtorId: String? = null,
)

@Serializable
data class MemberIdentityDto(
    val id: String? = null,
    val accountRole: String,
    val type: String,
    val address: List<AddressDto>? = null,
    val identityId: String? = null,
)

@Serializable
data class AddressDto(
    val id: String? = null,
    val identityId: String? = null,
    val addressLine1: String,
    val addressLine2: String? = null,
    val zipCode: String,
    val city: String,
    val country: String,
    val addressType: String,
    val validFrom: String? = null,
    val validTo: String? = null,
)
