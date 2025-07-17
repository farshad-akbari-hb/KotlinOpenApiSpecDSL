package me.farshad.dsl.builder.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.reflect.full.createType

// Helper function to create JsonElement from any value
fun Any.toJsonElement(): JsonElement =
    when (this) {
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is Map<*, *> ->
            JsonObject(
                this
                    .mapKeys { it.key.toString() }
                    .mapValues { it.value?.toJsonElement() ?: JsonNull },
            )

        is List<*> -> JsonArray(this.map { it?.toJsonElement() ?: JsonNull })
        else -> {
            // Try to serialize as @Serializable object
            try {
                val json =
                    Json {
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
    val json =
        Json {
            prettyPrint = false
            encodeDefaults = false
            explicitNulls = false
        }
    return json.encodeToJsonElement(this)
}
