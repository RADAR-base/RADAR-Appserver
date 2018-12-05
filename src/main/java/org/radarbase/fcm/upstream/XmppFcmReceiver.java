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

package org.radarbase.fcm.upstream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.radarbase.fcm.common.CcsClient;
import org.radarbase.fcm.common.ObjectMapperFactory;
import org.radarbase.fcm.downstream.FcmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.xmpp.XmppHeaders;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * @author yatharthranjan
 */
@Component("fcmReceiver")
public class XmppFcmReceiver implements CcsClient {

    private static final Logger logger = LoggerFactory.getLogger(XmppFcmReceiver.class);

    @Autowired
    private UpstreamMessageHandler messageHandler;

    @Autowired
    private ObjectMapperFactory mapperFactory;

    @Autowired
    @Qualifier("fcmSenderProps")
    FcmSender fcmSender;

    public void handleIncomingMessage(Message<String> message) throws Exception {
        logger.debug("Header = " + message.getHeaders());
        logger.debug("Payload = " + message.getPayload());
        if(message.getHeaders().get(XmppHeaders.TYPE) == org.jivesoftware.smack.packet.Message.Type.normal) {
            logger.debug("Normal Message");
        }

        final ObjectMapper mapper = mapperFactory.getObject();
        JsonNode tree = null;
        try (JsonParser parser = mapper.getFactory().createParser(message.getPayload())) {
            tree = mapper.readTree(parser);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Optional<JsonNode> messageTypeObj = Optional.ofNullable(tree.get("message_type"));

        if (!messageTypeObj.isPresent()) {
            // Normal upstream message from a device client
            // logger.debug("Sent Message: " + fcmSender.send(message));
            messageHandler.handleUpstreamMessage(tree);
            return;
        }

        final String messageType = messageTypeObj.get().asText();
        logger.info("Message Type : {}", messageType);
        switch (messageType) {
            case "ack":
                messageHandler.handleAckReceipt(tree);
                break;
            case "nack":
                messageHandler.handleNackReceipt(tree);
                break;
            case "receipt":
                messageHandler.handleStatusReceipt(tree);
                break;
            case "control":
                messageHandler.handleControlMessage(tree);
                break;
            default:
                messageHandler.handleOthers(tree);
                logger.info("Received unknown FCM message type: {}", messageType);
                break;
        }
    }
}
