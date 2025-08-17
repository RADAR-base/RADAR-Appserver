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

package org.radarbase.appserver.jersey.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.radarbase.appserver.jersey.dto.protocol.ReferenceTimestamp
import org.radarbase.appserver.jersey.dto.protocol.ReferenceTimestampType

object ReferenceTimestampSerializer : KSerializer<ReferenceTimestamp> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        "ReferenceTimestamp"
    )

    override fun serialize(encoder: Encoder, value: ReferenceTimestamp) {
        encoder.encodeString(value.timestamp.toString())
    }

    override fun deserialize(decoder: Decoder): ReferenceTimestamp {
        val input = decoder as? JsonDecoder
            ?: throw IllegalStateException("Only works with JSON")

        val element = input.decodeJsonElement()
        return when (element) {
            is JsonObject -> {
                input.json.decodeFromJsonElement(ReferenceTimestamp.serializer(), element)
            }
            is JsonPrimitive -> {
                ReferenceTimestamp(element.content, ReferenceTimestampType.DATETIMEUTC)
            }
            else -> throw IllegalArgumentException("Unexpected JSON for ReferenceTimestamp: $element")
        }
    }
}
