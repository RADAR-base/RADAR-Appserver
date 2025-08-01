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

package org.radarbase.appserver.jersey.service.questionnaire_schedule

import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.entity.Message
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.fcm.downstream.FcmSender
import org.radarbase.appserver.jersey.service.quartz.MessageJob
import org.radarbase.appserver.jersey.service.quartz.MessageType
import org.radarbase.appserver.jersey.service.quartz.QuartzNamingStrategy
import org.radarbase.appserver.jersey.service.quartz.SchedulerService
import org.radarbase.appserver.jersey.service.quartz.SimpleQuartzNamingStrategy
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Date

class MessageSchedulerService<T : Message>(
    val fcmSender: FcmSender,
    val schedulerService: SchedulerService,
) {
    fun schedule(message: T) {
        logger.debug("Scheduling message with id {}", message.id)
        val jobDetail = getJobDetailForMessage(message, getMessageType(message))
        if (schedulerService.checkJobExists(jobDetail.key)) {
            logger.info("Job with key {} has been scheduled already", { jobDetail.key })
        } else {
            logger.debug("Job Detail = {}", jobDetail);
            val trigger = getTriggerForMessage(message, jobDetail)
            schedulerService.scheduleJob(jobDetail, trigger)
        }
    }

    fun scheduleMultiple(messages: List<T>) {
        val jobDetailSetMap: Map<JobDetail, Set<Trigger>> = buildMap {
            for (message in messages) {
                val jobDetail = getJobDetailForMessage(message, getMessageType(message))

                if (schedulerService.checkJobExists(jobDetail.key)) {
                    logger.info("Job with key {} is already scheduled", { jobDetail.key })
                    continue
                }

                setOf(getTriggerForMessage(message, jobDetail)).also { triggers ->
                    this.putIfAbsent(jobDetail, triggers)
                }
            }
        }
        logger.info("Scheduling {} messages", jobDetailSetMap.size)
        schedulerService.scheduleJobs(jobDetailSetMap)
    }

    fun updateScheduled(message: T) {
        val (messageId: Long, subjectId: String) = nonNullMessageIdAndSubjectId(message)
        val jobKeyString: String = NAMING_STRATEGY.getJobKeyName(
            subjectId, messageId.toString(),
        )
        val jobKey = JobKey(jobKeyString)
        val triggerKeyString: String =
            NAMING_STRATEGY.getTriggerName(
                subjectId, messageId.toString(),
            )
        val triggerKey = TriggerKey(triggerKeyString)
        val jobDataMap = JobDataMap()

        schedulerService.updateScheduledJob(jobKey, triggerKey, jobDataMap, message)
    }

    fun deleteScheduledMultiple(messages: List<T>) {
        messages.map {
            val (messageId: Long, subjectId: String) = nonNullMessageIdAndSubjectId(it)
            JobKey(NAMING_STRATEGY.getJobKeyName(subjectId, messageId.toString()))
        }
            .let(schedulerService::deleteScheduledJobs)
    }

    fun deleteScheduled(message: T) {
        JobKey(NAMING_STRATEGY.getJobKeyName(message.user!!.subjectId!!, message.id.toString()))
            .let(schedulerService::deleteScheduledJob)
    }

    fun getMessageType(message: T): MessageType = when (message) {
        is Notification -> MessageType.NOTIFICATION
        is DataMessage -> MessageType.DATA
        else -> MessageType.UNKNOWN
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MessageSchedulerService::class.java)

        val NAMING_STRATEGY: QuartzNamingStrategy = SimpleQuartzNamingStrategy()

        /**
         * Build a Quartz [Trigger] that will fire exactly once at the message’s scheduled time.
         *
         * @param message      the [Message] whose scheduling fields must be non-null
         * @param jobDetail    the [JobDetail] to which this trigger will be bound
         * @return a one-shot [Trigger] that starts at `message.scheduledTime`
         * @throws IllegalArgumentException if any of `message.id`, `message.user?.subjectId` or
         *                                  `message.scheduledTime` is null
         */
        fun getTriggerForMessage(message: Message, jobDetail: JobDetail): Trigger {
            val (messageId: Long, subjectId: String, scheduledTime: Instant) = nonNullTriggerUtils(message)

            return TriggerBuilder.newTrigger()
                .withIdentity(
                    TriggerKey(
                        NAMING_STRATEGY.getTriggerName(
                            subjectId, messageId.toString(),
                        ),
                    ),
                )
                .forJob(jobDetail)
                .startAt(Date(scheduledTime.toEpochMilli()))
                .withSchedule(
                    SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)
                        .withIntervalInMilliseconds(0)
                        .withMisfireHandlingInstructionFireNow(),
                )
                .build()
        }

        /**
         * Build a Quartz [JobDetail] that carries the message payload.
         *
         * @param message      the [Message] whose fields must be non-null
         * @param messageType  the type of the message
         * @return a durable [JobDetail] with its [JobDataMap] populated from `message` and `messageType`
         * @throws IllegalArgumentException if any of `message.id`, `message.user?.subjectId`,
         *                                  `message.user?.project?.projectId` is null
         */
        fun getJobDetailForMessage(message: Message, messageType: MessageType): JobDetail {
            val (messageId: Long, subjectId: String, projectId: String) = nonNullJobUtils(message)

            val dataMap = JobDataMap(
                mapOf(
                    "subjectId" to subjectId,
                    "projectId" to projectId,
                    "messageId" to messageId,
                    "messageType" to messageType.toString(),
                ),
            )

            return JobBuilder.newJob(MessageJob::class.java)
                .withIdentity(
                    JobKey(
                        NAMING_STRATEGY.getJobKeyName(
                            subjectId, messageId.toString(),
                        ),
                    ),
                )
                .withDescription("Send message at scheduled time...")
                .setJobData(dataMap)
                .storeDurably(true)
                .build()
        }

        /**
         * Extract and validate the three mandatory scheduling fields from [message].
         *
         * @return a [Triple] of `(messageId, subjectId, scheduledTime)`
         * @throws IllegalArgumentException if any required field is null
         */
        fun nonNullTriggerUtils(message: Message): Triple<Long, String, Instant> {
            val (messageId: Long, subjectId: String) = nonNullMessageIdAndSubjectId(message)
            val scheduledTime: Instant = requireNotNull(message.scheduledTime) { "Scheduled time cannot be null" }

            return Triple(messageId, subjectId, scheduledTime)
        }

        /**
         * Extract and validate the three mandatory job-creation fields from [message].
         *
         * @return a [Triple] of `(messageId, subjectId, projectId)`
         * @throws IllegalArgumentException if any required field is null
         */
        fun nonNullJobUtils(message: Message): Triple<Long, String, String> {
            val (messageId: Long, subjectId: String) = nonNullMessageIdAndSubjectId(message)

            val user: User = requireNotNull(message.user) { "User for message cannot be null" }
            val projectId: String = requireNotNull(
                requireNotNull(user.project) { "Project for user in message cannot be null" }
                    .projectId,
            ) { "Project Id for user in message cannot be null" }

            return Triple(messageId, subjectId, projectId)
        }

        /**
         * Extract and validate the two mandatory common fields from [message].
         *
         * @return a [Pair] of `(messageId, subjectId)`
         * @throws IllegalArgumentException if `message.id` or `message.user?.subjectId` is null
         */
        fun nonNullMessageIdAndSubjectId(message: Message): Pair<Long, String> {
            val messageId: Long = requireNotNull(message.id) { "Message Id cannot be null" }
            val subjectId: String = requireNotNull(
                requireNotNull(message.user) { "User for message cannot be null" }.subjectId,
            ) { "Subject Id in message cannot be null" }

            return Pair(messageId, subjectId)
        }

    }
}

