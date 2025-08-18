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

import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.radarbase.appserver.entity.DataMessage
import org.radarbase.appserver.entity.Message
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.service.MessageType
import org.radarbase.appserver.service.scheduler.quartz.MessageJob
import org.radarbase.appserver.service.scheduler.quartz.QuartzNamingStrategy
import org.radarbase.appserver.service.scheduler.quartz.SchedulerService
import org.radarbase.appserver.service.scheduler.quartz.SimpleQuartzNamingStrategy
import org.radarbase.fcm.downstream.FcmSender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean
import org.springframework.stereotype.Service
import java.util.Date

@Service
@Suppress("unused")
class MessageSchedulerService<T : Message>(
    @param:Qualifier("fcmSenderProps") val fcmSender: FcmSender?,
    val schedulerService: SchedulerService,
) {
    fun schedule(message: T) {
        val jobDetail = getJobDetailForMessage(message, getMessageType(message)).`object`
        if (jobDetail != null) {
            if (schedulerService.checkJobExists(jobDetail.key)) {
                println("Job has been scheduled already.")
            } else {
                val trigger = getTriggerForMessage(message, jobDetail).`object`!!
                schedulerService.scheduleJob(jobDetail, trigger)
            }
        }
    }

    fun scheduleMultiple(messages: List<T>) {
        val jobDetailSetMap = mutableMapOf<JobDetail, Set<Trigger>>()
        for (message in messages) {
            val jobDetail = getJobDetailForMessage(message, getMessageType(message)).`object`

            if (jobDetail != null) {
                if (schedulerService.checkJobExists(jobDetail.key)) {
                    continue
                }
                val triggerSet = setOf(getTriggerForMessage(message, jobDetail).`object`!!)
                jobDetailSetMap[jobDetail] = triggerSet
            }
        }
        schedulerService.scheduleJobs(jobDetailSetMap.toMap())
    }

    fun updateScheduled(message: T) {
        val jobKeyString: String =
            NAMING_STRATEGY.getJobKeyName(
                message.user!!.subjectId!!,
                message.id.toString(),
            )
        val jobKey = JobKey(jobKeyString)
        val triggerKeyString: String =
            NAMING_STRATEGY.getTriggerName(
                message.user!!.subjectId!!,
                message.id.toString(),
            )
        val triggerKey = TriggerKey(triggerKeyString)
        val jobDataMap = JobDataMap()

        schedulerService.updateScheduledJob(jobKey, triggerKey, jobDataMap, message)
    }

    fun deleteScheduledMultiple(messages: List<T>) {
        val keys = messages.map {
            JobKey(NAMING_STRATEGY.getJobKeyName(it.user!!.subjectId!!, it.id.toString()))
        }
        schedulerService.deleteScheduledJobs(keys)
    }

    fun deleteScheduled(message: T) {
        val key = JobKey(NAMING_STRATEGY.getJobKeyName(message.user!!.subjectId!!, message.id.toString()))
        schedulerService.deleteScheduledJob(key)
    }

    fun getMessageType(message: T): MessageType = when (message) {
        is Notification -> MessageType.NOTIFICATION
        is DataMessage -> MessageType.DATA
        else -> MessageType.UNKNOWN
    }

    companion object {

        private val log: Logger = LoggerFactory.getLogger(MessageSchedulerService::class.java)

        // TODO add a schedule cache to cache incoming requests
        val NAMING_STRATEGY: QuartzNamingStrategy = SimpleQuartzNamingStrategy()

        fun getTriggerForMessage(message: Message, jobDetail: JobDetail): SimpleTriggerFactoryBean {
            return SimpleTriggerFactoryBean().apply {
                this.setJobDetail(jobDetail)
                this.setName(
                    NAMING_STRATEGY.getTriggerName(
                        message.user!!.subjectId!!,
                        message.id.toString(),
                    ),
                )
                this.setRepeatCount(0)
                this.setRepeatInterval(0L)
                this.setStartTime(Date(message.scheduledTime!!.toEpochMilli()))
                this.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)
                this.afterPropertiesSet()
            }
        }

        fun getJobDetailForMessage(message: Message, messageType: MessageType): JobDetailFactoryBean {
            return JobDetailFactoryBean().apply {
                setJobClass(MessageJob::class.java)
                setDescription("Send message at scheduled time...")
                setDurability(true)
                this.setName(
                    NAMING_STRATEGY.getJobKeyName(
                        message.user!!.subjectId!!,
                        message.id.toString(),
                    ),
                )
                val map = hashMapOf(
                    "subjectId" to message.user!!.subjectId,
                    "projectId" to message.user!!.project!!.projectId,
                    "messageId" to message.id,
                    "messageType" to messageType.toString(),
                )
                setJobDataAsMap(map)
                afterPropertiesSet()
            }
        }
    }
}
