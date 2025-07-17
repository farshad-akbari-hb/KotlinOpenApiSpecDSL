package me.farshad.dsl.builder.example

import kotlinx.serialization.json.JsonElement
import me.farshad.dsl.builder.utils.toJsonElement
import me.farshad.dsl.spec.Example

class ExampleBuilder {
    var summary: String? = null
    var description: String? = null
    var value: JsonElement? = null
    var externalValue: String? = null

    fun value(obj: Any) {
        value = obj.toJsonElement()
    }

    fun build() = Example(summary, description, value, externalValue)
}
