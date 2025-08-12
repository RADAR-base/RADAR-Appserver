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

package org.radarbase.appserver.jersey.event.listener.quartz

import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
import org.radarbase.appserver.jersey.event.state.MessageState
import org.radarbase.appserver.jersey.event.state.dto.DataMessageStateEventDto
import org.radarbase.appserver.jersey.event.state.dto.NotificationStateEventDto
import org.radarbase.appserver.jersey.repository.DataMessageRepository
import org.radarbase.appserver.jersey.repository.NotificationRepository
import org.radarbase.appserver.jersey.service.quartz.MessageType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

class QuartzMessageJobListener @Inject constructor(
    private val messageStateEventPublisher: EventBus,
    private val notificationRepository: NotificationRepository,
    private val dataMessageRepository: DataMessageRepository,
) : JobListener {
    /**
     * Get the name of the `JobListener`.
     */
    override fun getName(): String {
        return javaClass.getName()
    }

    /**
     * Called by the `[org.quartz.Scheduler]` when a `[org.quartz.JobDetail]` is about to
     * be executed (an associated `[org.quartz.Trigger]` has occurred).
     *
     *
     * This method will not be invoked if the execution of the Job was vetoed by a `TriggerListener`.
     *
     * @see .jobExecutionVetoed
     */
    override fun jobToBeExecuted(context: JobExecutionContext?) {
        // Not implemented
    }

    /**
     * Called by the `[org.quartz.Scheduler]` when a `[org.quartz.JobDetail]` was about to
     * be executed (an associated `[org.quartz.Trigger]` has occurred), but a `TriggerListener` vetoed its execution.
     *
     * @see .jobToBeExecuted
     */
    override fun jobExecutionVetoed(context: JobExecutionContext?) {
        // Not implemented
    }

    /**
     * Called by the `[org.quartz.Scheduler]` after a `[org.quartz.JobDetail]` has been
     * executed, and be for the associated `Trigger`'s `triggered(xx)` method
     * has been called.
     */
    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
        val jobDataMap = context.mergedJobDataMap
        val messageId = jobDataMap.getLongValue("messageId")
        val messageType = jobDataMap.getString("messageType") ?: run {
            log.warn("Message type does not exist.")
            return
        }

        val type = MessageType.valueOf(messageType)
        when (type) {
            MessageType.NOTIFICATION -> {
                val notification = runBlocking {
                    notificationRepository.find(messageId)
                } ?: run {
                    log.warn("The notification does not exist in database and yet was scheduled.")
                    return
                }
                if (jobException != null) {
                    val additionalInfo: MutableMap<String, String> = hashMapOf()
                    additionalInfo.put("error", jobException.message!!)
                    additionalInfo.put("error_description", jobException.toString())
                    val notificationStateEventError = NotificationStateEventDto(
                        notification, MessageState.ERRORED, additionalInfo, Instant.now(),
                    )
                    messageStateEventPublisher.post(notificationStateEventError)

                    log.warn("The job could not be executed.", jobException)
                    return
                }

                val notificationStateEvent = NotificationStateEventDto(
                    notification, MessageState.EXECUTED, null, Instant.now(),
                )
                messageStateEventPublisher.post(notificationStateEvent)
            }

            MessageType.DATA -> {
                val dataMessage = runBlocking {
                    dataMessageRepository.find(messageId)
                } ?: run {
                    log.warn("The data message does not exist in database and yet was scheduled.")
                    return
                }

                if (jobException != null) {
                    val additionalInfo: MutableMap<String, String> = hashMapOf()
                    additionalInfo.put("error", jobException.message!!)
                    additionalInfo.put("error_description", jobException.toString())
                    val dataMessageStateEventError = DataMessageStateEventDto(
                        dataMessage, MessageState.ERRORED, additionalInfo, Instant.now(),
                    )
                    messageStateEventPublisher.post(dataMessageStateEventError)

                    log.warn("The job could not be executed.", jobException)
                    return
                }

                val dataMessageStateEvent = DataMessageStateEventDto(
                    dataMessage, MessageState.EXECUTED, null, Instant.now(),
                )
                messageStateEventPublisher.post(dataMessageStateEvent)
            }

            MessageType.UNKNOWN -> log.warn("The message type does not exist.")
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(QuartzMessageJobListener::class.java)
    }
}
