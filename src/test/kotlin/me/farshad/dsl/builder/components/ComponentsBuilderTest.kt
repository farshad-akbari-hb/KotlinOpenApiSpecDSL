package me.farshad.dsl.builder.components

import kotlinx.serialization.Serializable
import me.farshad.dsl.annotation.PropertyDescription
import me.farshad.dsl.annotation.SchemaDescription
import me.farshad.dsl.builder.components.ComponentsBuilder
import me.farshad.dsl.spec.PropertyType
import me.farshad.dsl.spec.SchemaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ComponentsBuilderTest {
    @Test
    fun testEmptyComponents() {
        val componentsBuilder = ComponentsBuilder()
        val components = componentsBuilder.build()

        assertNull(components.schemas)
        assertNull(components.securitySchemes)
        assertNull(components.examples)
    }

    @Test
    fun testComponentsWithSingleSchema() {
        val componentsBuilder = ComponentsBuilder()
        componentsBuilder.schema("User") {
            type = SchemaType.OBJECT
            property("id", PropertyType.INTEGER, true)
            property("name", PropertyType.STRING, true)
        }

        val components = componentsBuilder.build()

        assertNotNull(components.schemas)
        assertEquals(1, components.schemas?.size)
        assertNotNull(components.schemas?.get("User"))
        assertEquals(SchemaType.OBJECT, components.schemas?.get("User")?.type)
        assertEquals(
            2,
            components.schemas
                ?.get("User")
                ?.properties
                ?.size,
        )
    }

    @Test
    fun testComponentsWithMultipleSchemas() {
        val componentsBuilder = ComponentsBuilder()
        componentsBuilder.schema("User") {
            type = SchemaType.OBJECT
            property("id", PropertyType.INTEGER, true)
        }
        componentsBuilder.schema("Product") {
            type = SchemaType.OBJECT
            property("sku", PropertyType.STRING, true)
            property("price", PropertyType.NUMBER, true)
        }
        componentsBuilder.schema("Order") {
            type = SchemaType.OBJECT
            property("orderId", PropertyType.STRING, true)
            property("items", PropertyType.ARRAY, true) {
                items {
                    type = SchemaType.OBJECT
                }
            }
        }

        val components = componentsBuilder.build()

        assertNotNull(components.schemas)
        assertEquals(3, components.schemas?.size)
        assertNotNull(components.schemas?.get("User"))
        assertNotNull(components.schemas?.get("Product"))
        assertNotNull(components.schemas?.get("Order"))
    }

    @Test
    fun testComponentsWithSchemaFromClass() {
        val componentsBuilder = ComponentsBuilder()
        componentsBuilder.schema(TestComponentUser::class)

        val components = componentsBuilder.build()

        assertNotNull(components.schemas)
        assertEquals(1, components.schemas?.size)

        val userSchema = components.schemas?.get("TestComponentUser")
        assertNotNull(userSchema)
        assertEquals(SchemaType.OBJECT, userSchema.type)
        assertNotNull(userSchema.properties)

        // Check properties
        assertNotNull(userSchema.properties?.get("id"))
        assertNotNull(userSchema.properties?.get("name"))
        assertNotNull(userSchema.properties?.get("email"))
        assertNotNull(userSchema.properties?.get("tags"))

        // Check required fields (non-nullable)
        assertNotNull(userSchema.required)
        assertEquals(listOf("id", "name"), userSchema.required)
    }

    @Test
    fun testComponentsWithAnnotatedClass() {
        val componentsBuilder = ComponentsBuilder()
        componentsBuilder.schema(AnnotatedModel::class)

        val components = componentsBuilder.build()

        val schema = components.schemas?.get("AnnotatedModel")
        assertNotNull(schema)
        assertEquals("This is an annotated model for testing", schema.description)

        // Check property descriptions
        assertEquals("Unique identifier", schema?.properties?.get("id")?.description)
        assertEquals("User's full name", schema?.properties?.get("name")?.description)
        assertNull(schema?.properties?.get("email")?.description) // No annotation
    }

    @Test
    fun testComponentsWithSecuritySchemes() {
        val componentsBuilder = ComponentsBuilder()
        componentsBuilder.securityScheme("bearerAuth", "http", "bearer", "JWT")
        componentsBuilder.securityScheme("apiKey", "apiKey")
        componentsBuilder.securityScheme("oauth2", "oauth2")

        val components = componentsBuilder.build()

        assertNotNull(components.securitySchemes)
        assertEquals(3, components.securitySchemes?.size)

        val bearerAuth = components.securitySchemes?.get("bearerAuth")
        assertNotNull(bearerAuth)
        assertEquals("http", bearerAuth.type)
        assertEquals("bearer", bearerAuth.scheme)
        assertEquals("JWT", bearerAuth.bearerFormat)

        val apiKey = components.securitySchemes?.get("apiKey")
        assertNotNull(apiKey)
        assertEquals("apiKey", apiKey.type)
        assertNull(apiKey.scheme)
        assertNull(apiKey.bearerFormat)
    }

    @Test
    fun testComponentsWithExamples() {
        val componentsBuilder = ComponentsBuilder()
        componentsBuilder.example("userExample") {
            summary = "User example"
            description = "A complete user example"
            value(mapOf("id" to 123, "name" to "John Doe"))
        }
        componentsBuilder.example("errorExample", mapOf("error" to "Not found"), "Error example", "404 error response")

        val components = componentsBuilder.build()

        assertNotNull(components.examples)
        assertEquals(2, components.examples?.size)

        val userExample = components.examples?.get("userExample")
        assertNotNull(userExample)
        assertEquals("User example", userExample.summary)
        assertEquals("A complete user example", userExample.description)
        assertNotNull(userExample.value)

        val errorExample = components.examples?.get("errorExample")
        assertNotNull(errorExample)
        assertEquals("Error example", errorExample.summary)
        assertEquals("404 error response", errorExample.description)
    }

    @Test
    fun testComponentsWithEverything() {
        val componentsBuilder = ComponentsBuilder()

        // Add schemas
        componentsBuilder.schema("User") {
            type = SchemaType.OBJECT
            property("id", PropertyType.INTEGER, true)
        }
        componentsBuilder.schema(TestComponentProduct::class)

        // Add security schemes
        componentsBuilder.securityScheme("bearerAuth", "http", "bearer")

        // Add examples
        componentsBuilder.example("successExample", mapOf("status" to "ok"))

        val components = componentsBuilder.build()

        assertNotNull(components.schemas)
        assertEquals(2, components.schemas?.size)
        assertNotNull(components.securitySchemes)
        assertEquals(1, components.securitySchemes?.size)
        assertNotNull(components.examples)
        assertEquals(1, components.examples?.size)
    }

    @Test
    fun testComponentsSchemaOverwrite() {
        val componentsBuilder = ComponentsBuilder()

        componentsBuilder.schema("User") {
            type = SchemaType.OBJECT
            property("id", PropertyType.INTEGER)
        }

        // Overwrite with different schema
        componentsBuilder.schema("User") {
            type = SchemaType.OBJECT
            property("userId", PropertyType.STRING, true)
            property("username", PropertyType.STRING, true)
        }

        val components = componentsBuilder.build()

        assertEquals(1, components.schemas?.size)
        val userSchema = components.schemas?.get("User")
        assertNotNull(userSchema?.properties?.get("userId"))
        assertNotNull(userSchema?.properties?.get("username"))
        assertNull(userSchema?.properties?.get("id")) // Original property gone
    }

    @Test
    fun testComponentsWithComplexTypes() {
        val componentsBuilder = ComponentsBuilder()
        componentsBuilder.schema(ComplexTypeModel::class)

        val components = componentsBuilder.build()

        val schema = components.schemas?.get("ComplexTypeModel")
        assertNotNull(schema)

        // Check different property types
        assertEquals(SchemaType.ARRAY, schema?.properties?.get("items")?.type)
        assertEquals(SchemaType.STRING, schema?.properties?.get("name")?.type)
        assertEquals(SchemaType.INTEGER, schema?.properties?.get("count")?.type)
        assertEquals(SchemaType.NUMBER, schema?.properties?.get("price")?.type)
        assertEquals(SchemaType.BOOLEAN, schema?.properties?.get("active")?.type)
        assertEquals(SchemaType.OBJECT, schema?.properties?.get("metadata")?.type)
    }

    @Test
    fun testComponentsBuilderChaining() {
        val components =
            ComponentsBuilder()
                .apply {
                    schema("ChainedSchema") {
                        type = SchemaType.OBJECT
                        property("prop", PropertyType.STRING)
                    }
                    securityScheme("chainedAuth", "apiKey")
                    example("chainedExample", "test value")
                }.build()

        assertNotNull(components.schemas)
        assertNotNull(components.securitySchemes)
        assertNotNull(components.examples)
    }

    @Test
    fun testComponentsWithNullableProperties() {
        val componentsBuilder = ComponentsBuilder()
        componentsBuilder.schema(NullablePropertiesModel::class)

        val components = componentsBuilder.build()

        val schema = components.schemas?.get("NullablePropertiesModel")
        assertNotNull(schema)

        // Required should only contain non-nullable properties
        assertEquals(listOf("id"), schema.required)

        // All properties should exist
        assertEquals(3, schema.properties?.size)
    }
}

// Test models
@Serializable
data class TestComponentUser(
    val id: Int,
    val name: String,
    val email: String? = null,
    val tags: List<String>? = null,
)

@Serializable
data class TestComponentProduct(
    val sku: String,
    val name: String,
    val price: Double,
)

@Serializable
@SchemaDescription("This is an annotated model for testing")
data class AnnotatedModel(
    @PropertyDescription("Unique identifier")
    val id: String,
    @PropertyDescription("User's full name")
    val name: String,
    val email: String? = null,
)

@Serializable
data class ComplexTypeModel(
    val items: List<String>,
    val name: String,
    val count: Int,
    val price: Double,
    val active: Boolean,
    val metadata: Map<String, String>,
)

@Serializable
data class NullablePropertiesModel(
    val id: String,
    val optionalName: String? = null,
    val optionalAge: Int? = null,
)
