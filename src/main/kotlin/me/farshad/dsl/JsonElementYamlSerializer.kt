package me.farshad.dsl

import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

@OptIn(InternalSerializationApi::class)
object JsonElementYamlSerializer : KSerializer<JsonElement> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("JsonElement", PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: JsonElement) {
        when (value) {
            is JsonNull -> encoder.encodeNull()
            is JsonPrimitive -> {
                when {
                    value.isString -> encoder.encodeString(value.content)
                    value.booleanOrNull != null -> encoder.encodeBoolean(value.boolean)
                    value.longOrNull != null -> encoder.encodeLong(value.long)
                    value.doubleOrNull != null -> encoder.encodeDouble(value.double)
                    else -> encoder.encodeString(value.content)
                }
            }
            is JsonArray -> {
                encoder.encodeSerializableValue(ListSerializer(JsonElementYamlSerializer), value.toList())
            }
            is JsonObject -> {
                encoder.encodeSerializableValue(MapSerializer(String.serializer(), JsonElementYamlSerializer), value.toMap())
            }
        }
    }

    override fun deserialize(decoder: Decoder): JsonElement {
        // This is a simplified version - in production you'd need full implementation
        throw NotImplementedError("Deserialization not implemented for YAML")
    }
}

// Create a custom serializers module for YAML
val yamlSerializersModule = SerializersModule {
    contextual(JsonElementYamlSerializer)
}