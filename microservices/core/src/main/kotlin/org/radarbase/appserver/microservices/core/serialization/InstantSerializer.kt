/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.microservices.core.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.floor

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer only works with JSON")
        val element = jsonDecoder.decodeJsonElement().jsonPrimitive

        return when {
            element.isString -> Instant.parse(element.content) 
            element.doubleOrNull != null -> {
                val d = element.double
                val secs = floor(d).toLong()
                val nanos = ((d - secs) * 1e9).toLong()
                Instant.ofEpochSecond(secs, nanos)
            }
            else -> throw IllegalArgumentException("Unsupported Instant format: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        val truncated = value.truncatedTo(ChronoUnit.MILLIS)
        encoder.encodeString(truncated.toString())
    }
}
