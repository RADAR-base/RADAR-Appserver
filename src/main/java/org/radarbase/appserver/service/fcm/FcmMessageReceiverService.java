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

package org.radarbase.appserver.service.fcm;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.exception.InvalidNotificationDetailsException;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.fcm.common.ObjectMapperFactory;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.dto.FcmNotificationDto;
import org.radarbase.fcm.upstream.UpstreamMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@Slf4j
public class FcmMessageReceiverService implements UpstreamMessageHandler {

    @Autowired
    private ObjectMapperFactory mapperFactory;

    @Autowired
    @Qualifier("fcmSenderProps")
    private FcmSender fcmSender;

    @Autowired
    private FcmNotificationService notificationService;

    @Override
    public void handleUpstreamMessage(JsonNode jsonMessage) {
        log.info("Normal Message: {}", jsonMessage.toString());

        Optional<JsonNode> jsonData = Optional.ofNullable(jsonMessage.get("data"));

        jsonData.ifPresentOrElse(data -> {

            Optional<JsonNode> action = Optional.ofNullable(jsonData.get().get("action"));

            action.ifPresentOrElse(act -> {
                switch (Action.valueOf(act.asText())) {
                    case ECHO:
                        log.info("Got an ECHO request");
                        break;

                    case SCHEDULE:
                        log.info("Got a SCHEDULE Request");
                        notificationService.addNotificationForced(notificationDtoMapper(
                                jsonData.get()), jsonMessage.get("from").asText(),
                                jsonMessage.get("subjectId") == null ? "unknown-user" : jsonMessage.get("subjectId").asText(),
                                jsonMessage.get("projectId") == null ? "unknown-project" : jsonMessage.get("projectId").asText());
                        break;

                    case CANCEL:
                        log.info("Got a CANCEL Request");
                        break;
                }
            }, () -> {
                log.warn("No Action provided");
                throw new IllegalStateException("Action must not be null! Options: 'ECHO', 'SCHEDULE', 'CANCEL'");
            });
                }, () ->  {
                log.warn("No Data provided");
                throw new IllegalStateException("Data must not be null!");
            }
        );
    }

    @Override
    public void handleAckReceipt(JsonNode jsonMessage) {
        log.info("Ack Receipt: {}", jsonMessage.toString());
    }

    @Override
    public void handleNackReceipt(JsonNode jsonMessage) {
        log.info("Nack Receipt: {}", jsonMessage.toString());
    }

    @Override
    public void handleStatusReceipt(JsonNode jsonMessage) {
        log.info("Status Receipt: {}", jsonMessage.toString());
        Optional<JsonNode> jsonData = Optional.ofNullable(jsonMessage.get("data"));
         if(jsonData.isPresent()) {
             Optional<String> messageStatus = Optional.ofNullable(jsonData.get().get("message_status").asText());
             if(messageStatus.isPresent()){
                if(messageStatus.get().equals("MESSAGE_SENT_TO_DEVICE")) {
                    notificationService.deleteNotificationByFcmMessageId(jsonData.get().get("original_message_id").asText());
                }
             }
         }
    }

    @Override
    public void handleControlMessage(JsonNode jsonMessage) {
        log.info("Control Message: {}", jsonMessage.toString());
    }

    @Override
    public void handleOthers(JsonNode jsonMessage) {
        log.debug("Message Type not recognised {}", jsonMessage.toString());
    }

    @SneakyThrows
    private FcmNotificationDto notificationDtoMapper(JsonNode jsonMessage) {

        if(jsonMessage.get("notificationTitle") == null
                || jsonMessage.get("notificationMessage") == null || jsonMessage.get("time") == null) {
            throw new InvalidNotificationDetailsException("The notifications details are invalid: " + jsonMessage);
        }

        LocalDateTime scheduledTime = LocalDateTime.ofEpochSecond(jsonMessage.get("time").asLong()/1000L
                , 0, ZoneOffset.UTC);

        if(scheduledTime.isBefore(LocalDateTime.now())) {
            throw new InvalidNotificationDetailsException("The notification scheduled time cannot be before current time");
        }

        return new FcmNotificationDto().setTitle(jsonMessage.get("notificationTitle").asText())
                .setBody(jsonMessage.get("notificationMessage").asText())
                .setScheduledTime(scheduledTime)
                .setAppPackage(jsonMessage.get("appPackage") == null ? "unknown" : jsonMessage.get("appPackage").asText())
                .setDelivered(false)
                .setSourceId(jsonMessage.get("sourceId") == null ? "unknown" : jsonMessage.get("sourceId").asText())
                .setSourceType(jsonMessage.get("sourceType") == null ? "unknown" : jsonMessage.get("sourceType").asText())
                .setTtlSeconds(jsonMessage.get("ttlSeconds") == null ? 86400 : jsonMessage.get("ttlSeconds").asInt())
                .setType(jsonMessage.get("type") == null ? "Unknown" : jsonMessage.get("type").asText());
    }
}
