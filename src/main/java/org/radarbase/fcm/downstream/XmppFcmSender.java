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

package org.radarbase.fcm.downstream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.radarbase.fcm.common.CcsClient;
import org.radarbase.fcm.common.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.xmpp.XmppHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author yatharthranjan
 */
@Component
public class XmppFcmSender implements CcsClient, FcmSender {

    @Autowired
    private MessageChannel xmppOutbound;

    @Autowired
    private ObjectMapperFactory mapperFactory;

    @Override
    public Message<String> send(Message<?> message) throws Exception {
        //System.out.println(message.getHeaders());
        //System.out.println(message.getPayload());

        ObjectMapper mapper = mapperFactory.getObject();
        JsonParser parser;
        JsonNode tree = null;
        try {
            parser = mapper.getFactory().createParser(message.getPayload().toString());
            tree = mapper.readTree(parser);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String data = mapper.writeValueAsString(tree.get("data"));
            System.out.println("NodeType : " + tree.get("data").getNodeType().name());
            System.out.println("DATA : " + data);
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("time_to_live", 900);
            map.put("message_id", UUID.randomUUID());
            map.put("to", tree.get("from").textValue());
            Message<String> xmppOutboundMsg = MessageBuilder.withPayload(mapper.writeValueAsString(map))
                    .setHeader(XmppHeaders.TO, message.getHeaders().get(XmppHeaders.FROM))
                    .build();
            xmppOutbound.send(xmppOutboundMsg);

            return xmppOutboundMsg;
        } catch (JsonProcessingException exc) {
            exc.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean doesProvideDeliveryReceipt() {
        return true;
    }
}
