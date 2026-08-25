package intelbras.mobi.smart.domain.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface IntCoded {
    val code: Int
}

abstract class IntCodedSerializer<T>(
    serialName: String,
    private val values: Array<T>,
) : KSerializer<T> where T : Enum<T>, T : IntCoded {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeInt(value.code)

    override fun deserialize(decoder: Decoder): T {
        val code = decoder.decodeInt()
        return values.firstOrNull { it.code == code }
            ?: throw SerializationException("Código $code não é válido para ${descriptor.serialName}")
    }
}
