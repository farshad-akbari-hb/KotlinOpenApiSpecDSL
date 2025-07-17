package me.farshad.dsl.builder.response

import me.farshad.dsl.builder.utils.toJsonElement
import me.farshad.dsl.spec.Example

class ExamplesBuilder {
    private val examples = mutableMapOf<String, Example>()

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

    fun build() = examples.toMap()
}
