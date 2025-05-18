package org.radarbase.appserver.jersey.utils.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import java.util.Base64

class Base64Deserializer : JsonDeserializer<String>(), ContextualDeserializer {
    override fun createContextual(
        context: DeserializationContext,
        property: BeanProperty,
    ): JsonDeserializer<*> {
        if (!String::class.java.isAssignableFrom(property.type.rawClass)) {
            throw context.invalidTypeIdException(
                property.type,
                "String",
                "Base64 decoding is only applied to String fields.",
            )
        }
        return this
    }

    override fun deserialize(parser: JsonParser, context: DeserializationContext): String {
        val value = clean(parser.valueAsString)
        val decoder = Base64.getDecoder()

        return try {
            val decodedValue = decoder.decode(value)
            String(decodedValue)
        } catch (_: IllegalArgumentException) {
            val fieldName = parser.parsingContext.currentName
            val wrapperClass = parser.parsingContext.currentValue.javaClass

            throw InvalidFormatException(
                parser,
                "Value for '$fieldName' is not a base64 encoded JSON",
                value,
                wrapperClass,
            )
        }
    }

    private fun clean(value: String): String {
        return value.replace(Regex("[\n\r]"), "")
    }
}
