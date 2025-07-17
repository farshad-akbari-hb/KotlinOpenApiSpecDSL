package me.farshad.dsl.builder


import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.charleskorn.kaml.*
import me.farshad.dsl.spec.Components
import me.farshad.dsl.spec.Contact
import me.farshad.dsl.spec.Discriminator
import me.farshad.dsl.spec.Example
import me.farshad.dsl.spec.Info
import me.farshad.dsl.spec.License
import me.farshad.dsl.spec.MediaType
import me.farshad.dsl.spec.OpenApiSpec
import me.farshad.dsl.spec.Operation
import me.farshad.dsl.spec.Parameter
import me.farshad.dsl.spec.ParameterLocation
import me.farshad.dsl.spec.PathItem
import me.farshad.dsl.spec.PropertyType
import me.farshad.dsl.spec.RequestBody
import me.farshad.dsl.spec.Response
import me.farshad.dsl.spec.Schema
import me.farshad.dsl.spec.SchemaFormat
import me.farshad.dsl.spec.SchemaReference
import me.farshad.dsl.spec.SchemaType
import me.farshad.dsl.spec.SecurityScheme
import me.farshad.dsl.spec.Server
import me.farshad.dsl.annotation.PropertyDescription
import me.farshad.dsl.annotation.SchemaDescription
import me.farshad.dsl.spec.inlineSchema
import me.farshad.dsl.spec.schemaRef
import me.farshad.dsl.serializer.yamlSerializersModule
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

// DSL Builder Functions
class OpenApiBuilder {
    var openapi: String = "3.1.0"
    private lateinit var info: Info
    private val servers = mutableListOf<Server>()
    private val paths = mutableMapOf<String, PathItem>()
    private var components: Components? = null

    fun info(block: InfoBuilder.() -> Unit) {
        info = InfoBuilder().apply(block).build()
    }

    fun server(url: String, block: ServerBuilder.() -> Unit = {}) {
        servers.add(ServerBuilder(url).apply(block).build())
    }

    fun paths(block: PathsBuilder.() -> Unit) {
        PathsBuilder(paths).apply(block)
    }

    fun components(block: ComponentsBuilder.() -> Unit) {
        components = ComponentsBuilder().apply(block).build()
    }

    fun build() = OpenApiSpec(
        openapi = openapi,
        info = info,
        servers = servers,
        paths = paths,
        components = components
    )
}

class InfoBuilder {
    lateinit var title: String
    lateinit var version: String
    var description: String? = null
    var termsOfService: String? = null
    private var contact: Contact? = null
    private var license: License? = null

    fun contact(block: ContactBuilder.() -> Unit) {
        contact = ContactBuilder().apply(block).build()
    }

    fun license(name: String, url: String? = null) {
        license = License(name, url)
    }

    fun build() = Info(title, version, description, termsOfService, contact, license)
}

class ContactBuilder {
    var name: String? = null
    var url: String? = null
    var email: String? = null

    fun build() = Contact(name, url, email)
}

class ServerBuilder(private val url: String) {
    var description: String? = null

    fun build() = Server(url, description)
}

class PathsBuilder(private val paths: MutableMap<String, PathItem>) {
    fun path(path: String, block: PathItemBuilder.() -> Unit) {
        paths[path] = PathItemBuilder().apply(block).build()
    }
}

class PathItemBuilder {
    private var get: Operation? = null
    private var post: Operation? = null
    private var put: Operation? = null
    private var delete: Operation? = null
    private var patch: Operation? = null

    fun get(block: OperationBuilder.() -> Unit) {
        get = OperationBuilder().apply(block).build()
    }

    fun post(block: OperationBuilder.() -> Unit) {
        post = OperationBuilder().apply(block).build()
    }

    fun put(block: OperationBuilder.() -> Unit) {
        put = OperationBuilder().apply(block).build()
    }

    fun delete(block: OperationBuilder.() -> Unit) {
        delete = OperationBuilder().apply(block).build()
    }

    fun patch(block: OperationBuilder.() -> Unit) {
        patch = OperationBuilder().apply(block).build()
    }

    fun build() = PathItem(get, post, put, delete, patch)
}

