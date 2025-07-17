package me.farshad.dsl.builder.schema

import kotlin.reflect.KClass

// Composition builders for type-safe schema composition
class OneOfBuilder {
    private val schemas = mutableListOf<SchemaReference>()

    fun schema(ref: String) {
        schemas.add(
            if (ref.startsWith("#/")) {
                SchemaReference.Ref(ref)
            } else {
                SchemaReference.Ref("#/components/schemas/$ref")
            },
        )
    }

    fun schema(clazz: KClass<*>) {
        schemas.add(schemaRef(clazz))
    }

    fun schema(block: SchemaBuilder.() -> Unit) {
        schemas.add(inlineSchema(block))
    }

    fun build() = schemas.toList()
}

class AllOfBuilder {
    private val schemas = mutableListOf<SchemaReference>()

    fun schema(ref: String) {
        schemas.add(
            if (ref.startsWith("#/")) {
                SchemaReference.Ref(ref)
            } else {
                SchemaReference.Ref("#/components/schemas/$ref")
            },
        )
    }

    fun schema(clazz: KClass<*>) {
        schemas.add(schemaRef(clazz))
    }

    fun schema(block: SchemaBuilder.() -> Unit) {
        schemas.add(inlineSchema(block))
    }

    fun build() = schemas.toList()
}

class AnyOfBuilder {
    private val schemas = mutableListOf<SchemaReference>()

    fun schema(ref: String) {
        schemas.add(
            if (ref.startsWith("#/")) {
                SchemaReference.Ref(ref)
            } else {
                SchemaReference.Ref("#/components/schemas/$ref")
            },
        )
    }

    fun schema(clazz: KClass<*>) {
        schemas.add(schemaRef(clazz))
    }

    fun schema(block: SchemaBuilder.() -> Unit) {
        schemas.add(inlineSchema(block))
    }

    fun build() = schemas.toList()
}
