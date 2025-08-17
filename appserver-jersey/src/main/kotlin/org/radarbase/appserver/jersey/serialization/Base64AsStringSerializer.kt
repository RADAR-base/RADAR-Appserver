package org.radarbase.appserver.jersey.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Base64

object Base64AsStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Base64AsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        // when writing JSON, we must re-encode into Base64
        val encoded = Base64.getEncoder().encodeToString(value.toByteArray())
        encoder.encodeString(encoded)
    }

    override fun deserialize(decoder: Decoder): String {
        val raw = decoder.decodeString().replace(Regex("[\n\r]"), "")
        return try {
            val decodedBytes = Base64.getDecoder().decode(raw)
            String(decodedBytes)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid base64 value: $raw", e)
        }
    }
}