class OperationBuilder {
    var summary: String? = null
    var description: String? = null
    var operationId: String? = null
    private val tags = mutableListOf<String>()
    private val parameters = mutableListOf<Parameter>()
    private var requestBody: RequestBody? = null
    private val responses = mutableMapOf<String, Response>()
    private val security = mutableListOf<Map<String, List<String>>>()

    fun tags(vararg tagNames: String) {
        tags.addAll(tagNames)
    }

    fun parameter(
        name: String,
        location: ParameterLocation,
        type: PropertyType,
        required: Boolean = false,
        description: String? = null,
        format: SchemaFormat? = null
    ) {
        parameters.add(
            Parameter(
                name = name,
                location = location,
                required = required,
                description = description,
                schema = Schema(type = when(type) {
                    PropertyType.STRING -> SchemaType.STRING
                    PropertyType.NUMBER -> SchemaType.NUMBER
                    PropertyType.INTEGER -> SchemaType.INTEGER
                    PropertyType.BOOLEAN -> SchemaType.BOOLEAN
                    PropertyType.ARRAY -> SchemaType.ARRAY
                    PropertyType.OBJECT -> SchemaType.OBJECT
                    PropertyType.NULL -> SchemaType.NULL
                }, format = format)
            )
        )
    }

    fun requestBody(block: RequestBodyBuilder.() -> Unit) {
        requestBody = RequestBodyBuilder().apply(block).build()
    }

    fun response(code: String, description: String, block: ResponseBuilder.() -> Unit = {}) {
        responses[code] = ResponseBuilder(description).apply(block).build()
    }

    fun security(scheme: String, vararg scopes: String) {
        security.add(mapOf(scheme to scopes.toList()))
    }

    fun build() = Operation(
        tags = tags.takeIf { it.isNotEmpty() },
        summary = summary,
        description = description,
        operationId = operationId,
        parameters = parameters.takeIf { it.isNotEmpty() },
        requestBody = requestBody,
        responses = responses,
        security = security.takeIf { it.isNotEmpty() }
    )
}

class RequestBodyBuilder {
    var description: String? = null
    var required: Boolean = false
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

    fun build() = RequestBody(description, content, required)
}

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

class ExamplesBuilder {
    private val examples = mutableMapOf<String, Example>()

    fun example(name: String, block: ExampleBuilder.() -> Unit) {
        examples[name] = ExampleBuilder().apply(block).build()
    }

    fun example(name: String, value: Any, summary: String? = null, description: String? = null) {
        examples[name] = Example(summary, description, value.toJsonElement())
    }

    fun build() = examples.toMap()
}

class SchemaBuilder {
    var type: SchemaType? = null
    var format: SchemaFormat? = null
    var description: String? = null
    var properties = mutableMapOf<String, Schema>()
    private val required = mutableListOf<String>()
    var items: Schema? = null
    private var oneOfInternal: MutableList<SchemaReference>? = null
    private var allOf: MutableList<SchemaReference>? = null
    private var anyOf: MutableList<SchemaReference>? = null
    private var not: SchemaReference? = null
    private var discriminator: Discriminator? = null
    var example: JsonElement? = null
    private var examples: Map<String, Example>? = null
    
    // Backward compatibility: allow setting oneOf as List<String>
    var oneOf: List<String>?
        get() = oneOfInternal?.mapNotNull { 
            when (it) {
                is SchemaReference.Ref -> it.path
                else -> null
            }
        }
        set(value) {
            oneOfInternal = value?.map { SchemaReference.Ref(it) }?.toMutableList()
        }

    fun property(name: String, type: PropertyType, required: Boolean = false, block: SchemaBuilder.() -> Unit = {}) {
        properties[name] = SchemaBuilder().apply {
            this.type = when(type) {
                PropertyType.STRING -> SchemaType.STRING
                PropertyType.NUMBER -> SchemaType.NUMBER
                PropertyType.INTEGER -> SchemaType.INTEGER
                PropertyType.BOOLEAN -> SchemaType.BOOLEAN
                PropertyType.ARRAY -> SchemaType.ARRAY
                PropertyType.OBJECT -> SchemaType.OBJECT
                PropertyType.NULL -> SchemaType.NULL
            }
            block()
        }.build()
        if (required) {
            this.required.add(name)
        }
    }

    fun items(block: SchemaBuilder.() -> Unit) {
        items = SchemaBuilder().apply(block).build()
    }

