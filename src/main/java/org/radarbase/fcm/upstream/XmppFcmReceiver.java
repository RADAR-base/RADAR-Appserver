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
import org.radarbase.fcm.config.ReconnectionEnabledXmppConnectionFactoryBean;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.model.FcmAckMessage;
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

    @Autowired
    private ReconnectionEnabledXmppConnectionFactoryBean connectionFactoryBean;

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

        final Optional<Object> from = Optional.ofNullable(message.getHeaders().get(XmppHeaders.FROM));
        from.ifPresent(fromHeader -> sendAck(ref.tree));

        final Optional<JsonNode> messageTypeObj = Optional.ofNullable(ref.tree.get("message_type"));

        messageTypeObj.ifPresentOrElse(messageTypeObj1 -> handleMessageTypes(messageTypeObj1, ref.tree),
                () -> // Normal upstream message from a device client
            messageHandler.handleUpstreamMessage(ref.tree)
        );
    }

    private void handleMessageTypes(JsonNode messageTypeObj, JsonNode tree) {
        final String messageType = messageTypeObj.asText();
        log.info("Message Type : {}", messageType);
        switch (FcmMessageType.valueOf(messageType.toUpperCase())) {
            case ACK:
                messageHandler.handleAckReceipt(tree);
                break;
            case NACK:
                Optional<String> errorCodeObj = Optional.ofNullable(tree.get("error").asText());
                if (errorCodeObj.isEmpty()) {
                    log.error("Received null FCM Error Code.");
                    return;
                }

                final String errorCode = errorCodeObj.get();

                switch (errorCode) {
                    case "INVALID_JSON":
                    case "BAD_REGISTRATION":
                    case "BAD_ACK":
                    case "TOPICS_MESSAGE_RATE_EXCEEDED":
                    case "DEVICE_MESSAGE_RATE_EXCEEDED":
                        log.info("Device error: {} -> {}", tree.get("error"), tree.get("error_description"));
                        break;
                    case "SERVICE_UNAVAILABLE":
                    case "INTERNAL_SERVER_ERROR":
                        log.info("Server error: {} -> {}", tree.get("error"), tree.get("error_description"));
                        break;
                    case "CONNECTION_DRAINING":
                        log.info("Connection draining from Nack ...");
                        handleConnectionDraining();
                        break;
                    case "DEVICE_UNREGISTERED":
                        log.info("Received unknown FCM Error Code: {}", errorCode);
                        break;
                    default:
                        log.info("Received unknown FCM Error Code: {}", errorCode);
                        break;
                }
                messageHandler.handleNackReceipt(tree);
                break;
            case RECEIPT:
                messageHandler.handleStatusReceipt(tree);
                break;
            case CONTROL:
                handleControlMessage(tree);
                messageHandler.handleControlMessage(tree);
                break;
            default:
                messageHandler.handleOthers(tree);
                log.info("Received unknown FCM message type: {}", messageType);
                break;
        }
    }

    private void handleConnectionDraining() {
        log.info("FCM Connection is draining!");
        connectionFactoryBean.setIsConnectionDraining(true);
    }

    /**
     * Handles a Control message from FCM
     */
    private void handleControlMessage(JsonNode jsonNode) {
        final String controlType = jsonNode.get("control_type").asText();

        if (controlType.equals("CONNECTION_DRAINING")) {
            handleConnectionDraining();
        } else {
            log.info("Received unknown FCM Control message: {}", controlType);
        }
    }

    @SneakyThrows
    private void sendAck(JsonNode jsonMessage) {

        FcmAckMessage ackMessage = FcmAckMessage.builder()
                .messageId(jsonMessage.get("from").textValue())
                .to(jsonMessage.get("from").textValue())
                .build();
        fcmSender.send(ackMessage);
    }
}
