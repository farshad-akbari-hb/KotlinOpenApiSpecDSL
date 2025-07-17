package me.farshad.dsl.builder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import me.farshad.dsl.spec.SchemaType

class CompositionBuildersTest {
    
    // OneOfBuilder Tests
    @Test
    fun testOneOfBuilderEmpty() {
        val oneOfBuilder = OneOfBuilder()
        val schemas = oneOfBuilder.build()
        
        assertEquals(0, schemas.size)
    }
    
    @Test
    fun testOneOfBuilderWithStringRefs() {
        val oneOfBuilder = OneOfBuilder()
        oneOfBuilder.schema("User")
        oneOfBuilder.schema("Admin")
        oneOfBuilder.schema("Guest")
        
        val schemas = oneOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertTrue(schemas[0] is SchemaReference.Ref)
        assertEquals("#/components/schemas/User", (schemas[0] as SchemaReference.Ref).path)
        assertEquals("#/components/schemas/Admin", (schemas[1] as SchemaReference.Ref).path)
        assertEquals("#/components/schemas/Guest", (schemas[2] as SchemaReference.Ref).path)
    }
    
    @Test
    fun testOneOfBuilderWithFullRefs() {
        val oneOfBuilder = OneOfBuilder()
        oneOfBuilder.schema("#/components/schemas/CustomType")
        oneOfBuilder.schema("#/definitions/LocalType")
        
        val schemas = oneOfBuilder.build()
        
        assertEquals(2, schemas.size)
        assertEquals("#/components/schemas/CustomType", (schemas[0] as SchemaReference.Ref).path)
        assertEquals("#/definitions/LocalType", (schemas[1] as SchemaReference.Ref).path)
    }
    
    @Test
    fun testOneOfBuilderWithClasses() {
        val oneOfBuilder = OneOfBuilder()
        oneOfBuilder.schema(String::class)
        oneOfBuilder.schema(Int::class)
        oneOfBuilder.schema(TestCompositionClass::class)
        
        val schemas = oneOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertTrue(schemas.all { it is SchemaReference.Ref })
    }
    
    @Test
    fun testOneOfBuilderWithInlineSchemas() {
        val oneOfBuilder = OneOfBuilder()
        oneOfBuilder.schema {
            type = SchemaType.STRING
            description = "String option"
        }
        oneOfBuilder.schema {
            type = SchemaType.NUMBER
            description = "Number option"
        }
        oneOfBuilder.schema {
            type = SchemaType.OBJECT
            property("id", me.farshad.dsl.spec.PropertyType.INTEGER)
        }
        
        val schemas = oneOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertTrue(schemas[0] is SchemaReference.Inline)
        assertEquals(SchemaType.STRING, (schemas[0] as SchemaReference.Inline).schema.type)
        assertEquals("String option", (schemas[0] as SchemaReference.Inline).schema.description)
        
        assertTrue(schemas[2] is SchemaReference.Inline)
        assertNotNull((schemas[2] as SchemaReference.Inline).schema.properties)
    }
    
    @Test
    fun testOneOfBuilderMixed() {
        val oneOfBuilder = OneOfBuilder()
        oneOfBuilder.schema("StringRef")
        oneOfBuilder.schema(Int::class)
        oneOfBuilder.schema {
            type = SchemaType.ARRAY
            items {
                type = SchemaType.STRING
            }
        }
        
        val schemas = oneOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertTrue(schemas[0] is SchemaReference.Ref)
        assertTrue(schemas[1] is SchemaReference.Ref)
        assertTrue(schemas[2] is SchemaReference.Inline)
    }
    
    // AllOfBuilder Tests
    @Test
    fun testAllOfBuilderEmpty() {
        val allOfBuilder = AllOfBuilder()
        val schemas = allOfBuilder.build()
        
        assertEquals(0, schemas.size)
    }
    
    @Test
    fun testAllOfBuilderWithStringRefs() {
        val allOfBuilder = AllOfBuilder()
        allOfBuilder.schema("BaseModel")
        allOfBuilder.schema("Timestamped")
        allOfBuilder.schema("Versioned")
        
        val schemas = allOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertEquals("#/components/schemas/BaseModel", (schemas[0] as SchemaReference.Ref).path)
        assertEquals("#/components/schemas/Timestamped", (schemas[1] as SchemaReference.Ref).path)
        assertEquals("#/components/schemas/Versioned", (schemas[2] as SchemaReference.Ref).path)
    }
    
    @Test
    fun testAllOfBuilderWithClasses() {
        val allOfBuilder = AllOfBuilder()
        allOfBuilder.schema(TestBaseClass::class)
        allOfBuilder.schema(TestExtensionClass::class)
        
        val schemas = allOfBuilder.build()
        
        assertEquals(2, schemas.size)
        assertTrue(schemas.all { it is SchemaReference.Ref })
    }
    