    fun example(value: Any) {
        example = value.toJsonElement()
    }

    fun examples(block: ExamplesBuilder.() -> Unit) {
        examples = ExamplesBuilder().apply(block).build()
    }
    
    // OneOf DSL methods
    fun oneOf(vararg refs: String) {
        if (oneOfInternal == null) oneOfInternal = mutableListOf()
        oneOfInternal?.addAll(refs.map { ref ->
            if (ref.startsWith("#/")) {
                SchemaReference.Ref(ref)
            } else {
                SchemaReference.Ref("#/components/schemas/$ref")
            }
        })
    }
    
    fun oneOf(vararg classes: KClass<*>) {
        if (oneOfInternal == null) oneOfInternal = mutableListOf()
        oneOfInternal?.addAll(classes.map { schemaRef(it) })
    }
    
    fun oneOf(block: OneOfBuilder.() -> Unit) {
        if (oneOfInternal == null) oneOfInternal = mutableListOf()
        oneOfInternal?.addAll(OneOfBuilder().apply(block).build())
    }
    
    // AllOf DSL methods
    fun allOf(vararg refs: String) {
        if (allOf == null) allOf = mutableListOf()
        allOf?.addAll(refs.map { ref ->
            if (ref.startsWith("#/")) {
                SchemaReference.Ref(ref)
            } else {
                SchemaReference.Ref("#/components/schemas/$ref")
            }
        })
    }
    
    fun allOf(vararg classes: KClass<*>) {
        if (allOf == null) allOf = mutableListOf()
        allOf?.addAll(classes.map { schemaRef(it) })
    }
    
    fun allOf(block: AllOfBuilder.() -> Unit) {
        if (allOf == null) allOf = mutableListOf()
        allOf?.addAll(AllOfBuilder().apply(block).build())
    }
    
    // AnyOf DSL methods
    fun anyOf(vararg refs: String) {
        if (anyOf == null) anyOf = mutableListOf()
        anyOf?.addAll(refs.map { ref ->
            if (ref.startsWith("#/")) {
                SchemaReference.Ref(ref)
            } else {
                SchemaReference.Ref("#/components/schemas/$ref")
            }
        })
    }
    
    fun anyOf(vararg classes: KClass<*>) {
        if (anyOf == null) anyOf = mutableListOf()
        anyOf?.addAll(classes.map { schemaRef(it) })
    }
    
    fun anyOf(block: AnyOfBuilder.() -> Unit) {
        if (anyOf == null) anyOf = mutableListOf()
        anyOf?.addAll(AnyOfBuilder().apply(block).build())
    }
    
    // Not DSL methods
    fun not(ref: String) {
        not = if (ref.startsWith("#/")) {
            SchemaReference.Ref(ref)
        } else {
            SchemaReference.Ref("#/components/schemas/$ref")
        }
    }
    
    fun not(clazz: KClass<*>) {
        not = schemaRef(clazz)
    }
    
    fun not(block: SchemaBuilder.() -> Unit) {
        not = inlineSchema(block)
    }
    
    // Discriminator DSL
    fun discriminator(propertyName: String, block: DiscriminatorBuilder.() -> Unit = {}) {
        discriminator = DiscriminatorBuilder(propertyName).apply(block).build()
    }

    fun build() = Schema(
        type = type,
        format = format,
        properties = properties.takeIf { it.isNotEmpty() },
        required = required.takeIf { it.isNotEmpty() },
        items = items,
        oneOf = oneOfInternal?.toList(),
        allOf = allOf?.toList(),
        anyOf = anyOf?.toList(),
        not = not,
        discriminator = discriminator,
        description = description,
        example = example,
        examples = examples
    )
}

// Composition builders for type-safe schema composition
class OneOfBuilder {
    private val schemas = mutableListOf<SchemaReference>()
    
    fun schema(ref: String) {
        schemas.add(if (ref.startsWith("#/")) {
            SchemaReference.Ref(ref)
        } else {
            SchemaReference.Ref("#/components/schemas/$ref")
        })
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
        schemas.add(if (ref.startsWith("#/")) {
            SchemaReference.Ref(ref)
        } else {
            SchemaReference.Ref("#/components/schemas/$ref")
        })
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
        schemas.add(if (ref.startsWith("#/")) {
            SchemaReference.Ref(ref)
        } else {
            SchemaReference.Ref("#/components/schemas/$ref")
        })
    }
    
