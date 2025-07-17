package me.farshad.dsl.spec

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.farshad.dsl.serializer.ExampleSerializer
import me.farshad.dsl.serializer.MediaTypeSerializer


// OpenAPI DSL Classes with kotlinx.serialization support
@Serializable
data class OpenApiSpec(
    val openapi: String,
    val info: Info,
    val servers: List<Server> = emptyList(),
    val paths: Map<String, PathItem> = emptyMap(),
    val components: Components? = null
)

@Serializable
data class Info(
    val title: String,
    val version: String,
    val description: String? = null,
    val termsOfService: String? = null,
    val contact: Contact? = null,
    val license: License? = null
)

@Serializable
data class Contact(
    val name: String? = null,
    val url: String? = null,
    val email: String? = null
)

@Serializable
data class License(
    val name: String,
    val url: String? = null
)

@Serializable
data class Server(
    val url: String,
    val description: String? = null,
    val variables: Map<String, ServerVariable>? = null
)

@Serializable
data class ServerVariable(
    val default: String,
    @SerialName("enum")
    val enumValues: List<String>? = null,
    val description: String? = null
)

@Serializable
data class PathItem(
    val get: Operation? = null,
    val post: Operation? = null,
    val put: Operation? = null,
    val delete: Operation? = null,
    val patch: Operation? = null,
    val summary: String? = null,
    val description: String? = null
)

@Serializable
data class Operation(
    val tags: List<String>? = null,
    val summary: String? = null,
    val description: String? = null,
    val operationId: String? = null,
    val parameters: List<Parameter>? = null,
    val requestBody: RequestBody? = null,
    val responses: Map<String, Response>,
    val security: List<Map<String, List<String>>>? = null
)

@Serializable
enum class ParameterLocation {
    @SerialName("query")
    QUERY,
    @SerialName("header")
    HEADER,
    @SerialName("path")
    PATH,
    @SerialName("cookie")
    COOKIE
}

@Serializable
enum class SchemaType {
    @SerialName("string")
    STRING,
    @SerialName("number")
    NUMBER,
    @SerialName("integer")
    INTEGER,
    @SerialName("boolean")
    BOOLEAN,
    @SerialName("array")
    ARRAY,
    @SerialName("object")
    OBJECT,
    @SerialName("null")
    NULL
}

@Serializable
enum class PropertyType {
    @SerialName("string")
    STRING,
    @SerialName("number")
    NUMBER,
    @SerialName("integer")
    INTEGER,
    @SerialName("boolean")
    BOOLEAN,
    @SerialName("array")
    ARRAY,
    @SerialName("object")
    OBJECT,
    @SerialName("null")
    NULL
}

@Serializable
enum class SchemaFormat {
    @SerialName("int32")
    INT32,
    @SerialName("int64")
    INT64,
    @SerialName("date-time")
    DATE_TIME,
    @SerialName("email")
    EMAIL,
    @SerialName("password")
    PASSWORD,
    @SerialName("url")
    URL
}

@Serializable
data class Parameter(
    val name: String,
    @SerialName("in")
    val location: ParameterLocation,
    val description: String? = null,
    val required: Boolean = false,
    val schema: Schema? = null,
    val example: JsonElement? = null,
    val examples: Map<String, Example>? = null
)

@Serializable
data class RequestBody(
    val description: String? = null,
    val content: Map<String, MediaType>,
    val required: Boolean = false
)

@Serializable
data class Response(
    val description: String,
    val content: Map<String, MediaType>? = null
)

@Serializable(with = ExampleSerializer::class)
data class Example(
    val summary: String? = null,
    val description: String? = null,
    val value: JsonElement? = null,
    val externalValue: String? = null
)

@Serializable(with = MediaTypeSerializer::class)
data class MediaType(
    val schema: Schema? = null,
    val example: JsonElement? = null,
    val examples: Map<String, Example>? = null
)

@Serializable
data class Schema(
    val type: SchemaType? = null,
    val format: SchemaFormat? = null,
    val properties: Map<String, Schema>? = null,
    val required: List<String>? = null,
    val items: Schema? = null,
    @SerialName("\$ref")
    val ref: String? = null,
    @SerialName("enum")
    val enumValues: List<JsonElement>? = null,
    val oneOf: List<SchemaReference>? = null,
    val allOf: List<SchemaReference>? = null,
    val anyOf: List<SchemaReference>? = null,
    val not: SchemaReference? = null,
    val discriminator: Discriminator? = null,
    val description: String? = null,
    val example: JsonElement? = null,
    val examples: Map<String, Example>? = null
)

@Serializable
data class Discriminator(
    val propertyName: String,
    val mapping: Map<String, String>? = null
)

@Serializable
data class Components(
    val schemas: Map<String, Schema>? = null,
    val securitySchemes: Map<String, SecurityScheme>? = null,
    val examples: Map<String, Example>? = null
)

@Serializable
data class SecurityScheme(
    val type: String,
    val scheme: String? = null,
    val bearerFormat: String? = null,
    val description: String? = null
)