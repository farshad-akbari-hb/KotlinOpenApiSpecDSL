package me.farshad

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.*
import me.farshad.dsl.builder.SchemaBuilder
import me.farshad.dsl.spec.SchemaType
import me.farshad.dsl.spec.SchemaFormat
import me.farshad.dsl.spec.PropertyType
import me.farshad.dsl.spec.SchemaReference

class SchemaBuilderTest {
    
    @Test
    fun testMinimalSchema() {
        val schemaBuilder = SchemaBuilder()
        val schema = schemaBuilder.build()
        
        assertNull(schema.type)
        assertNull(schema.format)
        assertNull(schema.description)
        assertNull(schema.properties)
        assertNull(schema.required)
        assertNull(schema.items)
        assertNull(schema.oneOf)
        assertNull(schema.allOf)
        assertNull(schema.anyOf)
        assertNull(schema.not)
        assertNull(schema.discriminator)
        assertNull(schema.example)
        assertNull(schema.examples)
    }
    
    @Test
    fun testSimpleObjectSchema() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.type = SchemaType.OBJECT
        schemaBuilder.description = "A simple object"
        schemaBuilder.property("id", PropertyType.INTEGER, true)
        schemaBuilder.property("name", PropertyType.STRING, true)
        schemaBuilder.property("email", PropertyType.STRING, false)
        
        val schema = schemaBuilder.build()
        
