package org.radarbase.appserver.jersey.utils.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import org.radarbase.appserver.jersey.dto.protocol.ReferenceTimestamp
import org.radarbase.appserver.jersey.dto.protocol.ReferenceTimestampType

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
