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

import com.google.firebase.messaging.FirebaseMessagingException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.service.scheduler.quartz.SchedulerService;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.model.FcmDataMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * {@link Service} for scheduling Data Messages to be sent through FCM at the {@link
 * org.radarbase.appserver.entity.Scheduled} time. It also provided functions for updating/ deleting
 * already scheduled Data Messsage Jobs.
 *
 * @author yatharthranjan
 */
@Service
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class DataMessageSchedulerService extends MessageSchedulerService<DataMessage> {

    public DataMessageSchedulerService(
            @Autowired @Qualifier("fcmSenderProps") FcmSender fcmSender,
            @Autowired SchedulerService schedulerService) {
        super(fcmSender, schedulerService);
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
                .data(dataMessage.getDataMap())
                .build();
    }

    public void send(DataMessage dataMessage) throws Exception {
        try {
            fcmSender.send(createMessageFromDataMessage(dataMessage));
        } catch (FirebaseMessagingException exc) {
            log.error("Error occurred when sending downstream message.", exc);
            // TODO: update the data message status using event
            handleErrorCode(exc.getErrorCode());
            handleFCMErrorCode(exc.getMessagingErrorCode());
        }
    }
}