        assertEquals(SchemaType.OBJECT, schema.type)
        assertEquals("A simple object", schema.description)
        assertNotNull(schema.properties)
        assertEquals(3, schema.properties?.size)
        assertNotNull(schema.required)
        assertEquals(2, schema.required?.size)
        assertEquals(listOf("id", "name"), schema.required)
    }
    
    @Test
    fun testArraySchema() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.type = SchemaType.ARRAY
        schemaBuilder.items {
            type = SchemaType.STRING
        }
        
        val schema = schemaBuilder.build()
        
        assertEquals(SchemaType.ARRAY, schema.type)
        assertNotNull(schema.items)
        assertEquals(SchemaType.STRING, schema.items?.type)
    }
    
    @Test
    fun testSchemaWithFormat() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.type = SchemaType.STRING
        schemaBuilder.format = SchemaFormat.DATE_TIME
        
        val schema = schemaBuilder.build()
        
        assertEquals(SchemaType.STRING, schema.type)
        assertEquals(SchemaFormat.DATE_TIME, schema.format)
    }
    
    @Test
    fun testSchemaWithExample() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.type = SchemaType.OBJECT
        schemaBuilder.example(mapOf("id" to 123, "name" to "Test"))
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.example)
        val exampleJson = schema.example as JsonObject
        assertEquals(123, exampleJson["id"]?.jsonPrimitive?.int)
        assertEquals("Test", exampleJson["name"]?.jsonPrimitive?.content)
    }
    
    @Test
    fun testSchemaWithExamples() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.type = SchemaType.STRING
        schemaBuilder.examples {
            example("valid", "valid-string", "Valid example")
            example("invalid") {
                summary = "Invalid example"
                value("")
            }
        }
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.examples)
        assertEquals(2, schema.examples?.size)
        assertNull(schema.example)
    }
    
    @Test
    fun testOneOfWithStrings() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.oneOf("User", "Admin", "Guest")
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.oneOf)
        assertEquals(3, schema.oneOf?.size)
        
        val refs = schema.oneOf?.mapNotNull { (it as? SchemaReference.Ref)?.path }
        assertEquals(listOf(
            "#/components/schemas/User",
            "#/components/schemas/Admin",
            "#/components/schemas/Guest"
        ), refs)
    }
    
    @Test
    fun testOneOfWithFullRefs() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.oneOf("#/components/schemas/CustomUser", "#/definitions/LocalRef")
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.oneOf)
        assertEquals(2, schema.oneOf?.size)
        
        val refs = schema.oneOf?.mapNotNull { (it as? SchemaReference.Ref)?.path }
        assertEquals(listOf(
            "#/components/schemas/CustomUser",
            "#/definitions/LocalRef"
        ), refs)
    }
    
    @Test
    fun testOneOfWithClasses() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.oneOf(String::class, Int::class, Boolean::class)
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.oneOf)
        assertEquals(3, schema.oneOf?.size)
    }
    
    @Test
    fun testOneOfWithBuilder() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.oneOf {
            schema("User")
            schema(String::class)
            schema {
                type = SchemaType.OBJECT
                property("inline", PropertyType.STRING)
            }
        }
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.oneOf)
        assertEquals(3, schema.oneOf?.size)
    }
    
    @Test
    fun testAllOfComposition() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.allOf("BaseUser", "Timestamps")
        schemaBuilder.allOf {
            schema {
                type = SchemaType.OBJECT
                property("additionalProp", PropertyType.STRING)
            }
        }
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.allOf)
        assertEquals(3, schema.allOf?.size)
    }
    
    @Test
    fun testAnyOfComposition() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.anyOf("StringValue", "NumberValue", "BooleanValue")
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.anyOf)
        assertEquals(3, schema.anyOf?.size)
    }
    
    @Test
    fun testNotComposition() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.not("ForbiddenType")
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.not)
        assertEquals("#/components/schemas/ForbiddenType", (schema.not as SchemaReference.Ref).path)
    }
    
    @Test
    fun testNotWithClass() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.not(String::class)
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.not)
    }
    
    @Test
    fun testNotWithInlineSchema() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.not {
            type = SchemaType.NULL
        }
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.not)
        val notSchema = schema.not as SchemaReference.Inline
        assertEquals(SchemaType.NULL, notSchema.schema.type)
    }
    
    @Test
    fun testDiscriminator() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.oneOf("Cat", "Dog", "Bird")
        schemaBuilder.discriminator("petType") {
            mapping("cat", "Cat")
            mapping("dog", "Dog")
            mapping("bird", Bird::class)
        }
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.discriminator)
        assertEquals("petType", schema.discriminator?.propertyName)
        assertNotNull(schema.discriminator?.mapping)
        assertEquals(3, schema.discriminator?.mapping?.size)
        assertEquals("Cat", schema.discriminator?.mapping?.get("cat"))
        assertEquals("Dog", schema.discriminator?.mapping?.get("dog"))
        assertEquals("#/components/schemas/Bird", schema.discriminator?.mapping?.get("bird"))
    }
    
    @Test
    fun testComplexNestedSchema() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.type = SchemaType.OBJECT
        schemaBuilder.description = "Complex nested object"
        schemaBuilder.property("user", PropertyType.OBJECT, true) {
            property("id", PropertyType.INTEGER, true)
            property("profile", PropertyType.OBJECT, true) {
                property("name", PropertyType.STRING, true)
                property("tags", PropertyType.ARRAY, false) {
                    items {
                        type = SchemaType.STRING
                    }
                }
            }
        }
        schemaBuilder.property("metadata", PropertyType.OBJECT, false) {
            property("created", PropertyType.STRING, true) {
                format = SchemaFormat.DATE_TIME
            }
        }
        
        val schema = schemaBuilder.build()
        
        assertEquals(SchemaType.OBJECT, schema.type)
        assertEquals(2, schema.properties?.size)
        assertEquals(1, schema.required?.size)
        
        val userSchema = schema.properties?.get("user")
        assertNotNull(userSchema)
        assertEquals(2, userSchema.properties?.size)
        
        val profileSchema = userSchema.properties?.get("profile")
        assertNotNull(profileSchema)
        assertEquals(2, profileSchema.properties?.size)
        
        val tagsSchema = profileSchema.properties?.get("tags")
        assertNotNull(tagsSchema)
        assertEquals(SchemaType.ARRAY, tagsSchema.type)
        assertNotNull(tagsSchema.items)
    }
    
    @Test
    fun testSchemaBuilderChaining() {
        val schema = SchemaBuilder().apply {
            type = SchemaType.OBJECT
            description = "Chained schema"
            property("id", PropertyType.STRING, true)
            example(mapOf("id" to "123"))
            oneOf {
                schema("Type1")
                schema("Type2")
            }
        }.build()
        
        assertEquals(SchemaType.OBJECT, schema.type)
        assertEquals("Chained schema", schema.description)
        assertNotNull(schema.properties)
        assertNotNull(schema.example)
        assertNotNull(schema.oneOf)
    }
    
    @Test
    fun testBackwardCompatibilityOneOf() {
        val schemaBuilder = SchemaBuilder()
        // Test backward compatibility with List<String>
        schemaBuilder.oneOf = listOf("Type1", "Type2", "Type3")
        
        val schema = schemaBuilder.build()
        
        assertNotNull(schema.oneOf)
        assertEquals(3, schema.oneOf?.size)
        
        // Test getter
        assertEquals(listOf("Type1", "Type2", "Type3"), schemaBuilder.oneOf)
    }
    
    @Test
    fun testEmptyCollections() {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.type = SchemaType.OBJECT
        // Add no properties
        
        val schema = schemaBuilder.build()
        
        assertEquals(SchemaType.OBJECT, schema.type)
        assertNull(schema.properties) // Empty map should be null
        assertNull(schema.required)   // Empty list should be null
    }
}

// Test class for discriminator mapping
class Bird