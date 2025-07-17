package me.farshad.dsl.builder.schema

import me.farshad.dsl.builder.schema.DiscriminatorBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DiscriminatorBuilderTest {
    @Test
    fun testDiscriminatorWithPropertyNameOnly() {
        val discriminatorBuilder = DiscriminatorBuilder("type")
        val discriminator = discriminatorBuilder.build()

        assertEquals("type", discriminator.propertyName)
        assertNull(discriminator.mapping)
    }

    @Test
    fun testDiscriminatorWithStringMappings() {
        val discriminatorBuilder = DiscriminatorBuilder("petType")
        discriminatorBuilder.mapping("cat", "Cat")
        discriminatorBuilder.mapping("dog", "Dog")
        discriminatorBuilder.mapping("bird", "Bird")

        val discriminator = discriminatorBuilder.build()

        assertEquals("petType", discriminator.propertyName)
        assertNotNull(discriminator.mapping)
        assertEquals(3, discriminator.mapping?.size)
        assertEquals("Cat", discriminator.mapping?.get("cat"))
        assertEquals("Dog", discriminator.mapping?.get("dog"))
        assertEquals("Bird", discriminator.mapping?.get("bird"))
    }

    @Test
    fun testDiscriminatorWithClassMappings() {
        val discriminatorBuilder = DiscriminatorBuilder("animalType")
        discriminatorBuilder.mapping("cat", TestCat::class)
        discriminatorBuilder.mapping("dog", TestDog::class)
        discriminatorBuilder.mapping("bird", TestBird::class)

        val discriminator = discriminatorBuilder.build()

        assertEquals("animalType", discriminator.propertyName)
        assertNotNull(discriminator.mapping)
        assertEquals(3, discriminator.mapping?.size)
        assertEquals("#/components/schemas/TestCat", discriminator.mapping?.get("cat"))
        assertEquals("#/components/schemas/TestDog", discriminator.mapping?.get("dog"))
        assertEquals("#/components/schemas/TestBird", discriminator.mapping?.get("bird"))
    }

    @Test
    fun testDiscriminatorWithMixedMappings() {
        val discriminatorBuilder = DiscriminatorBuilder("objectType")
        discriminatorBuilder.mapping("user", "User")
        discriminatorBuilder.mapping("admin", TestAdmin::class)
        discriminatorBuilder.mapping("guest", "Guest")
        discriminatorBuilder.mapping("moderator", TestModerator::class)

        val discriminator = discriminatorBuilder.build()

        assertEquals("objectType", discriminator.propertyName)
        assertNotNull(discriminator.mapping)
        assertEquals(4, discriminator.mapping?.size)
        assertEquals("User", discriminator.mapping?.get("user"))
        assertEquals("#/components/schemas/TestAdmin", discriminator.mapping?.get("admin"))
        assertEquals("Guest", discriminator.mapping?.get("guest"))
        assertEquals("#/components/schemas/TestModerator", discriminator.mapping?.get("moderator"))
    }

    @Test
    fun testDiscriminatorWithSpecialPropertyNames() {
        val specialNames =
            listOf(
                "kind",
                "_type",
                "\$type",
                "type",
                "object-type",
                "object_type",
                "objectType",
                "@type",
                "type.name",
            )

        specialNames.forEach { propertyName ->
            val discriminatorBuilder = DiscriminatorBuilder(propertyName)
            discriminatorBuilder.mapping("value1", "Schema1")

            val discriminator = discriminatorBuilder.build()

            assertEquals(propertyName, discriminator.propertyName)
            assertNotNull(discriminator.mapping)
        }
    }

    @Test
    fun testDiscriminatorWithSpecialMappingKeys() {
        val discriminatorBuilder = DiscriminatorBuilder("type")
        discriminatorBuilder.mapping("simple-key", "SimpleSchema")
        discriminatorBuilder.mapping("complex.key", "ComplexSchema")
        discriminatorBuilder.mapping("\$special", "SpecialSchema")
        discriminatorBuilder.mapping("_underscore", "UnderscoreSchema")
        discriminatorBuilder.mapping("123numeric", "NumericSchema")

        val discriminator = discriminatorBuilder.build()

        assertEquals("type", discriminator.propertyName)
        assertNotNull(discriminator.mapping)
        assertEquals(5, discriminator.mapping?.size)
        assertEquals("SimpleSchema", discriminator.mapping?.get("simple-key"))
        assertEquals("ComplexSchema", discriminator.mapping?.get("complex.key"))
        assertEquals("SpecialSchema", discriminator.mapping?.get("\$special"))
        assertEquals("UnderscoreSchema", discriminator.mapping?.get("_underscore"))
        assertEquals("NumericSchema", discriminator.mapping?.get("123numeric"))
    }

    @Test
    fun testDiscriminatorOverwriteMappings() {
        val discriminatorBuilder = DiscriminatorBuilder("type")
        discriminatorBuilder.mapping("key1", "FirstSchema")
        discriminatorBuilder.mapping("key1", "SecondSchema") // Overwrite
        discriminatorBuilder.mapping("key2", "Schema2")
        discriminatorBuilder.mapping("key2", TestSchema::class) // Overwrite with class

        val discriminator = discriminatorBuilder.build()

        assertNotNull(discriminator.mapping)
        assertEquals(2, discriminator.mapping?.size)
        assertEquals("SecondSchema", discriminator.mapping?.get("key1"))
        assertEquals("#/components/schemas/TestSchema", discriminator.mapping?.get("key2"))
    }

    @Test
    fun testDiscriminatorEmptyStringPropertyName() {
        val discriminatorBuilder = DiscriminatorBuilder("")
        discriminatorBuilder.mapping("type1", "Schema1")

        val discriminator = discriminatorBuilder.build()

        assertEquals("", discriminator.propertyName) // Empty string is allowed
        assertNotNull(discriminator.mapping)
    }

    @Test
    fun testDiscriminatorUnicodePropertyName() {
        val discriminatorBuilder = DiscriminatorBuilder("类型")
        discriminatorBuilder.mapping("猫", "Cat")
        discriminatorBuilder.mapping("狗", "Dog")

        val discriminator = discriminatorBuilder.build()

        assertEquals("类型", discriminator.propertyName)
        assertNotNull(discriminator.mapping)
        assertEquals("Cat", discriminator.mapping?.get("猫"))
        assertEquals("Dog", discriminator.mapping?.get("狗"))
    }

    @Test
    fun testDiscriminatorLongPropertyName() {
        val longPropertyName = "a".repeat(100)
        val discriminatorBuilder = DiscriminatorBuilder(longPropertyName)
        discriminatorBuilder.mapping("key", "Schema")

        val discriminator = discriminatorBuilder.build()

        assertEquals(longPropertyName, discriminator.propertyName)
        assertNotNull(discriminator.mapping)
    }

    @Test
    fun testDiscriminatorSingleMapping() {
        val discriminatorBuilder = DiscriminatorBuilder("singleType")
        discriminatorBuilder.mapping("only", "OnlySchema")

        val discriminator = discriminatorBuilder.build()

        assertEquals("singleType", discriminator.propertyName)
        assertNotNull(discriminator.mapping)
        assertEquals(1, discriminator.mapping?.size)
        assertEquals("OnlySchema", discriminator.mapping?.get("only"))
    }

    @Test
    fun testDiscriminatorManyMappings() {
        val discriminatorBuilder = DiscriminatorBuilder("manyTypes")

        // Add many mappings
        for (i in 1..20) {
            discriminatorBuilder.mapping("type$i", "Schema$i")
        }

        val discriminator = discriminatorBuilder.build()

        assertEquals("manyTypes", discriminator.propertyName)
        assertNotNull(discriminator.mapping)
        assertEquals(20, discriminator.mapping?.size)

        // Verify a few mappings
        assertEquals("Schema1", discriminator.mapping?.get("type1"))
        assertEquals("Schema10", discriminator.mapping?.get("type10"))
        assertEquals("Schema20", discriminator.mapping?.get("type20"))
    }
}

// Test classes for discriminator
class TestCat

class TestDog

class TestBird

class TestAdmin

class TestModerator

class TestSchema
