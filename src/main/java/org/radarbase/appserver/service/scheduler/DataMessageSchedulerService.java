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

package org.radarbase.appserver.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Message;
import org.radarbase.appserver.service.scheduler.quartz.*;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.model.FcmDataMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * {@link Service} for scheduling Notifications to be sent through FCM at the {@link
 * org.radarbase.appserver.entity.Scheduled} time. It also provided functions for updating/ deleting
 * already scheduled Notification Jobs.
 *
 * @author yatharthranjan
 */
@Service
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class DataMessageSchedulerService extends MessageSchedulerService {

  // TODO add a schedule cache to cache incoming requests and do batch scheduling

  public DataMessageSchedulerService(
      @Autowired @Qualifier("fcmSenderProps") FcmSender fcmSender,
      @Autowired SchedulerService schedulerService) {
    super(fcmSender, schedulerService);
  }

  public void scheduleDataMessage(DataMessage dataMessage) {
    super.scheduleMessage(dataMessage);
  }

  public void scheduleDataMessages(List<DataMessage> dataMessages) {
    super.scheduleMessages(getMessagesFromDataMessages(dataMessages));
  }

  public void updateScheduledDataMessage(DataMessage dataMessage) {
    super.updateScheduledMessage(dataMessage);
  }

  public void deleteScheduledDataMessage(DataMessage dataMessage) {
    super.deleteScheduledMessage(dataMessage);
  }

  public void deleteScheduledDataMessages(List<DataMessage> dataMessages) {
    super.deleteScheduledMessages(getMessagesFromDataMessages(dataMessages));
  }

  public List<Message> getMessagesFromDataMessages(List<DataMessage> dataMessages){
    List<Message> messages = new ArrayList<>();
    messages.addAll(dataMessages);
    return messages;
  }

  private static Map getDataMap(DataMessage dataMessage) {
    Map<String, Object> dataMap = new HashMap<>();

    for (Map.Entry<String,String> entry : dataMessage.getDataMap().entrySet())
      putIfNotNull(dataMap, entry.getKey(), entry.getValue());

    return dataMap;
  }

  private static FcmDataMessage createMessageFromDataMessage(DataMessage dataMessage) {

    String to =
            Objects.requireNonNullElseGet(
                    dataMessage.getFcmTopic(), dataMessage.getUser()::getFcmToken);
    return FcmDataMessage.builder()
            .to(to)
            .condition(dataMessage.getFcmCondition())
            .priority(dataMessage.getPriority())
            .mutableContent(dataMessage.isMutableContent())
            .deliveryReceiptRequested(IS_DELIVERY_RECEIPT_REQUESTED)
            .messageId(String.valueOf(dataMessage.getFcmMessageId()))
            .timeToLive(Objects.requireNonNullElse(dataMessage.getTtlSeconds(), 2_419_200))
            .data(getDataMap(dataMessage))
            .build();
  }

  public void sendDataMessage(DataMessage dataMessage) throws Exception {
    super.sendMessage(createMessageFromDataMessage(dataMessage));
  }

}
