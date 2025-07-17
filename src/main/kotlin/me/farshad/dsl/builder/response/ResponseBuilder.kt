package me.farshad.dsl.builder.response

import me.farshad.dsl.builder.schema.SchemaBuilder
import me.farshad.dsl.builder.utils.toJsonElement
import me.farshad.dsl.spec.MediaType
import me.farshad.dsl.spec.Response
import me.farshad.dsl.spec.Schema
import kotlin.reflect.KClass

class ResponseBuilder(private val description: String) {
    private val content = mutableMapOf<String, MediaType>()

    fun jsonContent(schemaRef: String? = null, block: SchemaBuilder.() -> Unit = {}) {
        val schema = if (schemaRef != null) {
            Schema(ref = "#/components/schemas/$schemaRef")
        } else {
            SchemaBuilder().apply(block).build()
        }
        content["application/json"] = MediaType(schema = schema)
    }

    fun jsonContent(schemaClass: KClass<*>) {
        val schemaName = schemaClass.simpleName
        content["application/json"] = MediaType(schema = Schema(ref = "#/components/schemas/$schemaName"))
    }

    fun jsonContent(schemaClass: KClass<*>, example: Any) {
        val schemaName = schemaClass.simpleName
        content["application/json"] = MediaType(
            schema = Schema(ref = "#/components/schemas/$schemaName"),
            example = example.toJsonElement()
        )
    }

    fun jsonContent(schemaRef: String? = null, example: Any, block: SchemaBuilder.() -> Unit = {}) {
        val schema = if (schemaRef != null) {
            Schema(ref = "#/components/schemas/$schemaRef")
        } else {
            SchemaBuilder().apply(block).build()
        }
        content["application/json"] = MediaType(schema = schema, example = example.toJsonElement())
    }

    fun example(value: Any) {
        val mediaType = content["application/json"]
        if (mediaType != null) {
            content["application/json"] = mediaType.copy(example = value.toJsonElement())
        }
    }

    fun examples(block: ExamplesBuilder.() -> Unit) {
        val mediaType = content["application/json"]
        if (mediaType != null) {
            val examplesMap = ExamplesBuilder().apply(block).build()
            content["application/json"] = mediaType.copy(examples = examplesMap, example = null)
        }
    }

    fun build() = Response(description, content.takeIf { it.isNotEmpty() })
}