    fun schema(clazz: KClass<*>) {
        schemas.add(schemaRef(clazz))
    }
    
    fun schema(block: SchemaBuilder.() -> Unit) {
        schemas.add(inlineSchema(block))
    }
    
    fun build() = schemas.toList()
}

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

class ComponentsBuilder {
    private val schemas = mutableMapOf<String, Schema>()
    private val securitySchemes = mutableMapOf<String, SecurityScheme>()
    private val examples = mutableMapOf<String, Example>()

    fun schema(name: String, block: SchemaBuilder.() -> Unit) {
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
            val propType = when {
                prop.returnType.classifier == List::class -> PropertyType.ARRAY
                prop.returnType.classifier == String::class -> PropertyType.STRING
                prop.returnType.classifier == Int::class || prop.returnType.classifier == Long::class -> PropertyType.INTEGER
                prop.returnType.classifier == Double::class || prop.returnType.classifier == Float::class -> PropertyType.NUMBER
                prop.returnType.classifier == Boolean::class -> PropertyType.BOOLEAN
                else -> PropertyType.OBJECT
            }
            
            // Check for property-level PropertyDescription annotation
            val propertyDescription = prop.annotations.find { it is PropertyDescription }?.let { annotation ->
                (annotation as PropertyDescription).value
            }
            
            schemaBuilder.property(prop.name, propType, !prop.returnType.isMarkedNullable) {
                propertyDescription?.let { this.description = it }
            }
        }
        schemas[kClass.simpleName!!] = schemaBuilder.build()
    }

    fun securityScheme(name: String, type: String, scheme: String? = null, bearerFormat: String? = null) {
        securitySchemes[name] = SecurityScheme(type, scheme, bearerFormat)
    }

    fun example(name: String, block: ExampleBuilder.() -> Unit) {
        examples[name] = ExampleBuilder().apply(block).build()
    }

    fun example(name: String, value: Any, summary: String? = null, description: String? = null) {
        examples[name] = Example(summary, description, value.toJsonElement())
    }

    fun build() = Components(
        schemas = schemas.takeIf { it.isNotEmpty() },
        securitySchemes = securitySchemes.takeIf { it.isNotEmpty() },
        examples = examples.takeIf { it.isNotEmpty() }
    )
}

// DSL entry point
fun openApi(block: OpenApiBuilder.() -> Unit): OpenApiSpec {
    return OpenApiBuilder().apply(block).build()
}

// Helper function to create JsonElement from any value
fun Any.toJsonElement(): JsonElement = when (this) {
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Map<*, *> -> JsonObject(this.mapKeys { it.key.toString() }
        .mapValues { it.value?.toJsonElement() ?: JsonNull })
    is List<*> -> JsonArray(this.map { it?.toJsonElement() ?: JsonNull })
    else -> {
        // Try to serialize as @Serializable object
        try {
            val json = Json { 
                prettyPrint = false
                encodeDefaults = false
                explicitNulls = false 
            }
            // Try to get the serializer for this class dynamically
            val serializer = kotlinx.serialization.serializer(this::class.createType())
            @Suppress("UNCHECKED_CAST")
            json.encodeToJsonElement(serializer as KSerializer<Any>, this)
        } catch (e: Exception) {
            // Fallback to toString() if serialization fails
            JsonPrimitive(toString())
        }
    }
}

// Extension function to convert @Serializable objects to JsonElement
inline fun <reified T> T.toSerializableJsonElement(): JsonElement where T : Any {
    val json = Json { 
        prettyPrint = false
        encodeDefaults = false
        explicitNulls = false 
    }
    return json.encodeToJsonElement(this)
}

// Extension function to convert to JSON using kotlinx.serialization
fun OpenApiSpec.toJson(): String {
    val json = Json {
        prettyPrint = true
        encodeDefaults = false
        explicitNulls = false
    }
    return json.encodeToString(this)
}

// Extension function to convert to YAML using kaml
fun OpenApiSpec.toYaml(): String {
    val yaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = false,
            strictMode = false,
            breakScalarsAt = 80
        ),
        serializersModule = yamlSerializersModule
    )
    return yaml.encodeToString(this)
}

