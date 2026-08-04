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

package org.radarbase.appserver.dto.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * @author yatharthranjan
 */
public class ReferenceTimestampDeserializer extends JsonDeserializer<Object> {

    @Override
    public ReferenceTimestamp deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        ReferenceTimestamp response;

        if (JsonToken.START_OBJECT.equals(parser.getCurrentToken())) {
            ObjectMapper mapper = new ObjectMapper();
            response = mapper.readValue(parser, ReferenceTimestamp.class);
        } else
            response = new ReferenceTimestamp(parser.getValueAsString(), ReferenceTimestampType.DATETIMEUTC);

        return response;
    }
}
