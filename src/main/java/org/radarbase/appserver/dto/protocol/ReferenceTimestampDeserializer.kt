/*
 * Copyright 2018 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.dto.protocol

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper

class ReferenceTimestampDeserializer : JsonDeserializer<Any>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): ReferenceTimestamp {
        return if (parser.currentToken == JsonToken.START_OBJECT) {
            val mapper = ObjectMapper()
            mapper.readValue(parser, ReferenceTimestamp::class.java)
        } else {
            ReferenceTimestamp(parser.valueAsString, ReferenceTimestampType.DATETIMEUTC)
        }
    }
}
