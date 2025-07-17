package me.farshad.dsl.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement
import me.farshad.dsl.spec.Example

@Serializer(forClass = Example::class)
object ExampleSerializer : KSerializer<Example> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Example") {
        element<String?>("summary", isOptional = true)
        element<String?>("description", isOptional = true)
        element<JsonElement?>("value", isOptional = true)
        element<String?>("externalValue", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Example) {
        encoder.encodeStructure(descriptor) {
            value.summary?.let { encodeStringElement(descriptor, 0, it) }
            value.description?.let { encodeStringElement(descriptor, 1, it) }
            value.value?.let {
                @Suppress("UNCHECKED_CAST")
                val serializer =
                    encoder.serializersModule.getContextual(JsonElement::class) as? KSerializer<JsonElement>
                        ?: JsonElement.serializer()
                encodeSerializableElement(descriptor, 2, serializer, it)
            }
            value.externalValue?.let { encodeStringElement(descriptor, 3, it) }
        }
    }

    override fun deserialize(decoder: Decoder): Example {
        var summary: String? = null
        var description: String? = null
        var value: JsonElement? = null
        var externalValue: String? = null

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> summary = decodeStringElement(descriptor, 0)
                    1 -> description = decodeStringElement(descriptor, 1)
                    2 -> {
                        @Suppress("UNCHECKED_CAST")
                        val serializer =
                            decoder.serializersModule.getContextual(JsonElement::class) as? KSerializer<JsonElement>
                                ?: JsonElement.serializer()
                        value = decodeSerializableElement(descriptor, 2, serializer)
                    }

                    3 -> externalValue = decodeStringElement(descriptor, 3)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return Example(summary, description, value, externalValue)
    }
}