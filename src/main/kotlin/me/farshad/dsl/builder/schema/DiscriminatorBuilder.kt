package me.farshad.dsl.builder.schema

import me.farshad.dsl.spec.Discriminator
import kotlin.reflect.KClass

class DiscriminatorBuilder(private val propertyName: String) {
    private val mapping = mutableMapOf<String, String>()

    fun mapping(value: String, schemaRef: String) {
        mapping[value] = schemaRef
    }

    fun mapping(value: String, clazz: KClass<*>) {
        mapping[value] = "#/components/schemas/${clazz.simpleName}"
    }

    fun build() = Discriminator(propertyName, mapping.takeIf { it.isNotEmpty() })
}