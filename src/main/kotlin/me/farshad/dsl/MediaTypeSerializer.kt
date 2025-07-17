package me.farshad.dsl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

object MediaTypeSerializer : KSerializer<MediaType> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MediaType") {
        element<Schema?>("schema", isOptional = true)
        element<JsonElement?>("example", isOptional = true)
        element<Map<String, Example>?>("examples", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: MediaType) {
        encoder.encodeStructure(descriptor) {
            value.schema?.let { encodeSerializableElement(descriptor, 0, Schema.serializer(), it) }
            value.example?.let { 
                @Suppress("UNCHECKED_CAST")
                val serializer = encoder.serializersModule.getContextual(JsonElement::class) as? KSerializer<JsonElement> 
                    ?: JsonElement.serializer()
                encodeSerializableElement(descriptor, 1, serializer, it) 
            }
            value.examples?.let { examplesMap ->
                encodeSerializableElement(descriptor, 2, MapSerializer(
                    serializer<String>(),
                    Example.serializer()
                ), examplesMap)
            }
        }
    }

    override fun deserialize(decoder: Decoder): MediaType {
        var schema: Schema? = null
        var example: JsonElement? = null
        var examples: Map<String, Example>? = null

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> schema = decodeSerializableElement(descriptor, 0, Schema.serializer())
                    1 -> {
                        @Suppress("UNCHECKED_CAST")
                        val serializer = decoder.serializersModule.getContextual(JsonElement::class) as? KSerializer<JsonElement> 
                            ?: JsonElement.serializer()
                        example = decodeSerializableElement(descriptor, 1, serializer)
                    }
                    2 -> examples = decodeSerializableElement(descriptor, 2, MapSerializer(
                        serializer<String>(),
                        Example.serializer()
                    ))
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return MediaType(schema, example, examples)
    }
}