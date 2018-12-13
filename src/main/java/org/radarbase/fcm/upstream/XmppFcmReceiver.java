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
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.fcm.common.CcsClient;
import org.radarbase.fcm.common.ObjectMapperFactory;
import org.radarbase.fcm.downstream.FcmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.xmpp.XmppHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A Message receiver for receiving upstream messages from devices using FCM XMPP protocol.
 * Currently, FCM only supports upstream messages using the XMPP protocol.
 * This uses an inbound-adapter configured in classpath to receive messages.
 *
 * @author yatharthranjan
 */
@Slf4j
@Component("fcmReceiver")
public class XmppFcmReceiver implements CcsClient {

    @Autowired
    private UpstreamMessageHandler messageHandler;

    @Autowired
    private ObjectMapperFactory mapperFactory;

    @Autowired
    @Qualifier("fcmSenderProps")
    private FcmSender fcmSender;

    public void handleIncomingMessage(Message<String> message) throws Exception {
        log.debug("Header = " + message.getHeaders());
        log.debug("Payload = " + message.getPayload());
        if(message.getHeaders().get(XmppHeaders.TYPE) == org.jivesoftware.smack.packet.Message.Type.normal) {
            log.debug("Normal Message");
        }

        final ObjectMapper mapper = mapperFactory.getObject();

        // Effectively final
        var ref = new Object() {
            JsonNode tree = null;
        };

        @Cleanup JsonParser parser = mapper.getFactory().createParser(message.getPayload());
        ref.tree = mapper.readTree(parser);

        Optional<Object> from = Optional.ofNullable(message.getHeaders().get(XmppHeaders.FROM));

        from.ifPresent(fromHeader -> sendAck(fromHeader.toString(), ref.tree));

        final Optional<JsonNode> messageTypeObj = Optional.ofNullable(ref.tree.get("message_type"));

        messageTypeObj.ifPresentOrElse(messageTypeObj1 -> {
            final String messageType = messageTypeObj1.asText();
            log.info("Message Type : {}", messageType);
            switch (messageType) {
                case "ack":
                    messageHandler.handleAckReceipt(ref.tree);
                    break;
                case "nack":
                    messageHandler.handleNackReceipt(ref.tree);
                    break;
                case "receipt":
                    messageHandler.handleStatusReceipt(ref.tree);
                    break;
                case "control":
                    messageHandler.handleControlMessage(ref.tree);
                    break;
                default:
                    messageHandler.handleOthers(ref.tree);
                    log.info("Received unknown FCM message type: {}", messageType);
                    break;
            }
        }, () -> {
            // Normal upstream message from a device client
            // log.debug("Sent Message: " + fcmSender.send(message));
            messageHandler.handleUpstreamMessage(ref.tree);
        });


    }

    @SneakyThrows
    private void sendAck(String headerTo, JsonNode jsonMessage) {
        final Map<String, Object> map = new HashMap<>();
        map.put("message_type", "ack");
        map.put("to", jsonMessage.get("from").textValue());
        map.put("message_id", jsonMessage.get("message_id").textValue());

        String ackJson = mapperFactory.getObject().writeValueAsString(map);
        fcmSender.send(MessageBuilder.withPayload(ackJson)
                .setHeader(XmppHeaders.TO, headerTo).build());
    }
}
