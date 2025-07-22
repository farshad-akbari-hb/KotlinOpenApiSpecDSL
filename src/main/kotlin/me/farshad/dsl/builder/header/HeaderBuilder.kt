package me.farshad.dsl.builder.header

import me.farshad.dsl.builder.example.ExamplesBuilder
import me.farshad.dsl.builder.schema.SchemaBuilder
import me.farshad.dsl.builder.utils.toJsonElement
import me.farshad.dsl.spec.Header
import me.farshad.dsl.spec.Schema

class HeaderBuilder {
    var description: String? = null
    var required: Boolean = false
    var deprecated: Boolean = false
    private var schema: Schema? = null
    private var example: Any? = null
    private var examples: ExamplesBuilder? = null

    fun schema(block: SchemaBuilder.() -> Unit) {
        schema = SchemaBuilder().apply(block).build()
    }

    fun schema(ref: String) {
        schema = Schema(ref = "#/components/schemas/$ref")
    }

    fun example(value: Any) {
        example = value
        examples = null // Clear examples if setting a single example
    }

    fun examples(block: ExamplesBuilder.() -> Unit) {
        examples = ExamplesBuilder().apply(block)
        example = null // Clear single example if setting multiple examples
    }

    fun build() = Header(
        description = description,
        required = required,
        deprecated = deprecated,
        schema = schema,
        example = example?.toJsonElement(),
        examples = examples?.build()
    )
}