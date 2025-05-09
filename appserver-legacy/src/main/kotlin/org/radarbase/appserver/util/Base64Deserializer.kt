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

package org.radarbase.appserver.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import java.util.*

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
        } catch (e: IllegalArgumentException) {
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
