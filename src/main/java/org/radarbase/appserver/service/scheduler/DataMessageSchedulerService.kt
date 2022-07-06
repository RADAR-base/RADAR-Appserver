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
package org.radarbase.appserver.service.scheduler

import org.radarbase.appserver.entity.DataMessage
import org.radarbase.appserver.service.scheduler.quartz.SchedulerService
import org.radarbase.fcm.downstream.FcmSender
import org.radarbase.fcm.model.FcmDataMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import kotlin.random.Random

/**
 * [Service] for scheduling Data Messages to be sent through FCM at the [ ] time. It also provided functions for updating/ deleting
 * already scheduled Data Messsage Jobs.
 *
 * @author yatharthranjan
 */
@Service
class DataMessageSchedulerService(
    @Autowired @Qualifier("fcmSenderProps") fcmSender: FcmSender?,
    @Autowired schedulerService: SchedulerService?
) : MessageSchedulerService<DataMessage?>(fcmSender, schedulerService) {
    @Throws(Exception::class)
    override fun send(dataMessage: DataMessage?) {
        dataMessage?.let {
            fcmSender.send(createMessageFromDataMessage(it))
        }
    }

    companion object {
        private fun createMessageFromDataMessage(dataMessage: DataMessage): FcmDataMessage {
            val to = checkNotNull(
                dataMessage.fcmTopic ?: dataMessage.user?.fcmToken
            ) { "FCM Topic or User FCM Token is not set" }

            return FcmDataMessage(
                to = to,
                condition = dataMessage.fcmCondition,
                priority = dataMessage.priority,
                mutableContent = dataMessage.mutableContent,
                deliveryReceiptRequested = IS_DELIVERY_RECEIPT_REQUESTED,
                messageId = dataMessage.fcmMessageId,
                timeToLive = dataMessage.ttlSeconds,
                data = dataMessage.dataMap,
            )
        }
    }
}