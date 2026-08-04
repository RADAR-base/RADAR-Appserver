/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.util.Base64;

public class Base64Deserializer extends JsonDeserializer<String> implements ContextualDeserializer {
  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) throws JsonMappingException {
    if (!String.class.isAssignableFrom(property.getType().getRawClass())) {
      throw context.invalidTypeIdException(property.getType(), "String", "Base64 decoding is only applied to String fields.");
    }
    return this;
  }

  @Override
  public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    String value = clean(parser.getValueAsString());
    Base64.Decoder decoder = Base64.getDecoder();

    try {
      byte[] decodedValue = decoder.decode(value);
      return new String(decodedValue);
    } catch (IllegalArgumentException e) {
      String fieldName = parser.getParsingContext().getCurrentName();
      Class<?> wrapperClass = parser.getParsingContext().getCurrentValue().getClass();

      throw new InvalidFormatException(
              parser,
              String.format("Value for '%s' is not a base64 encoded JSON", fieldName),
              value,
              wrapperClass
      );
    }
  }

  public String clean(String value) {
    return value.replaceAll("[\n\r]", "");
  }
}
