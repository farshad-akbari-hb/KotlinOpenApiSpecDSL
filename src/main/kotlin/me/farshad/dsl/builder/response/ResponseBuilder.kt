package me.farshad.dsl.builder.response

import me.farshad.dsl.builder.example.ExamplesBuilder
import me.farshad.dsl.builder.header.HeaderBuilder
import me.farshad.dsl.builder.schema.SchemaBuilder
import me.farshad.dsl.builder.utils.toJsonElement
import me.farshad.dsl.spec.Header
import me.farshad.dsl.spec.MediaType
import me.farshad.dsl.spec.Response
import me.farshad.dsl.spec.Schema
import me.farshad.dsl.spec.SchemaType
import kotlin.reflect.KClass

class ResponseBuilder(
    private val description: String,
) {
    private val content = mutableMapOf<String, MediaType>()
    private val headers = mutableMapOf<String, Header>()

    fun jsonContent(
        schemaRef: String? = null,
        block: SchemaBuilder.() -> Unit = {},
    ) {
        val schema =
            if (schemaRef != null) {
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

    fun jsonContent(
        schemaClass: KClass<*>,
        example: Any,
    ) {
        val schemaName = schemaClass.simpleName
        content["application/json"] =
            MediaType(
                schema = Schema(ref = "#/components/schemas/$schemaName"),
                example = example.toJsonElement(),
            )
    }

    fun jsonContent(
        schemaRef: String? = null,
        example: Any,
        block: SchemaBuilder.() -> Unit = {},
    ) {
        val schema =
            if (schemaRef != null) {
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

    fun header(
        name: String,
        description: String? = null,
        block: HeaderBuilder.() -> Unit = {},
    ) {
        headers[name] = HeaderBuilder().apply {
            this.description = description
            block()
        }.build()
    }

    fun header(
        name: String,
        description: String? = null,
        type: SchemaType,
        required: Boolean = false,
    ) {
        headers[name] = HeaderBuilder().apply {
            this.description = description
            this.required = required
            schema {
                this.type = type
            }
        }.build()
    }

    fun header(
        name: String,
        ref: String,
        description: String? = null,
        required: Boolean = false,
    ) {
        headers[name] = HeaderBuilder().apply {
            this.description = description
            this.required = required
            schema(ref)
        }.build()
    }

    fun build() = Response(
        description = description,
        content = content.takeIf { it.isNotEmpty() },
        headers = headers.takeIf { it.isNotEmpty() }
    )
}
