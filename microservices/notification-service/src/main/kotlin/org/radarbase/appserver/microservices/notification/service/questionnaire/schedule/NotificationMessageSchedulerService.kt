/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.microservices.notification.service.questionnaire.schedule

import jakarta.inject.Inject
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.radarbase.appserver.microservices.core.entity.Message
import org.radarbase.appserver.microservices.core.entity.Notification
import org.radarbase.appserver.microservices.core.fcm.downstream.FcmSender
import org.radarbase.appserver.microservices.core.service.quartz.MessageType
import org.radarbase.appserver.microservices.core.service.quartz.NotificationJob
import org.radarbase.appserver.microservices.core.service.quartz.SchedulerService
import org.radarbase.appserver.microservices.core.service.questionnaire.schedule.MessageSchedulerService
import org.radarbase.appserver.microservices.notification.service.quartz.NotificationJobImpl

class NotificationMessageSchedulerService @Inject constructor(
    override val fcmSender: FcmSender,
    override val schedulerService: SchedulerService,
) : MessageSchedulerService<Notification>() {
    /**
     * Build a Quartz [JobDetail] that carries the message payload.
     *
     * @param message      the [Message] whose fields must be non-null
     * @param messageType  the type of the message
     * @return a durable [JobDetail] with its [JobDataMap] populated from `message` and `messageType`
     * @throws IllegalArgumentException if any of `message.id`, `message.user?.subjectId`,
     *                                  `message.user?.project?.projectId` is null
     */
    override fun getJobDetailForMessage(message: Message, messageType: MessageType): JobDetail {
        val (messageId: Long, subjectId: String, projectId: String) = nonNullJobUtils(message)

        val dataMap = JobDataMap(
            mapOf(
                "subjectId" to subjectId,
                "projectId" to projectId,
                "messageId" to messageId,
                "messageType" to messageType.toString(),
            ),
        )

        return when (messageType) {
            MessageType.NOTIFICATION -> JobBuilder.newJob(NotificationJobImpl::class.java)
                .withIdentity(
                    JobKey(
                        NAMING_STRATEGY.getJobKeyName(
                            subjectId,
                            messageId.toString(),
                        ),
                    ),
                )
                .withDescription("Send message at scheduled time...")
                .setJobData(dataMap)
                .storeDurably(true)
                .build()

            else -> throw IllegalStateException("Unexpected message type $messageType")
        }
    }

}
