package org.radarbase.appserver.jersey.service.questionnaire_schedule.task

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.AssessmentType
import org.radarbase.appserver.jersey.entity.Task
import java.time.Instant

class TaskGeneratorService {
    fun buildTask(assessment: Assessment, timestamp: Instant, completionWindow: Long): Task {
        val isClinical = assessment.type == AssessmentType.CLINICAL
        return Task.TaskBuilder().apply {
            name(assessment.name)
            type(assessment.type)
            estimatedCompletionTime(assessment.estimatedCompletionTime!!)
            completionWindow(completionWindow)
            priority(assessment.order)
            timestamp(timestamp)
            showInCalendar(assessment.showInCalendar)
            isDemo(assessment.isDemo)
            nQuestions(assessment.nQuestions)
            isClinical(isClinical)
        }.build()
    }
}
