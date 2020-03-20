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
import java.util.Locale;
import java.util.Optional;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.fcm.common.CcsClient;
import org.radarbase.fcm.config.ReconnectionEnabledXmppConnectionFactoryBean;
import org.radarbase.fcm.downstream.XmppFcmSender;
import org.radarbase.fcm.model.FcmAckMessage;
import org.radarbase.fcm.upstream.error.ErrorHandlingStrategy;
import org.springframework.integration.xmpp.XmppHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * A Message receiver for receiving upstream messages from devices using FCM XMPP protocol.
 * Currently, FCM only supports upstream messages using the XMPP protocol. This uses an
 * inbound-adapter configured in classpath to receive messages.
 *
 * @author yatharthranjan
 */
@Slf4j
@Component("fcmReceiver")
public class XmppFcmReceiver implements CcsClient {

  // TODO add support for subscribing to publisher updates added in Java 9

  private final transient UpstreamMessageHandler messageHandler;

  private final transient ObjectMapper mapper;

  private final transient ErrorHandlingStrategy errorHandlingStrategy;
  private final transient ReconnectionEnabledXmppConnectionFactoryBean connectionFactoryBean;
  // We need to use an Xmpp sender here as we need to send ACK messages back to FCM server which is
  // not possible with Admin SDK.
  private transient XmppFcmSender fcmSender;

  public XmppFcmReceiver(
      UpstreamMessageHandler messageHandler,
      ObjectMapper mapper,
      ErrorHandlingStrategy errorHandlingStrategy,
      ReconnectionEnabledXmppConnectionFactoryBean connectionFactoryBean,
      XmppFcmSender xmppFcmSender) {
    this.messageHandler = messageHandler;
    this.mapper = mapper;
    this.errorHandlingStrategy = errorHandlingStrategy;
    this.connectionFactoryBean = connectionFactoryBean;
    this.fcmSender = xmppFcmSender;
  }

  public void handleIncomingMessage(Message<String> message) throws Exception {
    log.debug("Header = " + message.getHeaders());
    log.debug("Payload = " + message.getPayload());
    if (message.getHeaders().get(XmppHeaders.TYPE)
        == org.jivesoftware.smack.packet.Message.Type.normal) {
      log.debug("Normal Message");
    }

    @Cleanup JsonParser parser = mapper.getFactory().createParser(message.getPayload());
    final JsonNode tree = mapper.readTree(parser);

    final Optional<Object> from = Optional.ofNullable(tree.get("from"));
    from.ifPresent(fromHeader -> sendAck(tree));

    final Optional<JsonNode> messageTypeObj = Optional.ofNullable(tree.get("message_type"));

    messageTypeObj.ifPresentOrElse(
        messageTypeObj1 -> handleMessageTypes(messageTypeObj1, tree),
        () -> // Normal upstream message from a device client
        messageHandler.handleUpstreamMessage(tree));
  }

  private void handleMessageTypes(JsonNode messageTypeObj, JsonNode tree) {
    final String messageType = messageTypeObj.asText();
    log.info("Message Type : {}", messageType);
    switch (FcmMessageType.valueOf(messageType.toUpperCase(Locale.UK))) {
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
        errorHandlingStrategy.handleError(errorCode, tree);
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

  /** Handles a Control message from FCM */
  private void handleControlMessage(JsonNode jsonNode) {
    final String controlType = jsonNode.get("control_type").asText();

    if ("CONNECTION_DRAINING".equals(controlType)) {
      handleConnectionDraining();
    } else {
      log.info("Received unknown FCM Control message: {}", controlType);
    }
  }

  @SneakyThrows
  private void sendAck(JsonNode jsonMessage) {

    FcmAckMessage ackMessage =
        FcmAckMessage.builder()
            .messageId(jsonMessage.get("message_id").textValue())
            .to(jsonMessage.get("from").textValue())
            .build();
    fcmSender.send(ackMessage);
  }
}
