package me.farshad.dsl.builder.schema

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.farshad.dsl.spec.Schema
import me.farshad.dsl.spec.SchemaType
import kotlin.reflect.KClass

/**
 * Represents a reference to a schema, either as a string reference or an inline schema.
 * This allows type-safe composition of schemas in oneOf, allOf, and anyOf.
 */
@Serializable(with = SchemaReferenceSerializer::class)
sealed class SchemaReference {
    /**
     * A reference to a schema by its path (e.g., "#/components/schemas/Pet")
     */
    @Serializable
    data class Ref(val path: String) : SchemaReference()

    /**
     * An inline schema definition
     */
    @Serializable
    data class Inline(val schema: Schema) : SchemaReference()
}

/**
 * Custom serializer for SchemaReference that handles both ref strings and inline schemas
 */
object SchemaReferenceSerializer : KSerializer<SchemaReference> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SchemaReference", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SchemaReference) {
        when (value) {
            is SchemaReference.Ref -> {
                // Serialize as a schema with $ref property
                val refSchema = Schema(ref = value.path)
                encoder.encodeSerializableValue(Schema.serializer(), refSchema)
            }

            is SchemaReference.Inline -> {
                encoder.encodeSerializableValue(Schema.serializer(), value.schema)
            }
        }
    }

    override fun deserialize(decoder: Decoder): SchemaReference {
        val schema = decoder.decodeSerializableValue(Schema.serializer())
        return if (schema.ref != null) {
            SchemaReference.Ref(schema.ref)
        } else {
            SchemaReference.Inline(schema)
        }
    }
}

// Extension functions for creating schema references

/**
 * Creates a schema reference from a string path
 */
fun schemaRef(path: String): SchemaReference = SchemaReference.Ref(path)

/**
 * Creates a schema reference from a Kotlin class
 */
fun <T : Any> schemaRef(kClass: KClass<T>): SchemaReference =
    SchemaReference.Ref("#/components/schemas/${kClass.simpleName}")

/**
 * Creates a schema reference from a reified type
 */
inline fun <reified T : Any> schemaRef(): SchemaReference = schemaRef(T::class)

/**
 * Creates an inline schema reference
 */
fun inlineSchema(block: SchemaBuilder.() -> Unit): SchemaReference =
    SchemaReference.Inline(SchemaBuilder().apply(block).build())

// DSL operator overloading for more idiomatic Kotlin

/**
 * Allows using the 'or' operator to create oneOf schemas
 */
infix fun SchemaReference.or(other: SchemaReference): List<SchemaReference> = listOf(this, other)

/**
 * Allows chaining 'or' operators
 */
infix fun List<SchemaReference>.or(other: SchemaReference): List<SchemaReference> = this + other

/**
 * Allows using the 'and' operator to create allOf schemas
 */
infix fun SchemaReference.and(other: SchemaReference): List<SchemaReference> = listOf(this, other)

/**
 * Allows chaining 'and' operators
 */
infix fun List<SchemaReference>.and(other: SchemaReference): List<SchemaReference> = this + other

// Helper extension functions for common schema composition patterns

/**
 * Creates a discriminated union (oneOf with discriminator) for polymorphic types
 */
fun SchemaBuilder.discriminatedUnion(
    discriminatorProperty: String,
    vararg mappings: Pair<String, KClass<*>>,
) {
    oneOf(*mappings.map { it.second }.toTypedArray())
    discriminator(discriminatorProperty) {
        mappings.forEach { (value, clazz) ->
            mapping(value, clazz)
        }
    }
}

/**
 * Creates a nullable schema by using anyOf with the schema and null
 */
fun SchemaBuilder.nullable(block: SchemaBuilder.() -> Unit) {
    anyOf {
        schema(block)
        schema {
            type = SchemaType.NULL
        }
    }
}

/**
 * Creates a schema that extends another schema using allOf
 */
fun SchemaBuilder.extending(baseClass: KClass<*>, block: SchemaBuilder.() -> Unit = {}) {
    allOf {
        schema(baseClass)
        schema {
            type = SchemaType.OBJECT
            apply(block)
        }
    }
}

/**
 * Creates a schema that extends multiple schemas using allOf
 */
fun SchemaBuilder.extending(vararg baseClasses: KClass<*>, block: SchemaBuilder.() -> Unit = {}) {
    allOf {
        baseClasses.forEach { schema(it) }
        schema {
            type = SchemaType.OBJECT
            apply(block)
        }
    }
}

/**
 * Creates a oneOf schema from varargs of classes with optional discriminator
 */
fun SchemaBuilder.oneOfClasses(
    vararg classes: KClass<*>,
    discriminatorProperty: String? = null,
    discriminatorMappings: Map<String, KClass<*>>? = null,
) {
    oneOf(*classes)
    if (discriminatorProperty != null) {
        discriminator(discriminatorProperty) {
            discriminatorMappings?.forEach { (value, clazz) ->
                mapping(value, clazz)
            }
        }
    }
}

/**
 * Creates an allOf schema from varargs of classes
 */
fun SchemaBuilder.allOfClasses(vararg classes: KClass<*>, additionalProperties: SchemaBuilder.() -> Unit = {}) {
    allOf {
        classes.forEach { schema(it) }
        if (additionalProperties != {}) {
            schema {
                type = SchemaType.OBJECT
                apply(additionalProperties)
            }
        }
    }
}

/**
 * Creates a schema that represents a choice between types (useful for union types)
 */
fun SchemaBuilder.choice(block: OneOfBuilder.() -> Unit) {
    oneOf(block)
}

/**
 * Creates a schema that combines multiple schemas
 */
fun SchemaBuilder.combine(block: AllOfBuilder.() -> Unit) {
    allOf(block)
}

/**
 * Creates a schema for optional fields that can be either the type or null
 */
inline fun <reified T : Any> SchemaBuilder.optionalSchema() {
    anyOf {
        schema(T::class)
        schema {
            type = SchemaType.NULL
        }
    }
}