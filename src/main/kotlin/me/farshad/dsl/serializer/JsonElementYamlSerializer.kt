package me.farshad.dsl.serializer

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

@OptIn(InternalSerializationApi::class)
object JsonElementYamlSerializer : KSerializer<JsonElement> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("JsonElement", PolymorphicKind.SEALED)

    override fun serialize(
        encoder: Encoder,
        value: JsonElement,
    ) {
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
                encoder.encodeSerializableValue(
                    MapSerializer(String.serializer(), JsonElementYamlSerializer),
                    value.toMap(),
                )
            }
        }
    }

    override fun deserialize(decoder: Decoder): JsonElement {
        // This is a simplified version - in production you'd need full implementation
        throw NotImplementedError("Deserialization not implemented for YAML")
    }
}

// Create a custom serializers module for YAML
val yamlSerializersModule =
    SerializersModule {
        contextual(JsonElementYamlSerializer)
    }
