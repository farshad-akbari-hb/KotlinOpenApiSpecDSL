package me.farshad.dsl.builder.components

import me.farshad.dsl.annotation.PropertyDescription
import me.farshad.dsl.annotation.SchemaDescription
import me.farshad.dsl.builder.response.ExampleBuilder
import me.farshad.dsl.builder.schema.SchemaBuilder
import me.farshad.dsl.builder.utils.toJsonElement
import me.farshad.dsl.spec.Components
import me.farshad.dsl.spec.Example
import me.farshad.dsl.spec.PropertyType
import me.farshad.dsl.spec.Schema
import me.farshad.dsl.spec.SchemaType
import me.farshad.dsl.spec.SecurityScheme
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

class ComponentsBuilder {
    private val schemas = mutableMapOf<String, Schema>()
    private val securitySchemes = mutableMapOf<String, SecurityScheme>()
    private val examples = mutableMapOf<String, Example>()

    fun schema(
        name: String,
        block: SchemaBuilder.() -> Unit,
    ) {
        schemas[name] = SchemaBuilder().apply(block).build()
    }

    fun schema(kClass: KClass<*>) {
        val schemaBuilder = SchemaBuilder()
        schemaBuilder.type = SchemaType.OBJECT

        // Check for class-level ApiDescription annotation
        kClass.annotations.find { it is SchemaDescription }?.let { annotation ->
            schemaBuilder.description = (annotation as SchemaDescription).value
        }

        kClass.declaredMemberProperties.forEach { prop ->
            val propType =
                when {
                    prop.returnType.classifier == List::class -> PropertyType.ARRAY
                    prop.returnType.classifier == String::class -> PropertyType.STRING
                    prop.returnType.classifier == Int::class || prop.returnType.classifier == Long::class -> PropertyType.INTEGER
                    prop.returnType.classifier == Double::class || prop.returnType.classifier == Float::class -> PropertyType.NUMBER
                    prop.returnType.classifier == Boolean::class -> PropertyType.BOOLEAN
                    else -> PropertyType.OBJECT
                }

            // Check for property-level PropertyDescription annotation
            val propertyDescription =
                prop.annotations.find { it is PropertyDescription }?.let { annotation ->
                    (annotation as PropertyDescription).value
                }

            schemaBuilder.property(prop.name, propType, !prop.returnType.isMarkedNullable) {
                propertyDescription?.let { this.description = it }
            }
        }
        schemas[kClass.simpleName!!] = schemaBuilder.build()
    }

    fun securityScheme(
        name: String,
        type: String,
        scheme: String? = null,
        bearerFormat: String? = null,
    ) {
        securitySchemes[name] = SecurityScheme(type, scheme, bearerFormat)
    }

    fun example(
        name: String,
        block: ExampleBuilder.() -> Unit,
    ) {
        examples[name] = ExampleBuilder().apply(block).build()
    }

    fun example(
        name: String,
        value: Any,
        summary: String? = null,
        description: String? = null,
    ) {
        examples[name] = Example(summary, description, value.toJsonElement())
    }

    fun build() =
        Components(
            schemas = schemas.takeIf { it.isNotEmpty() },
            securitySchemes = securitySchemes.takeIf { it.isNotEmpty() },
            examples = examples.takeIf { it.isNotEmpty() },
        )
}
