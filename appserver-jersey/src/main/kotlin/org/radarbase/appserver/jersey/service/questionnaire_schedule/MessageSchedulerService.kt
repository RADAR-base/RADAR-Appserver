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
import org.radarbase.appserver.jersey.fcm.downstream.FcmSender
import org.radarbase.appserver.jersey.service.quartz.MessageJob
import org.radarbase.appserver.jersey.service.quartz.MessageType
import org.radarbase.appserver.jersey.service.quartz.QuartzNamingStrategy
import org.radarbase.appserver.jersey.service.quartz.SchedulerService
import org.radarbase.appserver.jersey.service.quartz.SimpleQuartzNamingStrategy
import org.slf4j.LoggerFactory
import java.util.Date

class MessageSchedulerService<T : Message>(
    val fcmSender: FcmSender,
    val schedulerService: SchedulerService,
) {
    fun schedule(message: T) {
        val jobDetail = getJobDetailForMessage(message, getMessageType(message))
        if (schedulerService.checkJobExists(jobDetail.key)) {
            println("Job has been scheduled already.")
        } else {
            val trigger = getTriggerForMessage(message, jobDetail)
            schedulerService.scheduleJob(jobDetail, trigger)
        }
    }

    fun scheduleMultiple(messages: List<T>) {
        val jobDetailSetMap = mutableMapOf<JobDetail, Set<Trigger>>()
        for (message in messages) {
            val jobDetail = getJobDetailForMessage(message, getMessageType(message))

            if (schedulerService.checkJobExists(jobDetail.key)) {
                continue
            }
            val triggerSet = setOf(getTriggerForMessage(message, jobDetail))
            jobDetailSetMap[jobDetail] = triggerSet
        }
        schedulerService.scheduleJobs(jobDetailSetMap.toMap())
    }

    fun updateScheduled(message: T) {
        val jobKeyString: String =
            NAMING_STRATEGY.getJobKeyName(
                message.user!!.subjectId!!, message.id.toString(),
            )
        val jobKey = JobKey(jobKeyString)
        val triggerKeyString: String =
            NAMING_STRATEGY.getTriggerName(
                message.user!!.subjectId!!, message.id.toString(),
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

        private val log = LoggerFactory.getLogger(MessageSchedulerService::class.java)

        // TODO add a schedule cache to cache incoming requests
        val NAMING_STRATEGY: QuartzNamingStrategy = SimpleQuartzNamingStrategy()

        fun getTriggerForMessage(message: Message, jobDetail: JobDetail): Trigger {
            val triggerKey = TriggerKey(
                NAMING_STRATEGY.getTriggerName(
                    message.user!!.subjectId!!, message.id.toString(),
                ),
            )

            return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(
                    NAMING_STRATEGY.getJobKeyName(
                        message.user!!.subjectId!!, message.id.toString(),
                    ),
                )
                .startAt(Date(message.scheduledTime!!.toEpochMilli()))
                .withSchedule(
                    SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)
                        .withIntervalInMilliseconds(0)
                        .withMisfireHandlingInstructionFireNow(),
                )
                .build()
        }

        fun getJobDetailForMessage(message: Message, messageType: MessageType): JobDetail {
            val jobKey = JobKey(
                NAMING_STRATEGY.getJobKeyName(
                    message.user!!.subjectId!!, message.id.toString(),
                ),
            )

            val dataMap = JobDataMap(
                mapOf(
                    "subjectId" to message.user!!.subjectId,
                    "projectId" to message.user!!.project!!.projectId,
                    "messageId" to message.id,
                    "messageType" to messageType.toString(),
                ),
            )

            return JobBuilder.newJob(MessageJob::class.java)
                .withIdentity(jobKey)
                .withDescription("Send message at scheduled time...")
                .setJobData(dataMap)
                .storeDurably(true) // equivalent to setDurability(true)
                .build()
        }
    }
}