    @Test
    fun testAllOfBuilderWithInlineSchemas() {
        val allOfBuilder = AllOfBuilder()
        allOfBuilder.schema {
            type = SchemaType.OBJECT
            property("id", me.farshad.dsl.spec.PropertyType.INTEGER, true)
        }
        allOfBuilder.schema {
            type = SchemaType.OBJECT
            property("name", me.farshad.dsl.spec.PropertyType.STRING, true)
        }
        allOfBuilder.schema {
            type = SchemaType.OBJECT
            property("createdAt", me.farshad.dsl.spec.PropertyType.STRING, true) {
                format = me.farshad.dsl.spec.SchemaFormat.DATE_TIME
            }
        }
        
        val schemas = allOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertTrue(schemas.all { it is SchemaReference.Inline })
        
        val firstSchema = (schemas[0] as SchemaReference.Inline).schema
        assertNotNull(firstSchema.properties?.get("id"))
        
        val secondSchema = (schemas[1] as SchemaReference.Inline).schema
        assertNotNull(secondSchema.properties?.get("name"))
    }
    
    // AnyOfBuilder Tests
    @Test
    fun testAnyOfBuilderEmpty() {
        val anyOfBuilder = AnyOfBuilder()
        val schemas = anyOfBuilder.build()
        
        assertEquals(0, schemas.size)
    }
    
    @Test
    fun testAnyOfBuilderWithStringRefs() {
        val anyOfBuilder = AnyOfBuilder()
        anyOfBuilder.schema("StringValue")
        anyOfBuilder.schema("NumberValue")
        anyOfBuilder.schema("BooleanValue")
        anyOfBuilder.schema("NullValue")
        
        val schemas = anyOfBuilder.build()
        
        assertEquals(4, schemas.size)
        assertTrue(schemas.all { it is SchemaReference.Ref })
    }
    
    @Test
    fun testAnyOfBuilderWithFullRefs() {
        val anyOfBuilder = AnyOfBuilder()
        anyOfBuilder.schema("#/components/schemas/Option1")
        anyOfBuilder.schema("#/definitions/Option2")
        anyOfBuilder.schema("#/components/schemas/Option3")
        
        val schemas = anyOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertEquals("#/components/schemas/Option1", (schemas[0] as SchemaReference.Ref).path)
        assertEquals("#/definitions/Option2", (schemas[1] as SchemaReference.Ref).path)
    }
    
    @Test
    fun testAnyOfBuilderWithClasses() {
        val anyOfBuilder = AnyOfBuilder()
        anyOfBuilder.schema(String::class)
        anyOfBuilder.schema(List::class)
        anyOfBuilder.schema(Map::class)
        
        val schemas = anyOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertTrue(schemas.all { it is SchemaReference.Ref })
    }
    
    @Test
    fun testAnyOfBuilderWithInlineSchemas() {
        val anyOfBuilder = AnyOfBuilder()
        anyOfBuilder.schema {
            type = SchemaType.STRING
            format = me.farshad.dsl.spec.SchemaFormat.EMAIL
        }
        anyOfBuilder.schema {
            type = SchemaType.STRING
            format = me.farshad.dsl.spec.SchemaFormat.URL
        }
        anyOfBuilder.schema {
            type = SchemaType.NULL
        }
        
        val schemas = anyOfBuilder.build()
        
        assertEquals(3, schemas.size)
        assertTrue(schemas.all { it is SchemaReference.Inline })
        
        assertEquals(me.farshad.dsl.spec.SchemaFormat.EMAIL, 
            (schemas[0] as SchemaReference.Inline).schema.format)
        assertEquals(me.farshad.dsl.spec.SchemaFormat.URL, 
            (schemas[1] as SchemaReference.Inline).schema.format)
        assertEquals(SchemaType.NULL, 
            (schemas[2] as SchemaReference.Inline).schema.type)
    }
    
    @Test
    fun testAnyOfBuilderMixed() {
        val anyOfBuilder = AnyOfBuilder()
        anyOfBuilder.schema("ExistingSchema")
        anyOfBuilder.schema(Double::class)
        anyOfBuilder.schema {
            type = SchemaType.OBJECT
            description = "Inline object option"
        }
        anyOfBuilder.schema("#/definitions/CustomRef")
        
        val schemas = anyOfBuilder.build()
        
        assertEquals(4, schemas.size)
        assertTrue(schemas[0] is SchemaReference.Ref)
        assertTrue(schemas[1] is SchemaReference.Ref)
        assertTrue(schemas[2] is SchemaReference.Inline)
        assertTrue(schemas[3] is SchemaReference.Ref)
        
        assertEquals("#/components/schemas/ExistingSchema", 
            (schemas[0] as SchemaReference.Ref).path)
        assertEquals("#/definitions/CustomRef", 
            (schemas[3] as SchemaReference.Ref).path)
    }
    
    @Test
    fun testCompositionBuildersOrdering() {
        // Test that schemas are added in the order they are defined
        val oneOfBuilder = OneOfBuilder()
        oneOfBuilder.schema("First")
        oneOfBuilder.schema("Second")
        oneOfBuilder.schema("Third")
        
        val schemas = oneOfBuilder.build()
        
        assertEquals("#/components/schemas/First", (schemas[0] as SchemaReference.Ref).path)
        assertEquals("#/components/schemas/Second", (schemas[1] as SchemaReference.Ref).path)
        assertEquals("#/components/schemas/Third", (schemas[2] as SchemaReference.Ref).path)
    }
}

// Test classes for composition
class TestCompositionClass
class TestBaseClass
class TestExtensionClass