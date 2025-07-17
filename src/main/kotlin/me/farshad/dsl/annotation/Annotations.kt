package me.farshad.dsl.annotation

/**
 * Annotation to provide a description for a data class that will be used as an OpenAPI schema.
 * This description will be included in the generated OpenAPI specification.
 *
 * @property value The description text for the schema
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SchemaDescription(
    val value: String,
)

/**
 * Annotation to provide a description for a property of a data class used in OpenAPI schemas.
 * This description will be included in the generated OpenAPI specification for the property.
 *
 * @property value The description text for the property
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PropertyDescription(
    val value: String,
)
